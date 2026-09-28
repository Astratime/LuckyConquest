package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import fr.astratime.lucky.assets.Palette;

/**
 * Bingo du Bar : la forge. Une barre chauffée au rouge est posée sur une
 * enclume ; un marteau de forge la frappe trois fois (à {@link #HITS}) et, au
 * troisième coup, elle vole en éclats (les éclats et les étincelles sont
 * lancés par {@link JackpotCelebration}).
 */
public class ForgeScene extends BingoScene {

    /** Instants des trois coups de marteau ; la barre éclate au dernier. */
    public static final float[] HITS = {0.55f, 1.0f, 1.45f};

    private static final int   ANVIL_WIDTH  = 44;
    private static final int   ANVIL_HEIGHT = 24;
    private static final int   BAR_WIDTH    = 30;
    private static final int   BAR_HEIGHT   = 7;
    private static final int   HAMMER_WIDTH = 22;
    private static final int   HAMMER_HEAD  = 12;
    private static final int   HAMMER_HEIGHT = 44;
    // Angles du marteau (0 : tête en haut ; 90 : manche horizontal, tête à gauche).
    private static final float RAISED = 15f;
    private static final float HIT    = 90f;
    private static final float SWING  = 0.12f;

    private final Image anvil, bar, hammer;
    private final Vector2 strike = new Vector2();

    public ForgeScene() {
        anvil  = image(texture(anvil()));
        bar    = image(texture(hotBar()));
        bar.setOrigin(bar.getWidth() / 2f, 0f);
        hammer = image(texture(hammer()));
        hammer.setOrigin(hammer.getWidth() / 2f, 2f * SCALE); // bout du manche
        addActor(anvil);
        addActor(bar);
        addActor(hammer);
    }

    /** @return le milieu (Stage) du dessus de la barre, où frappe le marteau. */
    public Vector2 strikePoint() { return strike; }

    /** Place l'enclume, son pied centré en {@code (x, y)}, puis programme les trois coups. */
    @Override
    protected void start(float x, float y) {
        anvil.setPosition(x - anvil.getWidth() / 2f, y);
        float barY = y + anvil.getHeight() - SCALE;
        bar.setPosition(x - bar.getWidth() / 2f, barY);
        bar.setScale(1f);
        bar.setColor(Color.WHITE);
        bar.setVisible(true);
        strike.set(x, barY + bar.getHeight());

        // Manche horizontal à la frappe : la face basse de la tête touche la barre.
        float headCenter = (HAMMER_HEIGHT - 2f - HAMMER_HEAD / 2f) * SCALE;
        placeOrigin(hammer, strike.x + headCenter, strike.y + HAMMER_WIDTH / 2f * SCALE);
        hammer.setRotation(RAISED);
        hammer.getColor().a = 0f;

        SequenceAction swings = Actions.sequence(Actions.fadeIn(0.15f));
        float at = 0.15f;
        for (float hit : HITS) {
            swings.addAction(Actions.rotateTo(RAISED - 20f, hit - SWING - at, Interpolation.pow2Out)); // élan
            swings.addAction(Actions.rotateTo(HIT, SWING, Interpolation.pow3In));
            at = hit;
            if (hit != HITS[HITS.length - 1]) {
                swings.addAction(Actions.rotateTo(HIT - 30f, 0.12f, Interpolation.pow2Out)); // rebond
                at += 0.12f;
            }
        }
        swings.addAction(Actions.parallel(
            Actions.rotateTo(40f, 0.3f, Interpolation.pow2Out),
            Actions.sequence(Actions.delay(0.15f), Actions.fadeOut(0.3f))));
        hammer.addAction(swings);

        // La barre s'écrase un peu à chaque coup, puis disparaît en éclats au dernier.
        SequenceAction squash = Actions.sequence();
        float previous = 0f;
        for (int i = 0; i < HITS.length - 1; i++) {
            squash.addAction(Actions.delay(HITS[i] - previous));
            squash.addAction(Actions.scaleTo(1.08f, 0.8f, 0.04f));
            squash.addAction(Actions.scaleTo(1f, 1f, 0.12f));
            previous = HITS[i] + 0.16f;
        }
        squash.addAction(Actions.delay(HITS[HITS.length - 1] - previous));
        squash.addAction(Actions.visible(false));
        bar.addAction(Actions.parallel(squash, Actions.forever(Actions.sequence(
            Actions.color(c("ffe0b0"), 0.15f), Actions.color(Color.WHITE, 0.15f)))));
    }

