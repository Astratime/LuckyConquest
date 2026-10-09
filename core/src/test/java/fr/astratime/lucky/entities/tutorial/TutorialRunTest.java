package fr.astratime.lucky.entities.tutorial;

import fr.astratime.lucky.controllers.GameController;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.RankBonus;
import fr.astratime.lucky.entities.SpinEconomy;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.loaders.CardLoader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le combat du tutoriel, joué comme le Croupier le demande : les mains
 * prévues sortent dans l'ordre, l'ennemi tient les deux premiers tours, les
 * gains paient la carte Bingo de l'échoppe, et le Bingo achève l'ennemi.
 */
class TutorialRunTest {

    private static final Path ASSETS = Path.of("..", "assets");
    private static final CardLoader.AssetReader READER = path -> {
        try {
            return Files.readString(ASSETS.resolve(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    };
    private static final Function<String, Card> CARDS = CardLoader.cardFactory(READER);

    private static GameController newTutorial() {
        TutorialRun run = new TutorialRun();
        GameController controller = new GameController(() -> CardLoader.loadDeck(TutorialRun.deck(), READER), CARDS,
            TutorialRun.shop(),
            cards -> new Player("Joueur", Player.BASE_HP, cards, RankBonus.NONE, Symbol.classicReels()));
        controller.restart(run.getEnemy());
        TutorialRun.arrange(controller.getGameState().getPlayer().getDeck().getCards());
        TutorialRun.arrange(controller.getGameState().getEnemy());
        return controller;
    }

    private static void play(GameController controller, String id) {
        Card card = controller.getGameState().getPlayer().getCurrentHand().stream()
            .filter(c -> c.getId().equals(id)).findFirst().orElseThrow(() -> new AssertionError(id + " pas en main"));
        assertNull(controller.unplayableReason(card), id);
        controller.playCard(card);
    }

    private static List<String> handIds(GameController controller) {
        return controller.getGameState().getPlayer().getCurrentHand().stream().map(Card::getId).toList();
    }

    @Test
    void theScriptedFightTeachesEveryLessonBeforeTheEnemyFalls() {
        for (int attempt = 0; attempt < 200; attempt++) {
            GameController controller = newTutorial();
            // Tour 1 : Gains +500, 7 de Trèfle, tirage Cerise, Cloche, Raisin.
            controller.drawCards();
            assertEquals(TutorialRun.HANDS.get(0), handIds(controller));
            play(controller, TutorialRun.GAINS_CARD);
            play(controller, TutorialRun.CLUB_SEVEN);
            controller.rigSpin(TutorialRun.rigged(1));
            assertEquals(1000, controller.getGameState().getPlayer().getGains(), "500 au départ, puis le Gains +500");
            TurnResult first = controller.spin();
            assertArrayEquals(TutorialRun.rigged(1), first.getSymbols());
            assertFalse(controller.getGameState().getEnemy().isDefeated(), "l'ennemi tient le premier tour");
            assertFalse(controller.getGameState().getPlayer().isDefeated());

            // Tour 2 : la Paire de 7, le Porte-bonheur, une Mise de 10 %.
            controller.drawCards();
            assertEquals(TutorialRun.HANDS.get(1), handIds(controller));
            TutorialRun.PAIR.forEach(id -> play(controller, id));
            play(controller, TutorialRun.LUCKY_CHARM);
            assertEquals(SpinEconomy.Stake.LOW, controller.nextStake());
            controller.rigSpin(TutorialRun.rigged(2));
            controller.spin();
            assertFalse(controller.getGameState().getEnemy().isDefeated(), "l'ennemi tient le deuxième tour");
            assertFalse(controller.getGameState().getPlayer().isDefeated());

            // Tour 3 : le Valet de Pique (il perce la défense), l'échoppe, la carte Bingo.
            controller.drawCards();
            assertEquals(TutorialRun.HANDS.get(2), handIds(controller));
            play(controller, TutorialRun.SPADE);
            GameController.ShopOffer bingo = controller.getShopOffers().get(0);
            assertEquals(TutorialRun.BINGO, bingo.card().getId());
            assertNull(controller.unavailableReason(bingo), "les gains paient la carte Bingo");
            GameController.Purchase purchase = controller.buy(bingo);
            assertNotNull(purchase);
            assertTrue(purchase.addedToHand());
            controller.rigJackpot(TutorialRun.BINGO_SYMBOL);
            play(controller, TutorialRun.BINGO);
            TurnResult last = controller.spin();
            assertTrue(last.isJackpot());
            assertEquals(TutorialRun.BINGO_SYMBOL, last.getSymbols()[0]);
            assertTrue(controller.getGameState().getEnemy().isDefeated(), "le Bingo achève le croupier");
        }
    }
}
