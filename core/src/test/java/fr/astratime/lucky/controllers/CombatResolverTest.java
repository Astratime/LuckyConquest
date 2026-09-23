package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolOutcome;
import fr.astratime.lucky.entities.SymbolRegistry;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.events.DamageReflectedEvent;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.JackpotEvent;
import fr.astratime.lucky.entities.events.PlayerDamagedEvent;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Couvre en particulier la régression corrigée : le renvoi de dégâts
 * (Carreau) doit réellement toucher l'ennemi.
 */
class CombatResolverTest {

    private final CombatResolver resolver = new CombatResolver();
    private final Symbol[] noSymbols = new Symbol[] { null, null, null };

    @Test
    void reflectsPercentOfEnemyAttackPowerBackToTheEnemy() {
        Player player = new Player("Joueur", 100, List.of());
        Enemy  enemy  = new Enemy("Ennemi", 100); // attackPower par défaut = 10
        player.setReflectPercent(50);

        TurnResult result = resolver.resolve(new CombatContext(player, enemy), List.of(), noSymbols, List.of());

        assertEquals(90, player.getHp(), "le joueur subit toujours l'attaque complète (pas de bouclier ici)");
        assertEquals(95, enemy.getHp(), "50% de l'attaque (10) doit revenir à l'ennemi, soit 5");
        assertTrue(result.getEvents().stream().anyMatch(e -> e instanceof DamageReflectedEvent),
            "un DamageReflectedEvent doit être journalisé");
    }

    @Test
    void doesNotReflectWhenPlayerHasNoReflectPercent() {
        Player player = new Player("Joueur", 100, List.of());
        Enemy  enemy  = new Enemy("Ennemi", 100);

        resolver.resolve(new CombatContext(player, enemy), List.of(), noSymbols, List.of());

        assertEquals(100, enemy.getHp(), "sans renvoi, l'ennemi ne doit subir aucun dégât de riposte");
    }

    @Test
    void skipsEnemyRiposteWhenEnemyAlreadyDefeated() {
        Player player = new Player("Joueur", 100, List.of());
        Enemy  enemy  = new Enemy("Ennemi", 100);
        enemy.takeDamage(200); // l'ennemi est déjà mort avant la riposte

        TurnResult result = resolver.resolve(new CombatContext(player, enemy), List.of(), noSymbols, List.of());

        assertEquals(100, player.getHp(), "un ennemi vaincu ne peut pas riposter");
        assertTrue(result.getEvents().stream().noneMatch(e -> e instanceof PlayerDamagedEvent));
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
    void separatesJackpotAndEnemyRiposteEventsForDisplay() {
        Player player = new Player("Joueur", 100, List.of());
        Enemy  enemy  = new Enemy("Ennemi", 100);
        Symbol[] symbols = { Symbol.SEVEN, Symbol.SEVEN, Symbol.SEVEN };

        TurnResult result = resolver.resolve(new CombatContext(player, enemy), List.of(), symbols, List.of());

        assertTrue(result.getPairOrJackpotEvents().stream().anyMatch(e -> e instanceof JackpotEvent));
        assertTrue(result.getEnemyTurnEvents().stream().anyMatch(e -> e instanceof PlayerDamagedEvent));
        assertTrue(result.getPairOrJackpotEvents().stream().noneMatch(e -> e instanceof PlayerDamagedEvent));
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
    void resetsShieldAndReflectAfterTheTurn() {
        Player player = new Player("Joueur", 100, List.of());
        Enemy  enemy  = new Enemy("Ennemi", 100);
        player.addShield(50);
        player.setReflectPercent(100);

        resolver.resolve(new CombatContext(player, enemy), List.of(), noSymbols, List.of());

        assertEquals(0, player.getShield());
        assertEquals(0, player.getReflectPercent());
    }
}
