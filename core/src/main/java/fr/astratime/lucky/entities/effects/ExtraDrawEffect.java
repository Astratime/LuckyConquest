package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

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
        context.addPopups(getPopups());
    }

    /** Aucun effet au moment du spin : la pioche a déjà eu lieu quand la carte a été jouée. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return Lang.f(extraCards > 1 ? "Piochez {0} cartes" : "Piochez {0} carte",
        extraCards); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            EffectPopup.scaled(Lang.f("PIOCHE +{0}", extraCards), EffectPopup.Style.DRAW, extraCards, PopupScale.CARD_EXTRA_DRAW)
        );
    }

    @Override
    public EffectSound getSound() { return EffectSound.EXTRA_DRAW; }
}
