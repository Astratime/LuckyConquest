package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.LifeDrain;
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

    /** @param percent drain de vie, en % (voir {@link LifeDrain}). */
    public HeartDrainEffect(int percent) { this.percent = percent; }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addLifeDrainPercent(percent);
    }

    @Override
    public String getDescription() {
        return Lang.f("Drain de vie {0} % : enlever 10 % des PV de l'ennemi te soigne de {1} % de tes PV max ({2} % au plus "
            + "par tour). La moitié du surplus remplit le Sang", percent, LifeDrain.per10(percent), LifeDrain.TURN_CAP_PERCENT);
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
