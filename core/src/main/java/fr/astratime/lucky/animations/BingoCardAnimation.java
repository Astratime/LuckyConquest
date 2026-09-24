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
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.settings.VisualSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * Carte Bingo jouée : elle s'élève au centre de l'écran, qui s'assombrit, et
 * des faisceaux lumineux dorés jaillissent d'elle en tournant. Puis elle se
 * téléporte : elle s'étire en un trait de lumière et disparaît dans un éclair,
 * pour réapparaître de la même façon sur la défausse, où elle se pose.
 *
 * Posé par-dessus le jeu, l'acteur bloque les clics pendant l'animation.
 */
public class BingoCardAnimation extends Group implements Disposable {

    /** Durée totale de l'animation, en secondes. */
    public static final float DURATION = 3.0f;

    private static final float RISE_TIME     = 0.45f;
    private static final float BEAMS_TIME    = 1.5f;   // faisceaux, après l'élévation
    private static final float BEAMS_GROW    = 0.4f;
    private static final float VANISH_TIME   = 0.3f;
    private static final float APPEAR_TIME   = 0.35f;
    private static final float SETTLE_TIME   = 0.4f;
    private static final float RISE_SCALE    = 1.6f;
    private static final float STRETCH_Y     = 2.2f;  // la carte s'étire en trait de lumière

    private static final int   BEAMS         = 16;
    private static final float BEAM_LENGTH   = 1100f;
    private static final float BEAM_WIDTH    = 70f;
    private static final float BEAM_SPIN     = 25f;   // degrés par seconde
    private static final float DIM_ALPHA     = 0.55f;
    private static final float PILLAR_WIDTH  = 46f;
    private static final float PILLAR_HEIGHT = 700f;
    private static final int   SPARKS        = 40;

    private static final Color GOLD  = Color.valueOf("ffd54aff");
    private static final Color WHITE = new Color(1f, 1f, 0.9f, 1f);

    private final VisualSettings settings;
    private final TextureRegion  pixel;
    private final Texture        beamTexture;
    private final TextureRegion  beam;
    private final List<float[]>  sparks = new ArrayList<>(); // x, y, vx, vy, vie restante, vie totale, taille

    private Actor    card;
    private float    time = -1f;          // < 0 : aucune animation en cours
    private float    centerX, centerY;    // centre de la carte pendant les faisceaux
    private float    landX, landY;        // centre de la carte sur la défausse
    private Runnable onDone;

    /** @param pixel région d'un pixel blanc (voile sombre, colonnes de lumière, étincelles) */
    public BingoCardAnimation(VisualSettings settings, TextureRegion pixel) {
        this.settings = settings;
        this.pixel    = pixel;
        beamTexture   = createBeamTexture();
        beam          = new TextureRegion(beamTexture);
        setTouchable(Touchable.disabled);
    }

