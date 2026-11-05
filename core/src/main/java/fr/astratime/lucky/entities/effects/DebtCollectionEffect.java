package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;
import java.math.BigDecimal;

/**
 * Recouvrement (archétype Rentier) : toutes les Créances de l'ennemi sont encaissées d'un coup
 * (voir {@link fr.astratime.lucky.controllers.CombatResolver}).
 */
public class DebtCollectionEffect extends Effect {

    private final float damagePercent;
    private final int   spins;

    /**
     * @param damagePercent dégâts par Créance, en % du prix du tirage
     * @param spins     gains par Créance, en coûts de tirage
     */
    public DebtCollectionEffect(float damagePercent, int spins) {
        this.damagePercent = damagePercent;
        this.spins     = spins;
    }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().addDebtCollection(damagePercent, spins); }

    @Override
    public String getDescription() {
        return Lang.f("Encaisse toutes les Créances : par Créance, {0} % du prix du tirage en dégâts (sans tenir compte de sa défense) "
            + "et {1} tirages de gains. Elles retombent à 0.", formatPercent(damagePercent), spins);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("RECOUVREMENT !"), EffectPopup.Style.DRAIN, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ALL_IN; }

    /** @return {@code percent} sans décimale inutile, avec la virgule en français (ex : "1,5", "1"). */
    private static String formatPercent(float percent) {
        String text = percent == (int) percent ? String.valueOf((int) percent)
            : new BigDecimal(Float.toString(percent)).stripTrailingZeros().toPlainString();
        return !Lang.isEnglish() ? text.replace('.', ',') : text;
    }
}
