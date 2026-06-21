package fr.astratime.lucky.entities;


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

    /**
     * Code d'une lettre utilisé dans le nom des fichiers d'assets
     * (ex: "cards/light/13-C.png" pour le Roi de Trèfle).
     */
    public String getSuitCode() {
        switch (suit) {
            case COEUR:   return "H";
            case CARREAU: return "D";
            case TREFLE:  return "C";
            case PIQUE:   return "P";
            default: throw new IllegalStateException("Couleur inconnue: " + suit);
        }
    }

    /**
     * Chemin (relatif au dossier assets) de l'image représentant cette carte,
     * pour un thème donné ("light" ou "dark").
     */
    public String getAssetPath(String theme) {
        return "cards/" + theme + "/" + rank + "-" + getSuitCode() + ".png";
    }

    @Override
    public String toString() {
        return rank + " de " + suit;
    }
}
