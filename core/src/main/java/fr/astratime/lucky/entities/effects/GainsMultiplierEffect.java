package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Augmente immédiatement les gains du joueur d'un pourcentage (ex : Pot de Lutin, gains +50 %). */
public class GainsMultiplierEffect extends Effect {

    private final int percent;
    private final int gaugeFactor;

    /** @param percent pourcentage des gains actuels ajouté aux gains du joueur. */
    public GainsMultiplierEffect(int percent) { this(percent, 1); }

    /**
     * @param percent     pourcentage des gains actuels ajouté aux gains du joueur
     * @param gaugeFactor multiplicateur des jauges Lames, Sang et Coffre (1 : inchangées)
     */
    public GainsMultiplierEffect(int percent, int gaugeFactor) {
        this.percent     = percent;
        this.gaugeFactor = gaugeFactor;
    }

    @Override
    public void onPlay(PlayContext context) {
        int added = context.addGainsPercent(percent);
        context.addPopups(List.of(
            new EffectPopup(Lang.f("GAINS +{0}%", percent), EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY),
            EffectPopup.scaled(Lang.f("GAINS +{0}", added), EffectPopup.Style.GAINS, added, PopupScale.SPIN_GAINS)));
        if (gaugeFactor > 1) {
            context.getLastingEffects().multiplyGauges(gaugeFactor);
            context.addPopups(List.of(new EffectPopup(Lang.f("JAUGES x{0}", gaugeFactor), EffectPopup.Style.SPECIAL,
                PopupScale.SECONDARY_INTENSITY)));
        }
    }

    /** Aucun effet au spin : les gains sont multipliés quand la carte est jouée. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() {
        return Lang.f("Gains actuels +{0}%{1}",
            percent, (gaugeFactor > 1 ? Lang.f(". Lames, Sang et Coffre x{0}", gaugeFactor) : ""));
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("GAINS +{0}%", percent), EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.GAINS_MULTIPLIER; }
}
