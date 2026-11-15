package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Mèche de diamant (carte du Grand foreur) : les coups du tirage percent mieux la défense et frappent plus fort.
 */
public class DrillEffect extends Effect {
    private final int piercePercent;
    private final int attackPercent;

    /** @param piercePercent défense percée en plus, en points ; @param attackPercent attaque en plus, en % */
    public DrillEffect(int piercePercent, int attackPercent) { this.piercePercent = piercePercent; this.attackPercent = attackPercent; }

    @Override
    public void apply(TurnContext context) {
        var combat = context.getCombatContext();
        combat.pierceDefense(Math.min(100, combat.getPiercePercent() + piercePercent));
        combat.multiplyAttack(1f + attackPercent / 100f, "Mèche de diamant");
    }

    @Override
    public String getDescription() { return Lang.f("Ce tour, tes coups percent {0} % de défense en plus. Attaque +{1} %.", piercePercent, attackPercent); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("MÈCHE DE DIAMANT : PERCE +{0} %", piercePercent), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.SPADE_IGNORE_DEFENSE; }
}
