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
import java.math.BigDecimal;

/**
 * Carreau (non-As) : augmente la probabilité de tirer un symbole de défense et
 * riposte si l'ennemi attaque : une part de ses coups, plus 20 % du Coffre et le bonus
 * d'attaque du rang, le tout multiplié comme l'attaque du joueur ce tour (combos, Bingo).
 * Plus rien sur ses PV max (Astra 2026-11-06 : trop fort à haut niveau).
 * Le renvoi s'additionne sur toutes les cartes Carreau jouées ce tour (plafonné à
 * CombatContext#MAX_REFLECT_ATTACK_PERCENT), et s'active si au moins un symbole de
 * défense sort (il frappe au tour de l'ennemi, voir EnemyTurnResolver).
 * Les deux valeurs augmentent avec le rang (voir cards/definitions/carreau.json).
 */
public class DiamondReflectEffect extends Effect {

    private final float attackPercent;
    private final int   defenseBoost;
    private final int   vaultBonus;

    public DiamondReflectEffect(float attackPercent, int defenseBoost) { this(attackPercent, defenseBoost, 0); }

    /**
     * @param attackPercent part (%) de l'attaque de l'ennemi qui lui est renvoyée (additionnée aux autres cartes Carreau)
     * @param defenseBoost boost de poids appliqué à chaque symbole de défense pour ce tour
     * @param vaultBonus   % ajouté au multiplicateur du bouclier restant versé au Coffre en fin de tour (additionné)
     */
    public DiamondReflectEffect(float attackPercent, int defenseBoost, int vaultBonus) {
        this.attackPercent = attackPercent;
        this.defenseBoost = defenseBoost;
        this.vaultBonus   = vaultBonus;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addReflectAttackPercent(attackPercent);
        context.getCombatContext().addVaultBonus(vaultBonus);
        context.getCombatContext().addDefenseBonus(defenseBoost, "Cartes"); // bouclier en plus : il remplit le Coffre
        if (defenseBoost > 0) {
            for (Symbol symbol : SymbolRegistry.getDefenseSymbols()) {
                context.getSpinContext().addWeightBoost(symbol, defenseBoost);
            }
        }
    }

    @Override
    public String getDescription() {
        return Lang.f("Si un symbole de défense sort et que l'ennemi attaque, riposte : {0}% de ses coups "
            + "+ 20% du Coffre + bonus d'attaque du rang, multipliés par tes combos ({1}% de ses coups au plus "
            + "avec tous les Carreaux){2}",
            formatPercent(attackPercent), formatPercent(CombatContext.MAX_REFLECT_ATTACK_PERCENT),
            (defenseBoost > 0 ? Lang.f(". Symboles de défense : bouclier +{0} et plus fréquents", defenseBoost) : ""))
            + (vaultBonus > 0 ? Lang.f(". Bouclier restant en fin de tour : Coffre x{0} de plus", formatPercent(vaultBonus / 100f)) : "");
    }

    @Override
    public List<EffectPopup> getPopups() {
        EffectPopup reflect = EffectPopup.scaled(Lang.f("RENVOI {0}% DES COUPS", formatPercent(attackPercent)),
            EffectPopup.Style.REFLECT, Math.round(attackPercent * 5), PopupScale.CARD_REFLECT_PERCENT);
        List<EffectPopup> popups = new java.util.ArrayList<>(List.of(reflect));
        if (defenseBoost > 0) popups.add(EffectPopup.scaled(Lang.f("BOOST DÉFENSE +{0}", defenseBoost), EffectPopup.Style.DEFENSE,
            defenseBoost, PopupScale.CARD_DEFENSE_BOOST));
        if (vaultBonus > 0) popups.add(new EffectPopup(Lang.f("COFFRE x{0} DE PLUS", formatPercent(vaultBonus / 100f)),
            EffectPopup.Style.DEFENSE, PopupScale.SECONDARY_INTENSITY));
        return popups;
    }

    /** @return {@code percent} sans décimale inutile, avec la virgule en français (ex : "3,75", "1"). */
    private static String formatPercent(float percent) {
        String text = percent == (int) percent ? String.valueOf((int) percent)
            : new BigDecimal(Float.toString(percent)).stripTrailingZeros().toPlainString();
        return !Lang.isEnglish() ? text.replace('.', ',') : text;
    }

    @Override
    public EffectSound getSound() { return EffectSound.DIAMOND_REFLECT; }
}
