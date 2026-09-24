package fr.astratime.lucky.entities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Combinaisons de poker vérifiées par les cartes combo, sur les cartes à
 * suite jouées pendant le tour (les cartes spéciales, sans suite ni rang
 * pertinent, sont ignorées).
 */
public enum Combo {

    /** Trois rangs qui se suivent (l'As compte avant le 2 ou après le Roi). */
    SUITE("SUITE"),
    /** Au moins trois cartes, toutes de la même suite. */
    COULEUR("COULEUR"),
    /** Trois cartes du même rang. */
    BRELAN("BRELAN"),
    /** Un brelan et une paire d'un autre rang. */
    FULL("FULL");

    /** Nombre de cartes minimum pour une suite, une couleur ou un brelan. */
    private static final int MIN_CARDS = 3;
    private static final int ACE  = 1;
    private static final int KING = 13;

    private final String displayName;

    Combo(String displayName) {
        this.displayName = displayName;
    }

    /** @return le nom affiché de la combinaison. */
    public String getDisplayName() { return displayName; }

    /** @return {@code true} si les cartes à suite de {@code played} forment cette combinaison. */
    public boolean matches(List<Card> played) {
        List<Card> suited = played.stream().filter(card -> card.getSuit() != null).toList();
        Map<Integer, Integer> byRank = new HashMap<>();
        suited.forEach(card -> byRank.merge(card.getRank(), 1, Integer::sum));

        return switch (this) {
            case SUITE   -> hasStraight(byRank.keySet());
            case COULEUR -> suited.size() >= MIN_CARDS
                && suited.stream().map(Card::getSuit).distinct().count() == 1;
            case BRELAN  -> byRank.values().stream().anyMatch(count -> count >= MIN_CARDS);
            case FULL    -> hasFullHouse(byRank);
        };
    }

    private static boolean hasStraight(Set<Integer> ranks) {
        Set<Integer> all = new HashSet<>(ranks);
        if (all.contains(ACE)) all.add(KING + 1); // l'As après le Roi
        for (int rank : all) {
            if (all.contains(rank + 1) && all.contains(rank + 2)) return true;
        }
        return false;
    }

    private static boolean hasFullHouse(Map<Integer, Integer> byRank) {
        for (Map.Entry<Integer, Integer> three : byRank.entrySet()) {
            if (three.getValue() < MIN_CARDS) continue;
            for (Map.Entry<Integer, Integer> pair : byRank.entrySet()) {
                if (!pair.getKey().equals(three.getKey()) && pair.getValue() >= 2) return true;
            }
        }
        return false;
    }
}
