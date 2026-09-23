package fr.astratime.lucky.entities;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private final Player player = new Player("Joueur", 100, List.of());

    private static List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) cards.add(new Card("card" + i, "card" + i, "card" + i + ".png", List.of(), null, 1));
        return cards;
    }

    @Test
    void drawAddsCardsToTheHandWhileThereIsRoom() {
        Player p = new Player("Joueur", 100, cards(20));

        DrawResult result = p.draw(6);

        assertEquals(6, result.getAddedToHand().size());
        assertTrue(result.getDiscarded().isEmpty());
        assertEquals(6, p.getCurrentHand().size());
        assertTrue(p.getDiscardPile().isEmpty());
    }

    @Test
    void cardsDrawnBeyondTheHandLimitGoToTheDiscardPile() {
        Player p = new Player("Joueur", 100, cards(20));
        p.draw(7);

        DrawResult result = p.draw(3);

        assertEquals(1, result.getAddedToHand().size(), "une seule place avant d'atteindre " + Player.MAX_HAND_SIZE);
        assertEquals(2, result.getDiscarded().size());
        assertEquals(Player.MAX_HAND_SIZE, p.getCurrentHand().size());
        assertEquals(2, p.getDiscardPile().size());
    }

    @Test
    void playedCardsFreeRoomInTheHand() {
        Player p = new Player("Joueur", 100, cards(20));
        p.draw(Player.MAX_HAND_SIZE);

        assertTrue(p.playCard(p.getCurrentHand().get(0)));
        DrawResult result = p.draw(2);

        assertEquals(1, result.getAddedToHand().size());
        assertEquals(1, result.getDiscarded().size());
        assertEquals(1, p.getPlayedCards().size());
    }

    @Test
    void playingACardNotInTheHandDoesNothing() {
        Player p = new Player("Joueur", 100, cards(5));

        assertFalse(p.playCard(cards(1).get(0)));
        assertTrue(p.getPlayedCards().isEmpty());
    }

    @Test
    void discardHandSendsPlayedAndRemainingCardsToTheDiscardPile() {
        Player p = new Player("Joueur", 100, cards(10));
        p.draw(6);
        Card played = p.getCurrentHand().get(0);
        p.playCard(played);

        List<Card> remaining = p.discardHand();

        assertEquals(5, remaining.size());
        assertFalse(remaining.contains(played));
        assertEquals(6, p.getDiscardPile().size());
        assertTrue(p.getCurrentHand().isEmpty());
        assertTrue(p.getPlayedCards().isEmpty());
    }

    @Test
    void shieldAbsorbsDamageBeforeHp() {
        player.addShield(30);

        int actualLoss = player.takeDamage(20);

        assertEquals(0, actualLoss, "20 de bouclier suffisent à absorber 20 de dégâts");
        assertEquals(100, player.getHp());
        assertEquals(10, player.getShield(), "il doit rester 30 - 20 = 10 de bouclier");
    }

    @Test
    void damageExceedingShieldSpillsOverToHp() {
        player.addShield(10);

        int actualLoss = player.takeDamage(25);

        assertEquals(15, actualLoss, "25 - 10 de bouclier = 15 en trop");
        assertEquals(85, player.getHp());
        assertEquals(0, player.getShield());
    }

    @Test
    void hpNeverGoesBelowZero() {
        int actualLoss = player.takeDamage(500);

        assertEquals(100, actualLoss);
        assertEquals(0, player.getHp());
        assertTrue(player.isDefeated());
    }

    @Test
    void healNeverExceedsMaxHp() {
        player.takeDamage(30);

        int healed = player.heal(1000);

        assertEquals(100, player.getHp());
        assertEquals(30, healed, "seuls les 30 PV manquants sont réellement rendus");
    }

    @Test
    void gainsNeverGoNegative() {
        player.addGains(5);

        player.addGains(-100);

        assertEquals(0, player.getGains());
    }

    @Test
    void consumeGainsPercentReturnsAndDeductsTheComputedAmount() {
        player.addGains(1000);

        int consumed = player.consumeGainsPercent(0.3f);

        assertEquals(300, consumed);
        assertEquals(700, player.getGains());
    }

    @Test
    void reflectPercentKeepsTheBestValueOfTheTurn() {
        player.setReflectPercent(50);
        player.setReflectPercent(30); // ne doit pas faire redescendre le bonus

        assertEquals(50, player.getReflectPercent());
    }

    @Test
    void resetTurnDefensesClearsShieldAndReflect() {
        player.addShield(40);
        player.setReflectPercent(80);

        player.resetTurnDefenses();

        assertEquals(0, player.getShield());
        assertEquals(0, player.getReflectPercent());
    }
}
