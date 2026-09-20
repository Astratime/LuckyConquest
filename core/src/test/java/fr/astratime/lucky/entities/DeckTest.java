package fr.astratime.lucky.entities;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    private static Card card(String id) {
        return new Card(id, id, "assets/" + id + ".png", List.of(), null, 1);
    }

    private static List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) cards.add(card("card" + i));
        return cards;
    }

    @Test
    void drawsExactlyCountWhenEnoughCardsRemain() {
        Deck deck = new Deck(cards(10), new DiscardPile());

        List<Card> drawn = deck.draw(4);

        assertEquals(4, drawn.size());
        assertEquals(6, deck.getCards().size());
    }

    @Test
    void refillsFromDiscardPileOnceTheDeckIsEmpty() {
        DiscardPile discardPile = new DiscardPile();
        discardPile.addAll(cards(5));
        Deck deck = new Deck(cards(2), discardPile);

        List<Card> drawn = deck.draw(4);

        assertEquals(4, drawn.size(), "2 du deck puis 2 de la défausse remélangée");
        assertTrue(discardPile.isEmpty(), "la défausse est vidée dans le deck lors du rechargement");
        assertEquals(3, deck.getCards().size(), "5 rechargés - 2 tirés = 3 restants");
    }

    @Test
    void returnsFewerCardsThanRequestedWhenBothPilesAreExhausted() {
        Deck deck = new Deck(cards(1), new DiscardPile());

        List<Card> drawn = deck.draw(5);

        assertEquals(1, drawn.size(), "un deck et une défausse vides ne peuvent pas produire plus de cartes");
    }
}
