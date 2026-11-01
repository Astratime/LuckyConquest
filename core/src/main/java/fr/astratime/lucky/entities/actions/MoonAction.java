package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.i18n.Lang;

/** Lune : une attaque qui gagne +1 % par % de PV perdus par le joueur (jusqu'à x2 au bord de la mort). */
public final class MoonAction extends AttackAction {

    public MoonAction(int baseDamage) { super(baseDamage); }

    @Override
    protected double hitFactor(CombatContext context) {
        Player player = context.getPlayer();
        return 1.0 + Math.max(0f, Math.min(1f, 1f - player.getHpRatio()));
    }

    @Override
    public String getDescription() {
        return Lang.f("{0} dégâts de base, +1 % par % de PV perdus", getBaseDamage());
    }
}
