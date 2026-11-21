package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.i18n.Lang;

/**
 * Spectre (rouleau du Manoir hanté) : une attaque qui passe à travers les murs.
 * Au moins {@link #PIERCE_PERCENT} % de son coup traversent la défense de l'ennemi
 * (plus si les Piques du tour percent déjà davantage).
 */
public final class SpectreAction extends AttackAction {

    /** Part du coup du Spectre qui traverse toujours la défense, en %. */
    public static final int PIERCE_PERCENT = 50;

    public SpectreAction(int baseDamage) { super(baseDamage); }

    @Override
    protected int piercePercent(CombatContext context) {
        return Math.max(PIERCE_PERCENT, super.piercePercent(context));
    }

    @Override
    public String getDescription() {
        return Lang.f("{0} dégâts de base, {1} % du coup traversent la défense", getBaseDamage(), PIERCE_PERCENT);
    }
}
