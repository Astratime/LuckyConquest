package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import fr.astratime.lucky.assets.Palette;

/**
 * Bingo de la Bombe : une bombe géante tombe au milieu de la table, sa mèche
 * grésillant et raccourcissant ; un compte à rebours 3, 2, 1 s'affiche sur
 * elle (à {@link #TICKS}) pendant qu'elle enfle, rougit et tremble de plus
 * en plus, puis elle explose en une boule de feu qui envahit l'écran (la
 * fumée, les débris et le flash sont lancés par {@link JackpotCelebration}).
 * En effets réduits, la boule de feu reste à la taille de la bombe.
 */
public class BombScene extends BingoScene {

    /** Instants où s'affichent le 3, le 2 puis le 1. */
    public static final float[] TICKS = {0.35f, 0.75f, 1.15f};
    /** La bombe explose. */
    public static final float BLAST_TIME = 1.55f;

    private static final float DROP_TIME    = 0.25f;
    private static final float BOMB_SCALE   = 7f;
    private static final float DIGIT_SCALE  = 14f;
    private static final float TREMBLE_MAX  = 7f;
    private static final float BLAST_GROWTH = 0.45f;   // durée de l'expansion de la boule de feu
    private static final float BLAST_SIZE_FULL    = 9f;   // agrandissement final : tout l'écran
    private static final float BLAST_SIZE_REDUCED = 1.6f;

    private static final int BOMB_WIDTH  = 40;
    private static final int BOMB_HEIGHT = 44;
    private static final int BALL_X = 18, BALL_Y = 27, BALL_RADIUS = 16;
    private static final int FIREBALL_SIZE = 48;
    /** Tracé de la mèche, du bouchon de la bombe à son bout (pixels de l'image). */
    private static final int[][] FUSE = {{18, 9}, {19, 7}, {21, 5}, {23, 4}, {25, 4}, {27, 3}, {28, 2}, {29, 1}};

    private final TextureRegion[] bombs = new TextureRegion[FUSE.length]; // mèche de plus en plus courte
    private final TextureRegion[] digits = new TextureRegion[3];          // « 1 », « 2 », « 3 »
    private final Image bomb, digit, fireball;
    private final Vector2 center = new Vector2(), fuseTip = new Vector2();
    private boolean reducedEffects;

    public BombScene() {
        for (int length = 0; length < FUSE.length; length++) bombs[length] = new TextureRegion(texture(bomb(length)));
        for (int value = 1; value <= 3; value++) digits[value - 1] = new TextureRegion(texture(digit(value)));
        bomb = image(bombs[FUSE.length - 1], BOMB_SCALE);
        bomb.setOrigin(BALL_X * BOMB_SCALE, (BOMB_HEIGHT - BALL_Y) * BOMB_SCALE);
        digit = image(digits[2], DIGIT_SCALE);
        digit.setOrigin(digit.getWidth() / 2f, digit.getHeight() / 2f);
        fireball = image(new TextureRegion(texture(fireball())), SCALE);
        fireball.setOrigin(fireball.getWidth() / 2f, fireball.getHeight() / 2f);
        addActor(bomb);
        addActor(digit);
        addActor(fireball);
    }

    /** En effets réduits, la boule de feu n'envahit pas l'écran. */
    public void setReducedEffects(boolean reduced) { this.reducedEffects = reduced; }

    /** @return le centre (Stage) de la bombe, puis de l'explosion. */
    public Vector2 center() { return center; }

    /** @return la position (Stage) du bout de la mèche, qui grésille. */
    public Vector2 fuseTip() {
        int[] point = FUSE[fuseLength()];
        return bomb.localToStageCoordinates(fuseTip.set((point[0] + 0.5f) * BOMB_SCALE,
            (BOMB_HEIGHT - point[1] - 0.5f) * BOMB_SCALE));
    }

