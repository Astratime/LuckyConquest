package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import fr.astratime.lucky.assets.Palette;

/**
 * Bouclier d'un camp, à côté de sa machine à sous : une icône de bouclier et
 * sa valeur (« Défense 130 », « Bouclier 24 »). Il réagit aux coups :
 * <ul>
 *   <li>{@link #block} : un coup est absorbé, le bouclier encaisse (il se
 *       contracte et tremble) et sa valeur baisse ;</li>
 *   <li>usé jusqu'à 0, il se brise : l'icône se fend en deux moitiés grises ;</li>
 *   <li>{@link #pierce} : une attaque l'ignore (Pique) ; il se brise en rouge
 *       et sa valeur, devenue inutile, est barrée de rouge ;</li>
 *   <li>{@link #restore} : il se reforme (nouveau tour).</li>
 * </ul>
 * Le bouclier n'est qu'un affichage : la valeur montrée suit les textes du
 * tirage, pas le modèle (déjà résolu).
 */
public class ShieldBadge extends Group {

    /** Côté de l'icône du bouclier (16 pixels x3). */
    public static final float ICON_SIZE    = 48f;
    public static final float LABEL_GAP    = 8f;
    private static final float SPLIT       = 3f;     // écart des deux moitiés d'un bouclier brisé
    private static final float SPLIT_ANGLE = 14f;
    private static final float BREAK_TIME  = 0.25f;
    private static final float STRIKE_ANGLE = 8f;
    private static final Color BROKEN_GREY = new Color(0.5f, 0.5f, 0.55f, 0.85f);
    private static final Color PIERCED_RED = new Color(1f, 0.35f, 0.35f, 0.9f);
    private static final Color GAIN_FLASH  = Color.valueOf("9fd8ffff");

    private final String name;
    private final int    baseValue;
    private final Group  icon = new Group();
    private final Image  leftHalf;
    private final Image  rightHalf;
    private final Label  label;
    private final Image  strike;
    private int     value;
    private boolean broken;
    private boolean pierced;

    /**
     * @param name      mot affiché devant la valeur (ex : "Défense")
     * @param baseValue valeur au repos : au-delà, la valeur s'affiche en bleu ; à 0, elle s'estompe
     * @param pixel     texture d'un pixel blanc (pour le trait qui barre la valeur)
     */
    public ShieldBadge(String name, int baseValue, Texture shield, BitmapFont font, Texture pixel) {
        this.name      = name;
        this.baseValue = baseValue;
        int half = shield.getWidth() / 2;
        leftHalf  = new Image(new TextureRegion(shield, 0, 0, half, shield.getHeight()));
        rightHalf = new Image(new TextureRegion(shield, half, 0, shield.getWidth() - half, shield.getHeight()));
        leftHalf.setSize(ICON_SIZE / 2f, ICON_SIZE);
        rightHalf.setSize(ICON_SIZE / 2f, ICON_SIZE);
        leftHalf.setOrigin(ICON_SIZE / 2f, 0f);   // les moitiés pivotent depuis le bas du bouclier
        rightHalf.setOrigin(0f, 0f);
        icon.addActor(leftHalf);
        icon.addActor(rightHalf);
        icon.setSize(ICON_SIZE, ICON_SIZE);
        icon.setOrigin(ICON_SIZE / 2f, ICON_SIZE / 2f);
        icon.setTouchable(Touchable.disabled);

        label  = new Label("", new Label.LabelStyle(font, Color.WHITE));
        label.setTouchable(Touchable.disabled);
        strike = new Image(pixel);
        strike.setColor(Palette.TEXT_ALERT);
        strike.setTouchable(Touchable.disabled);
        strike.setVisible(false);

        addActor(icon);
        addActor(label);
        addActor(strike);
        setValue(baseValue);
    }

    /** @return la valeur affichée. */
    public int getValue() { return value; }

    /** @return {@code true} si le bouclier est brisé (usé jusqu'à 0, ou percé ce tour). */
    public boolean isBroken() { return broken; }

    /** Affiche {@code amount}, bouclier intact, sans animation (nouveau combat, fin de tour). */
    public void setValue(int amount) {
        icon.clearActions();
        icon.setScale(1f);
        icon.setPosition(0f, 0f);
        mend(0f);
        value = Math.max(0, amount);
        refresh();
    }

    /** Le bouclier se reforme à {@code amount} (nouveau tour) : ses moitiés se recollent s'il était brisé. */
    public void restore(int amount) {
        mend(broken ? BREAK_TIME : 0f);
        value = Math.max(0, amount);
        refresh();
    }

    /** Le bouclier gagne {@code amount} : il se reforme s'il était brisé et luit en bleu. */
    public void add(int amount) {
        mend(broken ? BREAK_TIME : 0f);
        value += amount;
        refresh();
        punch(1.25f);
        flashHalves(GAIN_FLASH);
    }

