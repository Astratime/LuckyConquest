package fr.astratime.lucky.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {

    private final List<Card> cards = new ArrayList<>();
    private final DiscardPile discardPile;

    public Deck(DiscardPile discardPile) {
        this.discardPile = discardPile;
        for (Card.Suit suit : Card.Suit.values()) {
            for (int rank = 1; rank <= 13; rank++) {
                cards.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(cards);
    }

    /**
     * Return a list of n card to draw in the deck.
     *
     * @param count number of cards to draw
     * @return list of cards
     */
    public List<Card> draw(int count) {
        List<Card> drawn = new ArrayList<>();

        // Pioche des cartes une par une
        while (drawn.size() < count) {
            // si plus de cartes à piocher
            if (cards.isEmpty()) {
                // check de la defausse pour ne pas avoir de boucle infinie
                if (discardPile.isEmpty()) {
                    break;
                }
                // on remet les cartes de la defausse dans le deck
                cards.addAll(discardPile.drainShuffled());
            }

            drawn.add(cards.removeLast());
        }
        return drawn;
    }

    public List<Card> getCards() {
        return cards;
    }

}
