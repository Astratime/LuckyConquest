package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import fr.astratime.lucky.assets.Palette;

/**
 * Bingo de la Pépite : la mine. Un gros rocher veiné d'or est posé sur la
 * table ; une pioche le frappe trois fois (à {@link #HITS}), la fissure
 * s'élargit à chaque coup et, au troisième, il se fend en deux moitiés qui
 * basculent en découvrant un cœur d'or (le geyser de pépites est lancé par
 * {@link JackpotCelebration}).
 */
public class MineScene extends BingoScene {

    /** Instants des trois coups de pioche ; le rocher se fend au dernier. */
    public static final float[] HITS = {0.55f, 1.0f, 1.45f};

    // Angles de la pioche (0 : fer en haut ; 90 : manche horizontal, fer à gauche).
    private static final float RAISED = 10f;
    private static final float HIT    = 75f;
    private static final float SWING  = 0.12f;
    private static final float SPLIT_TILT = 24f;
    private static final float SPLIT_GAP  = 70f;

    private static final int ROCK_WIDTH  = 48;
    private static final int ROCK_HEIGHT = 32;
    private static final int PICK_WIDTH  = 40;
    private static final int PICK_HEIGHT = 46;
    /** Pointe gauche du fer de la pioche, dans son image (en haut à gauche). */
    private static final int PICK_TIP_X = 1, PICK_TIP_Y = 13;
    private static final int HANDLE_X   = 19;

    private final TextureRegion[] rocks = new TextureRegion[3];   // intact, fissuré, très fissuré
    private final Image rock, core, left, right, pick;
    private final Vector2 strike = new Vector2();
    private final Vector2 heart = new Vector2();

    public MineScene() {
        for (int cracks = 0; cracks < rocks.length; cracks++) rocks[cracks] = new TextureRegion(texture(rock(cracks, 0)));
        rock  = image(rocks[0], SCALE);
        core  = image(texture(goldCore()));
        core.setOrigin(core.getWidth() / 2f, 0f);
        left  = image(new TextureRegion(texture(rock(2, -1))), SCALE);
        left.setOrigin(0f, 0f);            // bascule sur son coin du bas, à l'extérieur
        right = image(new TextureRegion(texture(rock(2, 1))), SCALE);
        right.setOrigin(right.getWidth(), 0f);
        pick  = image(texture(pickaxe()));
        pick.setOrigin((HANDLE_X + 1) * SCALE, 2f * SCALE); // bout du manche
        addActor(core);
        addActor(rock);
        addActor(left);
        addActor(right);
        addActor(pick);
    }

    /** @return le point (Stage) du rocher où frappe la pioche. */
    public Vector2 strikePoint() { return strike; }

    /** @return le cœur (Stage) du rocher fendu, d'où jaillissent les pépites. */
    public Vector2 heart() { return heart; }

