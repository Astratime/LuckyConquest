package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;

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
public class RisingMachine extends Billboard {

    /** Durée de la montée et de la descente, en secondes. */
    private static final float MOVE_TIME = 0.32f;
    /**
     * Caisse autour de la vitre des rouleaux : marges (table à plat). Sur les
     * côtés, celle du cadre des rouleaux : la machine a la largeur de son
     * emplacement et sort de son bord avant ({@link #FRAME}).
     */
    private static final float SIDE = TableView.REEL_FRAME_PAD, BOTTOM = 44f, TOP = 74f;
    /** Le bord avant du cadre des rouleaux, sous la vitre : la fente d'où sort la machine. */
    private static final float FRAME = TableView.REEL_FRAME_PAD;
    private static final float TRIM = 8f, LINE = 4f, BULB = 10f, BULB_GAP = 22f;

    private static final Color OUTLINE = Color.valueOf("22141aff");
    private static final Color TRIM_COLOR = Color.valueOf("e6b422ff");
    private static final Color TRIM_SHADE = Color.valueOf("a8741aff");
    private static final Color GLASS   = Color.valueOf("f3ead2ff");
    private static final Color SLOT    = Color.valueOf("140a0eff");
    private static final Color BULB_ON = Color.valueOf("fff2a8ff");
    private static final Color BULB_OFF = Color.valueOf("c9862aff");

    private final Supplier<Rectangle> window;
    private final IntSupplier         reelCount;
    private final Texture             pixel;
    private final Color               body, bodyLight, bodyDark;
    private final Rectangle           scissor = new Rectangle();
    private final Rectangle           area    = new Rectangle();

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
     * @param reelCount nombre de rouleaux (séparations de la vitre)
     * @param body      couleur de la caisse
     */
    public RisingMachine(TableProjection projection, Supplier<Rectangle> window, IntSupplier reelCount, Texture pixel,
                         Color body) {
        super(projection, 1f, () -> {
            Rectangle glass = window.get();
            return new Vector2(glass.x + glass.width / 2f, glass.y - BOTTOM);
        });
        this.window    = window;
        this.reelCount = reelCount;
        this.pixel     = pixel;
        this.body      = body;
        this.bodyLight = body.cpy().lerp(Color.WHITE, 0.25f);
        this.bodyDark  = body.cpy().lerp(Color.BLACK, 0.35f);
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
        float height = (window.get().height + BOTTOM + TOP) * getScaleY();
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

    /** La caisse, sous les symboles (dans le repère de la table à plat, comme eux). */
    @Override
    protected void drawChildren(Batch batch, float parentAlpha) {
        Rectangle glass = window.get();
        float x0 = glass.x - SIDE, y0 = glass.y - BOTTOM;
        float w = glass.width + SIDE * 2, h = glass.height + BOTTOM + TOP;
        rect(batch, OUTLINE, x0 - LINE, y0, w + LINE * 2, h + LINE, parentAlpha);
        rect(batch, bodyDark, x0, y0, w, h, parentAlpha);
        rect(batch, body, x0 + LINE, y0, w - LINE * 3, h - LINE, parentAlpha);
        rect(batch, bodyLight, x0 + LINE, y0, LINE * 2, h - LINE * 2, parentAlpha); // reflet à gauche
        // Fronton d'ampoules, qui clignotent en chenillard.
        float frontY = glass.y + glass.height + TRIM + LINE * 2;
        float frontH = y0 + h - LINE * 3 - frontY;
        rect(batch, OUTLINE, x0 + LINE * 3, frontY, w - LINE * 6, frontH, parentAlpha);
        int bulbs = Math.max(2, (int) ((w - LINE * 10) / BULB_GAP));
        float step = (w - LINE * 10) / bulbs;
        for (int i = 0; i < bulbs; i++) {
            boolean on = ((int) (time * 6f) + i) % 3 == 0;
            float bx = x0 + LINE * 5 + step * (i + 0.5f) - BULB / 2f;
            rect(batch, on ? BULB_ON : BULB_OFF, bx, frontY + frontH * 0.62f - BULB / 2f, BULB, BULB, parentAlpha);
            rect(batch, on ? BULB_OFF : BULB_ON, bx, frontY + frontH * 0.28f - BULB / 2f, BULB, BULB, parentAlpha);
        }
        // Vitre des rouleaux : cadre doré, fond crème, une séparation entre deux rouleaux.
        rect(batch, OUTLINE, glass.x - TRIM - LINE, glass.y - TRIM - LINE, glass.width + (TRIM + LINE) * 2,
            glass.height + (TRIM + LINE) * 2, parentAlpha);
        rect(batch, TRIM_SHADE, glass.x - TRIM, glass.y - TRIM, glass.width + TRIM * 2, glass.height + TRIM * 2, parentAlpha);
        rect(batch, TRIM_COLOR, glass.x - TRIM, glass.y, glass.width + TRIM * 2, glass.height + TRIM, parentAlpha);
        rect(batch, GLASS, glass.x, glass.y, glass.width, glass.height, parentAlpha);
        int reels = Math.max(1, reelCount.getAsInt());
        for (int i = 1; i < reels; i++) {
            rect(batch, OUTLINE, glass.x + glass.width * i / reels - LINE / 2f, glass.y, LINE, glass.height, parentAlpha);
        }
        // Fente à pièces, en bas.
        rect(batch, SLOT, glass.x + glass.width / 2f - 30f, y0 + BOTTOM * 0.3f, 60f, LINE * 2, parentAlpha);
        batch.setColor(Color.WHITE);
        super.drawChildren(batch, parentAlpha);
    }

    private void rect(Batch batch, Color color, float x, float y, float width, float height, float alpha) {
        batch.setColor(color.r, color.g, color.b, color.a * alpha);
        batch.draw(pixel, x, y, width, height);
    }
}
