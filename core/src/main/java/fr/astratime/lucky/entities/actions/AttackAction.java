package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.CombatContext;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.Event;

import java.util.List;

/**
 * Inflige des dégâts à l'ennemi.
 * Le bonus d'attaque (cartes jouées) est lu dans le CombatContext.
 * Appliquer +2 à chaque attaque est le comportement voulu :
 * si le bonus s'applique "aux attaques", chaque attaque le reçoit.
 */
public class AttackAction extends Action {

    private final int baseDamage;

    public AttackAction(int baseDamage) { this.baseDamage = baseDamage; }

    @Override
    public List<Event> resolve(CombatContext context) {
        int damage = baseDamage + context.getAttackBonus();
        context.getEnemy().takeDamage(damage);
        return List.of(new EnemyDamagedEvent(damage));
    }

    @Override
    public String getDescription() { return baseDamage + " degats de base"; }
}
