package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
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
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.effects.Effect;

import java.util.function.Function;

/**
 * Fiche d'une carte, par-dessus le jeu : la carte en grand à gauche et, à sa
 * droite, ses informations (nom, couleur, rang, type, effets, prix à
 * l'échoppe). Un clic n'importe où ou Échap (géré par l'appelant via
 * {@link #hide()}) la referme.
 */
public class CardDetailOverlay implements Disposable {

    private static final Color VEIL       = new Color(0f, 0f, 0f, 0.78f);
    private static final Color GOLD       = Color.valueOf("ffd454ff");
    private static final Color CREAM      = Color.valueOf("f0e0b0ff");
    private static final Color TEXT_SHADE = Color.valueOf("1a0f0fff");
    private static final float CARD_SCALE = 3.4f;
    private static final float PADDING    = 48f;
    private static final float INFO_WIDTH = 560f;
    private static final float CARD_GAP   = 56f;
    private static final float LINE_GAP   = 10f;
    private static final float FADE       = 0.18f;

    private final Stage       stage;
    private final Tooltip     tooltip;
    private final float       cardWidth;
    private final float       cardHeight;
    private final BitmapFont  nameFont  = Fonts.jersey(64, GOLD, 3f, TEXT_SHADE);
    private final BitmapFont  keyFont   = Fonts.jersey(34, GOLD, 2f, TEXT_SHADE);
    private final BitmapFont  valueFont = Fonts.jersey(34, CREAM, 2f, TEXT_SHADE);
    private final BitmapFont  hintFont  = Fonts.jersey(24, Color.LIGHT_GRAY, 2f, TEXT_SHADE);

    private final Group root  = new Group();
    private final Image veil;
    private final Table panel = new Table();
    private final Image rule;

    /** @param cardWidth taille d'une carte de la main (agrandie ici) */
    public CardDetailOverlay(Stage stage, HudTextures hud, Tooltip tooltip, float cardWidth, float cardHeight) {
        this.stage      = stage;
        this.tooltip    = tooltip;
        this.cardWidth  = cardWidth * CARD_SCALE;
        this.cardHeight = cardHeight * CARD_SCALE;
        veil = new Image(new TextureRegionDrawable(new TextureRegion(hud.pixel)));
        veil.setColor(VEIL);
        rule = new Image(new TextureRegionDrawable(new TextureRegion(hud.tooltipRule)));
        panel.setBackground(hud.tooltipDrawable());
        panel.pad(PADDING);
        root.addActor(veil);
        root.addActor(panel);
        root.setVisible(false);
        root.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { hide(); }
        });
    }

    /** @return l'acteur racine de la fiche, à ajouter au Stage par-dessus le jeu. */
    public Group getActor() { return root; }

    /** @return {@code true} tant que la fiche est affichée. */
    public boolean isShown() { return root.isVisible(); }

    /**
     * Affiche la fiche de {@code card}.
     *
     * @param texture image de la carte
     * @param price   prix à l'échoppe, ou {@code null} si la carte n'y est pas vendue
     */
    public void show(Card card, Texture texture, Integer price) {
        panel.clearChildren();
        Image image = new Image(new TextureRegionDrawable(new TextureRegion(texture)));
        panel.add(image).size(cardWidth, cardHeight).top().padRight(CARD_GAP);

        Table info = new Table();
        info.top().left();
        info.add(new Label(card.getName(), new Label.LabelStyle(nameFont, Color.WHITE))).left();
        info.row();
        info.add(rule).growX().height(rule.getPrefHeight() * 1.5f).padTop(LINE_GAP).padBottom(LINE_GAP * 2);
        info.row();
        line(info, "Nom", card.getName());
        line(info, "Couleur", suitName(card.getSuit()));
        if (card.getSuit() != null) line(info, "Rang", rankName(card.getRank()));
        line(info, "Type", card.isConsumable() ? "Consommable : disparaît une fois jouée"
            : card.getSuit() != null ? "Carte à suite" : "Carte spéciale");
        if (price != null) line(info, "Prix à l'échoppe", SidePanel.formatGains(price) + " gains");

        info.add(label("Effets :", keyFont)).left().padTop(LINE_GAP * 2);
        info.row();
        if (card.getEffects().isEmpty()) {
            info.add(wrapped("- Aucun effet")).width(INFO_WIDTH).left().padTop(LINE_GAP);
            info.row();
        }
        for (Effect effect : card.getEffects()) {
            for (String part : effect.getDescription().split("\n")) {
                info.add(wrapped("- " + part)).width(INFO_WIDTH).left().padTop(LINE_GAP);
                info.row();
            }
        }
        info.add(label("Clic ou Échap pour fermer", hintFont)).left().padTop(LINE_GAP * 4);
        panel.add(info).width(INFO_WIDTH).top();

        layout();
        tooltip.hide();
        root.clearActions();
        root.setVisible(true);
        root.setTouchable(Touchable.enabled);
        root.getColor().a = 0f;
        root.addAction(Actions.fadeIn(FADE, Interpolation.pow2Out));
        image.setOrigin(cardWidth / 2f, cardHeight / 2f);
        image.setScale(0.85f);
        image.addAction(Actions.scaleTo(1f, 1f, 0.25f, Interpolation.swingOut));
        root.toFront();
    }

    /** Ajoute une ligne « Clé : valeur » (la valeur est renvoyée à la ligne si besoin). */
    private void line(Table info, String key, String value) {
        Table row = new Table();
        row.add(label(key + " : ", keyFont)).top().left();
        Label valueLabel = new Label(value, new Label.LabelStyle(valueFont, Color.WHITE));
        valueLabel.setWrap(true);
        row.add(valueLabel).growX().left();
        info.add(row).width(INFO_WIDTH).left().padTop(LINE_GAP);
        info.row();
    }

    private Label label(String text, BitmapFont font) {
        return new Label(text, new Label.LabelStyle(font, Color.WHITE));
    }

    private Label wrapped(String text) {
        Label label = new Label(text, new Label.LabelStyle(valueFont, Color.WHITE));
        label.setWrap(true);
        label.setAlignment(Align.left);
        return label;
    }

    /** @return le nom affiché de la couleur d'une carte ("Spéciale" pour une carte sans suite). */
    static String suitName(Card.Suit suit) {
        if (suit == null) return "Spéciale";
        return switch (suit) {
            case COEUR   -> "Coeur";
            case CARREAU -> "Carreau";
            case TREFLE  -> "Trèfle";
            case PIQUE   -> "Pique";
        };
    }

    /** @return le nom affiché d'un rang (As, Valet, Dame, Roi, ou le chiffre). */
    static String rankName(int rank) {
        return switch (rank) {
            case 1  -> "As";
            case 11 -> "Valet";
            case 12 -> "Dame";
            case 13 -> "Roi";
            default -> String.valueOf(rank);
        };
    }

    /** Referme la fiche. */
    public void hide() {
        root.clearActions();
        root.setVisible(false);
    }

    /** Recentre la fiche (après un redimensionnement). */
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
        nameFont.dispose();
        keyFont.dispose();
        valueFont.dispose();
        hintFont.dispose();
    }
}
