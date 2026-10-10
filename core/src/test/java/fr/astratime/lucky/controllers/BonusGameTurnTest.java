package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.BonusGame;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.RankBonus;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.effects.BingoEffect;
import fr.astratime.lucky.entities.effects.MultiplierEffect;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Le Jeu bonus dans un tour : il suit un Bingo, ses gains sont crédités, et le tutoriel n'en a pas. */
class BonusGameTurnTest {

    private static List<Card> plainCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 20; i++) cards.add(new Card("c" + i, "c" + i, "c.png", List.of(), null, 1));
        return cards;
    }

    private static GameState fight() {
        GameController controller = new GameController(BonusGameTurnTest::plainCards);
        controller.restart(EnemyKind.ENTRAINEMENT);
        return controller.getGameState();
    }

    @Test
    void theTestCardOpensTheBonusGameAndItsGainsAreCredited() {
        GameState state = fight();
        TurnEngine engine = new TurnEngine();
        long before = state.getPlayer().getGains();

        TurnResult result = engine.playTurn(state, List.of(new BingoEffect(1, Symbol.SWORD, true)));

        BonusGame bonus = result.getBonusGame();
        assertNotNull(bonus);
        assertTrue(result.isJackpot());
        long fromTurn = result.getEvents().stream()
            .filter(e -> e instanceof fr.astratime.lucky.entities.events.GainsEarnedEvent)
            .mapToLong(e -> ((fr.astratime.lucky.entities.events.GainsEarnedEvent) e).amount).sum();
        assertEquals(before + fromTurn + bonus.getTotal(), state.getPlayer().getGains(),
            "les gains du Jeu bonus s'ajoutent à ceux du tirage");
    }

    @Test
    void noBingoNoBonusGame() {
        GameState state = fight();
        TurnEngine engine = new TurnEngine();
        for (int i = 0; i < 200; i++) {
            TurnResult result = engine.playTurn(state, List.of());
            if (!result.isJackpot()) assertNull(result.getBonusGame());
            if (state.getEnemy().isDefeated() || state.getPlayer().isDefeated()) state = fight();
        }
    }

    @Test
    void aBingoOpensItAboutOneTimeInTen() {
        TurnEngine engine = new TurnEngine();
        int opened = 0, bingos = 400;
        for (int i = 0; i < bingos; i++) {
            if (engine.playTurn(fight(), List.of(new BingoEffect(1, Symbol.SWORD))).getBonusGame() != null) opened++;
        }
        assertEquals(bingos * BonusGame.TRIGGER_CHANCE, opened, 25);
    }

    @Test
    void theBonusGameCountsLikeTheGainSymbolsOfTheTurn() {
        RankBonus rank = new RankBonus(0, 0, 0, 70);
        GameController controller = new GameController(BonusGameTurnTest::plainCards, id -> null, java.util.Map.of(),
            cards -> new Player("Joueur", Player.BASE_HP, cards, rank, Symbol.classicReels()));
        controller.restart(EnemyKind.ENTRAINEMENT);
        TurnResult result = new TurnEngine().playTurn(controller.getGameState(),
            List.of(new MultiplierEffect(9), new BingoEffect(1, Symbol.SWORD, true)));

        BonusGame bonus = result.getBonusGame();
        long expected = 0;
        for (BonusGame.Line line : bonus.getLines()) {
            // le bonus du rang par symbole aligné, puis les cartes Trèfle (1 + 9 = x10)
            expected += (BonusGame.gains(line.symbol(), line.length(), 100) + 70L * line.length()) * 10;
        }
        assertEquals(expected, bonus.getTotal());
    }

    @Test
    void theTutorialHasNoBonusGame() {
        TurnEngine engine = new TurnEngine();
        engine.setBonusGameEnabled(false);
        TurnResult result = engine.playTurn(fight(), List.of(new BingoEffect(1, Symbol.SWORD, true)));
        assertTrue(result.isJackpot());
        assertNull(result.getBonusGame());
    }
}
