package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Disposable;

/**
 * Projecteurs disco d'un Bingo : des faisceaux de couleur tombent du haut de
 * l'écran, balaient la table en se croisant et changent de couleur au rythme
 * de la fête ; chacun dessine une flaque de lumière là où il touche la table.
 * La lumière est ajoutée (mélange additif) et s'allume puis s'éteint en fondu.
 *
 * Les positions sont celles du Stage : l'acteur doit être placé à l'origine.
 */
public class DiscoSpotlights extends Actor implements Disposable {

    private static final int   BEAMS        = 6;
    private static final float FADE_IN      = 0.25f;
    private static final float FADE_OUT     = 0.5f;
    private static final float ALPHA        = 0.85f;
    private static final float BEAM_WIDTH   = 520f;
    private static final float SWEEP        = 32f;     // amplitude du balayage, en degrés
    private static final float COLOR_STEP   = 0.35f;   // durée d'une couleur avant de passer à la suivante
    private static final float POOL_WIDTH   = 420f;
    private static final float POOL_HEIGHT  = 120f;
    /** Effets réduits : faisceaux moins nombreux, plus lents et plus doux. */
    private static final float REDUCED_ALPHA = 0.4f;

    /** Couleurs franches de boîte de nuit. */
    private static final Color[] COLORS = {
        Color.valueOf("ff3fb4ff"), Color.valueOf("3fe0ffff"), Color.valueOf("ffe23fff"),
        Color.valueOf("5cff6aff"), Color.valueOf("8a5cffff"), Color.valueOf("ff5a3fff"),
    };

    private final Texture       beamTexture;
    private final Texture       poolTexture;
    private final TextureRegion beam;
    private final TextureRegion pool;
    private final Color         tint = new Color();

    private final float[] phase = new float[BEAMS];
    private final float[] speed = new float[BEAMS];
    private float   left, right, top, floor;
    private float   elapsed, duration;
    private boolean running;
    private boolean reduced;

    public DiscoSpotlights() {
        setTouchable(Touchable.disabled);
        beamTexture = toTexture(beamPixmap(64, 256));
        poolTexture = toTexture(poolPixmap(128));
        beamTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        poolTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        beam = new TextureRegion(beamTexture);
        pool = new TextureRegion(poolTexture);
    }

    /**
     * Allume les projecteurs pour {@code duration} secondes au-dessus de la table
     * comprise entre {@code left} et {@code right}, du haut de l'écran {@code top}
     * jusqu'au sol {@code floor}.
     */
    public void play(float left, float right, float top, float floor, float duration, boolean reduced) {
        this.left     = left;
        this.right    = right;
        this.top      = top;
        this.floor    = floor;
        this.duration = duration;
        this.reduced  = reduced;
        elapsed = 0f;
        running = true;
        for (int i = 0; i < BEAMS; i++) {
            phase[i] = MathUtils.random(MathUtils.PI2);
            speed[i] = MathUtils.random(1.6f, 2.6f) * (i % 2 == 0 ? 1f : -1f) * (reduced ? 0.4f : 1f);
        }
    }

    /** Éteint tout immédiatement. */
    public void stop() {
        running = false;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!running) return;
        elapsed += delta;
        if (elapsed >= duration) running = false;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!running) return;
        float fade = Math.min(Interpolation.pow2Out.apply(Math.min(1f, elapsed / FADE_IN)),
            MathUtils.clamp((duration - elapsed) / FADE_OUT, 0f, 1f));
        float alpha = (reduced ? REDUCED_ALPHA : ALPHA) * fade * parentAlpha;
        if (alpha <= 0f) return;

        Color previous = batch.getColor().cpy();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE); // additif : la lumière s'ajoute
        int count = reduced ? BEAMS / 2 : BEAMS;

        for (int i = 0; i < count; i++) {
            // Les projecteurs sont répartis le long du haut de la table et balaient en se croisant.
            float x = left + (right - left) * (i + 0.5f) / count;
            float angle = MathUtils.sin(elapsed * speed[i] + phase[i]) * SWEEP;
            float length = (top - floor) / MathUtils.cosDeg(angle); // le faisceau s'arrête sur la table
            colorOf(i, tint);
            batch.setColor(tint.r, tint.g, tint.b, alpha);
            batch.draw(beam, x - BEAM_WIDTH / 2f, top - length, BEAM_WIDTH / 2f, length,
                BEAM_WIDTH, length, 1f, 1f, angle);

            // Flaque de lumière au point où le faisceau touche la table.
            float poolX = x + MathUtils.sinDeg(angle) * length;
            batch.setColor(tint.r, tint.g, tint.b, alpha * 0.9f);
            batch.draw(pool, poolX - POOL_WIDTH / 2f, floor - POOL_HEIGHT / 2f, POOL_WIDTH, POOL_HEIGHT);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(previous);
    }

    /** Couleur du faisceau {@code index} : elle glisse d'une teinte à la suivante, décalée d'un faisceau à l'autre. */
    private void colorOf(int index, Color out) {
        float step = elapsed / (reduced ? COLOR_STEP * 2.5f : COLOR_STEP) + index * 1.7f;
        int from = MathUtils.floor(step);
        float t = Interpolation.smooth.apply(step - from);
        out.set(COLORS[Math.floorMod(from, COLORS.length)]).lerp(COLORS[Math.floorMod(from + 1, COLORS.length)], t);
    }

    /** @return une texture (avec mipmaps) tirée de {@code pixmap}, qui est libéré. */
    private static Texture toTexture(Pixmap pixmap) {
        Texture texture = new Texture(pixmap, true);
        pixmap.dispose();
        return texture;
    }

    /** Faisceau : cône blanc, pointe en haut, qui s'élargit et s'estompe vers le bas, aux bords adoucis. */
    private static Pixmap beamPixmap(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < height; y++) {
            float v = y / (float) (height - 1);                // 0 à la pointe, 1 au bout
            float half = 0.03f + 0.97f * v;                     // demi-largeur relative du cône
            float along = 0.35f + 0.65f * (1f - v) * (1f - v);  // plus vif près du projecteur
            if (v > 0.9f) along *= (1f - v) / 0.1f;             // bout adouci, sur la flaque de lumière
            for (int x = 0; x < width; x++) {
                float u = Math.abs((x + 0.5f) / width * 2f - 1f) / half;
                float edge = u >= 1f ? 0f : (float) Math.pow(1f - u, 1.1f);
                pixmap.drawPixel(x, y, Color.rgba8888(1f, 1f, 1f, MathUtils.clamp(edge * along, 0f, 1f)));
            }
        }
        return pixmap;
    }

    /** Flaque de lumière : disque blanc dont l'éclat décroît doucement vers les bords. */
    private static Pixmap poolPixmap(int size) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = (x + 0.5f) / size * 2f - 1f, dy = (y + 0.5f) / size * 2f - 1f;
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                float a = d >= 1f ? 0f : (float) Math.pow(1f - d, 1.8f);
                pixmap.drawPixel(x, y, Color.rgba8888(1f, 1f, 1f, a));
            }
        }
        return pixmap;
    }

    @Override
    public void dispose() {
        beamTexture.dispose();
        poolTexture.dispose();
    }
}
