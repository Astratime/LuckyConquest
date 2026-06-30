package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;

/** Ajoute un bonus d'attaque via le CombatContext. */
public class AttackEffect extends Effect {

    private final int bonus;

    public AttackEffect(int bonus) { this.bonus = bonus; }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addAttackBonus(bonus);
    }

    @Override
    public String getDescription() { return "Attaque +" + bonus; }
}
