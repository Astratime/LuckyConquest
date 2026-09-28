package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.CardPlayResult;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.effects.BingoEffect;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Un Bingo dont le symbole est retiré des rouleaux (Recyclage) ne peut pas
 * sortir : il n'est pas en vente à l'échoppe, et ne peut pas être joué même
 * s'il est déjà dans la main.
 */
class RecycledBingoTest {

    private static Card bingo(Symbol symbol) {
        return new Card("bingo_" + symbol.name().toLowerCase(), "Bingo", "x.png",
            List.of(new BingoEffect(1, symbol)), null, 1);
    }

    private final GameController controller;
    private final Player player;

    RecycledBingoTest() {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 10; i++) deck.add(new Card("c" + i, "c" + i, "x.png", List.of(), null, 1));
        Map<String, Integer> shop = new LinkedHashMap<>();
        shop.put("bingo_cherry", 0);
        shop.put("bingo_bell", 0);
        controller = new GameController(() -> new ArrayList<>(deck),
            id -> bingo(Symbol.valueOf(id.substring("bingo_".length()).toUpperCase())), shop);
        player = controller.getGameState().getPlayer();
    }

    private GameController.ShopOffer offer(Symbol symbol) {
        return controller.getShopOffers().stream()
            .filter(o -> o.card().getId().equals("bingo_" + symbol.name().toLowerCase()))
            .findFirst().orElseThrow();
    }

    @Test
    void everyBingoIsForSaleWhileNoSymbolIsRemoved() {
        assertNull(controller.unavailableReason(offer(Symbol.CHERRY)));
        assertNotNull(controller.buy(offer(Symbol.CHERRY)));
    }

    @Test
    void theBingoOfARecycledSymbolCannotBeBoughtAndSaysWhy() {
        player.getLastingEffects().removeSymbol(Symbol.CHERRY, 2);

        String reason = controller.unavailableReason(offer(Symbol.CHERRY));
        assertNotNull(reason);
        assertTrue(reason.contains("CERISE") && reason.contains("2 tours"), reason);
        assertNull(controller.buy(offer(Symbol.CHERRY)));
        assertTrue(player.getCurrentHand().isEmpty(), "rien n'est posé sur la table");

        assertNull(controller.unavailableReason(offer(Symbol.BELL)), "les autres Bingo restent en vente");
        assertNotNull(controller.buy(offer(Symbol.BELL)));
    }

    @Test
    void theBingoIsForSaleAgainOnceTheSymbolIsBack() {
        player.getLastingEffects().removeSymbol(Symbol.CHERRY, 1);
        player.getLastingEffects().endTurn();

        assertNull(controller.unavailableReason(offer(Symbol.CHERRY)));
    }

    @Test
    void aBingoInHandCannotBePlayedWhileItsSymbolIsRecycled() {
        Card cherry = bingo(Symbol.CHERRY);
        player.addToHandOrDeck(cherry);
        player.getLastingEffects().removeSymbol(Symbol.CHERRY, 2);

        String reason = controller.unplayableReason(cherry);
        assertNotNull(reason);
        assertTrue(reason.contains("CERISE") && reason.contains("Recyclage"), reason);

        CardPlayResult result = controller.playCard(cherry);
        assertFalse(result.isAutoSpin(), "le Bingo ne se lance pas");
        assertTrue(player.getCurrentHand().contains(cherry), "la carte reste dans la main");
        assertFalse(controller.isHandLocked());
    }

    @Test
    void aBingoInHandCanBePlayedAgainOnceItsSymbolIsBack() {
        Card cherry = bingo(Symbol.CHERRY);
        player.addToHandOrDeck(cherry);
        player.getLastingEffects().removeSymbol(Symbol.CHERRY, 1);
        player.getLastingEffects().endTurn();

        assertNull(controller.unplayableReason(cherry));
        assertTrue(controller.playCard(cherry).isAutoSpin());
        assertFalse(player.getCurrentHand().contains(cherry));
    }

    @Test
    void otherCardsStayPlayableDuringARecycling() {
        Card bell = bingo(Symbol.BELL);
        player.addToHandOrDeck(bell);
        player.getLastingEffects().removeSymbol(Symbol.CHERRY, 2);

        assertNull(controller.unplayableReason(bell));
        assertTrue(controller.playCard(bell).isAutoSpin());
    }
}
