package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.actions.GainAction;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.events.GainsEarnedEvent;
import fr.astratime.lucky.progress.Rank;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** La formule des gains d'Astra (2026-11-16) : Gains = C x Table x J, le même J pour toutes les sources. */
class GainFormulaTest {

    private static CombatContext context(RankBonus rank) {
        Player player = new Player("Joueur", Player.BASE_HP, List.of(), rank, Symbol.classicReels());
        return new CombatContext(player, new Enemy("Ennemi", 1_000_000));
    }

    @Test
    void theRanksGiveAPercentOfGains() {
        int[] expected = {35, 60, 130, 140, 150, 180, 300, 360, 620, 820};
        for (Rank rank : Rank.values()) assertEquals(expected[rank.ordinal()], rank.getBonus().gains(), rank.name());
        assertEquals(2_800, Rank.LEGENDE.getBonus().heart(), "le Cœur d'or garde l'ancien bonus du rang");
    }

    @Test
    void thePlayerMultiplierIsRankTimesPrestigeTimesCards() {
        CombatContext context = context(Rank.LEGENDE.getBonus().withPrestige(2));
        context.addGainMultiplier(6f); // 2 de Trèfle, 7 de Coeur, Roi de Pique : 1 + 2 + 3 points = +60 %
        assertEquals(9.2 * 1.2 * 1.6, GainFormula.player(context), 1e-6);
    }

    @Test
    void aBingoAlwaysPaysMoreThanAnyGainSymbol() {
        CombatContext context = context(Rank.LEGENDE.getBonus());
        context.addGainMultiplier(6f);
        double crown = ((GainsEarnedEvent) new GainAction(100).resolve(context).get(0)).amount;
        double bingo = SpinEconomy.jackpotGains(Symbol.CHERRY, context.getSpinCost()) * GainFormula.player(context);
        assertEquals(Math.rint(0.5 * 100 * 9.2 * 1.6), crown, "Couronne : 0,5 C x J");
        assertEquals(20 * crown, bingo, 20, "un Bingo commun vaut 20 Couronnes");
    }
}
