package fr.astratime.lucky.entities.enemy;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.CardFamily;
import fr.astratime.lucky.entities.effects.AceOfHeartsEffect;
import fr.astratime.lucky.entities.effects.BingoEffect;
import fr.astratime.lucky.entities.effects.GoldenHeartEffect;
import fr.astratime.lucky.entities.effects.HeartDrainEffect;
import fr.astratime.lucky.entities.effects.TransfusionEffect;
import fr.astratime.lucky.i18n.Lang;

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
 * Les autres cartes sans couleur sont les cartes propres des ennemis ({@link EnemySignature}),
 * et les cartes spéciales du joueur copiées par son Ombre ({@link #shadowDeck}).
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
        String name = Lang.f("{0} de {1}", rank, suitName(suit));
        return new Card("enemy_" + suit.cardId(rank), name, "cards/dark/" + rank + "-" + letter(suit) + ".png",
            List.of(), suit, rank);
    }

    /** Préfixe des As volés par le Pilleur (copies des cartes achetées à l'échoppe). */
    static final String LOOT_PREFIX = "enemy_loot_";

    /** @return l'As sombre de {@code suit} que le Pilleur garde d'une carte achetée à l'échoppe. */
    public static Card loot(Card.Suit suit) {
        Card ace = card(suit, MAX_RANK);
        return new Card(LOOT_PREFIX + suit.cardId(MAX_RANK), Lang.f("{0} de {1}", MAX_RANK, suitName(suit)),
            ace.getAssetPath(), List.of(), suit, MAX_RANK);
    }

    /** @return {@code true} pour un As volé par le Pilleur. */
    public static boolean isLoot(Card card) { return card.getId().startsWith(LOOT_PREFIX); }

    /**
     * Rôle d'une carte spéciale du joueur dans le deck de son Ombre : elle agit
     * comme le Reflet, à pleine force.
     */
    public enum ShadowRole {
        /** Une carte d'attaque devient un coup. */
        ATTACK,
        /** Une carte de gains vole ses gains, comme les Intérêts. */
        GAINS,
        /** Une carte Bingo rend son Épée bien plus probable. */
        BINGO,
        /** Une carte de soin la soigne. */
        HEAL,
        /** Les autres deviennent un Bouclier. */
        SHIELD
    }

    /** Préfixe des cartes copiées par l'Ombre du Joueur. */
    public static final String SHADOW_PREFIX = "enemy_shadow_";

    /**
     * L'Ombre du Joueur : le deck du joueur, carte pour carte, en version
     * sombre (même image, même nom). Une carte de couleur garde sa couleur et son
     * rang (l'As vaut 14). Une carte spéciale agit comme le Reflet, à pleine
     * force, selon son rôle ({@link ShadowRole}).
     *
     * @return le deck sombre copié sur {@code playerCards}
     */
    public static List<Card> shadowDeck(List<Card> playerCards) {
        List<Card> cards = new ArrayList<>();
        for (Card card : playerCards) {
            boolean suited = card.getSuit() != null;
            int rank = suited ? (card.getRank() == 1 ? MAX_RANK : Math.clamp(card.getRank(), 2, MAX_RANK)) : SHADOW_SPECIAL_RANK;
            String role = suited ? "suit" : shadowRole(card).name().toLowerCase();
            cards.add(new Card(SHADOW_PREFIX + role + "_" + card.getId(), card.getName(), card.getAssetPath(), List.of(),
                card.getSuit(), rank));
        }
        return cards.isEmpty() ? starterDeck() : cards;
    }

    /** @return le rôle de la carte spéciale {@code card} du joueur, copiée par son Ombre. */
    static ShadowRole shadowRole(Card card) {
        if (CardFamily.ATTAQUE.contains(card)) return ShadowRole.ATTACK;
        if (CardFamily.GAINS.contains(card)) return ShadowRole.GAINS;
        if (card.getEffects().stream().anyMatch(BingoEffect.class::isInstance)) return ShadowRole.BINGO;
        if (card.getEffects().stream().anyMatch(effect -> effect instanceof AceOfHeartsEffect
            || effect instanceof GoldenHeartEffect || effect instanceof HeartDrainEffect
            || effect instanceof TransfusionEffect)) return ShadowRole.HEAL;
        return ShadowRole.SHIELD;
    }

    /** @return {@code true} pour une carte copiée par l'Ombre du Joueur. */
    public static boolean isShadow(Card card) { return card.getId().startsWith(SHADOW_PREFIX); }

    /** @return le rôle de la carte spéciale copiée {@code card}, ou {@code null} (carte de couleur, ou pas de l'Ombre). */
    public static ShadowRole shadowRoleOf(Card card) {
        if (!isShadow(card) || card.getSuit() != null) return null;
        String rest = card.getId().substring(SHADOW_PREFIX.length());
        for (ShadowRole role : ShadowRole.values()) {
            if (rest.startsWith(role.name().toLowerCase() + "_")) return role;
        }
        return ShadowRole.SHIELD;
    }

    /** Chance ajoutée à son Épée par une carte Bingo copiée par l'Ombre, en %. */
    public static final int SHADOW_BINGO_LUCK = 100;

    /** Rang des cartes spéciales du joueur, copiées par l'Ombre du Joueur. */
    public static final int SHADOW_SPECIAL_RANK = 10;

    /** @return l'attaque ajoutée à chaque Épée et chaque Croc par une carte Pique. */
    public static int swordBonus(Card card)   { return scaled(card, SWORD_MIN, SWORD_MAX); }
    /** @return le soin (en % des PV max) ajouté à chaque Potion par une carte Cœur. */
    public static int healBonus(Card card)    { return scaled(card, HEAL_MIN, HEAL_MAX); }
    /** @return la défense ajoutée à chaque Bouclier par une carte Carreau. */
    public static int shieldBonus(Card card)  { return scaled(card, SHIELD_MIN, SHIELD_MAX); }
    /** @return le pourcentage de chance ajouté à un symbole par une carte Trèfle. */
    public static int luckBonus(Card card)    { return scaled(card, LUCK_MIN, LUCK_MAX); }

    /** @return la description de l'effet de la carte, pour son infobulle. */
    public static String describe(Card card) { return describe(card, EnemyKind.CROUPIER, false); }

    /** @return la description de l'effet de la carte chez un ennemi {@code kind} (bonus renforcés par sa force). */
    public static String describe(Card card, EnemyKind kind, boolean hard) {
        EnemySignature signature = EnemySignature.of(card);
        if (signature != null) return signature.getDescription();
        ShadowRole role = shadowRoleOf(card);
        if (role != null) return describe(role, kind, hard);
        if (card.getSuit() == null) return Lang.t("Bloque un de tes rouleaux à ton prochain tirage. Pas de Bingo possible");
        return switch (card.getSuit()) {
            case PIQUE   -> Lang.f("Épées et Crocs : attaque +{0}", kind.hurt(kind.swordBonus(card), hard));
            case COEUR   -> Lang.f("Potions : soin +{0} % des PV max", healBonus(card));
            case CARREAU -> Lang.f("Boucliers : défense +{0}", kind.empowered(kind.shieldBonus(card), hard));
            case TREFLE  -> Lang.f("Un symbole au hasard : chance +{0} %", luckBonus(card));
        };
    }

    /** @return l'effet d'une carte spéciale du joueur copiée par son Ombre, de rôle {@code role}. */
    private static String describe(ShadowRole role, EnemyKind kind, boolean hard) {
        Card ten = card(Card.Suit.PIQUE, SHADOW_SPECIAL_RANK);
        return switch (role) {
            case ATTACK -> Lang.f("Ta carte, retournée contre toi : attaque de {0}",
                kind.hurt(kind.swordDamage() + kind.swordBonus(ten), hard));
            case GAINS  -> Lang.f("Ta carte, retournée contre toi : elle prend {0} % de tes gains", kind.interestPercent(hard));
            case BINGO  -> Lang.f("Ta carte, retournée contre toi : son Épée devient {0} % plus probable", SHADOW_BINGO_LUCK);
            case HEAL   -> Lang.f("Ta carte, retournée contre toi : elle se soigne de {0} % de ses PV max",
                Math.round(EnemyKind.cappedHeal(kind.potionPercent(healBonus(ten), hard))));
            case SHIELD -> Lang.f("Ta carte, retournée contre toi : défense +{0}",
                kind.empowered(kind.shieldDefense() + kind.shieldBonus(ten), hard));
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

    static String suitName(Card.Suit suit) {
        return switch (suit) {
            case PIQUE   -> Lang.t("Pique");
            case COEUR   -> Lang.t("Coeur");
            case CARREAU -> Lang.t("Carreau");
            case TREFLE  -> Lang.t("Trèfle");
        };
    }
}
