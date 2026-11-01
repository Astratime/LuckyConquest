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
        PRICES.put(Symbol.HORSESHOE,     3_000_000L);
        PRICES.put(Symbol.ECU,           3_000_000L);
        PRICES.put(Symbol.PIGGY_BANK,    4_000_000L);
        PRICES.put(Symbol.SWORD,         6_000_000L);
        PRICES.put(Symbol.HEART,         6_000_000L);
        PRICES.put(Symbol.SPIKED_SHIELD, 7_000_000L);
        PRICES.put(Symbol.FANGS,         7_000_000L);
        PRICES.put(Symbol.DIE,           8_000_000L);
        PRICES.put(Symbol.MOON,          9_000_000L);
        PRICES.put(Symbol.STAR,          10_000_000L);
        PRICES.put(Symbol.SCYTHE,        11_000_000L);
        PRICES.put(Symbol.BOMB,          12_000_000L);
        PRICES.put(Symbol.CROWN,         20_000_000L);
        // Roi démon et Apôtre de la Chance (rouleaux d'Astra) : les plus chers
        PRICES.put(Symbol.DEMON_KING,    40_000_000L);
        PRICES.put(Symbol.LUCK_APOSTLE,  40_000_000L);
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
            case BELL          -> Lang.t("Gains 12.");
            case DIAMOND       -> Lang.t("Bouclier 20.");
            case TRIPLE_CHERRY -> Lang.t("Attaque 45.");
            case TRIPLE_SEVEN  -> Lang.t("Attaque 90.");
            case GOLD_BAR      -> Lang.t("Gains 25.");
            case WATERMELON    -> Lang.t("Gains 8.");
            case HORSESHOE     -> Lang.t("Gains 40.");
            case ECU           -> Lang.t("Bouclier 40.");
            case SWORD         -> Lang.t("Attaque 70. Ignore la défense.");
            case HEART         -> Lang.t("Soigne 10 % des PV max. Le surplus remplit le Sang.");
            case DIE           -> Lang.t("Attaque de 1 à 250, au hasard.");
            case STAR          -> Lang.t("Attaque 30. Bouclier 30. Gains 30.");
            case BOMB          -> Lang.t("Attaque 200. Tu perds 5 % de tes PV max.");
            case CROWN         -> Lang.t("Gains 100.");
            case NUGGET        -> Lang.t("Attaque 40. Gains 60. Gagné en vidant les Mines d'Or.");
            case FANGS         -> Lang.t("Attaque 50. Soigne 30 % des dégâts infligés. Le surplus remplit le Sang.");
            case PIGGY_BANK    -> Lang.t("Gains 10, puis +10 à chaque sortie du combat (10, 20, 30...).");
            case SPIKED_SHIELD -> Lang.t("Bouclier 25. Attaque égale à la moitié de ton bouclier.");
            case SCYTHE        -> Lang.t("Attaque 60. Dégâts x2 si l'ennemi a moins de 30 % de ses PV.");
            case MOON          -> Lang.t("Attaque 40, +1 % par % de PV perdus (jusqu'à x2).");
            case DEMON_KING    -> Lang.t("Attaque 100. Bouclier 100. Gains 100. Démonisation +1. À 6 : invincible jusqu'à "
                + "la fin du tour suivant, dont le tirage est un Bingo de Descente aux enfers.");
            case LUCK_APOSTLE  -> Lang.t("Attaque 100. Bouclier 100. Gains 100. Ange chanceux +1. À 6 : invincible "
                + "jusqu'à la fin du tour suivant, dont le tirage est un Bingo de Lumière du bingo.");
            case TICKET        -> Lang.t("Gains de 0 à 150, au hasard. Gagné en vidant la Fête foraine.");
            case ANCHOR        -> Lang.t("Bouclier 35, gardé un tour de plus. Gagné en vidant le Port des Contrebandiers.");
            case GOLD_CHIP     -> Lang.t("Gains 50, x2 si un autre symbole de gains sort avec lui. Gagné en vidant le "
                + "Casino Englouti.");
            case FLAME         -> Lang.t("Attaque 80. Brûlure : l'ennemi perd 2 % de ses PV max à ses 2 prochains tours. "
                + "Gagné en vidant le Désert de Braise.");
            case DESCENT       -> Lang.t("Bingo du Roi démon. Au hasard : attaque 1000 et vampirisme. Ou bouclier 3000 "
                + "et 30 % de ses PV max renvoyés. Ou gains 200, et l'ennemi frappe -50 % et joue 2 cartes.");
            case BINGO_LIGHT   -> Lang.t("Bingo de l'Apôtre de la Chance. Au hasard : attaque et bouclier 800. Ou bouclier "
                + "3000 qui te soigne et rend ses coups. Ou gains 400, 8 cartes, et ses rouleaux s'illuminent.");
            case JOKER         -> Lang.t("Compte comme n'importe quel symbole.");
        };
    }

    private ReelShop() { }
}
