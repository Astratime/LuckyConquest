package fr.astratime.lucky.entities;

public class Card {

    public enum Suit { COEUR, CARREAU, TREFLE, PIQUE }

    private final Suit suit;
    private final int rank;

    public Card(Suit suit, int rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() { return suit; }
    public int getRank() { return rank; }

    public String getSuitCode() {
        switch (suit) {
            case COEUR:   return "H";
            case CARREAU: return "D";
            case TREFLE:  return "C";
            case PIQUE:   return "P";
            default: throw new IllegalStateException("Couleur inconnue: " + suit);
        }
    }

    public String getAssetPath(String theme) {
        return "cards/" + theme + "/" + rank + "-" + getSuitCode() + ".png";
    }

    /**
     * Symbole dont cette carte améliore la probabilité.
     */
    public Symbol getTargetSymbol() {
        switch (suit) {
            case CARREAU: return Symbol.DIAMOND;
            case COEUR:   return Symbol.TRIPLE_CHERRY;
            case PIQUE:   return Symbol.TRIPLE_SEVEN;
            case TREFLE:  return Symbol.GOLD_BAR;
            default: throw new IllegalStateException("Couleur inconnue: " + suit);
        }
    }

    /** Description de l'effet affichée au survol de la carte. */
    public String getDescription() {
        switch (suit) {
            case CARREAU: return "Ameliore les chances\nd'obtenir Diamond";
            case COEUR:   return "Ameliore les chances\nd'obtenir Triple Cherry";
            case PIQUE:   return "Ameliore les chances\nd'obtenir Triple Seven";
            case TREFLE:  return "Ameliore les chances\nd'obtenir Gold Bar";
            default: return "";
        }
    }

    @Override
    public String toString() {
        return rank + " de " + suit;
    }
}
