package fr.astratime.lucky.views;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

/**
 * Bouton au thème casino qui réagit : il s'écrase légèrement quand on appuie
 * dessus, et un bouton d'action pulse doucement tant qu'il est disponible pour
 * montrer au joueur quoi faire ensuite. Le survol est rendu par le style
 * (liseré plus clair, voir {@link CasinoButtons}).
 */
public class CasinoButton extends TextButton {

    private static final float PRESS_SCALE = 0.92f;
    private static final float PULSE_SCALE = 1.06f;
    private static final float PULSE_SPEED = 5f;    // radians par seconde
    private static final float EASE        = 20f;   // rapidité du rattrapage (par seconde)

    private final boolean pulses;
    private float         time;

    /** @param pulses true pour un bouton d'action, qui pulse tant qu'il est disponible */
    public CasinoButton(String text, TextButtonStyle style, boolean pulses) {
        super(text, style);
        this.pulses = pulses;
        setTransform(true); // nécessaire pour l'échelle
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        setOrigin(getWidth() / 2f, getHeight() / 2f);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        time += delta;
        float target = 1f;
        if (!isDisabled() && isPressed()) {
            target = PRESS_SCALE;
        } else if (pulses && !isDisabled() && isVisible()) {
            target = 1f + (PULSE_SCALE - 1f) * (0.5f + 0.5f * MathUtils.sin(time * PULSE_SPEED));
        }
        float scale = getScaleX() + (target - getScaleX()) * Math.min(1f, EASE * delta);
        setScale(scale);
    }
}
