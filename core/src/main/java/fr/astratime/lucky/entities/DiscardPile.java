package fr.astratime.lucky.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tas de défausse : reçoit les cartes piochées une fois qu'elles ont été
 * utilisées, puis peut les restituer (mélangées) pour recharger le Deck.
 */
public class DiscardPile {

    private final List<Card> cards = new ArrayList<>();

    /** Ajoute une carte à la défausse. */
    public void add(Card card) {
        cards.add(card);
    }

    /** Ajoute plusieurs cartes à la défausse d'un coup (ex : main non jouée avant une nouvelle pioche). */
    public void addAll(List<Card> discarded) {
        cards.addAll(discarded);
    }

    /** @return {@code true} si la défausse ne contient aucune carte. */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /** @return le nombre de cartes actuellement dans la défausse. */
    public int size() {
        return cards.size();
    }

    /**
     * Vide la défausse et renvoie son contenu mélangé, prêt à être remis
     * dans le deck. Après cet appel, la défausse est vide.
     */
    public List<Card> drainShuffled() {
        List<Card> result = new ArrayList<>(cards);
        Collections.shuffle(result);
        cards.clear();
        return result;
    }
}