    /** Pose le rocher, le milieu de sa base en {@code (x, y)}, puis programme les trois coups. */
    @Override
    protected void start(float x, float y) {
        float left0 = x - rock.getWidth() / 2f;
        rock.setPosition(left0, y);
        rock.setVisible(true);
        rock.setScale(1f);
        rock.setOrigin(rock.getWidth() / 2f, 0f);
        setRock(0);
        for (Image half : new Image[] {left, right}) {
            half.setPosition(left0, y);
            half.setRotation(0f);
            half.setVisible(false);
        }
        core.setPosition(x - core.getWidth() / 2f, y);
        core.setVisible(false);
        core.setScale(1f);
        core.setColor(Color.WHITE);
        heart.set(x, y + rock.getHeight() * 0.55f);
        strike.set(x + (crackX(0) + 0.5f - ROCK_WIDTH / 2f) * SCALE, y + (ROCK_HEIGHT - 1) * SCALE);

        // La pointe du fer touche le haut du rocher quand le manche est à l'angle de frappe.
        Vector2 tip = new Vector2((PICK_TIP_X - HANDLE_X - 1) * SCALE, (PICK_HEIGHT - PICK_TIP_Y - 2) * SCALE).rotateDeg(HIT);
        placeOrigin(pick, strike.x - tip.x, strike.y - tip.y);
        pick.setRotation(RAISED);
        pick.getColor().a = 0f;

        SequenceAction swings = Actions.sequence(Actions.fadeIn(0.15f));
        float at = 0.15f;
        for (float hit : HITS) {
            swings.addAction(Actions.rotateTo(RAISED - 25f, hit - SWING - at, Interpolation.pow2Out)); // élan
            swings.addAction(Actions.rotateTo(HIT, SWING, Interpolation.pow3In));
            at = hit;
            if (hit != HITS[HITS.length - 1]) {
                swings.addAction(Actions.rotateTo(HIT - 25f, 0.12f, Interpolation.pow2Out)); // rebond
                at += 0.12f;
            }
        }
        swings.addAction(Actions.parallel(
            Actions.rotateTo(30f, 0.3f, Interpolation.pow2Out),
            Actions.sequence(Actions.delay(0.15f), Actions.fadeOut(0.3f))));
        pick.addAction(swings);

        // Le rocher tressaille à chaque coup.
        SequenceAction jolts = Actions.sequence();
        float previous = 0f;
        for (int i = 0; i < HITS.length - 1; i++) {
            jolts.addAction(Actions.delay(HITS[i] - previous));
            jolts.addAction(Actions.scaleTo(1.05f, 0.92f, 0.04f));
            jolts.addAction(Actions.scaleTo(1f, 1f, 0.12f));
            previous = HITS[i] + 0.16f;
        }
        rock.addAction(jolts);
    }

    @Override
    protected void update(float delta) {
        float last = HITS[HITS.length - 1];
        if (time < last) {
            setRock(time >= HITS[1] ? 2 : time >= HITS[0] ? 1 : 0);
            return;
        }
        if (!rock.isVisible()) return;
        // Il se fend : les deux moitiés basculent de part et d'autre, le cœur d'or apparaît.
        rock.setVisible(false);
        for (Image half : new Image[] {left, right}) {
            float side = half == left ? -1f : 1f;
            half.setVisible(true);
            half.addAction(Actions.parallel(
                Actions.moveBy(side * SPLIT_GAP, 0f, 0.3f, Interpolation.pow2Out),
                Actions.rotateBy(-side * SPLIT_TILT, 0.3f, Interpolation.pow2Out)));
        }
        core.setVisible(true);
        core.setScale(0.4f);
        core.addAction(Actions.sequence(
            Actions.scaleTo(1.15f, 1.15f, 0.15f, Interpolation.pow2Out),
            Actions.scaleTo(1f, 1f, 0.15f),
            Actions.forever(Actions.sequence(
                Actions.color(c("fff6c8"), 0.15f),
                Actions.color(Color.WHITE, 0.15f)))));
    }

    private void setRock(int cracks) {
        ((TextureRegionDrawable) rock.getDrawable()).setRegion(rocks[cracks]);
    }

    /** @return {@code true} si {@code (x, y)} est dans le rocher : un gros bloc arrondi, aplati à sa base. */
    private static boolean inRock(int x, int y) {
        float dx = (x + 0.5f - ROCK_WIDTH / 2f) / (ROCK_WIDTH / 2f);
        float dy = (y + 0.5f - ROCK_HEIGHT * 0.62f) / (ROCK_HEIGHT * 0.62f);
        // Bosses irrégulières sur le dessus.
        float bump = 0.06f * (float) Math.sin(x * 0.7f) + 0.04f * (float) Math.cos(x * 1.9f);
        return y < ROCK_HEIGHT && dx * dx + dy * dy <= 1f + bump;
    }

    /** @return l'abscisse de la fissure du rocher à la ligne {@code y} : un zigzag du haut vers le bas. */
    private static int crackX(int y) {
        int[] zigzag = {0, 1, 2, 1, 0, -1, -2, -1, 0, 1, 2, 2, 1, 0, -1, -1};
        return ROCK_WIDTH / 2 + 2 + zigzag[(y / 2) % zigzag.length];
    }

