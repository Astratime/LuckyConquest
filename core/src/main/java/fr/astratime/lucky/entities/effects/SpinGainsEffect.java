package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Accorde immédiatement des gains comptés en tirages (ex : Pot de Lutin, 5
 * tirages) : {@code spins} fois le coût d'un tirage du combat, qui suit le lieu
 * (voir {@link fr.astratime.lucky.entities.SpinEconomy}).
 */
public class SpinGainsEffect extends Effect {

    private final int spins;
    private final int gaugeFactor;

    /**
     * @param spins       gains accordés, en coûts de tirage
     * @param gaugeFactor multiplicateur des jauges Lames, Sang et Coffre (1 : inchangées)
     */
    public SpinGainsEffect(int spins, int gaugeFactor) {
        this.spins       = spins;
        this.gaugeFactor = gaugeFactor;
    }

    @Override
    public void onPlay(PlayContext context) {
        int added = spins * context.getSpinCost();
        context.addGains(added);
        context.addPopups(List.of(
            EffectPopup.scaled(Lang.f("GAINS +{0}", added), EffectPopup.Style.GAINS, added, PopupScale.SPIN_GAINS)));
        if (gaugeFactor > 1) {
            context.getLastingEffects().multiplyGauges(gaugeFactor);
            context.addPopups(List.of(new EffectPopup(Lang.f("JAUGES x{0}", gaugeFactor), EffectPopup.Style.SPECIAL,
                PopupScale.SECONDARY_INTENSITY)));
        }
    }

    /** Aucun effet au spin : les gains sont accordés quand la carte est jouée. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() {
        return Lang.f("Gains : {0} tirages{1}",
            spins, (gaugeFactor > 1 ? Lang.f(". Lames, Sang et Coffre x{0}", gaugeFactor) : ""));
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("GAINS : {0} TIRAGES", spins), EffectPopup.Style.GAINS,
            PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.GAINS_MULTIPLIER; }
}
