package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.ReelGauges;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.i18n.Lang;

/** Tirelire : des gains qui grossissent de {@link ReelGauges#PIGGY_STEP} à chaque sortie du combat (10, 20, 30…). */
public final class PiggyBankAction extends GainAction {

    public PiggyBankAction() { super(ReelGauges.PIGGY_STEP); }

    @Override
    protected int rollBaseGain(CombatContext context) {
        return context.getPlayer().getLastingEffects().getReelGauges().nextPiggy();
    }

    @Override
    public String getDescription() {
        return Lang.f("{0} gains de base, +{0} à chaque sortie du combat", ReelGauges.PIGGY_STEP);
    }
}
