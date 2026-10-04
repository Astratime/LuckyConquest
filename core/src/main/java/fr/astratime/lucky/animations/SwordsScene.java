package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import fr.astratime.lucky.assets.Palette;

/**
 * Bingo de l'Épée : trois épées tombent du ciel, pointe en bas, et se plantent
 * l'une après l'autre devant l'ennemi. Elles s'arrachent du sol en
 * s'illuminant, puis disparaissent dans deux grands coups de lame qui
 * tranchent l'écran d'un X lumineux (les étincelles sont lancées par
 * {@link JackpotCelebration}).
 */
public class SwordsScene extends BingoScene {

    /** Instants où les épées se plantent, dans leur ordre d'arrivée : milieu, gauche, droite. */
    public static final float[] LANDINGS = {0.45f, 0.6f, 0.75f};
    /** Ordre d'arrivée des épées (0 : gauche, 1 : milieu, 2 : droite) : le milieu d'abord. */
    public static final int[] ORDER = {1, 0, 2};
    /** Premier coup de lame (« \ ») ; le second (« / ») suit de {@link #SECOND_SLASH}. */
    public static final float SLASH_TIME   = 1.3f;
    public static final float SECOND_SLASH = 0.12f;
    /** Inclinaison des deux coups de lame, de part et d'autre de l'horizontale. */
    public static final float SLASH_ANGLE  = 32f;

    private static final float FALL_TIME   = 0.22f;
    private static final float PULL_TIME   = 1.0f;    // les épées s'arrachent du sol
    private static final float PULL_HEIGHT = 50f;
    private static final float SPACING     = 210f;
    private static final float SWORD_SCALE = 6f;
    private static final float EMBED       = 7f * SWORD_SCALE;   // longueur de lame enfoncée dans la table
    private static final float SLASH_LENGTH = 1700f;
    private static final float SLASH_HOLD  = 0.2f;     // le X reste un instant à pleine lumière
    private static final float[] TILTS     = {7f, 0f, -7f};   // les épées des côtés penchent vers le milieu

    private static final int SWORD_WIDTH  = 13;
    private static final int SWORD_HEIGHT = 54;

    private final Image[] swords = new Image[3];
    private final Image[] glows  = new Image[2];
    private final Image[] cores  = new Image[2];
    private final Vector2 center = new Vector2();
    private final Vector2[] tips = {new Vector2(), new Vector2(), new Vector2()};

    public SwordsScene() {
        TextureRegion sword = new TextureRegion(texture(sword()));
        for (int i = 0; i < swords.length; i++) {
            swords[i] = image(sword, SWORD_SCALE);
            swords[i].setOrigin(swords[i].getWidth() / 2f, 0f); // la pointe
            addActor(swords[i]);
        }
        TextureRegion white = new TextureRegion(texture(whitePixel()));
        for (int i = 0; i < 2; i++) {
            glows[i] = image(white, 1f);
            cores[i] = image(white, 1f);
            addActor(glows[i]);
            addActor(cores[i]);
        }
    }

    /** @return le point (Stage) où se croisent les deux coups de lame. */
    public Vector2 center() { return center; }

    /** @return la position (Stage) où l'épée {@code index} (0 : gauche, 1 : milieu, 2 : droite) entre dans la table. */
    public Vector2 groundPoint(int index) { return tips[index]; }

