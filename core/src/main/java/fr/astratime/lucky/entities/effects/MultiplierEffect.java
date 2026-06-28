package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.GameState;

/** Multiplie les gains de la machine à sous ce tour. */
public class MultiplierEffect extends Effect {

    private final float factor;

    public MultiplierEffect(float factor) {
        this.factor = factor;
    }

    @Override
    public void apply(GameState state) {
        state.getTurnContext().setMultiplier(factor);
    }

    @Override
    public String getDescription() {
        return "Gains machine x" + factor;
    }
}
