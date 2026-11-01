package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.i18n.Lang;

/** Ancre : du bouclier, et ce qu'il en reste après le tour de l'ennemi est gardé un tour de plus. */
public final class AnchorAction extends DefenseAction {

    private final int baseShield;

    public AnchorAction(int baseShield) {
        super(baseShield);
        this.baseShield = baseShield;
    }

    @Override
    protected void afterShield(CombatContext context, int shield) {
        context.getPlayer().getLastingEffects().getReelGauges().anchor(shield);
    }

    @Override
    public String getDescription() { return Lang.f("{0} bouclier de base, gardé un tour de plus", baseShield); }
}
