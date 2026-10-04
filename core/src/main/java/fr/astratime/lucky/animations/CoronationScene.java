package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import fr.astratime.lucky.assets.Palette;

/**
 * Bingo de la Couronne : le sacre. Un tapis rouge se déroule depuis le bas de
 * l'écran jusqu'au milieu de la table, où apparaît un coussin de velours ;
 * deux trompettes à bannière sonnent la fanfare (à {@link #FANFARE}), puis la
 * couronne descend du ciel et se pose sur le coussin (les notes, la pluie de
 * pièces et de joyaux sont lancées par {@link JackpotCelebration}, qui reprend
 * {@link #note()} et {@link #jewels()}).
 */
public class CoronationScene extends BingoScene {

    /** Le tapis rouge a fini de se dérouler. */
    public static final float UNROLLED_TIME = 0.6f;
    /** Les trois coups de la fanfare : ta, ta, taaa. */
    public static final float[] FANFARE = {0.75f, 0.92f, 1.09f};
    /** La couronne se pose sur le coussin. */
    public static final float CROWN_TIME = 1.5f;

    private static final float UNROLL_FROM  = 0.05f;
    private static final float CROWN_FROM   = 0.85f;  // elle commence à descendre
    private static final float TRUMPET_TILT = 18f;
    private static final float TRUMPET_GAP  = 175f;   // du milieu du tapis à l'embouchure de chaque trompette

    private static final int CARPET_TOP    = 30;      // largeur du tapis au loin, en pixels de l'image
    private static final int CARPET_BOTTOM = 52;      // largeur au premier plan
    private static final int CARPET_HEIGHT = 80;
    private static final int ROLL_WIDTH    = 56;
    private static final int ROLL_HEIGHT   = 9;
    private static final int CUSHION_WIDTH = 48;
    private static final int CUSHION_HEIGHT = 12;
    private static final int CROWN_WIDTH   = 38;
    private static final int CROWN_HEIGHT  = 30;
    private static final int TRUMPET_WIDTH = 32;
    private static final int TRUMPET_HEIGHT = 19;
    /** Pavillon de la trompette, dans son image (en haut à gauche). */
    private static final int BELL_X = 31, BELL_Y = 5;
    private static final int MOUTH_Y = 5;

    private final TextureRegion carpetFull, carpetShown;
    private final Image carpet, roll, cushion, crown, crownGlow;
    private final Image[] trumpets = new Image[2];
    private final TextureRegion note;
    private final TextureRegion[] jewels = new TextureRegion[3];
    private final Vector2 cushionTop = new Vector2();
    private final Vector2 bell = new Vector2();
    private float baseY;

    public CoronationScene() {
        Texture carpetTexture = texture(carpet());
        carpetFull  = new TextureRegion(carpetTexture);
        carpetShown = new TextureRegion(carpetTexture);
        carpet = image(carpetShown, SCALE);
        roll   = image(texture(roll()));
        roll.setOrigin(roll.getWidth() / 2f, roll.getHeight() / 2f);
        cushion = image(texture(cushion()));
        cushion.setOrigin(cushion.getWidth() / 2f, 0f);
        crownGlow = image(texture(halo(56, c("ffe066"))));
        crownGlow.setOrigin(crownGlow.getWidth() / 2f, crownGlow.getHeight() / 2f);
        crown = image(texture(crown()));
        crown.setOrigin(crown.getWidth() / 2f, 0f);
        Texture trumpet = texture(trumpet());
        for (int i = 0; i < 2; i++) {
            trumpets[i] = image(trumpet);
            trumpets[i].setOrigin(0f, (TRUMPET_HEIGHT - MOUTH_Y) * SCALE); // l'embouchure
        }
        note = new TextureRegion(texture(musicNote()));
        String[][] gems = {{"ff4a5a", "ffb0b8", "a8102a"}, {"4ac8ff", "c8f0ff", "1a5aa8"}, {"4affa0", "c8ffe0", "128a4a"}};
        for (int i = 0; i < gems.length; i++) {
            jewels[i] = new TextureRegion(texture(jewel(c(gems[i][0]), c(gems[i][1]), c(gems[i][2]))));
        }
        addActor(carpet);
        addActor(roll);
        addActor(trumpets[0]);
        addActor(trumpets[1]);
        addActor(cushion);
        addActor(crownGlow);
        addActor(crown);
    }

