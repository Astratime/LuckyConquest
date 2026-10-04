package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

/**
 * Bingo du Cœur : un cœur géant surgit au milieu de la table et bat trois fois
 * (à {@link #BEATS}), de plus en plus fort, un halo rose palpitant derrière
 * lui ; il enfle une dernière fois en tremblant puis éclate (les ondes roses
 * et les petits cœurs qui retombent sont lancés par {@link JackpotCelebration}).
 */
public class HeartbeatScene extends BingoScene {

    /** Instants des trois battements. */
    public static final float[] BEATS = {0.35f, 0.7f, 1.05f};
    /** Le cœur éclate. */
    public static final float BURST_TIME = 1.6f;

    private static final float HEART_SCALE = 7f;
    private static final float SWELL_TIME  = 0.22f;   // il enfle avant d'éclater
    private static final float TREMBLE     = 6f;

    private static final int HEART_WIDTH  = 30;
    private static final int HEART_HEIGHT = 27;

    private final Image heart, glow;
    private final Vector2 center = new Vector2();

    public HeartbeatScene() {
        glow  = image(texture(halo(56, c("ff6fa0"))));
        glow.setOrigin(glow.getWidth() / 2f, glow.getHeight() / 2f);
        heart = image(new TextureRegion(texture(heart())), HEART_SCALE);
        heart.setOrigin(heart.getWidth() / 2f, heart.getHeight() / 2f);
        addActor(glow);
        addActor(heart);
    }

    /** @return le centre (Stage) du cœur. */
    public Vector2 center() { return center; }

    @Override
    protected void start(float x, float y) {
        center.set(x, y);
        placeOrigin(heart, x, y);
        placeOrigin(glow, x, y);
        heart.setVisible(true);
        heart.setColor(Color.WHITE);
        heart.setScale(0f);
        glow.getColor().a = 0f;
        glow.setScale(0.8f);

        // Il surgit, puis bat : chaque battement plus ample que le précédent.
        SequenceAction beats = Actions.sequence(
            Actions.scaleTo(1.1f, 1.1f, 0.15f, Interpolation.pow2Out),
            Actions.scaleTo(1f, 1f, 0.1f));
        SequenceAction pulses = Actions.sequence();
        float at = 0.25f;
        for (int i = 0; i < BEATS.length; i++) {
            float strength = 1.18f + i * 0.08f;
            beats.addAction(Actions.delay(BEATS[i] - at));
            beats.addAction(Actions.parallel(
                Actions.scaleTo(strength, strength, 0.07f, Interpolation.pow2Out),
                Actions.color(c("ffd0e0"), 0.07f)));
            beats.addAction(Actions.parallel(
                Actions.scaleTo(1f, 1f, 0.22f, Interpolation.pow2In),
                Actions.color(Color.WHITE, 0.22f)));
            pulses.addAction(Actions.delay(BEATS[i] - (i == 0 ? 0f : BEATS[i - 1] + 0.3f)));
            pulses.addAction(Actions.alpha(0.95f, 0.07f));
            pulses.addAction(Actions.alpha(0.35f, 0.23f));
            at = BEATS[i] + 0.29f;
        }
        // Dernier gonflement, puis il éclate.
        beats.addAction(Actions.delay(BURST_TIME - SWELL_TIME - at));
        beats.addAction(Actions.parallel(
            Actions.scaleTo(1.5f, 1.5f, SWELL_TIME, Interpolation.pow2In),
            Actions.color(c("ffe0ec"), SWELL_TIME)));
        beats.addAction(Actions.visible(false));
        heart.addAction(beats);
        pulses.addAction(Actions.delay(BURST_TIME - BEATS[BEATS.length - 1] - 0.3f));
        pulses.addAction(Actions.parallel(Actions.alpha(1f, 0.05f), Actions.scaleTo(1.6f, 1.6f, 0.3f)));
        pulses.addAction(Actions.fadeOut(0.3f));
        glow.addAction(pulses);
    }

    @Override
    protected void update(float delta) {
        // Juste avant d'éclater, le cœur tremble.
        float since = time - (BURST_TIME - SWELL_TIME);
        if (since < 0f || time >= BURST_TIME) return;
        float shake = TREMBLE * since / SWELL_TIME;
        placeOrigin(heart, center.x + MathUtils.random(-shake, shake), center.y + MathUtils.random(-shake, shake));
    }

    /** @return le cœur : rouge framboise, reflet clair en haut à gauche, ombre en bas à droite. */
    private static Pixmap heart() {
        Pixmap pixmap = pixmap(HEART_WIDTH, HEART_HEIGHT);
        Shape shape = (x, y) -> {
            // Courbe du cœur : (u² + v² - 1)³ - u² v³ ≤ 0, v vers le haut.
            float u = (x + 0.5f - HEART_WIDTH / 2f) / (HEART_WIDTH / 2f) * 1.16f;
            float v = 1.24f - (y + 0.5f) / HEART_HEIGHT * 2.28f;
            float a = u * u + v * v - 1f;
            return a * a * a - u * u * v * v * v <= 0f;
        };
        Color red = c("e8264f"), light = c("ff7a9a"), shine = c("ffd0dc"), dark = c("a8123a");
        fillShape(pixmap, shape, (x, y) -> {
            float dx = x - HEART_WIDTH / 2f, dy = y - HEART_HEIGHT * 0.4f;
            if (x >= 5 && x <= 7 && y >= 4 && y <= 6 && !(x == 7 && y == 6)) return shine;
            if (dx + dy > 10f || (dx > 10f && dy > -3f)) return dark;
            if (dx < -4f && dy < 1f) return light;
            return red;
        });
        return pixmap;
    }
}
