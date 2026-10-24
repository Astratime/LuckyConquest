package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Taux variable (archétype Rentier) : une Créance par symbole de gains sorti ce tour.
 */
public class VariableRateEffect extends Effect {
    private final int perSymbol;

    /**
     * Taux variable.
     * @param perSymbol Créances par symbole de gains sorti
     */
    public VariableRateEffect(int perSymbol) {
        this.perSymbol = perSymbol;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addVariableRate(perSymbol);
    }

    @Override
    public String getDescription() { return Lang.f("Ce tour, +{0} Créance par symbole de gains sorti.", perSymbol); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("TAUX VARIABLE"), EffectPopup.Style.DRAIN, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.GAIN; }

}