    /**
     * Plante les épées, la ligne du sol à l'ordonnée {@code y} et centrée en
     * {@code x} ; le X se croise en {@code (x, y)} lui aussi.
     */
    @Override
    protected void start(float x, float y) {
        float screenHeight = getStage().getViewport().getWorldHeight();
        center.set(x, y);
        for (int k = 0; k < ORDER.length; k++) {
            int i = ORDER[k];
            Image sword = swords[i];
            float tipX = x + (i - 1) * SPACING;
            tips[i].set(tipX, y);
            float drop = screenHeight - y + 80f;
            placeOrigin(sword, tipX, y - EMBED + drop);
            sword.setRotation(TILTS[i]);
            sword.setColor(Color.WHITE);
            sword.setScale(1f);
            sword.setVisible(true);
            sword.getColor().a = 1f;
            sword.addAction(Actions.sequence(
                Actions.delay(LANDINGS[k] - FALL_TIME),
                Actions.moveBy(0f, -drop, FALL_TIME, Interpolation.pow3In),
                Actions.rotateBy(-TILTS[i] * 0.3f, 0.05f),
                Actions.rotateBy(TILTS[i] * 0.3f, 0.2f, Interpolation.elasticOut),
                Actions.delay(PULL_TIME - LANDINGS[k] - 0.25f),
                // Elles s'arrachent du sol en brillant, puis filent dans les coups de lame.
                Actions.parallel(
                    Actions.moveBy(0f, PULL_HEIGHT + EMBED, 0.25f, Interpolation.pow2Out),
                    Actions.rotateTo(0f, 0.25f),
                    Actions.color(c("fff0c0"), 0.25f)),
                Actions.delay(SLASH_TIME - PULL_TIME - 0.25f - 0.05f),
                Actions.parallel(Actions.scaleTo(1.4f, 1.4f, 0.12f), Actions.fadeOut(0.12f)),
                Actions.visible(false)));
        }

        for (int i = 0; i < 2; i++) {
            float angle = i == 0 ? -SLASH_ANGLE : SLASH_ANGLE;
            float delay = SLASH_TIME + i * SECOND_SLASH;
            for (Image line : new Image[] {glows[i], cores[i]}) {
                boolean glow = line == glows[i];
                float thickness = glow ? 46f : 12f;
                line.setSize(SLASH_LENGTH, thickness);
                line.setOrigin(SLASH_LENGTH / 2f, thickness / 2f);
                placeOrigin(line, x, y);
                line.setRotation(angle);
                line.setColor(glow ? new Color(1f, 0.6f, 0.2f, 0.5f) : c("fff6dc"));
                line.setScale(0f, 1f);
                line.setVisible(false);
                line.addAction(Actions.sequence(
                    Actions.delay(delay - 0.06f),
                    Actions.visible(true),
                    Actions.scaleTo(1f, 1f, 0.06f, Interpolation.pow2Out),
                    Actions.delay(SLASH_HOLD),
                    Actions.parallel(Actions.scaleTo(1f, 0f, 0.35f), Actions.fadeOut(0.35f)),
                    Actions.visible(false)));
            }
        }
    }

    /** @return l'épée, pointe en bas : pommeau et garde dorés, poignée de cuir, lame d'acier à gouttière. */
    private static Pixmap sword() {
        Pixmap pixmap = pixmap(SWORD_WIDTH, SWORD_HEIGHT);
        int center = SWORD_WIDTH / 2;
        // Pommeau.
        fillOutlinedCircle(pixmap, center, 3, 3, Palette.GOLD);
        pixmap.setColor(c("fff3a8"));
        pixmap.drawPixel(center - 1, 2);
        // Poignée.
        fillOutlined(pixmap, center - 2, 6, 5, 10, c("6a3a1c"));
        pixmap.setColor(c("9a5a2c"));
        for (int y = 8; y < 15; y += 2) pixmap.drawLine(center - 1, y, center + 1, y - 1);
        // Garde.
        fillOutlined(pixmap, 0, 15, SWORD_WIDTH, 4, Palette.GOLD);
        hLine(pixmap, 1, SWORD_WIDTH - 2, 16, c("fff3a8"));
        pixmap.setColor(Palette.RUBY);
        pixmap.fillRectangle(center - 1, 16, 2, 2);
        // Lame, qui s'affine en pointe.
        int bladeFrom = 19, tipFrom = SWORD_HEIGHT - 7;
        for (int y = bladeFrom; y < SWORD_HEIGHT; y++) {
            int half = y < tipFrom ? 3 : Math.max(0, 3 - (y - tipFrom + 1) / 2);
            hLine(pixmap, center - half - 1, center + half + 1, y, OUTLINE);
            if (half <= 0) continue;
            hLine(pixmap, center - half, center - 1, y, c("eef4fa"));
            hLine(pixmap, center + 1, center + half, y, Palette.STEEL);
            pixmap.setColor(y < tipFrom ? Palette.STEEL_DARK : Palette.STEEL_LIGHT);
            pixmap.drawPixel(center, y); // gouttière
        }
        return pixmap;
    }
}
