package fr.astratime.lucky.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Deck de cartes du joueur.
 * Les cartes sont chargées depuis les JSON par CardLoader et passées au constructeur.
 * Deck ne sait plus rien de la logique de construction des cartes — c'est
 * la responsabilité de CardLoader et des fichiers de définition.
 */
public class Deck {

    private static final Random RANDOM = new Random();

    private final List<Card>  cards = new ArrayList<>();
    private final DiscardPile discardPile;

    /**
     * Copie et mélange les cartes fournies pour former le deck initial.
     *
     * @param cards       cartes composant le deck (copiées, non modifiées)
     * @param discardPile défausse associée, utilisée pour recharger le deck une fois vide
     */
    public Deck(List<Card> cards, DiscardPile discardPile) {
        this.discardPile = discardPile;
        this.cards.addAll(cards);
        Collections.shuffle(this.cards);
    }

    /**
     * Pioche jusqu'à {@code count} cartes. Si le deck s'épuise en cours de
     * pioche, la défausse est remélangée et réintégrée au deck pour continuer ;
     * si la défausse est également vide, la pioche s'arrête là (moins de
     * cartes que demandé peuvent être retournées).
     *
     * @param count nombre de cartes souhaité
     * @return les cartes piochées (au plus {@code count})
     */
    public List<Card> draw(int count) {
        List<Card> drawn = new ArrayList<>();
        while (drawn.size() < count) {
            if (cards.isEmpty()) {
                if (discardPile.isEmpty()) break;
                cards.addAll(discardPile.drainShuffled());
            }
            drawn.add(cards.removeLast());
        }
        return drawn;
    }

    /** Glisse {@code card} dans le deck, à une place tirée au hasard. */
    public void insertRandomly(Card card) {
        cards.add(RANDOM.nextInt(cards.size() + 1), card);
    }

    /** @return les cartes restantes dans le deck. */
    public List<Card> getCards() { return cards; }
}
