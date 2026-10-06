package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.i18n.Lang;

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
            case DOUBLE_BAR    -> Lang.t("Attaque 20.");
            case CHERRY        -> Lang.t("Attaque 15.");
            case SEVEN         -> Lang.t("Attaque 30.");
            case BAR           -> Lang.t("Attaque 10.");
            case GRAPE         -> Lang.t("Bouclier 8.");
            case BELL          -> Lang.t("Gains 8.");
            case DIAMOND       -> Lang.t("Bouclier 20.");
            case TRIPLE_CHERRY -> Lang.t("Attaque 45.");
            case TRIPLE_SEVEN  -> Lang.t("Attaque 90.");
            case GOLD_BAR      -> Lang.t("Gains 25.");
            case WATERMELON    -> Lang.t("Gains 12.");
            case HORSESHOE     -> Lang.t("Gains 40.");
            case ECU           -> Lang.t("Bouclier 40.");
            case SWORD         -> Lang.t("Attaque 70. Ignore la défense.");
            case HEART         -> Lang.t("Soigne 10 % des PV max. Le surplus remplit le Sang.");
            case DIE           -> Lang.t("Attaque de 1 à 250, au hasard.");
            case STAR          -> Lang.t("Attaque 30. Bouclier 30. Gains 30.");
            case BOMB          -> Lang.t("Attaque 200. Tu perds 5 % de tes PV max.");
            case CROWN         -> Lang.t("Gains 100.");
            case NUGGET        -> Lang.t("Attaque 40. Gains 60. Gagné en vidant les Mines d'Or.");
            case JOKER         -> Lang.t("Compte comme n'importe quel symbole.");
        };
    }

    private ReelShop() { }
}
