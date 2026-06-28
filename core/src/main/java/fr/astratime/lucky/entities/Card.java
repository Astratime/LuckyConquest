package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.effects.Effect;

import java.util.List;
import java.util.stream.Collectors;

public class Card {

    public enum Suit { COEUR, CARREAU, TREFLE, PIQUE }

    private final Suit             suit;
    private final int              rank;
    private final List<Effect> effects;

    public Card(Suit suit, int rank, List<Effect> effects) {
        this.suit    = suit;
        this.rank    = rank;
        this.effects = List.copyOf(effects);
    }

    public Suit getSuit() { return suit; }
    public int  getRank() { return rank; }

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

    /** Applique tous les effets de cette carte sur l'état du jeu. */
    public void applyEffects(GameState state) {
        effects.forEach(e -> e.apply(state));
    }

    /** Description générée automatiquement depuis la liste des effets. */
    public String getDescription() {
        return effects.stream()
            .map(Effect::getDescription)
            .collect(Collectors.joining("\n"));
    }

    @Override
    public String toString() {
        return rank + " de " + suit;
    }
}
