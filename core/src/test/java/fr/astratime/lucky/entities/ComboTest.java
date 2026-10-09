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
    void brelanNeedsThreeCardsOfTheSameRank() {
        List<Card> brelan = List.of(card(1, Card.Suit.COEUR), card(1, Card.Suit.PIQUE), card(1, Card.Suit.TREFLE));
        assertTrue(Combo.BRELAN.matches(brelan));
        assertFalse(Combo.BRELAN.matches(List.of(card(1, Card.Suit.COEUR), card(1, Card.Suit.PIQUE))));
    }

    @Test
    void carreNeedsFourCardsOfTheSameRankAndKeepsBrelanAndPaire() {
        List<Card> fourAces = List.of(card(1, Card.Suit.COEUR), card(1, Card.Suit.PIQUE),
            card(1, Card.Suit.TREFLE), card(1, Card.Suit.CARREAU));
        assertTrue(Combo.CARRE.matches(fourAces));
        assertFalse(Combo.CARRE.matches(fourAces.subList(0, 3)));
        assertEquals(List.of(Combo.CARRE, Combo.BRELAN, Combo.PAIRE), Combo.formed(fourAces));
        assertEquals(20.5f, Combo.totalFactor(Combo.formed(fourAces)), 1e-6, "Carré + Brelan + Paire : 10 + 6 + 4.5");
    }

    @Test
    void paireNeedsTwoCardsOfTheSameRank() {
        assertTrue(Combo.PAIRE.matches(List.of(card(12, Card.Suit.COEUR), card(12, Card.Suit.PIQUE))));
        assertFalse(Combo.PAIRE.matches(List.of(card(12, Card.Suit.COEUR), card(13, Card.Suit.COEUR), special())));
    }

    @Test
    void combosAddUpAndABrelanAlsoCountsItsPair() {
        List<Card> brelan = List.of(card(12, Card.Suit.COEUR), card(12, Card.Suit.PIQUE), card(12, Card.Suit.TREFLE));
        assertEquals(List.of(Combo.BRELAN, Combo.PAIRE), Combo.formed(brelan), "la Paire validée reste avec le Brelan");
        assertEquals(10.5f, Combo.totalFactor(Combo.formed(brelan)), 1e-6, "Brelan + Paire : 6 + 4.5");

        List<Card> brelanAndPair = List.of(card(12, Card.Suit.COEUR), card(12, Card.Suit.PIQUE),
            card(12, Card.Suit.TREFLE), card(1, Card.Suit.COEUR), card(1, Card.Suit.CARREAU));
        assertEquals(List.of(Combo.BRELAN, Combo.PAIRE), Combo.formed(brelanAndPair), "une paire d'un autre rang compte");
        assertEquals(10.5f, Combo.totalFactor(Combo.formed(brelanAndPair)), 1e-6, "les multiplicateurs s'additionnent");

        List<Card> straightFlush = List.of(card(11, Card.Suit.COEUR), card(12, Card.Suit.COEUR), card(13, Card.Suit.COEUR));
        assertEquals(List.of(Combo.COULEUR, Combo.SUITE), Combo.formed(straightFlush));
        assertEquals(10f, Combo.totalFactor(Combo.formed(straightFlush)), 1e-6);

        List<Card> straightWithPair = List.of(card(11, Card.Suit.COEUR), card(12, Card.Suit.PIQUE),
            card(13, Card.Suit.TREFLE), card(13, Card.Suit.CARREAU));
        assertEquals(List.of(Combo.SUITE, Combo.PAIRE), Combo.formed(straightWithPair));
        assertEquals(9.5f, Combo.totalFactor(Combo.formed(straightWithPair)), 1e-6, "Suite + Paire : 5 + 4.5");

        List<Card> flushWithPair = List.of(card(1, Card.Suit.COEUR), card(1, Card.Suit.COEUR), card(13, Card.Suit.COEUR));
        assertEquals(List.of(Combo.COULEUR, Combo.PAIRE), Combo.formed(flushWithPair));

        assertTrue(Combo.formed(List.of(card(1, Card.Suit.COEUR), card(12, Card.Suit.PIQUE), special())).isEmpty());
        assertEquals(1f, Combo.totalFactor(List.of()), 1e-6);
    }

    @Test
    void strongerCombosMultiplyMore() {
        Combo[] strongestFirst = Combo.values();
        for (int i = 1; i < strongestFirst.length; i++) {
            assertTrue(strongestFirst[i - 1].getFactor() >= strongestFirst[i].getFactor(), strongestFirst[i - 1].name());
        }
    }
}
