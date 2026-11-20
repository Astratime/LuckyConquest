package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.ReelGauges;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.i18n.Lang;

/**
 * Vapeur (rouleau du Rapide de Minuit) : une attaque qui monte en pression.
 * Chaque tirage d'affilée où elle est déjà sortie ajoute
 * {@link ReelGauges#STEAM_STEP_PERCENT} % à son coup ({@link ReelGauges#STEAM_MAX} crans au plus) ;
 * un tirage sans Vapeur la fait retomber.
 */
public final class SteamAction extends AttackAction {

    public SteamAction(int baseDamage) { super(baseDamage); }

    @Override
    protected double hitFactor(CombatContext context) {
        return context.getPlayer().getLastingEffects().getReelGauges().steamFactor();
    }

    @Override
    public String getDescription() {
        return Lang.f("{0} dégâts de base, +{1} % par tirage d'affilée où elle sort (x{2} au plus)", getBaseDamage(),
            ReelGauges.STEAM_STEP_PERCENT, 1 + ReelGauges.STEAM_STEP_PERCENT * ReelGauges.STEAM_MAX / 100);
    }
}
