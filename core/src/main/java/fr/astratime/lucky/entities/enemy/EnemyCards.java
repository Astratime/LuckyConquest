package fr.astratime.lucky.entities.enemy;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.CardFamily;

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
 * Le Rouleau interdit ({@link #forbiddenReel()}), sans couleur, n'est que dans le
 * deck de l'Éclat Originel : il bloque un rouleau du joueur à son prochain tirage.
 * Les bonus d'attaque et de défense grandissent avec la force de l'ennemi
 * ({@link EnemyKind#getPower()}), comme ses symboles.
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

    /** Identifiant du Rouleau interdit. */
    public static final String FORBIDDEN_REEL_ID = "enemy_forbidden_reel";

    /** @return le Rouleau interdit : joué, il bloque un rouleau du joueur à son prochain tirage. */
    public static Card forbiddenReel() {
        return new Card(FORBIDDEN_REEL_ID, "Rouleau interdit", "cards/dark/FORBIDDEN_REEL.png", List.of(), null, 1);
    }

    /** @return {@code true} pour le Rouleau interdit. */
    public static boolean isForbiddenReel(Card card) { return FORBIDDEN_REEL_ID.equals(card.getId()); }

    /** @return la carte sombre de rang {@code rank} et de couleur {@code suit}. */
    public static Card card(Card.Suit suit, int rank) {
        String name = rank + " de " + suitName(suit);
        return new Card("enemy_" + suit.cardId(rank), name, "cards/dark/" + rank + "-" + letter(suit) + ".png",
            List.of(), suit, rank);
    }

    /**
     * L'Ombre du Joueur : le deck du joueur, carte pour carte, en cartes sombres.
     * Une carte de couleur garde sa couleur et son rang (l'As vaut 14) ; une
     * carte spéciale devient un 10 de la couleur de sa famille : Attaque en
     * Pique, Gains en Trèfle, Pioche en Carreau, les autres en Cœur.
     *
     * @return le deck sombre copié sur {@code playerCards}
     */
    public static List<Card> shadowDeck(List<Card> playerCards) {
        List<Card> cards = new ArrayList<>();
        for (Card card : playerCards) {
            if (card.getSuit() != null) {
                cards.add(card(card.getSuit(), card.getRank() == 1 ? MAX_RANK : Math.clamp(card.getRank(), 2, MAX_RANK)));
            } else {
                Card.Suit suit = CardFamily.ATTAQUE.contains(card) ? Card.Suit.PIQUE
                    : CardFamily.GAINS.contains(card) ? Card.Suit.TREFLE
                    : CardFamily.PIOCHE.contains(card) ? Card.Suit.CARREAU : Card.Suit.COEUR;
                cards.add(card(suit, SHADOW_SPECIAL_RANK));
            }
        }
        return cards.isEmpty() ? starterDeck() : cards;
    }

    /** Rang des cartes spéciales du joueur, copiées par l'Ombre du Joueur. */
    static final int SHADOW_SPECIAL_RANK = 10;

    /** @return l'attaque ajoutée à chaque Épée et chaque Croc par une carte Pique. */
    public static int swordBonus(Card card)   { return scaled(card, SWORD_MIN, SWORD_MAX); }
    /** @return le soin (en % des PV max) ajouté à chaque Potion par une carte Cœur. */
    public static int healBonus(Card card)    { return scaled(card, HEAL_MIN, HEAL_MAX); }
    /** @return la défense ajoutée à chaque Bouclier par une carte Carreau. */
    public static int shieldBonus(Card card)  { return scaled(card, SHIELD_MIN, SHIELD_MAX); }
    /** @return le pourcentage de chance ajouté à un symbole par une carte Trèfle. */
    public static int luckBonus(Card card)    { return scaled(card, LUCK_MIN, LUCK_MAX); }

    /** @return la description de l'effet de la carte, pour son infobulle. */
    public static String describe(Card card) { return describe(card, EnemyKind.CROUPIER); }

    /** @return la description de l'effet de la carte chez un ennemi {@code kind} (bonus renforcés par sa force). */
    public static String describe(Card card, EnemyKind kind) {
        if (card.getSuit() == null) return "Bloque un de tes rouleaux à ton prochain tirage. Pas de Bingo possible";
        return switch (card.getSuit()) {
            case PIQUE   -> "Épées et Crocs : attaque +" + kind.empowered(kind.swordBonus(card));
            case COEUR   -> "Potions : soin +" + healBonus(card) + " % des PV max";
            case CARREAU -> "Boucliers : défense +" + kind.empowered(kind.shieldBonus(card));
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
