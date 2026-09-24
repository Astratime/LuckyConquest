package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import fr.astratime.lucky.animations.SpinningSprite;

/**
 * Option d'un menu. Au repos, seul son texte (crème) est visible ;
 * sélectionnée (survol de la souris ou flèches du clavier), elle s'encadre
 * d'un liseré doré, son texte passe en or et grossit légèrement, et un jeton
 * de casino tournoie de chaque côté.
 */
public class MenuOption extends Group {

    private static final Color IDLE_COLOR     = Color.valueOf("f0e0b0ff");
    private static final Color SELECTED_COLOR = Color.valueOf("ffd54aff");
    private static final float CHIP_SIZE      = 48f;   // 16 pixels de la grille x3
    private static final float CHIP_GAP       = 16f;   // entre le cadre et un jeton
    private static final float CHIP_FPS       = 12f;
    private static final float SELECTED_SCALE = 1.06f;
    private static final float ANIM_TIME      = 0.12f;

    private final Label          label;
    private final Image          frame;
    private final SpinningSprite leftChip;
    private final SpinningSprite rightChip;
    private boolean              selected;

    /**
     * @param frameDrawable cadre affiché quand l'option est sélectionnée
     * @param chipStrip     étapes de rotation du jeton, côte à côte
     */
    public MenuOption(String text, Label.LabelStyle style, Drawable frameDrawable, TextureRegion chipStrip,
                      float width, float height) {
        setSize(width, height);
        setOrigin(width / 2f, height / 2f);
        setTransform(true); // nécessaire pour l'échelle

        frame = new Image(frameDrawable);
        frame.setBounds(0f, 0f, width, height);
        frame.setTouchable(Touchable.disabled);
        frame.getColor().a = 0f;
        addActor(frame);

        label = new Label(text, style);
        label.setBounds(0f, 0f, width, height);
        label.setAlignment(Align.center);
        label.setColor(IDLE_COLOR);
        label.setTouchable(Touchable.disabled);
        addActor(label);

        float chipY = (height - CHIP_SIZE) / 2f;
        leftChip  = chip(chipStrip, -CHIP_GAP - CHIP_SIZE, chipY, 0);
        rightChip = chip(chipStrip, width + CHIP_GAP, chipY, 4); // décalé : les deux jetons ne tournent pas en miroir
    }

    private SpinningSprite chip(TextureRegion strip, float x, float y, int startFrame) {
        SpinningSprite chip = new SpinningSprite(strip, CHIP_FPS, startFrame);
        chip.setBounds(x, y, CHIP_SIZE, CHIP_SIZE);
        chip.setVisible(false);
        addActor(chip);
        return chip;
    }

    /** @return true si l'option est sélectionnée. */
    public boolean isSelected() { return selected; }

    /** Sélectionne (cadre doré, texte en or, jetons qui tournoient) ou désélectionne l'option. */
    public void setSelected(boolean selected) {
        if (this.selected == selected) return;
        this.selected = selected;
        clearActions();
        addAction(Actions.scaleTo(selected ? SELECTED_SCALE : 1f, selected ? SELECTED_SCALE : 1f,
            ANIM_TIME, Interpolation.pow2Out));
        frame.clearActions();
        frame.addAction(Actions.alpha(selected ? 1f : 0f, ANIM_TIME));
        label.setColor(selected ? SELECTED_COLOR : IDLE_COLOR);
        for (SpinningSprite chip : new SpinningSprite[] {leftChip, rightChip}) {
            chip.clearActions();
            if (selected) {
                chip.setVisible(true);
                chip.setScale(0f);
                chip.addAction(Actions.scaleTo(1f, 1f, ANIM_TIME * 2f, Interpolation.swingOut));
            } else {
                chip.setVisible(false);
            }
        }
    }

    /** Change le texte de l'option (ex : réglage basculé). */
    public void setText(String text) {
        label.setText(text);
    }
}
