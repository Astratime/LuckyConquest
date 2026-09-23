package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.entities.effects.EffectPopup;
import fr.astratime.lucky.entities.effects.PopupScale;

import java.util.List;

/** Événement émis quand le joueur gagne du bouclier (DefenseAction). */
public class ShieldGainedEvent extends Event {
    /** Montant de bouclier accordé. */
    public final int amount;

    /** @param amount montant de bouclier accordé. */
    public ShieldGainedEvent(int amount) { this.amount = amount; }

    @Override
    public String describe() { return "Bouclier +" + amount; }

    /** Bouclier gagné, taille maximale selon {@link PopupScale}. */
    @Override
    public List<EffectPopup> getPopups() {
        return List.of(EffectPopup.scaled("BOUCLIER +" + amount, EffectPopup.Style.DEFENSE, amount, PopupScale.SPIN_SHIELD));
    }
}
