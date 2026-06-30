package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.TurnContext;

/**
 * Coeur (non-As) : ajoute du drain de vie sur tous les symboles d'attaque ce tour.
 * percent augmente avec le rang de la carte (voir Deck.createCard).
 */
public class HeartDrainEffect extends Effect {

    private final int percent;

    public HeartDrainEffect(int percent) { this.percent = percent; }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addLifeDrainPercent(percent);
    }

    @Override
    public String getDescription() { return "Drain de vie +" + percent + "%"; }
}
