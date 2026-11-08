package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.CardPlayResult;
import fr.astratime.lucky.entities.effects.ExtraDrawEffect;
import fr.astratime.lucky.entities.effects.ExtraPlaysEffect;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayLimitTest {

    private static Card filler(int i) {
        return new Card("carte" + i, "carte" + i, "x.png", List.of(new ExtraDrawEffect(0)), null, 1);
    }

    private static GameController controllerWith(List<Card> deck) {
        return new GameController(() -> new ArrayList<>(deck), id -> null);
    }

    @Test
    void theFifthCardOfATurnIsRefusedUntilTheNextTurn() {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 6; i++) deck.add(filler(i));
        GameController controller = controllerWith(deck);
        controller.drawCards();
        List<Card> hand = new ArrayList<>(controller.getGameState().getPlayer().getCurrentHand());

        for (int i = 0; i < GameController.DEFAULT_PLAY_LIMIT; i++) {
            assertNull(controller.unplayableReason(hand.get(i)));
            controller.playCard(hand.get(i));
        }
        assertEquals(4, controller.getCardsPlayedThisTurn());
        assertTrue(controller.isPlayLimitReached());
        assertNotNull(controller.unplayableReason(hand.get(4)));
        assertSame(CardPlayResult.none().getDrawResult().getAddedToHand().size(),
            controller.playCard(hand.get(4)).getDrawResult().getAddedToHand().size());
        assertTrue(controller.getGameState().getPlayer().getCurrentHand().contains(hand.get(4)), "la carte reste en main");

        controller.spin();
        assertEquals(0, controller.getCardsPlayedThisTurn(), "le compteur repart à zéro au tour suivant");
    }

    @Test
    void inTheSleeveAllowsSixCardsForThreeTurnsIncludingThisOne() {
        List<Card> deck = new ArrayList<>();
        deck.add(new Card("manche", "Dans la manche", "x.png", List.of(new ExtraPlaysEffect(6, 3)), null, 1, true));
        for (int i = 0; i < 30; i++) deck.add(filler(i));
        GameController controller = controllerWith(deck);
        controller.getGameState().getPlayer().getLastingEffects().addExtraPlays(6, 3);

        for (int turn = 0; turn < 3; turn++) {
            assertEquals(6, controller.getPlayLimit(), "tour " + (turn + 1));
            controller.spin();
        }
        assertEquals(GameController.DEFAULT_PLAY_LIMIT, controller.getPlayLimit(), "l'effet est terminé");
        assertEquals(0, controller.getGameState().getPlayer().getLastingEffects().getExtraPlaysTurns());
    }
}
