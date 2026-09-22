package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.effects.ExtraDrawEffect;

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
}
