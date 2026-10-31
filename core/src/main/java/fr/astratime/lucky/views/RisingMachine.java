package fr.astratime.lucky.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Disposable;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Vue de face : la machine à sous qui sort de la table quand on tire le levier.
 * Au repos, les rouleaux sont trois cases couchées sur la table. Au tirage,
 * une machine (caisse, fronton d'ampoules, vitre des rouleaux) monte de la
 * table à leur emplacement, emporte les symboles et les fait tourner debout,
 * face au joueur ; le tirage fini, elle redescend sous la table et rend les
 * symboles à leurs cases. Le joueur et l'ennemi ont chacun la leur.
 *
 * <p>En vue classique, elle ne fait rien : les rouleaux restent sur la table.
 */
public class RisingMachine extends Billboard implements Disposable {

    /** Durée de la montée et de la descente, en secondes. */
    private static final float MOVE_TIME = 0.32f;
    /**
     * Caisse autour de la vitre des rouleaux : marges (table à plat). Sur les
     * côtés, celle du cadre des rouleaux : la machine a la largeur de son
     * emplacement et sort de son bord avant ({@link #FRAME}).
     */
    private static final float SIDE = TableView.REEL_FRAME_PAD, BOTTOM = 45f, TOP = 72f;
    /** Le bord avant du cadre des rouleaux, sous la vitre : la fente d'où sort la machine. */
    private static final float FRAME = TableView.REEL_FRAME_PAD;
    /** Un pixel des dessins de la machine, en unités de la table (comme le cadre des rouleaux). */
    private static final float PIXEL = 3f;
    /** Sommet arrondi au-dessus de la caisse, et écart entre deux ampoules du fronton (en pixels des dessins). */
    private static final float CREST_HEIGHT = 14f * PIXEL, BULB_STEP = 7f;

    private final Supplier<Rectangle> window;
    private final IntSupplier         reelCount;
    private final Rectangle           scissor = new Rectangle();
    private final Rectangle           area    = new Rectangle();
    /** Le cadre et les cases des rouleaux, les mêmes que sur la table : la machine a leur forme. */
    private final NinePatchDrawable   reelFrame, reelCell;
    private final Texture bodyTexture, crestTexture, marqueeTexture, bulbOn, bulbOff, trayTexture;
    private final NinePatch body, marquee;

    /** Les symboles emportés, et le groupe où ils retournent au repos. */
    private Actor   content;
    private Group   home;
    private int     homeIndex;
    /** 0 : sous la table, 1 : sortie. */
    private float   rise;
    private boolean up;
    private float   time;

    /**
     * @param window    vitre des rouleaux sur la table à plat (le rectangle de leurs cases)
     * @param reelCount nombre de rouleaux (une case chacun)
     * @param reelFrame cadre des rouleaux de la table (non possédé)
     * @param reelCell  case d'un rouleau de la table (non possédée)
     * @param enemy     {@code true} : la machine de l'ennemi (violette), sinon celle du joueur (rouge)
     */
    public RisingMachine(TableProjection projection, Supplier<Rectangle> window, IntSupplier reelCount,
                         NinePatchDrawable reelFrame, NinePatchDrawable reelCell, boolean enemy) {
        super(projection, 1f, () -> {
            Rectangle glass = window.get();
            return new Vector2(glass.x + glass.width / 2f, glass.y - BOTTOM);
        });
        this.window    = window;
        this.reelCount = reelCount;
        this.reelFrame = reelFrame;
        this.reelCell  = reelCell;
        String who = enemy ? "enemy" : "player";
        bodyTexture    = load("front/machine/body_" + who + ".png");
        crestTexture   = load("front/machine/crest_" + who + ".png");
        marqueeTexture = load("front/machine/marquee.png");
        bulbOn         = load("front/machine/bulb_on.png");
        bulbOff        = load("front/machine/bulb_off.png");
        trayTexture    = load("front/machine/tray.png");
        body    = new NinePatch(bodyTexture, 5, 5, 5, 5);
        marquee = new NinePatch(marqueeTexture, 4, 4, 4, 4);
    }

    private static Texture load(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return texture;
    }

    @Override
    public void dispose() {
        bodyTexture.dispose();
        crestTexture.dispose();
        marqueeTexture.dispose();
        bulbOn.dispose();
        bulbOff.dispose();
        trayTexture.dispose();
    }

    /** @return {@code true} tant que la machine est hors de la table (même en train de redescendre). */
    public boolean isOut() { return content != null; }

    /** Tirage : la machine sort de la table et emporte {@code symbols} (vue de face seulement). */
    public void raise(Actor symbols) {
        if (!projection.isEnabled()) return;
        if (content == null) {
            content   = symbols;
            home      = symbols.getParent();
            homeIndex = home == null ? 0 : symbols.getZIndex();
            addActor(symbols);
        }
        up = true;
    }

    /** Tirage fini : la machine redescend, puis rend les symboles à la table. */
    public void lower() {
        up = false;
    }

    /** Range la machine sur-le-champ (nouveau combat, changement de vue). */
    public void putAway() {
        up   = false;
        rise = 0f;
        giveBack();
    }

    private void giveBack() {
        if (content == null) return;
        if (home != null) home.addActorAt(Math.min(homeIndex, home.getChildren().size), content);
        content = null;
        home    = null;
    }

    @Override
    public void act(float delta) {
        time += delta;
        if (!projection.isEnabled() && content != null) putAway();
        rise = MathUtils.clamp(rise + (up ? delta : -delta) / MOVE_TIME, 0f, 1f);
        if (!up && rise <= 0f) giveBack();
        super.act(delta);
    }

    @Override
    protected void place() {
        lift = 0f;
        super.place();
        if (!projection.isEnabled()) return;
        // Le bas de la caisse est posé sur le bord avant du cadre des rouleaux.
        Vector2 slit = slit();
        Vector2 foot = getAnchor();
        Rectangle glass = window.get();
        setScale(projection.scaleAt(glass.x + glass.width / 2f, glass.y - FRAME)); // largeur du cadre à cet endroit
        setPosition(slit.x - foot.x, slit.y - foot.y);
        // Hauteur de la machine à l'écran : sous la table, elle est descendue d'autant.
        float height = (window.get().height + BOTTOM + TOP + CREST_HEIGHT) * getScaleY();
        lift = -(1f - Interpolation.pow2Out.apply(rise)) * height;
        setY(getY() + lift);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (content == null || !projection.isEnabled()) return;
        place();
        // Seul ce qui est sorti de la table se voit : on coupe sous la fente.
        Vector2 slit = slit();
        area.set(-1e5f, slit.y, 2e5f, 1e5f);
        batch.flush();
        ScissorStack.calculateScissors(getStage().getCamera(), batch.getTransformMatrix(), area, scissor);
        if (!ScissorStack.pushScissors(scissor)) return;
        super.draw(batch, parentAlpha);
        batch.flush();
        ScissorStack.popScissors();
    }

    /** @return la fente à l'écran : le milieu du bord avant du cadre des rouleaux. */
    private Vector2 slit() {
        Rectangle glass = window.get();
        return projection.project(new Vector2(glass.x + glass.width / 2f, glass.y - FRAME));
    }

    /**
     * La machine, sous les symboles (dans le repère de la table à plat, comme
     * eux) : la caisse, le cadre et les cases des rouleaux tels qu'on les voit
     * sur la table (3 ou 4 rouleaux), le fronton d'ampoules, le sommet arrondi
     * et le bac à pièces.
     */
    @Override
    protected void drawChildren(Batch batch, float parentAlpha) {
        Rectangle glass = window.get();
        float x0 = glass.x - SIDE, y0 = glass.y - BOTTOM;
        float w = glass.width + SIDE * 2, top = glass.y + glass.height + TOP;
        batch.setColor(1f, 1f, 1f, parentAlpha);
        // Sommet arrondi, posé sur la caisse.
        float crestWidth = crestTexture.getWidth() * PIXEL;
        batch.draw(crestTexture, x0 + (w - crestWidth) / 2f, top - PIXEL, crestWidth, CREST_HEIGHT);
        nine(batch, body, x0, y0, w, top - y0);
        // Fronton : panneau doré et ses ampoules en chenillard.
        float frameTop = glass.y + glass.height + FRAME;
        float mx = x0 + PIXEL * 3, my = frameTop + PIXEL * 2, mw = w - PIXEL * 6, mh = top - PIXEL * 3 - my;
        nine(batch, marquee, mx, my, mw, mh);
        int bulbs = Math.max(2, (int) ((mw / PIXEL - 8f) / BULB_STEP));
        float step = (mw - PIXEL * 8f) / bulbs;
        for (int row = 0; row < 2; row++) {
            float by = my + mh * (row == 0 ? 0.62f : 0.28f) - PIXEL * 2f;
            for (int i = 0; i < bulbs; i++) {
                boolean on = ((int) (time * 6f) + i + row) % 3 == 0;
                float bx = mx + PIXEL * 4f + step * (i + 0.5f) - PIXEL * 2f;
                batch.draw(on ? bulbOn : bulbOff, bx, by, PIXEL * 4f, PIXEL * 4f);
            }
        }
        // Le cadre et les cases des rouleaux, comme sur la table.
        reelFrame.draw(batch, glass.x - FRAME, glass.y - FRAME, glass.width + FRAME * 2, glass.height + FRAME * 2);
        int reels = Math.max(1, reelCount.getAsInt());
        float cell = glass.width / reels;
        for (int i = 0; i < reels; i++) reelCell.draw(batch, glass.x + i * cell, glass.y, cell, glass.height);
        // Bac à pièces, en bas.
        float trayWidth = trayTexture.getWidth() * PIXEL, trayHeight = trayTexture.getHeight() * PIXEL;
        batch.draw(trayTexture, x0 + (w - trayWidth) / 2f, y0 + (glass.y - FRAME - y0 - trayHeight) / 2f,
            trayWidth, trayHeight);
        batch.setColor(Color.WHITE);
        super.drawChildren(batch, parentAlpha);
    }

    /** Pièce étirable, ses pixels à la taille de ceux de la table. */
    private static void nine(Batch batch, NinePatch patch, float x, float y, float width, float height) {
        patch.draw(batch, x, y, 0f, 0f, width / PIXEL, height / PIXEL, PIXEL, PIXEL, 0f);
    }
}
