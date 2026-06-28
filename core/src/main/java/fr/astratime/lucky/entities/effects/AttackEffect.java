package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.GameState;

/** Ajoute un bonus d'attaque pour ce tour. */
public class AttackEffect extends Effect {

    private final int bonus;

    public AttackEffect(int bonus) {
        this.bonus = bonus;
    }

    @Override
    public void apply(GameState state) {
        state.getTurnContext().addAttackBonus(bonus);
    }

    @Override
    public String getDescription() {
        return "Attaque +" + bonus;
    }
}
