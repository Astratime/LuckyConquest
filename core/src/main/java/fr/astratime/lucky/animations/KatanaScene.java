package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

/**
 * Bingo de la Pastèque : le sabre. Une pastèque entière est lancée en l'air
 * en tournant ; au sommet de sa course, un éclair de lame la tranche net. Les
 * deux moitiés s'écartent au ralenti en montrant leur chair rouge, puis
 * retombent (le jus, les pépins et les tranches sont lancés par
 * {@link JackpotCelebration}).
 */
public class KatanaScene extends BingoScene {

    /** La lame tranche la pastèque, au sommet de sa course. */
    public static final float SLICE_TIME = 0.9f;

    private static final float SLOW_MOTION  = 0.4f;    // les moitiés s'écartent lentement
    private static final float SPREAD_SPEED = 130f;    // vitesse d'écartement, au ralenti
    private static final float SPIN         = 70f;     // rotation des moitiés, degrés par seconde
    private static final float GRAVITY      = 1800f;
    private static final float SLASH_ANGLE  = 72f;
    private static final float SLASH_LENGTH = 720f;

    private static final int MELON_WIDTH  = 44;
    private static final int MELON_HEIGHT = 34;
    private static final int HALF         = MELON_WIDTH / 2;

    private final Image melon, left, right, slash, slashGlow;
    private final Vector2 apex = new Vector2();
    private float fallSpeed;

    public KatanaScene() {
        melon = image(texture(melon(false, false)));
        melon.setOrigin(melon.getWidth() / 2f, melon.getHeight() / 2f);
        left  = image(texture(melon(true, false)));
        left.setOrigin(left.getWidth(), left.getHeight() / 2f);
        right = image(texture(melon(true, true)));
        right.setOrigin(0f, right.getHeight() / 2f);
        TextureRegion white = new TextureRegion(texture(whitePixel()));
        slashGlow = image(white, 1f);
        slash     = image(white, 1f);
        addActor(melon);
        addActor(left);
        addActor(right);
        addActor(slashGlow);
        addActor(slash);
    }

    /** @return le point (Stage) où la pastèque est tranchée. */
    public Vector2 apex() { return apex; }

    /** Lance la pastèque depuis le bas de l'écran ; elle est tranchée en {@code (x, y)}. */
    @Override
    protected void start(float x, float y) {
        apex.set(x, y);
        fallSpeed = 0f;
        placeOrigin(melon, x, -melon.getHeight());
        melon.setVisible(true);
        melon.setRotation(0f);
        // Montée qui ralentit fortement en approchant du sommet (effet de ralenti).
        melon.addAction(Actions.parallel(
            Actions.moveTo(x - melon.getOriginX(), y - melon.getOriginY(), SLICE_TIME, Interpolation.pow3Out),
            Actions.rotateBy(-200f, SLICE_TIME, Interpolation.pow2Out)));
        left.setVisible(false);
        right.setVisible(false);

        for (Image line : new Image[] {slashGlow, slash}) {
            boolean glow = line == slashGlow;
            float thickness = glow ? 34f : 10f;
            line.setSize(SLASH_LENGTH, thickness);
            line.setOrigin(SLASH_LENGTH / 2f, thickness / 2f);
            placeOrigin(line, x, y);
            line.setRotation(SLASH_ANGLE);
            line.setColor(glow ? new Color(0.7f, 1f, 0.8f, 0.45f) : Color.WHITE);
            line.setScale(0f, 1f);
            line.setVisible(false);
            line.addAction(Actions.sequence(
                Actions.delay(SLICE_TIME - 0.05f),
                Actions.visible(true),
                Actions.scaleTo(1f, 1f, 0.05f, Interpolation.pow2Out),
                Actions.parallel(Actions.scaleTo(1f, 0f, 0.25f), Actions.fadeOut(0.25f)),
                Actions.visible(false)));
        }
    }

    @Override
    protected void update(float delta) {
        if (time < SLICE_TIME) return;
        if (melon.isVisible()) {
            // Tranchée : les deux moitiés remplacent la pastèque, de part et d'autre de la coupe.
            melon.setVisible(false);
            float angle = melon.getRotation();
            placeOrigin(left, apex.x, apex.y);
            placeOrigin(right, apex.x, apex.y);
            left.setRotation(angle % 360f);
            right.setRotation(angle % 360f);
            left.setVisible(true);
            right.setVisible(true);
        }
        float since = time - SLICE_TIME;
        float speed = since < SLOW_MOTION ? SPREAD_SPEED : SPREAD_SPEED * 3f;
        if (since >= SLOW_MOTION) fallSpeed += GRAVITY * delta;
        left.moveBy(-speed * delta, -fallSpeed * delta);
        right.moveBy(speed * delta, -fallSpeed * delta);
        left.rotateBy(SPIN * delta);
        right.rotateBy(-SPIN * delta);
    }

    /**
     * @return la pastèque entière (écorce rayée), ou une de ses moitiés
     *         ({@code half}) : la gauche, ou la droite si {@code rightHalf}, avec
     *         sa chair rouge et ses pépins sur la face coupée.
     */
    private static Pixmap melon(boolean half, boolean rightHalf) {
        int width = half ? HALF : MELON_WIDTH;
        int offset = half && rightHalf ? HALF : 0;
        Pixmap pixmap = pixmap(width, MELON_HEIGHT);
        float rx = MELON_WIDTH / 2f, ry = MELON_HEIGHT / 2f;
        for (int y = 0; y < MELON_HEIGHT; y++) {
            for (int px = 0; px < width; px++) {
                int x = px + offset;
                float dx = (x + 0.5f - rx) / rx, dy = (y + 0.5f - ry) / ry;
                float d = dx * dx + dy * dy;
                if (d > 1f) continue;
                Color color;
                // Distance à la coupe (au milieu) : la face coupée montre la chair.
                int fromCut = rightHalf ? px : width - 1 - px;
                if (d > 0.84f)                         color = OUTLINE;
                else if (half && fromCut == 0)         color = c("ffe3ec");      // bord de la coupe
                else if (half && fromCut < 7 && d < 0.62f) {
                    boolean seed = (x * 7 + y * 3) % 11 == 0 && d < 0.5f;
                    color = seed ? c("1a1a1a") : (d < 0.3f ? c("ff5a6a") : c("e0303c"));
                } else if (half && fromCut < 7 && d < 0.74f) color = c("eaffea"); // blanc de l'écorce
                else {
                    boolean stripe = ((int) (dx * 6f + 20f + dy * dy * 2f)) % 2 == 0;
                    color = stripe ? c("1f7a2e") : c("3fa84a");
                    if (dy < -0.5f && dx < -0.1f && d < 0.7f) color = c("6cc45a");  // reflet
                }
                pixmap.setColor(color);
                pixmap.drawPixel(px, y);
            }
        }
        return pixmap;
    }

    private static Pixmap whitePixel() {
        Pixmap pixmap = pixmap(1, 1);
        pixmap.setColor(Color.WHITE);
        pixmap.drawPixel(0, 0);
        return pixmap;
    }
}
