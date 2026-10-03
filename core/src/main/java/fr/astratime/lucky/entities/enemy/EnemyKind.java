package fr.astratime.lucky.entities.enemy;

import fr.astratime.lucky.entities.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Les ennemis du jeu et leur façon de jouer : points de vie, défense de base,
 * symboles de leurs rouleaux (et leur poids), composition de leur deck sombre
 * et ordre dans lequel ils jouent leurs cartes.
 * <ul>
 *   <li>{@link #CROUPIER} : le combat de départ, équilibré ;</li>
 *   <li>{@link #GARDIEN} : défense épaisse et Épines, qui renvoient les coups ;</li>
 *   <li>{@link #SANGSUE} : Crocs, qui volent la vie du joueur ;</li>
 *   <li>{@link #BRETTEUR} : Épées et Rage, il frappe de plus en plus fort ;</li>
 *   <li>{@link #COMETE} : le boss du chapitre 1, qui reprend tout.</li>
 * </ul>
 */
public enum EnemyKind {

    CROUPIER("Croupier démoniaque", "Il tient la table. Il frappe. Il se protège. Il se soigne.",
        5_000, 30, 3,
        weights(EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1),
        deck(new int[] {2, 5, 8, 11, 14}, new int[] {2, 5, 8, 11, 14},
            new int[] {2, 5, 8, 11, 14}, new int[] {2, 5, 8, 11, 14}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    GARDIEN("Gardien de la Banque", "Il garde le coffre. Sa défense est épaisse. Ses Épines te renvoient tes coups.",
        10_000, 150, 3,
        weights(EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1, EnemySymbol.THORNS, 1),
        deck(new int[] {4, 9}, new int[] {5, 11},
            new int[] {2, 5, 7, 9, 11, 12, 13, 14}, new int[] {3, 8, 12}),
        List.of(Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.PIQUE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.PIQUE)),

    SANGSUE("Sangsue du Tapis", "Elle colle au feutre. Chaque morsure la soigne. Garde ton bouclier levé.",
        10_000, 30, 3,
        weights(EnemySymbol.FANG, 2, EnemySymbol.SHIELD, 2, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {2, 5, 8, 11, 13, 14},
            new int[] {4, 10}, new int[] {6, 12}),
        List.of(Card.Suit.PIQUE, Card.Suit.TREFLE, Card.Suit.COEUR, Card.Suit.CARREAU),
        List.of(Card.Suit.COEUR, Card.Suit.PIQUE, Card.Suit.TREFLE, Card.Suit.CARREAU)),

    BRETTEUR("Bretteur à la Mise", "Il ne pare jamais. Il frappe. Chaque Rage le rend plus fort.",
        10_000, 0, 3,
        weights(EnemySymbol.SWORD, 1, EnemySymbol.RAGE, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {5, 11},
            new int[] {3, 9}, new int[] {4, 8, 13}),
        List.of(Card.Suit.PIQUE, Card.Suit.TREFLE, Card.Suit.CARREAU, Card.Suit.COEUR),
        List.of(Card.Suit.PIQUE, Card.Suit.TREFLE, Card.Suit.CARREAU, Card.Suit.COEUR)),

    COMETE("Comète Dorée", "Elle est tombée du ciel. Elle a créé la règle. Elle joue tous les jeux à la fois.",
        20_000, 100, 3,
        weights(EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 2, EnemySymbol.THORNS, 1,
            EnemySymbol.FANG, 1, EnemySymbol.RAGE, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12, 14},
            new int[] {5, 9, 13, 14}, new int[] {4, 8, 12}),
        List.of(Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.PIQUE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE));

    /** Les trois adversaires possibles du 2e combat d'un chapitre. */
    public static final List<EnemyKind> CHALLENGERS = List.of(GARDIEN, SANGSUE, BRETTEUR);

    private final String                     displayName;
    private final String                     description;
    private final int                        maxHp;
    private final int                        baseDefense;
    private final int                        playsPerTurn;
    private final Map<EnemySymbol, Integer>  weights;
    private final Map<Card.Suit, int[]>      deck;
    private final List<Card.Suit>            priority;
    private final List<Card.Suit>            lowHpPriority;

    EnemyKind(String displayName, String description, int maxHp, int baseDefense, int playsPerTurn,
              Map<EnemySymbol, Integer> weights, Map<Card.Suit, int[]> deck, List<Card.Suit> priority,
              List<Card.Suit> lowHpPriority) {
        this.displayName  = displayName;
        this.description  = description;
        this.maxHp        = maxHp;
        this.baseDefense  = baseDefense;
        this.playsPerTurn = playsPerTurn;
        this.weights      = Collections.unmodifiableMap(weights);
        this.deck         = deck;
        this.priority     = priority;
        this.lowHpPriority = lowHpPriority;
    }

    /** @return le nom affiché (ex : "Gardien de la Banque"). */
    public String getDisplayName() { return displayName; }
    /** @return sa présentation, en quelques phrases courtes. */
    public String getDescription() { return description; }
    /** @return ses points de vie maximum. */
    public int getMaxHp() { return maxHp; }
    /** @return sa défense de base, reformée à chacun de ses tours. */
    public int getBaseDefense() { return baseDefense; }
    /** @return les cartes qu'il joue à chaque tour, parmi celles piochées. */
    public int getPlaysPerTurn() { return playsPerTurn; }
    /** @return {@code true} pour le boss d'un chapitre. */
    public boolean isBoss() { return this == COMETE; }

    /** @return les symboles de ses rouleaux et leur poids (dans l'ordre de tirage). */
    public Map<EnemySymbol, Integer> getWeights() { return weights; }

    /** @return les symboles qui peuvent sortir sur ses rouleaux. */
    public List<EnemySymbol> getSymbols() { return List.copyOf(weights.keySet()); }

    /** @return les couleurs qu'il joue en priorité quand il est en forme (la première d'abord). */
    public List<Card.Suit> getPriority() { return priority; }

    /** @return les couleurs qu'il joue en priorité quand sa vie est basse. */
    public List<Card.Suit> getLowHpPriority() { return lowHpPriority; }

    /** @return un deck neuf, aux cartes de sa composition. */
    public List<Card> createDeck() {
        List<Card> cards = new ArrayList<>();
        for (Map.Entry<Card.Suit, int[]> entry : deck.entrySet()) {
            for (int rank : entry.getValue()) cards.add(EnemyCards.card(entry.getKey(), rank));
        }
        return cards;
    }

    /** @return la phrase qui résume ses rouleaux, pour son infobulle (ex : "Épée, Bouclier x2, Épines x2"). */
    public String describeReels() {
        List<String> parts = new ArrayList<>();
        int total = weights.values().stream().mapToInt(Integer::intValue).sum();
        for (Map.Entry<EnemySymbol, Integer> entry : weights.entrySet()) {
            parts.add(entry.getKey().getDisplayName() + " " + Math.round(100f * entry.getValue() / total) + " %");
        }
        return String.join(", ", parts);
    }

    private static Map<EnemySymbol, Integer> weights(Object... pairs) {
        Map<EnemySymbol, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) map.put((EnemySymbol) pairs[i], (Integer) pairs[i + 1]);
        return map;
    }

    /** Rangs des cartes de chaque couleur, dans l'ordre Pique, Cœur, Carreau, Trèfle. */
    private static Map<Card.Suit, int[]> deck(int[] pique, int[] coeur, int[] carreau, int[] trefle) {
        Map<Card.Suit, int[]> map = new EnumMap<>(Card.Suit.class);
        map.put(Card.Suit.PIQUE, pique);
        map.put(Card.Suit.COEUR, coeur);
        map.put(Card.Suit.CARREAU, carreau);
        map.put(Card.Suit.TREFLE, trefle);
        return map;
    }
}
