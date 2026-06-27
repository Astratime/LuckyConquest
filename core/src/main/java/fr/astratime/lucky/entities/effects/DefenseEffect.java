package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.CardEffect;
import fr.astratime.lucky.entities.GameState;

/** Ajoute un bonus de défense pour ce tour. */
public class DefenseEffect implements CardEffect {

    private final int bonus;

    public DefenseEffect(int bonus) {
        this.bonus = bonus;
    }

    @Override
    public void apply(GameState state) {
        state.getTurnContext().addDefenseBonus(bonus);
    }

    @Override
    public String getDescription() {
        return "Defense +" + bonus;
    }
}
