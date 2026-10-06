package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GaugeFilledEvent;
import fr.astratime.lucky.entities.events.PlayerHealedEvent;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.List;

/**
 * Coeur : soigne le joueur d'une part de ses PV max (multipliée par la
 * puissance des symboles, comme au Bingo). Le soin au-delà des PV max remplit
 * le Sang (Coeur).
 */
public class HealAction extends Action {

    private final int percent;

    /** @param percent part des PV max rendue, en % */
    public HealAction(int percent) { this.percent = percent; }

    @Override
    public List<Event> resolve(CombatContext context) {
        List<Event> events = new ArrayList<>();
        Player player = context.getPlayer();
        int amount = Math.round(player.getMaxHp() * percent / 100f * context.getSymbolPower());
        int healed = player.heal(amount);
        if (healed > 0) events.add(new PlayerHealedEvent(healed));
        if (amount > healed) {
            player.getLastingEffects().addBlood(amount - healed);
            events.add(new GaugeFilledEvent(GaugeFilledEvent.Gauge.SANG, amount - healed));
        }
        return events;
    }

    @Override
    public String getDescription() { return Lang.f("Soigne {0} % des PV max", percent); }
}
