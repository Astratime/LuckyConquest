package fr.astratime.lucky.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Deck de cartes du joueur.
 * Les cartes sont chargées depuis les JSON par CardLoader et passées au constructeur.
 * Deck ne sait plus rien de la logique de construction des cartes — c'est
 * la responsabilité de CardLoader et des fichiers de définition.
 */
public class Deck {

    private final List<Card>  cards = new ArrayList<>();
    private final DiscardPile discardPile;

    public Deck(List<Card> cards, DiscardPile discardPile) {
        this.discardPile = discardPile;
        this.cards.addAll(cards);
        Collections.shuffle(this.cards);
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
