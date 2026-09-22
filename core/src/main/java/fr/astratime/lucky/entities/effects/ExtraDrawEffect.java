package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;

/**
 * Pioche immédiatement des cartes supplémentaires, pendant le tour en cours.
 * Les cartes qui ne tiennent pas dans la main (voir Player.MAX_HAND_SIZE)
 * partent directement à la défausse.
 */
public class ExtraDrawEffect extends Effect {

    private final int extraCards;

    /** @param extraCards nombre de cartes à piocher immédiatement. */
    public ExtraDrawEffect(int extraCards) { this.extraCards = extraCards; }

    @Override
    public void onPlay(PlayContext context) {
        context.addCardsToDraw(extraCards);
    }

    /** Aucun effet au moment du spin : la pioche a déjà eu lieu quand la carte a été jouée. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return "Piochez " + extraCards + " carte(s)"; }
}
