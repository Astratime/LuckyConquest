package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.controllers.GameController.ShopOffer;
import fr.astratime.lucky.entities.Card;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

/**
 * L'échoppe, ouverte par-dessus le jeu depuis l'icône du marché : les cartes
 * en vente, avec leur description et leur prix. Acheter retire le prix des
 * gains ; sans assez de gains, le prix clignote en rouge. Un clic à côté, le
 * bouton « Fermer » ou Échap (géré par l'appelant via {@link #hide()}) la referment.
 */
public class ShopOverlay implements Disposable {

    private static final Color VEIL       = new Color(0f, 0f, 0f, 0.72f);
    private static final Color GOLD       = Color.valueOf("ffd454ff");
    private static final Color CREAM      = Color.valueOf("f0e0b0ff");
    private static final Color RED        = Color.valueOf("ff5a5aff");
    private static final Color TEXT_SHADE = Color.valueOf("1a0f0fff");
    private static final float FADE       = 0.2f;
    private static final float PADDING    = 44f;
    private static final float GAP        = 16f;
    private static final float CARD_SCALE = 2.2f;
    private static final float TEXT_WIDTH = 360f;
    private static final float COIN_SIZE  = 40f;
    private static final float CLOSE_AFTER_PURCHASE = 0.7f;

    private final Stage       stage;
    private final HudTextures hud;
    private final float       cardWidth;
    private final float       cardHeight;
    private final BitmapFont  titleFont   = Fonts.jersey(72, GOLD, 3f, TEXT_SHADE);
    private final BitmapFont  textFont    = Fonts.jersey(30, CREAM, 2f, TEXT_SHADE);
    private final BitmapFont  priceFont   = Fonts.jersey(44, Color.WHITE, 3f, TEXT_SHADE);

    private final Group root  = new Group();
    private final Image veil;
    private final Table panel = new Table();

    /** @param cardWidth taille d'une carte de la main (agrandie ici) */
    public ShopOverlay(Stage stage, HudTextures hud, float cardWidth, float cardHeight) {
        this.stage      = stage;
        this.hud        = hud;
        this.cardWidth  = cardWidth * CARD_SCALE;
        this.cardHeight = cardHeight * CARD_SCALE;
        veil = new Image(new TextureRegionDrawable(new TextureRegion(hud.pixel)));
        veil.setColor(VEIL);
        veil.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { hide(); }
        });
        panel.setBackground(hud.panelDrawable());
        panel.pad(PADDING);
        panel.setTouchable(Touchable.enabled);
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
     * @param buy       tente l'achat : {@code true} s'il a eu lieu (gains suffisants)
     */
    public void show(List<ShopOffer> offers, IntSupplier gains, Function<Card, Texture> textureOf,
                     BiFunction<String, Runnable, Actor> button, Predicate<ShopOffer> buy) {
        panel.clearChildren();
        panel.add(new Label("ÉCHOPPE", new Label.LabelStyle(titleFont, Color.WHITE))).colspan(offers.size());
        panel.row();
        Label wallet = new Label(walletText(gains.getAsInt()), new Label.LabelStyle(textFont, Color.WHITE));
        panel.add(wallet).colspan(offers.size()).padBottom(GAP * 2);
        panel.row();

        for (ShopOffer offer : offers) {
            Card card = offer.card();
            Table column = new Table();
            column.add(new Image(new TextureRegionDrawable(new TextureRegion(textureOf.apply(card)))))
                .size(cardWidth, cardHeight);
            column.row();
            column.add(new Label(card.getName(), new Label.LabelStyle(priceFont, GOLD))).padTop(GAP);
            column.row();
            Label description = new Label(card.getDescription(), new Label.LabelStyle(textFont, Color.WHITE));
            description.setWrap(true);
            description.setAlignment(Align.center);
            column.add(description).width(TEXT_WIDTH).padTop(GAP / 2f);
            column.row();

            Table price = new Table();
            price.add(new Image(new TextureRegionDrawable(new TextureRegion(hud.coin)))).size(COIN_SIZE).padRight(8f);
            Label priceLabel = new Label(SidePanel.formatGains(offer.price()), new Label.LabelStyle(priceFont, Color.WHITE));
            price.add(priceLabel);
            column.add(price).padTop(GAP);
            column.row();

            Label status = new Label(" ", new Label.LabelStyle(textFont, Color.WHITE));
            column.add(button.apply("Acheter", () -> {
                if (!root.isVisible() || !root.isTouchable()) return;
                if (buy.test(offer)) {
                    wallet.setText(walletText(gains.getAsInt()));
                    status.setText("Acheté !");
                    status.setColor(GOLD);
                    root.setTouchable(Touchable.disabled);
                    root.addAction(Actions.delay(CLOSE_AFTER_PURCHASE, Actions.run(this::hide)));
                } else {
                    status.setText("Pas assez de gains");
                    status.setColor(RED);
                    priceLabel.clearActions();
                    priceLabel.setColor(RED);
                    priceLabel.addAction(Actions.sequence(Actions.delay(0.4f), Actions.color(Color.WHITE, 0.4f)));
                }
            })).padTop(GAP);
            column.row();
            column.add(status).padTop(GAP / 2f);
            panel.add(column).top().pad(0f, GAP, 0f, GAP);
        }
        panel.row();
        panel.add(button.apply("Fermer", this::hide)).colspan(offers.size()).padTop(GAP * 2);

        layout();
        root.clearActions();
        root.setVisible(true);
        root.setTouchable(Touchable.enabled);
        root.getColor().a = 0f;
        root.addAction(Actions.fadeIn(FADE, Interpolation.pow2Out));
        root.toFront();
    }

    private static String walletText(int gains) {
        return "Tes gains : " + SidePanel.formatGains(gains);
    }

    /** Referme l'échoppe. */
    public void hide() {
        root.clearActions();
        root.setVisible(false);
    }

    /** Recentre l'échoppe (après un redimensionnement). */
    public void layout() {
        float width  = stage.getViewport().getWorldWidth();
        float height = stage.getViewport().getWorldHeight();
        root.setSize(width, height);
        veil.setSize(width, height);
        panel.pack();
        panel.setPosition((width - panel.getWidth()) / 2f, (height - panel.getHeight()) / 2f);
    }

    @Override
    public void dispose() {
        titleFont.dispose();
        textFont.dispose();
        priceFont.dispose();
    }
}
