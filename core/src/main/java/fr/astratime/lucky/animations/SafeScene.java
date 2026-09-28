package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

/**
 * Bingo du Lingot : le coffre-fort. Un lourd coffre tombe du ciel et s'écrase
 * sur la table ; sa molette tourne cran par cran pour composer la
 * combinaison, puis sa porte s'ouvre d'un coup sur des piles de lingots (le
 * flot de lingots et de pièces est lancé par {@link JackpotCelebration}).
 */
public class SafeScene extends BingoScene {

    /** Le coffre touche la table. */
    public static final float LAND_TIME = 0.35f;
    /** La porte s'ouvre. */
    public static final float OPEN_TIME = 1.15f;

    private static final float OPEN_DURATION = 0.18f;
    /** Combinaison : rotations successives de la molette (degrés, sens inverse des aiguilles d'une montre). */
    private static final float[] COMBINATION = {120f, 90f, -150f, -60f, 200f};

    private static final int BODY_WIDTH  = 52;
    private static final int BODY_HEIGHT = 56;
    private static final int DOOR_X      = 6;     // porte, dans l'image du coffre
    private static final int DOOR_Y      = 6;
    private static final int DOOR_WIDTH  = 40;
    private static final int DOOR_HEIGHT = 42;
    private static final int DIAL_SIZE   = 16;

    private final Image body, inside, door, dial;
    private final Group doorGroup = new Group();   // porte et molette pivotent ensemble sur la charnière
    private final Vector2 opening = new Vector2();

    public SafeScene() {
        body   = image(texture(body()));
        inside = image(texture(inside()));
        door   = image(texture(door()));
        dial   = image(texture(dial()));
        dial.setOrigin(dial.getWidth() / 2f, dial.getHeight() / 2f);
        doorGroup.addActor(door);
        doorGroup.addActor(dial);
        addActor(body);
        addActor(inside);
        addActor(doorGroup);
    }

    /** @return le centre (Stage) de l'ouverture du coffre, d'où jaillit l'or. */
    public Vector2 opening() { return opening; }

    /** Fait tomber le coffre, le milieu de sa base en {@code (x, y)}. */
    @Override
    protected void start(float x, float y) {
        float screenHeight = getStage().getViewport().getWorldHeight();
        float left = x - body.getWidth() / 2f;
        float drop = screenHeight - y + 40f;   // part juste au-dessus de l'écran

        // Tout le coffre tombe d'un bloc : chaque pièce suit le même déplacement.
        float doorLeft = left + DOOR_X * SCALE;
        float doorBottom = y + (BODY_HEIGHT - DOOR_Y - DOOR_HEIGHT) * SCALE;
        body.setPosition(left, y + drop);
        inside.setPosition(doorLeft, doorBottom + drop);
        doorGroup.setPosition(doorLeft, doorBottom + drop);
        door.setPosition(0f, 0f);
        door.setColor(Color.WHITE);
        dial.setPosition(door.getWidth() / 2f - dial.getWidth() / 2f, door.getHeight() / 2f - dial.getHeight() / 2f);
        dial.setRotation(0f);
        doorGroup.setOrigin(0f, door.getHeight() / 2f); // charnière à gauche
        doorGroup.setScale(1f);
        opening.set(doorLeft + door.getWidth() / 2f, doorBottom + door.getHeight() / 2f);

        for (Actor part : new Actor[] {body, inside, doorGroup}) {
            part.addAction(Actions.sequence(
                Actions.moveBy(0f, -drop, LAND_TIME, Interpolation.pow2In),
                Actions.moveBy(0f, 14f, 0.07f, Interpolation.pow2Out),
                Actions.moveBy(0f, -14f, 0.09f, Interpolation.pow2In)));
        }

        // La molette tourne cran par cran, avec une courte pause entre deux nombres.
        SequenceAction turns = Actions.sequence(Actions.delay(LAND_TIME + 0.2f));
        float step = (OPEN_TIME - LAND_TIME - 0.3f) / COMBINATION.length;
        for (float turn : COMBINATION) {
            turns.addAction(Actions.rotateBy(turn, step * 0.7f, Interpolation.pow2Out));
            turns.addAction(Actions.delay(step * 0.3f));
        }
        dial.addAction(turns);

        // La porte pivote sur sa charnière : elle s'amincit puis se retourne, assombrie.
        doorGroup.addAction(Actions.sequence(
            Actions.delay(OPEN_TIME),
            Actions.scaleTo(0.1f, 1f, OPEN_DURATION * 0.6f, Interpolation.pow2In),
            Actions.scaleTo(-0.35f, 1f, OPEN_DURATION * 0.4f, Interpolation.pow2Out)));
        door.addAction(Actions.sequence(Actions.delay(OPEN_TIME), Actions.color(c("6a7680"), OPEN_DURATION)));
    }

