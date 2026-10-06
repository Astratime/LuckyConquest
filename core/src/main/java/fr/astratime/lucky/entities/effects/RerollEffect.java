package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Relance : si le tirage n'a pas de paire, la machine relance une fois. */
public class RerollEffect extends Effect {

    @Override
    public void apply(TurnContext context) {
        context.getSpinContext().addReroll();
    }

    @Override
    public String getDescription() { return Lang.t("Pas de paire ? La machine relance une fois."); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("RELANCE PRÊTE"), EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.REROLL; }
}
