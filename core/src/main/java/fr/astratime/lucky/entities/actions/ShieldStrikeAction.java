package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.i18n.Lang;

/** Bouclier clouté (sa partie attaque) : des dégâts de base égaux à la moitié du bouclier du joueur. */
public final class ShieldStrikeAction extends AttackAction {

    /** Part du bouclier du joueur qui frappe, en %. */
    public static final int SHIELD_PERCENT = 50;

    public ShieldStrikeAction() { super(0); }

    @Override
    protected int rollBaseDamage(CombatContext context) {
        return (int) ((long) context.getPlayer().getShield() * SHIELD_PERCENT / 100);
    }

    @Override
    public String getDescription() { return Lang.f("Attaque égale à {0} % de ton bouclier", SHIELD_PERCENT); }
}
