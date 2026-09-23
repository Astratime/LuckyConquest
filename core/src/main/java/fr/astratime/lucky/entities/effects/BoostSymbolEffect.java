package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.events.SymbolBoostedEvent;

import java.util.List;

/** Augmente la probabilité d'apparition d'un symbole via le SpinContext. */
public class BoostSymbolEffect extends Effect {

    private final Symbol symbol;
    private final int    amount;

    /**
     * @param symbol symbole dont le poids de tirage est augmenté
     * @param amount montant du boost de poids
     */
    public BoostSymbolEffect(Symbol symbol, int amount) {
        this.symbol = symbol;
        this.amount = amount;
    }

    @Override
    public void apply(TurnContext context) {
        context.getSpinContext().addWeightBoost(symbol, amount);
        context.addEvent(new SymbolBoostedEvent(symbol, amount));
    }

    @Override
    public String getDescription() {
        return "Boost " + symbol.name() + " +" + amount;
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            EffectPopup.scaled("BOOST " + symbol.name() + " +" + amount, EffectPopup.Style.SPECIAL, amount, PopupScale.CARD_SYMBOL_BOOST)
        );
    }
}
