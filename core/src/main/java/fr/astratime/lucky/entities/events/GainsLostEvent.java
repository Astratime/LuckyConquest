package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Événement émis quand le joueur perd des gains (pari perdu). */
public class GainsLostEvent extends Event {
    /** Montant de gains perdus. */
    public final double amount;

    /** @param amount montant de gains perdus. */
    public GainsLostEvent(double amount) { this.amount = amount; }

    @Override
    public String describe() { return "-" + Lang.big(amount) + " gains"; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(EffectPopup.scaled(Lang.f("GAINS -{0}", Lang.big(amount)), EffectPopup.Style.DAMAGE, amount, PopupScale.SPIN_GAINS));
    }
}
