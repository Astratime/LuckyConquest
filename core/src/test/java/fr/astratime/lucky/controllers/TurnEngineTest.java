package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.effects.Effect;
import fr.astratime.lucky.entities.effects.ExtraDrawEffect;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Couvre la régression ExtraDrawEffect : la valeur calculée en phase 1
 * (TurnContext.drawCount) doit ressortir intacte dans le TurnResult, pour
 * que GameController puisse effectivement piocher plus de cartes au tour
 * suivant.
 */
class TurnEngineTest {

    private final TurnEngine engine = new TurnEngine();

    @Test
    void extraDrawEffectIncreasesNextDrawCountBeyondTheBase() {
        GameState gameState = new GameState(List.of());

        TurnResult result = engine.playTurn(gameState, List.of(new ExtraDrawEffect(2)), 6);

        assertEquals(8, result.getNextDrawCount(), "6 (base) + 2 (effet) = 8");
    }

    @Test
    void withoutAnyEffectNextDrawCountEqualsTheBase() {
        GameState gameState = new GameState(List.of());

        TurnResult result = engine.playTurn(gameState, List.<Effect>of(), 6);

        assertEquals(6, result.getNextDrawCount());
    }

    @Test
    void multipleExtraDrawEffectsStackWithinTheSameTurn() {
        GameState gameState = new GameState(List.of());

        TurnResult result = engine.playTurn(
            gameState,
            List.of(new ExtraDrawEffect(1), new ExtraDrawEffect(3)),
            6
        );

        assertEquals(10, result.getNextDrawCount());
    }

    @Test
    void playingATurnAdvancesTheTurnNumber() {
        GameState gameState = new GameState(List.of());
        assertEquals(1, gameState.getTurnNumber());

        engine.playTurn(gameState, List.of(), 6);

        assertEquals(2, gameState.getTurnNumber());
    }
}
