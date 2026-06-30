package fr.astratime.lucky.entities.context;

import fr.astratime.lucky.entities.Symbol;

import java.util.EnumMap;
import java.util.Map;

/**
 * Modificateurs liés au spin pour le tour en cours.
 * Alimenté par les effets de cartes (ex: BoostSymbolEffect).
 * Transmis à SlotMachine.spin() — la machine ne connaît rien d'autre.
 */
public class SpinContext {

    private final Map<Symbol, Integer> weightBoosts = new EnumMap<>(Symbol.class);

    public void addWeightBoost(Symbol symbol, int amount) {
        weightBoosts.merge(symbol, amount, Integer::sum);
    }

    public int getWeightBoost(Symbol symbol) {
        return weightBoosts.getOrDefault(symbol, 0);
    }
}
