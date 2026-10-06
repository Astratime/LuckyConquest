package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Transfusion (donjon du Coeur) : soigne tout de suite une part des PV max ;
 * ce qui dépasse les PV max remplit le Sang.
 */
public class TransfusionEffect extends Effect {

    private final int percent;

    /** @param percent soin, en % des PV max */
    public TransfusionEffect(int percent) { this.percent = percent; }

    @Override
    public void onPlay(PlayContext context) {
        context.healPercent(percent);
        context.addPopups(getPopups());
    }

    /** Rien au tirage : le soin a lieu à la pose. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() {
        return Lang.f("Soigne {0}% des PV max. Le surplus remplit le Sang", percent);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("VIE +{0}%", percent), EffectPopup.Style.DRAIN, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.HEART_DRAIN; }
}
