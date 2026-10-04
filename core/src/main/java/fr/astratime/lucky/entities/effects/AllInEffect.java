package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Tapis : tous les gains sont misés. Une paire les triple ; sinon, ils sont perdus. */
public class AllInEffect extends Effect {

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addAllIn();
    }

    @Override
    public String getDescription() { return "Mise tous tes gains. Paire : gains x3. Sinon, tu perds tout."; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("TAPIS !", EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ALL_IN; }
}
