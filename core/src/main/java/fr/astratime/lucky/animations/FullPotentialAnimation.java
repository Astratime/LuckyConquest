package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.entities.FullPotential;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Un As joué à plein potentiel (voir {@link FullPotential}) : une courte
 * animation en pixel art, sans texte, au milieu de l'écran (Astra 2026-11-14).
 * <ul>
 *   <li>Coeur (Sang au maximum) : un grand coeur bat deux fois en se remplissant
 *       de sang, puis éclate en gouttes qui filent vers l'ennemi ;</li>
 *   <li>Pique (10 Lames ou plus) : les Lames s'ouvrent en éventail autour d'un
 *       pique géant puis plongent ensemble sur l'ennemi, qui est tranché en X ;</li>
 *   <li>Trèfle (Bingo garanti) : un trèfle d'or tourne, ses quatre feuilles
 *       s'ouvrent et une étoile filante en jaillit vers la machine ;</li>
 *   <li>Carreau (Coffre plein) : un coffre en losange tremble, ses verrous
 *       sautent un par un, puis il s'ouvre sur une pluie de diamants.</li>
 * </ul>
 * Elle ne bloque pas le jeu : les clics passent au travers, et un clic la fait
 * disparaître ({@link #skip()}). Elle suit la vitesse des animations (le Stage
 * avance plus vite) ; avec les effets réduits, ni flash ni tremblement d'écran,
 * et moins de particules.
 */
public class FullPotentialAnimation extends Actor implements Disposable {

    /** Durée de l'animation (à la vitesse x1). */
    public static final float DURATION = 1.8f;
    /** Fondu de la fin. */
    private static final float FADE_TIME = 0.3f;

    private static final float SPRITE_SCALE = 8f;  // taille d'un pixel des grands motifs
    private static final float SMALL_SCALE  = 5f;  // lames, gouttes, diamants, verrous

    private static final Color OUTLINE = Palette.OUTLINE;

    private final Array<Texture> textures = new Array<>();
    private final TextureRegion pixel;
    private final TextureRegion heart, heartEmpty, drop;
    private final TextureRegion spade, blade;
    private final TextureRegion clover, leaf, star;
    private final TextureRegion vault, bolt, gem;
    private final TextureRegion halo;

    private FullPotential kind;
    private float   time = -1f;
    private float   centerX, centerY, targetX, targetY;
    private boolean reduced;
    private boolean impactDone;
    private Runnable onImpact = () -> { };

    /** Particules (gouttes, diamants) : position de départ, direction et rotation tirées au lancement. */
    private static final int MAX_PARTICLES = 36;
    private final float[] angles = new float[MAX_PARTICLES];
    private final float[] speeds = new float[MAX_PARTICLES];
    private final float[] spins  = new float[MAX_PARTICLES];
    private int particles;

    public FullPotentialAnimation() {
        setTouchable(Touchable.disabled);
        pixel      = region(whitePixel());
        heart      = region(heart(false));
        heartEmpty = region(heart(true));
        drop       = region(grid(DROP, "o#l", OUTLINE, hex("c8102e"), hex("ff7a8a")));
        spade      = region(spade());
        blade      = region(grid(BLADE, "o#lgh", OUTLINE, hex("c9d3dc"), hex("ffffff"), hex("8a5a2b"), hex("e8b84a")));
        clover     = region(clover());
        leaf       = region(grid(LEAF, "o#l", OUTLINE, hex("f2b632"), hex("fff0a0")));
        star       = region(grid(STAR, "o#l", OUTLINE, hex("ffd84a"), hex("fffbe0")));
        vault      = region(vault());
        bolt       = region(grid(BOLT, "o#l", OUTLINE, hex("b8c2cc"), hex("f4f8fb")));
        gem        = region(grid(GEM, "o#ld", OUTLINE, hex("4fd6ff"), hex("e6fbff"), hex("1d8fc4")));
        halo       = region(halo(48));
    }

    /** @param onImpact lancé à l'instant fort de l'animation (tremblement d'écran, par exemple) */
    public void setOnImpact(Runnable onImpact) { this.onImpact = onImpact == null ? () -> { } : onImpact; }

    /**
     * Joue l'animation de {@code kind} au milieu de l'écran {@code center} ;
     * {@code target} est l'ennemi (Coeur, Pique) ou la machine (Trèfle).
     */
    public void play(FullPotential kind, Vector2 center, Vector2 target, boolean reducedEffects) {
        this.kind    = kind;
        this.reduced = reducedEffects;
        centerX = center.x;
        centerY = center.y;
        targetX = target.x;
        targetY = target.y;
        particles = reducedEffects ? MAX_PARTICLES / 2 : MAX_PARTICLES;
        for (int i = 0; i < MAX_PARTICLES; i++) {
            angles[i] = MathUtils.random(360f);
            speeds[i] = MathUtils.random(0.55f, 1f);
            spins[i]  = MathUtils.random(-540f, 540f);
        }
        impactDone = false;
        time = 0f;
        toFront();
    }

    /** @return {@code true} pendant l'animation. */
    public boolean isPlaying() { return time >= 0f; }

    /** Fait disparaître l'animation (clic du joueur, nouveau combat). */
    public void skip() { time = -1f; }

    /** @return l'instant fort de l'animation de {@code kind} (coup, éclat, ouverture). */
    static float impactTime(FullPotential kind) {
        return switch (kind) {
            case HEARTS   -> 1.0f;
            case SPADES   -> 1.15f;
            case CLUBS    -> 0.95f;
            case DIAMONDS -> 0.95f;
        };
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (time < 0f) return;
        time += delta;
        if (!impactDone && time >= impactTime(kind)) {
            impactDone = true;
            onImpact.run();
        }
        if (time > DURATION) time = -1f;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (time < 0f) return;
        Color saved = batch.getColor().cpy();
        float alpha = parentAlpha * (time < DURATION - FADE_TIME ? 1f : Math.max(0f, (DURATION - time) / FADE_TIME));
        switch (kind) {
            case HEARTS   -> drawHearts(batch, alpha);
            case SPADES   -> drawSpades(batch, alpha);
            case CLUBS    -> drawClubs(batch, alpha);
            case DIAMONDS -> drawDiamonds(batch, alpha);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(saved);
    }

    // ------------------------------------------------------------------ Coeur

    /** Le coeur surgit, bat deux fois en se remplissant de sang, enfle puis éclate en gouttes vers l'ennemi. */
    private void drawHearts(Batch batch, float alpha) {
        float burst = impactTime(FullPotential.HEARTS);
        if (time < burst) {
            float appear = Interpolation.swingOut.apply(Math.min(1f, time / 0.25f));
            float beat = beat(time, 0.4f) * 0.14f + beat(time, 0.7f) * 0.2f;
            float swell = time > burst - 0.15f ? (time - (burst - 0.15f)) / 0.15f * 0.35f : 0f;
            float scale = SPRITE_SCALE * (appear + beat + swell);
            float shake = swell > 0f ? 7f * swell : 0f;
            float x = centerX + MathUtils.random(-shake, shake), y = centerY + MathUtils.random(-shake, shake);
            glow(batch, x, y, scale * 40f, 1f, 0.25f, 0.35f, alpha * (0.35f + beat * 2f));
            batch.setColor(1f, 1f, 1f, alpha);
            drawCentered(batch, heartEmpty, x, y, scale, 0f);
            // Le sang monte du bas vers le haut : la partie pleine du coeur est découpée.
            float fill = Interpolation.pow2Out.apply(MathUtils.clamp((time - 0.15f) / 0.65f, 0f, 1f));
            int h = heart.getRegionHeight(), rows = Math.round(h * fill);
            if (rows > 0) {
                TextureRegion part = new TextureRegion(heart, 0, h - rows, heart.getRegionWidth(), rows);
                float w = heart.getRegionWidth() * scale, full = h * scale;
                batch.draw(part, x - w / 2f, y - full / 2f, w, rows * scale);
            }
            return;
        }
        float since = time - burst;
        flash(batch, since, 1f, 0.4f, 0.45f, alpha);
        // Les gouttes jaillissent en couronne, puis filent vers l'ennemi.
        batch.setColor(1f, 1f, 1f, alpha);
        for (int i = 0; i < particles; i++) {
            float out = Interpolation.pow2Out.apply(Math.min(1f, since / 0.3f)) * 220f * speeds[i];
            float bx = centerX + MathUtils.cosDeg(angles[i]) * out, by = centerY + MathUtils.sinDeg(angles[i]) * out;
            float home = Interpolation.pow3In.apply(MathUtils.clamp((since - 0.2f - i * 0.006f) / 0.45f, 0f, 1f));
            float x = MathUtils.lerp(bx, targetX, home), y = MathUtils.lerp(by, targetY, home);
            if (home >= 1f) continue; // la goutte a atteint l'ennemi
            float angle = MathUtils.atan2(targetY - y, targetX - x) * MathUtils.radiansToDegrees - 90f;
            drawCentered(batch, drop, x, y, SMALL_SCALE * (1f - 0.4f * home), home > 0f ? angle : 0f);
        }
    }

    // ------------------------------------------------------------------ Pique

    /** Lames en éventail autour d'un pique géant, puis elles plongent sur l'ennemi : coup en X. */
    private void drawSpades(Batch batch, float alpha) {
        float launch = 0.8f, strike = impactTime(FullPotential.SPADES);
        float appear = Interpolation.swingOut.apply(Math.min(1f, time / 0.25f));
        float spadeFade = time < launch ? 1f : Math.max(0f, 1f - (time - launch) / 0.3f);
        if (spadeFade > 0f) {
            glow(batch, centerX, centerY, SPRITE_SCALE * 45f, 0.6f, 0.7f, 1f, alpha * 0.5f * spadeFade);
            batch.setColor(1f, 1f, 1f, alpha * spadeFade);
            drawCentered(batch, spade, centerX, centerY, SPRITE_SCALE * appear * (1f + 0.15f * spadeFade * beat(time, 0.6f)), 0f);
        }
        int blades = 10;
        float radius = 190f;
        batch.setColor(1f, 1f, 1f, alpha);
        for (int i = 0; i < blades; i++) {
            float shown = MathUtils.clamp((time - 0.1f - i * 0.05f) / 0.15f, 0f, 1f);
            if (shown <= 0f) continue;
            float fanAngle = 90f + (i - (blades - 1) / 2f) * 22f;           // éventail au-dessus du pique
            float fx = centerX + MathUtils.cosDeg(fanAngle) * radius * shown;
            float fy = centerY - 20f + MathUtils.sinDeg(fanAngle) * radius * shown;
            float dive = Interpolation.pow3In.apply(MathUtils.clamp((time - launch - i * 0.012f) / (strike - launch), 0f, 1f));
            if (dive >= 1f) continue;
            float x = MathUtils.lerp(fx, targetX, dive), y = MathUtils.lerp(fy, targetY, dive);
            // La lame pointe vers l'extérieur de l'éventail, puis vers l'ennemi.
            float outward = fanAngle - 90f;
            float toward = MathUtils.atan2(targetY - fy, targetX - fx) * MathUtils.radiansToDegrees - 90f;
            float angle = dive > 0f ? toward : outward;
            drawCentered(batch, blade, x, y, SMALL_SCALE * shown, angle);
        }
        if (time >= strike) slashX(batch, time - strike, alpha);
    }

    /** Deux traits blancs qui se croisent sur l'ennemi. */
    private void slashX(Batch batch, float since, float alpha) {
        float grow = Interpolation.pow2Out.apply(Math.min(1f, since / 0.12f));
        float fade = Math.max(0f, 1f - since / 0.55f);
        flash(batch, since, 0.85f, 0.9f, 1f, alpha);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        float length = 320f * grow;
        for (float angle : new float[]{45f, -45f}) {
            batch.setColor(0.6f, 0.75f, 1f, 0.6f * fade * alpha);
            batch.draw(pixel, targetX - length / 2f, targetY - 9f, length / 2f, 9f, length, 18f, 1f, 1f, angle);
            batch.setColor(1f, 1f, 1f, fade * alpha);
            batch.draw(pixel, targetX - length / 2f, targetY - 3f, length / 2f, 3f, length, 6f, 1f, 1f, angle);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    // ------------------------------------------------------------------ Trèfle

    /** Le trèfle d'or tourne et grandit, ses feuilles s'ouvrent, une étoile filante jaillit vers la machine. */
    private void drawClubs(Batch batch, float alpha) {
        float open = impactTime(FullPotential.CLUBS);
        if (time < open) {
            float grow = Interpolation.swingOut.apply(Math.min(1f, time / 0.3f));
            float angle = Interpolation.pow2Out.apply(Math.min(1f, time / open)) * 360f;
            glow(batch, centerX, centerY, SPRITE_SCALE * 45f * grow, 1f, 0.85f, 0.3f, alpha * 0.55f);
            batch.setColor(1f, 1f, 1f, alpha);
            drawCentered(batch, clover, centerX, centerY, SPRITE_SCALE * grow, angle);
            return;
        }
        float since = time - open;
        flash(batch, since, 1f, 0.9f, 0.5f, alpha);
        // Les quatre feuilles s'écartent et s'effacent.
        float spread = Interpolation.pow2Out.apply(Math.min(1f, since / 0.45f));
        float leafAlpha = Math.max(0f, 1f - since / 0.6f);
        batch.setColor(1f, 1f, 1f, alpha * leafAlpha);
        for (int k = 0; k < 4; k++) {
            float a = 45f + k * 90f;
            float d = 40f + spread * 140f;
            drawCentered(batch, leaf, centerX + MathUtils.cosDeg(a) * d, centerY + MathUtils.sinDeg(a) * d,
                SPRITE_SCALE * (1f - 0.3f * spread), a - 90f + spread * 120f);
        }
        // L'étoile filante, et sa traînée d'étincelles.
        float fly = Interpolation.pow2In.apply(MathUtils.clamp(since / 0.5f, 0f, 1f));
        float sx = MathUtils.lerp(centerX, targetX, fly), sy = MathUtils.lerp(centerY, targetY, fly) + MathUtils.sin(fly * MathUtils.PI) * 120f;
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        int trail = reduced ? 6 : 12;
        for (int i = 1; i <= trail; i++) {
            float t = Math.max(0f, fly - i * 0.035f);
            float tx = MathUtils.lerp(centerX, targetX, t), ty = MathUtils.lerp(centerY, targetY, t) + MathUtils.sin(t * MathUtils.PI) * 120f;
            float s = 14f * (1f - i / (float) (trail + 1));
            batch.setColor(1f, 0.9f, 0.4f, alpha * 0.7f * (1f - i / (float) (trail + 1)));
            batch.draw(pixel, tx - s / 2f, ty - s / 2f, s, s);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        if (fly < 1f) {
            batch.setColor(1f, 1f, 1f, alpha);
            drawCentered(batch, star, sx, sy, SMALL_SCALE * 1.4f, since * 720f);
        } else {
            float burst = since - 0.5f;
            glow(batch, targetX, targetY, 260f * Math.min(1f, burst / 0.15f), 1f, 0.9f, 0.4f, alpha * Math.max(0f, 1f - burst / 0.5f));
        }
    }

    // ------------------------------------------------------------------ Carreau

    /** Le coffre tremble, ses quatre verrous sautent, il s'ouvre sur une pluie de diamants. */
    private void drawDiamonds(Batch batch, float alpha) {
        float open = impactTime(FullPotential.DIAMONDS);
        float appear = Interpolation.swingOut.apply(Math.min(1f, time / 0.25f));
        float[] pops = {0.3f, 0.45f, 0.6f, 0.75f};
        float vw = vault.getRegionWidth() * SPRITE_SCALE / 2f;
        if (time < open + 0.25f) {
            float shake = time < open ? 2f + 5f * time / open : 0f;
            float x = centerX + MathUtils.random(-shake, shake), y = centerY + MathUtils.random(-shake, shake);
            // La porte pivote sur un côté : elle s'aplatit en s'ouvrant.
            float swing = time < open ? 1f : 1f - Interpolation.pow2In.apply((time - open) / 0.25f);
            glow(batch, centerX, centerY, SPRITE_SCALE * 46f, 0.4f, 0.85f, 1f, alpha * (time < open ? 0.3f : 0.8f));
            batch.setColor(1f, 1f, 1f, alpha);
            float scale = SPRITE_SCALE * appear;
            float w = vault.getRegionWidth() * scale, h = vault.getRegionHeight() * scale;
            batch.draw(vault, x - w / 2f, y - h / 2f, 0f, h / 2f, w, h, swing, 1f, 0f);
            // Les verrous, aux quatre pointes, tant qu'ils n'ont pas sauté.
            for (int k = 0; k < 4; k++) {
                float a = k * 90f;
                float bx = x + MathUtils.cosDeg(a) * vw * 0.78f * appear, by = y + MathUtils.sinDeg(a) * vw * 0.78f * appear;
                float since = time - pops[k];
                if (since < 0f) {
                    drawCentered(batch, bolt, bx, by, SMALL_SCALE * appear, a);
                } else if (since < 0.5f) { // il saute, tourne et retombe
                    float px = bx + MathUtils.cosDeg(a) * 260f * since;
                    float py = by + MathUtils.sinDeg(a) * 260f * since + 500f * since - 1400f * since * since;
                    batch.setColor(1f, 1f, 1f, alpha * (1f - since / 0.5f));
                    drawCentered(batch, bolt, px, py, SMALL_SCALE, a + since * 900f);
                    batch.setColor(1f, 1f, 1f, alpha);
                }
            }
        }
        if (time < open) return;
        float since = time - open;
        flash(batch, since, 0.6f, 0.9f, 1f, alpha);
        batch.setColor(1f, 1f, 1f, alpha);
        for (int i = 0; i < particles; i++) {
            float t = since - i * 0.008f;
            if (t < 0f) continue;
            float v = 520f * speeds[i];
            float a = 30f + (angles[i] % 120f);          // vers le haut, en gerbe
            float x = centerX + MathUtils.cosDeg(a) * v * t;
            float y = centerY + MathUtils.sinDeg(a) * v * t - 900f * t * t;
            drawCentered(batch, gem, x, y, SMALL_SCALE * (0.8f + 0.4f * speeds[i]), spins[i] * t);
        }
    }

    // ------------------------------------------------------------------ outils

    /** @return de 0 à 1 puis 0 : un battement autour de l'instant {@code at}. */
    private static float beat(float time, float at) {
        float d = Math.abs(time - at);
        return d > 0.12f ? 0f : 1f - d / 0.12f;
    }

    /** Halo coloré en mode additif, de diamètre {@code size}. */
    private void glow(Batch batch, float x, float y, float size, float r, float g, float b, float a) {
        if (a <= 0f || size <= 0f) return;
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.setColor(r, g, b, Math.min(1f, a));
        batch.draw(halo, x - size / 2f, y - size / 2f, size, size);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** Éclair plein écran à l'instant fort (sauf effets réduits), puis un grand halo qui s'efface. */
    private void flash(Batch batch, float since, float r, float g, float b, float alpha) {
        float fade = Math.max(0f, 1f - since / 0.35f);
        if (fade <= 0f) return;
        if (!reduced && getStage() != null) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            batch.setColor(r, g, b, 0.3f * fade * fade * alpha);
            batch.draw(pixel, 0f, 0f, getStage().getWidth(), getStage().getHeight());
        }
        glow(batch, centerX, centerY, 420f * (1.2f - fade * 0.5f), r, g, b, alpha * fade);
    }

    /** Dessine {@code region} agrandie de {@code scale}, centrée en {@code (x, y)} et tournée de {@code angle}. */
    private static void drawCentered(Batch batch, TextureRegion region, float x, float y, float scale, float angle) {
        float w = region.getRegionWidth(), h = region.getRegionHeight();
        batch.draw(region, x - w / 2f, y - h / 2f, w / 2f, h / 2f, w, h, scale, scale, angle);
    }

    private TextureRegion region(Pixmap pixmap) {
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        textures.add(texture);
        return new TextureRegion(texture);
    }

    @Override
    public void dispose() {
        for (Texture texture : textures) texture.dispose();
        textures.clear();
    }

    // ------------------------------------------------------------------ pixel art

    private static final String[] DROP = {
        "..o..",
        ".o#o.",
        ".o#o.",
        "o#l#o",
        "o##lo",
        "o###o",
        ".ooo.",
    };

    private static final String[] BLADE = {
        "..o..",
        ".olo.",
        ".ol#o",
        ".ol#o",
        ".ol#o",
        ".ol#o",
        ".ol#o",
        ".ol#o",
        ".ol#o",
        "ohhho",
        ".ogo.",
        ".ogo.",
        "..o..",
    };

    private static final String[] LEAF = {
        "..ooo..",
        ".o###o.",
        "o#ll##o",
        "o#l###o",
        "o#####o",
        ".o###o.",
        "..o#o..",
        "...o...",
    };

    private static final String[] STAR = {
        "....o....",
        "...olo...",
        "...o#o...",
        "ooo###ooo",
        "o##l####o",
        ".o#####o.",
        "..o###o..",
        ".o##o##o.",
        ".ooo.ooo.",
    };

    private static final String[] BOLT = {
        ".oooo.",
        "o#ll#o",
        "o####o",
        ".oooo.",
    };

    private static final String[] GEM = {
        ".ooooo.",
        "o#lll#o",
        "oooooo.",
        ".o#l#o.",
        "..odo..",
        "...o...",
    };

    private static Pixmap pixmap(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        return pixmap;
    }

    private static Pixmap grid(String[] rows, String keys, Color... colors) {
        Pixmap pixmap = pixmap(rows[0].length(), rows.length);
        BingoScene.drawGrid(pixmap, rows, 0, 0, keys, colors);
        return pixmap;
    }

    private static Pixmap whitePixel() {
        Pixmap pixmap = pixmap(1, 1);
        pixmap.setColor(Color.WHITE);
        pixmap.drawPixel(0, 0);
        return pixmap;
    }

    /** @return un halo rond blanc (teinté au dessin), de plus en plus dense vers le centre. */
    private static Pixmap halo(int size) {
        Pixmap pixmap = pixmap(size, size);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float d = Vector2.len(x + 0.5f - size / 2f, y + 0.5f - size / 2f) / (size / 2f);
                if (d >= 1f) continue;
                pixmap.setColor(1f, 1f, 1f, (1f - d) * (1f - d));
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }

    /** @return le coeur, plein (rouge sang, reflet) ou vide (sombre, en attente du sang). */
    private static Pixmap heart(boolean empty) {
        int width = 30, height = 27;
        Pixmap pixmap = pixmap(width, height);
        BingoScene.Shape shape = (x, y) -> {
            float u = (x + 0.5f - width / 2f) / (width / 2f) * 1.16f;
            float v = 1.24f - (y + 0.5f) / height * 2.28f;
            float a = u * u + v * v - 1f;
            return a * a * a - u * u * v * v * v <= 0f;
        };
        Color red = hex("c8102e"), light = hex("f0405a"), shine = hex("ffc8d0"), dark = hex("7a0a1c");
        Color hollow = hex("3a1a22"), hollowDark = hex("26121a");
        BingoScene.fillShape(pixmap, shape, (x, y) -> {
            float dx = x - width / 2f, dy = y - height * 0.4f;
            if (empty) return dx + dy > 10f ? hollowDark : hollow;
            if (x >= 5 && x <= 7 && y >= 4 && y <= 6 && !(x == 7 && y == 6)) return shine;
            if (dx + dy > 10f || (dx > 10f && dy > -3f)) return dark;
            if (dx < -4f && dy < 1f) return light;
            return red;
        });
        return pixmap;
    }

    /** @return le pique : noir bleuté, reflet acier. */
    private static Pixmap spade() {
        int width = 26, height = 30;
        Pixmap pixmap = pixmap(width, height);
        BingoScene.Shape shape = (x, y) -> {
            float cx = width / 2f, px = x + 0.5f, py = y + 0.5f;
            // Coeur renversé pour la tête, pied en trapèze.
            float u = (px - cx) / (width / 2f) * 1.16f;
            float v = (py - 1f) / 22f * 2.28f - 1.04f;
            float a = u * u + v * v - 1f;
            boolean head = py < 23f && a * a * a - u * u * v * v * v <= 0f;
            boolean stem = py >= 17f && py < 29f && Math.abs(px - cx) <= 1.5f + (py - 17f) * 0.45f && py > 21f;
            return head || stem;
        };
        Color body = hex("2b3350"), light = hex("5a6a9a"), shine = hex("c8d4ff"), dark = hex("161a2c");
        BingoScene.fillShape(pixmap, shape, (x, y) -> {
            if (x >= 7 && x <= 8 && y >= 8 && y <= 10) return shine;
            float dx = x - width / 2f, dy = y - 14f;
            if (dx + dy > 9f) return dark;
            if (dx < -3f && dy < 0f) return light;
            return body;
        });
        return pixmap;
    }

    /** @return le trèfle d'or à quatre feuilles. */
    private static Pixmap clover() {
        int size = 30;
        Pixmap pixmap = pixmap(size, size);
        float c = size / 2f;
        BingoScene.Shape shape = (x, y) -> {
            float px = x + 0.5f, py = y + 0.5f;
            for (int k = 0; k < 4; k++) {
                float a = 45f + k * 90f;
                float lx = c + MathUtils.cosDeg(a) * 7f, ly = c + MathUtils.sinDeg(a) * 7f;
                if (Vector2.len(px - lx, py - ly) <= 6.6f) return true;
            }
            return Vector2.len(px - c, py - c) <= 4f;
        };
        Color gold = hex("f2b632"), light = hex("ffe27a"), shine = hex("fffbe0"), dark = hex("b07a12");
        BingoScene.fillShape(pixmap, shape, (x, y) -> {
            float dx = x - c, dy = y - c;
            if (Math.abs(Math.abs(dx) - Math.abs(dy)) < 0.8f && Vector2.len(dx, dy) > 3f && Vector2.len(dx, dy) < 11f) return dark; // nervures
            if ((x == 7 && y == 7) || (x == 8 && y == 7) || (x == 7 && y == 8)) return shine;
            if (dx + dy > 8f) return dark;
            if (dx + dy < -6f) return light;
            return gold;
        });
        return pixmap;
    }

    /** @return la porte du coffre en losange : acier, cadran au centre, bord de diamant. */
    private static Pixmap vault() {
        int size = 31;
        Pixmap pixmap = pixmap(size, size);
        float c = size / 2f;
        BingoScene.Shape shape = (x, y) -> Math.abs(x + 0.5f - c) + Math.abs(y + 0.5f - c) <= c - 0.5f;
        Color rim = hex("4fd6ff"), rimDark = hex("1d8fc4"), steel = hex("8c98a6"), steelLight = hex("c4ced8"),
            steelDark = hex("5c6672"), dial = hex("e8b84a"), dialDark = hex("9a6a18");
        BingoScene.fillShape(pixmap, shape, (x, y) -> {
            float dx = x + 0.5f - c, dy = y + 0.5f - c;
            float d = Math.abs(dx) + Math.abs(dy);
            float r = Vector2.len(dx, dy);
            if (d > c - 4f) return dx + dy > 0f ? rimDark : rim;
            if (r <= 2.2f) return dialDark;
            if (r <= 4.6f) return dial;
            if (Math.abs(dx) < 0.6f || Math.abs(dy) < 0.6f) return r <= 7f ? dialDark : steelDark; // rayons du cadran
            if (dx + dy > 3f) return steelDark;
            if (dx + dy < -5f) return steelLight;
            return steel;
        });
        return pixmap;
    }
}
