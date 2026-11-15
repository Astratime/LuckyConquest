package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Taux usuraire (carte du Roi de Trèfle) : les gains du tirage augmentent.
 */
public class TurnGainsEffect extends Effect {
    private final int percent;

    /** @param percent gains du tirage en plus, en % */
    public TurnGainsEffect(int percent) { this.percent = percent; }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().multiplyGains(1f + percent / 100f, "Taux usuraire"); }

    @Override
    public String getDescription() { return Lang.f("Gains du tour +{0} %.", percent); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("GAINS DU TOUR +{0} %", percent), EffectPopup.Style.GAINS, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.GAINS_MULTIPLIER; }
}
