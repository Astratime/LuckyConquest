package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;

/**
 * Bingo du Raisin : le pressoir. Une grappe géante tombe dans la cuve d'un
 * pressoir en bois ; la vis descend, la touche puis l'écrase d'un coup (le
 * jus, les bulles et les grains qui roulent sont lancés par
 * {@link JackpotCelebration}).
 */
public class WinePressScene extends BingoScene {

    /** La grappe est écrasée au fond de la cuve. */
    public static final float CRUSH_TIME = 0.95f;

    private static final float FALL_TIME   = 0.4f;
    private static final float PRESS_FROM  = 0.5f;   // la vis commence à descendre
    private static final float TOUCH_TIME  = 0.78f;  // le plateau touche la grappe
    private static final float GRAPE_SCALE = 1.4f;   // image d'un symbole (144 px) : ~200 px

    private static final int VAT_WIDTH    = 46;
    private static final int VAT_HEIGHT   = 22;
    private static final int FRAME_WIDTH  = 60;
    private static final int FRAME_HEIGHT = 74;
    private static final int PLATE_WIDTH  = 38;
    private static final int PLATE_HEIGHT = 6;
    private static final int ROD_WIDTH    = 8;

    private final Image frame, vat, plate, rod, grape;
    private float vatTop;

    /** @param grapeRegion image (détourée) de la grappe de raisin */
    public WinePressScene(TextureRegion grapeRegion) {
        frame = image(texture(frame()));
        vat   = image(texture(vat()));
        plate = image(texture(plate()));
        // La vis est pavée de son motif (filets nets) quelle que soit sa longueur.
        TiledDrawable threads = new TiledDrawable(new TextureRegion(texture(rod())));
        threads.setScale(SCALE);
        rod = new Image(threads);
        rod.setSize(ROD_WIDTH * SCALE, 0f);
        grape = image(grapeRegion, GRAPE_SCALE);
        grape.setOrigin(grape.getWidth() / 2f, 0f);
        addActor(frame);
        addActor(rod);
        addActor(plate);
        addActor(grape);
        addActor(vat);
    }

    /** @return l'ordonnée (Stage) du bord de la cuve, d'où gicle le jus. */
    public float vatTop() { return vatTop; }

    /** Place le pressoir, le pied de sa cuve centré en {@code (x, y)}. */
    @Override
    protected void start(float x, float y) {
        float screenHeight = getStage().getViewport().getWorldHeight();
        frame.setPosition(x - frame.getWidth() / 2f, y);
        vat.setPosition(x - vat.getWidth() / 2f, y);
        vatTop = y + vat.getHeight();

        // La grappe tombe du haut de l'écran et se pose à moitié dans la cuve.
        float grapeY = vatTop - grape.getHeight() * 0.35f;
        grape.setPosition(x - grape.getWidth() / 2f, screenHeight);
        grape.setScale(1f);
        grape.setVisible(true);
        grape.addAction(Actions.sequence(
            Actions.moveTo(grape.getX(), grapeY, FALL_TIME, Interpolation.pow2In),
            Actions.scaleTo(1.1f, 0.88f, 0.06f), Actions.scaleTo(1f, 1f, 0.12f, Interpolation.pow2Out),
            Actions.delay(TOUCH_TIME - FALL_TIME - 0.18f),
            Actions.scaleTo(1.45f, 0.25f, CRUSH_TIME - TOUCH_TIME, Interpolation.pow3In),
            Actions.visible(false)));

        // Le plateau descend lentement, touche la grappe, puis l'écrase jusqu'au fond de la cuve.
        float plateX = x - plate.getWidth() / 2f;
        float grapeTop = grapeY + grape.getHeight();
        plate.setPosition(plateX, screenHeight * 0.95f);
        plate.addAction(Actions.sequence(
            Actions.moveTo(plateX, grapeTop + 120f, PRESS_FROM, Interpolation.pow2Out),
            Actions.moveTo(plateX, grapeTop, TOUCH_TIME - PRESS_FROM, Interpolation.sine),
            Actions.moveTo(plateX, vatTop - 40f, CRUSH_TIME - TOUCH_TIME, Interpolation.pow3In),
            Actions.moveBy(0f, 8f, 0.06f), Actions.moveBy(0f, -8f, 0.08f)));
        update(0f);
    }

