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
 * une machine en volume (caisse, fronton d'ampoules, vitre des rouleaux) monte
 * de leur emplacement, exactement par le cadre des rouleaux sur la table,
 * emporte les symboles et les fait tourner debout, face au joueur ; le tirage
 * fini, elle redescend sous la table et rend les symboles à leurs cases. Le
 * joueur et l'ennemi ont chacun la leur.
 *
 * <p>En vue classique, elle ne fait rien : les rouleaux restent sur la table.
 */
public class RisingMachine extends Billboard {

    /** Durée de la montée et de la descente, en secondes. */
    private static final float MOVE_TIME = 0.32f;
    /**
     * Caisse autour de la vitre des rouleaux : marges sur les côtés (celles du
     * cadre des rouleaux sur la table : la machine sort de ce cadre), sous la
     * vitre et au-dessus (fronton). Sa profondeur est celle du cadre.
     */
    private static final float SIDE = TableView.REEL_FRAME_PAD, BOTTOM = 44f, TOP = 74f;
    private static final float TRIM = 8f, LINE = 4f, BULB = 10f, BULB_GAP = 22f;
    /** Le dessus : rebord doré, puis le gyrophare (socle et lampe), en unités de la table. */
    private static final float RIM = 8f, TOPPER = 30f, TOPPER_HEIGHT = 16f, LAMP = 13f;

    private static final Color OUTLINE = Color.valueOf("22141aff");
    private static final Color TRIM_COLOR = Color.valueOf("e6b422ff");
    private static final Color TRIM_SHADE = Color.valueOf("a8741aff");
    private static final Color GLASS   = Color.valueOf("f3ead2ff");
    private static final Color SLOT    = Color.valueOf("140a0eff");
    private static final Color BULB_ON = Color.valueOf("fff2a8ff");
    private static final Color BULB_OFF = Color.valueOf("c9862aff");

    private final SolidPainter        painter;
    private final Supplier<Rectangle> window;
    private final IntSupplier         reelCount;
    private final Texture             pixel;
    private final Color               body, bodyLight, bodyDark;
    private final Rectangle           scissor = new Rectangle();
    private final Rectangle           area    = new Rectangle();
    private final Vector2             point   = new Vector2();
    /** Hauteur du bas de la caisse par rapport au feutre (négative tant qu'elle n'est pas toute sortie). */
    private float                     bottom;

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
    public RisingMachine(SolidPainter painter, Supplier<Rectangle> window, IntSupplier reelCount, Texture pixel,
                         Color body) {
        super(painter.getProjection(), 1f, () -> {
            Rectangle glass = window.get();
            return new Vector2(glass.x + glass.width / 2f, glass.y - SIDE);
        });
        this.painter   = painter;
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

    /**
     * Pose la face avant : le dessin de la caisse (coordonnées de la table à
     * plat, la vitre sur les cases des rouleaux) est redressé contre le bord
     * avant du cadre, à la hauteur où la machine est sortie.
     */
    @Override
    protected void place() {
        if (!projection.isEnabled()) {
            super.place();
            return;
        }
        Rectangle glass = window.get();
        float x0 = glass.x - SIDE, y0 = glass.y - SIDE, cabinet = glass.y - BOTTOM;
        bottom = -(1f - Interpolation.pow2Out.apply(rise)) * height(glass);
        float scale = projection.scaleAt(x0, y0);
        projection.project(x0, y0, bottom, point);
        setOrigin(x0, cabinet);
        setScale(scale);
        setPosition(point.x - x0, point.y - cabinet);
    }

    private static float height(Rectangle glass) { return BOTTOM + glass.height + TOP; }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (content == null || !projection.isEnabled()) return;
        place();
        Rectangle glass = window.get();
        float x0 = glass.x - SIDE, x1 = glass.x + glass.width + SIDE;
        float y0 = glass.y - SIDE, y1 = glass.y + glass.height + SIDE;
        // Le dessus et le côté de la caisse (ce qui est sorti de la table) ; l'avant vient ensuite.
        float top = bottom + height(glass);
        painter.box(batch, x0, y0, bottom, x1, y1, top, painter.pixel(), painter.pixel(),
            painter.pixel(), TRIM_SHADE, body, parentAlpha, false);
        if (top > 0f) {
            // Dessus : un rebord doré autour d'un capot sombre.
            painter.face(batch, painter.pixel(), bodyDark, 1f, parentAlpha, x0 + RIM, y0 + RIM, top, x1 - RIM, y0 + RIM, top,
                x1 - RIM, y1 - RIM, top, x0 + RIM, y1 - RIM, top);
        }
        // Le gyrophare sur le capot, au fond : il sort de la table le premier.
        float cx = (x0 + x1) / 2f;
        painter.box(batch, cx - TOPPER, y1 - RIM - TOPPER, top, cx + TOPPER, y1 - RIM, top + TOPPER_HEIGHT,
            TRIM_COLOR, parentAlpha);
        boolean lit = ((int) (time * 4f)) % 2 == 0;
        painter.box(batch, cx - LAMP, y1 - RIM - TOPPER / 2f - LAMP, top + TOPPER_HEIGHT, cx + LAMP,
            y1 - RIM - TOPPER / 2f + LAMP, top + TOPPER_HEIGHT + LAMP * 2f, lit ? BULB_ON : BULB_OFF, parentAlpha);
        batch.setColor(Color.WHITE);
        // Seul ce qui est sorti de la table se voit : on coupe l'avant sous le bord du cadre.
        projection.project(x0, y0, 0f, point);
        area.set(-1e5f, point.y, 2e5f, 1e5f);
        batch.flush();
        ScissorStack.calculateScissors(getStage().getCamera(), batch.getTransformMatrix(), area, scissor);
        if (!ScissorStack.pushScissors(scissor)) return;
        super.draw(batch, parentAlpha);
        batch.flush();
        ScissorStack.popScissors();
    }

    /** La caisse, sous les symboles (dans le repère de la table à plat, comme eux). */
    @Override
    protected void drawChildren(Batch batch, float parentAlpha) {
        Rectangle glass = window.get();
        float x0 = glass.x - SIDE, y0 = glass.y - BOTTOM;
        float w = glass.width + SIDE * 2, h = glass.height + BOTTOM + TOP;
        rect(batch, OUTLINE, x0, y0, w, h, parentAlpha);
        rect(batch, bodyDark, x0 + LINE, y0, w - LINE * 2, h - LINE, parentAlpha);
        rect(batch, body, x0 + LINE, y0, w - LINE * 3, h - LINE * 2, parentAlpha);
        rect(batch, bodyLight, x0 + LINE, y0, LINE * 2, h - LINE * 3, parentAlpha); // reflet à gauche
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
