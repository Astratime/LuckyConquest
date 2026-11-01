package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.i18n.Lang;

/** Faux : une attaque dont les dégâts doublent quand l'ennemi a moins de {@link #LOW_HP_PERCENT} % de ses PV. */
public final class ScytheAction extends AttackAction {

    /** Sous cette part de ses PV max, la Faux frappe deux fois plus fort. */
    public static final int LOW_HP_PERCENT = 30;
    /** Multiplicateur de la Faux sur un ennemi affaibli. */
    public static final int FACTOR = 2;

    public ScytheAction(int baseDamage) { super(baseDamage); }

    @Override
    protected double hitFactor(CombatContext context) {
        Enemy enemy = context.getEnemy();
        return (double) enemy.getHp() * 100 < (double) enemy.getMaxHp() * LOW_HP_PERCENT ? FACTOR : 1.0;
    }

    @Override
    public String getDescription() {
        return Lang.f("{0} dégâts de base, x{1} si l'ennemi a moins de {2} % de ses PV", getBaseDamage(), FACTOR,
            LOW_HP_PERCENT);
    }
}
