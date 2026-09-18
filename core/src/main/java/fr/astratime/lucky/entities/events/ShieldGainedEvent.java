package fr.astratime.lucky.entities.events;

/** Événement émis quand le joueur gagne du bouclier (DefenseAction). */
public class ShieldGainedEvent extends Event {
    /** Montant de bouclier accordé. */
    public final int amount;

    /** @param amount montant de bouclier accordé. */
    public ShieldGainedEvent(int amount) { this.amount = amount; }

    @Override
    public String describe() { return "Bouclier +" + amount; }
}
