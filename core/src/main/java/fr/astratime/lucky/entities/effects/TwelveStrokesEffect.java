package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.events.StatusEvent;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Douze coups (archétype Horloger) : les Ressorts se vident d'un coup, et
 * chacun donne beaucoup d'attaque à ce tirage.
 */
public class TwelveStrokesEffect extends Effect {
    private final int percent;

    /** @param percent attaque en plus par Ressort vidé, en % */
    public TwelveStrokesEffect(int percent) { this.percent = percent; }

    @Override
    public void apply(TurnContext context) {
        CombatContext combat = context.getCombatContext();
        int springs = combat.getPlayer().getLastingEffects().getCourtAndClock().takeSprings();
        if (springs <= 0) return;
        combat.multiplyAttack(1f + springs * percent / 100f, "Douze coups");
        context.addEvent(new StatusEvent(Lang.f("DOUZE COUPS : {0} RESSORTS, ATTAQUE +{1} %", springs, springs * percent),
            EffectPopup.Style.ATTACK));
    }

    @Override
    public String getDescription() {
        return Lang.f("Vide tes Ressorts : attaque +{0} % par Ressort à ce tirage.", percent);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("DOUZE COUPS"), EffectPopup.Style.ATTACK, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.MULTIPLIER; }
}
