package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

/**
 * Bingo de l'Étoile : une étoile filante dorée traverse l'écran en diagonale,
 * depuis le coin en haut à gauche, en tournoyant et en grossissant, une
 * longue traîne de lumière derrière elle ; au milieu de la table, elle
 * explose en feu d'artifice rouge, bleu et or (la traîne et les gerbes sont
 * lancées par {@link JackpotCelebration}, qui suit {@link #starPosition()}).
 */
public class ShootingStarScene extends BingoScene {

    /** L'étoile part du coin de l'écran. */
    public static final float LAUNCH_TIME = 0.15f;
    /** L'étoile explose. */
    public static final float BURST_TIME = 1.1f;

    private static final float STAR_SCALE = 6f;
    private static final float SPIN       = -540f;   // tours de l'étoile pendant sa traversée
    private static final float CURVE      = 180f;    // sa course s'incurve vers le bas

    private static final int STAR_SIZE = 25;

    private final Image star, glow;
    private final Vector2 from = new Vector2(), to = new Vector2(), position = new Vector2();

    public ShootingStarScene() {
        glow = image(texture(halo(40, c("fff0a0"))));
        glow.setOrigin(glow.getWidth() / 2f, glow.getHeight() / 2f);
        star = image(new TextureRegion(texture(star())), STAR_SCALE);
        star.setOrigin(star.getWidth() / 2f, star.getHeight() / 2f);
        addActor(glow);
        addActor(star);
    }

    /** @return la position (Stage) actuelle de l'étoile, puis celle de l'explosion. */
    public Vector2 starPosition() { return position; }

    /** L'étoile part du coin en haut à gauche de l'écran et explose en {@code (x, y)}. */
    @Override
    protected void start(float x, float y) {
        float screenHeight = getStage().getViewport().getWorldHeight();
        from.set(x - 900f, screenHeight + 80f);
        to.set(x, y);
        position.set(from);
        star.setVisible(false);
        glow.setVisible(false);
        star.setColor(Color.WHITE);
        update(0f);
    }

    @Override
    protected void update(float delta) {
        if (time < LAUNCH_TIME) return;
        if (time >= BURST_TIME) {
            position.set(to);
            star.setVisible(false);
            glow.setVisible(false);
            return;
        }
        float progress = (time - LAUNCH_TIME) / (BURST_TIME - LAUNCH_TIME);
        float eased = Interpolation.pow2In.apply(progress) * 0.55f + progress * 0.45f; // elle accélère
        position.set(MathUtils.lerp(from.x, to.x, eased),
            MathUtils.lerp(from.y, to.y, eased) + CURVE * 4f * eased * (1f - eased));
        star.setVisible(true);
        glow.setVisible(true);
        float scale = 0.55f + 0.45f * eased;
        star.setScale(scale);
        star.setRotation(SPIN * eased);
        placeOrigin(star, position.x, position.y);
        glow.setScale(scale * (1.2f + 0.15f * MathUtils.sin(time * 30f)));
        placeOrigin(glow, position.x, position.y);
    }

    /** @return l'étoile à cinq branches : or, cœur blanc, reflet sur les branches de gauche. */
    private static Pixmap star() {
        Pixmap pixmap = pixmap(STAR_SIZE, STAR_SIZE);
        float center = STAR_SIZE / 2f, outer = 12.2f, inner = 5.2f;
        // Les dix sommets de l'étoile, pointe en haut.
        float[] vx = new float[10], vy = new float[10];
        for (int i = 0; i < 10; i++) {
            float radius = i % 2 == 0 ? outer : inner;
            float angle = 90f + i * 36f;
            vx[i] = center + MathUtils.cosDeg(angle) * radius;
            vy[i] = center + 0.8f - MathUtils.sinDeg(angle) * radius;
        }
        Shape shape = (x, y) -> {
            float px = x + 0.5f, py = y + 0.5f;
            boolean inside = false;
            for (int i = 0, j = 9; i < 10; j = i++) {
                if ((vy[i] > py) != (vy[j] > py)
                    && px < (vx[j] - vx[i]) * (py - vy[i]) / (vy[j] - vy[i]) + vx[i]) inside = !inside;
            }
            return inside;
        };
        Color gold = c("ffd54a"), pale = c("fff3a8"), white = Color.WHITE, dark = c("e09a1a");
        fillShape(pixmap, shape, (x, y) -> {
            float dx = x + 0.5f - center, dy = y + 0.5f - center - 0.8f;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance < 2.5f) return white;
            if (distance < 4.5f) return pale;
            if (dx + dy > 3f) return dark;
            return dx < -1f ? pale : gold;
        });
        return pixmap;
    }
}
