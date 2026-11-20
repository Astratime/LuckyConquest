package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GaugeFilledEvent;
import fr.astratime.lucky.entities.events.PlayerHealedEvent;

import java.util.List;

/**
 * Drain de vie relatif (2026-11-20) : le soin se mesure aux PV max du joueur, pas aux dégâts bruts.
 *
 * <p>Avant, le drain rendait un % des dégâts infligés. Un soldat a environ 90 fois les PV max du joueur (un roi
 * environ 180 fois), donc un seul Coeur sur un tour moyen remettait presque toute la vie, et une suite de Coeur
 * plusieurs fois la vie entière : le joueur survivait à chaque tour (même leçon que le vol de vie de Diablo III
 * ou de Path of Exile, plafonné par rapport à la vie du joueur).
 *
 * <p>Maintenant : soin = drain % x (part des PV max de l'ennemi enlevée) x PV max du joueur x {@link #FACTOR}.
 * Un tour du drain ne peut pas soigner plus de {@link #TURN_CAP_PERCENT} % des PV max (Sang compris), et le soin
 * au-delà des PV max remplit le Sang ({@link #BLOOD_PERCENT} %, Astra 2026-11-20 : le plafond du tour freine déjà).
 */
public final class LifeDrain {

    /**
     * Coefficient (simulateur, 2026-11-20 : 10 plutôt que 3, à 3 tous les decks s'effondraient) : un 9 de Coeur
     * (10 %) sur un tour qui enlève 10 % des PV de l'ennemi rend 10 % des PV max ; une suite 10-V-D-R (50 %), 50 %.
     */
    public static final double FACTOR = 10.0;
    /** Ce que le drain peut rendre au plus en un tour, en % des PV max du joueur (Sang compris). */
    public static final int TURN_CAP_PERCENT = 50;
    /** Part du soin en trop qui remplit le Sang, en % (50 d'abord, 100 depuis le 2026-11-20 : le Sang ne servait plus). */
    public static final int BLOOD_PERCENT = 100;

    private LifeDrain() { }

    /** @return ce que soigne {@code drainPercent} % de drain sur un coup qui enlève 10 % des PV de l'ennemi, en % des PV max (affiché). */
    public static String per10(int drainPercent) {
        double value = drainPercent * FACTOR / 10.0;
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(Math.round(value * 10) / 10.0);
    }

    /**
     * @param drainPercent drain du coup, en %
     * @param damage       dégâts réellement infligés par le coup
     * @param enemyMaxHp   PV max de l'ennemi
     * @param playerMaxHp  PV max du joueur
     * @return le soin du coup, avant le plafond du tour
     */
    public static long amount(int drainPercent, long damage, long enemyMaxHp, int playerMaxHp) {
        if (drainPercent <= 0 || damage <= 0 || enemyMaxHp <= 0) return 0;
        double share = Math.min(1.0, (double) damage / enemyMaxHp);
        return Math.round(drainPercent / 100.0 * share * playerMaxHp * FACTOR);
    }

    /**
     * Soigne le joueur pour un coup qui a infligé {@code damage}, dans la limite du plafond du tour ; le surplus
     * remplit le Sang à {@link #BLOOD_PERCENT} %.
     */
    public static void apply(CombatContext context, long damage, int drainPercent, List<Event> events) {
        Player player = context.getPlayer();
        long wanted = amount(drainPercent, damage, context.getEnemy().getMaxHp(), player.getMaxHp());
        long room = Math.max(0, Math.round(player.getMaxHp() * TURN_CAP_PERCENT / 100.0) - context.getDrainHealed());
        int drained = (int) Math.min(Integer.MAX_VALUE, Math.min(wanted, room));
        if (drained <= 0) return;
        context.addDrainHealed(drained);
        int received = player.afterThirst(drained); // la Soif divise le soin : la part retirée est perdue
        int healed = player.heal(drained); // plafonné aux PV max
        if (healed > 0) events.add(new PlayerHealedEvent(healed));
        int blood = (int) Math.round(Math.max(0, received - healed) * BLOOD_PERCENT / 100.0);
        if (blood > 0) {
            player.getLastingEffects().addBlood(blood);
            events.add(new GaugeFilledEvent(GaugeFilledEvent.Gauge.SANG, blood));
        }
    }
}
