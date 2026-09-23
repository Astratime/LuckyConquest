package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;

import java.util.List;

/**
 * Coeur (non-As) : ajoute du drain de vie sur tous les symboles d'attaque ce tour.
 * percent augmente avec le rang de la carte (voir cards/definitions/).
 */
public class HeartDrainEffect extends Effect {

    private final int percent;

    /** @param percent pourcentage des dégâts infligés rendu en soin. */
    public HeartDrainEffect(int percent) { this.percent = percent; }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addLifeDrainPercent(percent);
    }

    @Override
    public String getDescription() { return "Drain de vie +" + percent + "%"; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            EffectPopup.scaled("DRAIN DE VIE +" + percent + "%", EffectPopup.Style.DRAIN, percent, PopupScale.CARD_DRAIN_PERCENT)
        );
    }
}