    /** @return la note de musique que crachent les trompettes. */
    public TextureRegion note() { return note; }

    /** @return les joyaux (rubis, saphir, émeraude) qui pleuvent après le sacre. */
    public TextureRegion[] jewels() { return jewels; }

    /** @return le dessus (Stage) du coussin, où se pose la couronne. */
    public Vector2 cushionTop() { return cushionTop; }

    /** @return la position (Stage) du pavillon de la trompette {@code index} (0 : gauche, 1 : droite). */
    public Vector2 bell(int index) {
        return trumpets[index].localToStageCoordinates(bell.set(BELL_X * SCALE, (TRUMPET_HEIGHT - BELL_Y) * SCALE));
    }

    /** Déroule le tapis, son bord du premier plan centré en {@code (x, y)}. */
    @Override
    protected void start(float x, float y) {
        float screenHeight = getStage().getViewport().getWorldHeight();
        baseY = y;
        carpet.setX(x - carpet.getWidth() / 2f);
        carpet.setY(y);
        roll.setVisible(true);
        roll.getColor().a = 1f;
        float top = y + CARPET_HEIGHT * SCALE;

        // Le coussin apparaît au bout du tapis.
        cushion.setPosition(x - cushion.getWidth() / 2f, top - cushion.getHeight() * 0.6f);
        cushionTop.set(x, cushion.getY() + cushion.getHeight() - SCALE);
        cushion.setScale(0f);
        cushion.addAction(Actions.sequence(
            Actions.delay(UNROLLED_TIME),
            Actions.scaleTo(1.2f, 1.2f, 0.1f, Interpolation.pow2Out),
            Actions.scaleTo(1f, 1f, 0.15f)));

        // Les trompettes entrent par les côtés, puis sonnent : elles se dressent à chaque coup.
        for (int i = 0; i < 2; i++) {
            Image trumpet = trumpets[i];
            float side = i == 0 ? -1f : 1f;
            trumpet.setScale(side, 1f);
            trumpet.setRotation(side * TRUMPET_TILT * 0.4f);
            float mouthX = x + side * TRUMPET_GAP, mouthY = top + 20f;
            placeOrigin(trumpet, mouthX + side * 260f, mouthY);
            trumpet.getColor().a = 0f;
            SequenceAction blasts = Actions.sequence(
                Actions.delay(0.4f),
                Actions.parallel(Actions.fadeIn(0.2f),
                    Actions.moveBy(-side * 260f, 0f, 0.3f, Interpolation.pow2Out)));
            float at = 0.7f;
            for (float blast : FANFARE) {
                blasts.addAction(Actions.delay(blast - at));
                blasts.addAction(Actions.parallel(
                    Actions.rotateTo(side * TRUMPET_TILT, 0.04f),
                    Actions.scaleTo(side * 1.12f, 1.12f, 0.04f)));
                blasts.addAction(Actions.parallel(
                    Actions.rotateTo(side * TRUMPET_TILT * 0.6f, 0.11f),
                    Actions.scaleTo(side, 1f, 0.11f)));
                at = blast + 0.15f;
            }
            trumpet.addAction(blasts);
        }

        // La couronne descend lentement du ciel en se balançant, puis luit sur son coussin.
        crown.setColor(Color.WHITE);
        crown.setScale(1f);
        crown.setRotation(0f);
        float landX = x - crown.getWidth() / 2f;
        float landY = cushionTop.y - 3f * SCALE;   // elle s'enfonce un peu dans le velours
        float drop = screenHeight - landY + 20f;
        crown.setPosition(landX, landY + drop);
        crown.addAction(Actions.sequence(
            Actions.delay(CROWN_FROM),
            Actions.parallel(
                Actions.moveBy(0f, -drop, CROWN_TIME - CROWN_FROM, Interpolation.sineOut),
                Actions.sequence(
                    Actions.rotateTo(-6f, 0.2f, Interpolation.sine),
                    Actions.rotateTo(5f, 0.2f, Interpolation.sine),
                    Actions.rotateTo(0f, CROWN_TIME - CROWN_FROM - 0.4f, Interpolation.sine))),
            Actions.scaleTo(1.1f, 0.9f, 0.05f),
            Actions.scaleTo(1f, 1f, 0.25f, Interpolation.elasticOut),
            Actions.forever(Actions.sequence(
                Actions.color(c("fff6c8"), 0.25f),
                Actions.color(Color.WHITE, 0.25f)))));
        placeOrigin(crownGlow, x, landY + crown.getHeight() * 0.5f);
        crownGlow.setScale(0.5f);
        crownGlow.getColor().a = 0f;
        crownGlow.addAction(Actions.sequence(
            Actions.delay(CROWN_TIME),
            Actions.parallel(Actions.alpha(1f, 0.1f), Actions.scaleTo(1.3f, 1.3f, 0.35f, Interpolation.pow2Out)),
            Actions.forever(Actions.sequence(Actions.alpha(0.55f, 0.3f), Actions.alpha(1f, 0.3f)))));
        update(0f);
    }

