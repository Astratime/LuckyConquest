package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.Symbol;

public class BoostSymbolEffect extends Effect {

    private final Symbol symbol;
    private final int    amount;

    public BoostSymbolEffect(Symbol symbol, int amount) {
        this.symbol = symbol;
        this.amount = amount;
    }

    @Override
    public void apply(GameState state) {
        state.getPlayer().getSlotMachine().boostSymbol(symbol, amount);
    }

    @Override
    public String getDescription() {
        return "Boost " + symbol.name() + " +" + amount;
    }
}
