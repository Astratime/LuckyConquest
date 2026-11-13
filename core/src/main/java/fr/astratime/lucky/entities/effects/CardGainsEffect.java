package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Gains des cartes classiques (2 au Roi, toutes couleurs) : la carte ajoute sa part au
 * multiplicateur de gains du tour, selon sa valeur (voir cards/definitions/). Aucune
 * couleur n'est « la couleur des gains » (Astra, 2026-11-13).
 */
public class CardGainsEffect extends Effect {

    private final float gainMultiplierAdd;

    /** @param gainMultiplierAdd valeur ajoutée au multiplicateur de gains */
    public CardGainsEffect(float gainMultiplierAdd) { this.gainMultiplierAdd = gainMultiplierAdd; }

    /** @return la valeur ajoutée au multiplicateur de gains. */
    public float getGainMultiplierAdd() { return gainMultiplierAdd; }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().addGainMultiplier(gainMultiplierAdd); }

    @Override
    public String getDescription() { return Lang.f("Multiplicateur de gains +{0}", format()); }

    private String format() {
        return gainMultiplierAdd == Math.round(gainMultiplierAdd) ? String.valueOf(Math.round(gainMultiplierAdd))
            : Lang.decimal(String.valueOf(gainMultiplierAdd));
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(EffectPopup.scaled(Lang.f("GAINS x+{0}", format()), EffectPopup.Style.GAINS,
            Math.round(gainMultiplierAdd), PopupScale.CARD_GAIN_MULTIPLIER));
    }

    @Override
    public EffectSound getSound() { return EffectSound.MULTIPLIER; }
}
