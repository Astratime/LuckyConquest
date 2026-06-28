package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.effects.BoostSymbolEffect;
import fr.astratime.lucky.entities.effects.Effect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {

    private final List<Card>  cards = new ArrayList<>();
    private final DiscardPile discardPile;

    public Deck(DiscardPile discardPile) {
        this.discardPile = discardPile;
        for (Card.Suit suit : Card.Suit.values()) {
            for (int rank = 1; rank <= 13; rank++) {
                cards.add(createCard(suit, rank));
            }
        }
        Collections.shuffle(cards);
    }

    /**
     * Fabrique une carte avec les effets correspondant à sa couleur.
     * C'est ici — et nulle part ailleurs — que le lien couleur → effet est défini.
     */
    private Card createCard(Card.Suit suit, int rank) {
        List<Effect> effects = new ArrayList<>();
        switch (suit) {
            case CARREAU: effects.add(new BoostSymbolEffect(Symbol.DIAMOND,       100)); break;
            case COEUR:   effects.add(new BoostSymbolEffect(Symbol.TRIPLE_CHERRY, 100)); break;
            case PIQUE:   effects.add(new BoostSymbolEffect(Symbol.TRIPLE_SEVEN,  100)); break;
            case TREFLE:  effects.add(new BoostSymbolEffect(Symbol.GOLD_BAR,      100)); break;
        }
        return new Card(suit, rank, effects);
    }

    public List<Card> draw(int count) {
        List<Card> drawn = new ArrayList<>();
        while (drawn.size() < count) {
            if (cards.isEmpty()) {
                if (discardPile.isEmpty()) break;
                cards.addAll(discardPile.drainShuffled());
            }
            drawn.add(cards.removeLast());
        }
        return drawn;
    }

    public List<Card> getCards() { return cards; }
}
