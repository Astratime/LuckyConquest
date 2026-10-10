package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolRegistry;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Carreau (non-As) : augmente la probabilité de tirer un symbole de défense et
 * renvoie à l'ennemi, s'il attaque, une part de ses PV max. Le renvoi
 * s'additionne sur toutes les cartes Carreau jouées ce tour (plafonné à
 * CombatContext#MAX_REFLECT_HP_PERCENT), et s'active si au moins un symbole de
 * défense sort (il frappe au tour de l'ennemi, voir EnemyTurnResolver).
 * Les deux valeurs augmentent avec le rang (voir cards/definitions/carreau.json).
 */
public class DiamondReflectEffect extends Effect {

    private final float hpPercent;
    private final int   defenseBoost;

    /**
     * @param hpPercent    part (%) des PV max de l'ennemi renvoyée s'il attaque (additionnée aux autres cartes Carreau)
     * @param defenseBoost boost de poids appliqué à chaque symbole de défense pour ce tour
     */
    public DiamondReflectEffect(float hpPercent, int defenseBoost) {
        this.hpPercent    = hpPercent;
        this.defenseBoost = defenseBoost;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addReflectHpPercent(hpPercent);
        context.getCombatContext().addDefenseBonus(defenseBoost, "Cartes"); // bouclier en plus : il remplit le Coffre
        if (defenseBoost > 0) {
            for (Symbol symbol : SymbolRegistry.getDefenseSymbols()) {
                context.getSpinContext().addWeightBoost(symbol, defenseBoost);
            }
        }
    }

    @Override
    public String getDescription() {
        return Lang.f("Si un symbole de défense sort et que l'ennemi attaque, il perd {0}% de ses PV max "
            + "(+20% du Coffre ; {1}% au plus avec tous les Carreaux){2}",
            formatPercent(hpPercent), formatPercent(CombatContext.MAX_REFLECT_HP_PERCENT),
            (defenseBoost > 0 ? Lang.f(". Symboles de défense : bouclier +{0} et plus fréquents", defenseBoost) : ""));
    }

    @Override
    public List<EffectPopup> getPopups() {
        EffectPopup reflect = EffectPopup.scaled(Lang.f("RENVOI {0}% DES PV", formatPercent(hpPercent)),
            EffectPopup.Style.REFLECT, Math.round(hpPercent * 100), PopupScale.CARD_REFLECT_PERCENT);
        if (defenseBoost <= 0) return List.of(reflect);
        return List.of(reflect, EffectPopup.scaled(Lang.f("BOOST DÉFENSE +{0}", defenseBoost), EffectPopup.Style.DEFENSE,
            defenseBoost, PopupScale.CARD_DEFENSE_BOOST));
    }

    /** @return {@code percent} sans décimale inutile, avec la virgule en français (ex : "3,75", "1"). */
    private static String formatPercent(float percent) {
        String text = percent == (int) percent ? String.valueOf((int) percent)
            : new java.math.BigDecimal(Float.toString(percent)).stripTrailingZeros().toPlainString();
        return !Lang.isEnglish() ? text.replace('.', ',') : text;
    }

    @Override
    public EffectSound getSound() { return EffectSound.DIAMOND_REFLECT; }
}
