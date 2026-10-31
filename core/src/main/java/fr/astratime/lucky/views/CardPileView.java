package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import fr.astratime.lucky.assets.Palette;

import java.util.ArrayList;
import java.util.List;

/**
 * Pile de cartes dos visible (deck ou défausse), avec son nom et le nombre de
 * cartes qu'elle contient au-dessus. La hauteur de la pile reflète le nombre
 * de cartes (jusqu'à STACK_SIZE) ; une pile vide n'affiche qu'un dos estompé.
 *
 * Ne possède pas cardBackTexture : la texture reste possédée (et disposée) par l'appelant.
 */
public class CardPileView extends Group {

    private static final int   STACK_SIZE   = 4;
    private static final float STACK_OFFSET = 3f;
    private static final float LABEL_MARGIN = 6f;
    private static final float EMPTY_ALPHA  = 0.25f;

    private final String      name;
    private final float       cardWidth;
    private final float       cardHeight;
    private final List<Image> stack = new ArrayList<>();
    private final Label       label;
    /** Couleurs du libellé : nom en crème, nombre de cartes en doré (comme les textes du panneau latéral). */
    private static final String NAME_COLOR  = Palette.TEXT_BODY.toString();
    private static final String COUNT_COLOR = Palette.TEXT_TITLE.toString();
    private final BitmapFont  font;
    private       int         count;
    /** Les cartes du dessous dépassent à droite (voir {@link #setStackTowardRight}). */
    private       boolean     towardRight;

    /**
     * @param name            nom affiché au-dessus de la pile (ex : "Deck")
     * @param cardBackTexture texture du dos de carte
     * @param font            police du label
     */
    public CardPileView(String name, Texture cardBackTexture, BitmapFont font, float cardWidth, float cardHeight) {
        this.name       = name;
        this.cardWidth  = cardWidth;
        this.cardHeight = cardHeight;
        this.font       = font;

        for (int i = 0; i < STACK_SIZE; i++) {
            Image card = new Image(new TextureRegionDrawable(new TextureRegion(cardBackTexture)));
            card.setSize(cardWidth, cardHeight);
            card.setPosition(i * STACK_OFFSET, i * STACK_OFFSET);
            addActor(card);
            stack.add(card);
        }

        label = new Label("", new Label.LabelStyle(font, Color.WHITE));
        addActor(label);

        setSize(cardWidth + (STACK_SIZE - 1) * STACK_OFFSET, cardHeight + (STACK_SIZE - 1) * STACK_OFFSET);
        setCount(0);
    }

    /** Met à jour le nombre de cartes affiché et la hauteur de la pile. */
    public void setCount(int count) {
        this.count = count;
        int visible = Math.clamp(count, 1, STACK_SIZE);
        for (int i = 0; i < STACK_SIZE; i++) {
            Image card = stack.get(i);
            card.setVisible(i < visible);
            card.getColor().a = count == 0 ? EMPTY_ALPHA : 1f;
        }
        label.setText(getLabelText()); // police à balises de couleur
        label.pack();
        label.setPosition((getWidth() - label.getWidth()) / 2f, getHeight() + LABEL_MARGIN);
    }

    /**
     * Vue de face : la pile reste couchée sur la table, mais son nom est écrit
     * debout, plus grand, par {@link TableProps} (couché, il serait illisible).
     */
    public void setFrontView(boolean frontView) {
        label.setVisible(!frontView);
    }

    /**
     * Sens de l'empilement : les cartes du dessous dépassent en bas à gauche
     * (par défaut) ou, avec {@code towardRight}, en bas à droite. En vue de face,
     * les piles de gauche de la table empilent vers la droite (vers le milieu).
     */
    public void setStackTowardRight(boolean towardRight) {
        this.towardRight = towardRight;
        for (int i = 0; i < STACK_SIZE; i++) stack.get(i).setPosition(stackX(i), i * STACK_OFFSET);
    }

    private float stackX(int index) {
        return (towardRight ? STACK_SIZE - 1 - index : index) * STACK_OFFSET;
    }

    /** @return le milieu du bas du nom (coordonnées du Stage, à plat). */
    public float getLabelCenterX() { return getX() + getWidth() / 2f; }

    /** @return l'ordonnée (Stage, à plat) du bas du nom. */
    public float getLabelBottomY() { return getY() + label.getY(); }

    /** @return le nom et le nombre de cartes, avec les balises de couleur de la police. */
    public String getLabelText() {
        return "[#" + NAME_COLOR + "]" + name + " [#" + COUNT_COLOR + "]" + count;
    }

    /** @return la police du nom. */
    public BitmapFont getFont() { return font; }

    /** @return le nombre de cartes actuellement affiché. */
    public int getCount() { return count; }

    /** @return l'abscisse (Stage) de la carte du dessus : départ/arrivée des cartes animées. */
    public float getTopX() { return getX() + stackX(topIndex()); }
    /** @return l'ordonnée (Stage) de la carte du dessus : départ/arrivée des cartes animées. */
    public float getTopY() { return getY() + topIndex() * STACK_OFFSET; }

    /** @return l'ordonnée (Stage) du haut du label, pour placer une infobulle au-dessus. */
    public float getLabelTopY() { return getY() + label.getY() + label.getHeight(); }

    /** @return la largeur d'une carte de la pile. */
    public float getCardWidth()  { return cardWidth; }
    /** @return la hauteur d'une carte de la pile. */
    public float getCardHeight() { return cardHeight; }

    private int topIndex() { return Math.clamp(count - 1, 0, STACK_SIZE - 1); }
}
