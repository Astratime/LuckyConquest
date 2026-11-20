package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.LifeDrain;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.i18n.Lang;

import java.util.List;

/**
 * Crocs : une attaque qui soigne le joueur d'une part des dégâts qu'elle
 * inflige (le soin au-delà des PV max remplit le Sang). Sert aussi au
 * Vampirisme de la Descente aux enfers.
 */
public final class DrainAttackAction extends AttackAction {

    private final int drainPercent;

    /**
     * @param baseDamage   dégâts de base
     * @param drainPercent drain de vie, en % (voir {@link LifeDrain})
     */
    public DrainAttackAction(int baseDamage, int drainPercent) {
        super(baseDamage);
        this.drainPercent = drainPercent;
    }

    @Override
    protected void afterHit(CombatContext context, long damage, List<Event> events) {
        if (damage > 0) LifeDrain.apply(context, damage, drainPercent, events);
    }

    @Override
    public String getDescription() {
        return Lang.f("{0} dégâts de base, drain de vie {1} %", getBaseDamage(), drainPercent);
    }
}
