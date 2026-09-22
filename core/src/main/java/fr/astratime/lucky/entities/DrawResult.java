package fr.astratime.lucky.entities;

import java.util.List;

/**
 * Résultat d'une pioche du joueur : les cartes ajoutées à sa main, et celles
 * envoyées directement à la défausse faute de place (main pleine, voir
 * {@link Player#MAX_HAND_SIZE}).
 */
public class DrawResult {

    private final List<Card> addedToHand;
    private final List<Card> discarded;

    /**
     * @param addedToHand cartes piochées qui ont rejoint la main (dans l'ordre de pioche)
     * @param discarded   cartes piochées en surplus, parties directement à la défausse
     */
    public DrawResult(List<Card> addedToHand, List<Card> discarded) {
        this.addedToHand = List.copyOf(addedToHand);
        this.discarded   = List.copyOf(discarded);
    }

    /** @return une pioche vide (aucune carte ajoutée ni défaussée). */
    public static DrawResult empty() { return new DrawResult(List.of(), List.of()); }

    /** @return les cartes piochées qui ont rejoint la main (liste immuable). */
    public List<Card> getAddedToHand() { return addedToHand; }
    /** @return les cartes piochées en surplus, parties à la défausse (liste immuable). */
    public List<Card> getDiscarded()   { return discarded; }
}
