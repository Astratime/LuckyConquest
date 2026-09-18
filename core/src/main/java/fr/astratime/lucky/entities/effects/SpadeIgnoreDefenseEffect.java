package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;

/**
 * Pique (non-As) : les attaques ignorent la défense ennemie ce tour
 * et reçoivent un bonus d'attaque. Le bonus augmente avec le rang.
 */
public class SpadeIgnoreDefenseEffect extends Effect {

    private final int attackBonus;

    /** @param attackBonus bonus d'attaque plat accordé pour ce tour. */
    public SpadeIgnoreDefenseEffect(int attackBonus) { this.attackBonus = attackBonus; }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().setIgnoreDefense(true);
        context.getCombatContext().addAttackBonus(attackBonus);
    }

    @Override
    public String getDescription() {
        return "Ignore la defense ennemie, Attaque +" + attackBonus;
    }
}
