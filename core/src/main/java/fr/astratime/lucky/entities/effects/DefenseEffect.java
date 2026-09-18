package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;

/** Ajoute un bonus de défense via le CombatContext. */
public class DefenseEffect extends Effect {

    private final int bonus;

    /** @param bonus bonus de bouclier plat accordé pour ce tour. */
    public DefenseEffect(int bonus) { this.bonus = bonus; }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addDefenseBonus(bonus);
    }

    @Override
    public String getDescription() { return "Defense +" + bonus; }
}
