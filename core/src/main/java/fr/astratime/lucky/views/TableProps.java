package fr.astratime.lucky.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.List;

/**
 * Vue de face : ce qui se dresse sur la table. L'échoppe est un étal en
 * relief (comptoir garni, marchandises, poteaux, auvent rayé, gros contours
 * noirs comme le reste du jeu) ; les decks et les défausses restent couchés sur
 * leur emplacement, mais leur nom est écrit debout, en grand. Les zones
 * cliquables restent celles des piles et de l'icône de l'échoppe
 * ({@link CardPileView#setFrontView}, {@link ShopIcon#setFrontView}).
 *
 * <p>En vue classique, rien n'est dessiné ici.
 */
public class TableProps extends Actor implements Disposable {

    /** Épaisseur des contours noirs de l'étal, en pixels de l'écran. */
    private static final float STALL_OUTLINE = 3f;
    /**
     * Pancarte des piles, plantée derrière chacune (plus loin sur la table) :
     * taille d'un pixel de la planche, marges autour du texte et hauteur des
     * pieds (en pixels de la planche), largeur d'un pied.
     */
    private static final float SIGN_PIXEL = 2f, SIGN_PAD_X = 4f, SIGN_PAD_Y = 3f, SIGN_LEGS = 5f, SIGN_LEG = 2f;
    /** Taille du texte de la pancarte (au moins celle du bord du joueur), et recul derrière la pile. */
    private static final float SIGN_TEXT = 1.2f, SIGN_MIN_SCALE = 0.9f, SIGN_BACK = 10f;
    private static final Color SIGN_LEG_COLOR = Color.valueOf("5c3820ff");
    private static final Color OUTLINE = Color.valueOf("22141aff");
    /** Le nom de l'échoppe, peint en bas du devant du comptoir (hauteur du bas des lettres, place qu'il prend). */
    private static final float SHOP_NAME_Z = 5f, SHOP_NAME_HEIGHT = 22f;

    /** L'étal (unités de la table) : demi-largeur, profondeur et hauteur du comptoir, puis l'auvent. */
    private static final float STALL_HALF = 72f, COUNTER_DEPTH = 44f, COUNTER_HEIGHT = 42f;
    private static final float POST = 7f, AWNING_FRONT = 112f, AWNING_BACK = 134f, AWNING_OVER = 10f, VALANCE = 12f;
    private static final float GOODS_HALF = 64f, GOODS_HEIGHT = 17f;
    /** L'étal est un peu rentré vers le milieu de la table, loin du rebord (voir GameScreen). */
    public static final float STALL_SHIFT = 18f;

    private static final Color WHITE = new Color(Color.WHITE);
    private static final Color WOOD  = Color.valueOf("8a5a30ff");
    private static final Color POST_COLOR = Color.valueOf("6a4024ff");

