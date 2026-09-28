package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * Bingo de la Cerise : le cerisier. Un arbre pousse depuis la table, son
 * feuillage s'ouvre, se couvre de fleurs roses qui deviennent des cerises ;
 * puis l'arbre est secoué et ses cerises tombent (la pluie de cerises et les
 * pétales sont lancés par {@link JackpotCelebration}).
 */
public class CherryTreeScene extends BingoScene {

    /** L'arbre est secoué. */
    public static final float SHAKE_TIME = 1.35f;
    /** Les cerises se détachent (voir {@link #fruitPositions()}). */
    public static final float DROP_TIME  = 1.5f;

    private static final int   FRUITS        = 14;
    private static final float GROW_TIME     = 0.45f;
    private static final float CANOPY_AT     = 0.3f;
    private static final float BLOOM_FROM    = 0.75f;
    private static final float BLOOM_TO      = 1.0f;
    private static final float FRUIT_AT      = 1.05f;
    private static final float CHERRY_SCALE  = 0.42f;  // image d'un symbole (144 px) : ~60 px

    private static final int TRUNK_WIDTH  = 14;
    private static final int TRUNK_HEIGHT = 34;
    private static final int CANOPY_WIDTH = 64;
    private static final int CANOPY_HEIGHT = 46;

    private final Group tree = new Group();
    private final Image trunk, canopy;
    private final List<Image> flowers = new ArrayList<>();
    private final List<Image> fruits  = new ArrayList<>();
    private final List<Vector2> fruitPositions = new ArrayList<>();

    /** @param cherry image (détourée) de la cerise, accrochée à l'arbre */
    public CherryTreeScene(TextureRegion cherry) {
        trunk  = image(texture(trunk()));
        trunk.setOrigin(trunk.getWidth() / 2f, 0f);
        canopy = image(texture(canopy()));
        canopy.setOrigin(canopy.getWidth() / 2f, canopy.getHeight() * 0.3f);
        tree.addActor(trunk);
        tree.addActor(canopy);
        TextureRegion flower = new TextureRegion(texture(flower()));
        for (int i = 0; i < FRUITS; i++) {
            Image bloom = image(flower, 4f);
            bloom.setOrigin(bloom.getWidth() / 2f, bloom.getHeight() / 2f);
            Image fruit = image(cherry, CHERRY_SCALE);
            fruit.setOrigin(fruit.getWidth() / 2f, fruit.getHeight() * 0.8f); // accrochée par la queue
            flowers.add(bloom);
            fruits.add(fruit);
            tree.addActor(bloom);
            tree.addActor(fruit);
            fruitPositions.add(new Vector2());
        }
        addActor(tree);
    }

    /** @return la position (Stage) de chaque cerise accrochée à l'arbre. */
    public List<Vector2> fruitPositions() { return fruitPositions; }

    /** Fait pousser l'arbre, le pied de son tronc en {@code (x, y)}. */
    @Override
    protected void start(float x, float y) {
        tree.setOrigin(x, y);
        tree.setRotation(0f);

        trunk.setPosition(x - trunk.getWidth() / 2f, y);
        trunk.setScale(1f, 0f);
        trunk.addAction(Actions.scaleTo(1f, 1f, GROW_TIME, Interpolation.pow2Out));

        float canopyBottom = y + trunk.getHeight() - 12f * SCALE;
        canopy.setPosition(x - canopy.getWidth() / 2f, canopyBottom);
        canopy.setScale(0f);
        canopy.addAction(Actions.sequence(
            Actions.delay(CANOPY_AT),
            Actions.scaleTo(1.12f, 1.12f, 0.3f, Interpolation.pow2Out),
            Actions.scaleTo(1f, 1f, 0.15f, Interpolation.pow2In)));

        // Fleurs puis cerises, éparpillées dans le feuillage (ellipse intérieure).
        float centerX = x, centerY = canopyBottom + canopy.getHeight() * 0.5f;
        float radiusX = canopy.getWidth() * 0.38f, radiusY = canopy.getHeight() * 0.32f;
        for (int i = 0; i < FRUITS; i++) {
            float angle = MathUtils.PI2 * i / FRUITS + MathUtils.random(-0.2f, 0.2f);
            float distance = (float) Math.sqrt(MathUtils.random(0.25f, 1f));
            Vector2 at = fruitPositions.get(i).set(centerX + MathUtils.cos(angle) * radiusX * distance,
                centerY + MathUtils.sin(angle) * radiusY * distance);

            Image bloom = flowers.get(i);
            placeOrigin(bloom, at.x, at.y);
            bloom.setScale(0f);
            bloom.getColor().a = 1f;
            float bloomAt = BLOOM_FROM + (BLOOM_TO - BLOOM_FROM) * i / FRUITS;
            bloom.addAction(Actions.sequence(
                Actions.delay(bloomAt),
                Actions.scaleTo(1.3f, 1.3f, 0.1f, Interpolation.pow2Out),
                Actions.scaleTo(1f, 1f, 0.08f),
                Actions.delay(FRUIT_AT - bloomAt - 0.18f),
                Actions.parallel(Actions.fadeOut(0.12f), Actions.scaleTo(0f, 0f, 0.12f))));

            Image fruit = fruits.get(i);
            placeOrigin(fruit, at.x, at.y);
            fruit.setScale(0f);
            fruit.setVisible(true);
            fruit.setRotation(0f);
            fruit.addAction(Actions.sequence(
                Actions.delay(FRUIT_AT + i * 0.005f),
                Actions.scaleTo(1.2f, 1.2f, 0.1f, Interpolation.pow2Out),
                Actions.scaleTo(1f, 1f, 0.08f),
                // Les cerises se balancent quand l'arbre est secoué, puis se détachent.
                Actions.delay(SHAKE_TIME - FRUIT_AT - i * 0.005f - 0.18f),
                Actions.rotateTo(12f, 0.05f), Actions.rotateTo(-12f, 0.05f), Actions.rotateTo(8f, 0.05f),
                Actions.visible(false)));
        }

        SequenceAction shake = Actions.sequence(Actions.delay(SHAKE_TIME));
        for (int i = 0; i < 8; i++) {
            shake.addAction(Actions.rotateTo((i % 2 == 0 ? 4f : -4f) * (1f - i / 8f), 0.05f));
        }
        shake.addAction(Actions.rotateTo(0f, 0.05f));
        tree.addAction(shake);
    }

