package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.CardEffect;
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.Symbol;

/** Augmente la probabilité d'apparition d'un symbole au prochain spin. */
public class BoostSymbolEffect implements CardEffect {

    private final Symbol symbol;
    private final int    amount;

    public BoostSymbolEffect(Symbol symbol, int amount) {
        this.symbol = symbol;
        this.amount = amount;
    }

    @Override
    public void apply(GameState state) {
        state.getSlotMachine().boostSymbol(symbol, amount);
    }

    @Override
    public String getDescription() {
        return "Boost " + symbol.name() + " +" + amount;
    }
}
