package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Filon (carte du Coeur de la montagne) : un soin tout de suite, et un autre une fois le tour de l'ennemi passé.
 */
public class VeinEffect extends Effect {
    private final int percent;

    /** @param percent soin de chaque fois, en % des PV max */
    public VeinEffect(int percent) { this.percent = percent; }

    @Override
    public void onPlay(PlayContext context) {
        context.healPercent(percent);
        context.getLastingEffects().addPendingHeal(percent);
        context.addPopups(getPopups());
    }

    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return Lang.f("Soigne {0} % des PV max, puis encore {0} % après le tour de l'ennemi. Le surplus remplit le Sang.", percent); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("FILON : SOIN {0} %", percent), EffectPopup.Style.DRAIN, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.HEART_DRAIN; }
}
