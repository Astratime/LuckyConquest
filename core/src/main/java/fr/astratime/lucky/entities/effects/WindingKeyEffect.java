package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.CourtAndClock;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Remontoir (archétype Horloger) : il pose des Ressorts, qui donnent de
 * l'attaque à chacun des tirages suivants, jusqu'à ce que les Douze coups les vident.
 */
public class WindingKeyEffect extends Effect {
    private final int springs;
    private final int percent;

    /**
     * Remontoir.
     * @param springs Ressorts posés
     * @param percent attaque en plus par Ressort à chaque tirage, en %
     */
    public WindingKeyEffect(int springs, int percent) {
        this.springs = springs;
        this.percent = percent;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().getPlayer().getLastingEffects().getCourtAndClock().addSprings(springs, percent);
    }

    @Override
    public String getDescription() {
        return Lang.f("Pose {0} Ressorts ({1} au plus) : chacun donne +{2} % d'attaque à chacun de tes tirages suivants.",
            springs, CourtAndClock.MAX_SPRINGS, percent);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("RESSORTS +{0}", springs), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ATTACK; }
}
