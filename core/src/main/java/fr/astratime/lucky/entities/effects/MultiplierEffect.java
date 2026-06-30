package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;

/** Effet générique de multiplicateur de gains (utilisable hors thème de suite). */
public class MultiplierEffect extends Effect {

    private final float amount;

    public MultiplierEffect(float amount) { this.amount = amount; }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addGainMultiplier(amount);
    }

    @Override
    public String getDescription() { return "Gains x+" + amount; }
}
