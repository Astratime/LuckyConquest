package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Pique (non-As) : les attaques ignorent la défense ennemie ce tour et
 * reçoivent un bonus d'attaque. La carte ajoute aussi des Lames, qui restent
 * tout le combat (voir PreparationResolver#BLADE_ATTACK) et que l'As de Pique
 * encaisse. Bonus et Lames augmentent avec le rang.
 */
public class SpadeIgnoreDefenseEffect extends Effect {

    private final int attackBonus;
    private final int blades;

    /**
     * @param attackBonus bonus d'attaque plat accordé pour ce tour
     * @param blades      Lames ajoutées quand la carte est jouée
     */
    public SpadeIgnoreDefenseEffect(int attackBonus, int blades) {
        this.attackBonus = attackBonus;
        this.blades      = blades;
    }

    /** Les Lames s'ajoutent tout de suite (elles apparaissent dans le panneau), le reste au spin. */
    @Override
    public void onPlay(PlayContext context) {
        context.getLastingEffects().addBlades(blades);
        super.onPlay(context);
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().setIgnoreDefense(true);
        context.getCombatContext().addAttackBonus(attackBonus);
    }

    @Override
    public String getDescription() {
        return "Ignore la defense ennemie, Attaque +" + attackBonus + "\nLames +" + blades + " (combat)";
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup("PERCE-DÉFENSE", EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY),
            EffectPopup.scaled("ATTAQUE +" + attackBonus, EffectPopup.Style.ATTACK, attackBonus, PopupScale.CARD_ATTACK_BONUS),
            new EffectPopup("LAMES +" + blades, EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY)
        );
    }
}
