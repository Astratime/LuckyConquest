package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.Symbol;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Rouleaux vendus à la boutique (onglet « Rouleau ») et leur prix, en pièces.
 * Un rouleau acheté peut ensuite entrer dans la machine du joueur, à la Table
 * du croupier. Les 11 rouleaux classiques sont possédés dès le départ.
 */
public final class ReelShop {

    private static final Map<Symbol, Long> PRICES = new LinkedHashMap<>();

    static {
        PRICES.put(Symbol.HORSESHOE, 1_500_000L);
        PRICES.put(Symbol.ECU,       1_500_000L);
        PRICES.put(Symbol.SWORD,     3_000_000L);
        PRICES.put(Symbol.HEART,     3_000_000L);
        PRICES.put(Symbol.DIE,       4_000_000L);
        PRICES.put(Symbol.STAR,      5_000_000L);
        PRICES.put(Symbol.BOMB,      6_000_000L);
        PRICES.put(Symbol.CROWN,     10_000_000L);
    }

    /** @return les rouleaux en vente et leur prix, dans l'ordre d'affichage. */
    public static Map<Symbol, Long> getPrices() { return Collections.unmodifiableMap(PRICES); }

    /**
     * @return la description d'un rouleau, en phrases courtes, pour la boutique
     *         et la Table du croupier
     */
    public static String describe(Symbol symbol) {
        return switch (symbol) {
            case DOUBLE_BAR    -> "Attaque 20.";
            case CHERRY        -> "Attaque 15.";
            case SEVEN         -> "Attaque 30.";
            case BAR           -> "Attaque 10.";
            case GRAPE         -> "Bouclier 8.";
            case BELL          -> "Gains 8.";
            case DIAMOND       -> "Bouclier 20.";
            case TRIPLE_CHERRY -> "Attaque 45.";
            case TRIPLE_SEVEN  -> "Attaque 90.";
            case GOLD_BAR      -> "Gains 25.";
            case WATERMELON    -> "Gains 12.";
            case HORSESHOE     -> "Gains 40.";
            case ECU           -> "Bouclier 40.";
            case SWORD         -> "Attaque 70. Ignore la défense.";
            case HEART         -> "Soigne 10 % des PV max. Le surplus remplit le Sang.";
            case DIE           -> "Attaque de 1 à 250, au hasard.";
            case STAR          -> "Attaque 30. Bouclier 30. Gains 30.";
            case BOMB          -> "Attaque 200. Tu perds 5 % de tes PV max.";
            case CROWN         -> "Gains 100.";
            case NUGGET        -> "Attaque 40. Gains 60. Gagné en vidant les Mines d'Or.";
            case JOKER         -> "Compte comme n'importe quel symbole.";
        };
    }

    private ReelShop() { }
}
