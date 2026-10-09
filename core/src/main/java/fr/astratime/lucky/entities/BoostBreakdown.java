package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.context.CombatContext.PowerStep;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.List;

/**
 * Le calcul du bouclier ou des gains d'un symbole, étape par étape : valeur du
 * symbole, bonus du rang et des cartes, puis multiplicateurs (combinaisons,
 * Bingo, Mise, Porte-bonheur, coût du tirage...).
 *
 * @param kind   bouclier ou gains
 * @param base   valeur du symbole
 * @param rank   bonus du rang (Jeton de rang compris)
 * @param steps  bonus plats et multiplicateurs du tour, dans l'ordre
 * @param result bouclier ou gains obtenus
 */
public record BoostBreakdown(Kind kind, int base, int rank, List<PowerStep> steps, int result)
    implements SymbolBreakdown {

    /** Ce que donne le symbole. */
    public enum Kind { SHIELD, GAINS }

    public BoostBreakdown {
        steps = List.copyOf(steps);
    }

    @Override
    public List<String> lines(Symbol symbol) {
        List<String> lines = new ArrayList<>();
        lines.add(Lang.f("{0} : {1}", symbol.getDisplayName(), Lang.big(base)));
        if (rank != 0) lines.add(Lang.f("{0} : +{1}", Lang.t("Rang"), Lang.big(rank)));
        DamageBreakdown.addSteps(lines, steps);
        lines.add(Lang.f(kind == Kind.SHIELD ? "= {0} de bouclier" : "= {0} gains", Lang.big(result)));
        return lines;
    }
}
