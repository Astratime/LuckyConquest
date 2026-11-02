package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.DamageBreakdown;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GainsEarnedEvent;
import fr.astratime.lucky.entities.events.GaugeFilledEvent;
import fr.astratime.lucky.entities.events.PlayerHealedEvent;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.List;

/**
 * Inflige des dégâts à l'ennemi.
 * Lit dans le CombatContext :
 *  - attackBonus    : bonus plat ajouté à chaque attaque (cartes jouées)
 *  - attackFactor / symbolPower : multiplicateurs des dégâts (combos, Bingo)
 *  - piercePercent  : part (%) du coup qui traverse la défense (Pique ; 100 : elle est ignorée) ;
 *                     la défense absorbe ce qu'elle peut du reste et s'use d'autant
 *  - lifeDrainPercent : si > 0 (Coeur), soigne le joueur d'un % des dégâts infligés ;
 *                     le soin au-delà des PV max remplit le Sang
 *  - gainsFromDamage  : si vrai (As de Pique), convertit les dégâts en gains
 * Le bonus d'attaque du rang du joueur s'ajoute aux dégâts de base.
 */
public non-sealed class AttackAction extends Action {

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

    /** @return les dégâts de base de ce coup selon le combat (le Bouclier clouté : la moitié du bouclier). */
    protected int rollBaseDamage(CombatContext context) { return rollBaseDamage(); }

    /** @return le multiplicateur propre à ce rouleau, après tous les autres (la Faux, la Lune), 1 par défaut. */
    protected double hitFactor(CombatContext context) { return 1.0; }

    /**
     * Après le coup : ce que fait ce rouleau en plus (les Crocs soignent, la Flamme brûle).
     *
     * @param damage les dégâts portés aux PV de l'ennemi (jamais plus que ses PV max)
     */
    protected void afterHit(CombatContext context, long damage, List<Event> events) { }

    /** @return les dégâts de base affichés (description). */
    protected int getBaseDamage() { return baseDamage; }

    /**
     * Calcule les dégâts (base + bonus, moins ce qu'absorbe la défense
     * ennemie sauf si ignorée), les applique à l'ennemi, puis résout le drain de vie et la
     * conversion en gains le cas échéant.
     */
    @Override
    public List<Event> resolve(CombatContext context) {
        List<Event> events = new ArrayList<>();
        Enemy enemy = context.getEnemy();

        int symbol = rollBaseDamage(context);
        int rank   = context.getPlayer().getRankBonus().attack() * context.getRankFactor();
        int base   = symbol + rank;
        // En long et en double : un gros combo dépasse le milliard de dégâts.
        long unscaled = base + context.getAttackBonus(); // avant les multiplicateurs : ce que vise le pistolet
        long rawDamage = Math.round((double) unscaled * context.getAttackFactor() * context.getSymbolPower() * hitFactor(context));
        long hit        = enemy.capHit(rawDamage); // ce qui touche vraiment sa défense et ses PV
        int pierce      = piercing ? 100 : context.getPiercePercent(); // Pique : une part du coup traverse la défense
        int blocked     = enemy.absorb(hit, pierce); // la défense s'use à chaque coup
        long damage     = enemy.skinned(hit - blocked); // une peau d'or encaisse la moitié
        long shown      = enemy.skinned(rawDamage - blocked); // le vrai coup, affiché même s'il dépasse ses PV

        int lost = enemy.takeDamage(hit - blocked);
        if (lost < damage && !enemy.isDefeated()) damage = shown = lost; // coup annulé (la Maison), Garde, ou fatal évité (Machine Originelle)
        damage = Math.min(damage, enemy.getMaxHp()); // drain de vie et gains : jamais plus que ses PV max, comme avant
        events.add(new EnemyDamagedEvent(shown, rawDamage, unscaled, blocked, enemy.getDefense(), pierce)
            .withBreakdown(new DamageBreakdown(symbol, rank, context.getSteps(CombatContext.Target.ATTACK), rawDamage, pierce,
                blocked, shown)));

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

        afterHit(context, damage, events);

        if (context.isGainsFromDamage() && damage > 0) {
            double earned = damage;
            context.getPlayer().addGains(earned);
            events.add(new GainsEarnedEvent(earned));
        }

        return events;
    }

    @Override
    public String getDescription() { return Lang.f("{0} dégâts de base{1}",
        baseDamage, (piercing ? Lang.t(", ignore la défense") : "")); }
}
