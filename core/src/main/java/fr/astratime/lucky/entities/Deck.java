package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.effects.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {

    private static final int ACE_RANK = 1;

    private final List<Card>  cards = new ArrayList<>();
    private final DiscardPile discardPile;

    public Deck(DiscardPile discardPile) {
        this.discardPile = discardPile;
        for (Card.Suit suit : Card.Suit.values()) {
            for (int rank = ACE_RANK; rank <= 13; rank++) {
                cards.add(createCard(suit, rank));
            }
        }
        Collections.shuffle(cards);
    }

    /**
     * Fabrique une carte avec les effets correspondant à sa couleur et son rang.
     * C'est ici — et nulle part ailleurs — que le lien suite/rang → effet est défini.
     *
     * Thème par suite :
     *  - COEUR   : drain de vie sur les attaques (sustain)
     *  - TREFLE  : multiplicateur de gains + bonus d'attaque (amplification)
     *  - CARREAU : renvoi de dégâts via les symboles de défense (résilience)
     *  - PIQUE   : ignore la défense ennemie + bonus d'attaque (pénétration)
     *
     * Les As ont un effet qualitativement différent (pas juste plus fort) — voir les
     * classes AceOf*Effect.
     *
     * Échelle de scaling par rang (2 à 13, le Roi=13 donnant le bonus le plus fort
     * des cartes non-As) : volontairement linéaire et simple pour l'instant,
     * à rééquilibrer une fois les premiers playtests faits.
     */
    private Card createCard(Card.Suit suit, int rank) {
        List<Effect> effects = new ArrayList<>();
        boolean isAce = (rank == ACE_RANK);

        switch (suit) {
            case COEUR:
                effects.add(isAce
                    ? new AceOfHeartsEffect()
                    : new HeartDrainEffect(rank + 1)); // +3% (rang2) a +14% (rang13)
                break;

            case TREFLE:
                effects.add(isAce
                    ? new AceOfClubsEffect()
                    : new ClubGainAttackEffect(rank * 0.08f, rank)); // gains x+0.16..1.04, attaque +2..13
                break;

            case CARREAU:
                effects.add(isAce
                    ? new AceOfDiamondsEffect()
                    : new DiamondReflectEffect(rank * 3)); // +6%..+39% renvoi
                break;

            case PIQUE:
                effects.add(isAce
                    ? new AceOfSpadesEffect()
                    : new SpadeIgnoreDefenseEffect(rank)); // attaque +2..+13, ignore defense
                break;
        }

        return new Card(suit, rank, effects);
    }

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

    public List<Card> getCards() { return cards; }
}
