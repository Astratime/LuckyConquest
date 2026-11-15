package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Grappin (carte du Capitaine Noir) : la meilleure carte que l'ennemi choisit à son prochain tour lui est volée, et l'attaque du tirage monte.
 */
public class GrappleEffect extends Effect {
    private final int attackPercent;

    /** @param attackPercent attaque du tirage en plus, en % */
    public GrappleEffect(int attackPercent) { this.attackPercent = attackPercent; }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().multiplyAttack(1f + attackPercent / 100f, "Grappin");
        context.getCombatContext().getEnemy().getMaluses().grapple();
    }

    @Override
    public String getDescription() { return Lang.f("Attaque +{0} %. À son prochain tour, la meilleure carte que joue l'ennemi lui est volée.", attackPercent); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("GRAPPIN : ATTAQUE +{0} %", attackPercent), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ATTACK; }
}
