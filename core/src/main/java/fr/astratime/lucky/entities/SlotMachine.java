package fr.astratime.lucky.entities;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/**
 * Machine à sous : tire des symboles selon des poids.
 * Ne sait pas ce que font les symboles — c'est le rôle de CombatResolver.
 */
public class SlotMachine {

    private static final int BASE_WEIGHT = 10;

    private final Random               random  = new Random();
    private final Map<Symbol, Integer> weights = new EnumMap<>(Symbol.class);
    private final Symbol[]             result  = new Symbol[3];

    public SlotMachine() {
        resetBoosts();
    }

    public void spin() {
        for (int i = 0; i < result.length; i++) {
            result[i] = weightedRandom();
        }
    }

    private Symbol weightedRandom() {
        int total = 0;
        for (int w : weights.values()) total += w;
        int rand = random.nextInt(total);
        for (Symbol s : Symbol.values()) {
            rand -= weights.get(s);
            if (rand < 0) return s;
        }
        return Symbol.values()[Symbol.values().length - 1];
    }

    public void boostSymbol(Symbol symbol, int amount) {
        weights.put(symbol, weights.get(symbol) + amount);
    }

    public void resetBoosts() {
        for (Symbol s : Symbol.values()) {
            weights.put(s, BASE_WEIGHT);
        }
    }

    public Symbol[] getResult() { return result.clone(); }
}
