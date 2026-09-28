package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

/**
 * Bingo du Double Bar : une presse hydraulique. Deux lourdes mâchoires d'acier,
 * poussées par leurs vérins depuis le haut et le bas de l'écran, se
 * rapprochent lentement puis se percutent au centre ; après le choc, elles
 * s'écartent et libèrent l'espace où s'écrit « BINGO! ».
 */
public class HydraulicPressScene extends BingoScene {

    /** Les deux mâchoires se percutent au centre. */
    public static final float SLAM_TIME = 0.8f;

    private static final int   JAW_WIDTH  = 74;
    private static final int   JAW_HEIGHT = 16;
    private static final int   ROD_WIDTH  = 12;
    private static final float APPROACH   = 0.45f;   // descente lente jusqu'à mi-course
    private static final float OPEN_DELAY = 0.25f;   // les mâchoires restent collées après le choc
    /** Écart entre le centre et chaque mâchoire, une fois rouvertes. */
    private static final float OPEN_GAP   = 150f;

    private final Image topJaw, bottomJaw, topRod, bottomRod;

    public HydraulicPressScene() {
        TextureRegion jaw = new TextureRegion(texture(jaw()));
        TextureRegion rod = new TextureRegion(texture(rod()));
        topRod    = image(rod, SCALE);
        bottomRod = image(rod, SCALE);
        topJaw    = image(jaw, SCALE);
        bottomJaw = image(new TextureRegion(jaw.getTexture()), SCALE);
        bottomJaw.setOrigin(bottomJaw.getWidth() / 2f, bottomJaw.getHeight() / 2f);
        bottomJaw.setScaleY(-1f); // retournée : la bande de danger face à l'autre mâchoire
        addActor(topRod);
        addActor(bottomRod);
        addActor(topJaw);
        addActor(bottomJaw);
    }

    @Override
    protected void start(float x, float y) {
        float jawHeight = topJaw.getHeight();
        float screenHeight = getStage().getViewport().getWorldHeight();
        float left = x - topJaw.getWidth() / 2f;
        float offscreen = screenHeight;  // assez loin pour partir hors de l'écran

        // Positions (bas de la mâchoire du haut, haut de celle du bas) : départ, mi-course, contact, ouverture.
        topJaw.setPosition(left, y + offscreen);
        bottomJaw.setPosition(left, y - offscreen - jawHeight);
        topJaw.addAction(Actions.sequence(
            Actions.moveTo(left, y + 260f, APPROACH, Interpolation.pow2Out),
            Actions.moveTo(left, y, SLAM_TIME - APPROACH, Interpolation.pow3In),
            Actions.moveBy(0f, 6f, 0.05f), Actions.moveBy(0f, -6f, 0.05f),
            Actions.delay(OPEN_DELAY - 0.1f),
            Actions.moveTo(left, y + OPEN_GAP, 0.35f, Interpolation.pow2Out)));
        bottomJaw.addAction(Actions.sequence(
            Actions.moveTo(left, y - 260f - jawHeight, APPROACH, Interpolation.pow2Out),
            Actions.moveTo(left, y - jawHeight, SLAM_TIME - APPROACH, Interpolation.pow3In),
            Actions.moveBy(0f, -6f, 0.05f), Actions.moveBy(0f, 6f, 0.05f),
            Actions.delay(OPEN_DELAY - 0.1f),
            Actions.moveTo(left, y - OPEN_GAP - jawHeight, 0.35f, Interpolation.pow2Out)));
        update(0f);
    }

    /** Les vérins suivent leurs mâchoires jusqu'au bord de l'écran. */
    @Override
    protected void update(float delta) {
        float screenHeight = getStage() == null ? 0f : getStage().getViewport().getWorldHeight();
        float rodX = topJaw.getX() + (topJaw.getWidth() - topRod.getWidth()) / 2f;
        float topStart = topJaw.getY() + topJaw.getHeight();
        topRod.setBounds(rodX, topStart, topRod.getWidth(), Math.max(0f, screenHeight - topStart + 10f));
        float bottomEnd = bottomJaw.getY();
        bottomRod.setBounds(rodX, -10f, bottomRod.getWidth(), Math.max(0f, bottomEnd + 10f));
    }

    /** @return une mâchoire d'acier riveté, bande de danger jaune et noire sur sa face de frappe (en bas). */
    private static Pixmap jaw() {
        Pixmap pixmap = pixmap(JAW_WIDTH, JAW_HEIGHT);
        Color steel = c("8a97a8"), light = c("c8d2dc"), dark = c("4a5566");
        fillOutlined(pixmap, 0, 0, JAW_WIDTH, JAW_HEIGHT, steel);
        hLine(pixmap, 1, JAW_WIDTH - 2, 1, light);
        hLine(pixmap, 1, JAW_WIDTH - 2, JAW_HEIGHT - 6, dark);
        // Rivets.
        for (int x = 5; x < JAW_WIDTH - 4; x += 8) {
            pixmap.setColor(dark);
            pixmap.fillRectangle(x, 4, 2, 2);
            pixmap.setColor(light);
            pixmap.drawPixel(x, 4);
        }
        // Bande de danger, rayures en biais.
        for (int y = JAW_HEIGHT - 5; y < JAW_HEIGHT - 1; y++) {
            for (int x = 1; x < JAW_WIDTH - 1; x++) {
                pixmap.setColor(((x + y) / 3) % 2 == 0 ? c("ffd23c") : c("2a2a33"));
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }

    /** @return un tronçon de vérin chromé (étiré en hauteur). */
    private static Pixmap rod() {
        Pixmap pixmap = pixmap(ROD_WIDTH, 1);
        String[] columns = {"1a0f0f", "5a6676", "c8d2dc", "ffffff", "c8d2dc", "a8b4c2",
                            "8a97a8", "8a97a8", "6a7684", "5a6676", "4a5566", "1a0f0f"};
        for (int x = 0; x < ROD_WIDTH; x++) {
            pixmap.setColor(c(columns[x]));
            pixmap.drawPixel(x, 0);
        }
        return pixmap;
    }
}
