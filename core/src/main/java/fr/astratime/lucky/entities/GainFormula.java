package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.context.CombatContext;

/**
 * La formule des gains de la machine (Astra, 2026-11-16) : <b>Gains = C x Table x J</b>.
 * <ul>
 *   <li><b>C</b> : le coût du tirage du lieu ({@link SpinEconomy}).</li>
 *   <li><b>Table</b> : ce que paie chaque source, en C, la même pour tous les joueurs : symboles de gains
 *       (leur base x {@link SpinEconomy#SYMBOL_TABLE} / 100), Paire, Bingo ({@link Symbol.Rarity}) et Jeu bonus
 *       ({@link BonusGame}).</li>
 *   <li><b>J</b>, le multiplicateur du joueur, le même pour toutes ces sources :
 *       (1 + % du rang) x (1 + 10 % par Prestige) x (1 + % des cartes) x combinaisons x carte Bingo.</li>
 * </ul>
 * La cagnotte du Jeu bonus n'est jamais multipliée. Ainsi la Table seule décide de l'ordre : un Bingo paie
 * toujours plus qu'un symbole, et le Jeu bonus plus qu'un Bingo.
 */
public final class GainFormula {

    /** Ce que vaut en % un point de « multiplicateur de gains » des cartes (Trèfle 2 à 5 : 1 point, +10 %). */
    public static final int CARD_PERCENT_PER_POINT = 10;

    private GainFormula() { }

    /** @return le % de gains que donnent {@code points} points de cartes (1 -> 10). */
    public static float cardPercent(float points) { return points * CARD_PERCENT_PER_POINT; }

    /** @return {@link #cardPercent(float)} écrit sans décimale inutile (« 10 », « 15 », « 2,5 »). */
    public static String cardPercentText(float points) {
        float percent = cardPercent(points);
        return percent == Math.round(percent) ? String.valueOf(Math.round(percent))
            : fr.astratime.lucky.i18n.Lang.decimal(String.valueOf(percent));
    }

    /** @return le multiplicateur du rang (Jeton de rang compris : il double le %), 1 sans rang. */
    public static double rankFactor(Player player, int rankFactor) {
        return 1.0 + (double) player.getRankBonus().gains() * rankFactor / 100.0;
    }

    /** @return le multiplicateur du Prestige du joueur (x1,1 par niveau ajouté), 1 sans Prestige. */
    public static double prestigeFactor(Player player) { return player.getRankBonus().prestigeGainsFactor(); }

    /** @return le multiplicateur des cartes jouées ce tour : 1 + leurs % de gains. */
    public static double cardFactor(CombatContext context) {
        return 1.0 + Math.max(0.0, context.getGainMultiplier() - 1.0) * CARD_PERCENT_PER_POINT / 100.0;
    }

    /** @return le multiplicateur du rang et du Prestige ensemble. */
    public static double rankAndPrestige(CombatContext context) {
        return rankFactor(context.getPlayer(), context.getRankFactor()) * prestigeFactor(context.getPlayer());
    }

    /**
     * @return J, le multiplicateur du joueur ce tour : rang, Prestige, cartes, combinaisons et autres facteurs
     *         de gains du tirage, puissance des symboles (carte Bingo)
     */
    public static double player(CombatContext context) {
        return rankAndPrestige(context) * cardFactor(context) * context.getGainFactor() * context.getSymbolPower();
    }
}
