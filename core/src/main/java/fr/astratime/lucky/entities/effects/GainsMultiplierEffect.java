package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Multiplie immédiatement les gains du joueur (ex : Pot de Lutin, gains x5). */
public class GainsMultiplierEffect extends Effect {

    private final int factor;

    /** @param factor multiplicateur des gains du joueur. */
    public GainsMultiplierEffect(int factor) { this.factor = factor; }

    @Override
    public void onPlay(PlayContext context) {
        int added = context.multiplyGains(factor);
        context.addPopups(List.of(
            new EffectPopup("GAINS x" + factor, EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY),
            EffectPopup.scaled("GAINS +" + added, EffectPopup.Style.GAINS, added, PopupScale.SPIN_GAINS)));
    }

    /** Aucun effet au spin : les gains sont multipliés quand la carte est jouée. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return "Gains x" + factor; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("GAINS x" + factor, EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY));
    }
}
