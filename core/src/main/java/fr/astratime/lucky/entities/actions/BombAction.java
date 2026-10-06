package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.PlayerDamagedEvent;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.List;

/**
 * Bombe : une grosse attaque, mais le joueur perd une part de ses PV max
 * (sans passer par le bouclier, jamais sous 1 PV).
 */
public class BombAction extends AttackAction {

    private final int baseDamage;
    private final int hpCostPercent;

    /**
     * @param baseDamage    dégâts de base infligés avant bonus et défense
     * @param hpCostPercent part des PV max du joueur perdue, en %
     */
    public BombAction(int baseDamage, int hpCostPercent) {
        super(baseDamage);
        this.baseDamage    = baseDamage;
        this.hpCostPercent = hpCostPercent;
    }

    @Override
    public List<Event> resolve(CombatContext context) {
        List<Event> events = new ArrayList<>(super.resolve(context));
        Player player = context.getPlayer();
        int lost = player.sacrificeHp(Math.max(1, Math.round(player.getMaxHp() * hpCostPercent / 100f)));
        if (lost > 0) events.add(new PlayerDamagedEvent(lost));
        return events;
    }

    @Override
    public String getDescription() {
        return Lang.f("{0} dégâts de base, coûte {1} % des PV max", baseDamage, hpCostPercent);
    }
}