    /** Fait tomber la bombe, centrée en {@code (x, y)}. */
    @Override
    protected void start(float x, float y) {
        float screenHeight = getStage().getViewport().getWorldHeight();
        center.set(x, y);
        float drop = screenHeight - y + bomb.getHeight();
        setBomb(FUSE.length - 1);
        bomb.setVisible(true);
        bomb.setColor(Color.WHITE);
        bomb.setScale(1f);
        placeOrigin(bomb, x, y + drop);
        bomb.addAction(Actions.sequence(
            Actions.moveBy(0f, -drop, DROP_TIME, Interpolation.pow2In),
            Actions.scaleTo(1.15f, 0.85f, 0.05f),
            Actions.scaleTo(1f, 1f, 0.2f, Interpolation.elasticOut),
            Actions.delay(TICKS[TICKS.length - 1] - DROP_TIME - 0.25f),
            // Au « 1 », elle rougit en palpitant jusqu'à l'explosion.
            Actions.forever(Actions.sequence(Actions.color(c("ff9a8a"), 0.1f), Actions.color(Color.WHITE, 0.1f)))));

        digit.setVisible(false);
        digit.setScale(0f);
        fireball.setVisible(false);
    }

    @Override
    protected void update(float delta) {
        if (time >= BLAST_TIME) {
            if (bomb.isVisible()) blast();
            return;
        }
        setBomb(fuseLength());
        // Compte à rebours : chaque chiffre surgit, et la bombe enfle d'un coup.
        for (int i = TICKS.length - 1; i >= 0; i--) {
            if (time < TICKS[i]) continue;
            int value = 3 - i;
            TextureRegionDrawable drawable = (TextureRegionDrawable) digit.getDrawable();
            if (!digit.isVisible() || drawable.getRegion() != digits[value - 1]) {
                drawable.setRegion(digits[value - 1]);
                digit.setVisible(true);
                digit.clearActions();
                digit.setScale(1.8f);
                digit.setColor(value == 1 ? c("ff5a5a") : Color.WHITE);
                digit.addAction(Actions.scaleTo(1f, 1f, 0.2f, Interpolation.swingOut));
                float swell = 1.06f + i * 0.05f;
                bomb.addAction(Actions.sequence(Actions.scaleTo(swell, swell, 0.06f),
                    Actions.scaleTo(1f + i * 0.04f, 1f + i * 0.04f, 0.2f)));
            }
            break;
        }
        // De plus en plus nerveuse en approchant de l'explosion.
        if (time > DROP_TIME + 0.25f) {
            float shake = TREMBLE_MAX * time / BLAST_TIME;
            float dx = MathUtils.random(-shake, shake), dy = MathUtils.random(-shake, shake);
            placeOrigin(bomb, center.x + dx, center.y + dy);
            placeOrigin(digit, center.x + dx, center.y + dy - BOMB_SCALE);
        } else {
            placeOrigin(digit, center.x, center.y - BOMB_SCALE);
        }
    }

    /** La bombe disparaît dans une boule de feu qui grandit puis s'estompe. */
    private void blast() {
        bomb.setVisible(false);
        bomb.clearActions();
        digit.setVisible(false);
        digit.clearActions();
        placeOrigin(fireball, center.x, center.y);
        fireball.setVisible(true);
        fireball.setScale(0.4f);
        fireball.setRotation(MathUtils.random(360f));
        fireball.setColor(Color.WHITE);
        float size = reducedEffects ? BLAST_SIZE_REDUCED : BLAST_SIZE_FULL;
        fireball.addAction(Actions.sequence(
            Actions.parallel(
                Actions.scaleTo(size, size, BLAST_GROWTH, Interpolation.pow3Out),
                Actions.rotateBy(40f, BLAST_GROWTH),
                Actions.sequence(Actions.delay(BLAST_GROWTH * 0.2f), Actions.fadeOut(BLAST_GROWTH * 0.8f))),
            Actions.visible(false)));
    }

    /** @return combien de segments de mèche il reste : elle brûle de la chute jusqu'à l'explosion. */
    private int fuseLength() {
        float burnt = MathUtils.clamp(time / BLAST_TIME, 0f, 1f);
        return Math.max(0, Math.round((FUSE.length - 1) * (1f - burnt)));
    }

    private void setBomb(int fuseLength) {
        ((TextureRegionDrawable) bomb.getDrawable()).setRegion(bombs[fuseLength]);
    }

