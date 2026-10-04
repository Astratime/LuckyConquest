package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Chope, Cocktail des abysses, Coffre de l'épave : les gains du joueur
 * augmentent tout de suite d'une part de ses gains actuels.
 */
public class GainsPercentEffect extends Effect {

    private final int percent;

    /** @param percent part des gains actuels ajoutée, en % */
    public GainsPercentEffect(int percent) { this.percent = percent; }

    @Override
    public void onPlay(PlayContext context) {
        int added = context.addGainsPercent(percent);
        context.addPopups(List.of(EffectPopup.scaled("GAINS +" + added, EffectPopup.Style.GAINS, added,
            PopupScale.SPIN_GAINS)));
    }

    /** Rien de plus au tirage. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return "Gains +" + percent + " % de tes gains actuels."; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("GAINS +" + percent + " %", EffectPopup.Style.GAINS, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.GAIN; }
}
