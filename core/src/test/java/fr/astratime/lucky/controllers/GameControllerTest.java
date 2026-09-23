package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.CardPlayResult;
import fr.astratime.lucky.entities.DrawResult;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.effects.Effect;
import fr.astratime.lucky.entities.effects.EffectPopup;
import fr.astratime.lucky.entities.effects.ExtraDrawEffect;
import fr.astratime.lucky.entities.effects.GainEffect;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Enchaînement d'un tour via GameController, avec des decks construits à la main (sans libGDX). */
class GameControllerTest {

    /** Effet de test qui compte combien de fois il est appliqué au spin. */
    private static class CountingEffect extends Effect {
        int applied = 0;
        @Override public void apply(TurnContext context) { applied++; }
        @Override public String getDescription() { return "compteur"; }
        @Override public List<EffectPopup> getPopups() { return List.of(); }
    }

    private static Card card(String id, Effect... effects) {
        return new Card(id, id, id + ".png", List.of(effects), null, 1);
    }

    private static List<Card> plainCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) cards.add(card("c" + i));
        return cards;
    }

    private static GameController controllerWith(List<Card> deck) {
        return new GameController(() -> new ArrayList<>(deck));
    }

    private static Player player(GameController controller) {
        return controller.getGameState().getPlayer();
    }

    @Test
    void drawCardsFillsTheHandFromTheDeck() {
        GameController controller = controllerWith(plainCards(20));

        DrawResult result = controller.drawCards();

        assertEquals(GameController.DEFAULT_DRAW_COUNT, result.getAddedToHand().size());
        assertEquals(GameController.DEFAULT_DRAW_COUNT, player(controller).getCurrentHand().size());
        assertEquals(20 - GameController.DEFAULT_DRAW_COUNT, player(controller).getDeck().getCards().size());
    }

    @Test
    void playedCardEffectsAreAppliedOnceAtSpin() {
        CountingEffect effect = new CountingEffect();
        GameController controller = controllerWith(List.of(card("compteur", effect)));
        controller.drawCards();

        controller.playCard(player(controller).getCurrentHand().get(0));
        assertEquals(0, effect.applied, "rien n'est appliqué avant le spin");

        controller.spin();
        assertEquals(1, effect.applied);

        controller.spin();
        assertEquals(1, effect.applied, "les effets en attente sont vidés après le spin");
    }

    @Test
    void playingACardNotInTheHandDoesNothing() {
        CountingEffect effect = new CountingEffect();
        GameController controller = controllerWith(plainCards(10));
        controller.drawCards();

        CardPlayResult result = controller.playCard(card("hors_main", effect));
        controller.spin();

        assertTrue(result.getPopups().isEmpty());
        assertTrue(result.getDrawResult().getAddedToHand().isEmpty());
        assertEquals(0, effect.applied);
    }

    @Test
    void drawCardDrawsImmediately() {
        List<Card> deck = new ArrayList<>(plainCards(19));
        deck.add(card("pioche", new ExtraDrawEffect(2)));
        GameController controller = controllerWith(deck);
        // Pioche toute la main jusqu'à tomber sur la carte de pioche (le deck est mélangé).
        Card drawCard = null;
        while (drawCard == null) {
            controller.drawCards();
            drawCard = player(controller).getCurrentHand().stream()
                .filter(c -> c.getId().equals("pioche")).findFirst().orElse(null);
            if (drawCard == null) controller.spin();
        }
        int handBefore = player(controller).getCurrentHand().size();

        CardPlayResult result = controller.playCard(drawCard);

        assertEquals(2, result.getDrawResult().getAddedToHand().size());
        assertEquals(handBefore - 1 + 2, player(controller).getCurrentHand().size());
        assertEquals(List.of("PIOCHE +2"), result.getPopups().stream().map(EffectPopup::getText).toList());
    }

    @Test
    void gainCardCreditsGainsImmediately() {
        GameController controller = controllerWith(List.of(card("gain", new GainEffect(500))));
        controller.drawCards();

        controller.playCard(player(controller).getCurrentHand().get(0));

        assertEquals(500, player(controller).getGains());
    }

    @Test
    void spinSendsPlayedAndRemainingCardsToTheDiscardPile() {
        GameController controller = controllerWith(plainCards(20));
        controller.drawCards();
        controller.playCard(player(controller).getCurrentHand().get(0));

        controller.spin();

        assertTrue(player(controller).getCurrentHand().isEmpty());
        assertTrue(player(controller).getPlayedCards().isEmpty());
        assertEquals(GameController.DEFAULT_DRAW_COUNT, player(controller).getDiscardPile().size());
    }

    @Test
    void restartStartsANewCombatAndForgetsPendingEffects() {
        CountingEffect effect = new CountingEffect();
        GameController controller = controllerWith(List.of(card("compteur", effect)));
        controller.drawCards();
        controller.playCard(player(controller).getCurrentHand().get(0));
        player(controller).takeDamage(30);

        controller.restart();

        assertEquals(player(controller).getMaxHp(), player(controller).getHp());
        assertEquals(1, player(controller).getDeck().getCards().size(), "un deck neuf est fourni au nouveau combat");

        controller.spin(); // la riposte ennemie de ce spin ne concerne pas ce test
        assertEquals(0, effect.applied, "l'effet joué avant le redémarrage ne doit pas s'appliquer");
    }
}
