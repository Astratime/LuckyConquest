package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.context.SpinContext;

import java.util.Random;

/**
 * Machine à sous : tire des symboles selon des poids de base,
 * ajustés par le SpinContext fourni par PreparationResolver.
 * Elle ne sait pas ce que font les symboles — c'est le rôle de CombatResolver.
 * Elle ne stocke plus les boosts : ils vivent dans SpinContext.
 */
public class SlotMachine {

    /** Poids de base attribué à chaque symbole avant application des boosts du tour. */
    private static final int BASE_WEIGHT = 10;

    private final Random   random = new Random();
    private       Symbol[] result = new Symbol[3];

    /**
     * Tire trois symboles indépendamment, selon les poids de base plus les
     * boosts du {@code spinContext}.
     *
     * @param spinContext boosts de probabilité par symbole pour ce tour
     * @return une copie des trois symboles tirés
     */
    public Symbol[] spin(SpinContext spinContext) {
        for (int i = 0; i < result.length; i++) {
            result[i] = weightedRandom(spinContext);
        }
        return result.clone();
    }

    /** Tire un symbole au hasard, pondéré par {@link #BASE_WEIGHT} + le boost du symbole dans {@code spinContext}. */
    private Symbol weightedRandom(SpinContext spinContext) {
        int total = 0;
        for (Symbol s : Symbol.values()) {
            total += BASE_WEIGHT + spinContext.getWeightBoost(s);
        }
        int rand = random.nextInt(total);
        for (Symbol s : Symbol.values()) {
            rand -= BASE_WEIGHT + spinContext.getWeightBoost(s);
            if (rand < 0) return s;
        }
        return Symbol.values()[Symbol.values().length - 1];
    }

    /** Dernier résultat de spin, pour lecture par GameScreen. */
    public Symbol[] getResult() { return result.clone(); }
}
