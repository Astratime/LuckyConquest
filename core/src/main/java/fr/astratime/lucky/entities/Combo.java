package fr.astratime.lucky.entities;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Combinaisons de poker formées par les cartes à suite jouées pendant le tour
 * (les cartes spéciales, sans suite ni rang pertinent, sont ignorées). Elles
 * sont vérifiées automatiquement au lancer de la machine : chaque combinaison
 * formée ({@link #formed}) apporte son multiplicateur, et ceux de toutes
 * les combinaisons formées s'additionnent avant de multiplier les gains et
 * l'attaque du tirage (ex : Suite et Paire, 5 + 4.5 = x9.5). Une paire
 * contenue dans un brelan ne compte pas en plus ; une paire d'un autre rang,
 * si (Brelan et Paire, x10.5). L'ordre dans lequel les cartes sont jouées ne
 * compte pas.
 *
 * Elles poussent le joueur à varier les cartes qu'il pose, plutôt que de ne
 * jouer que les plus fortes. Leurs multiplicateurs sont hauts (+3 depuis
 * l'Exploration) pour que les cartes à suite gardent leur place dans le deck
 * face aux cartes débloquées dans les donjons, qui n'ont pas de suite.
 */
public enum Combo {

    // Déclarées de la plus forte à la plus faible.

    /** Trois cartes du même rang. */
    BRELAN("BRELAN", 6f, "3 cartes du même rang"),
    /** Au moins trois cartes, toutes de la même suite. */
    COULEUR("COULEUR", 5f, "3 cartes ou plus de la même suite"),
    /** Trois rangs qui se suivent (l'As compte avant le 2 ou après le Roi). */
    SUITE("SUITE", 5f, "3 rangs qui se suivent"),
    /** Deux cartes du même rang. */
    PAIRE("PAIRE", 4.5f, "2 cartes du même rang");

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
    public String formatFactor() { return formatFactor(factor); }

    /** @return la règle, en quelques mots (ex : "2 cartes du même rang"). */
    public String getRule() { return rule; }

    /** @return {@code true} si, formée, elle remplit la jauge de chaque carte jouée (Couleur, Suite). */
    public boolean fillsGauges() { return this == COULEUR || this == SUITE; }

    /**
     * @return les combinaisons formées par les cartes de {@code played}, de la
     *         plus forte à la plus faible ; la Paire n'est comptée avec un Brelan
     *         que si elle est d'un autre rang
     */
    public static List<Combo> formed(List<Card> played) {
        EnumSet<Combo> formed = EnumSet.noneOf(Combo.class);
        for (Combo combo : values()) {
            if (combo.matches(played)) formed.add(combo);
        }
        if (formed.contains(BRELAN)) {
            long ranksWithPair = countByRank(played).values().stream().filter(count -> count >= 2).count();
            if (ranksWithPair < 2) formed.remove(PAIRE); // la seule paire est celle du brelan
        }
        return List.copyOf(formed);
    }

    /** @return la somme des multiplicateurs de {@code combos} (1 s'il n'y en a aucune). */
    public static float totalFactor(Collection<Combo> combos) {
        if (combos.isEmpty()) return 1f;
        float total = 0f;
        for (Combo combo : combos) total += combo.factor;
        return total;
    }

    /** @return {@code factor} sans décimale inutile (ex : "2", "1.5"). */
    public static String formatFactor(float factor) {
        return factor == (int) factor ? String.valueOf((int) factor) : String.valueOf(factor);
    }

    /** @return {@code true} si les cartes à suite de {@code played} forment cette combinaison. */
    public boolean matches(List<Card> played) {
        List<Card> suited = suited(played);
        Map<Integer, Integer> byRank = countByRank(played);

        return switch (this) {
            case PAIRE   -> byRank.values().stream().anyMatch(count -> count >= 2);
            case SUITE   -> hasStraight(byRank.keySet());
            case COULEUR -> suited.size() >= MIN_CARDS
                && suited.stream().map(Card::getSuit).distinct().count() == 1;
            case BRELAN  -> byRank.values().stream().anyMatch(count -> count >= MIN_CARDS);
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

    /** @return les cartes à suite de {@code played} (les cartes spéciales n'ont pas de rang pertinent). */
    private static List<Card> suited(List<Card> played) {
        return played.stream().filter(card -> card.getSuit() != null).toList();
    }

    /** @return le nombre de cartes à suite de chaque rang parmi {@code played}. */
    private static Map<Integer, Integer> countByRank(List<Card> played) {
        Map<Integer, Integer> byRank = new HashMap<>();
        suited(played).forEach(card -> byRank.merge(card.getRank(), 1, Integer::sum));
        return byRank;
    }
}
