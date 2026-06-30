package fr.astratime.lucky.entities.events;

public class ShieldGainedEvent extends Event {
    public final int amount;

    public ShieldGainedEvent(int amount) { this.amount = amount; }

    @Override
    public String describe() { return "Bouclier +" + amount; }
}
