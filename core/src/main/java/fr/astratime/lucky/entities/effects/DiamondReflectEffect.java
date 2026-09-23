package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;

import java.util.List;

/**
 * Carreau (non-As) : ajoute un pourcentage de renvoi de dégâts, activé par un
 * symbole de défense tiré ce tour. percent augmente avec le rang.
 * Consommé lors de la riposte ennemie par CombatResolver.
 */
public class DiamondReflectEffect extends Effect {

    private final int percent;

    /** @param percent pourcentage de renvoi de dégâts additionné par symbole de défense. */
    public DiamondReflectEffect(int percent) { this.percent = percent; }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addReflectPercentBonus(percent);
    }

    @Override
    public String getDescription() { return "Renvoi de degats +" + percent + "% par symbole de defense"; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            EffectPopup.scaled("RENVOI +" + percent + "%", EffectPopup.Style.REFLECT, percent, PopupScale.CARD_REFLECT_PERCENT)
        );
    }
}
