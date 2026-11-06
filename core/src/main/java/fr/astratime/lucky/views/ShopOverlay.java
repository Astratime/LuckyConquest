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
import fr.astratime.lucky.controllers.ReelStall;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.DoubleSupplier;
import java.util.function.Predicate;

/**
 * L'échoppe, ouverte par-dessus le jeu depuis l'icône du marché : les cartes
 * en vente, en grille, avec leur prix (leur effet au survol, leur fiche au clic). Acheter retire le prix des
 * gains ; sans assez de gains, le prix clignote en rouge. Une carte
 * indisponible (ex : Bingo d'un symbole retiré par le Recyclage) est grisée,
 * et tenter de l'acheter explique pourquoi. Un clic à côté, le
 * bouton « Fermer » ou Échap (géré par l'appelant via {@link #hide()}) la referment.
 * Quand les cartes ne tiennent pas à l'écran, leur grille défile (molette ou
 * glisser). L'onglet « Rouleaux » vend des rouleaux et en retire de la machine,
 * le temps du combat (voir {@link ReelStand}).
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
    private static final float SYMBOL_SIZE         = 96f;
    private static final float MACHINE_SYMBOL_SIZE = 64f;
    private static final int   MACHINE_COLUMNS     = 7;
    private static final Color INACTIVE_TAB = new Color(0.6f, 0.6f, 0.6f, 1f);

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
    /** Contenu de l'onglet des rouleaux, dans le même panneau qui défile. */
    private final Table      reelGrid = new Table();
    private final ScrollPane scroll = new ScrollPane(grid);
    private Cell<ScrollPane> scrollCell;
    private final BitmapFont  tabFont     = Fonts.jersey(40, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private DoubleSupplier gains;
    private BiFunction<String, Runnable, Actor> button;
    private ReelStand reels;
    private boolean reelsShown;
    private Label wallet;
    private Label hint;
    private Label notice;

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
     * Le côté rouleaux de l'échoppe : acheter un rouleau en vente ou retirer un
     * rouleau de la machine, le temps du combat (voir GameController#buyReel).
     */
    public interface ReelStand {
        /** @return les rouleaux en vente et le prix d'un achat */
        List<ReelStall.ReelOffer> offers();
        /** @return les rouleaux de la machine du joueur */
        List<Symbol> machine();
        /** @return le prix du prochain rouleau retiré */
        double removePrice();
        /** @return pourquoi {@code symbol} ne peut pas être acheté (hors manque de gains), ou {@code null} */
        String buyProblem(Symbol symbol);
        /** @return pourquoi {@code symbol} ne peut pas être retiré (hors manque de gains), ou {@code null} */
        String removeProblem(Symbol symbol);
        /** Tente l'achat. @return {@code true} s'il a eu lieu */
        boolean buy(Symbol symbol);
        /** Tente le retrait. @return {@code true} s'il a eu lieu */
        boolean remove(Symbol symbol);
        /** @return l'image du symbole */
        TextureRegion region(Symbol symbol);
    }

    /**
     * Ouvre l'échoppe, sur l'onglet des cartes.
     *
     * @param offers    cartes en vente
     * @param gains     gains actuels du joueur (relus après chaque achat)
     * @param textureOf image de chaque carte
     * @param button    crée un bouton du jeu (texte, action au clic)
     * @param unavailable pourquoi une carte ne peut pas être achetée en ce moment, ou {@code null} si elle le peut
     * @param buy       tente l'achat : {@code true} s'il a eu lieu (gains suffisants)
     * @param inspect   affiche la fiche d'une carte (clic sur son image)
     * @param reels     le côté rouleaux (onglet « Rouleaux »)
     */
    public void show(List<ShopOffer> offers, DoubleSupplier gains, Function<Card, Texture> textureOf,
                     BiFunction<String, Runnable, Actor> button, Function<ShopOffer, String> unavailable,
                     Predicate<ShopOffer> buy, Consumer<ShopOffer> inspect, ReelStand reels) {
        this.gains  = gains;
        this.button = button;
        this.reels  = reels;
        int columns = Math.min(COLUMNS, Math.max(1, offers.size()));
        panel.clearChildren();
        panel.add(new Label(Lang.t("ÉCHOPPE"), new Label.LabelStyle(titleFont, Color.WHITE)));
        panel.row();
        wallet = new Label(walletText(gains.getAsDouble()), new Label.LabelStyle(textFont, Color.WHITE));
        panel.add(wallet);
        panel.row();
        Table tabs = new Table();
        Label cardsTab = tab(Lang.t("Cartes"));
        Label reelsTab = tab(Lang.t("Rouleaux"));
        tabs.add(cardsTab).padRight(GAP * 3f);
        tabs.add(reelsTab);
        panel.add(tabs).padTop(GAP / 2f);
        panel.row();
        hint = new Label(" ", new Label.LabelStyle(hintFont, Color.WHITE));
        hint.setAlignment(Align.center);
        panel.add(hint);
        panel.row();
        // Avertissement (carte indisponible) : ligne réservée, vide tant qu'il n'y a rien à dire.
        notice = new Label(" ", new Label.LabelStyle(textFont, Color.WHITE)); // teinte posée à chaque message
        panel.add(notice).padTop(GAP / 2f).padBottom(GAP);
        panel.row();
        grid.clearChildren();

        for (int i = 0; i < offers.size(); i++) {
            ShopOffer offer = offers.get(i);
            Card card = offer.card();
            Table cell = new Table();
            cell.setBackground(hud.insetDrawable());
            cell.pad(CELL_PAD);
            cell.top(); // les blocs d'une rangée ont la même hauteur, contenu en haut

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
            Label name = new Label(card.getName(), new Label.LabelStyle(nameFont, Palette.TEXT_TITLE));
            name.setWrap(true); // un nom trop long passe à la ligne au lieu de sortir de son bloc
            name.setAlignment(Align.center);
            cell.add(name).width(CELL_WIDTH - GAP).padTop(GAP / 2f);
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
                    warn(reason);
                } else if (buy.test(offer)) {
                    wallet.setText(walletText(gains.getAsDouble()));
                    status.setText(Lang.t("Acheté !"));
                    status.setColor(Palette.TEXT_TITLE);
                    root.setTouchable(Touchable.disabled);
                    root.addAction(Actions.delay(CLOSE_AFTER_PURCHASE, Actions.run(this::hide)));
                } else {
                    tooExpensive(status, priceLabel);
                }
            })).padTop(GAP / 2f);
            cell.row();
            cell.add(status);
            grid.add(cell).width(CELL_WIDTH).pad(GAP / 2f).fillY();
            if ((i + 1) % columns == 0) grid.row();
        }
        scrollCell = panel.add(scroll);
        panel.row();
        panel.add(button.apply(Lang.t("Fermer"), this::hide)).padTop(GAP);

        cardsTab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { selectTab(false, cardsTab, reelsTab); }
        });
        reelsTab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { selectTab(true, cardsTab, reelsTab); }
        });
        selectTab(false, cardsTab, reelsTab);

        root.clearActions();
        root.setVisible(true);
        root.setTouchable(Touchable.enabled);
        root.getColor().a = 0f;
        root.addAction(Actions.fadeIn(FADE, Interpolation.pow2Out));
        root.toFront();
        tooltip.getActor().toFront();
        stage.setScrollFocus(scroll); // la molette fait défiler les cartes
    }

    /** @return un onglet de l'échoppe, cliquable */
    private Label tab(String text) {
        Label label = new Label(text, new Label.LabelStyle(tabFont, Color.WHITE));
        label.setTouchable(Touchable.enabled);
        return label;
    }

    /** Affiche l'onglet des cartes ou celui des rouleaux. */
    private void selectTab(boolean showReels, Label cardsTab, Label reelsTab) {
        reelsShown = showReels && reels != null;
        cardsTab.setColor(reelsShown ? INACTIVE_TAB : Palette.TEXT_TITLE);
        reelsTab.setColor(reelsShown ? Palette.TEXT_TITLE : INACTIVE_TAB);
        reelsTab.setVisible(reels != null);
        hint.setText(reelsShown
            ? Lang.f("Achète ou retire des rouleaux pour ce combat seulement : ta machine garde de {0} à {1} rouleaux. "
                + "Le prix double à chaque fois.", ReelStall.MIN_REELS, ReelStall.MAX_REELS)
            : Lang.t("Survole une carte pour son effet, clique dessus pour sa fiche. Les cartes achetées "
                + "disparaissent une fois jouées.") + " " + Lang.t("Le prix d'une carte double à chaque achat dans un combat."));
        notice.clearActions();
        notice.setText(" ");
        if (reelsShown) buildReels();
        scroll.setActor(reelsShown ? reelGrid : grid);
        layout();
        scroll.setScrollY(0f);
    }

    /** Remplit l'onglet des rouleaux : ceux en vente, puis ceux de la machine (à sélectionner pour les retirer). */
    private void buildReels() {
        reelGrid.clearChildren();
        List<ReelStall.ReelOffer> offers = reels.offers();
        reelGrid.add(new Label(Lang.t("Rouleaux en vente"), new Label.LabelStyle(nameFont, Palette.TEXT_TITLE)));
        reelGrid.row();
        Table sale = new Table();
        if (offers.isEmpty()) {
            sale.add(new Label(Lang.t("Plus aucun rouleau en vente dans ce combat"), new Label.LabelStyle(hintFont, Color.WHITE)));
        }
        for (ReelStall.ReelOffer offer : offers) {
            Table cell = new Table();
            cell.setBackground(hud.insetDrawable());
            cell.pad(CELL_PAD);
            cell.top();
            cell.add(symbolImage(offer.symbol())).size(SYMBOL_SIZE);
            cell.row();
            Label name = new Label(offer.symbol().getDisplayName(), new Label.LabelStyle(nameFont, Palette.TEXT_TITLE));
            name.setWrap(true);
            name.setAlignment(Align.center);
            cell.add(name).width(CELL_WIDTH - GAP).padTop(GAP / 2f);
            cell.row();
            Label priceLabel = priceRow(cell, offer.price());
            Label status = new Label(" ", new Label.LabelStyle(hintFont, Color.WHITE));
            cell.add(button.apply(Lang.t("Acheter"), () -> {
                if (!root.isVisible() || !root.isTouchable()) return;
                String reason = reels.buyProblem(offer.symbol());
                if (reason != null) {
                    status.setText(Lang.t("Indisponible"));
                    status.setColor(Palette.TEXT_ALERT);
                    warn(reason);
                } else if (reels.buy(offer.symbol())) {
                    afterReelChange(Lang.f("{0} rejoint ta machine pour ce combat", offer.symbol().getDisplayName()));
                } else {
                    tooExpensive(status, priceLabel);
                }
            })).padTop(GAP / 2f);
            cell.row();
            cell.add(status);
            sale.add(cell).width(CELL_WIDTH).pad(GAP / 2f).fillY();
        }
        reelGrid.add(sale).padBottom(GAP);
        reelGrid.row();

        List<Symbol> machine = reels.machine();
        reelGrid.add(new Label(Lang.f("Ta machine : {0} rouleaux (au moins {1})", machine.size(), ReelStall.MIN_REELS),
            new Label.LabelStyle(nameFont, Palette.TEXT_TITLE)));
        reelGrid.row();
        Table owned = new Table();
        List<Table> slots = new ArrayList<>();
        Symbol[] chosen = {null};
        Label choice = new Label(Lang.t("Choisis un rouleau"), new Label.LabelStyle(hintFont, Color.WHITE));
        choice.setWrap(true);
        choice.setAlignment(Align.center);
        for (int i = 0; i < machine.size(); i++) {
            Symbol symbol = machine.get(i);
            Table slot = new Table();
            slot.pad(6f);
            slot.add(symbolImage(symbol)).size(MACHINE_SYMBOL_SIZE);
            slot.setTouchable(Touchable.enabled);
            slot.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    chosen[0] = symbol;
                    for (Table other : slots) other.setBackground((com.badlogic.gdx.scenes.scene2d.utils.Drawable) null);
                    slot.setBackground(hud.insetDrawable());
                    choice.setText(symbol.getDisplayName());
                }
            });
            slots.add(slot);
            owned.add(slot).pad(4f);
            if ((i + 1) % MACHINE_COLUMNS == 0) owned.row();
        }
        // À droite des rouleaux : celui choisi, le prix du retrait et le bouton.
        Table remove = new Table();
        remove.setBackground(hud.insetDrawable());
        remove.pad(CELL_PAD);
        remove.add(choice).width(CELL_WIDTH - GAP * 2f);
        remove.row();
        Label priceLabel = priceRow(remove, reels.removePrice());
        Label status = new Label(" ", new Label.LabelStyle(hintFont, Color.WHITE));
        remove.add(button.apply(Lang.t("Retirer"), () -> {
            if (!root.isVisible() || !root.isTouchable()) return;
            if (chosen[0] == null) {
                warn(Lang.t("Choisis d'abord un rouleau de ta machine"));
                return;
            }
            String reason = reels.removeProblem(chosen[0]);
            if (reason != null) {
                warn(reason);
            } else if (reels.remove(chosen[0])) {
                afterReelChange(Lang.f("{0} quitte ta machine pour ce combat", chosen[0].getDisplayName()));
            } else {
                tooExpensive(status, priceLabel);
            }
        })).padTop(GAP / 2f);
        remove.row();
        remove.add(status);
        Table machineRow = new Table();
        machineRow.add(owned);
        machineRow.add(remove).width(CELL_WIDTH).padLeft(GAP * 2f);
        reelGrid.add(machineRow);
    }

    /** Un rouleau acheté ou retiré : les gains et l'onglet sont mis à jour, le message s'affiche. */
    private void afterReelChange(String message) {
        tooltip.hide();
        buildReels();
        wallet.setText(walletText(gains.getAsDouble()));
        layout();
        notice.clearActions();
        notice.setColor(Palette.TEXT_TITLE);
        notice.setText(message);
        notice.getColor().a = 1f;
        notice.addAction(Actions.sequence(Actions.delay(NOTICE_TIME), Actions.fadeOut(0.4f)));
    }

    /** Ajoute à {@code cell} la ligne du prix (pièce + montant). @return le montant, qui clignote s'il est trop cher */
    private Label priceRow(Table cell, double amount) {
        Table price = new Table();
        price.add(new Image(new TextureRegionDrawable(new TextureRegion(hud.coin)))).size(COIN_SIZE).padRight(6f);
        Label priceLabel = new Label(SidePanel.formatGains(amount), new Label.LabelStyle(priceFont, Color.WHITE));
        price.add(priceLabel);
        cell.add(price).padTop(GAP / 4f);
        cell.row();
        return priceLabel;
    }

    /** @return l'image d'un rouleau, avec son effet au survol */
    private Image symbolImage(Symbol symbol) {
        Image image = new Image(new TextureRegionDrawable(reels.region(symbol)));
        image.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer != -1) return;
                Vector2 pos = image.localToStageCoordinates(new Vector2(image.getWidth() + 8f, 0f));
                tooltip.show(symbol.getDisplayName(), symbol.getDescription(), pos.x, pos.y);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer != -1) return;
                tooltip.hide();
            }
        });
        return image;
    }

    /** Affiche {@code reason} en rouge quelques secondes. */
    private void warn(String reason) {
        notice.clearActions();
        notice.setColor(Palette.TEXT_ALERT);
        notice.setText(reason);
        notice.getColor().a = 1f;
        notice.addAction(Actions.sequence(Actions.delay(NOTICE_TIME), Actions.fadeOut(0.4f)));
    }

    /** Pas assez de gains : le prix clignote en rouge. */
    private static void tooExpensive(Label status, Label priceLabel) {
        status.setText(Lang.t("Pas assez de gains"));
        status.setColor(Palette.TEXT_ALERT);
        priceLabel.clearActions();
        priceLabel.setColor(Palette.TEXT_ALERT);
        priceLabel.addAction(Actions.sequence(Actions.delay(0.4f), Actions.color(Color.WHITE, 0.4f)));
    }

    private static String walletText(double gains) {
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
            Table content = reelsShown ? reelGrid : grid;
            content.pack();
            scrollCell.width(content.getPrefWidth()).height(content.getPrefHeight());
            panel.invalidate(); // les tailles de cellule ne sont relues qu'après invalidation
            float overflow = panel.getPrefHeight() - (height - GAP * 2f);
            if (overflow > 0f) {
                scrollCell.height(Math.max(CELL_WIDTH, content.getPrefHeight() - overflow));
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
        Fonts.release(tabFont);
    }
}
