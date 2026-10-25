package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Chamboule-tout (archétype Relanceur, coffre des Stands) : au tirage, chaque rouleau qui ne fait
 * pas partie d'une paire relance une fois (voir {@link fr.astratime.lucky.controllers.TurnEngine}).
 */
public class KnockdownEffect extends Effect {

    @Override
    public void onPlay(PlayContext context) {
        context.queueForSpin(this);
        context.addPopups(getPopups());
    }

    @Override
    public void apply(TurnContext context) { context.getSpinContext().addKnockdown(); }

    @Override
    public String getDescription() { return Lang.t("Au tirage, chaque rouleau qui ne fait pas partie d'une paire est relancé une fois."); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("CHAMBOULE-TOUT"), EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.REROLL; }
}
