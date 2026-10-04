package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Chope (la Taverne) : chaque combinaison de ce tour compte un peu plus
 * au multiplicateur.
 */
public class ComboBonusEffect extends Effect {

    private final float bonus;

    /** @param bonus ajouté au multiplicateur de chaque combinaison */
    public ComboBonusEffect(float bonus) { this.bonus = bonus; }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().addComboBonus(bonus); }

    private String bonusText() { return bonus == Math.round(bonus) ? String.valueOf(Math.round(bonus)) : String.valueOf(bonus); }

    @Override
    public String getDescription() { return "Ce tour, chaque combinaison compte +" + bonusText() + " au multiplicateur."; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("COMBINAISONS +" + bonusText(), EffectPopup.Style.GAINS, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.MULTIPLIER; }
}
