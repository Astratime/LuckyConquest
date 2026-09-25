package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Multiplie immédiatement les gains du joueur (ex : Pot de Lutin, gains x5). */
public class GainsMultiplierEffect extends Effect {

    private final int factor;
    private final int gaugeFactor;

    /** @param factor multiplicateur des gains du joueur. */
    public GainsMultiplierEffect(int factor) { this(factor, 1); }

    /**
     * @param factor      multiplicateur des gains du joueur
     * @param gaugeFactor multiplicateur des jauges Lames, Sang et Coffre (1 : inchangées)
     */
    public GainsMultiplierEffect(int factor, int gaugeFactor) {
        this.factor      = factor;
        this.gaugeFactor = gaugeFactor;
    }

    @Override
    public void onPlay(PlayContext context) {
        int added = context.multiplyGains(factor);
        context.addPopups(List.of(
            new EffectPopup("GAINS x" + factor, EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY),
            EffectPopup.scaled("GAINS +" + added, EffectPopup.Style.GAINS, added, PopupScale.SPIN_GAINS)));
        if (gaugeFactor > 1) {
            context.getLastingEffects().multiplyGauges(gaugeFactor);
            context.addPopups(List.of(new EffectPopup("JAUGES x" + gaugeFactor, EffectPopup.Style.SPECIAL,
                PopupScale.SECONDARY_INTENSITY)));
        }
    }

    /** Aucun effet au spin : les gains sont multipliés quand la carte est jouée. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() {
        return "Gains x" + factor + (gaugeFactor > 1 ? "\nLames, Sang et Coffre x" + gaugeFactor : "");
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("GAINS x" + factor, EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY));
    }
}
