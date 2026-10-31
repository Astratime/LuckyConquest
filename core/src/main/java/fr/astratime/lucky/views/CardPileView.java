package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
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
    private final Texture     cardBack;
    private       int         count;
    /** Vue de face : la pile est dessinée en volume par {@link PileSolid} ; ici, rien que la zone cliquable. */
    private       boolean     frontView;
    /** Vue de face : la zone cliquable monte au-dessus de la carte du dessous (le volume de la pile). */
    private static final float FRONT_HIT_EXTRA = 40f;

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
        this.cardBack   = cardBackTexture;

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
            card.getColor().a = frontView ? 0f : (count == 0 ? EMPTY_ALPHA : 1f);
        }
        label.setText(getLabelText()); // police à balises de couleur
        label.pack();
        label.setPosition((getWidth() - label.getWidth()) / 2f, getHeight() + LABEL_MARGIN);
    }

    /**
     * Vue de face : les cartes et le nom ne sont plus dessinés ici (la pile est
     * un volume posé sur la table, voir {@link PileSolid}) ; la zone cliquable reste.
     */
    public void setFrontView(boolean frontView) {
        this.frontView = frontView;
        for (Image card : stack) card.getColor().a = frontView ? 0f : (count == 0 ? EMPTY_ALPHA : 1f);
        label.setVisible(!frontView);
    }

    @Override
    public Actor hit(float x, float y, boolean touchable) {
        if (frontView && touchable && getTouchable() == Touchable.enabled && isVisible()
            && x >= 0f && x < getWidth() && y >= 0f && y < getHeight() + FRONT_HIT_EXTRA) {
            return this;
        }
        return super.hit(x, y, touchable);
    }

    /** @return la place de la pile sur la table (une carte, au milieu de l'emplacement), coordonnées du Stage. */
    public Rectangle getFootprint(Rectangle out) {
        float shift = (STACK_SIZE - 1) * STACK_OFFSET / 2f;
        return out.set(getX() + shift, getY() + shift, cardWidth, cardHeight);
    }

    /** @return le nom et le nombre de cartes, avec les balises de couleur de la police. */
    public String getLabelText() {
        return "[#" + NAME_COLOR + "]" + name + " [#" + COUNT_COLOR + "]" + count;
    }

    /** @return la police du nom. */
    public BitmapFont getFont() { return font; }

    /** @return le dos des cartes. */
    public Texture getCardBack() { return cardBack; }

    /** @return le nombre de cartes actuellement affiché. */
    public int getCount() { return count; }

    /** @return l'abscisse (Stage) de la carte du dessus : départ/arrivée des cartes animées. */
    public float getTopX() { return getX() + topIndex() * STACK_OFFSET; }
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
