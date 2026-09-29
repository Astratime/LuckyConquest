package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolOutcome;
import fr.astratime.lucky.entities.SymbolRegistry;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.events.BetLostEvent;
import fr.astratime.lucky.entities.events.BetWonEvent;
import fr.astratime.lucky.entities.events.PistolShotEvent;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.JackpotEvent;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Tour du joueur : symboles, pistolet, paire/jackpot, paris (le tour de l'ennemi : EnemyTurnResolverTest). */
class CombatResolverTest {

    private final CombatResolver resolver = new CombatResolver();
    private final Symbol[] noSymbols = new Symbol[] { null, null, null };

    @Test
    void diamondReflectNeedsADefenseSymbolDrawnThisTurn() {
        Player player = new Player("Joueur", 100, List.of());
        Enemy  enemy  = new Enemy("Ennemi", 1000);
        CombatContext context = new CombatContext(player, enemy);
        context.addReflectPercentBonus(390);
        Symbol[] symbols = { Symbol.GRAPE, null, null };
        List<SymbolAction> actions = List.of(
            new SymbolAction(Symbol.GRAPE, 0, SymbolRegistry.getAction(Symbol.GRAPE).orElseThrow()));

        resolver.resolve(context, actions, symbols, List.of());

        assertEquals(390, context.getTotalReflectPercent(), "le renvoi vaudra pour le tour de l'ennemi");
    }

    @Test
    void diamondReflectIsInactiveWithoutDefenseSymbol() {
        Player player = new Player("Joueur", 100, List.of());
        Enemy  enemy  = new Enemy("Ennemi", 1000);
        CombatContext context = new CombatContext(player, enemy);
        context.addReflectPercentBonus(390);

        resolver.resolve(context, List.of(), noSymbols, List.of());

        assertEquals(0, context.getTotalReflectPercent());
    }

    @Test
    void awardsJackpotBonusWhenAllThreeSymbolsMatch() {
        Player player = new Player("Joueur", 100, List.of());
        Enemy  enemy  = new Enemy("Ennemi", 100);
        Symbol[] symbols = { Symbol.SEVEN, Symbol.SEVEN, Symbol.SEVEN };

        TurnResult result = resolver.resolve(new CombatContext(player, enemy), List.of(), symbols, List.of());

        assertEquals(2000, result.getGainsFromPairOrJackpot());
        assertEquals(2000, player.getGains());
        assertTrue(result.isJackpot());
        assertTrue(result.getEvents().stream().anyMatch(e -> e instanceof JackpotEvent));
    }

    @Test
    void jackpotEventCarriesTheAlignedSymbol() {
        Player player = new Player("Joueur", 100, List.of());
        Enemy  enemy  = new Enemy("Ennemi", 100);
        Symbol[] symbols = { Symbol.DIAMOND, Symbol.DIAMOND, Symbol.DIAMOND };

        TurnResult result = resolver.resolve(new CombatContext(player, enemy), List.of(), symbols, List.of());

        assertTrue(result.getEvents().stream()
            .anyMatch(e -> e instanceof JackpotEvent jackpot && jackpot.symbol == Symbol.DIAMOND));
    }

    @Test
    void awardsPairBonusWhenExactlyTwoSymbolsMatch() {
        Player player = new Player("Joueur", 100, List.of());
        Enemy  enemy  = new Enemy("Ennemi", 100);
        Symbol[] symbols = { Symbol.SEVEN, Symbol.SEVEN, Symbol.CHERRY };

        TurnResult result = resolver.resolve(new CombatContext(player, enemy), List.of(), symbols, List.of());

        assertEquals(500, result.getGainsFromPairOrJackpot());
        assertTrue(result.isPair());
        assertFalse(result.isJackpot());
    }

    @Test
    void attachesResultingEventsToTheSymbolThatProducedThem() {
        Player player = new Player("Joueur", 100, List.of());
        Enemy  enemy  = new Enemy("Ennemi", 100);
        List<SymbolAction> symbolActions = List.of(
            new SymbolAction(Symbol.SEVEN, 0, SymbolRegistry.getAction(Symbol.SEVEN).orElseThrow())
        );
        Symbol[] symbols = { Symbol.SEVEN, null, null };

        TurnResult result = resolver.resolve(new CombatContext(player, enemy), symbolActions, symbols, List.of());

        assertEquals(1, result.getSymbolOutcomes().size());
        SymbolOutcome outcome = result.getSymbolOutcomes().get(0);
        assertEquals(Symbol.SEVEN, outcome.getSymbol());
        assertTrue(outcome.getEvents().stream().anyMatch(e -> e instanceof EnemyDamagedEvent),
            "l'action de SEVEN doit produire un EnemyDamagedEvent rattaché à ce symbole");
    }

    @Test
    void keepsTheShieldForTheEnemysTurnThenResetsIt() {
        Player player = new Player("Joueur", 100, List.of());
        Enemy  enemy  = new Enemy("Ennemi", 100);
        player.addShield(50);

        resolver.resolve(new CombatContext(player, enemy), List.of(), noSymbols, List.of());
        assertEquals(50, player.getShield(), "le bouclier attend les attaques de l'ennemi");

        TurnEngine.storeLeftoverShield(player);
        assertEquals(0, player.getShield());
    }

    private static SymbolAction action(Symbol symbol, int slot) {
        return new SymbolAction(symbol, slot, SymbolRegistry.getAction(symbol).orElseThrow());
    }

    @Test
    void pistolMultipliesTheStrongestAttackSymbol() {
        Player player = new Player("Joueur", 100, List.of());
        Enemy  enemy  = new Enemy("Ennemi", 100_000); // défense 30
        CombatContext context = new CombatContext(player, enemy);
        context.addPistolShot(50);
        Symbol[] symbols = { Symbol.BAR, Symbol.SEVEN, Symbol.GRAPE };

        TurnResult result = resolver.resolve(context,
            List.of(action(Symbol.BAR, 0), action(Symbol.SEVEN, 1), action(Symbol.GRAPE, 2)), symbols, List.of());

        PistolShotEvent shot = (PistolShotEvent) result.getPistolEvents().get(0);
        assertEquals(1, shot.slotIndex, "le SEPT (30) est le symbole d'attaque le plus fort");
        assertEquals(30 * 50, shot.rawDamage);
        assertEquals(30 * 50 - 30, shot.damage);
        assertEquals(100_000 - 0 - 0 - shot.damage, enemy.getHp(), "BAR (10) et SEPT (30) ne passent pas la défense");
    }

    @Test
    void pistolWithoutAttackSymbolUsesItsBaseDamage() {
        Player player = new Player("Joueur", 100, List.of());
        Enemy  enemy  = new Enemy("Ennemi", 100_000);
        CombatContext context = new CombatContext(player, enemy);
        context.addPistolShot(50);

        TurnResult result = resolver.resolve(context, List.of(), noSymbols, List.of());

        PistolShotEvent shot = (PistolShotEvent) result.getPistolEvents().get(0);
        assertEquals(-1, shot.slotIndex);
        assertEquals(CombatResolver.PISTOL_BASE_DAMAGE * 50, shot.rawDamage);
    }

    @Test
    void wonBetMultipliesGainsByOccurrencesPlusOne() {
        Player player = new Player("Joueur", 100, List.of());
        player.addGains(1000);
        CombatContext context = new CombatContext(player, new Enemy("Ennemi", 100_000));
        context.addBet(Symbol.CHERRY);
        Symbol[] symbols = { Symbol.CHERRY, Symbol.CHERRY, Symbol.BAR };

        TurnResult result = resolver.resolve(context, List.of(), symbols, List.of());

        assertEquals((1000 + 500) * 3, player.getGains(), "paire créditée (500), puis gains x3 (2 cerises)");
        assertTrue(result.getPairOrJackpotEvents().stream().anyMatch(e -> e instanceof BetWonEvent won && won.multiplier == 3));
    }

    @Test
    void lostBetHalvesGains() {
        Player player = new Player("Joueur", 100, List.of());
        player.addGains(1000);
        CombatContext context = new CombatContext(player, new Enemy("Ennemi", 100_000));
        context.addBet(Symbol.DIAMOND);
        Symbol[] symbols = { Symbol.CHERRY, Symbol.BELL, Symbol.BAR };

        TurnResult result = resolver.resolve(context, List.of(), symbols, List.of());

        assertEquals(500, player.getGains());
        assertTrue(result.getPairOrJackpotEvents().stream().anyMatch(e -> e instanceof BetLostEvent lost && lost.amount == 500));
    }

    @Test
    void gainFactorAlsoAppliesToThePairBonus() {
        Player player = new Player("Joueur", 100, List.of());
        CombatContext context = new CombatContext(player, new Enemy("Ennemi", 100_000));
        context.multiplyGains(1.5f);
        Symbol[] symbols = { Symbol.SEVEN, Symbol.SEVEN, Symbol.CHERRY };

        TurnResult result = resolver.resolve(context, List.of(), symbols, List.of());

        assertEquals(750, result.getGainsFromPairOrJackpot());
    }

    @Test
    void symbolPowerMultipliesAttackShieldAndGains() {
        Player player = new Player("Joueur", 100, List.of());
        Enemy  enemy  = new Enemy("Ennemi", 100_000);
        CombatContext context = new CombatContext(player, enemy);
        context.multiplySymbolPower(100);
        Symbol[] symbols = { Symbol.SEVEN, Symbol.GRAPE, Symbol.BELL };

        resolver.resolve(context, List.of(action(Symbol.SEVEN, 0), action(Symbol.GRAPE, 1), action(Symbol.BELL, 2)),
            symbols, List.of());

        assertEquals(100_000 - (3000 - 30), enemy.getHp());
        assertEquals(800, player.getGains(), "cloche : 8 x 100");
    }
}
