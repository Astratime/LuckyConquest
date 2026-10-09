package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * La Mise posée sur la table, à droite du bouclier du joueur : un jeton de
 * casino par palier (10 %, 25 %, 50 %), qui tombe et rebondit quand le joueur
 * mise plus. Au lancer, les jetons glissent vers la machine et disparaissent ;
 * quand la Mise revient à zéro, ils s'effacent.
 */
public class StakeChips extends Group {

    /** Côté d'un jeton (le PNG fait 60 pixels). */
    public static final float CHIP_SIZE = 48f;
    /** Décalage vertical entre deux jetons de la pile. */
    private static final float STACK_STEP = 9f;
    /** Hauteur d'où tombe un jeton. */
    private static final float DROP_HEIGHT = 140f;
    private static final float DROP_TIME   = 0.35f;
    private static final float FADE_TIME   = 0.2f;
    private static final float SLIDE_TIME  = 0.3f;

    private final Texture chip;
    /** Jetons sur la pile (sans ceux qui s'effacent). */
    private final List<Image> chips = new ArrayList<>();

    public StakeChips(Texture chip) {
        this.chip = chip;
        setSize(CHIP_SIZE, CHIP_SIZE + STACK_STEP * 2);
        setTouchable(Touchable.disabled);
    }

    /**
     * Montre {@code count} jetons (0 à 3) : les nouveaux tombent sur la pile,
     * ceux en trop s'effacent.
     */
    public void show(int count) {
        int shown = chips.size();
        for (int i = shown; i < count; i++) {
            Image image = new Image(chip);
            image.setSize(CHIP_SIZE, CHIP_SIZE);
            image.setOrigin(CHIP_SIZE / 2f, CHIP_SIZE / 2f);
            float y = i * STACK_STEP;
            image.setPosition(0f, y + DROP_HEIGHT);
            image.getColor().a = 0f;
            image.addAction(Actions.parallel(
                Actions.fadeIn(DROP_TIME / 3f),
                Actions.moveTo(0f, y, DROP_TIME, Interpolation.bounceOut),
                Actions.sequence(Actions.rotateTo(-20f), Actions.rotateTo(0f, DROP_TIME, Interpolation.pow2Out))));
            addActor(image);
            chips.add(image);
        }
        for (int i = shown - 1; i >= count; i--) {
            Actor image = chips.remove(i);
            image.clearActions();
            image.addAction(Actions.sequence(Actions.fadeOut(FADE_TIME), Actions.removeActor()));
        }
    }

    /** Lancer : les jetons glissent de {@code dx} (vers la machine) et disparaissent. */
    public void spend(float dx) {
        for (Actor image : chips) {
            image.clearActions();
            image.addAction(Actions.sequence(
                Actions.parallel(Actions.moveBy(dx, 0f, SLIDE_TIME, Interpolation.pow2In), Actions.fadeOut(SLIDE_TIME)),
                Actions.removeActor()));
        }
        chips.clear();
    }
}
