package fr.astratime.lucky;


public class Card {

    public enum Suit { COEUR, CARREAU, TREFLE, PIQUE }

    private final Suit suit;
    private final int rank; // 1 (As) à 13 (Roi)

    public Card(Suit suit, int rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() { return suit; }
    public int getRank() { return rank; }

    @Override
    public String toString() {
        return rank + " de " + suit;
    }
}
