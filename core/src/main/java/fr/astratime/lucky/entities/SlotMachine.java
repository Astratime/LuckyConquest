package fr.astratime.lucky.entities;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

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

    /** Augmente le poids du symbole ciblé. Appelé par BoostSymbolEffect. */
    public void boostSymbol(Symbol symbol, int amount) {
        weights.put(symbol, weights.get(symbol) + amount);
    }

    /** Remet tous les poids à leur valeur de base. Appelé par GameState.nextTurn(). */
    public void resetBoosts() {
        for (Symbol s : Symbol.values()) {
            weights.put(s, BASE_WEIGHT);
        }
    }

    public Symbol[] getResult() { return result.clone(); }

    public boolean isJackpot() {
        return result[0] != null
            && result[0] == result[1]
            && result[1] == result[2];
    }

    public boolean hasPair() {
        if (result[0] == null) return false;
        return !isJackpot()
            && (result[0] == result[1] || result[1] == result[2] || result[0] == result[2]);
    }
}
