package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.CardPlayResult;
import fr.astratime.lucky.entities.DrawResult;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.effects.Effect;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.entities.effects.ExtraDrawEffect;
import fr.astratime.lucky.entities.effects.BetEffect;
import fr.astratime.lucky.entities.effects.BingoEffect;
import fr.astratime.lucky.entities.effects.ComboEffect;
import fr.astratime.lucky.entities.effects.GainsMultiplierEffect;
import fr.astratime.lucky.entities.effects.RainbowEffect;
import fr.astratime.lucky.entities.effects.RecycleEffect;
import fr.astratime.lucky.entities.effects.RussianRouletteEffect;
import fr.astratime.lucky.entities.Combo;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.choices.BetChoice;
import fr.astratime.lucky.entities.choices.RouletteChoice;
import fr.astratime.lucky.entities.events.BetLostEvent;
import fr.astratime.lucky.entities.events.BetWonEvent;
import fr.astratime.lucky.entities.events.ComboEvent;
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

    @Test
    void betAsksForASymbolThenAppliesAtSpin() {
        GameController controller = controllerWith(List.of(card("pari", new BetEffect())));
        controller.drawCards();
        player(controller).addGains(1000);

        CardPlayResult result = controller.playCard(player(controller).getCurrentHand().get(0));
        assertInstanceOf(BetChoice.class, result.getChoice());
        assertFalse(controller.getBetOptions().contains(Symbol.JOKER));

        controller.placeBet(Symbol.BELL);
        assertNull(controller.getPendingChoice());
        assertEquals(List.of(Symbol.BELL), controller.getBetsThisTurn());

        TurnResult turn = controller.spin();
        assertTrue(turn.getPairOrJackpotEvents().stream()
            .anyMatch(e -> e instanceof BetWonEvent || e instanceof BetLostEvent));
        assertTrue(controller.getBetsThisTurn().isEmpty());
    }

    @Test
    void cursedJokerCostsGainsAndOtherCardsLoadThePistol() {
        for (int index = 0; index < RussianRouletteEffect.CARDS; index++) {
            GameController controller = controllerWith(List.of(card("roulette", new RussianRouletteEffect(50, 20))));
            controller.drawCards();
            player(controller).addGains(1000);
            RouletteChoice choice = (RouletteChoice) controller
                .playCard(player(controller).getCurrentHand().get(0)).getChoice();
            assertEquals(1, choice.cursed().stream().filter(c -> c).count(), "un seul Joker maudit");

            GameController.RouletteOutcome outcome = controller.pickRouletteCard(index);
            TurnResult turn = controller.spin();

            assertEquals(choice.cursed().get(index), outcome.cursed());
            if (outcome.cursed()) {
                assertTrue(turn.getPistolEvents().isEmpty());
            } else {
                assertEquals(1, turn.getPistolEvents().size());
            }
        }
    }

    @Test
    void bingoLocksTheHandAndGuaranteesAJackpot() {
        List<Card> deck = new ArrayList<>(plainCards(5));
        deck.add(card("bingo", new BingoEffect(100)));
        GameController controller = controllerWith(deck);
        controller.drawCards();
        Card bingo = player(controller).getCurrentHand().stream()
            .filter(c -> c.getId().equals("bingo")).findFirst().orElseThrow();

        assertTrue(controller.playCard(bingo).isAutoSpin());
        assertTrue(controller.isHandLocked());
        assertTrue(controller.playCard(player(controller).getCurrentHand().get(0)).getPopups().isEmpty(),
            "plus aucune carte ne peut être jouée");

        TurnResult turn = controller.spin();
        assertTrue(turn.isJackpot());
        assertFalse(controller.isHandLocked());
    }

    @Test
    void recycledSymbolStaysOutOfTheReelsForThreeTurns() {
        GameController controller = controllerWith(List.of(card("recyclage", new RecycleEffect(3))));
        controller.drawCards();
        controller.playCard(player(controller).getCurrentHand().get(0));
        Symbol removed = player(controller).getLastingEffects().getRemovedSymbols().keySet().iterator().next();

        for (int turn = 0; turn < 3; turn++) {
            assertTrue(player(controller).getLastingEffects().getRemovedSymbols().containsKey(removed));
            for (Symbol symbol : controller.spin().getDrawnSymbols()) assertNotEquals(removed, symbol);
        }
        assertTrue(player(controller).getLastingEffects().getRemovedSymbols().isEmpty());
    }

    @Test
    void comboMultipliesWhenPlayedCardsFormIt() {
        List<Card> deck = List.of(
            new Card("a1", "a1", "x.png", List.of(), Card.Suit.COEUR, 1),
            new Card("a2", "a2", "x.png", List.of(), Card.Suit.PIQUE, 1),
            new Card("a3", "a3", "x.png", List.of(), Card.Suit.TREFLE, 1),
            card("brelan", new ComboEffect(Combo.BRELAN, 3)));
        GameController controller = controllerWith(deck);
        controller.drawCards();
        for (Card card : new ArrayList<>(player(controller).getCurrentHand())) controller.playCard(card);

        TurnResult turn = controller.spin();

        assertTrue(turn.getCardEvents().stream().anyMatch(e -> e instanceof ComboEvent combo && combo.success));
    }

    private static Card suited(int rank, Card.Suit suit) {
        return new Card(suit.cardId(rank), suit.cardId(rank), "x.png", List.of(), suit, rank);
    }

    /** Fabrique de test : cartes à suite d'après leur id, et un Pot de Lutin consommable (gains x5). */
    private static Card fromId(String id) {
        if (id.equals("pot")) {
            return new Card("pot", "pot", "x.png", List.of(new GainsMultiplierEffect(5)), null, 1, true);
        }
        String[] parts = id.split("_");
        return suited(Integer.parseInt(parts[0]), Card.Suit.valueOf(parts[1].toUpperCase()));
    }

    @Test
    void rainbowRecolorsSuitedCardsAndAddsThePotToTheTable() {
        List<Card> deck = new ArrayList<>(List.of(suited(1, Card.Suit.COEUR), suited(12, Card.Suit.PIQUE),
            card("pioche", new ExtraDrawEffect(0)), card("arc", new RainbowEffect("pot"))));
        GameController controller = new GameController(() -> new ArrayList<>(deck), GameControllerTest::fromId);
        controller.drawCards();
        Card rainbow = player(controller).getCurrentHand().stream()
            .filter(c -> c.getId().equals("arc")).findFirst().orElseThrow();

        CardPlayResult.Rainbow result = controller.playCard(rainbow).getRainbow();

        assertEquals(2, result.recolored().size(), "seules les cartes à suite changent de couleur");
        for (CardPlayResult.Recolor recolor : result.recolored()) {
            assertEquals(recolor.before().getRank(), recolor.after().getRank());
            assertTrue(player(controller).getCurrentHand().contains(recolor.after()));
            assertFalse(player(controller).getCurrentHand().contains(recolor.before()));
        }
        assertTrue(result.addedToHand());
        assertTrue(player(controller).getCurrentHand().contains(result.added()));
    }

    @Test
    void aCardAddedToAFullTableGoesToTheDiscardPile() {
        Player player = new Player("Joueur", 100, plainCards(Player.MAX_HAND_SIZE));
        player.draw(Player.MAX_HAND_SIZE);
        Card pot = fromId("pot");

        assertFalse(player.addToHandOrDiscard(pot));
        assertEquals(Player.MAX_HAND_SIZE, player.getCurrentHand().size());
        assertTrue(player.getDiscardPile().getCards().contains(pot));
    }

    @Test
    void potDeLutinMultipliesGainsAndDisappearsOncePlayed() {
        GameController controller = new GameController(() -> new ArrayList<>(List.of(fromId("pot"))),
            GameControllerTest::fromId);
        controller.drawCards();
        player(controller).addGains(300);

        controller.playCard(player(controller).getCurrentHand().get(0));
        assertEquals(1500, player(controller).getGains());
        controller.spin();

        assertTrue(player(controller).getDiscardPile().getCards().isEmpty(), "le Pot de Lutin ne va pas en défausse");
        assertTrue(player(controller).getDeck().getCards().isEmpty(), "ni dans le deck");
    }
}
