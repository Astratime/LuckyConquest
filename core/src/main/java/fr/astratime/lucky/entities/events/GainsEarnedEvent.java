package fr.astratime.lucky.entities.events;

public class GainsEarnedEvent extends Event {
    public final int amount;

    public GainsEarnedEvent(int amount) { this.amount = amount; }

    @Override
    public String describe() { return "+" + amount + " gains"; }
}
