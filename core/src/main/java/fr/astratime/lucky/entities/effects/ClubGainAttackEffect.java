package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Trefle (non-As) : ajoute un multiplicateur de gains et un bonus d'attaque ce tour.
 * Les deux augmentent avec le rang de la carte (voir cards/definitions/).
 */
public class ClubGainAttackEffect extends Effect {

    private final int gainMultiplierAdd;
    private final int   attackBonus;

    /**
     * @param gainMultiplierAdd valeur ajoutée au multiplicateur de gains
     * @param attackBonus       bonus d'attaque plat accordé pour ce tour
     */
    public ClubGainAttackEffect(int gainMultiplierAdd, int attackBonus) {
        this.gainMultiplierAdd = gainMultiplierAdd;
        this.attackBonus       = attackBonus;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addGainMultiplier(gainMultiplierAdd);
        context.getCombatContext().addAttackBonus(attackBonus);
    }

    @Override
    public String getDescription() {
        return "Gains x+" + gainMultiplierAdd + ", Attaque +" + attackBonus;
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            EffectPopup.scaled("GAINS x+" + gainMultiplierAdd, EffectPopup.Style.GAINS, gainMultiplierAdd, PopupScale.CARD_GAIN_MULTIPLIER),
            EffectPopup.scaled("ATTAQUE +" + attackBonus, EffectPopup.Style.ATTACK, attackBonus, PopupScale.CARD_ATTACK_BONUS)
        );
    }
}
