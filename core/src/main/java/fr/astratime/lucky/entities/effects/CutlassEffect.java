package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Sabre d'abordage (le Galion) : l'attaque du tirage augmente et ignore la
 * défense ennemie.
 */
public class CutlassEffect extends Effect {

    private final int attackPercent;

    /** @param attackPercent attaque ajoutée au tirage, en % */
    public CutlassEffect(int attackPercent) { this.attackPercent = attackPercent; }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().multiplyAttack(1f + attackPercent / 100f, "Sabre d'abordage");
        context.getCombatContext().setIgnoreDefense(true);
    }

    @Override
    public String getDescription() { return Lang.f("Attaque +{0} %. Tes coups passent la défense ennemie.",
        attackPercent); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("ATTAQUE +{0} %", attackPercent), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY),
            new EffectPopup(Lang.t("DÉFENSE ENNEMIE IGNORÉE"), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.SPADE_IGNORE_DEFENSE; }
}
