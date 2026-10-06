package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

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
    public String getDescription() {
        return Lang.f("Drain de vie : soigne de {0}% des dégâts infligés. Le surplus remplit le Sang", percent);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            EffectPopup.scaled(Lang.f("DRAIN DE VIE +{0}%", percent), EffectPopup.Style.DRAIN, percent, PopupScale.CARD_DRAIN_PERCENT)
        );
    }

    @Override
    public EffectSound getSound() { return EffectSound.HEART_DRAIN; }
}
