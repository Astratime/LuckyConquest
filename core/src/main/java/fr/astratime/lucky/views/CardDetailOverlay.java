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
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.effects.Effect;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Fiche d'une carte, par-dessus le jeu : la carte en grand à gauche et, à sa
 * droite, ses informations (nom, couleur, rang, type, effets, prix à
 * l'échoppe). Un clic n'importe où ou Échap (géré par l'appelant via
 * {@link #hide()}) la referme.
 */
public class CardDetailOverlay implements Disposable {

    private static final Color VEIL       = new Color(0f, 0f, 0f, 0.78f);
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
    private final BitmapFont  nameFont  = Fonts.jersey(64, Palette.TEXT_TITLE, 3f, Palette.TEXT_SHADE);
    private final BitmapFont  keyFont   = Fonts.jersey(34, Palette.TEXT_TITLE, 2f, Palette.TEXT_SHADE);
    private final BitmapFont  valueFont = Fonts.jersey(34, Palette.TEXT_BODY, 2f, Palette.TEXT_SHADE);
    private final BitmapFont  hintFont  = Fonts.jersey(24, Color.LIGHT_GRAY, 2f, Palette.TEXT_SHADE);

    private final Group root  = new Group();
    private final Image veil;
    private final Table panel = new Table();
    private final Image rule;

    /**
     * @param tooltip   infobulle à cacher à l'ouverture ({@code null} : aucune)
     * @param cardWidth taille d'une carte de la main (agrandie ici)
     */
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
        show(card, texture, price != null ? Lang.t("Prix à l'échoppe") : null,
            price != null ? Lang.f("{0} gains", SidePanel.formatGains(price)) : null);
    }

    /**
     * Fiche d'une carte vendue à la Boutique du menu, avec son prix en pièces.
     *
     * @param price prix, déjà mis en forme (« 3 000 000 pièces »)
     */
    public void showForSale(Card card, Texture texture, String price) {
        show(card, texture, Lang.t("Prix en boutique"), price);
    }

    private void show(Card card, Texture texture, String priceKey, String price) {
        List<String[]> lines = new ArrayList<>();
        lines.add(new String[] {Lang.t("Nom"), card.getName()});
        lines.add(new String[] {Lang.t("Couleur"), suitName(card.getSuit())});
        if (card.getSuit() != null) lines.add(new String[] {Lang.t("Rang"), rankName(card.getRank())});
        lines.add(new String[] {Lang.t("Type"), card.isConsumable() ? Lang.t("Consommable : disparaît une fois jouée")
            : card.getSuit() != null ? Lang.t("Carte à suite") : Lang.t("Carte spéciale")});
        if (price != null) lines.add(new String[] {priceKey, price});
        List<String> effects = new ArrayList<>();
        for (Effect effect : card.getEffects()) effects.addAll(List.of(effect.getDescription().split("\n")));
        open(new TextureRegion(texture), cardWidth, cardHeight, card.getName(), lines, effects);
    }

    /**
     * Fiche d'un rouleau vendu à la Boutique : son symbole en grand, son effet et son prix.
     *
     * @param effect effet du rouleau (une phrase par ligne)
     * @param price  prix, déjà mis en forme
     * @param state  ce que le joueur en a (« Possédé »…)
     */
    public void showReel(String name, TextureRegion region, String effect, String price, String state) {
        float width = cardWidth * 1.2f;
        float height = width * region.getRegionHeight() / region.getRegionWidth();
        List<String[]> lines = new ArrayList<>();
        lines.add(new String[] {Lang.t("Nom"), name});
        lines.add(new String[] {Lang.t("Type"), Lang.t("Rouleau de la machine")});
        lines.add(new String[] {Lang.t("Prix en boutique"), price});
        lines.add(new String[] {Lang.t("État"), state});
        open(region, width, height, name, lines, List.of(effect.split("\n")));
    }

    /**
     * Fiche d'un effet en cours dans un combat (panneau « Effets ») : son icône
     * en grand, son état et ce qu'il fait.
     *
     * @param icon        icône de l'effet
     * @param state       son état (« 3 tours », « Lames 4 (+80) »…)
     * @param description ce que fait l'effet, en phrases courtes (une ligne par phrase sur la fiche)
     */
    public void showEffect(String name, TextureRegion icon, String state, String description) {
        float size = cardWidth * 0.8f;
        List<String[]> lines = new ArrayList<>();
        lines.add(new String[] {Lang.t("Nom"), name});
        lines.add(new String[] {Lang.t("Type"), Lang.t("Effet en cours")});
        lines.add(new String[] {Lang.t("État"), state});
        open(icon, size, size * icon.getRegionHeight() / icon.getRegionWidth(), name, lines,
            List.of(description.split("(?<=[.!]) ")));
    }

    /** Ouvre la fiche : l'image en grand à gauche, le nom, les lignes « Clé : valeur » et les effets à droite. */
    private void open(TextureRegion region, float width, float height, String name, List<String[]> lines,
                      List<String> effects) {
        panel.clearChildren();
        Image image = new Image(new TextureRegionDrawable(region));
        panel.add(image).size(width, height).top().padRight(CARD_GAP);

        Table info = new Table();
        info.top().left();
        info.add(new Label(name, new Label.LabelStyle(nameFont, Color.WHITE))).left();
        info.row();
        info.add(rule).growX().height(rule.getPrefHeight() * 1.5f).padTop(LINE_GAP).padBottom(LINE_GAP * 2);
        info.row();
        for (String[] line : lines) line(info, line[0], line[1]);

        info.add(label(Lang.t("Effets :"), keyFont)).left().padTop(LINE_GAP * 2);
        info.row();
        if (effects.isEmpty()) {
            info.add(wrapped(Lang.t("- Aucun effet"))).width(INFO_WIDTH).left().padTop(LINE_GAP);
            info.row();
        }
        for (String part : effects) {
            info.add(wrapped("- " + part)).width(INFO_WIDTH).left().padTop(LINE_GAP);
            info.row();
        }
        info.add(label(Lang.t("Clic ou Échap pour fermer"), hintFont)).left().padTop(LINE_GAP * 4);
        panel.add(info).width(INFO_WIDTH).top();

        layout();
        if (tooltip != null) tooltip.hide();
        root.clearActions();
        root.setVisible(true);
        root.setTouchable(Touchable.enabled);
        root.getColor().a = 0f;
        root.addAction(Actions.fadeIn(FADE, Interpolation.pow2Out));
        image.setOrigin(width / 2f, height / 2f);
        image.setScale(0.85f);
        image.addAction(Actions.scaleTo(1f, 1f, 0.25f, Interpolation.swingOut));
        root.toFront();
    }

    /** Ajoute une ligne « Clé : valeur » (la valeur est renvoyée à la ligne si besoin). */
    private void line(Table info, String key, String value) {
        Table row = new Table();
        row.add(label(Lang.f("{0} : ", key), keyFont)).top().left();
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
        if (suit == null) return Lang.t("Spéciale");
        return switch (suit) {
            case COEUR   -> Lang.t("Coeur");
            case CARREAU -> Lang.t("Carreau");
            case TREFLE  -> Lang.t("Trèfle");
            case PIQUE   -> Lang.t("Pique");
        };
    }

    /** @return le nom affiché d'un rang (As, Valet, Dame, Roi, ou le chiffre). */
    static String rankName(int rank) {
        return switch (rank) {
            case 1  -> Lang.t("As");
            case 11 -> Lang.t("Valet");
            case 12 -> Lang.t("Dame");
            case 13 -> Lang.t("Roi");
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
        Fonts.release(nameFont);
        Fonts.release(keyFont);
        Fonts.release(valueFont);
        Fonts.release(hintFont);
    }
}
