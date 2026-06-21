package fr.astratime.lucky;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {


    private final List<Card> cards = new ArrayList<>();

    public Deck() {
        for (Card.Suit suit : Card.Suit.values()) {
            for (int rank = 1; rank <= 13; rank++) {
                cards.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(cards);
    }

    public List<Card> draw(int count) {
        List<Card> drawn = new ArrayList<>();
        for (int i = 0; i < count && !cards.isEmpty(); i++) {
            drawn.add(cards.removeLast());
        }
        return drawn;
    }
    public List<Card> getCards() {
        return cards;
    }

}
