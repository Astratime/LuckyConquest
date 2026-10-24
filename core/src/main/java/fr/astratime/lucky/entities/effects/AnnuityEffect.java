package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Rente viagère (archétype Rentier) : pendant quelques tours, une Créance et des gains à chaque tour
 * (voir {@link fr.astratime.lucky.controllers.PreparationResolver}).
 */
public class AnnuityEffect extends Effect {

    private final int turns;
    private final int claims;
    private final int spins;

    /**
     * @param turns  tours de la Rente, à partir du prochain
     * @param claims Créances posées à chaque tour
     * @param spins  gains à chaque tour, en coûts de tirage
     */
    public AnnuityEffect(int turns, int claims, int spins) {
        this.turns  = turns;
        this.claims = claims;
        this.spins  = spins;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().getPlayer().getLastingEffects().addAnnuity(turns, claims, spins);
    }

    @Override
    public String getDescription() {
        return Lang.f("Pendant tes {0} prochains tours : +{1} Créance et +{2} tirages de gains à chaque tour.", turns, claims, spins);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("RENTE VIAGÈRE"), EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.SAFE; }
}
