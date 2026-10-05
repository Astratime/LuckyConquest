package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.animations.ScreenShake;
import fr.astratime.lucky.settings.VisualSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Une cinématique plein écran, dessinée dans le code à chaque image : la scène
 * joue, puis tout l'écran se couvre d'une couleur ({@link #coverColor()}).
 * {@code onCovered} est appelé à ce moment-là (l'écran suivant apparaît dessous),
 * puis la couleur s'estompe.
 *
 * Un clic ou une touche ({@link #skip()}) passe la scène : l'écran se couvre
 * tout de suite. Avec les effets réduits, les sous-classes évitent secousses et éclairs.
 *
 * L'acteur couvre le Stage et bloque les clics tant qu'il est visible.
 */
public abstract class Cutscene extends Actor implements Disposable {

    protected static final float MARGIN = 40f;        // le décor déborde, pour les secousses
    private static final float COVER_HOLD = 0.2f;
    private static final float COVER_OUT  = 0.9f;
    private static final float SKIP_COVER = 0.35f;    // durée du fondu quand on passe la scène

    protected final VisualSettings settings;
    protected final ScreenShake    shake;
    protected final Random         random = new Random();
    protected final TextureRegion  pixel, soft, trail, ring;
    protected final Color          tmp = new Color();
    /** Temps écoulé depuis le début de la scène, en secondes. */
    protected float time;

    private final Sound          sound;
    private final List<Texture>  textures = new ArrayList<>();
    private final GlyphLayout    layout = new GlyphLayout();
    private Runnable onCovered;
    private float    skipAt  = -1f;    // instant où le joueur a passé la scène
    private float    coverAt = -1f;    // instant où l'écran est devenu tout couvert
    private long     soundId = -1;

    protected Cutscene(VisualSettings settings, ScreenShake shake, Sound sound) {
        this.settings = settings;
        this.shake    = shake;
        this.sound    = sound;
        pixel = region(solid(), false);
        soft  = region(softDisc(64), true);
        trail = region(trailGradient(), true);
        ring  = region(softRing(128), true);
        setVisible(false);
        setTouchable(Touchable.disabled);
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                skip();
                return true;
            }
        });
    }

    // -------------------------------------------------------------------------
    // À écrire par chaque scène
    // -------------------------------------------------------------------------

    /** @return l'instant où l'écran commence à se couvrir. */
    protected abstract float coverStart();

    /** @return l'instant où l'écran est tout couvert ({@code onCovered} est appelé). */
    protected abstract float coverFull();

    /** @return la couleur qui couvre l'écran à la fin. */
    protected Color coverColor() { return Color.WHITE; }

    /** Remet la scène au début (appelé par {@link #play}). */
    protected abstract void reset();

    /** Fait vivre la scène ({@link #time} est déjà avancé). */
    protected abstract void simulate(float delta);

    /** Dessine la scène, en mélange normal ; le fondu final est dessiné par-dessus. */
    protected abstract void drawScene(Batch batch);

    // -------------------------------------------------------------------------
    // Lecture
    // -------------------------------------------------------------------------

    /**
     * Lance la scène par-dessus tout l'écran ; {@code onCovered} est appelé une
     * fois, quand l'écran est tout couvert.
     */
    public void play(Runnable onCovered) {
        this.onCovered = onCovered;
        time = 0f;
        skipAt = -1f;
        coverAt = -1f;
        reset();
        setVisible(true);
        setTouchable(Touchable.enabled);
        toFront();
        soundId = sound != null ? sound.play() : -1;
    }

    /** @return {@code true} tant que la scène joue, jusqu'à ce que le fondu final commence à s'estomper. */
    public boolean isPlaying() { return isVisible() && coverAt < 0f; }

    /** Passe la scène : l'écran se couvre tout de suite. */
    public void skip() {
        if (!isPlaying() || skipAt >= 0f || time >= coverStart()) return;
        skipAt = time;
        stopSound();
    }

    /** Menu pause : le son se fige avec l'image. */
    public void pauseSound() {
        if (isVisible() && soundId != -1) sound.pause(soundId);
    }

    /** Fin de la pause : le son reprend. */
    public void resumeSound() {
        if (isVisible() && soundId != -1) sound.resume(soundId);
    }

    /** Arrête et cache la scène sans appeler {@code onCovered}. */
    public void cancel() {
        stopSound();
        onCovered = null;
        setVisible(false);
        setTouchable(Touchable.disabled);
    }

    private void stopSound() {
        if (soundId != -1) sound.stop(soundId);
        soundId = -1;
    }

    @Override
    public Actor hit(float x, float y, boolean touchable) {
        return touchable && getTouchable() == Touchable.enabled && isVisible() ? this : null;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isVisible()) return;
        time += delta;
        if (coverAt >= 0f) {
            if (time - coverAt >= COVER_HOLD + COVER_OUT) {
                setVisible(false);
                setTouchable(Touchable.disabled);
            }
            return;
        }
        if (skipAt < 0f) simulate(delta);
        if (coverAlpha() >= 1f) {
            coverAt = time;
            setTouchable(Touchable.disabled); // l'écran suivant est déjà dessous
            Runnable callback = onCovered;
            onCovered = null;
            if (callback != null) callback.run();
        }
    }

    /** @return l'opacité du fondu final (1 = écran tout couvert). */
    private float coverAlpha() {
        if (coverAt >= 0f) {
            float out = time - coverAt - COVER_HOLD;
            return out <= 0f ? 1f : 1f - Interpolation.pow2.apply(Math.min(1f, out / COVER_OUT));
        }
        float alpha = time <= coverStart() ? 0f
            : Interpolation.pow2In.apply(Math.min(1f, (time - coverStart()) / (coverFull() - coverStart())));
        if (skipAt >= 0f) alpha = Math.max(alpha, Math.min(1f, (time - skipAt) / SKIP_COVER));
        return alpha;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float cover = coverAlpha();
        if (coverAt < 0f) {
            drawScene(batch);
            normal(batch);
        }
        if (cover > 0f) fill(batch, coverColor(), cover);
        batch.setColor(Color.WHITE);
    }

    // -------------------------------------------------------------------------
    // Outils de dessin
    // -------------------------------------------------------------------------

    protected float worldWidth()  { return getStage().getViewport().getWorldWidth(); }
    protected float worldHeight() { return getStage().getViewport().getWorldHeight(); }

    /** @return la progression (0 à 1) de {@code from} à {@code to}, bornée. */
    protected float progress(float from, float to) {
        return MathUtils.clamp((time - from) / (to - from), 0f, 1f);
    }

    /** Couvre tout l'écran (marges comprises) de {@code color}. */
    protected void fill(Batch batch, Color color, float alpha) {
        batch.setColor(color.r, color.g, color.b, alpha);
        batch.draw(pixel, -MARGIN, -MARGIN, worldWidth() + 2f * MARGIN, worldHeight() + 2f * MARGIN);
    }

    /** Un fond en dégradé vertical, de {@code bottom} à {@code top}. */
    protected void gradient(Batch batch, Color bottom, Color top) {
        int bands = 40;
        float height = worldHeight() + 2f * MARGIN, band = height / bands;
        for (int i = 0; i < bands; i++) {
            tmp.set(bottom).lerp(top, i / (float) (bands - 1));
            batch.setColor(tmp);
            batch.draw(pixel, -MARGIN, -MARGIN + i * band - 1f, worldWidth() + 2f * MARGIN, band + 2f);
        }
    }

    /** Lumière douce (en mélange additif) centrée sur ({@code x}, {@code y}). */
    protected void glow(Batch batch, float x, float y, float size, Color color, float alpha) {
        additive(batch);
        batch.setColor(color.r, color.g, color.b, alpha);
        batch.draw(soft, x - size / 2f, y - size / 2f, size, size);
        normal(batch);
    }

    /** Dessine {@code region} centrée sur ({@code x}, {@code y}), agrandie {@code scale} fois et tournée. */
    protected void sprite(Batch batch, TextureRegion region, float x, float y, float scale, float rotation) {
        float width = region.getRegionWidth() * scale, height = region.getRegionHeight() * scale;
        batch.draw(region, x - width / 2f, y - height / 2f, width / 2f, height / 2f, width, height, 1f, 1f, rotation);
    }

    /** Écrit {@code text} centré sur ({@code x}, {@code y}). */
    protected void caption(Batch batch, BitmapFont font, String text, float x, float y, float alpha) {
        if (alpha <= 0f) return;
        Color old = font.getColor().cpy();
        font.setColor(old.r, old.g, old.b, alpha);
        layout.setText(font, text, font.getColor(), 0f, Align.center, false);
        font.draw(batch, layout, x, y + layout.height / 2f);
        font.setColor(old);
    }

    /** Secoue l'écran, sauf avec les effets réduits. */
    protected void rumble(float duration, float strength) {
        if (!settings.isReducedEffects()) shake.shake(duration, strength);
    }

    /** Éclair plein écran qui s'éteint en {@code length} secondes après {@code at} (pas avec les effets réduits). */
    protected void flash(Batch batch, Color color, float at, float length, float strength) {
        if (settings.isReducedEffects() || time < at) return;
        float alpha = 1f - (time - at) / length;
        if (alpha > 0f) fill(batch, color, strength * alpha);
    }

    /** La scène sort du noir pendant {@code length} secondes. */
    protected void fadeFromBlack(Batch batch, float length) {
        if (time < length) fill(batch, Color.BLACK, 1f - Interpolation.pow2Out.apply(time / length));
    }

    /**
     * Dessine un portrait en pixel art centré en bas sur ({@code x}, {@code y}) :
     * {@code shadow} = 1 en ombre noire, 0 en couleurs ; {@code alpha} pour l'apparition.
     */
    protected void portrait(Batch batch, TextureRegion region, float x, float y, float scale, float shadow,
                            float alpha) {
        float width = region.getRegionWidth() * scale, height = region.getRegionHeight() * scale;
        float light = 1f - shadow;
        batch.setColor(light, light, light, alpha);
        batch.draw(region, x - width / 2f, y, width, height);
    }

    /** Un ciel étoilé : des étoiles fixes qui scintillent, placées une fois pour toutes. */
    protected final class Starfield {
        private final float[] x, y, size, phase;

        Starfield(int count, float minHeight) {
            x = new float[count];
            y = new float[count];
            size = new float[count];
            phase = new float[count];
            for (int i = 0; i < count; i++) {
                x[i] = random.nextFloat();
                y[i] = minHeight + (1f - minHeight) * (float) Math.pow(random.nextFloat(), 0.8f);
                size[i] = random.nextFloat() < 0.12f ? 3f : random.nextFloat() < 0.4f ? 2f : 1.5f;
                phase[i] = random.nextFloat() * MathUtils.PI2;
            }
        }

        /** Dessine les étoiles, avec l'opacité {@code alpha}. */
        void draw(Batch batch, float alpha) {
            if (alpha <= 0f) return;
            additive(batch);
            float width = worldWidth(), height = worldHeight();
            for (int i = 0; i < x.length; i++) {
                float twinkle = 0.45f + 0.55f * (0.5f + 0.5f * MathUtils.sin(time * (2f + i % 5) + phase[i]));
                batch.setColor(1f, 0.96f, 0.85f, twinkle * alpha);
                batch.draw(pixel, x[i] * width - size[i] / 2f, y[i] * height - size[i] / 2f, size[i], size[i]);
                if (size[i] >= 3f) {
                    batch.setColor(0.8f, 0.85f, 1f, 0.25f * twinkle * alpha);
                    batch.draw(soft, x[i] * width - 8f, y[i] * height - 8f, 16f, 16f);
                }
            }
            normal(batch);
        }
    }

    /**
     * Des particules qui partent toutes de ({@code fromX}, {@code fromY}) et
     * s'assemblent pour dessiner {@code shape} (chaque pixel plein devient une
     * cible), agrandie {@code scale} fois et centrée sur ({@code x}, {@code y}).
     */
    protected Array<Particle> assemble(Pixmap shape, float fromX, float fromY, float x, float y, float scale,
                                       float spread) {
        Array<Particle> parts = new Array<>();
        int width = shape.getWidth(), height = shape.getHeight();
        for (int py = 0; py < height; py++) {
            for (int px = 0; px < width; px++) {
                int color = shape.getPixel(px, py);
                if ((color & 0xff) == 0) continue;
                Particle part = particle(fromX, fromY, 0f, 0f, 1f, scale);
                part.color.set(color);
                float angle = random.nextFloat() * MathUtils.PI2, distance = spread * (0.3f + 0.7f * random.nextFloat());
                part.scatterX = fromX + MathUtils.cos(angle) * distance;
                part.scatterY = fromY + Math.abs(MathUtils.sin(angle)) * distance * 0.8f;
                part.targetX = x + (px + 0.5f - width / 2f) * scale;
                part.targetY = y + (height / 2f - py - 0.5f) * scale;
                part.delay = random.nextFloat();
                parts.add(part);
            }
        }
        return parts;
    }

    /**
     * Dessine les particules de {@link #assemble} à la progression {@code t} :
     * elles jaillissent et s'éparpillent (jusqu'à 0,4), puis rejoignent leur place (1 : la forme est faite).
     */
    protected void drawAssembled(Batch batch, Array<Particle> parts, float t) {
        for (Particle part : parts) {
            float px, py;
            if (t < 0.4f) {
                float k = Interpolation.pow2Out.apply(t / 0.4f);
                px = MathUtils.lerp(part.x, part.scatterX, k);
                py = MathUtils.lerp(part.y, part.scatterY, k);
            } else {
                float k = MathUtils.clamp((t - 0.4f - part.delay * 0.25f) / 0.35f, 0f, 1f);
                float e = Interpolation.pow2.apply(k);
                px = MathUtils.lerp(part.scatterX, part.targetX, e);
                py = MathUtils.lerp(part.scatterY, part.targetY, e);
            }
            batch.setColor(part.color);
            batch.draw(pixel, px - part.size / 2f, py - part.size / 2f, part.size, part.size);
        }
    }

    /**
     * @return une ville en ombre chinoise ({@code width} x {@code height} pixels) :
     * immeubles aux fenêtres dorées, enseignes au néon ; {@code ruined} : toits brisés, peu de lumières.
     */
    protected static Pixmap skyline(int width, int height, long seed, boolean ruined) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        Random random = new Random(seed);
        Color body = c("0b0712"), window = c("ffcf5a"), windowDim = c("8a5a2a"), neon = c("ff4fa0"), neon2 = c("4fd8ff");
        int x = 0;
        while (x < width) {
            int buildingWidth = 8 + random.nextInt(14);
            int buildingHeight = Math.min(height - 4, 12 + random.nextInt(Math.max(1, height - 30)));
            int top = height - buildingHeight;
            pixmap.setColor(body);
            pixmap.fillRectangle(x, top, buildingWidth, buildingHeight);
            if (ruined) {                                        // toit brisé : des bouts arrachés
                pixmap.setColor(0, 0, 0, 0);
                for (int k = 0; k < 3; k++) {
                    int bx = x + random.nextInt(buildingWidth), bw = 2 + random.nextInt(4), bh = 2 + random.nextInt(6);
                    pixmap.fillRectangle(bx, top, bw, bh);
                }
            }
            float dark = ruined ? 0.88f : 0.45f;
            for (int wy = top + 3; wy < height - 2; wy += 3) {
                for (int wx = x + 2; wx < x + buildingWidth - 2; wx += 3) {
                    float roll = random.nextFloat();
                    if (roll < dark) continue;
                    pixmap.setColor(roll < (ruined ? 0.97f : 0.85f) ? windowDim : window);
                    pixmap.drawPixel(wx, wy);
                }
            }
            if (!ruined && random.nextFloat() < 0.35f && buildingWidth > 10) {
                pixmap.setColor(random.nextBoolean() ? neon : neon2);
                pixmap.fillRectangle(x + 2, top - 3, buildingWidth - 4, 2);
            }
            x += buildingWidth + random.nextInt(3);
        }
        return pixmap;
    }

    protected static void additive(Batch batch) { batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE); }
    protected static void normal(Batch batch)   { batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA); }

    // -------------------------------------------------------------------------
    // Particules
    // -------------------------------------------------------------------------

    /** Étincelle, flamme, pièce ou débris : part d'un point, ralentit (ou retombe) et s'éteint. */
    protected static final class Particle {
        public float x, y, vx, vy, age, life, size, gravity, drag = 1f, rotation, spin;
        /** Pour {@link #assemble} : où la particule s'envole d'abord, puis sa place dans la forme. */
        public float scatterX, scatterY, targetX, targetY, delay;
        public final Color color = new Color(Color.WHITE);
        public TextureRegion region;

        /** @return {@code true} quand la particule est éteinte. */
        public boolean update(float delta) {
            age += delta;
            vy -= gravity * delta;
            float slow = (float) Math.pow(drag, delta);
            vx *= slow;
            vy *= slow;
            x += vx * delta;
            y += vy * delta;
            rotation += spin * delta;
            return age >= life;
        }

        /** @return la vie écoulée, de 0 à 1. */
        public float life() { return Math.min(1f, age / life); }
    }

    /** Une particule lancée de ({@code x}, {@code y}) dans la direction {@code degrees}. */
    protected Particle particle(float x, float y, float degrees, float speed, float life, float size) {
        Particle particle = new Particle();
        particle.x = x;
        particle.y = y;
        particle.vx = MathUtils.cosDeg(degrees) * speed;
        particle.vy = MathUtils.sinDeg(degrees) * speed;
        particle.life = life;
        particle.size = size;
        return particle;
    }

    /** Fait vivre les particules et retire celles qui sont éteintes. */
    protected static void update(Array<Particle> particles, float delta) {
        for (int i = particles.size - 1; i >= 0; i--) {
            if (particles.get(i).update(delta)) particles.removeIndex(i);
        }
    }

    /**
     * Dessine les particules : leur image ({@code region}, sinon une lueur douce
     * en mélange additif), qui s'éteint avec l'âge.
     */
    protected void drawParticles(Batch batch, Array<Particle> particles) {
        for (Particle particle : particles) {
            float alpha = particle.color.a * (1f - particle.life() * particle.life());
            if (particle.region != null) {
                normal(batch);
                batch.setColor(particle.color.r, particle.color.g, particle.color.b, alpha);
                sprite(batch, particle.region, particle.x, particle.y, particle.size, particle.rotation);
            } else {
                additive(batch);
                batch.setColor(particle.color.r, particle.color.g, particle.color.b, alpha);
                batch.draw(soft, particle.x - particle.size / 2f, particle.y - particle.size / 2f,
                    particle.size, particle.size);
            }
        }
        normal(batch);
    }

    // -------------------------------------------------------------------------
    // Images
    // -------------------------------------------------------------------------

    /** @return une image de la scène, libérée avec elle. */
    protected TextureRegion region(Pixmap pixmap, boolean smooth) {
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        if (smooth) texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        textures.add(texture);
        return new TextureRegion(texture);
    }

    /** @return une image en pixel art : chaque caractère de {@code rows} est un pixel ({@code palette}, '.' = vide). */
    protected TextureRegion art(String palette, Color[] colors, String... rows) {
        int width = 0;
        for (String row : rows) width = Math.max(width, row.length());
        Pixmap pixmap = new Pixmap(width, rows.length, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < rows[y].length(); x++) {
                int index = palette.indexOf(rows[y].charAt(x));
                if (index < 0) continue;
                pixmap.setColor(colors[index]);
                pixmap.drawPixel(x, y);
            }
        }
        return region(pixmap, false);
    }

    /** @return l'image {@code path} des ressources du jeu, libérée avec la scène. */
    protected TextureRegion load(String path) {
        Texture texture = new Texture(com.badlogic.gdx.Gdx.files.internal(path));
        textures.add(texture);
        return new TextureRegion(texture);
    }

    /**
     * Un rouleau de machine à sous dans la fenêtre ({@code x}, {@code y},
     * {@code w}, {@code h}) : les symboles défilent vers le bas ; {@code offset}
     * (en symboles) donne celui du milieu, qui s'arrête pile quand il est entier.
     */
    protected void reel(Batch batch, TextureRegion[] symbols, float x, float y, float w, float h, float offset) {
        int first = MathUtils.floor(offset);
        float shift = offset - first;
        for (int k = -1; k <= 1; k++) {
            TextureRegion symbol = symbols[Math.floorMod(first + k, symbols.length)];
            float top = y + h - (k - shift) * h;      // haut du symbole
            float bottom = top - h;
            float clipTop = Math.min(top, y + h), clipBottom = Math.max(bottom, y);
            if (clipTop <= clipBottom) continue;
            float v0 = (top - clipTop) / h, v1 = (top - clipBottom) / h;
            float u = symbol.getU(), u2 = symbol.getU2(), sv = symbol.getV(), sv2 = symbol.getV2();
            batch.draw(symbol.getTexture(), x, clipBottom, w, clipTop - clipBottom, u, sv + (sv2 - sv) * v1, u2,
                sv + (sv2 - sv) * v0);
        }
    }

    private static Pixmap solid() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        return pixmap;
    }

    /** @return un disque blanc qui s'estompe doucement vers le bord. */
    protected static Pixmap softDisc(int size) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        float center = (size - 1) / 2f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float d = (float) Math.hypot(x - center, y - center) / (size / 2f);
                float a = d >= 1f ? 0f : (1f - d) * (1f - d);
                pixmap.setColor(1f, 1f, 1f, a);
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }

    /** @return un anneau blanc flou (onde de choc). */
    protected static Pixmap softRing(int size) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        float center = (size - 1) / 2f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float d = (float) Math.hypot(x - center, y - center) / (size / 2f);
                float a = Math.max(0f, 1f - Math.abs(d - 0.88f) / 0.1f);
                pixmap.setColor(1f, 1f, 1f, a * a);
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }

    /** @return une traînée : transparente à gauche, éclatante à droite (la tête). */
    protected static Pixmap trailGradient() {
        int width = 128, height = 8;
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        for (int x = 0; x < width; x++) {
            float along = x / (float) (width - 1);
            for (int y = 0; y < height; y++) {
                float across = 1f - Math.abs(y - (height - 1) / 2f) / (height / 2f);
                float taper = along * along;
                pixmap.setColor(1f, 1f, 1f, taper * Math.max(0f, across) * (0.3f + 0.7f * along));
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }

    protected static Color c(String hex) { return Color.valueOf(hex); }

    @Override
    public void dispose() {
        cancel();
        for (Texture texture : textures) texture.dispose();
        textures.clear();
    }
}
