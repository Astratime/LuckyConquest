package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.context.SpinContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
 *
 * Les symboles qui peuvent sortir sont ceux de la machine du joueur
 * ({@link Symbol#MACHINE_SIZE} rouleaux choisis à la Table du croupier ; les
 * 11 classiques par défaut), plus le Joker.
 */
public class SlotMachine {

    /** Nombre de symboles tirés à chaque spin (un par rouleau). */
    public static final int SYMBOL_COUNT = 3;
    /** Rouleaux au plus, avec celui de la Machine en surchauffe. */
    public static final int MAX_SYMBOL_COUNT = 4;
    /** Symboles identiques qui font un jackpot (Bingo). */
    public static final int JACKPOT_COUNT = 3;

    /** Poids de base attribué à chaque symbole avant application des boosts du tour. */
    private static final int BASE_WEIGHT  = 10;
    /** Poids de base du Joker, plus rare que les autres symboles. */
    private static final int JOKER_WEIGHT = 4;

    private final Random       random;
    /** Symboles de la machine, sans le Joker. */
    private final List<Symbol> reels;

    /** Machine aux 11 rouleaux classiques. */
    public SlotMachine() {
        this(Symbol.classicReels());
    }

    /** @param reels symboles de la machine (le Joker s'y ajoute toujours) */
    public SlotMachine(List<Symbol> reels) {
        this(reels, new Random());
    }

    /** @param random source d'aléatoire (ex : graine fixe pour des tests reproductibles) */
    SlotMachine(Random random) {
        this(Symbol.classicReels(), random);
    }

    SlotMachine(List<Symbol> reels, Random random) {
        this.reels  = List.copyOf(reels);
        this.random = random;
    }

    /** @return les symboles de la machine, sans le Joker. */
    public List<Symbol> getReels() { return reels; }

    /**
     * Tire un symbole par rouleau ({@link SpinContext#getReelCount()} : trois,
     * quatre avec la Machine en surchauffe), indépendamment, selon les poids de
     * base plus les boosts du {@code spinContext}, sans les symboles retirés.
     * Un jackpot garanti (Bingo) donne partout le même symbole (celui imposé par
     * la carte, sinon tiré au hasard, jamais le Joker) ; sinon, les rouleaux
     * imposés (Rouleau truqué, Rouleau fantôme) prennent leur symbole, puis
     * l'Aimant peut transformer un tirage sans paire en paire. Un rouleau bloqué
     * (Rouleau interdit de l'ennemi) reste vide ({@code null}) : pas de Bingo possible.
     *
     * @param spinContext modificateurs du spin pour ce tour
     * @return les symboles arrêtés sur les rouleaux (Jokers compris)
     */
    public Symbol[] spin(SpinContext spinContext) {
        Symbol[] result = new Symbol[spinContext.getReelCount()];
        if (spinContext.isJackpotForced()) {
            Symbol symbol = spinContext.getJackpotSymbol();
            Arrays.fill(result, symbol != null ? symbol : weightedRandom(spinContext, false));
            return block(result, spinContext);
        }
        for (int i = 0; i < result.length; i++) {
            result[i] = weightedRandom(spinContext, true);
        }
        spinContext.getForcedReels().forEach((reel, symbol) -> {
            if (reel < result.length) result[reel] = symbol;
        });
        block(result, spinContext);
        if (!hasPairOrJoker(result) && random.nextFloat() < spinContext.getPairChance()) {
            List<Integer> open = new ArrayList<>();
            for (int i = 0; i < result.length; i++) {
                if (result[i] != null) open.add(i);
            }
            if (open.size() >= 2) {
                int from = open.remove(random.nextInt(open.size()));
                int to   = open.get(random.nextInt(open.size()));
                Map<Integer, Symbol> forced = spinContext.getForcedReels();
                if (forced.containsKey(to)) { // un rouleau imposé garde son symbole : c'est lui que l'autre copie
                    int swap = to;
                    to   = from;
                    from = swap;
                }
                if (!forced.containsKey(to)) result[to] = result[from];
            }
        }
        return result;
    }

    /** Vide les rouleaux bloqués du tirage, s'il y en a. @return {@code result} */
    private static Symbol[] block(Symbol[] result, SpinContext spinContext) {
        for (int blocked : spinContext.getBlockedReels()) {
            if (blocked < result.length) result[blocked] = null;
        }
        return result;
    }

    /**
     * Remplace chaque Joker par le symbole qui lui rapporte le plus : celui qui
     * sort le plus souvent parmi les autres (il complète le jackpot ou la paire ;
     * à égalité, l'un d'eux au hasard) ; si tous sont des Jokers, ils
     * deviennent un jackpot d'un symbole tiré au hasard.
     *
     * @param drawn       symboles arrêtés sur les rouleaux (voir {@link #spin})
     * @param spinContext modificateurs du tour (symboles retirés, boosts)
     * @return une copie de {@code drawn} dont les Jokers sont remplacés
     */
    public Symbol[] resolveJokers(Symbol[] drawn, SpinContext spinContext) {
        Symbol[] resolved = drawn.clone();
        Map<Symbol, Integer> counts = new EnumMap<>(Symbol.class);
        int jokers = 0;
        for (Symbol symbol : drawn) {
            if (symbol == Symbol.JOKER) jokers++;
            else if (symbol != null) counts.merge(symbol, 1, Integer::sum); // un rouleau bloqué ne compte pas
        }
        if (jokers == 0) return resolved;

        Symbol value;
        if (counts.isEmpty()) {
            value = weightedRandom(spinContext, false);
        } else {
            int best = counts.values().stream().max(Integer::compare).orElse(0);
            List<Symbol> top = new ArrayList<>();
            for (Symbol symbol : drawn) { // dans l'ordre des rouleaux
                if (symbol != null && symbol != Symbol.JOKER && counts.get(symbol) == best && !top.contains(symbol)) {
                    top.add(symbol);
                }
            }
            value = top.size() == 1 ? top.get(0) : top.get(random.nextInt(top.size()));
        }
        for (int i = 0; i < resolved.length; i++) {
            if (resolved[i] == Symbol.JOKER) resolved[i] = value;
        }
        return resolved;
    }

    /**
     * @return le symbole sorti au moins {@link #JACKPOT_COUNT} fois (jackpot),
     *         ou {@code null} s'il n'y en a pas
     */
    public static Symbol jackpotSymbol(Symbol[] s) {
        for (Symbol symbol : s) {
            if (symbol == null) continue;
            int count = 0;
            for (Symbol other : s) {
                if (other == symbol) count++;
            }
            if (count >= JACKPOT_COUNT) return symbol;
        }
        return null;
    }

    /** @return {@code true} si deux symboles sont identiques ou si un Joker est sorti (il fera déjà une paire). */
    private static boolean hasPairOrJoker(Symbol[] s) {
        for (Symbol symbol : s) {
            if (symbol == Symbol.JOKER) return true;
        }
        return hasPair(s);
    }

    /** @return {@code true} si deux symboles (non vides) sont identiques. */
    public static boolean hasPair(Symbol[] s) {
        for (int i = 0; i < s.length; i++) {
            for (int j = i + 1; j < s.length; j++) {
                if (s[i] != null && s[i] == s[j]) return true;
            }
        }
        return false;
    }

    /** @return le poids de tirage de {@code symbol} ce tour (0 s'il est retiré des rouleaux). */
    private static int weight(Symbol symbol, SpinContext spinContext) {
        if (spinContext.isRemoved(symbol)) return 0;
        int base = symbol == Symbol.JOKER ? JOKER_WEIGHT : BASE_WEIGHT;
        return Math.max(0, base + spinContext.getWeightBoost(symbol));
    }

    /** Tire un symbole au hasard selon son poids du tour, Joker compris ou non. */
    private Symbol weightedRandom(SpinContext spinContext, boolean withJoker) {
        List<Symbol> candidates = new ArrayList<>(reels);
        if (withJoker) candidates.add(Symbol.JOKER);
        int total = 0;
        for (Symbol s : candidates) total += weight(s, spinContext);
        if (total <= 0) return reels.get(0); // tous les symboles retirés : le premier reste
        int rand = random.nextInt(total);
        for (Symbol s : candidates) {
            rand -= weight(s, spinContext);
            if (rand < 0) return s;
        }
        return reels.get(0);
    }

}
