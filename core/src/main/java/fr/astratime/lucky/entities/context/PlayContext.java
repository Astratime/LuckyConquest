package fr.astratime.lucky.entities.context;

/**
 * Contexte d'une carte au moment où elle est jouée (pendant le tour, avant le spin).
 * Collecte ce que les effets immédiats demandent — cartes à piocher, gains à
 * créditer — sans leur donner accès au joueur ni au deck : c'est GameController
 * qui les applique ensuite.
 */
public class PlayContext {

    private int cardsToDraw = 0;
    private int gains       = 0;

    /** Demande à piocher {@code count} cartes supplémentaires immédiatement. */
    public void addCardsToDraw(int count) { cardsToDraw += count; }

    /** @return le nombre de cartes à piocher immédiatement suite aux effets de la carte jouée. */
    public int getCardsToDraw() { return cardsToDraw; }

    /** Demande à créditer immédiatement {@code amount} gains au joueur. */
    public void addGains(int amount) { gains += amount; }

    /** @return les gains à créditer immédiatement suite aux effets de la carte jouée. */
    public int getGains() { return gains; }
}
