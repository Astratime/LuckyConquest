package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GaugeFilledEvent;
import fr.astratime.lucky.entities.events.PlayerHealedEvent;
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
     * @param drainPercent part des dégâts infligés rendue en soin, en %
     */
    public DrainAttackAction(int baseDamage, int drainPercent) {
        super(baseDamage);
        this.drainPercent = drainPercent;
    }

    @Override
    protected void afterHit(CombatContext context, long damage, List<Event> events) {
        if (damage <= 0) return;
        Player player = context.getPlayer();
        int drained = (int) Math.min(Integer.MAX_VALUE, Math.round(damage * (drainPercent / 100.0)));
        int healed = player.heal(drained);
        if (healed > 0) events.add(new PlayerHealedEvent(healed));
        if (drained > healed) {
            player.getLastingEffects().addBlood(drained - healed);
            events.add(new GaugeFilledEvent(GaugeFilledEvent.Gauge.SANG, drained - healed));
        }
    }

    @Override
    public String getDescription() {
        return Lang.f("{0} dégâts de base, soigne {1} % des dégâts", getBaseDamage(), drainPercent);
    }
}
