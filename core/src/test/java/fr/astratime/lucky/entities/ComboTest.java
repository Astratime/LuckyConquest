package fr.astratime.lucky.entities;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComboTest {

    private static Card card(int rank, Card.Suit suit) {
        return new Card(rank + "_" + suit, rank + " " + suit, "x.png", List.of(), suit, rank);
    }

    private static Card special() {
        return new Card("special", "special", "x.png", List.of(), null, 1);
    }

    @Test
    void suiteNeedsThreeConsecutiveRanks() {
        assertTrue(Combo.SUITE.matches(List.of(card(11, Card.Suit.COEUR), card(12, Card.Suit.PIQUE), card(13, Card.Suit.TREFLE))));
        assertTrue(Combo.SUITE.matches(List.of(card(12, Card.Suit.COEUR), card(13, Card.Suit.PIQUE), card(1, Card.Suit.TREFLE))),
            "l'As peut suivre le Roi");
        assertFalse(Combo.SUITE.matches(List.of(card(11, Card.Suit.COEUR), card(13, Card.Suit.PIQUE), card(1, Card.Suit.TREFLE))));
    }

    @Test
    void couleurNeedsThreeSuitedCardsOfTheSameSuit() {
        List<Card> hearts = new ArrayList<>(List.of(card(1, Card.Suit.COEUR), card(11, Card.Suit.COEUR), card(13, Card.Suit.COEUR)));
        hearts.add(special());
        assertTrue(Combo.COULEUR.matches(hearts), "les cartes spéciales sont ignorées");
        assertFalse(Combo.COULEUR.matches(List.of(card(1, Card.Suit.COEUR), card(11, Card.Suit.COEUR))));
        assertFalse(Combo.COULEUR.matches(List.of(card(1, Card.Suit.COEUR), card(11, Card.Suit.COEUR), card(12, Card.Suit.PIQUE))));
    }

    @Test
    void brelanAndFull() {
        List<Card> brelan = List.of(card(1, Card.Suit.COEUR), card(1, Card.Suit.PIQUE), card(1, Card.Suit.TREFLE));
        assertTrue(Combo.BRELAN.matches(brelan));
        assertFalse(Combo.FULL.matches(brelan));

        List<Card> full = new ArrayList<>(brelan);
        full.add(card(12, Card.Suit.COEUR));
        full.add(card(12, Card.Suit.CARREAU));
        assertTrue(Combo.FULL.matches(full));
        assertFalse(Combo.BRELAN.matches(List.of(card(1, Card.Suit.COEUR), card(1, Card.Suit.PIQUE))));
    }
}
