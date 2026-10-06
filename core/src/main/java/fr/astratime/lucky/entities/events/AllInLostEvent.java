package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Tapis perdu : pas de paire, le joueur perd tous ses gains. */
public class AllInLostEvent extends GainsLostEvent {

    /** @param amount gains perdus */
    public AllInLostEvent(int amount) { super(amount); }

    @Override
    public String describe() { return "Tapis perdu : -" + amount + " gains"; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup(Lang.t("TAPIS PERDU !"), EffectPopup.Style.DAMAGE, PopupScale.MAX_INTENSITY),
            EffectPopup.scaled(Lang.f("GAINS -{0}", amount), EffectPopup.Style.DAMAGE, amount, PopupScale.SPIN_GAINS));
    }
}
