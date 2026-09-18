package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.entities.Symbol;

/** Événement émis quand un effet de carte augmente le poids de tirage d'un symbole pour ce tour. */
public class SymbolBoostedEvent extends Event {
    /** Symbole dont le poids de tirage est augmenté. */
    public final Symbol symbol;
    /** Montant du boost de poids accordé. */
    public final int    amount;

    /**
     * @param symbol symbole boosté
     * @param amount montant du boost de poids accordé
     */
    public SymbolBoostedEvent(Symbol symbol, int amount) {
        this.symbol = symbol;
        this.amount = amount;
    }

    @Override
    public String describe() { return "Boost " + symbol.name() + " +" + amount; }
}
