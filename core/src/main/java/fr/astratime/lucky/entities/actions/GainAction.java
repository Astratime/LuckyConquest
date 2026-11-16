package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.BigNumber;
import fr.astratime.lucky.entities.BoostBreakdown;
import fr.astratime.lucky.entities.GainFormula;
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
 * Accorde des gains au joueur : la base du symbole x la table des symboles x C / 100
 * ({@link SpinEconomy#symbolScale}), x le multiplicateur du joueur ({@link GainFormula} :
 * rang, Prestige, cartes, combinaisons, carte Bingo).
 */
public non-sealed class GainAction extends Action {

    private final int baseGain;

    /** @param baseGain gain de base avant application du multiplicateur. */
    public GainAction(int baseGain) { this.baseGain = baseGain; }

    /** Crédite au joueur {@code baseGain x table x C / 100 x J} ({@link GainFormula#player}), arrondi. */
    @Override
    public List<Event> resolve(CombatContext context) {
        if (context.isStoneGains()) { // Pépite de l'ennemi : le symbole n'est qu'une pierre
            return List.of(new StatusEvent(Lang.t("PIERRE : GAINS 0"), EffectPopup.Style.DAMAGE));
        }
        int symbol = rollBaseGain(context);
        double table = (double) symbol * SpinEconomy.SYMBOL_TABLE; // sa valeur dans la table, à 100 de coût de tirage
        float scale = SpinEconomy.gainScale(context.getSpinCost());
        double gain = BigNumber.round(table * scale * GainFormula.player(context) * gainFactor(context));
        context.getPlayer().addGains(gain);
        // Le détail : la valeur du symbole dans la table, puis rang, Prestige, cartes et les étapes du tour.
        List<CombatContext.PowerStep> steps = new ArrayList<>();
        double rank = GainFormula.rankFactor(context.getPlayer(), context.getRankFactor());
        if (rank != 1.0) steps.add(new CombatContext.PowerStep(CombatContext.Target.GAINS, "Rang", 0, (float) rank));
        double prestige = GainFormula.prestigeFactor(context.getPlayer());
        if (prestige != 1.0) {
            steps.add(new CombatContext.PowerStep(CombatContext.Target.GAINS, CombatContext.PRESTIGE, 0, (float) prestige));
        }
        double cards = GainFormula.cardFactor(context);
        if (cards != 1.0) steps.add(new CombatContext.PowerStep(CombatContext.Target.GAINS, "Cartes", 0, (float) cards));
        steps.addAll(context.getSteps(CombatContext.Target.GAINS));
        if (scale != 1f) steps.add(new CombatContext.PowerStep(CombatContext.Target.GAINS, "Coût du tirage", 0, scale));
        return List.of(new GainsEarnedEvent(gain).withBreakdown(
            new BoostBreakdown(BoostBreakdown.Kind.GAINS, table, 0, steps, gain)));
    }

    /** @return les gains de base de cette sortie (la Tirelire grossit, le Ticket les tire au hasard). */
    protected int rollBaseGain(CombatContext context) { return baseGain; }

    /** @return le multiplicateur propre à ce rouleau (le Jeton d'or), 1 par défaut. */
    protected double gainFactor(CombatContext context) { return 1.0; }

    /** @return les gains de base affichés (description). */
    protected int getBaseGain() { return baseGain; }

    @Override
    public String getDescription() { return Lang.f("{0} gains de base", SpinEconomy.symbolBaseText(baseGain)); }
}
