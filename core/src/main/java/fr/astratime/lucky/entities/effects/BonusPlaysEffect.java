package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Cocktail des abysses (le Bar) : des cartes jouables en plus, ce tour seulement.
 */
public class BonusPlaysEffect extends Effect {

    private final int plays;

    /** @param plays cartes jouables en plus ce tour */
    public BonusPlaysEffect(int plays) { this.plays = plays; }

    @Override
    public void onPlay(PlayContext context) {
        context.getLastingEffects().addBonusPlays(plays);
        context.addPopups(getPopups());
    }

    /** Rien de plus au tirage. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return "Joue " + plays + " cartes de plus ce tour."; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("+" + plays + " CARTES CE TOUR", EffectPopup.Style.DRAW, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.EXTRA_PLAYS; }
}
