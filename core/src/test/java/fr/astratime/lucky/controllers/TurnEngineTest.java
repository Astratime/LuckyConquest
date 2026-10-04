package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.effects.ExtraDrawEffect;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.events.ThornsEvent;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TurnEngineTest {

    private final TurnEngine engine = new TurnEngine();

    @Test
    void playingATurnAdvancesTheTurnNumber() {
        GameState gameState = new GameState(List.of());
        assertEquals(1, gameState.getTurnNumber());

        engine.playTurn(gameState, List.of());

        assertEquals(2, gameState.getTurnNumber());
    }

    @Test
    void extraDrawEffectHasNoImpactOnTheSpin() {
        GameState gameState = new GameState(List.of());

        TurnResult result = engine.playTurn(gameState, List.of(new ExtraDrawEffect(2)));

        assertEquals(3, result.getSymbols().length, "le tour se joue normalement, la pioche a déjà eu lieu au jeu de la carte");
    }

    @Test
    void aPlayerKilledByThornsEndsTheCombatBeforeTheEnemysTurn() {
        Enemy enemy = new Enemy(EnemyKind.GARDIEN);
        enemy.addThorns(10);
        enemy.takeDamage(1_000); // coups du joueur : ses Épines lui en renverront 10 %
        GameState gameState = new GameState(new Player("Joueur", 5, List.of()), enemy);

        TurnResult result = engine.playTurn(gameState, List.of());

        assertTrue(gameState.getPlayer().isDefeated());
        assertNull(result.getEnemyTurn(), "l'ennemi ne joue pas son tour");
        assertTrue(result.getEnemyTurnEvents().stream().anyMatch(e -> e instanceof ThornsEvent),
            "les Épines s'affichent à la fin du tour, avant la défaite");
    }
}
