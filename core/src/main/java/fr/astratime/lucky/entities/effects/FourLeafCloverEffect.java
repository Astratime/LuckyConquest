package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolRegistry;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Trèfle à quatre feuilles (donjon du Trèfle) : ajoute au multiplicateur de
 * gains et rend les symboles de gain plus fréquents ce tour.
 */
public class FourLeafCloverEffect extends Effect {

    private final int gainMultiplierAdd;
    private final int gainBoost;

    /**
     * @param gainMultiplierAdd valeur ajoutée au multiplicateur de gains
     * @param gainBoost         boost de poids appliqué à chaque symbole de gain pour ce tour
     */
    public FourLeafCloverEffect(int gainMultiplierAdd, int gainBoost) {
        this.gainMultiplierAdd = gainMultiplierAdd;
        this.gainBoost         = gainBoost;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addGainMultiplier(gainMultiplierAdd);
        for (Symbol symbol : SymbolRegistry.getGainSymbols()) {
            context.getSpinContext().addWeightBoost(symbol, gainBoost);
        }
    }

    @Override
    public String getDescription() {
        if (gainMultiplierAdd == 0) return "Les symboles de gain sortent plus souvent"; // Rhum
        return "Multiplicateur de gains +" + gainMultiplierAdd + ". Les symboles de gain sortent plus souvent";
    }

    @Override
    public List<EffectPopup> getPopups() {
        EffectPopup luck = new EffectPopup("CHANCE DE GAINS", EffectPopup.Style.GAINS, PopupScale.SECONDARY_INTENSITY);
        if (gainMultiplierAdd == 0) return List.of(luck);
        return List.of(
            EffectPopup.scaled("GAINS x+" + gainMultiplierAdd, EffectPopup.Style.GAINS, gainMultiplierAdd, PopupScale.CARD_GAIN_MULTIPLIER),
            luck);
    }

    @Override
    public EffectSound getSound() { return EffectSound.CLUB_GAIN_ATTACK; }
}
