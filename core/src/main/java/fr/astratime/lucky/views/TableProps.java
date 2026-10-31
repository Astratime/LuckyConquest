package fr.astratime.lucky.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.List;

/**
 * Vue de face : les volumes posés sur la table. Les decks et les défausses
 * sont de vraies piles, sur leur emplacement, plus ou moins hautes selon le
 * nombre de cartes ; l'échoppe est un étal (comptoir garni, marchandises,
 * poteaux, auvent rayé). Les zones cliquables restent celles des piles et de
 * l'icône de l'échoppe ({@link CardPileView#setFrontView}, {@link ShopIcon#setFrontView}).
 *
 * <p>En vue classique, rien n'est dessiné ici.
 */
public class TableProps extends Actor implements Disposable {

    /** Épaisseur d'une pile : socle, puis par carte (unités de la table), et cartes montrées au plus. */
    private static final float PILE_BASE = 2f, PILE_PER_CARD = 0.7f;
    private static final int   PILE_MAX_CARDS = 30;
    private static final float EMPTY_ALPHA = 0.25f, LABEL_GAP = 4f;

    /** L'étal (unités de la table) : demi-largeur, profondeur et hauteur du comptoir, puis l'auvent. */
    private static final float STALL_HALF = 72f, COUNTER_DEPTH = 44f, COUNTER_HEIGHT = 42f;
    private static final float POST = 7f, AWNING_FRONT = 112f, AWNING_BACK = 134f, AWNING_OVER = 10f, VALANCE = 12f;
    private static final float GOODS_HALF = 64f, GOODS_HEIGHT = 17f;
    /** L'étal est un peu rentré vers le milieu de la table, loin du rebord (voir GameScreen). */
    public static final float STALL_SHIFT = 18f;
    /** Son nom est debout devant lui. */
    private static final float NAME_SPACE = 1.6f;

    private static final Color WHITE = new Color(Color.WHITE);
    private static final Color WOOD  = Color.valueOf("8a5a30ff");
    private static final Color POST_COLOR = Color.valueOf("6a4024ff");

    private final SolidPainter       painter;
    private final List<CardPileView> piles = new ArrayList<>();
    private final ShopIcon           shop;
    private final Texture edges        = load("front/props/card_edges.png");
    private final Texture stallFront   = load("front/props/stall_front.png");
    private final Texture stallTop     = load("front/props/stall_top.png");
    private final Texture stallGoods   = load("front/props/stall_goods.png");
    private final Texture stallAwning  = load("front/props/stall_awning.png");
    private final Texture stallValance = load("front/props/stall_valance.png");
    private final TextureRegion front   = new TextureRegion(stallFront);
    private final TextureRegion top     = new TextureRegion(stallTop);
    private final TextureRegion goods   = new TextureRegion(stallGoods);
    private final TextureRegion awning  = new TextureRegion(stallAwning);
    private final TextureRegion valance = new TextureRegion(stallValance);
    private final TextureRegion edge    = new TextureRegion(edges);
    private final TextureRegion back    = new TextureRegion();
    private final Rectangle     footprint = new Rectangle();
    private final Vector2       point     = new Vector2();
    private final GlyphLayout   layout    = new GlyphLayout();

    /** @param shop l'icône de l'échoppe (sa place et son survol), ou {@code null} */
    public TableProps(SolidPainter painter, ShopIcon shop) {
        this.painter = painter;
        this.shop    = shop;
        setTouchable(Touchable.disabled);
    }

