package fr.astratime.lucky.entities.events;

/** Événement émis quand le joueur gagne des points (GainAction, bonus de paire/jackpot, etc.). */
public class GainsEarnedEvent extends Event {
    /** Montant de gains crédités. */
    public final int amount;

    /** @param amount montant de gains crédités. */
    public GainsEarnedEvent(int amount) { this.amount = amount; }

    @Override
    public String describe() { return "+" + amount + " gains"; }
}
