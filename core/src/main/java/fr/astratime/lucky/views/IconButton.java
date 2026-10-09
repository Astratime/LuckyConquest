package fr.astratime.lucky.views;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Bouton image posé sur la table (le levier de la machine, la pile de jetons
 * de la Mise) : comme {@link CasinoButton}, il s'écrase quand on appuie et,
 * pour un bouton d'action, pulse doucement tant qu'il est disponible. Il a
 * son image au repos, survolée, enfoncée et désactivée.
 */
public class IconButton extends ImageButton {

    private static final float PRESS_SCALE = 0.92f;
    private static final float PULSE_SCALE = 1.06f;
    private static final float PULSE_SPEED = 5f;    // radians par seconde
    private static final float EASE        = 20f;   // rapidité du rattrapage (par seconde)

    private final boolean pulses;
    private float         time;
    /** Temps restant où l'image enfoncée reste affichée (le levier reste tiré), voir {@link #hold}. */
    private float         held;

    /**
     * @param up       image au repos (sa taille fixe celle du bouton)
     * @param pulses   true pour un bouton d'action, qui pulse tant qu'il est disponible
     * @param sound    bruitage joué au clic
     * @param onClick  action déclenchée au clic (jamais quand le bouton est désactivé)
     */
    public IconButton(Texture up, Texture over, Texture down, Texture disabled, boolean pulses, Sound sound,
                      Runnable onClick) {
        super(style(up, over, down, disabled));
        this.pulses = pulses;
        setTransform(true); // nécessaire pour l'échelle
        setSize(up.getWidth(), up.getHeight());
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sound.play();
                onClick.run();
            }
        });
    }

    private static ImageButtonStyle style(Texture up, Texture over, Texture down, Texture disabled) {
        ImageButtonStyle style = new ImageButtonStyle();
        style.imageUp       = new TextureRegionDrawable(new TextureRegion(up));
        style.imageOver     = new TextureRegionDrawable(new TextureRegion(over));
        style.imageDown     = new TextureRegionDrawable(new TextureRegion(down));
        style.imageDisabled = new TextureRegionDrawable(new TextureRegion(disabled));
        return style;
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        setOrigin(getWidth() / 2f, getHeight() / 2f);
    }

    /** Garde l'image enfoncée pendant {@code seconds} (le levier tiré au lancer), même désactivé. */
    public void hold(float seconds) { held = seconds; }

    @Override
    protected Drawable getImageDrawable() {
        if (held > 0f && getStyle().imageDown != null) return getStyle().imageDown;
        return super.getImageDrawable();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        held = Math.max(0f, held - delta);
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
