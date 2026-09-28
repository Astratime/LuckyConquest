package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Bingo du Sept : un gros dé roule sur la table depuis la gauche en
 * rebondissant et en tournoyant, ses faces défilant à chaque rebond, puis
 * s'arrête au centre sur une face « 7 » qui s'embrase (les flammes sont
 * lancées par {@link JackpotCelebration}).
 */
public class FlamingDieScene extends BingoScene {

    /** Le dé s'immobilise sur sa face « 7 ». */
    public static final float LAND_TIME = 1.0f;

    private static final int   SIZE      = 26;       // côté du dé, en pixels de l'image
    private static final float DIE_SCALE = 6f;       // un peu plus grand que les autres décors
    private static final float ROLL_FROM = -760f;    // départ, relatif au centre
    private static final float[] BOUNCES = {0.3f, 0.6f, 0.82f, LAND_TIME}; // instants des contacts avec la table
    private static final float[] HEIGHTS = {220f, 130f, 60f, 22f};         // hauteur du saut avant chaque contact
    private static final float FACE_CHANGE = 0.07f;

    private final TextureRegion[] faces = new TextureRegion[7]; // 1 à 6, puis le 7 (indice 0)
    private final Image die;
    private final Image glow;
    private float centerX, centerY;
    private float faceTimer;
    private boolean landed;

    public FlamingDieScene() {
        for (int value = 0; value <= 6; value++) faces[value] = new TextureRegion(texture(face(value)));
        Texture glowTexture = texture(glowSquare());
        glow = image(new TextureRegion(glowTexture), DIE_SCALE);
        glow.setOrigin(glow.getWidth() / 2f, glow.getHeight() / 2f);
        die = image(faces[1], DIE_SCALE);
        die.setOrigin(die.getWidth() / 2f, die.getHeight() / 2f);
        addActor(glow);
        addActor(die);
    }

    /** @return la position (Stage) du haut du dé posé, d'où partent les flammes. */
    public float topY() { return centerY + halfSize(); }

    /** @return la moitié du côté du dé affiché. */
    public float halfSize() { return SIZE * DIE_SCALE / 2f; }

    @Override
    protected void start(float x, float y) {
        centerX   = x;
        centerY   = y;
        landed    = false;
        faceTimer = 0f;
        die.setColor(Color.WHITE);
        die.setScale(1f);
        setFace(MathUtils.random(1, 6));
        glow.getColor().a = 0f;
        placeOrigin(glow, x, y);
        glow.addAction(Actions.sequence(
            Actions.delay(LAND_TIME),
            Actions.alpha(0.85f, 0.15f),
            Actions.forever(Actions.sequence(Actions.alpha(0.45f, 0.18f), Actions.alpha(0.85f, 0.18f)))));
        update(0f);
    }

    @Override
    protected void update(float delta) {
        if (time >= LAND_TIME) {
            if (!landed) land();
            return;
        }
        float progress = time / LAND_TIME;
        float x = centerX + ROLL_FROM * (1f - Interpolation.pow2Out.apply(progress));
        // Saut parabolique entre deux contacts avec la table.
        float hop = 0f, previous = 0f;
        for (int i = 0; i < BOUNCES.length; i++) {
            if (time < BOUNCES[i]) {
                float t = (time - previous) / (BOUNCES[i] - previous);
                hop = HEIGHTS[i] * 4f * t * (1f - t);
                break;
            }
            previous = BOUNCES[i];
        }
        placeOrigin(die, x, centerY + hop);
        die.setRotation(-1080f * Interpolation.pow2Out.apply(progress));
        faceTimer += delta;
        if (faceTimer >= FACE_CHANGE) {
            faceTimer = 0f;
            setFace(MathUtils.random(1, 6));
        }
    }

    /** Le dé se pose : face « 7 », petit écrasement, puis il rougeoit en palpitant. */
    private void land() {
        landed = true;
        placeOrigin(die, centerX, centerY);
        die.setRotation(0f);
        setFace(0);
        die.addAction(Actions.sequence(
            Actions.scaleTo(1.18f, 0.82f, 0.06f),
            Actions.scaleTo(1f, 1f, 0.25f, Interpolation.elasticOut),
            Actions.forever(Actions.sequence(
                Actions.color(c("ffd0a0"), 0.18f),
                Actions.color(Color.WHITE, 0.18f)))));
    }

