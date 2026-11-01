package fr.astratime.lucky.entities;

import fr.astratime.lucky.controllers.ActionResolver;
import fr.astratime.lucky.controllers.CombatResolver;
import fr.astratime.lucky.controllers.TurnEngine;
import fr.astratime.lucky.entities.actions.BlessingAction;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.progress.ReelShop;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Les rouleaux du 2026-11-01 : Crocs, Tirelire, Bouclier clouté, Faux, Lune,
 * Roi démon, Apôtre de la Chance (boutique), Ticket, Ancre, Jeton d'or, Flamme
 * (lieux), et les Bingos imposés Descente aux enfers et Lumière du bingo.
 */
class ReelsOfNovemberTest {

    private static Player player() {
        return new Player("Joueur", Player.BASE_HP, List.of(), RankBonus.NONE, Symbol.classicReels());
    }

    /** Défense de base de la cible : elle absorbe le premier coup. */
    private static final int DEF = new Enemy("Cible", 1000).getDefense();

    private static Enemy target(int hp) { return new Enemy("Cible", hp); }

    private static CombatContext resolve(Player player, Enemy enemy, Symbol... symbols) {
        CombatContext context = new CombatContext(player, enemy);
        new CombatResolver().resolve(context, new ActionResolver().resolve(symbols), symbols, List.of());
        return context;
    }

    @Test
    void theFangsHealThirtyPercentOfTheirDamage() {
        Player player = player();
        player.takeDamage(50);
        Enemy enemy = target(100_000);
        resolve(player, enemy, Symbol.FANGS, null, null);
        assertEquals(100_000 - (50 - DEF), enemy.getHp());
        assertEquals(50 + Math.round((50 - DEF) * 0.3f), player.getHp(), "30 % des dégâts");
    }

    @Test
    void thePiggyBankGrowsEachTimeItComesUp() {
        Player player = player();
        resolve(player, target(100_000), Symbol.PIGGY_BANK, null, null);
        assertEquals(10, player.getGains());
        resolve(player, target(100_000), Symbol.PIGGY_BANK, Symbol.PIGGY_BANK, null);
        assertEquals(10 + 20 + 30 + SpinEconomy.pairGains(SpinEconomy.BASE_SPIN_COST), player.getGains(), 0.001,
            "puis 20 et 30, et la paire");
        assertEquals(40, player.getLastingEffects().getReelGauges().getPiggyNext());
        assertEquals(10, player.nextCombat().getLastingEffects().getReelGauges().getPiggyNext(), "repart au combat suivant");
    }

    @Test
    void theSpikedShieldHitsWithHalfTheShield() {
        Player player = player();
        player.addShield(100);
        Enemy enemy = target(100_000);
        resolve(player, enemy, Symbol.SPIKED_SHIELD, null, null);
        assertEquals(125, player.getShield());
        assertEquals(100_000 - (62 - DEF), enemy.getHp(), "la moitié de 125");
    }

    @Test
    void theScytheDoublesOnAWeakEnemy() {
        Enemy healthy = target(1000);
        resolve(player(), healthy, Symbol.SCYTHE, null, null);
        assertEquals(1000 - (60 - DEF), healthy.getHp());

        Enemy weak = target(1000);
        weak.takeDamage(800);
        resolve(player(), weak, Symbol.SCYTHE, null, null);
        assertEquals(200 - (120 - DEF), weak.getHp(), "sous 30 % : x2");
    }

    @Test
    void theMoonGrowsWithLostHp() {
        Player player = player();
        Enemy full = target(100_000);
        resolve(player, full, Symbol.MOON, null, null);
        assertEquals(100_000 - (40 - DEF), full.getHp());

        player.takeDamage(50);
        Enemy half = target(100_000);
        resolve(player, half, Symbol.MOON, null, null);
        assertEquals(100_000 - (60 - DEF), half.getHp(), "50 % de PV perdus : +50 %");
    }

