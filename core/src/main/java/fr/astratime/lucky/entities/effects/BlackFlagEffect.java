package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Pavillon noir (le Galion) : ce tour, chaque dégât infligé rapporte autant
 * de gains.
 */
public class BlackFlagEffect extends Effect {

    @Override
    public void apply(TurnContext context) { context.getCombatContext().setGainsFromDamage(true); }

    @Override
    public String getDescription() { return "Ce tour, chaque dégât fait te rapporte autant de gains."; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("PAVILLON NOIR : DÉGÂTS = GAINS", EffectPopup.Style.GAINS, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public boolean canBeDoubled() { return false; }

    @Override
    public EffectSound getSound() { return EffectSound.GAIN; }
}