    private void setFace(int value) {
        ((TextureRegionDrawable) die.getDrawable()).setRegion(faces[value]);
    }

    /** @return une face du dé : {@code value} points (1 à 6), ou un grand « 7 » rouge pour 0. */
    private static Pixmap face(int value) {
        Pixmap pixmap = pixmap(SIZE, SIZE);
        Color body = c("f5f0e6"), shade = c("cfc6b4"), light = c("ffffff");
        fillOutlined(pixmap, 0, 0, SIZE, SIZE, body);
        hLine(pixmap, 2, SIZE - 3, 1, light);
        hLine(pixmap, 1, SIZE - 2, SIZE - 2, shade);
        hLine(pixmap, 1, SIZE - 2, SIZE - 3, shade);
        pixmap.setColor(shade);
        pixmap.drawLine(SIZE - 2, 1, SIZE - 2, SIZE - 2);
        // Coins arrondis.
        pixmap.setColor(0f, 0f, 0f, 0f);
        for (int[] corner : new int[][] {{0, 0}, {SIZE - 1, 0}, {0, SIZE - 1}, {SIZE - 1, SIZE - 1}}) {
            pixmap.drawPixel(corner[0], corner[1]);
        }
        if (value == 0) {
            drawSeven(pixmap);
        } else {
            int[][] layout = switch (value) {
                case 1 -> new int[][] {{1, 1}};
                case 2 -> new int[][] {{0, 0}, {2, 2}};
                case 3 -> new int[][] {{0, 0}, {1, 1}, {2, 2}};
                case 4 -> new int[][] {{0, 0}, {2, 0}, {0, 2}, {2, 2}};
                case 5 -> new int[][] {{0, 0}, {2, 0}, {1, 1}, {0, 2}, {2, 2}};
                default -> new int[][] {{0, 0}, {2, 0}, {0, 1}, {2, 1}, {0, 2}, {2, 2}};
            };
            for (int[] pip : layout) {
                int px = 5 + pip[0] * 7, py = 5 + pip[1] * 7;
                pixmap.setColor(value == 1 ? c("d42a34") : c("2a2030"));
                pixmap.fillRectangle(px, py, 4, 4);
                pixmap.setColor(value == 1 ? c("ff7a80") : c("5a5068"));
                pixmap.drawPixel(px, py);
            }
        }
        return pixmap;
    }

    /** Dessine un grand « 7 » rouge cerné de sombre au milieu de la face. */
    private static void drawSeven(Pixmap pixmap) {
        for (int pass = 0; pass < 2; pass++) {
            pixmap.setColor(pass == 0 ? OUTLINE : c("e0303c"));
            int grow = pass == 0 ? 1 : 0;
            // Barre du haut.
            pixmap.fillRectangle(6 - grow, 5 - grow, 14 + grow * 2, 4 + grow * 2);
            // Jambe en diagonale, du bout de la barre vers le bas.
            for (int y = 9; y <= 20; y++) {
                int x = 17 - (y - 9) * 7 / 11;
                pixmap.fillRectangle(x - 1 - grow, y - (y == 9 ? grow : 0), 4 + grow * 2, 1 + (y == 20 ? grow : 0));
            }
        }
        pixmap.setColor(c("ff8a90"));
        pixmap.drawLine(7, 6, 18, 6);
    }

    /** @return un halo orangé carré, plus grand que le dé, qui rougeoie derrière lui. */
    private static Pixmap glowSquare() {
        int size = SIZE + 8;
        Pixmap pixmap = pixmap(size, size);
        for (int ring = 0; ring < 4; ring++) {
            pixmap.setColor(1f, 0.45f + ring * 0.1f, 0.1f, 0.18f + ring * 0.12f);
            pixmap.fillRectangle(ring, ring, size - ring * 2, size - ring * 2);
        }
        return pixmap;
    }
}
