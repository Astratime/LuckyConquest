package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Événement émis quand le joueur gagne des points (GainAction, bonus de paire/jackpot, etc.). */
public class GainsEarnedEvent extends Event {
    /** Montant de gains crédités. */
    public final double amount;

    /** @param amount montant de gains crédités. */
    public GainsEarnedEvent(double amount) { this.amount = amount; }

    @Override
    public String describe() { return "+" + Lang.big(amount) + " gains"; }

    /** Gains crédités, taille maximale selon {@link PopupScale}. */
    @Override
    public List<EffectPopup> getPopups() {
        return List.of(EffectPopup.scaled(Lang.f("GAINS +{0}", Lang.big(amount)), EffectPopup.Style.GAINS, amount, PopupScale.SPIN_GAINS));
    }
}
