package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Effet générique de multiplicateur de gains (utilisable hors thème de suite). */
public class MultiplierEffect extends Effect {

    private final float amount;

    /** @param amount valeur ajoutée au multiplicateur de gains. */
    public MultiplierEffect(float amount) { this.amount = amount; }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addGainMultiplier(amount);
    }

    @Override
    public String getDescription() { return Lang.f("Gains +{0} %", fr.astratime.lucky.entities.GainFormula.cardPercentText(amount)); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            EffectPopup.scaled(Lang.f("GAINS +{0} %", fr.astratime.lucky.entities.GainFormula.cardPercentText(amount)), EffectPopup.Style.GAINS, amount, PopupScale.CARD_GAIN_MULTIPLIER)
        );
    }

    @Override
    public EffectSound getSound() { return EffectSound.MULTIPLIER; }
}
