package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GainsEarnedEvent;
import fr.astratime.lucky.entities.events.GaugeFilledEvent;
import fr.astratime.lucky.entities.events.PlayerHealedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Inflige des dégâts à l'ennemi.
 * Lit dans le CombatContext :
 *  - attackBonus    : bonus plat ajouté à chaque attaque (cartes jouées)
 *  - attackFactor / symbolPower : multiplicateurs des dégâts (combos, Bingo)
 *  - ignoreDefense  : si vrai (Pique), la défense de l'ennemi est ignorée ;
 *                     sinon elle absorbe ce qu'elle peut du coup et s'use d'autant
 *  - lifeDrainPercent : si > 0 (Coeur), soigne le joueur d'un % des dégâts infligés ;
 *                     le soin au-delà des PV max remplit le Sang
 *  - gainsFromDamage  : si vrai (As de Pique), convertit les dégâts en gains
 * Le bonus d'attaque du rang du joueur s'ajoute aux dégâts de base.
 */
public class AttackAction extends Action {

    private final int     baseDamage;
    /** Ce rouleau ignore toujours la défense ennemie (Épée). */
    private final boolean piercing;

    /** @param baseDamage dégâts de base infligés avant bonus et défense. */
    public AttackAction(int baseDamage) { this(baseDamage, false); }

    /**
     * @param baseDamage dégâts de base infligés avant bonus et défense
     * @param piercing   {@code true} si le coup ignore toujours la défense ennemie
     */
    public AttackAction(int baseDamage, boolean piercing) {
        this.baseDamage = baseDamage;
        this.piercing   = piercing;
    }

    /** @return les dégâts de base de ce coup, avant bonus (le Dé les tire au hasard). */
    protected int rollBaseDamage() { return baseDamage; }

    /**
     * Calcule les dégâts (base + bonus, moins ce qu'absorbe la défense
     * ennemie sauf si ignorée), les applique à l'ennemi, puis résout le drain de vie et la
     * conversion en gains le cas échéant.
     */
    @Override
    public List<Event> resolve(CombatContext context) {
        List<Event> events = new ArrayList<>();
        Enemy enemy = context.getEnemy();

        int base = rollBaseDamage() + context.getPlayer().getRankBonus().attack();
        int rawDamage = Math.round((base + context.getAttackBonus())
            * context.getAttackFactor() * context.getSymbolPower());
        boolean pierced = piercing || context.isIgnoreDefense();
        int blocked     = pierced ? 0 : enemy.absorb(rawDamage); // la défense s'use à chaque coup
        int damage      = rawDamage - blocked;

        enemy.takeDamage(damage);
        events.add(new EnemyDamagedEvent(damage, rawDamage, blocked, enemy.getDefense(), pierced));

        if (context.getLifeDrainPercent() > 0 && damage > 0) {
            int drained = Math.round(damage * (context.getLifeDrainPercent() / 100f));
            int healed  = context.getPlayer().heal(drained); // plafonné aux PV max
            if (healed > 0) {
                events.add(new PlayerHealedEvent(healed));
            }
            if (drained > healed) { // le soin en trop remplit le Sang (Coeur)
                context.getPlayer().getLastingEffects().addBlood(drained - healed);
                events.add(new GaugeFilledEvent(GaugeFilledEvent.Gauge.SANG, drained - healed));
            }
        }

        if (context.isGainsFromDamage() && damage > 0) {
            context.getPlayer().addGains(damage);
            events.add(new GainsEarnedEvent(damage));
        }

        return events;
    }

    @Override
    public String getDescription() { return baseDamage + " dégâts de base" + (piercing ? ", ignore la défense" : ""); }
}
