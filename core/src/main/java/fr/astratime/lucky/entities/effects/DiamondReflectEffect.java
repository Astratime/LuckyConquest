package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.TurnContext;

/**
 * Carreau (non-As) : ajoute un pourcentage de renvoi de dégâts pour chaque
 * symbole de défense tiré ce tour. percent augmente avec le rang.
 * Le renvoi effectif sera consommé lors du tour ennemi (pas encore implémenté).
 */
public class DiamondReflectEffect extends Effect {

    private final int percent;

    public DiamondReflectEffect(int percent) { this.percent = percent; }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addReflectPercentBonus(percent);
    }

    @Override
    public String getDescription() { return "Renvoi de degats +" + percent + "% par symbole de defense"; }
}