    /** @return la caisse du coffre : acier sombre biseauté, charnières, pieds. */
    private static Pixmap body() {
        Pixmap pixmap = pixmap(BODY_WIDTH, BODY_HEIGHT);
        Color steel = c("37474f"), light = c("5b6e78"), dark = c("263238");
        fillOutlined(pixmap, 0, 0, BODY_WIDTH, BODY_HEIGHT - 3, steel);
        hLine(pixmap, 1, BODY_WIDTH - 2, 1, light);
        pixmap.setColor(light);
        pixmap.drawLine(1, 1, 1, BODY_HEIGHT - 5);
        pixmap.setColor(dark);
        pixmap.drawLine(BODY_WIDTH - 2, 2, BODY_WIDTH - 2, BODY_HEIGHT - 5);
        hLine(pixmap, 2, BODY_WIDTH - 2, BODY_HEIGHT - 5, dark);
        // Cadre de la porte, en creux.
        pixmap.setColor(OUTLINE);
        pixmap.drawRectangle(DOOR_X - 1, DOOR_Y - 1, DOOR_WIDTH + 2, DOOR_HEIGHT + 2);
        // Charnières.
        for (int y : new int[] {DOOR_Y + 5, DOOR_Y + DOOR_HEIGHT - 10}) {
            fillOutlined(pixmap, DOOR_X - 4, y, 4, 6, c("8a97a8"));
        }
        // Pieds.
        fillOutlined(pixmap, 3, BODY_HEIGHT - 4, 8, 4, dark);
        fillOutlined(pixmap, BODY_WIDTH - 11, BODY_HEIGHT - 4, 8, 4, dark);
        return pixmap;
    }

    /** @return l'intérieur du coffre : fond sombre et piles de lingots d'or. */
    private static Pixmap inside() {
        Pixmap pixmap = pixmap(DOOR_WIDTH, DOOR_HEIGHT);
        fillOutlined(pixmap, 0, 0, DOOR_WIDTH, DOOR_HEIGHT, c("1a1418"));
        hLine(pixmap, 1, DOOR_WIDTH - 2, DOOR_HEIGHT / 2, c("3a3038")); // étagère
        for (int shelf = 0; shelf < 2; shelf++) {
            int base = shelf == 0 ? DOOR_HEIGHT / 2 - 1 : DOOR_HEIGHT - 2;
            for (int row = 0; row < 3; row++) {
                for (int i = 0; i < 4 - row; i++) {
                    int x = 3 + row * 4 + i * 9, y = base - 4 - row * 4;
                    fillOutlined(pixmap, x, y, 9, 5, c("ffd23c"));
                    hLine(pixmap, x + 1, x + 7, y + 1, c("fff0a0"));
                }
            }
        }
        return pixmap;
    }

    /** @return la porte blindée : panneau en creux, rivets, poignée à trois branches. */
    private static Pixmap door() {
        Pixmap pixmap = pixmap(DOOR_WIDTH, DOOR_HEIGHT);
        fillOutlined(pixmap, 0, 0, DOOR_WIDTH, DOOR_HEIGHT, c("455a64"));
        hLine(pixmap, 1, DOOR_WIDTH - 2, 1, c("6a8290"));
        hLine(pixmap, 1, DOOR_WIDTH - 2, DOOR_HEIGHT - 2, c("2e3d44"));
        pixmap.setColor(c("2e3d44"));
        pixmap.drawRectangle(4, 4, DOOR_WIDTH - 8, DOOR_HEIGHT - 8);
        for (int[] rivet : new int[][] {{2, 2}, {DOOR_WIDTH - 4, 2}, {2, DOOR_HEIGHT - 4}, {DOOR_WIDTH - 4, DOOR_HEIGHT - 4}}) {
            pixmap.setColor(c("a8b4c2"));
            pixmap.fillRectangle(rivet[0], rivet[1], 2, 2);
        }
        // Poignée à droite de la molette.
        int hx = DOOR_WIDTH - 9, hy = DOOR_HEIGHT / 2;
        pixmap.setColor(OUTLINE);
        pixmap.fillRectangle(hx - 1, hy - 7, 4, 15);
        pixmap.setColor(c("c8d2dc"));
        pixmap.fillRectangle(hx, hy - 6, 2, 13);
        return pixmap;
    }

    /** @return la molette : couronne chromée graduée, repère rouge en haut. */
    private static Pixmap dial() {
        Pixmap pixmap = pixmap(DIAL_SIZE, DIAL_SIZE);
        int center = DIAL_SIZE / 2;
        fillOutlinedCircle(pixmap, center, center, center - 1, c("c8d2dc"));
        pixmap.setColor(c("8a97a8"));
        pixmap.fillCircle(center, center, center - 4);
        pixmap.setColor(c("2e3d44"));
        for (int i = 0; i < 8; i++) {
            float angle = i * 45f;
            pixmap.drawPixel(center + Math.round(MathUtils.cosDeg(angle) * (center - 3)),
                center + Math.round(MathUtils.sinDeg(angle) * (center - 3)));
        }
        pixmap.setColor(c("e0303c"));
        pixmap.fillRectangle(center - 1, 1, 2, 4);
        pixmap.setColor(OUTLINE);
        pixmap.fillCircle(center, center, 2);
        return pixmap;
    }
}
