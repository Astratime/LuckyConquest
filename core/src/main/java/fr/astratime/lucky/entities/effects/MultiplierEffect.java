package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
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
    public String getDescription() { return "Gains x+" + amount; }

    /** @return le montant sans décimale inutile (2.0 -> "2", 1.5 -> "1.5"). */
    private String formatAmount() {
        return amount == (int) amount ? String.valueOf((int) amount) : String.valueOf(amount);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            EffectPopup.scaled("GAINS x+" + formatAmount(), EffectPopup.Style.GAINS, amount, PopupScale.CARD_GAIN_MULTIPLIER)
        );
    }
}
