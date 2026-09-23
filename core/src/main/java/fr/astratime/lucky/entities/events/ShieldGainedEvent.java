package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.entities.effects.EffectPopup;

import java.util.List;

/** Événement émis quand le joueur gagne du bouclier (DefenseAction). */
public class ShieldGainedEvent extends Event {
    /** Montant de bouclier accordé. */
    public final int amount;

    /** @param amount montant de bouclier accordé. */
    public ShieldGainedEvent(int amount) { this.amount = amount; }

    @Override
    public String describe() { return "Bouclier +" + amount; }

    /** Bouclier gagné ; 40 donne la taille de texte maximale. */
    @Override
    public List<EffectPopup> getPopups() {
        return List.of(EffectPopup.scaled("BOUCLIER +" + amount, EffectPopup.Style.DEFENSE, amount, 40f));
    }
}
