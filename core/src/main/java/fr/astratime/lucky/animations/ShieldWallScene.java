package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import fr.astratime.lucky.assets.Palette;

/**
 * Bingo de l'Écu : le mur de boucliers. Trois écus géants tombent l'un après
 * l'autre devant le joueur et forment un mur ; des étincelles viennent
 * ricocher dessus (le mur tremble sous les coups), puis un blason doré
 * s'allume au-dessus (les étincelles et l'éclat du blason sont lancés par
 * {@link JackpotCelebration}).
 */
public class ShieldWallScene extends BingoScene {

    /** Instants où les écus touchent la table, dans leur ordre d'arrivée : milieu, gauche, droite. */
    public static final float[] LANDINGS = {0.45f, 0.6f, 0.75f};
    /** Les étincelles ricochent sur le mur entre ces deux instants. */
    public static final float HITS_FROM  = 0.85f;
    public static final float HITS_UNTIL = 1.45f;
    /** Le blason doré s'allume. */
    public static final float BLAZON_TIME = 1.55f;

    private static final float FALL_TIME = 0.25f;
    private static final float SPACING   = 190f;      // entre deux écus : ils se chevauchent un peu
    private static final float SHIELD_SCALE = 7f;     // les écus sont géants
    private static final float TREMBLE   = 4f;
    /** Ordre d'arrivée des écus (0 : gauche, 1 : milieu, 2 : droite) : le milieu d'abord. */
    public static final int[] ORDER = {1, 0, 2};

    private static final int SHIELD_WIDTH  = 30;
    private static final int SHIELD_HEIGHT = 36;
    private static final int BLAZON_WIDTH  = 34;
    private static final int BLAZON_HEIGHT = 42;

    private final Image[] shields = new Image[3];
    private final Image blazon, blazonGlow;
    private final float[] homeX = new float[3];
    private float baseY;
    private final Vector2 blazonCenter = new Vector2();

    public ShieldWallScene() {
        blazonGlow = image(texture(halo(64, c("ffe066"))));
        blazonGlow.setOrigin(blazonGlow.getWidth() / 2f, blazonGlow.getHeight() / 2f);
        addActor(blazonGlow);
        blazon = image(texture(blazon()));
        blazon.setOrigin(blazon.getWidth() / 2f, blazon.getHeight() / 2f);
        addActor(blazon);
        TextureRegion shield = new TextureRegion(texture(shield()));
        // Les écus des côtés d'abord : celui du milieu passe devant eux.
        for (int i : new int[] {0, 2, 1}) {
            shields[i] = image(shield, SHIELD_SCALE);
            addActor(shields[i]);
        }
    }

    /** @return la largeur (Stage) du mur d'écus. */
    public float wallWidth() { return SPACING * 2f + shields[0].getWidth(); }

    /** @return la hauteur (Stage) du haut du mur. */
    public float wallTop() { return baseY + shields[0].getHeight(); }

    /** @return le pied (Stage) du mur. */
    public float wallBottom() { return baseY; }

    /** @return le centre (Stage) du blason doré. */
    public Vector2 blazonCenter() { return blazonCenter; }

    /** @return l'abscisse (Stage) du milieu de l'écu {@code index} (0 : gauche, 1 : milieu, 2 : droite). */
    public float shieldX(int index) { return homeX[index] + shields[index].getWidth() / 2f; }

    /** Fait tomber les trois écus, le pied du mur centré en {@code (x, y)}. */
    @Override
    protected void start(float x, float y) {
        float screenHeight = getStage().getViewport().getWorldHeight();
        baseY = y;
        for (int k = 0; k < ORDER.length; k++) {
            int i = ORDER[k];
            Image shield = shields[i];
            homeX[i] = x + (i - 1) * SPACING - shield.getWidth() / 2f;
            float drop = screenHeight - y + 40f;
            shield.setPosition(homeX[i], y + drop);
            shield.setColor(Color.WHITE);
            shield.addAction(Actions.sequence(
                Actions.delay(LANDINGS[k] - FALL_TIME),
                Actions.moveBy(0f, -drop, FALL_TIME, Interpolation.pow2In),
                Actions.moveBy(0f, 12f, 0.06f, Interpolation.pow2Out),
                Actions.moveBy(0f, -12f, 0.08f, Interpolation.pow2In)));
        }
        // Le blason s'allume au-dessus du mur, au moment où les écus se parent d'or.
        float wallTop = y + shields[0].getHeight();
        blazonCenter.set(x, wallTop + blazon.getHeight() / 2f + 30f);
        placeOrigin(blazon, blazonCenter.x, blazonCenter.y);
        blazon.setScale(0f);
        blazon.setColor(Color.WHITE);
        blazon.addAction(Actions.sequence(
            Actions.delay(BLAZON_TIME),
            Actions.scaleTo(1.25f, 1.25f, 0.12f, Interpolation.pow2Out),
            Actions.scaleTo(1f, 1f, 0.25f, Interpolation.swingOut),
            Actions.forever(Actions.sequence(
                Actions.color(c("fff6c8"), 0.25f),
                Actions.color(Color.WHITE, 0.25f)))));
        placeOrigin(blazonGlow, blazonCenter.x, blazonCenter.y);
        blazonGlow.setScale(0.3f);
        blazonGlow.getColor().a = 0f;
        blazonGlow.addAction(Actions.sequence(
            Actions.delay(BLAZON_TIME),
            Actions.parallel(Actions.alpha(1f, 0.1f), Actions.scaleTo(1.1f, 1.1f, 0.35f, Interpolation.pow2Out)),
            Actions.forever(Actions.sequence(Actions.alpha(0.55f, 0.35f), Actions.alpha(1f, 0.35f)))));
        for (Image shield : shields) {
            shield.addAction(Actions.sequence(
                Actions.delay(BLAZON_TIME),
                Actions.color(c("ffe9a0"), 0.1f),
                Actions.color(Color.WHITE, 0.4f)));
        }
    }