    @Test
    void theTicketPaysBetweenZeroAndOneHundredFifty() {
        for (int i = 0; i < 50; i++) {
            Player player = player();
            resolve(player, target(100_000), Symbol.TICKET, null, null);
            assertTrue(player.getGains() >= 0 && player.getGains() <= 150, String.valueOf(player.getGains()));
        }
    }

    @Test
    void theGoldChipDoublesWithAnotherGainSymbol() {
        Player alone = player();
        resolve(alone, target(100_000), Symbol.GOLD_CHIP, Symbol.BAR, null);
        assertEquals(50, alone.getGains());

        Player paired = player();
        resolve(paired, target(100_000), Symbol.GOLD_CHIP, Symbol.BELL, null);
        assertEquals(100 + 12, paired.getGains(), "x2, plus la Cloche");
    }

    @Test
    void theAnchorKeepsItsShieldOneMoreTurn() {
        GameState state = new GameState(new Player("Joueur", Player.BASE_HP, List.of(), RankBonus.NONE,
            Collections.nCopies(Symbol.MACHINE_SIZE, Symbol.ANCHOR)), target(10_000_000));
        TurnEngine engine = new TurnEngine();
        engine.setBonusGameEnabled(false);
        engine.playTurn(state, List.of());
        int kept = state.getPlayer().getLastingEffects().getReelGauges().getAnchorKept();
        assertTrue(kept > 0, "il en reste après son tour (trois Ancres : 105)");
        assertTrue(kept <= 105);
        assertEquals(0, state.getPlayer().getLastingEffects().getVault(), "l'Ancre ne part pas au Coffre");
    }

    @Test
    void theFlameBurnsTheEnemyForTwoTurns() {
        Enemy enemy = target(100_000);
        resolve(player(), enemy, Symbol.FLAME, null, null);
        assertEquals(2, enemy.getMaluses().getBurnTurns());
        assertTrue(enemy.getMaluses().takeBurn());
        resolve(player(), enemy, Symbol.FLAME, null, null);
        assertEquals(2, enemy.getMaluses().getBurnTurns(), "ne se cumule pas, se relance");
    }

    @Test
    void theDemonKingFillsDemonizationThenForcesADescentIntoHell() {
        Player player = new Player("Joueur", Player.BASE_HP, List.of(), RankBonus.NONE,
            Collections.nCopies(Symbol.MACHINE_SIZE, Symbol.DEMON_KING));
        GameState state = new GameState(player, new Enemy("Cible", 100_000_000));
        TurnEngine engine = new TurnEngine();
        engine.setBonusGameEnabled(false);
        ReelGauges gauges = player.getLastingEffects().getReelGauges();

        engine.playTurn(state, List.of());
        assertEquals(3, gauges.getDemonization());
        assertFalse(gauges.isInvincible());
        engine.playTurn(state, List.of());
        assertEquals(0, gauges.getDemonization(), "pleine à 6 : elle s'active et repart");
        assertEquals(Symbol.DESCENT, gauges.getForced());
        assertTrue(gauges.isInvincible(), "jusqu'à la fin du tour suivant");

        int hp = player.getHp();
        TurnResult forced = engine.playTurn(state, List.of());
        assertArrayEquals(new Symbol[] {Symbol.DESCENT, Symbol.DESCENT, Symbol.DESCENT}, forced.getSymbols());
        assertTrue(forced.isJackpot());
        assertTrue(player.getHp() >= hp, "invincible pendant le tour du Bingo imposé");
        assertFalse(gauges.isInvincible(), "fini à la fin de ce tour");
        assertNull(gauges.getForced());
    }

    @Test
    void anInvinciblePlayerLosesNoHp() {
        Player player = player();
        player.getLastingEffects().getReelGauges().fill(6, 0, new java.util.Random(1));
        assertEquals(0, player.takeDamage(1000));
        assertEquals(Player.BASE_HP, player.getHp());
    }