    /** @return la bombe : boule de fonte bleu nuit, reflet, bouchon d'acier et mèche de {@code fuseLength} segments. */
    private static Pixmap bomb(int fuseLength) {
        Pixmap pixmap = pixmap(BOMB_WIDTH, BOMB_HEIGHT);
        Color body = c("3a3f5c"), light = c("6a7090"), shine = c("b8c0e0"), dark = c("23263a");
        fillShape(pixmap, (x, y) -> {
            float dx = x + 0.5f - BALL_X, dy = y + 0.5f - BALL_Y;
            return dx * dx + dy * dy <= BALL_RADIUS * BALL_RADIUS;
        }, (x, y) -> {
            float dx = x + 0.5f - BALL_X, dy = y + 0.5f - BALL_Y;
            float highlight = (dx + 6f) * (dx + 6f) + (dy + 6f) * (dy + 6f);
            if (highlight < 6f) return shine;
            if (highlight < 40f) return light;
            if (dx + dy > 12f) return dark;
            return body;
        });
        // Bouchon d'acier, sur le dessus.
        fillOutlined(pixmap, BALL_X - 4, 8, 9, 5, Palette.STEEL);
        hLine(pixmap, BALL_X - 3, BALL_X + 3, 9, Palette.STEEL_LIGHT);
        // Mèche torsadée.
        for (int i = 0; i <= fuseLength; i++) {
            int[] point = FUSE[i];
            pixmap.setColor(OUTLINE);
            pixmap.fillRectangle(point[0] - 1, point[1] - 1, 3, 3);
        }
        for (int i = 0; i <= fuseLength; i++) {
            int[] point = FUSE[i];
            pixmap.setColor(i % 2 == 0 ? c("c8a070") : c("8a6a40"));
            pixmap.drawPixel(point[0], point[1]);
        }
        // Le bout qui brûle.
        int[] tip = FUSE[fuseLength];
        pixmap.setColor(Palette.ORANGE);
        pixmap.drawPixel(tip[0], tip[1]);
        return pixmap;
    }

    /** @return le chiffre {@code value} (1 à 3) du compte à rebours, épais et cerné de sombre. */
    private static Pixmap digit(int value) {
        String[] rows = switch (value) {
            case 1 -> new String[] {"..#..", ".##..", "#.#..", "..#..", "..#..", "..#..", "#####"};
            case 2 -> new String[] {".###.", "#...#", "....#", "..##.", ".#...", "#....", "#####"};
            default -> new String[] {"####.", "....#", "....#", ".###.", "....#", "....#", "####."};
        };
        int width = rows[0].length() + 2, height = rows.length + 2;
        Pixmap pixmap = pixmap(width, height);
        // Liseré : chaque pixel du chiffre noircit ses voisins, puis le chiffre en blanc par-dessus.
        pixmap.setColor(OUTLINE);
        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < rows[y].length(); x++) {
                if (rows[y].charAt(x) == '#') pixmap.fillRectangle(x, y, 3, 3);
            }
        }
        drawGrid(pixmap, rows, 1, 1, "#", Color.WHITE);
        return pixmap;
    }

    /** @return la boule de feu : cœur blanc, jaune, orange, bord rouge déchiqueté. */
    private static Pixmap fireball() {
        Pixmap pixmap = pixmap(FIREBALL_SIZE, FIREBALL_SIZE);
        float center = FIREBALL_SIZE / 2f;
        Color[] layers = {c("c8201a"), Palette.ORANGE, c("ffc640"), c("fff3c0"), Color.WHITE};
        for (int y = 0; y < FIREBALL_SIZE; y++) {
            for (int x = 0; x < FIREBALL_SIZE; x++) {
                float dx = x + 0.5f - center, dy = y + 0.5f - center;
                float angle = MathUtils.atan2(dy, dx);
                // Bord déchiqueté : le rayon ondule avec l'angle.
                float edge = center - 1f - 2.5f * (0.5f + 0.5f * MathUtils.sin(angle * 7f) * MathUtils.cos(angle * 3f));
                float distance = (float) Math.sqrt(dx * dx + dy * dy) / edge;
                if (distance > 1f) continue;
                int layer = Math.min(layers.length - 1, (int) ((1f - distance) * layers.length * 1.1f));
                pixmap.setColor(layers[layer]);
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }
}