    /**
     * Joue l'animation sur {@code card}, déjà sortie de la main (coordonnées du Stage).
     *
     * @param centerX centre de l'écran où la carte s'élève
     * @param discardX coin bas gauche de la carte du dessus de la défausse
     * @param onDone  appelé quand la carte s'est posée sur la défausse
     */
    public void play(Actor card, float centerX, float centerY, float discardX, float discardY, Runnable onDone) {
        this.card    = card;
        this.centerX = centerX;
        this.centerY = centerY;
        this.landX   = discardX + card.getWidth() / 2f;
        this.landY   = discardY + card.getHeight() / 2f;
        this.onDone  = onDone;
        time = 0f;
        sparks.clear();
        setSize(getStage().getViewport().getWorldWidth(), getStage().getViewport().getWorldHeight());
        setTouchable(Touchable.enabled); // bloque les clics pendant l'animation
        toFront();

        addActor(card);
        card.clearActions();
        card.setOrigin(card.getWidth() / 2f, card.getHeight() / 2f);
        card.addAction(Actions.sequence(
            Actions.parallel(
                Actions.moveTo(centerX - card.getWidth() / 2f, centerY - card.getHeight() / 2f, RISE_TIME,
                    Interpolation.pow2Out),
                Actions.scaleTo(RISE_SCALE, RISE_SCALE, RISE_TIME, Interpolation.pow2Out)),
            Actions.delay(BEAMS_TIME),
            // Disparition : la carte s'étire en un trait de lumière vertical.
            Actions.parallel(
                Actions.scaleTo(0f, RISE_SCALE * STRETCH_Y, VANISH_TIME, Interpolation.pow3In),
                Actions.color(new Color(3f, 3f, 2f, 1f), VANISH_TIME)),
            Actions.run(() -> burst(centerX, centerY)),
            // Réapparition sur la défausse, étirée, puis elle se pose.
            Actions.moveTo(landX - card.getWidth() / 2f, landY - card.getHeight() / 2f),
            Actions.scaleTo(0f, STRETCH_Y),
            Actions.parallel(
                Actions.scaleTo(1f, 1f, APPEAR_TIME, Interpolation.swingOut),
                Actions.color(Color.WHITE, APPEAR_TIME)),
            Actions.run(() -> burst(landX, landY)),
            Actions.fadeOut(SETTLE_TIME)));
    }

    /** @return {@code true} pendant l'animation. */
    public boolean isPlaying() { return time >= 0f; }

