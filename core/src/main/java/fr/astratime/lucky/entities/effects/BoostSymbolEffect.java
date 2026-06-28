package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.TurnContext;

/** Augmente la probabilité d'apparition d'un symbole via le SpinContext. */
public class BoostSymbolEffect extends Effect {

    private final Symbol symbol;
    private final int    amount;

    public BoostSymbolEffect(Symbol symbol, int amount) {
        this.symbol = symbol;
        this.amount = amount;
    }

    @Override
    public void apply(TurnContext context) {
        context.getSpinContext().addWeightBoost(symbol, amount);
    }

    @Override
    public String getDescription() {
        return "Boost " + symbol.name() + " +" + amount;
    }
}
