package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Ajoute un bonus de défense via le CombatContext. */
public class DefenseEffect extends Effect {

    private final int bonus;

    /** @param bonus bonus de bouclier plat accordé pour ce tour. */
    public DefenseEffect(int bonus) { this.bonus = bonus; }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addDefenseBonus(bonus);
    }

    @Override
    public String getDescription() { return "Defense +" + bonus; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            EffectPopup.scaled("DÉFENSE +" + bonus, EffectPopup.Style.DEFENSE, bonus, PopupScale.CARD_DEFENSE_BONUS)
        );
    }
}
