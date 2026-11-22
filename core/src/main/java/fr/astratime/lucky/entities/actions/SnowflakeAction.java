package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.ReelGauges;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.i18n.Lang;

/**
 * Flocon (rouleau du Palace des Neiges) : du bouclier tout de suite, et
 * {@link ReelGauges#FROST_PERCENT} % de ce bouclier gèlent : ils reviennent au
 * début du tour suivant, quoi qu'il arrive au tour de l'ennemi.
 */
public final class SnowflakeAction extends DefenseAction {

    private final int baseShield;

    public SnowflakeAction(int baseShield) {
        super(baseShield);
        this.baseShield = baseShield;
    }

    @Override
    protected void afterShield(CombatContext context, int shield) {
        context.getPlayer().getLastingEffects().getReelGauges().freeze(shield);
    }

    @Override
    public String getDescription() {
        return Lang.f("{0} bouclier de base, dont {1} % gèlent et reviennent au tour suivant", baseShield,
            ReelGauges.FROST_PERCENT);
    }
}
