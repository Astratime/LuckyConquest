package fr.astratime.lucky.entities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Combinaisons de poker formées par les cartes à suite jouées pendant le tour
 * (les cartes spéciales, sans suite ni rang pertinent, sont ignorées). Elles
 * sont vérifiées automatiquement au lancer de la machine : la meilleure
 * combinaison formée ({@link #best}) multiplie les gains et l'attaque du
 * tirage. L'ordre dans lequel les cartes sont jouées ne compte pas.
 *
 * Elles poussent le joueur à varier les cartes qu'il pose, plutôt que de ne
 * jouer que les plus fortes.
 */
public enum Combo {

    // Déclarées de la plus forte à la plus faible : best() retient la première formée.

    /** Un brelan et une paire d'un autre rang. */
    FULL("FULL", 5f, "un brelan et une paire"),
    /** Trois cartes du même rang. */
    BRELAN("BRELAN", 3f, "3 cartes du même rang"),
    /** Au moins trois cartes, toutes de la même suite. */
    COULEUR("COULEUR", 2f, "3 cartes ou plus de la même suite"),
    /** Trois rangs qui se suivent (l'As compte avant le 2 ou après le Roi). */
    SUITE("SUITE", 2f, "3 rangs qui se suivent"),
    /** Deux cartes du même rang. */
    PAIRE("PAIRE", 1.5f, "2 cartes du même rang");

    /** Nombre de cartes minimum pour une suite, une couleur ou un brelan. */
    private static final int MIN_CARDS = 3;
    private static final int ACE  = 1;
    private static final int KING = 13;

    private final String displayName;
    private final float  factor;
    private final String rule;

    Combo(String displayName, float factor, String rule) {
        this.displayName = displayName;
        this.factor      = factor;
        this.rule        = rule;
    }

    /** @return le nom affiché de la combinaison. */
    public String getDisplayName() { return displayName; }

    /** @return le multiplicateur des gains et de l'attaque quand elle est formée. */
    public float getFactor() { return factor; }

    /** @return le multiplicateur, sans décimale inutile (ex : "2", "1.5"). */
    public String formatFactor() {
        return factor == (int) factor ? String.valueOf((int) factor) : String.valueOf(factor);
    }

    /** @return la règle, en quelques mots (ex : "2 cartes du même rang"). */
    public String getRule() { return rule; }

    /** @return {@code true} si, formée, elle remplit la jauge de chaque carte jouée (Couleur, Suite). */
    public boolean fillsGauges() { return this == COULEUR || this == SUITE; }

    /** @return la plus forte combinaison formée par les cartes de {@code played}, si elles en forment une. */
    public static Optional<Combo> best(List<Card> played) {
        for (Combo combo : values()) {
            if (combo.matches(played)) return Optional.of(combo);
        }
        return Optional.empty();
    }

    /** @return {@code true} si les cartes à suite de {@code played} forment cette combinaison. */
    public boolean matches(List<Card> played) {
        List<Card> suited = played.stream().filter(card -> card.getSuit() != null).toList();
        Map<Integer, Integer> byRank = new HashMap<>();
        suited.forEach(card -> byRank.merge(card.getRank(), 1, Integer::sum));

        return switch (this) {
            case PAIRE   -> byRank.values().stream().anyMatch(count -> count >= 2);
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
