package fr.astratime.lucky.entities.events;

public class PlayerHealedEvent extends Event {
    public final int amount;

    public PlayerHealedEvent(int amount) { this.amount = amount; }

    @Override
    public String describe() { return "Joueur soigne de " + amount + " PV (drain)"; }
}
