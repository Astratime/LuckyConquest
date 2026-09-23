package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.entities.effects.EffectPopup;
import fr.astratime.lucky.entities.effects.PopupScale;

import java.util.List;

/** Événement émis quand le joueur gagne des points (GainAction, bonus de paire/jackpot, etc.). */
public class GainsEarnedEvent extends Event {
    /** Montant de gains crédités. */
    public final int amount;

    /** @param amount montant de gains crédités. */
    public GainsEarnedEvent(int amount) { this.amount = amount; }

    @Override
    public String describe() { return "+" + amount + " gains"; }

    /** Gains crédités, taille maximale selon {@link PopupScale}. */
    @Override
    public List<EffectPopup> getPopups() {
        return List.of(EffectPopup.scaled("GAINS +" + amount, EffectPopup.Style.GAINS, amount, PopupScale.SPIN_GAINS));
    }
}