    /** @return le tronc : écorce brune éclairée à gauche, pied évasé, deux amorces de branches. */
    private static Pixmap trunk() {
        Pixmap pixmap = pixmap(TRUNK_WIDTH, TRUNK_HEIGHT);
        Color bark = c("7a4a2a"), light = c("a8683c"), dark = c("4e2e18");
        int center = TRUNK_WIDTH / 2;
        for (int y = 0; y < TRUNK_HEIGHT; y++) {
            int half = y > TRUNK_HEIGHT - 5 ? 4 + (y - (TRUNK_HEIGHT - 5)) : 3; // pied évasé
            pixmap.setColor(OUTLINE);
            pixmap.drawLine(center - half - 1, y, center + half, y);
            pixmap.setColor(bark);
            pixmap.drawLine(center - half, y, center + half - 1, y);
            pixmap.setColor(light);
            pixmap.drawPixel(center - half, y);
            pixmap.setColor(dark);
            pixmap.drawPixel(center + half - 1, y);
        }
        // Amorces de branches, cachées ensuite par le feuillage.
        pixmap.setColor(bark);
        pixmap.drawLine(center - 3, 6, 1, 1);
        pixmap.drawLine(center + 2, 7, TRUNK_WIDTH - 2, 2);
        return pixmap;
    }

    /** @return le feuillage : nuage de boules vertes, éclairé en haut à gauche. */
    private static Pixmap canopy() {
        Pixmap pixmap = pixmap(CANOPY_WIDTH, CANOPY_HEIGHT);
        int[][] blobs = {{32, 25, 19}, {16, 28, 14}, {48, 28, 14}, {23, 15, 13}, {41, 15, 13}};
        for (int[] b : blobs) { pixmap.setColor(OUTLINE); pixmap.fillCircle(b[0], b[1], b[2]); }
        for (int[] b : blobs) { pixmap.setColor(c("3f9a3a")); pixmap.fillCircle(b[0], b[1], b[2] - 1); }
        for (int[] b : blobs) { pixmap.setColor(c("2c7a2e")); pixmap.fillCircle(b[0] + 3, b[1] + 3, b[2] - 5); }
        for (int[] b : blobs) { pixmap.setColor(c("6cc45a")); pixmap.fillCircle(b[0] - 3, b[1] - 4, b[2] / 3); }
        return pixmap;
    }

    /** @return une fleur de cerisier : cinq pétales roses, cœur jaune. */
    private static Pixmap flower() {
        Pixmap pixmap = pixmap(7, 7);
        pixmap.setColor(OUTLINE);
        pixmap.fillRectangle(1, 0, 5, 7);
        pixmap.fillRectangle(0, 1, 7, 5);
        pixmap.setColor(c("ffb3cf"));
        pixmap.fillRectangle(2, 1, 3, 5);
        pixmap.fillRectangle(1, 2, 5, 3);
        pixmap.setColor(c("ffe3ec"));
        pixmap.drawPixel(2, 1);
        pixmap.setColor(c("ffd23c"));
        pixmap.drawPixel(3, 3);
        return pixmap;
    }
}
