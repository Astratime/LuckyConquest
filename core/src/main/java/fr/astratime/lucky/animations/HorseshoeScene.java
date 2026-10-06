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
 * Bingo du Fer à cheval : un poteau de bois se dresse au milieu de la table,
 * un piquet planté près de son sommet. Un fer à cheval doré géant est lancé
 * depuis la gauche en tournoyant et vient s'accrocher au piquet (« ding ! ») ;
 * il s'y balance, de moins en moins, puis luit (la pluie de trèfles et de
 * pièces est lancée par {@link JackpotCelebration}, qui reprend le trèfle de
 * {@link #clover()}).
 */
public class HorseshoeScene extends BingoScene {

    /** Le fer s'accroche au piquet. */
    public static final float HOOK_TIME = 1.0f;

    private static final float LAUNCH_TIME = 0.25f;
    private static final float THROW_FROM_X = -720f;   // départ, relatif au piquet
    private static final float THROW_FROM_Y = -260f;
    private static final float ARC_HEIGHT   = 420f;    // hauteur de la parabole au-dessus de la ligne droite
    private static final float THROW_SPIN   = -1080f;  // tours du fer en vol (il finit droit)
    private static final float SWING        = 26f;     // amplitude du premier balancement, en degrés
    private static final float SWING_SPEED  = 9f;
    private static final float SWING_DAMPING = 2.4f;

    /** Agrandissement du décor : le fer est géant. */
    private static final float HORSESHOE_SCALE = 6f;

    private static final int SHOE_SIZE   = 32;
    /** Centre du piquet dans l'image du fer (en haut à gauche) : contre le creux de l'arc. */
    private static final int HOOK_X      = 16;
    private static final int HOOK_Y      = 21;
    private static final int POST_WIDTH  = 14;
    private static final int POST_HEIGHT = 54;
    private static final int PEG_FROM_TOP = 13;       // hauteur du piquet, depuis le haut du poteau
    private static final int KNOB_SIZE   = 8;
    private static final int CLOVER_SIZE = 13;

    private final Image post, shoe, knob, glow;
    private final TextureRegion clover;
    private final Vector2 peg = new Vector2();
    private boolean hooked;

    public HorseshoeScene() {
        post = image(new TextureRegion(texture(post())), HORSESHOE_SCALE);
        glow = image(texture(halo(48, c("fff0a0"))));
        glow.setOrigin(glow.getWidth() / 2f, glow.getHeight() / 2f);
        shoe = image(new TextureRegion(texture(horseshoe())), HORSESHOE_SCALE);
        shoe.setOrigin(HOOK_X * HORSESHOE_SCALE, (SHOE_SIZE - HOOK_Y) * HORSESHOE_SCALE);
        knob = image(new TextureRegion(texture(knob())), HORSESHOE_SCALE);
        clover = new TextureRegion(texture(cloverLeaf()));
        addActor(post);
        addActor(glow);
        addActor(shoe);
        addActor(knob); // le bout du piquet passe devant le fer
    }

    /** @return la position (Stage) du piquet, où le fer s'accroche. */
    public Vector2 peg() { return peg; }

    /** @return le trèfle à quatre feuilles, en pixel art, qui pleut après l'accroche. */
    public TextureRegion clover() { return clover; }

    /** Dresse le poteau, son pied centré en {@code (x, y)}, puis lance le fer. */
    @Override
    protected void start(float x, float y) {
        hooked = false;
        post.setPosition(x - post.getWidth() / 2f, y);
        post.getColor().a = 0f;
        post.addAction(Actions.fadeIn(0.2f));
        peg.set(x, y + (POST_HEIGHT - PEG_FROM_TOP) * HORSESHOE_SCALE);
        knob.setPosition(peg.x - knob.getWidth() / 2f, peg.y - knob.getHeight() / 2f);
        knob.getColor().a = 0f;
        knob.addAction(Actions.fadeIn(0.2f));

        shoe.setColor(Color.WHITE);
        shoe.setScale(1f);
        shoe.setVisible(false);
        glow.setVisible(false);
        glow.setScale(0.6f);
        placeOrigin(glow, peg.x, peg.y + 3f * HORSESHOE_SCALE);
        update(0f);
    }

    @Override
    protected void update(float delta) {
        if (time < LAUNCH_TIME) return;
        if (time < HOOK_TIME) {
            // En vol : parabole depuis le bas à gauche, le fer tournoie sur lui-même.
            shoe.setVisible(true);
            float progress = (time - LAUNCH_TIME) / (HOOK_TIME - LAUNCH_TIME);
            float x = peg.x + THROW_FROM_X * (1f - progress);
            float y = peg.y + THROW_FROM_Y * (1f - progress) + ARC_HEIGHT * 4f * progress * (1f - progress);
            placeOrigin(shoe, x, y);
            shoe.setRotation(THROW_SPIN * (1f - Interpolation.pow2Out.apply(progress)));
            return;
        }
        if (!hooked) hook();
        // Accroché : il se balance autour du piquet, de moins en moins.
        float since = time - HOOK_TIME;
        shoe.setRotation(SWING * MathUtils.sin(since * SWING_SPEED) * (float) Math.exp(-since * SWING_DAMPING));
    }

    /** Le fer se pose sur le piquet : petit sursaut, puis il luit en palpitant devant un halo doré. */
    private void hook() {
        hooked = true;
        placeOrigin(shoe, peg.x, peg.y);
        shoe.addAction(Actions.sequence(
            Actions.scaleTo(1.15f, 0.88f, 0.05f),
            Actions.scaleTo(1f, 1f, 0.25f, Interpolation.elasticOut),
            Actions.forever(Actions.sequence(
                Actions.color(c("fff6c8"), 0.2f),
                Actions.color(Color.WHITE, 0.2f)))));
        glow.setVisible(true);
        glow.getColor().a = 0f;
        glow.addAction(Actions.sequence(
            Actions.parallel(Actions.alpha(0.9f, 0.1f), Actions.scaleTo(1.2f, 1.2f, 0.3f, Interpolation.pow2Out)),
            Actions.forever(Actions.sequence(Actions.alpha(0.5f, 0.3f), Actions.alpha(0.9f, 0.3f)))));
    }

    /** @return le fer à cheval doré, ouvert vers le haut : branches un peu évasées, trous de clous sombres. */
    private static Pixmap horseshoe() {
        Pixmap pixmap = pixmap(SHOE_SIZE, SHOE_SIZE);
        float cx = SHOE_SIZE / 2f, cy = 16f;
        Shape shape = (x, y) -> {
            float dx = x + 0.5f - cx, dy = y + 0.5f - cy;
            float outer = y < cy ? Math.abs(dx) - (y < 4 ? 15.5f : 14.5f) : (float) Math.sqrt(dx * dx + dy * dy) - 15.5f;
            float inner = y < cy ? Math.abs(dx) - 7.5f : (float) Math.sqrt(dx * dx + dy * dy) - 7.5f;
            return outer <= 0f && inner > 0f;
        };
        Color gold = Palette.GOLD, light = c("fff3a8"), shade = c("d49a1c"), dark = c("a8700c");
        fillShape(pixmap, shape, (x, y) -> {
            float dx = x + 0.5f - cx, dy = y + 0.5f - cy;
            float radius = y < cy ? Math.abs(dx) : (float) Math.sqrt(dx * dx + dy * dy);
            if (radius > 13.5f) return dx < 0 ? gold : shade;      // tranche extérieure
            if (radius < 9.5f)  return dx < 0 ? light : gold;      // tranche intérieure, qui accroche la lumière
            return dy > 6f || dx > 4f ? gold : light;
        });
        // Trous des clous, le long de l'arc.
        pixmap.setColor(dark);
        for (float angle : new float[] {195f, 232f}) {
            int hx = Math.round(cx + MathUtils.cosDeg(angle) * 11.5f - 0.5f);
            int hy = Math.round(cy - MathUtils.sinDeg(angle) * 11.5f - 0.5f);
            pixmap.drawPixel(hx, hy);
            pixmap.drawPixel(SHOE_SIZE - 1 - hx, hy);
        }
        for (int y : new int[] {5, 10}) {
            pixmap.drawPixel(4, y);
            pixmap.drawPixel(SHOE_SIZE - 5, y);
        }
        return pixmap;
    }

    /** @return le poteau de bois : veines verticales, chapeau plus clair. */
    private static Pixmap post() {
        Pixmap pixmap = pixmap(POST_WIDTH, POST_HEIGHT);
        fillOutlined(pixmap, 0, 2, POST_WIDTH, POST_HEIGHT - 2, Palette.WOOD);
        fillOutlined(pixmap, 1, 0, POST_WIDTH - 2, 4, Palette.WOOD_LIGHT);
        pixmap.setColor(Palette.WOOD_LIGHT);
        pixmap.drawLine(2, 4, 2, POST_HEIGHT - 2);
        pixmap.setColor(Palette.WOOD_DARK);
        pixmap.drawLine(POST_WIDTH - 3, 4, POST_WIDTH - 3, POST_HEIGHT - 2);
        for (int[] grain : new int[][] {{5, 8, 18}, {8, 22, 34}, {6, 38, 48}}) {
            pixmap.drawLine(grain[0], grain[1], grain[0], grain[2]);
        }
        hLine(pixmap, 1, POST_WIDTH - 2, POST_HEIGHT - 4, Palette.WOOD_DARK);
        return pixmap;
    }

    /** @return le bout du piquet : un rond de bois clair, vu de face. */
    private static Pixmap knob() {
        Pixmap pixmap = pixmap(KNOB_SIZE, KNOB_SIZE);
        fillOutlinedCircle(pixmap, KNOB_SIZE / 2, KNOB_SIZE / 2, KNOB_SIZE / 2 - 1, Palette.WOOD_LIGHT);
        pixmap.setColor(Palette.WOOD_HIGHLIGHT);
        pixmap.drawPixel(KNOB_SIZE / 2 - 1, KNOB_SIZE / 2 - 1);
        return pixmap;
    }

    /** @return un trèfle à quatre feuilles vert vif, sa petite tige en bas à droite. */
    private static Pixmap cloverLeaf() {
        Pixmap pixmap = pixmap(CLOVER_SIZE, CLOVER_SIZE);
        drawGrid(pixmap, new String[] {
            "..ooo.ooo....",
            ".oLLGoGGGo...",
            ".oLGGoGGDo...",
            ".oGGGGGGDo...",
            "ooooGGGGoooo.",
            "oLLGGoGGGGGo.",
            "oLGGGoDGGGDo.",
            ".oGGDoDGGDo..",
            ".ooooGooooo..",
            "....oGo.oo...",
            ".....ooSo....",
            "......oSo....",
            ".......o.....",
        }, 0, 0, "oGLDS", OUTLINE, c("3fbf4a"), c("8af07a"), c("23852e"), c("6a4a20"));
        return pixmap;
    }
}