    @Override
    protected void update(float delta) {
        // Le tapis se déroule du premier plan vers le fond ; le rouleau, de plus en plus mince, le précède.
        float progress = MathUtils.clamp((time - UNROLL_FROM) / (UNROLLED_TIME - UNROLL_FROM), 0f, 1f);
        progress = Interpolation.pow2Out.apply(progress);
        int rows = Math.max(1, Math.round(CARPET_HEIGHT * progress));
        carpetShown.setRegion(carpetFull, 0, CARPET_HEIGHT - rows, carpetFull.getRegionWidth(), rows);
        ((TextureRegionDrawable) carpet.getDrawable()).setRegion(carpetShown);
        carpet.setHeight(rows * SCALE);
        float rowWidth = carpetWidth(CARPET_HEIGHT - rows);
        float edge = baseY + rows * SCALE;
        roll.setScale(rowWidth / ROLL_WIDTH * 1.08f, 1.15f - 0.45f * progress);
        placeOrigin(roll, carpet.getX() + carpet.getWidth() / 2f, edge);
        if (progress >= 1f && roll.isVisible() && roll.getActions().size == 0) {
            roll.addAction(Actions.sequence(Actions.fadeOut(0.15f), Actions.visible(false)));
        }
    }

    /** @return la largeur du tapis à la ligne {@code row} de son image (0 : au loin). */
    private static float carpetWidth(int row) {
        return CARPET_TOP + (CARPET_BOTTOM - CARPET_TOP) * row / (float) (CARPET_HEIGHT - 1);
    }

    /** @return le tapis rouge, en perspective : il s'affine au loin, galons dorés, velours rayé de reflets. */
    private static Pixmap carpet() {
        Pixmap pixmap = pixmap(CARPET_BOTTOM, CARPET_HEIGHT);
        Color red = c("c8102e"), light = c("e8384e"), dark = c("8a0a1e"), gold = Palette.GOLD, goldDark = c("c88a10");
        for (int y = 0; y < CARPET_HEIGHT; y++) {
            float width = carpetWidth(y);
            int left = Math.round((CARPET_BOTTOM - width) / 2f), right = CARPET_BOTTOM - 1 - left;
            hLine(pixmap, left, right, y, OUTLINE);
            hLine(pixmap, left + 1, left + 2, y, gold);
            hLine(pixmap, right - 2, right - 1, y, goldDark);
            boolean sheen = (y + 3) % 9 < 2;
            hLine(pixmap, left + 3, right - 3, y, sheen ? light : red);
            // Motif losangé, discret, au milieu du tapis.
            int middle = CARPET_BOTTOM / 2, diamond = Math.abs((y % 12) - 6) * (right - left) / 40;
            pixmap.setColor(dark);
            pixmap.drawPixel(middle - 1 - diamond, y);
            pixmap.drawPixel(middle + diamond, y);
        }
        return pixmap;
    }

