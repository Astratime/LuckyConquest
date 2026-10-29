package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.i18n.Lang;
import java.util.Random;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Socle des cinématiques de phase et de mort des boss et des rois : chaque boss a
 * sa propre scène (voir {@link BossScenes}), dans le décor de son chapitre ou de
 * son lieu ({@link BossTheme}). Le socle dessine le ciel et le sol, le boss (avec
 * le geste que la scène lui donne), les particules, puis la phrase de la scène en
 * bas de l'écran ; fondu noir et la suite du combat.
 *
 * Le moment fort tombe sur le son : {@link #CLIMAX_PHASE} (cutscene/boss_phase)
 * ou {@link #CLIMAX_DEATH} (cutscene/boss_death).
 */
public abstract class BossScene extends Cutscene {

    /** Le moment joué. */
    public enum Moment { PHASE, DEATH }

    /** Le moment fort d'une phase (le boum du son cutscene/boss_phase). */
    public static final float CLIMAX_PHASE = 2.2f;
    /** Le moment fort d'une mort (le fracas du son cutscene/boss_death). */
    public static final float CLIMAX_DEATH = 2.5f;
    static final float PHASE_TEXT = 2.45f, DEATH_TEXT = 3.1f;
    static final float PHASE_COVER_START = 4.2f, PHASE_COVER_FULL = 4.8f;
    static final float DEATH_COVER_START = 5.0f, DEATH_COVER_FULL = 5.6f;

    protected static final Color WHITE = hex("ffffff"), HOT = hex("fff6d8"), GOLD = hex("ffc93a"), BLOOD = hex("c8102e");

    protected final Moment        moment;
    protected final EnemyKind     kind;
    protected final int           phase;
    protected final BossTheme     theme;
    protected final TextureRegion boss, coin;
    /** Étincelles et lueurs (mélange additif). */
    protected final Array<Particle> glows   = new Array<>(false, 256);
    /** Images : pièces, symboles, morceaux, accessoires. */
    protected final Array<Particle> sprites = new Array<>(false, 256);
    private final CutsceneKit kit;
    private final BitmapFont  font;
    private final String      headline, subtitle;
    private final Matrix4     saved = new Matrix4(), tilted = new Matrix4();
    private final GlyphLayout measure = new GlyphLayout();
    private float previous;

    /** Le geste du boss pour cette image, remis à zéro avant {@link #back}. */
    protected float poseX, poseY, poseTilt, poseStretch, poseScale, poseAlpha, poseShadow, poseSink, poseTint;
    protected final Color poseTintColor = new Color();
    /** {@code false} : le boss n'est pas dessiné à cette image (il a disparu). */
    protected boolean showBoss;

    protected BossScene(CutsceneKit kit, Moment moment, EnemyKind kind, int phase) {
        super(kit.settings(), kit.shake(), kit.sound(moment == Moment.PHASE ? "boss_phase" : "boss_death"));
        this.kit    = kit;
        this.moment = moment;
        this.kind   = kind;
        this.phase  = phase;
        theme = BossTheme.of(kind);
        boss  = kit.portrait(kind);
        coin  = load("hud/coin.png");
        font  = kit.font();
        if (moment == Moment.PHASE) {
            headline = kind.phaseText(phase);
            subtitle = Lang.f("{0} : PHASE {1}", kind.getBarName(), phase);
        } else {
            headline = theme.deathLine();
            subtitle = kind.getBarName();
        }
    }

    /** @return le moment joué. */
    public Moment getMoment()   { return moment; }
    /** @return la phase où il entre (0 pour sa mort). */
    public int getPhase()       { return phase; }
    /** @return le boss. */
    public EnemyKind getKind()  { return kind; }
    /** @return la phrase qui s'écrit (l'annonce de sa phase, ou sa dernière phrase). */
    public String getHeadline() { return headline; }

    @Override protected float coverStart() { return moment == Moment.PHASE ? PHASE_COVER_START : DEATH_COVER_START; }
    @Override protected float coverFull()  { return moment == Moment.PHASE ? PHASE_COVER_FULL : DEATH_COVER_FULL; }
    @Override protected Color coverColor() { return Color.BLACK; }

    /** @return le moment fort de la scène. */
    protected float climax() { return moment == Moment.PHASE ? CLIMAX_PHASE : CLIMAX_DEATH; }

    // -------------------------------------------------------------------------
    // À écrire par chaque scène
    // -------------------------------------------------------------------------

    /** Remet la scène au début. */
    protected void restart() {}

    /** Fait vivre la scène ; {@link #at} dit quand lancer chaque moment. */
    protected abstract void step(float delta);

    /** Le décor derrière le boss ; c'est aussi ici que la scène règle son geste ({@link #poseX}…). */
    protected abstract void back(Batch batch);

    /** Ce qui passe devant le boss. */
    protected void front(Batch batch) {}

    // -------------------------------------------------------------------------
    // Socle
    // -------------------------------------------------------------------------

    @Override
    protected final void reset() {
        glows.clear();
        sprites.clear();
        previous = 0f;
        restart();
    }

    @Override
    protected final void simulate(float delta) {
        step(delta);
        update(glows, delta);
        update(sprites, delta);
        previous = time;
    }

    @Override
    protected final void drawScene(Batch batch) {
        gradient(batch, theme.skyLow(), theme.skyTop());
        batch.setColor(theme.ground());
        batch.draw(pixel, -MARGIN, -MARGIN, worldWidth() + 2f * MARGIN, groundY() + MARGIN);
        poseX = poseY = poseTilt = poseStretch = poseShadow = poseSink = poseTint = 0f;
        poseScale = poseAlpha = 1f;
        showBoss = true;
        back(batch);
        if (showBoss) drawBoss(batch);
        front(batch);
        drawParticles(batch, sprites);
        drawParticles(batch, glows);
        drawTexts(batch);
        fadeFromBlack(batch, 0.4f);
    }

    /** Le boss, avec son geste ; {@link #poseSink} cache le bas du portrait (il s'enfonce). */
    private void drawBoss(Batch batch) {
        float scale = bossScale() * poseScale;
        int rows = Math.max(1, Math.round(boss.getRegionHeight() * (1f - MathUtils.clamp(poseSink, 0f, 1f))));
        TextureRegion shown = rows == boss.getRegionHeight() ? boss
            : new TextureRegion(boss, 0, 0, boss.getRegionWidth(), rows);
        float width = boss.getRegionWidth() * scale * (1f - poseStretch * 0.5f);
        float height = rows * scale * (1f + poseStretch);
        float x = bossX() + poseX - width / 2f, y = bossBottom() + poseY;
        float light = 1f - poseShadow;
        batch.setColor(light, light, light, poseAlpha);
        batch.draw(shown, x, y, width / 2f, 0f, width, height, 1f, 1f, poseTilt);
        if (poseTint > 0f) {
            additive(batch);
            batch.setColor(poseTintColor.r, poseTintColor.g, poseTintColor.b, poseTint * poseAlpha);
            batch.draw(shown, x, y, width / 2f, 0f, width, height, 1f, 1f, poseTilt);
            normal(batch);
        }
    }

    /** La phrase de la scène, qui s'imprime en bas, et son nom au-dessus. */
    private void drawTexts(Batch batch) {
        if (font == null) return;
        float at = moment == Moment.PHASE ? PHASE_TEXT : DEATH_TEXT;
        float show = progress(at, at + 0.3f);
        if (show <= 0f) return;
        float width = worldWidth(), height = worldHeight();
        batch.setColor(0f, 0f, 0f, 0.6f * show);
        batch.draw(soft, width * 0.05f, -height * 0.02f, width * 0.9f, height * 0.26f);
        float base = font.getData().scaleX;
        font.getData().setScale(base * 1.8f);
        caption(batch, font, subtitle, width / 2f, height * 0.16f, show * 0.85f);
        float wanted = MathUtils.lerp(4.0f, 2.8f, Interpolation.pow2Out.apply(show));
        font.getData().setScale(base * wanted);
        measure.setText(font, headline);
        if (measure.width > width * 0.9f) font.getData().setScale(base * wanted * width * 0.9f / measure.width);
        caption(batch, font, headline, width / 2f, height * 0.08f, show);
        font.getData().setScale(base);
    }

    // -------------------------------------------------------------------------
    // Outils des scènes
    // -------------------------------------------------------------------------

    /** @return {@code true} une seule fois : à l'image où le temps passe {@code moment}. */
    protected boolean at(float moment) { return previous < moment && time >= moment; }

    /** Une lueur qui pulse, entre {@code low} et 1. */
    protected float pulse(float speed, float low) {
        return low + (1f - low) * (0.5f + 0.5f * MathUtils.sin(time * speed));
    }

    /** @return de 0 à 1 puis 0, entre {@code from} et {@code to} (un geste aller-retour). */
    protected float gesture(float from, float to) { return MathUtils.sin(progress(from, to) * MathUtils.PI); }

    /** @return le symbole {@code symbol} des machines ennemies (16 x 16), {@code null} sans textures. */
    protected TextureRegion symbol(EnemySymbol symbol) { return kit.enemies() == null ? null : kit.symbol(symbol); }

    /** Une gerbe de {@code count} étincelles autour de ({@code x}, {@code y}). */
    protected void sparks(float x, float y, int count, Color color, float speed, float size) {
        for (int i = 0; i < count; i++) {
            Particle spark = particle(x, y, 360f * random.nextFloat(), speed * (0.3f + 0.7f * random.nextFloat()),
                0.5f + 0.6f * random.nextFloat(), size * (0.5f + random.nextFloat()));
            spark.drag = 0.2f;
            spark.color.set(color);
            glows.add(spark);
        }
    }

    /**
     * Lance {@code count} images {@code region} de ({@code x}, {@code y}), entre les angles {@code from}
     * et {@code to} (degrés), agrandies {@code scale} fois, qui retombent avec {@code gravity}.
     */
    protected void spray(TextureRegion region, float x, float y, int count, float from, float to, float speed,
                         float scale, float gravity, float life) {
        if (region == null) return;
        for (int i = 0; i < count; i++) {
            Particle piece = particle(x, y, MathUtils.lerp(from, to, random.nextFloat()), speed * (0.5f + 0.5f * random.nextFloat()),
                life * (0.8f + 0.4f * random.nextFloat()), scale * (0.7f + 0.5f * random.nextFloat()));
            piece.region = region;
            piece.gravity = gravity;
            piece.spin = (random.nextFloat() - 0.5f) * 540f;
            sprites.add(piece);
        }
    }

    /** Une image {@code region} posée en ({@code x}, {@code y}), qui part avec ({@code vx}, {@code vy}). */
    protected Particle place(TextureRegion region, float x, float y, float vx, float vy, float scale, float life) {
        Particle piece = particle(x, y, 0f, 0f, life, scale);
        piece.region = region;
        piece.vx = vx;
        piece.vy = vy;
        sprites.add(piece);
        return piece;
    }

    /** Dessine un rectangle plein. */
    protected void rect(Batch batch, Color color, float alpha, float x, float y, float w, float h) {
        batch.setColor(color.r, color.g, color.b, alpha);
        batch.draw(pixel, x, y, w, h);
    }

    /** Dessine un trait de ({@code x0}, {@code y0}) à ({@code x1}, {@code y1}), épais de {@code thick}. */
    protected void line(Batch batch, Color color, float alpha, float x0, float y0, float x1, float y1, float thick) {
        float dx = x1 - x0, dy = y1 - y0;
        batch.setColor(color.r, color.g, color.b, alpha);
        batch.draw(pixel, x0, y0 - thick / 2f, 0f, thick / 2f, (float) Math.sqrt(dx * dx + dy * dy), thick, 1f, 1f,
            MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees);
    }

    /** Dessine {@code region} centrée sur ({@code x}, {@code y}), teinte {@code color}, opacité {@code alpha}. */
    protected void draw(Batch batch, TextureRegion region, float x, float y, float scale, float rotation, Color color, float alpha) {
        if (region == null) return;
        batch.setColor(color.r, color.g, color.b, alpha);
        sprite(batch, region, x, y, scale, rotation);
    }

    /** Dessine {@code region} centrée sur ({@code x}, {@code y}), étirée de {@code sx} en largeur et {@code sy} en hauteur. */
    protected void stretch(Batch batch, TextureRegion region, float x, float y, float sx, float sy, float rotation,
                           Color color, float alpha) {
        if (region == null) return;
        float w = region.getRegionWidth() * sx, h = region.getRegionHeight() * sy;
        batch.setColor(color.r, color.g, color.b, alpha);
        batch.draw(region, x - w / 2f, y - h / 2f, w / 2f, h / 2f, w, h, 1f, 1f, rotation);
    }

    /**
     * Ses yeux s'allument : une lueur {@code color} sur chaque œil. {@code eyes} donne leurs centres en pixels
     * du portrait ({x, y}, depuis le coin en haut à gauche), pour que la lueur tombe pile sur ses yeux.
     */
    protected void eyes(Batch batch, float alpha, Color color, float[]... eyes) {
        if (alpha <= 0f) return;
        float size = bossScale() * poseScale;
        for (float[] eye : eyes) {
            float x = portraitX(eye[0]), y = portraitY(eye[1]);
            glow(batch, x, y, size * 6f, color, 0.9f * alpha);
            glow(batch, x, y, size * 2.2f, WHITE, alpha);
        }
    }

    /** Une fissure en zigzag, comme un éclair, de ({@code x}, {@code y}) vers {@code angle} sur {@code length}. */
    protected void crack(Batch batch, float x, float y, float angle, float length, long seed, Color color, float alpha, float thick) {
        Random zig = new Random(seed);
        int steps = 7;
        float px = x, py = y;
        for (int i = 1; i <= steps; i++) {
            float along = length * i / steps, side = i == steps ? 0f : (zig.nextFloat() - 0.5f) * length * 0.35f;
            float nx = x + MathUtils.cosDeg(angle) * along - MathUtils.sinDeg(angle) * side;
            float ny = y + MathUtils.sinDeg(angle) * along + MathUtils.cosDeg(angle) * side;
            line(batch, color, alpha, px, py, nx, ny, thick);
            if (i == 3 && length > 60f) {                              // une petite branche
                float branch = angle + (zig.nextBoolean() ? 35f : -35f);
                line(batch, color, alpha, nx, ny, nx + MathUtils.cosDeg(branch) * length * 0.25f,
                    ny + MathUtils.sinDeg(branch) * length * 0.25f, thick * 0.6f);
            }
            px = nx;
            py = ny;
        }
    }

    /**
     * Dessine {@code region} sur le portrait du boss : son coin haut-gauche au pixel ({@code left}, {@code top}),
     * un pixel de {@code region} valant {@code pixels} pixels du portrait. Elle suit tout son geste (bascule,
     * étirement, ombre, enfoncement), comme si elle faisait partie du dessin.
     */
    protected void onPortrait(Batch batch, TextureRegion region, float left, float top, float pixels, float alpha) {
        int height = boss.getRegionHeight();
        int rows = Math.max(1, Math.round(height * (1f - MathUtils.clamp(poseSink, 0f, 1f))));
        if (top >= rows) return;
        float scale = bossScale() * poseScale, sx = 1f - poseStretch * 0.5f, sy = 1f + poseStretch;
        float w = region.getRegionWidth() * pixels * scale * sx, h = region.getRegionHeight() * pixels * scale * sy;
        float footX = bossX() + poseX, footY = bossBottom() + poseY;
        float rx = footX + (left - boss.getRegionWidth() / 2f) * scale * sx;
        float ry = footY + (rows - top - region.getRegionHeight() * pixels) * scale * sy;
        float light = 1f - poseShadow;
        batch.setColor(light, light, light, alpha * poseAlpha);
        batch.draw(region, rx, ry, footX - rx, footY - ry, w, h, 1f, 1f, poseTilt);
    }

    /** @return l'abscisse à l'écran du pixel {@code px} du portrait (avec son geste). */
    protected float portraitX(float px) {
        return bossX() + poseX + (px - boss.getRegionWidth() / 2f) * bossScale() * poseScale;
    }

    /** @return l'ordonnée à l'écran du pixel {@code py} du portrait, compté depuis le haut (avec son geste). */
    protected float portraitY(float py) {
        return bossBottom() + poseY + (boss.getRegionHeight() - py) * bossScale() * poseScale;
    }

    private static final int[] CAP_INSETS = {0, 1, 3};
    private static final Color CAP_LINE = hex("1a0f0f");

    /**
     * Le haut du crâne sous une couronne qu'on lui retire : un dôme en pixels de {@code fill}, cerné de noir.
     * Sa base est la ligne {@code row} du portrait, de la colonne {@code left} à {@code right} ; il monte de
     * trois lignes. Le portrait est posé avec son coin bas-gauche en ({@code x0}, {@code y0}), à l'échelle
     * {@code scale}, éclairé à {@code light}.
     */
    protected void cap(Batch batch, float x0, float y0, float scale, int row, int left, int right, Color fill,
                       float light, float alpha) {
        cap(batch, x0, y0, scale, row, left, right, fill, light, alpha, 0f);
    }

    /** {@link #cap}, le portrait tourné de {@code rotation} degrés autour de son coin bas-gauche. */
    protected void cap(Batch batch, float x0, float y0, float scale, int row, int left, int right, Color fill,
                       float light, float alpha, float rotation) {
        int height = boss.getRegionHeight();
        for (int k = 0; k <= CAP_INSETS.length; k++) {
            int y = row - k;
            int inset = CAP_INSETS[Math.min(k, CAP_INSETS.length - 1)];
            float py = (height - 1 - y) * scale;
            if (k == CAP_INSETS.length) {                              // le trait du dessus
                capRect(batch, CAP_LINE, light, alpha, x0, y0, (left + inset) * scale, py, (right - left - 2 * inset + 1) * scale, scale, rotation);
                break;
            }
            capRect(batch, fill, light, alpha, x0, y0, (left + inset) * scale, py, (right - left - 2 * inset + 1) * scale, scale, rotation);
            int next = k + 1 < CAP_INSETS.length ? CAP_INSETS[k + 1] : inset;
            // les bords : du bord de cette ligne jusqu'au bord de la ligne du dessus
            float edge = Math.max(1, next - inset);
            capRect(batch, CAP_LINE, light, alpha, x0, y0, (left + inset - 1) * scale, py, edge * scale, scale, rotation);
            capRect(batch, CAP_LINE, light, alpha, x0, y0, (right - inset + 2 - edge) * scale, py, edge * scale, scale, rotation);
        }
    }

    private void capRect(Batch batch, Color color, float light, float alpha, float x0, float y0, float dx, float dy,
                         float w, float h, float rotation) {
        batch.setColor(color.r * light, color.g * light, color.b * light, alpha);
        batch.draw(pixel, x0 + dx, y0 + dy, -dx, -dy, w, h, 1f, 1f, rotation);
    }

    /** Une onde de choc qui part de ({@code x}, {@code y}) à {@code at} et s'efface en {@code length} secondes. */
    protected void wave(Batch batch, float at, float length, float x, float y, Color color, float size) {
        float k = progress(at, at + length);
        if (time < at || k >= 1f) return;
        float s = size * Interpolation.pow2Out.apply(k);
        additive(batch);
        batch.setColor(color.r, color.g, color.b, 0.8f * (1f - k));
        batch.draw(ring, x - s / 2f, y - s / 2f, s, s);
        normal(batch);
    }

    /** Tout ce qui est dessiné jusqu'à {@link #endTilt} penche de {@code degrees} autour du centre de l'écran. */
    protected void beginTilt(Batch batch, float degrees) {
        saved.set(batch.getTransformMatrix());
        tilted.set(saved).translate(worldWidth() / 2f, worldHeight() / 2f, 0f).rotate(0f, 0f, 1f, degrees)
            .translate(-worldWidth() / 2f, -worldHeight() / 2f, 0f);
        batch.setTransformMatrix(tilted);
    }

    protected void endTilt(Batch batch) { batch.setTransformMatrix(saved); }

    /** De la pluie qui tombe en biais sur tout l'écran. */
    protected void rain(Batch batch, int count, float alpha) {
        float width = worldWidth() + 2f * MARGIN, height = worldHeight() + 2f * MARGIN;
        batch.setColor(0.7f, 0.78f, 0.95f, alpha);
        for (int i = 0; i < count; i++) {
            float speed = 1400f + (i * 37 % 11) * 60f;
            float x = ((i * 157.3f + time * 300f) % width) - MARGIN;
            float y = height - ((i * 91.7f + time * speed) % height) - MARGIN;
            batch.draw(pixel, x, y, 1.5f, 0f, 3f, 46f, 1f, 1f, -14f);
        }
    }

    /** Des bulles qui remontent sur tout l'écran (sous l'eau). */
    protected void bubbles(Batch batch, int count, float alpha) {
        float width = worldWidth(), height = worldHeight() + 2f * MARGIN;
        additive(batch);
        for (int i = 0; i < count; i++) {
            float size = 8f + (i * 13 % 7) * 4f;
            float x = (i * 211.7f % width) + MathUtils.sin(time * 2f + i) * 12f;
            float y = ((i * 97.3f + time * (90f + (i % 5) * 30f)) % height) - MARGIN;
            batch.setColor(0.7f, 0.9f, 1f, alpha);
            batch.draw(ring, x - size / 2f, y - size / 2f, size, size);
        }
        normal(batch);
    }

    /** Un éclair zigzag du haut de l'écran jusqu'à {@code bottom}. */
    protected void bolt(Batch batch, float x, float bottom, long seed, float alpha) {
        if (alpha <= 0f) return;
        Random zig = new Random(seed);
        float y = worldHeight() + MARGIN, stepY = (y - bottom) / 9f;
        additive(batch);
        for (int i = 0; i < 9; i++) {
            float nx = x + (zig.nextFloat() - 0.5f) * 140f, ny = y - stepY;
            line(batch, hex("a0b4ff"), 0.5f * alpha, x, y, nx, ny, 22f);
            line(batch, WHITE, alpha, x, y, nx, ny, 7f);
            x = nx;
            y = ny;
        }
        normal(batch);
    }

    protected float groundY()     { return worldHeight() * 0.18f; }
    protected float bossScale()   { return worldHeight() * 0.42f / boss.getRegionHeight(); }
    protected float bossWidth()   { return boss.getRegionWidth() * bossScale(); }
    protected float bossHeight()  { return boss.getRegionHeight() * bossScale(); }
    protected float bossBottom()  { return worldHeight() * 0.24f; }
    protected float bossX()       { return worldWidth() / 2f; }
    protected float bossY()       { return bossBottom() + bossHeight() / 2f; }
    /** @return l'agrandissement qui donne {@code height} pixels d'écran à une image de {@code pixels} de haut. */
    protected float fit(float height, int pixels) { return height / pixels; }
    /** @return l'agrandissement d'un symbole de 16 pixels à {@code share} de la hauteur de l'écran. */
    protected float iconScale(float share) { return worldHeight() * share / 16f; }
    protected float coinScale(float share) { return worldHeight() * share / coin.getRegionHeight(); }
}
