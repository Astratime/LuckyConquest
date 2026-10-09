package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.BoostBreakdown;
import fr.astratime.lucky.entities.SpinEconomy;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GainsEarnedEvent;
import fr.astratime.lucky.entities.events.StatusEvent;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;

import java.util.ArrayList;
import java.util.List;

/**
 * Accorde des gains au joueur, multipliés par le gainMultiplier (cartes Trèfle),
 * le facteur de gains du tirage (combos, Porte-bonheur) et la puissance des
 * symboles (Bingo). Le bonus de gains du rang du joueur s'ajoute aux gains de base.
 */
public class GainAction extends Action {

    private final int baseGain;

    /** @param baseGain gain de base avant application du multiplicateur. */
    public GainAction(int baseGain) { this.baseGain = baseGain; }

    /**
     * Crédite au joueur {@code (baseGain + bonus du rang) * gainMultiplier * gainFactor * symbolPower},
     * à l'échelle du coût du tirage (x1 pour 100, x50 pour 5 000), arrondi.
     */
    @Override
    public List<Event> resolve(CombatContext context) {
        if (context.isStoneGains()) { // Pépite de l'ennemi : le symbole n'est qu'une pierre
            return List.of(new StatusEvent(Lang.t("PIERRE : GAINS 0"), EffectPopup.Style.DAMAGE));
        }
        int rank = context.getPlayer().getRankBonus().gains() * context.getRankFactor();
        int base = baseGain + rank;
        float scale = SpinEconomy.gainScale(context.getSpinCost());
        int gain = Math.round(base * context.getGainMultiplier() * context.getGainFactor()
            * context.getSymbolPower() * scale);
        context.getPlayer().addGains(gain);
        // Le détail : les cartes Trèfle (multiplicateur additionné) et le coût du tirage s'ajoutent aux étapes du tour.
        List<CombatContext.PowerStep> steps = new ArrayList<>();
        if (context.getGainMultiplier() != 1f) {
            steps.add(new CombatContext.PowerStep(CombatContext.Target.GAINS, "Cartes", 0, context.getGainMultiplier()));
        }
        steps.addAll(context.getSteps(CombatContext.Target.GAINS));
        if (scale != 1f) steps.add(new CombatContext.PowerStep(CombatContext.Target.GAINS, "Coût du tirage", 0, scale));
        return List.of(new GainsEarnedEvent(gain).withBreakdown(
            new BoostBreakdown(BoostBreakdown.Kind.GAINS, baseGain, rank, steps, gain)));
    }

    @Override
    public String getDescription() { return Lang.f("{0} gains de base", baseGain); }
}