    /**
     * Un coup est absorbé : le bouclier encaisse, sa valeur descend à
     * {@code left} ; il se brise s'il n'en reste rien.
     *
     * @return {@code true} si ce coup l'a brisé
     */
    public boolean block(int left) {
        value = Math.max(0, left);
        punch(0.8f);
        shake();
        if (value == 0 && !broken) {
            split(BROKEN_GREY);
            refresh();
            return true;
        }
        refresh();
        return false;
    }

    /**
     * Une attaque ignore le bouclier : il se brise en rouge et sa valeur est
     * barrée. Sans effet s'il est déjà percé, ou s'il n'y a rien à percer.
     *
     * @return {@code true} si le bouclier vient d'être percé
     */
    public boolean pierce() {
        if (pierced || value <= 0) return false;
        pierced = true;
        split(PIERCED_RED);
        shake();
        refresh();
        return true;
    }

    /** Les deux moitiés s'écartent et basculent, teintées de {@code tint}. */
    private void split(Color tint) {
        broken = true;
        leftHalf.clearActions();
        rightHalf.clearActions();
        leftHalf.addAction(Actions.parallel(
            Actions.moveTo(-SPLIT, -3f, BREAK_TIME, Interpolation.pow2Out),
            Actions.rotateTo(SPLIT_ANGLE, BREAK_TIME, Interpolation.pow2Out),
            Actions.color(tint, BREAK_TIME)));
        rightHalf.addAction(Actions.parallel(
            Actions.moveTo(ICON_SIZE / 2f + SPLIT, -3f, BREAK_TIME, Interpolation.pow2Out),
            Actions.rotateTo(-SPLIT_ANGLE, BREAK_TIME, Interpolation.pow2Out),
            Actions.color(tint, BREAK_TIME)));
    }

    /** Les deux moitiés se recollent en {@code duration} secondes (aussitôt si 0). */
    private void mend(float duration) {
        broken  = false;
        pierced = false;
        leftHalf.clearActions();
        rightHalf.clearActions();
        leftHalf.addAction(Actions.parallel(
            Actions.moveTo(0f, 0f, duration, Interpolation.pow2Out),
            Actions.rotateTo(0f, duration),
            Actions.color(Color.WHITE, duration)));
        rightHalf.addAction(Actions.parallel(
            Actions.moveTo(ICON_SIZE / 2f, 0f, duration, Interpolation.pow2Out),
            Actions.rotateTo(0f, duration),
            Actions.color(Color.WHITE, duration)));
        if (duration <= 0f) {
            leftHalf.act(0f);
            rightHalf.act(0f);
        }
    }

    /** L'icône se contracte (ou gonfle) à {@code scale}, puis reprend sa taille. */
    private void punch(float scale) {
        icon.addAction(Actions.sequence(
            Actions.scaleTo(scale, scale, 0.06f, Interpolation.pow2Out),
            Actions.scaleTo(1f, 1f, 0.2f, Interpolation.swingOut)));
    }

    /** L'icône tremble sous le choc. */
    private void shake() {
        icon.addAction(Actions.sequence(
            Actions.moveTo(5f, 0f, 0.03f), Actions.moveTo(-5f, 0f, 0.05f),
            Actions.moveTo(3f, 0f, 0.04f), Actions.moveTo(0f, 0f, 0.04f)));
    }

    /** Les moitiés luisent un instant de {@code color}, puis reprennent leurs couleurs. */
    private void flashHalves(Color color) {
        for (Image half : new Image[] {leftHalf, rightHalf}) {
            half.addAction(Actions.sequence(Actions.color(color, 0.08f), Actions.color(Color.WHITE, 0.35f)));
        }
    }

    /** Texte, couleur et trait de la valeur selon l'état du bouclier. */
    private void refresh() {
        label.setText(name + " " + value);
        Color color = pierced ? Palette.TEXT_ALERT
            : broken || value == 0 ? Palette.STEEL
            : value > baseValue ? Palette.SKY : Palette.TEXT_BODY;
        label.setColor(color);
        label.pack();
        label.setPosition(ICON_SIZE + LABEL_GAP, (ICON_SIZE - label.getHeight()) / 2f);

        strike.setVisible(pierced);
        strike.setSize(label.getWidth() + 8f, 3f);
        strike.setOrigin(strike.getWidth() / 2f, strike.getHeight() / 2f);
        strike.setRotation(STRIKE_ANGLE);
        strike.setPosition(label.getX() - 4f, label.getY() + label.getHeight() / 2f - 1.5f);
        setSize(label.getX() + label.getWidth(), ICON_SIZE);
    }
}
