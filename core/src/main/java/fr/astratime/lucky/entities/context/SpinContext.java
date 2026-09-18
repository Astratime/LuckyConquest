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

    /** Ajoute {@code amount} au boost de poids du symbole (cumulable sur plusieurs cartes). */
    public void addWeightBoost(Symbol symbol, int amount) {
        weightBoosts.merge(symbol, amount, Integer::sum);
    }

    /** @return le boost de poids accumulé pour ce symbole (0 si aucun). */
    public int getWeightBoost(Symbol symbol) {
        return weightBoosts.getOrDefault(symbol, 0);
    }
}
