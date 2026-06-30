package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GainsEarnedEvent;
import fr.astratime.lucky.entities.events.PlayerHealedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Inflige des dégâts à l'ennemi.
 * Lit dans le CombatContext :
 *  - attackBonus    : bonus plat ajouté à chaque attaque (cartes jouées)
 *  - ignoreDefense  : si vrai (Pique), la défense de l'ennemi est ignorée
 *  - lifeDrainPercent : si > 0 (Coeur), soigne le joueur d'un % des dégâts infligés
 *  - gainsFromDamage  : si vrai (As de Pique), convertit les dégâts en gains
 */
public class AttackAction extends Action {

    private final int baseDamage;

    public AttackAction(int baseDamage) { this.baseDamage = baseDamage; }

    @Override
    public List<Event> resolve(CombatContext context) {
        List<Event> events = new ArrayList<>();
        Enemy enemy = context.getEnemy();

        int rawDamage = baseDamage + context.getAttackBonus();
        int defense   = context.isIgnoreDefense() ? 0 : enemy.getDefense();
        int damage    = Math.max(0, rawDamage - defense);

        enemy.takeDamage(damage);
        events.add(new EnemyDamagedEvent(damage));

        if (context.getLifeDrainPercent() > 0 && damage > 0) {
            int healed = Math.round(damage * (context.getLifeDrainPercent() / 100f));
            if (healed > 0) {
                context.getPlayer().heal(healed);
                events.add(new PlayerHealedEvent(healed));
            }
        }

        if (context.isGainsFromDamage() && damage > 0) {
            context.getPlayer().addGains(damage);
            events.add(new GainsEarnedEvent(damage));
        }

        return events;
    }

    @Override
    public String getDescription() { return baseDamage + " degats de base"; }
}