    /** Interrompt l'animation sans appeler la suite (nouveau combat). */
    public void cancel() {
        time = -1f;
        sparks.clear();
        clearChildren();
        onDone = null;
        setTouchable(Touchable.disabled);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (time < 0f) return;
        time += delta;
        for (int i = sparks.size() - 1; i >= 0; i--) {
            float[] s = sparks.get(i);
            s[0] += s[2] * delta;
            s[1] += s[3] * delta;
            s[3] -= 300f * delta;
            s[4] -= delta;
            if (s[4] <= 0f) sparks.remove(i);
        }
        if (time < RISE_TIME + BEAMS_TIME && time > RISE_TIME && MathUtils.randomBoolean(0.5f)) {
            addSpark(centerX, centerY, 220f);
        }
        if (time >= DURATION) {
            Runnable done = onDone;
            cancel();
            if (done != null) done.run();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (time < 0f) return;
        float r = batch.getColor().r, g = batch.getColor().g, b = batch.getColor().b, a = batch.getColor().a;

        // Voile sombre : l'attention va à la carte.
        float dim = DIM_ALPHA * fadeInOut(0f, DURATION, 0.3f, 0.5f);
        batch.setColor(0f, 0f, 0f, dim * parentAlpha);
        batch.draw(pixel, 0f, 0f, getWidth(), getHeight());

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE); // lumière additive
        drawBeams(batch, parentAlpha);
        float vanishAt = RISE_TIME + BEAMS_TIME;
        drawPillar(batch, centerX, centerY,
            fadeInOut(vanishAt - 0.1f, vanishAt + VANISH_TIME + 0.25f, 0.15f, 0.25f), parentAlpha);
        float appearAt = vanishAt + VANISH_TIME;
        drawPillar(batch, landX, landY,
            fadeInOut(appearAt - 0.1f, appearAt + APPEAR_TIME + 0.3f, 0.1f, 0.3f), parentAlpha);
        for (float[] s : sparks) {
            float life = s[4] / s[5];
            batch.setColor(GOLD.r, GOLD.g, GOLD.b * 0.8f + 0.2f * life, life * parentAlpha);
            batch.draw(pixel, s[0] - s[6] / 2f, s[1] - s[6] / 2f, s[6], s[6]);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(r, g, b, a);

        super.draw(batch, parentAlpha); // la carte
    }

    /** Faisceaux tournants, dorés et blancs en alternance, qui s'allongent puis s'éteignent avant la téléportation. */
    private void drawBeams(Batch batch, float parentAlpha) {
        float start = RISE_TIME * 0.6f;
        float end   = RISE_TIME + BEAMS_TIME + VANISH_TIME * 0.5f;
        if (time < start || time > end) return;
        float grow  = Interpolation.pow2Out.apply(Math.min(1f, (time - start) / BEAMS_GROW));
        float alpha = fadeInOut(start, end, BEAMS_GROW, 0.3f);
        int   count = settings.isReducedEffects() ? BEAMS / 2 : BEAMS;
        for (int i = 0; i < count; i++) {
            float angle  = i * 360f / count + time * BEAM_SPIN * (i % 2 == 0 ? 1f : -0.6f);
            float length = BEAM_LENGTH * grow * (i % 2 == 0 ? 1f : 0.7f);
            float pulse  = 0.75f + 0.25f * MathUtils.sin(time * 9f + i);
            Color color  = i % 2 == 0 ? GOLD : WHITE;
            batch.setColor(color.r, color.g, color.b, alpha * pulse * 0.55f * parentAlpha);
            batch.draw(beam, centerX, centerY - BEAM_WIDTH / 2f, 0f, BEAM_WIDTH / 2f, length, BEAM_WIDTH, 1f, 1f, angle);
        }
    }

    /** Colonne de lumière verticale centrée sur {@code (x, y)} (téléportation), d'intensité {@code strength}. */
    private void drawPillar(Batch batch, float x, float y, float strength, float parentAlpha) {
        if (strength <= 0f) return;
        for (int k = 0; k < 3; k++) {
            float width = PILLAR_WIDTH * (1f - k * 0.3f) * strength;
            batch.setColor(1f, 0.95f, 0.7f, 0.35f * strength * parentAlpha);
            batch.draw(beam, x, y - width / 2f, 0f, width / 2f, PILLAR_HEIGHT / 2f, width, 1f, 1f, 90f);
            batch.draw(beam, x, y - width / 2f, 0f, width / 2f, PILLAR_HEIGHT / 2f, width, 1f, 1f, -90f);
        }
        batch.setColor(1f, 1f, 1f, 0.6f * strength * parentAlpha);
        batch.draw(pixel, x - 2f * strength, y - PILLAR_HEIGHT / 2f, 4f * strength, PILLAR_HEIGHT);
    }

    /** @return une intensité entre 0 et 1 : monte en {@code in} secondes après {@code from}, retombe en {@code out} avant {@code to}. */
    private float fadeInOut(float from, float to, float in, float out) {
        if (time < from || time > to) return 0f;
        return Math.min(1f, Math.min((time - from) / in, (to - time) / out));
    }

    /** Gerbe d'étincelles au point {@code (x, y)}. */
    private void burst(float x, float y) {
        int count = settings.isReducedEffects() ? SPARKS / 3 : SPARKS;
        for (int i = 0; i < count; i++) addSpark(x, y, 420f);
    }

    private void addSpark(float x, float y, float speed) {
        float angle = MathUtils.random(360f);
        float v     = MathUtils.random(0.3f, 1f) * speed;
        float life  = MathUtils.random(0.4f, 0.9f);
        sparks.add(new float[] { x, y, MathUtils.cosDeg(angle) * v, MathUtils.sinDeg(angle) * v, life, life,
            MathUtils.random(3f, 7f) });
    }

    /** Faisceau : opaque à sa base, qui s'estompe vers sa pointe et sur ses bords. */
    private static Texture createBeamTexture() {
        int w = 128, h = 32;
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        for (int x = 0; x < w; x++) {
            float along = 1f - x / (float) (w - 1);
            for (int y = 0; y < h; y++) {
                float across = Math.abs(y / (float) (h - 1) * 2f - 1f);
                float alpha  = along * along * (1f - across * across);
                pixmap.setColor(1f, 1f, 1f, alpha);
                pixmap.drawPixel(x, y);
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void dispose() {
        beamTexture.dispose();
    }
}
