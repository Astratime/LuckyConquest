package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Frénésie (carte du Requin banquier) : l'attaque du tirage monte contre un ennemi sous la moitié de ses PV.
 */
public class FrenzyEffect extends Effect {
    private final int attackPercent;

    /** @param attackPercent attaque en plus contre un ennemi blessé, en % */
    public FrenzyEffect(int attackPercent) { this.attackPercent = attackPercent; }

    @Override
    public void apply(TurnContext context) {
        if (context.getCombatContext().getEnemy().getHpRatio() < 0.5f) {
            context.getCombatContext().multiplyAttack(1f + attackPercent / 100f, "Frénésie");
        }
    }

    @Override
    public String getDescription() { return Lang.f("Si l'ennemi a moins de la moitié de ses PV : attaque +{0} %.", attackPercent); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("FRÉNÉSIE"), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ATTACK; }
}
