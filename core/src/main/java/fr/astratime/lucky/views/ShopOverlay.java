package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.controllers.GameController.ShopOffer;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.i18n.Lang;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

/**
 * L'échoppe, ouverte par-dessus le jeu depuis l'icône du marché : les cartes
 * en vente, en grille, avec leur prix (leur effet au survol, leur fiche au clic). Acheter retire le prix des
 * gains ; sans assez de gains, le prix clignote en rouge. Une carte
 * indisponible (ex : Bingo d'un symbole retiré par le Recyclage) est grisée,
 * et tenter de l'acheter explique pourquoi. Un clic à côté, le
 * bouton « Fermer » ou Échap (géré par l'appelant via {@link #hide()}) la referment.
 * Quand les cartes ne tiennent pas à l'écran, leur grille défile (molette ou
 * glisser).
 */
public class ShopOverlay implements Disposable {

    /** Teinte d'une carte qui ne peut pas être achetée en ce moment. */
    private static final Color UNAVAILABLE_TINT = new Color(0.4f, 0.4f, 0.4f, 1f);
    /** Temps d'affichage de l'avertissement d'une carte indisponible. */
    private static final float NOTICE_TIME = 3.5f;
    private static final float FADE       = 0.2f;
    private static final float PADDING    = 44f;
    private static final float GAP        = 16f;
    private static final float CARD_SCALE = 1.15f;
    private static final float CELL_WIDTH = 230f;
    private static final float COIN_SIZE  = 30f;
    private static final float CELL_PAD   = 14f;
    private static final int   COLUMNS    = 4;
    private static final float CLOSE_AFTER_PURCHASE = 0.7f;