    private final SolidPainter       painter;
    private final List<CardPileView> piles = new ArrayList<>();
    private final ShopIcon           shop;
    private final Texture stallFront   = load("front/props/stall_front.png");
    private final Texture stallTop     = load("front/props/stall_top.png");
    private final Texture stallGoods   = load("front/props/stall_goods.png");
    private final Texture stallAwning  = load("front/props/stall_awning.png");
    private final Texture stallValance = load("front/props/stall_valance.png");
    private final Texture signBoard    = load("front/props/sign_board.png");
    private final NinePatch board      = new NinePatch(signBoard, 3, 3, 3, 3);
    private final TextureRegion front   = new TextureRegion(stallFront);
    private final TextureRegion top     = new TextureRegion(stallTop);
    private final TextureRegion goods   = new TextureRegion(stallGoods);
    private final TextureRegion awning  = new TextureRegion(stallAwning);
    private final TextureRegion valance = new TextureRegion(stallValance);
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
        // Du fond vers le joueur : les pancartes derrière l'échoppe, l'échoppe, puis celles de devant.
        boolean stallDrawn = shop == null || shop.getStage() == null || !shop.isVisible();
        for (CardPileView pile : piles) {
            if (!stallDrawn && pile.getY() < shop.getY()) {
                drawStall(batch, parentAlpha);
                stallDrawn = true;
            }
            if (pile.getStage() != null && pile.isVisible()) drawPileSign(batch, pile, parentAlpha);
        }
        if (!stallDrawn) drawStall(batch, parentAlpha);
        batch.setColor(Color.WHITE);
    }

    /**
     * La pancarte d'une pile, plantée derrière elle : une planche à la largeur
     * de son nom et de son nombre de cartes, sur deux pieds.
     */
    private void drawPileSign(Batch batch, CardPileView pile, float alpha) {
        TableProjection projection = painter.getProjection();
        float footY = pile.getLabelBottomY() + SIGN_BACK;
        float scale = Math.max(SIGN_MIN_SCALE, projection.scaleAt(pile.getLabelCenterX(), footY)) * SIGN_TEXT;
        projection.project(pile.getLabelCenterX(), footY, 0f, point);
        BitmapFont font = pile.getFont();
        float oldX = font.getData().scaleX, oldY = font.getData().scaleY;
        font.getData().setScale(oldX * scale, oldY * scale);
        layout.setText(font, pile.getLabelText());
        float px = SIGN_PIXEL * scale;
        float width  = layout.width + SIGN_PAD_X * 2f * px;
        float height = layout.height + SIGN_PAD_Y * 2f * px;
        float left = point.x - width / 2f, bottom = point.y + SIGN_LEGS * px;
        // Les pieds, puis la planche.
        for (float x : new float[] {left + width * 0.22f, left + width * 0.78f - SIGN_LEG * px}) {
            rect(batch, OUTLINE, x - px / 2f, point.y, (SIGN_LEG + 1f) * px, SIGN_LEGS * px + px, alpha);
            rect(batch, SIGN_LEG_COLOR, x, point.y + px / 2f, SIGN_LEG * px, SIGN_LEGS * px, alpha);
        }
        batch.setColor(1f, 1f, 1f, alpha);
        board.draw(batch, left, bottom, 0f, 0f, width / px, height / px, px, px, 0f);
        font.setColor(1f, 1f, 1f, alpha);
        font.draw(batch, layout, point.x - layout.width / 2f, bottom + (height + layout.height) / 2f);
        font.getData().setScale(oldX, oldY);
        font.setColor(Color.WHITE);
        batch.setColor(Color.WHITE);
    }

    private void rect(Batch batch, Color color, float x, float y, float width, float height, float alpha) {
        batch.setColor(color.r, color.g, color.b, color.a * alpha);
        batch.draw(painter.pixel(), x, y, width, height);
    }

    /** L'étal de l'échoppe, à la place de son icône (qui grossit au survol : l'étal aussi). */
    private void drawStall(Batch batch, float alpha) {
        float k  = shop.getScaleX(); // survol
        float cx = shop.getX() + shop.getWidth() / 2f;
        float yf = shop.getY();
        painter.setOutline(STALL_OUTLINE);
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
        painter.setOutline(0f); // image détourée : son contour est dans l'image
        painter.face(batch, goods, WHITE, 1f, alpha, cx - gh, gy, counter, cx + gh, gy, counter,
            cx + gh, gy, counter + GOODS_HEIGHT * k, cx - gh, gy, counter + GOODS_HEIGHT * k);
        drawShopName(batch, cx, yf, k, alpha);
        painter.setOutline(STALL_OUTLINE);
        // Poteaux de devant.
        painter.box(batch, cx - half, yf, counter, cx - half + post, yf + post, postTop, POST_COLOR, alpha);
        painter.box(batch, cx + half - post, yf, counter, cx + half, yf + post, postTop, POST_COLOR, alpha);
        // L'auvent : la toile monte vers le fond, le lambrequin pend devant.
        float over = AWNING_OVER * k, ax0 = cx - half - over, ax1 = cx + half + over, ay = yf - over;
        painter.face(batch, awning, WHITE, 1f, alpha, ax0, ay, postTop, ax1, ay, postTop,
            ax1, yf + depth + over, AWNING_BACK * k, ax0, yf + depth + over, AWNING_BACK * k);
        painter.setOutline(0f);
        painter.face(batch, valance, WHITE, 1f, alpha, ax0, ay, postTop - VALANCE * k, ax1, ay, postTop - VALANCE * k,
            ax1, ay, postTop, ax0, ay, postTop);
    }

    /** « ÉCHOPPE », peint en bas du devant du comptoir (à la taille de la table, réduit s'il est trop large). */
    private void drawShopName(Batch batch, float cx, float yf, float k, float alpha) {
        BitmapFont font = shop.getNameFont();
        float scale = painter.getProjection().scaleAt(cx, yf) * k;
        float oldX = font.getData().scaleX, oldY = font.getData().scaleY;
        layout.setText(font, shop.getTitle());
        float fit = Math.min(1f, Math.min(SHOP_NAME_HEIGHT / layout.height, (STALL_HALF * 2f - 12f) / layout.width));
        font.getData().setScale(oldX * scale * fit, oldY * scale * fit);
        layout.setText(font, shop.getTitle());
        painter.getProjection().project(cx, yf, (SHOP_NAME_Z + SHOP_NAME_HEIGHT / 2f) * k, point);
        font.setColor(1f, 1f, 1f, alpha);
        font.draw(batch, layout, point.x - layout.width / 2f, point.y + layout.height / 2f);
        font.getData().setScale(oldX, oldY);
        font.setColor(Color.WHITE);
    }

    private static Texture load(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return texture;
    }

    @Override
    public void dispose() {
        stallFront.dispose();
        stallTop.dispose();
        stallGoods.dispose();
        stallAwning.dispose();
        stallValance.dispose();
        signBoard.dispose();
    }
}