    /** Ajoute une pile, à dessiner en volume (les plus lointaines d'abord). */
    public void addPile(CardPileView pile) { piles.add(pile); }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!painter.getProjection().isEnabled()) return;
        for (CardPileView pile : piles) {
            if (pile.getStage() != null && pile.isVisible()) drawPile(batch, pile, parentAlpha);
        }
        if (shop != null && shop.getStage() != null && shop.isVisible()) drawStall(batch, parentAlpha);
        batch.setColor(Color.WHITE);
    }

    /** Une pile : la carte du dessus sur le dessus, la tranche des cartes sur les côtés, son nom au-dessus. */
    private void drawPile(Batch batch, CardPileView pile, float alpha) {
        pile.getFootprint(footprint);
        // La pile est dans le calque de la table : ses coordonnées sont celles du Stage, à plat.
        float x0 = footprint.x, y0 = footprint.y, x1 = x0 + footprint.width, y1 = y0 + footprint.height;
        back.setRegion(pile.getCardBack());
        int count = pile.getCount();
        float height = 0f;
        if (count == 0) {
            painter.face(batch, back, WHITE, 1f, alpha * EMPTY_ALPHA, x0, y0, 0f, x1, y0, 0f, x1, y1, 0f, x0, y1, 0f);
        } else {
            int shown = Math.min(count, PILE_MAX_CARDS);
            height = PILE_BASE + shown * PILE_PER_CARD;
            edge.setRegion(0, 0, edges.getWidth(), Math.min(edges.getHeight(), shown * 2));
            painter.box(batch, x0, y0, 0f, x1, y1, height, back, edge, edge, WHITE, WHITE, alpha, true);
        }
        // Le nom, debout au-dessus du bord du fond de la pile, à la taille de la table à cet endroit.
        BitmapFont font = pile.getFont();
        float scale = painter.getProjection().scaleAt(x0 + footprint.width / 2f, y1);
        float oldX = font.getData().scaleX, oldY = font.getData().scaleY;
        font.getData().setScale(oldX * scale, oldY * scale);
        layout.setText(font, pile.getLabelText());
        painter.getProjection().project(x0 + footprint.width / 2f, y1, height, point);
        font.setColor(1f, 1f, 1f, alpha);
        font.draw(batch, layout, point.x - layout.width / 2f, point.y + LABEL_GAP * scale + layout.height);
        font.getData().setScale(oldX, oldY);
        font.setColor(Color.WHITE);
    }

    /** L'étal de l'échoppe, à la place de son icône (qui grossit au survol : l'étal aussi). */
    private void drawStall(Batch batch, float alpha) {
        float k  = shop.getScaleX(); // survol
        float cx = shop.getX() + shop.getWidth() / 2f;
        float yf = shop.getY() + shop.getNameHeight() * NAME_SPACE;
        float half = STALL_HALF * k, depth = COUNTER_DEPTH * k, counter = COUNTER_HEIGHT * k, post = POST * k;
        float postTop = AWNING_FRONT * k;
        // Poteaux du fond, derrière le comptoir.
        painter.box(batch, cx - half, yf + depth - post, counter, cx - half + post, yf + depth, AWNING_BACK * k,
            POST_COLOR, alpha);
        painter.box(batch, cx + half - post, yf + depth - post, counter, cx + half, yf + depth, AWNING_BACK * k,
            POST_COLOR, alpha);
        // Le comptoir : caisse en planches, dessus garni de paniers de fruits.
        painter.box(batch, cx - half, yf, 0f, cx + half, yf + depth, counter, top, front, painter.pixel(), WHITE, WOOD,
            alpha, true);
        // Marchandises debout sur le comptoir.
        float gy = yf + depth * 0.35f, gh = GOODS_HALF * k;
        painter.face(batch, goods, WHITE, 1f, alpha, cx - gh, gy, counter, cx + gh, gy, counter,
            cx + gh, gy, counter + GOODS_HEIGHT * k, cx - gh, gy, counter + GOODS_HEIGHT * k);
        // Poteaux de devant.
        painter.box(batch, cx - half, yf, counter, cx - half + post, yf + post, postTop, POST_COLOR, alpha);
        painter.box(batch, cx + half - post, yf, counter, cx + half, yf + post, postTop, POST_COLOR, alpha);
        // L'auvent : la toile monte vers le fond, le lambrequin pend devant.
        float over = AWNING_OVER * k, ax0 = cx - half - over, ax1 = cx + half + over, ay = yf - over;
        painter.face(batch, awning, WHITE, 1f, alpha, ax0, ay, postTop, ax1, ay, postTop,
            ax1, yf + depth + over, AWNING_BACK * k, ax0, yf + depth + over, AWNING_BACK * k);
        painter.face(batch, valance, WHITE, 1f, alpha, ax0, ay, postTop - VALANCE * k, ax1, ay, postTop - VALANCE * k,
            ax1, ay, postTop, ax0, ay, postTop);
    }

    private static Texture load(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return texture;
    }

    @Override
    public void dispose() {
        edges.dispose();
        stallFront.dispose();
        stallTop.dispose();
        stallGoods.dispose();
        stallAwning.dispose();
        stallValance.dispose();
    }
}
