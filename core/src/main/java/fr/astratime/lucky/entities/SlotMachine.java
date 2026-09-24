package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.context.SpinContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Machine à sous : tire des symboles selon des poids de base,
 * ajustés par le SpinContext fourni par PreparationResolver.
 * Elle ne sait pas ce que font les symboles — c'est le rôle de CombatResolver.
 * Elle ne stocke plus les boosts : ils vivent dans SpinContext.
 *
 * Le Joker est un symbole à part : plus rare, il compte comme n'importe quel
 * symbole. {@link #spin} rend les symboles tels qu'ils s'arrêtent sur les
 * rouleaux (Jokers compris), {@link #resolveJokers} ce qu'ils valent.
 */
public class SlotMachine {

    /** Nombre de symboles tirés à chaque spin (un par rouleau). */
    public static final int SYMBOL_COUNT = 3;

    /** Poids de base attribué à chaque symbole avant application des boosts du tour. */
    private static final int BASE_WEIGHT  = 10;
    /** Poids de base du Joker, plus rare que les autres symboles. */
    private static final int JOKER_WEIGHT = 4;

    private final Random   random;

    public SlotMachine() {
        this(new Random());
    }

    /** @param random source d'aléatoire (ex : graine fixe pour des tests reproductibles) */
    SlotMachine(Random random) {
        this.random = random;
    }

    /**
     * Tire trois symboles indépendamment, selon les poids de base plus les
     * boosts du {@code spinContext}, sans les symboles retirés. Un jackpot
     * garanti (Bingo) donne trois fois le même symbole (jamais le Joker) ; sinon,
     * l'Aimant peut transformer un tirage sans paire en paire.
     *
     * @param spinContext modificateurs du spin pour ce tour
     * @return les trois symboles arrêtés sur les rouleaux (Jokers compris)
     */
    public Symbol[] spin(SpinContext spinContext) {
        Symbol[] result = new Symbol[SYMBOL_COUNT];
        if (spinContext.isJackpotForced()) {
            Arrays.fill(result, weightedRandom(spinContext, false));
            return result;
        }
        for (int i = 0; i < result.length; i++) {
            result[i] = weightedRandom(spinContext, true);
        }
        if (!hasPairOrJoker(result) && random.nextFloat() < spinContext.getPairChance()) {
            int from = random.nextInt(SYMBOL_COUNT);
            int to   = (from + 1 + random.nextInt(SYMBOL_COUNT - 1)) % SYMBOL_COUNT;
            result[to] = result[from];
        }
        return result;
    }

    /**
     * Remplace chaque Joker par le symbole qui lui rapporte le plus : avec deux
     * symboles identiques, il complète le jackpot ; avec deux Jokers, ils prennent
     * la valeur du troisième symbole ; avec deux symboles différents, il forme une
     * paire avec l'un d'eux (au hasard) ; trois Jokers deviennent un jackpot d'un
     * symbole tiré au hasard.
     *
     * @param drawn       symboles arrêtés sur les rouleaux (voir {@link #spin})
     * @param spinContext modificateurs du tour (symboles retirés, boosts)
     * @return une copie de {@code drawn} dont les Jokers sont remplacés
     */
    public Symbol[] resolveJokers(Symbol[] drawn, SpinContext spinContext) {
        Symbol[] resolved = drawn.clone();
        List<Symbol> others = new ArrayList<>();
        for (Symbol symbol : drawn) {
            if (symbol != Symbol.JOKER) others.add(symbol);
        }
        if (others.size() == drawn.length) return resolved;

        Symbol value;
        if (others.isEmpty()) {
            value = weightedRandom(spinContext, false);
        } else if (others.size() == 1 || others.get(0) == others.get(1)) {
            value = others.get(0);
        } else {
            value = others.get(random.nextInt(others.size()));
        }
        for (int i = 0; i < resolved.length; i++) {
            if (resolved[i] == Symbol.JOKER) resolved[i] = value;
        }
        return resolved;
    }

    /** @return {@code true} si deux symboles sont identiques ou si un Joker est sorti (il fera déjà une paire). */
    private static boolean hasPairOrJoker(Symbol[] s) {
        for (Symbol symbol : s) {
            if (symbol == Symbol.JOKER) return true;
        }
        return s[0] == s[1] || s[1] == s[2] || s[0] == s[2];
    }

    /** @return le poids de tirage de {@code symbol} ce tour (0 s'il est retiré des rouleaux). */
    private static int weight(Symbol symbol, SpinContext spinContext) {
        if (spinContext.isRemoved(symbol)) return 0;
        int base = symbol == Symbol.JOKER ? JOKER_WEIGHT : BASE_WEIGHT;
        return Math.max(0, base + spinContext.getWeightBoost(symbol));
    }

    /** Tire un symbole au hasard selon son poids du tour, Joker compris ou non. */
    private Symbol weightedRandom(SpinContext spinContext, boolean withJoker) {
        int total = 0;
        for (Symbol s : Symbol.values()) {
            if (withJoker || s != Symbol.JOKER) total += weight(s, spinContext);
        }
        if (total <= 0) return Symbol.BAR; // tous les symboles retirés : le plus modeste reste
        int rand = random.nextInt(total);
        for (Symbol s : Symbol.values()) {
            if (!withJoker && s == Symbol.JOKER) continue;
            rand -= weight(s, spinContext);
            if (rand < 0) return s;
        }
        return Symbol.BAR;
    }

}
