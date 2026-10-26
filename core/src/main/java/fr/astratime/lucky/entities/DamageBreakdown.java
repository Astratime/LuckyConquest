package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.context.CombatContext.PowerStep;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.List;

/**
 * Le calcul d'un coup d'un symbole sur l'ennemi, étape par étape, pour le
 * détail du coup affiché au survol du rouleau : attaque du symbole, bonus du
 * rang et des cartes, multiplicateurs (combinaisons, Bingo, Mise,
 * Corruption...), puis défense absorbée ou percée.
 *
 * @param base     attaque du symbole
 * @param rank     bonus d'attaque du rang (Jeton de rang compris)
 * @param steps    bonus plats et multiplicateurs du tour, dans l'ordre
 * @param raw      coup qui frappe la défense (sans plafond : le vrai nombre, même au-delà des PV)
 * @param pierce   part (%) du coup qui traverse la défense
 * @param blocked  dégâts absorbés par la défense
 * @param damage   dégâts infligés
 */
public record DamageBreakdown(int base, int rank, List<PowerStep> steps, long raw, int pierce,
                              int blocked, long damage) implements SymbolBreakdown {

    public DamageBreakdown {
        steps = List.copyOf(steps);
    }

    @Override
    public List<String> lines(Symbol symbol) {
        List<String> lines = new ArrayList<>();
        lines.add(Lang.f("{0} : {1}", symbol.getDisplayName(), Lang.big(base)));
        if (rank != 0) lines.add(Lang.f("{0} : +{1}", Lang.t("Rang"), Lang.big(rank)));
        addSteps(lines, steps);
        if (pierce >= 100) {
            lines.add(Lang.t("Défense ignorée"));
        } else if (pierce > 0) {
            lines.add(Lang.f("Défense percée : {0} %", pierce));
        }
        if (blocked > 0) lines.add(Lang.f("Défense : -{0}", Lang.big(blocked)));
        if (damage < raw - blocked) lines.add(Lang.f("Encaissé par l'ennemi : -{0}", Lang.big(raw - blocked - damage)));
        lines.add(Lang.f("= {0} dégâts", Lang.big(damage)));
        return lines;
    }

    /**
     * Ajoute à {@code lines} les bonus plats de {@code steps} (ils s'ajoutent d'abord), puis leurs multiplicateurs.
     * Les étapes de même nom (ex : deux copies d'une carte) tiennent sur une seule ligne.
     */
    static void addSteps(List<String> lines, List<PowerStep> steps) {
        java.util.Map<String, Double> bonuses = new java.util.LinkedHashMap<>();
        java.util.Map<String, Float>  factors = new java.util.LinkedHashMap<>();
        for (PowerStep step : steps) {
            if (step.isFactor()) factors.merge(step.label(), step.factor(), (a, b) -> a * b);
            else bonuses.merge(step.label(), (double) step.bonus(), BigNumber::add);
        }
        bonuses.forEach((label, bonus) -> lines.add(Lang.f("{0} : +{1}", Lang.t(label), Lang.big(bonus))));
        factors.forEach((label, factor) -> lines.add(Lang.f("{0} : x{1}", Lang.t(label), factor(factor))));
    }

    /** @return {@code factor} avec deux décimales au plus, sans zéro inutile (x9,5 ; x0,75). */
    static String factor(float factor) {
        String text = String.format(java.util.Locale.ROOT, "%.2f", factor);
        text = text.contains(".") ? text.replaceAll("0+$", "").replaceAll("\\.$", "") : text;
        return Lang.decimal(text);
    }
}