    /** La vis relie le plateau au haut de l'écran. */
    @Override
    protected void update(float delta) {
        float screenHeight = getStage() == null ? 0f : getStage().getViewport().getWorldHeight();
        float top = plate.getY() + plate.getHeight();
        rod.setBounds(plate.getX() + (plate.getWidth() - rod.getWidth()) / 2f, top, rod.getWidth(),
            Math.max(0f, screenHeight - top + 10f));
    }

    /** @return la cuve : douelles de bois alternées, deux cercles de fer, rebord sombre. */
    private static Pixmap vat() {
        Pixmap pixmap = pixmap(VAT_WIDTH, VAT_HEIGHT);
        fillOutlined(pixmap, 0, 0, VAT_WIDTH, VAT_HEIGHT, c("8a5a2c"));
        for (int x = 1; x < VAT_WIDTH - 1; x++) {
            pixmap.setColor((x / 5) % 2 == 0 ? c("8a5a2c") : c("7a4a24"));
            pixmap.drawLine(x, 2, x, VAT_HEIGHT - 2);
            if (x % 5 == 0) {
                pixmap.setColor(c("5a3418"));
                pixmap.drawLine(x, 2, x, VAT_HEIGHT - 2);
            }
        }
        hLine(pixmap, 1, VAT_WIDTH - 2, 1, c("4e2e18"));
        for (int hoop : new int[] {5, 15}) {
            hLine(pixmap, 1, VAT_WIDTH - 2, hoop, c("8a8a9c"));
            hLine(pixmap, 1, VAT_WIDTH - 2, hoop + 1, c("3a3a44"));
        }
        return pixmap;
    }

    /** @return le bâti : deux montants et une traverse en bois sombre. */
    private static Pixmap frame() {
        Pixmap pixmap = pixmap(FRAME_WIDTH, FRAME_HEIGHT);
        Color wood = c("6e4524"), light = c("8a5a2c");
        fillOutlined(pixmap, 0, 0, 6, FRAME_HEIGHT, wood);
        fillOutlined(pixmap, FRAME_WIDTH - 6, 0, 6, FRAME_HEIGHT, wood);
        fillOutlined(pixmap, 0, 0, FRAME_WIDTH, 7, wood);
        hLine(pixmap, 1, FRAME_WIDTH - 2, 1, light);
        pixmap.setColor(light);
        pixmap.drawLine(1, 7, 1, FRAME_HEIGHT - 2);
        pixmap.drawLine(FRAME_WIDTH - 5, 7, FRAME_WIDTH - 5, FRAME_HEIGHT - 2);
        return pixmap;
    }

    /** @return le plateau de pressage en bois cerclé de fer. */
    private static Pixmap plate() {
        Pixmap pixmap = pixmap(PLATE_WIDTH, PLATE_HEIGHT);
        fillOutlined(pixmap, 0, 0, PLATE_WIDTH, PLATE_HEIGHT, c("8a5a2c"));
        hLine(pixmap, 1, PLATE_WIDTH - 2, 1, c("b07a3c"));
        hLine(pixmap, 1, PLATE_WIDTH - 2, PLATE_HEIGHT - 2, c("3a3a44"));
        return pixmap;
    }

    /** @return un tronçon de la vis en fer (étiré en hauteur), filets en biais. */
    private static Pixmap rod() {
        Pixmap pixmap = pixmap(ROD_WIDTH, 4);
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < ROD_WIDTH; x++) {
                Color color;
                if (x == 0 || x == ROD_WIDTH - 1) color = OUTLINE;
                else if ((x + y) % 4 == 0)       color = c("3a3a44");
                else if (x < 3)                  color = c("a8a8b8");
                else                             color = c("6a6a78");
                pixmap.setColor(color);
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }
}
