package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;

/**
 * Trefle (non-As) : ajoute un multiplicateur de gains et un bonus d'attaque ce tour.
 * Les deux augmentent avec le rang de la carte (voir Deck.createCard).
 */
public class ClubGainAttackEffect extends Effect {

    private final int gainMultiplierAdd;
    private final int   attackBonus;

    public ClubGainAttackEffect(int gainMultiplierAdd, int attackBonus) {
        this.gainMultiplierAdd = gainMultiplierAdd;
        this.attackBonus       = attackBonus;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addGainMultiplier(gainMultiplierAdd);
        context.getCombatContext().addAttackBonus(attackBonus);
    }

    @Override
    public String getDescription() {
        return "Gains x+" + gainMultiplierAdd + ", Attaque +" + attackBonus;
    }
}
