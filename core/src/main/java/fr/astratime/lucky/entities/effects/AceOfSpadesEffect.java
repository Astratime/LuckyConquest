package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * As de Pique : les attaques ignorent la défense ennemie et rapportent
 * des gains égaux aux dégâts infligés (voir AttackAction.resolve).
 */
public class AceOfSpadesEffect extends Effect {

    /** Active l'ignorance de la défense ennemie et la conversion des dégâts en gains pour ce tour. */
    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().setIgnoreDefense(true);
        context.getCombatContext().setGainsFromDamage(true);
    }

    @Override
    public String getDescription() {
        return "Ignore la defense ennemie, gains = degats infliges";
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup("PERCE-DÉFENSE", EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY),
            new EffectPopup("GAINS = DÉGÂTS", EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY)
        );
    }
}