    /**
     * @return le rocher gris veiné d'or, avec {@code cracks} fissures (0 à 2) ;
     *         {@code half} -1 ou 1 n'en garde que la moitié gauche ou droite (fendu), 0 entier.
     */
    private static Pixmap rock(int cracks, int half) {
        Pixmap pixmap = pixmap(ROCK_WIDTH, ROCK_HEIGHT);
        Color stone = c("7a7a88"), light = c("a0a0b0"), dark = c("50505e"), vein = Palette.GOLD;
        Shape shape = (x, y) -> inRock(x, y) && (half == 0 || (half < 0 ? x < crackX(y) : x >= crackX(y)));
        fillShape(pixmap, shape, (x, y) -> {
            // Paillettes d'or éparses.
            int fleck = Math.floorMod(x * 7919 ^ y * 104729, 47);
            if (fleck == 0 && y > 5) return vein;
            if (fleck == 1 && y > 5) return c("ffe9a0");
            float dx = x - ROCK_WIDTH * 0.35f, dy = y - ROCK_HEIGHT * 0.3f;
            if (dx * dx + dy * dy < 40f) return light;
            if (y > ROCK_HEIGHT - 6 || x > ROCK_WIDTH - 9) return dark;
            return stone;
        });
        if (half == 0 && cracks > 0) {
            // La fissure descend du point d'impact, plus longue au second coup.
            int depth = cracks == 1 ? ROCK_HEIGHT / 3 : ROCK_HEIGHT * 3 / 4;
            pixmap.setColor(OUTLINE);
            for (int y = 0; y < depth; y++) {
                int x = crackX(y);
                if (!inRock(x, y)) continue;
                pixmap.drawPixel(x, y);
                if (cracks == 2 && y < depth - 3) pixmap.drawPixel(x + 1, y);
            }
            if (cracks == 2) {
                // Une lueur dorée filtre déjà de la fissure.
                pixmap.setColor(Palette.GOLD);
                for (int y = 6; y < depth - 6; y += 3) pixmap.drawPixel(crackX(y), y);
            }
        }
        return pixmap;
    }

    /** @return le cœur d'or du rocher fendu : un amas de pépites luisantes. */
    private static Pixmap goldCore() {
        int width = 22, height = 20;
        Pixmap pixmap = pixmap(width, height);
        Color gold = Palette.GOLD, pale = c("fff3a8"), dark = c("c88a10");
        fillShape(pixmap, (x, y) -> {
            float dx = (x + 0.5f - width / 2f) / (width / 2f), dy = (y + 0.5f - height) / height;
            return dx * dx + dy * dy <= 1f;
        }, (x, y) -> (x * 5 + y * 3) % 7 == 0 ? pale : (x + y * 2) % 5 == 0 ? dark : gold);
        return pixmap;
    }

    /** @return la pioche, fer en haut : fer d'acier courbé à deux pointes, long manche en bois. */
    private static Pixmap pickaxe() {
        Pixmap pixmap = pixmap(PICK_WIDTH, PICK_HEIGHT);
        // Manche.
        fillOutlined(pixmap, HANDLE_X - 1, 6, 4, PICK_HEIGHT - 6, Palette.WOOD);
        pixmap.setColor(Palette.WOOD_LIGHT);
        pixmap.drawLine(HANDLE_X, 7, HANDLE_X, PICK_HEIGHT - 2);
        // Fer : un arc d'acier qui retombe en pointe de chaque côté.
        fillShape(pixmap, (x, y) -> {
            float dx = x + 0.5f - PICK_WIDTH / 2f;
            float arc = 3f + dx * dx / 40f;            // le fer s'incurve vers le bas en s'éloignant du manche
            float thickness = 6f - Math.abs(dx) / 5f;
            return Math.abs(dx) <= PICK_WIDTH / 2f - 0.5f && y >= arc - 0.5f && y <= arc + thickness;
        }, (x, y) -> {
            float dx = x + 0.5f - PICK_WIDTH / 2f;
            return y <= 3f + dx * dx / 40f + 0.6f ? Palette.STEEL_LIGHT : Palette.STEEL;
        });
        // Douille, où le manche traverse le fer.
        fillOutlined(pixmap, HANDLE_X - 2, 1, 6, 8, Palette.IRON);
        hLine(pixmap, HANDLE_X - 1, HANDLE_X + 2, 2, c("8a8a9c"));
        return pixmap;
    }
}
