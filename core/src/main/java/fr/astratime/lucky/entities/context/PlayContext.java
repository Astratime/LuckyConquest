package fr.astratime.lucky.entities.context;

/**
 * Contexte d'une carte au moment où elle est jouée (pendant le tour, avant le spin).
 * Collecte ce que les effets immédiats demandent — pour l'instant le nombre de
 * cartes à piocher — sans leur donner accès au deck : c'est GameController qui
 * exécute ensuite la pioche.
 */
public class PlayContext {

    private int cardsToDraw = 0;

    /** Demande à piocher {@code count} cartes supplémentaires immédiatement. */
    public void addCardsToDraw(int count) { cardsToDraw += count; }

    /** @return le nombre de cartes à piocher immédiatement suite aux effets de la carte jouée. */
    public int getCardsToDraw() { return cardsToDraw; }
}
