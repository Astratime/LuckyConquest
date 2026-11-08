package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Grande aiguille (archétype Horloger) : pendant quelques tours, un Ressort de
 * plus au début de chaque tirage.
 */
public class MinuteHandEffect extends Effect {
    private final int turns;
    private final int springs;
    private final int percent;

    /**
     * Grande aiguille.
     * @param turns   tours où elle pose des Ressorts
     * @param springs Ressorts posés à chacun
     * @param percent attaque en plus par Ressort à chaque tirage, en %
     */
    public MinuteHandEffect(int turns, int springs, int percent) {
        this.turns   = turns;
        this.springs = springs;
        this.percent = percent;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().getPlayer().getLastingEffects().getCourtAndClock().addMinuteHand(turns, springs, percent);
    }

    @Override
    public String getDescription() {
        return Lang.n(springs, "Pendant tes {1} prochains tours : +{0} Ressort au début de chaque tour.",
            "Pendant tes {1} prochains tours : +{0} Ressorts au début de chaque tour.", springs, turns);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("GRANDE AIGUILLE"), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ATTACK; }
}