    private final Stage       stage;
    private final HudTextures hud;
    private final Tooltip     tooltip;
    private final float       cardWidth;
    private final float       cardHeight;
    private final BitmapFont  titleFont   = Fonts.jersey(72, Palette.TEXT_TITLE, 3f, Palette.TEXT_SHADE);
    private final BitmapFont  textFont    = Fonts.jersey(30, Palette.TEXT_BODY, 2f, Palette.TEXT_SHADE);
    private final BitmapFont  hintFont    = Fonts.jersey(22, Palette.TEXT_BODY, 2f, Palette.TEXT_SHADE);
    private final BitmapFont  nameFont    = Fonts.jersey(30, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private final BitmapFont  priceFont   = Fonts.jersey(34, Color.WHITE, 2f, Palette.TEXT_SHADE);

    private final Group root  = new Group();
    private final Image veil;
    private final Table panel = new Table();
    /** Grille des cartes en vente, dans un panneau qui défile si elle dépasse de l'écran. */
    private final Table      grid   = new Table();
    private final ScrollPane scroll = new ScrollPane(grid);
    private Cell<ScrollPane> scrollCell;

    /** @param cardWidth taille d'une carte de la main (agrandie ici) */
    public ShopOverlay(Stage stage, HudTextures hud, Tooltip tooltip, float cardWidth, float cardHeight) {
        this.tooltip    = tooltip;
        this.stage      = stage;
        this.hud        = hud;
        this.cardWidth  = cardWidth * CARD_SCALE;
        this.cardHeight = cardHeight * CARD_SCALE;
        veil = new Image(new TextureRegionDrawable(new TextureRegion(hud.pixel)));
        veil.setColor(Palette.VEIL);
        veil.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { hide(); }
        });
        panel.setBackground(hud.panelDrawable());
        panel.pad(PADDING);
        panel.setTouchable(Touchable.enabled);
        scroll.setScrollingDisabled(true, false);
        scroll.setOverscroll(false, false);
        root.addActor(veil);
        root.addActor(panel);
        root.setVisible(false);
    }

    /** @return l'acteur racine de l'échoppe, à ajouter au Stage par-dessus le jeu. */
    public Group getActor() { return root; }

    /** @return {@code true} tant que l'échoppe est ouverte. */
    public boolean isShown() { return root.isVisible(); }

    /**
     * Ouvre l'échoppe.
     *
     * @param offers    cartes en vente
     * @param gains     gains actuels du joueur (relus après chaque achat)
     * @param textureOf image de chaque carte
     * @param button    crée un bouton du jeu (texte, action au clic)
     * @param unavailable pourquoi une carte ne peut pas être achetée en ce moment, ou {@code null} si elle le peut
     * @param buy       tente l'achat : {@code true} s'il a eu lieu (gains suffisants)
     * @param inspect   affiche la fiche d'une carte (clic sur son image)
     */
    public void show(List<ShopOffer> offers, IntSupplier gains, Function<Card, Texture> textureOf,
                     BiFunction<String, Runnable, Actor> button, Function<ShopOffer, String> unavailable,
                     Predicate<ShopOffer> buy, Consumer<ShopOffer> inspect) {
        int columns = Math.min(COLUMNS, Math.max(1, offers.size()));
        panel.clearChildren();
        panel.add(new Label(Lang.t("ÉCHOPPE"), new Label.LabelStyle(titleFont, Color.WHITE))).colspan(columns);
        panel.row();
        Label wallet = new Label(walletText(gains.getAsInt()), new Label.LabelStyle(textFont, Color.WHITE));
        panel.add(wallet).colspan(columns);
        panel.row();
        panel.add(new Label(Lang.t("Survole une carte pour son effet, clique dessus pour sa fiche. Les cartes achetées "
            + "disparaissent une fois jouées."), new Label.LabelStyle(hintFont, Color.WHITE))).colspan(columns);
        panel.row();
        // Avertissement (carte indisponible) : ligne réservée, vide tant qu'il n'y a rien à dire.
        Label notice = new Label(" ", new Label.LabelStyle(textFont, Palette.TEXT_ALERT));
        panel.add(notice).colspan(columns).padTop(GAP / 2f).padBottom(GAP);
        panel.row();
        grid.clearChildren();

        for (int i = 0; i < offers.size(); i++) {
            ShopOffer offer = offers.get(i);
            Card card = offer.card();
            Table cell = new Table();
            cell.setBackground(hud.insetDrawable());
            cell.pad(CELL_PAD);

            Image image = new Image(new TextureRegionDrawable(new TextureRegion(textureOf.apply(card))));
            image.addListener(new InputListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if (pointer != -1) return;
                    Vector2 pos = image.localToStageCoordinates(new Vector2(image.getWidth() + 8f, 0f));
                    tooltip.show(card.getName(), card.getDescription(), pos.x, pos.y);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    if (pointer != -1) return;
                    tooltip.hide();
                }

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    tooltip.hide();
                    inspect.accept(offer);
                    return true;
                }
            });
            if (unavailable.apply(offer) != null) image.setColor(UNAVAILABLE_TINT);
            cell.add(image).size(cardWidth, cardHeight);
            cell.row();
            cell.add(new Label(card.getName(), new Label.LabelStyle(nameFont, Palette.TEXT_TITLE))).padTop(GAP / 2f);
            cell.row();

            Table price = new Table();
            price.add(new Image(new TextureRegionDrawable(new TextureRegion(hud.coin)))).size(COIN_SIZE).padRight(6f);
            Label priceLabel = new Label(SidePanel.formatGains(offer.price()), new Label.LabelStyle(priceFont, Color.WHITE));
            price.add(priceLabel);
            cell.add(price).padTop(GAP / 4f);
            cell.row();

            Label status = new Label(" ", new Label.LabelStyle(hintFont, Color.WHITE));
            cell.add(button.apply(Lang.t("Acheter"), () -> {
                if (!root.isVisible() || !root.isTouchable()) return;
                String reason = unavailable.apply(offer);
                if (reason != null) {
                    status.setText(Lang.t("Indisponible"));
                    status.setColor(Palette.TEXT_ALERT);
                    notice.clearActions();
                    notice.setText(reason);
                    notice.getColor().a = 1f;
                    notice.addAction(Actions.sequence(Actions.delay(NOTICE_TIME), Actions.fadeOut(0.4f)));
                } else if (buy.test(offer)) {
                    wallet.setText(walletText(gains.getAsInt()));
                    status.setText(Lang.t("Acheté !"));
                    status.setColor(Palette.TEXT_TITLE);
                    root.setTouchable(Touchable.disabled);
                    root.addAction(Actions.delay(CLOSE_AFTER_PURCHASE, Actions.run(this::hide)));
                } else {
                    status.setText(Lang.t("Pas assez de gains"));
                    status.setColor(Palette.TEXT_ALERT);
                    priceLabel.clearActions();
                    priceLabel.setColor(Palette.TEXT_ALERT);
                    priceLabel.addAction(Actions.sequence(Actions.delay(0.4f), Actions.color(Color.WHITE, 0.4f)));
                }
            })).padTop(GAP / 2f);
            cell.row();
            cell.add(status);
            grid.add(cell).width(CELL_WIDTH).pad(GAP / 2f);
            if ((i + 1) % columns == 0) grid.row();
        }
        scrollCell = panel.add(scroll).colspan(columns);
        panel.row();
        panel.add(button.apply(Lang.t("Fermer"), this::hide)).colspan(columns).padTop(GAP);

        layout();
        root.clearActions();
        root.setVisible(true);
        root.setTouchable(Touchable.enabled);
        root.getColor().a = 0f;
        root.addAction(Actions.fadeIn(FADE, Interpolation.pow2Out));
        root.toFront();
        tooltip.getActor().toFront();
        scroll.setScrollY(0f);
        stage.setScrollFocus(scroll); // la molette fait défiler les cartes
    }

    private static String walletText(int gains) {
        return Lang.f("Tes gains : {0}", SidePanel.formatGains(gains));
    }

    /** Referme l'échoppe. */
    public void hide() {
        tooltip.hide();
        if (stage.getScrollFocus() == scroll) stage.setScrollFocus(null);
        root.clearActions();
        root.setVisible(false);
    }

    /** Recentre l'échoppe (après un redimensionnement). */
    public void layout() {
        float width  = stage.getViewport().getWorldWidth();
        float height = stage.getViewport().getWorldHeight();
        root.setSize(width, height);
        veil.setSize(width, height);
        if (scrollCell != null) {
            // La grille prend toute sa hauteur si elle tient à l'écran, sinon elle défile.
            grid.pack();
            scrollCell.width(grid.getPrefWidth()).height(grid.getPrefHeight());
            panel.invalidate(); // les tailles de cellule ne sont relues qu'après invalidation
            float overflow = panel.getPrefHeight() - (height - GAP * 2f);
            if (overflow > 0f) {
                scrollCell.height(Math.max(CELL_WIDTH, grid.getPrefHeight() - overflow));
                panel.invalidate();
            }
        }
        panel.pack();
        panel.setPosition((width - panel.getWidth()) / 2f, (height - panel.getHeight()) / 2f);
    }

    @Override
    public void dispose() {
        Fonts.release(titleFont);
        Fonts.release(textFont);
        Fonts.release(priceFont);
        Fonts.release(hintFont);
        Fonts.release(nameFont);
    }
}
