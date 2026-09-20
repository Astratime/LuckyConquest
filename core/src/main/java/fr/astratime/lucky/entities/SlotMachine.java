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
    private static final int      BASE_WEIGHT = 10;
    /** Copie mise en cache de {@link Symbol#values()}, qui clone son tableau à chaque appel. */
    private static final Symbol[] SYMBOLS     = Symbol.values();

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
        int[] weights = new int[SYMBOLS.length];
        int   total   = 0;
        for (int i = 0; i < SYMBOLS.length; i++) {
            weights[i] = BASE_WEIGHT + spinContext.getWeightBoost(SYMBOLS[i]);
            total += weights[i];
        }
        for (int i = 0; i < result.length; i++) {
            result[i] = weightedRandom(weights, total);
        }
        return result.clone();
    }

    /** Tire un symbole au hasard parmi {@code weights}, dont la somme vaut {@code total}. */
    private Symbol weightedRandom(int[] weights, int total) {
        int rand = random.nextInt(total);
        for (int i = 0; i < weights.length; i++) {
            rand -= weights[i];
            if (rand < 0) return SYMBOLS[i];
        }
        return SYMBOLS[SYMBOLS.length - 1];
    }

    /** Dernier résultat de spin, pour lecture par GameScreen. */
    public Symbol[] getResult() { return result.clone(); }
}