    @Test
    void theDescentEffectsAreChosenOnceForTheWholeBingo() {
        // Vampirisme : 1000 par symbole, la moitié rendue en soin
        Player player = player();
        player.takeDamage(90);
        Enemy enemy = target(100_000);
        CombatContext context = new CombatContext(player, enemy);
        context.setBlessing(CombatContext.Blessing.FIRST);
        Symbol[] symbols = {Symbol.DESCENT, Symbol.DESCENT, Symbol.DESCENT};
        new CombatResolver().resolve(context, new ActionResolver().resolve(symbols), symbols, List.of());
        assertEquals(100_000 - (3 * BlessingAction.HELL_ATTACK - DEF), enemy.getHp());
        assertEquals(Player.BASE_HP, player.getHp());

        // Bouclier et renvoi de 30 % (une seule fois)
        Player shielded = player();
        CombatContext second = new CombatContext(shielded, target(100_000));
        second.setBlessing(CombatContext.Blessing.SECOND);
        new CombatResolver().resolve(second, new ActionResolver().resolve(symbols), symbols, List.of());
        assertEquals(3 * BlessingAction.HELL_SHIELD, shielded.getShield());
        assertEquals(BlessingAction.HELL_REFLECT_PERCENT, second.getReflectHpPercent(), 0.001f);

        // Gains, et l'ennemi enchaîné pour son prochain tour
        Enemy bound = target(100_000);
        CombatContext third = new CombatContext(player(), bound);
        third.setBlessing(CombatContext.Blessing.THIRD);
        new CombatResolver().resolve(third, new ActionResolver().resolve(symbols), symbols, List.of());
        assertTrue(bound.getMaluses().isHellBound());
    }

    @Test
    void theBingoLightAbsorbsHealsAndLightsTheEnemyReels() {
        Player player = player();
        player.takeDamage(50);
        Enemy enemy = target(100_000);
        Symbol[] symbols = {Symbol.BINGO_LIGHT, Symbol.BINGO_LIGHT, Symbol.BINGO_LIGHT};
        CombatContext second = new CombatContext(player, enemy);
        second.setBlessing(CombatContext.Blessing.SECOND);
        new CombatResolver().resolve(second, new ActionResolver().resolve(symbols), symbols, List.of());
        assertTrue(player.isLightAbsorbing());
        assertEquals(0, player.takeDamage(40), "le bouclier prend le coup");
        assertEquals(90, player.getHp(), "et le coup soigne");
        assertEquals(40, player.takeLightAbsorbed(), "rendu au tour suivant");

        Player lucky = player();
        Enemy lit = target(100_000);
        CombatContext third = new CombatContext(lucky, lit);
        third.setBlessing(CombatContext.Blessing.THIRD);
        new CombatResolver().resolve(third, new ActionResolver().resolve(symbols), symbols, List.of());
        assertTrue(lit.getMaluses().isLitUp());
        assertEquals(BlessingAction.LIGHT_PLAYS, lucky.getLastingEffects().getExtraPlays());
    }

    @Test
    void theNewReelsAreSoldOrWonInPlaces() {
        for (Symbol symbol : List.of(Symbol.FANGS, Symbol.PIGGY_BANK, Symbol.SPIKED_SHIELD, Symbol.SCYTHE, Symbol.MOON,
                Symbol.DEMON_KING, Symbol.LUCK_APOSTLE)) {
            assertTrue(ReelShop.getPrices().containsKey(symbol), symbol.name());
        }
        assertEquals(Symbol.TICKET, Place.FOIRE.getReelReward());
        assertEquals(Symbol.ANCHOR, Place.PORT.getReelReward());
        assertEquals(Symbol.NUGGET, Place.MINES.getReelReward());
        assertEquals(Symbol.GOLD_CHIP, Place.CASINO.getReelReward());
        assertEquals(Symbol.FLAME, Place.DESERT.getReelReward());
        assertNull(Place.PRAIRIE.getReelReward());
        assertFalse(ReelShop.getPrices().containsKey(Symbol.DESCENT), "jamais vendus");
        assertFalse(ReelShop.getPrices().containsKey(Symbol.BINGO_LIGHT));
    }
}
