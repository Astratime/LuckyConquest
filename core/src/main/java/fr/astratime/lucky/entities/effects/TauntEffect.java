package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Provocation (carte du Roi de Pique) : l'attaque du tirage monte, mais l'ennemi provoqué frappe plus fort à son prochain tour.
 */
public class TauntEffect extends Effect {
    private final int attackPercent;
    private final int enemyPercent;

    /** @param attackPercent attaque du tirage en plus, en % ; @param enemyPercent force en plus de ses attaques à son prochain tour, en % */
    public TauntEffect(int attackPercent, int enemyPercent) { this.attackPercent = attackPercent; this.enemyPercent = enemyPercent; }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().multiplyAttack(1f + attackPercent / 100f, "Provocation");
        context.getCombatContext().getEnemy().getMaluses().shiftNextAttack(enemyPercent);
    }

    @Override
    public String getDescription() { return Lang.f("Attaque +{0} %. Provoqué, l'ennemi frappe +{1} % à son prochain tour.", attackPercent, enemyPercent); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("PROVOCATION : ATTAQUE +{0} %", attackPercent), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ATTACK; }
}
