package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.choices.CalibrationChoice;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Calibrage (archétype Relanceur) : le joueur choisit un symbole qui quitte ses rouleaux quelques
 * tirages (voir GameController#calibrate), et le tirage a plus de chances de faire une paire.
 */
public class CalibrationEffect extends Effect {

    private final int turns;
    private final int percent;

    /**
     * @param turns   tirages sans le symbole choisi, celui-ci compris
     * @param percent chance de paire en plus à ce tirage, en %
     */
    public CalibrationEffect(int turns, int percent) {
        this.turns   = turns;
        this.percent = percent;
    }

    @Override
    public void onPlay(PlayContext context) {
        context.requestChoice(new CalibrationChoice(turns));
        context.queueForSpin(this);
        context.addPopups(getPopups());
    }

    @Override
    public void apply(TurnContext context) { context.getSpinContext().addPairChance(percent / 100f); }

    @Override
    public String getDescription() {
        return Lang.f("Choisis un symbole : il quitte tes rouleaux pendant {0} tirages. Paire +{1} %.", turns, percent);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("CALIBRAGE"), EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY),
            new EffectPopup(Lang.f("AIMANT : PAIRE +{0}%", percent), EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.RECYCLE; }
}
