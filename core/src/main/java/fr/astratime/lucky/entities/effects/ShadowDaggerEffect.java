package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Dague de l'ombre (donjon du Pique) : les attaques ignorent la défense
 * ennemie, et chaque Lame donne un bonus d'attaque ce tour, sans être consommée.
 */
public class ShadowDaggerEffect extends Effect {

    private final int attackPerBlade;

    /** @param attackPerBlade bonus d'attaque par Lame, pour ce tour */
    public ShadowDaggerEffect(int attackPerBlade) { this.attackPerBlade = attackPerBlade; }

    @Override
    public void apply(TurnContext context) {
        CombatContext combat = context.getCombatContext();
        combat.setIgnoreDefense(true);
        combat.addAttackBonus(attackPerBlade * combat.getPlayer().getLastingEffects().getBlades());
    }

    @Override
    public String getDescription() {
        return "Les symboles d'attaque ignorent la défense. Attaque +" + attackPerBlade + " par Lame. Les Lames restent";
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup("DÉFENSE IGNORÉE", EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY),
            new EffectPopup("ATTAQUE +" + attackPerBlade + " PAR LAME", EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.SPADE_IGNORE_DEFENSE; }
}
