package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.EnemyMaluses;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.i18n.Lang;

import java.util.List;

/**
 * Flamme : une attaque qui brûle l'ennemi ; il perd {@link EnemyMaluses#BURN_PERCENT} %
 * de ses PV max au début de ses {@link EnemyMaluses#BURN_TURNS} prochains tours
 * (ne se cumule pas : une nouvelle Flamme relance le compte).
 */
public final class FlameAction extends AttackAction {

    public FlameAction(int baseDamage) { super(baseDamage); }

    @Override
    protected void afterHit(CombatContext context, long damage, List<Event> events) {
        if (!context.getEnemy().isDefeated()) context.getEnemy().getMaluses().burn();
    }

    @Override
    public String getDescription() {
        return Lang.f("{0} dégâts de base, brûle {1} % des PV max de l'ennemi pendant {2} tours", getBaseDamage(),
            EnemyMaluses.BURN_PERCENT, EnemyMaluses.BURN_TURNS);
    }
}
