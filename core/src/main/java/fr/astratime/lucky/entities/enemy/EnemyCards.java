package fr.astratime.lucky.entities.enemy;

import fr.astratime.lucky.entities.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * Cartes de l'ennemi (jeu sombre, cards/dark/) : leur effet dépend de leur
 * couleur, et grandit avec leur rang, du rang 1 au rang {@link #MAX_RANK} :
 * <ul>
 *   <li>Pique : chaque Épée et chaque Croc attaquent de +5 à +10 ;</li>
 *   <li>Cœur : chaque Potion soigne de +10 % à +20 % des PV max en plus ;</li>
 *   <li>Carreau : chaque Bouclier donne de +300 à +600 de défense en plus ;</li>
 *   <li>Trèfle : un symbole tiré au hasard devient de 10 % à 50 % plus probable.</li>
 * </ul>
 */
public final class EnemyCards {

    /** Rang le plus fort du jeu sombre. */
    public static final int MAX_RANK = 14;
    /** Rangs du deck de départ, dans chaque couleur (5 cartes par couleur, 20 en tout). */
    static final int[] STARTER_RANKS = {2, 5, 8, 11, 14};

    private static final int SWORD_MIN  = 5,   SWORD_MAX  = 10;
    private static final int HEAL_MIN   = 10,  HEAL_MAX   = 20;
    private static final int SHIELD_MIN = 300, SHIELD_MAX = 600;
    private static final int LUCK_MIN   = 10,  LUCK_MAX   = 50;

    private EnemyCards() {}

    /** @return le deck de départ de l'ennemi : 5 cartes par couleur, sans Joker. */
    public static List<Card> starterDeck() {
        List<Card> cards = new ArrayList<>();
        for (Card.Suit suit : Card.Suit.values()) {
            for (int rank : STARTER_RANKS) cards.add(card(suit, rank));
        }
        return cards;
    }

    /** @return la carte sombre de rang {@code rank} et de couleur {@code suit}. */
    public static Card card(Card.Suit suit, int rank) {
        String name = rank + " de " + suitName(suit);
        return new Card("enemy_" + suit.cardId(rank), name, "cards/dark/" + rank + "-" + letter(suit) + ".png",
            List.of(), suit, rank);
    }

    /** @return l'attaque ajoutée à chaque Épée et chaque Croc par une carte Pique. */
    public static int swordBonus(Card card)   { return scaled(card, SWORD_MIN, SWORD_MAX); }
    /** @return le soin (en % des PV max) ajouté à chaque Potion par une carte Cœur. */
    public static int healBonus(Card card)    { return scaled(card, HEAL_MIN, HEAL_MAX); }
    /** @return la défense ajoutée à chaque Bouclier par une carte Carreau. */
    public static int shieldBonus(Card card)  { return scaled(card, SHIELD_MIN, SHIELD_MAX); }
    /** @return le pourcentage de chance ajouté à un symbole par une carte Trèfle. */
    public static int luckBonus(Card card)    { return scaled(card, LUCK_MIN, LUCK_MAX); }

    /** @return la description de l'effet de la carte, pour son infobulle. */
    public static String describe(Card card) {
        return switch (card.getSuit()) {
            case PIQUE   -> "Épées et Crocs : attaque +" + swordBonus(card);
            case COEUR   -> "Potions : soin +" + healBonus(card) + " % des PV max";
            case CARREAU -> "Boucliers : défense +" + shieldBonus(card);
            case TREFLE  -> "Un symbole au hasard : chance +" + luckBonus(card) + " %";
        };
    }

    /** @return la valeur de {@code min} (rang 1) à {@code max} (rang {@link #MAX_RANK}), selon le rang. */
    private static int scaled(Card card, int min, int max) {
        int rank = Math.clamp(card.getRank(), 1, MAX_RANK);
        return Math.round(min + (max - min) * (rank - 1) / (float) (MAX_RANK - 1));
    }

    private static String letter(Card.Suit suit) {
        return switch (suit) {
            case PIQUE   -> "P";
            case COEUR   -> "H";
            case CARREAU -> "D";
            case TREFLE  -> "C";
        };
    }

    private static String suitName(Card.Suit suit) {
        return switch (suit) {
            case PIQUE   -> "Pique";
            case COEUR   -> "Coeur";
            case CARREAU -> "Carreau";
            case TREFLE  -> "Trèfle";
        };
    }
}
