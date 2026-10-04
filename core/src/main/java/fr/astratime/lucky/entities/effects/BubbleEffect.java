package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Bulle d'air (Casino Englouti) : la règle du lieu (Scorbut, coup de grisou,
 * marée) ne joue plus pendant quelques tours, celui-ci compris.
 */
public class BubbleEffect extends Effect {

    private final int turns;

    /** @param turns tours sans la règle du lieu, celui-ci compris */
    public BubbleEffect(int turns) { this.turns = turns; }

    @Override
    public void onPlay(PlayContext context) {
        context.getLastingEffects().addBubble(turns);
        context.addPopups(getPopups());
    }

    /** Rien de plus au tirage : la Bulle agit tant qu'elle dure. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return "La règle du lieu ne joue plus pendant " + turns + " tours."; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("BULLE D'AIR !", EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.INSURANCE; }
}