    @Override
    protected void update(float delta) {
        // Sous les coups, le mur tremble.
        boolean hit = time >= HITS_FROM && time < HITS_UNTIL;
        for (int i = 0; i < shields.length; i++) {
            if (time < LANDINGS[LANDINGS.length - 1] + 0.15f) break;
            shields[i].setX(homeX[i] + (hit ? MathUtils.random(-TREMBLE, TREMBLE) : 0f));
        }
    }

    /** @return la forme d'un écu de {@code width} x {@code height} : haut droit, flancs arrondis jusqu'à la pointe. */
    private static Shape shieldShape(int width, int height, int top) {
        float half = width / 2f, straight = height * 0.45f;
        return (x, y) -> {
            if (y < top) return false;
            float dx = Math.abs(x + 0.5f - half);
            if (y < straight) return dx <= half;
            float t = (y + 0.5f - straight) / (height - straight);
            return dx <= half * (1f - t * t * t) + 0.5f && t <= 1f;
        };
    }

    /** @return un écu bleu bordé d'or, une croix dorée au milieu, comme le symbole de l'Écu. */
    private static Pixmap shield() {
        Pixmap pixmap = pixmap(SHIELD_WIDTH, SHIELD_HEIGHT);
        Shape outer = shieldShape(SHIELD_WIDTH, SHIELD_HEIGHT, 0);
        Color gold = Palette.GOLD, goldDark = c("c88a10"), blue = c("3a6fd8"), blueLight = c("6a9cff"),
            blueDark = c("23479c");
        fillShape(pixmap, outer, (x, y) -> x < SHIELD_WIDTH / 2 ? gold : goldDark);
        // Champ bleu, en retrait de la bordure dorée.
        for (int y = 3; y < SHIELD_HEIGHT; y++) {
            for (int x = 3; x < SHIELD_WIDTH - 3; x++) {
                if (!(outer.contains(x - 3, y) && outer.contains(x + 3, y) && outer.contains(x, y + 3))) continue;
                Color color = x < SHIELD_WIDTH / 2 ? blue : blueDark;
                if (x < 7 && y < 18) color = blueLight;
                pixmap.setColor(color);
                pixmap.drawPixel(x, y);
            }
        }
        // Croix dorée.
        int cx = SHIELD_WIDTH / 2;
        fillOutlined(pixmap, cx - 3, 6, 6, 21, gold);
        fillOutlined(pixmap, 7, 12, SHIELD_WIDTH - 14, 6, gold);
        pixmap.setColor(gold);
        pixmap.fillRectangle(cx - 2, 7, 4, 19);
        pixmap.setColor(c("fff3a8"));
        pixmap.drawLine(cx - 2, 7, cx - 2, 25);
        pixmap.drawLine(8, 13, SHIELD_WIDTH - 9, 13);
        hLine(pixmap, 1, SHIELD_WIDTH - 2, 1, c("fff3a8"));
        return pixmap;
    }

    /** @return le blason doré : écu d'or ourlé, champ de gueules, fleur de lys et couronne. */
    private static Pixmap blazon() {
        Pixmap pixmap = pixmap(BLAZON_WIDTH, BLAZON_HEIGHT);
        int top = 8;
        Shape outer = shieldShape(BLAZON_WIDTH, BLAZON_HEIGHT, top);
        Color gold = Palette.GOLD, pale = c("fff3a8"), dark = c("c88a10"), red = c("b3122a"), redDark = c("7a0a1c");
        fillShape(pixmap, outer, (x, y) -> x < BLAZON_WIDTH / 2 ? gold : dark);
        for (int y = top + 3; y < BLAZON_HEIGHT; y++) {
            for (int x = 3; x < BLAZON_WIDTH - 3; x++) {
                if (!(outer.contains(x - 3, y) && outer.contains(x + 3, y) && outer.contains(x, y + 3))) continue;
                pixmap.setColor(x < BLAZON_WIDTH / 2 ? red : redDark);
                pixmap.drawPixel(x, y);
            }
        }
        hLine(pixmap, 2, BLAZON_WIDTH - 3, top + 1, pale);
        // Fleur de lys dorée.
        drawGrid(pixmap, new String[] {
            "......o......",
            ".....oGo.....",
            "....oGPGo....",
            "....oGPGo....",
            ".oo.oGPGo.oo.",
            "oGGooGPGooGGo",
            "oGPGoGPGoGPGo",
            ".oGGGGGGGGGo.",
            "..ooGGGGGoo..",
            ".oGGGGGGGGGo.",
            ".oooooGooooo.",
            "....oGGGo....",
            ".....ooo.....",
        }, BLAZON_WIDTH / 2 - 6, top + 9, "oGP", OUTLINE, gold, pale);
        // Couronne à trois fleurons posée sur l'écu.
        drawGrid(pixmap, new String[] {
            "..o.....o.....o..",
            ".oGo...oGo...oGo.",
            ".oGGo.oGPGo.oGGo.",
            ".oGGGoGGGGGoGGGo.",
            ".oGGGGGGGGGGGGGo.",
            ".oRGGGBGGGBGGGRo.",
            ".oDDDDDDDDDDDDDo.",
            "..ooooooooooooo..",
        }, BLAZON_WIDTH / 2 - 8, 0, "oGPRBD", OUTLINE, gold, pale, Palette.RUBY, Palette.SKY, dark);
        return pixmap;
    }
}
