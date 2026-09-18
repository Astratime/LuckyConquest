package fr.astratime.lucky.entities.events;

/** Événement émis quand le joueur est soigné (drain de vie d'une attaque, cartes Coeur). */
public class PlayerHealedEvent extends Event {
    /** Points de vie rendus au joueur. */
    public final int amount;

    /** @param amount points de vie rendus au joueur. */
    public PlayerHealedEvent(int amount) { this.amount = amount; }

    @Override
    public String describe() { return "Joueur soigne de " + amount + " PV (drain)"; }
}
