package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.TurnContext;

/** Multiplie les gains de la machine à sous via le CombatContext. */
public class MultiplierEffect extends Effect {

    private final float factor;

    public MultiplierEffect(float factor) { this.factor = factor; }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().setMultiplier(factor);
    }

    @Override
    public String getDescription() { return "Gains machine x" + factor; }
}