    /** @return l'enclume : table plate, bigorne pointue à gauche, pied évasé. */
    private static Pixmap anvil() {
        Pixmap pixmap = pixmap(ANVIL_WIDTH, ANVIL_HEIGHT);
        Color iron = Palette.IRON, light = c("7a7a8c"), dark = c("2a2a33");
        pixmap.setColor(OUTLINE);
        // Table et bigorne (pointe vers la gauche).
        pixmap.fillRectangle(10, 0, ANVIL_WIDTH - 12, 8);
        pixmap.fillTriangle(0, 1, 10, 0, 10, 7);
        // Taille, puis pied évasé.
        pixmap.fillRectangle(16, 8, 16, 8);
        pixmap.fillRectangle(10, 16, 28, 8);
        pixmap.setColor(iron);
        pixmap.fillRectangle(11, 1, ANVIL_WIDTH - 14, 6);
        pixmap.fillTriangle(2, 2, 11, 1, 11, 6);
        pixmap.fillRectangle(17, 8, 14, 9);
        pixmap.fillRectangle(11, 17, 26, 6);
        hLine(pixmap, 11, ANVIL_WIDTH - 4, 1, light);
        pixmap.setColor(dark);
        pixmap.fillRectangle(27, 8, 4, 9);
        hLine(pixmap, 11, 36, 22, dark);
        return pixmap;
    }

    /** @return la barre chauffée au rouge : cœur jaune, bords orange et rouge. */
    private static Pixmap hotBar() {
        Pixmap pixmap = pixmap(BAR_WIDTH, BAR_HEIGHT);
        Color[] rows = {OUTLINE, c("ff5a1f"), c("ffb347"), Palette.GOLD_PALE, c("ffb347"), c("d42a1a"), OUTLINE};
        for (int y = 0; y < BAR_HEIGHT; y++) hLine(pixmap, 0, BAR_WIDTH - 1, y, rows[y]);
        pixmap.setColor(OUTLINE);
        pixmap.drawLine(0, 0, 0, BAR_HEIGHT - 1);
        pixmap.drawLine(BAR_WIDTH - 1, 0, BAR_WIDTH - 1, BAR_HEIGHT - 1);
        return pixmap;
    }

    /** @return le marteau de forge, tête en haut : masse de fer, long manche en bois. */
    private static Pixmap hammer() {
        Pixmap pixmap = pixmap(HAMMER_WIDTH, HAMMER_HEIGHT);
        fillOutlined(pixmap, 0, 0, HAMMER_WIDTH, HAMMER_HEAD, Palette.IRON);
        hLine(pixmap, 1, HAMMER_WIDTH - 2, 1, c("8a8a9c"));
        hLine(pixmap, 1, HAMMER_WIDTH - 2, HAMMER_HEAD - 2, c("2a2a33"));
        int handle = 4, left = (HAMMER_WIDTH - handle) / 2;
        fillOutlined(pixmap, left, HAMMER_HEAD - 1, handle, HAMMER_HEIGHT - HAMMER_HEAD + 1, Palette.WOOD);
        pixmap.setColor(Palette.WOOD_LIGHT);
        pixmap.drawLine(left + 1, HAMMER_HEAD, left + 1, HAMMER_HEIGHT - 2);
        return pixmap;
    }
}
