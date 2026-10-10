package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Pacte de sang (donjon du Coeur, la carte rare) : coûte une part des PV max dès
 * la pose (sans passer par le bouclier, jamais sous 1 PV), puis multiplie
 * l'attaque du tirage. Les PV perdus renforcent la Frénésie de l'As de Coeur.
 */
public class BloodPactEffect extends Effect {

    private final int   hpPercent;
    private final float attackFactor;

    /**
     * @param hpPercent    PV perdus à la pose, en % des PV max
     * @param attackFactor multiplicateur de l'attaque du tirage
     */
    public BloodPactEffect(int hpPercent, float attackFactor) {
        this.hpPercent    = hpPercent;
        this.attackFactor = attackFactor;
    }

    @Override
    public void onPlay(PlayContext context) {
        context.sacrificeHpPercent(hpPercent);
        super.onPlay(context);
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().multiplyAttack(attackFactor, "Pacte de sang");
    }

    @Override
    public String getDescription() { return Lang.f("Perd {0}% des PV. Attaque x{1}", hpPercent, factorText()); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup(Lang.f("PV -{0}%", hpPercent), EffectPopup.Style.DAMAGE, PopupScale.SECONDARY_INTENSITY),
            new EffectPopup(Lang.f("ATTAQUE x{0}", factorText()), EffectPopup.Style.ATTACK, PopupScale.MAX_INTENSITY));
    }

    private String factorText() {
        return attackFactor == (int) attackFactor ? String.valueOf((int) attackFactor) : Lang.decimal(String.valueOf(attackFactor));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ACE_OF_HEARTS; }
}