    /** @return le rouleau du tapis encore enroulé : cylindre de velours, tranches dorées. */
    private static Pixmap roll() {
        Pixmap pixmap = pixmap(ROLL_WIDTH, ROLL_HEIGHT);
        fillOutlined(pixmap, 0, 0, ROLL_WIDTH, ROLL_HEIGHT, c("c8102e"));
        hLine(pixmap, 2, ROLL_WIDTH - 3, 2, c("ff6a7a"));
        hLine(pixmap, 1, ROLL_WIDTH - 2, ROLL_HEIGHT - 2, c("8a0a1e"));
        for (int x : new int[] {0, ROLL_WIDTH - 3}) fillOutlined(pixmap, x, 0, 3, ROLL_HEIGHT, Palette.GOLD);
        return pixmap;
    }

    /** @return le coussin de velours pourpre, bordé d'or, un pompon à chaque coin. */
    private static Pixmap cushion() {
        Pixmap pixmap = pixmap(CUSHION_WIDTH, CUSHION_HEIGHT);
        fillShape(pixmap, (x, y) -> {
            float dx = (x + 0.5f - CUSHION_WIDTH / 2f) / (CUSHION_WIDTH / 2f - 2f);
            float dy = (y + 0.5f - CUSHION_HEIGHT / 2f) / (CUSHION_HEIGHT / 2f);
            return dx * dx * dx * dx + dy * dy <= 1f;
        }, (x, y) -> y < 4 ? c("9a4ac8") : y > CUSHION_HEIGHT - 4 ? c("4a1470") : c("6a2a9a"));
        hLine(pixmap, 4, CUSHION_WIDTH - 5, CUSHION_HEIGHT / 2, Palette.GOLD);
        for (int x : new int[] {0, CUSHION_WIDTH - 3}) {
            fillOutlined(pixmap, x, CUSHION_HEIGHT - 5, 3, 5, Palette.GOLD);
        }
        return pixmap;
    }

    /** @return la couronne d'or : cinq fleurons perlés, bandeau serti de rubis, saphir et émeraudes. */
    private static Pixmap crown() {
        Pixmap pixmap = pixmap(CROWN_WIDTH, CROWN_HEIGHT);
        int bandTop = 18;
        int[][] spikes = {{4, 7}, {12, 4}, {19, 1}, {26, 4}, {34, 7}}; // pointe de chaque fleuron (x, y)
        Shape shape = (x, y) -> {
            if (y >= bandTop && x >= 2 && x < CROWN_WIDTH - 2) return true;
            for (int[] spike : spikes) {
                if (y < spike[1] + 3 || y > bandTop) continue;
                float half = 1f + 3.5f * (y - spike[1] - 3) / (float) (bandTop - spike[1] - 3);
                if (Math.abs(x + 0.5f - (spike[0] + 0.5f)) <= half) return true;
            }
            for (int[] spike : spikes) {
                float dx = x - spike[0], dy = y - spike[1] - 1;
                if (dx * dx + dy * dy <= 4.5f) return true; // perle
            }
            return y >= bandTop - 4 && x >= 3 && x < CROWN_WIDTH - 3;
        };
        Color gold = Palette.GOLD, pale = c("fff3a8"), dark = c("c88a10");
        fillShape(pixmap, shape, (x, y) -> {
            if (y < bandTop - 4) {
                for (int[] spike : spikes) {
                    float dx = x - spike[0], dy = y - spike[1] - 1;
                    if (dx * dx + dy * dy <= 4.5f) return dx < 0 && dy < 0 ? Color.WHITE : c("f0ecff");
                }
            }
            if (y >= CROWN_HEIGHT - 3) return dark;
            if (y == bandTop) return pale;
            return x < CROWN_WIDTH / 3 ? pale : x > CROWN_WIDTH * 2 / 3 ? dark : gold;
        });
        Color[] gems = {c("ff4a5a"), c("4affa0"), c("4ac8ff"), c("4affa0"), c("ff4a5a")};
        for (int i = 0; i < gems.length; i++) {
            int gx = 4 + i * 7, gy = bandTop + 3;
            fillOutlined(pixmap, gx, gy, 4, 4, gems[i]);
            pixmap.setColor(Color.WHITE);
            pixmap.drawPixel(gx + 1, gy + 1);
        }
        return pixmap;
    }

    /** @return la trompette de héraut, pavillon à droite, une bannière rouge frappée d'or sous le tube. */
    private static Pixmap trumpet() {
        Pixmap pixmap = pixmap(TRUMPET_WIDTH, TRUMPET_HEIGHT);
        Color gold = Palette.GOLD, pale = c("fff3a8"), dark = c("c88a10");
        // Bannière.
        fillOutlined(pixmap, 8, 6, 14, 10, c("c8102e"));
        for (int x = 9; x < 21; x += 2) {
            pixmap.setColor(gold);
            pixmap.drawPixel(x, 16);
            pixmap.setColor(OUTLINE);
            pixmap.drawPixel(x, 17);
        }
        pixmap.setColor(gold);
        pixmap.fillRectangle(13, 8, 4, 4);
        pixmap.setColor(pale);
        pixmap.drawPixel(13, 8);
        // Tube, embouchure et pistons.
        fillOutlined(pixmap, 0, MOUTH_Y - 2, 24, 4, gold);
        hLine(pixmap, 1, 22, MOUTH_Y - 1, pale);
        fillOutlined(pixmap, 0, MOUTH_Y - 3, 3, 6, dark);
        for (int x : new int[] {10, 13, 16}) fillOutlined(pixmap, x, 0, 2, 4, dark);
        // Pavillon évasé.
        for (int x = 22; x < TRUMPET_WIDTH; x++) {
            int half = 2 + (x - 22) * (x - 22) / 12;
            pixmap.setColor(OUTLINE);
            pixmap.drawLine(x, MOUTH_Y - half - 1, x, MOUTH_Y + half);
            if (x == TRUMPET_WIDTH - 1) continue;
            pixmap.setColor(gold);
            pixmap.drawLine(x, MOUTH_Y - half, x, MOUTH_Y + half - 1);
            pixmap.setColor(pale);
            pixmap.drawPixel(x, MOUTH_Y - half);
        }
        return pixmap;
    }

    /** @return une croche dorée. */
    private static Pixmap musicNote() {
        Pixmap pixmap = pixmap(10, 13);
        drawGrid(pixmap, new String[] {
            "....ooo...",
            "....oGGo..",
            "....oGoGo.",
            "....oGooGo",
            "....oGo.Go",
            "....oGo.oo",
            "....oGo...",
            "....oGo...",
            ".ooooGo...",
            "oGGGGGo...",
            "oPGGGGo...",
            ".oGGGo....",
            "..ooo.....",
        }, 0, 0, "oGP", OUTLINE, Palette.GOLD, c("fff3a8"));
        return pixmap;
    }

    /** @return un joyau taillé : {@code color}, reflet {@code light}, ombre {@code dark}. */
    private static Pixmap jewel(Color color, Color light, Color dark) {
        Pixmap pixmap = pixmap(9, 8);
        drawGrid(pixmap, new String[] {
            ".ooooooo.",
            "oLLCCCCDo",
            "oCCCCCCCo",
            ".oCCCCDo.",
            "..oCCDo..",
            "...oDo...",
            "....o....",
        }, 0, 0, "oLCD", OUTLINE, light, color, dark);
        return pixmap;
    }
}
