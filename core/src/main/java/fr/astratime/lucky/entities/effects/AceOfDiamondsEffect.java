package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * As de Carreau : renvoi garanti des dégâts ennemis, sans avoir besoin de tirer
 * un symbole de défense. Renforcé si le joueur est sous {@link CombatContext#LOW_HP_RATIO}
 * de sa vie au moment de la riposte. S'additionne au renvoi des autres cartes Carreau.
 */
public class AceOfDiamondsEffect extends Effect {

    /** Renvoi garanti, en pourcentage de l'attaque ennemie. */
    private static final int REFLECT_PERCENT        = 500;
    /** Renvoi garanti si le joueur est sous LOW_HP_RATIO de sa vie. */
    private static final int REFLECT_PERCENT_LOW_HP = 1000;

    /** Multiplicateur du Coffre infligé en contre-attaque ({@link #COUNTER_LOW_HP} si vie basse). */
    static final int COUNTER        = 3;
    static final int COUNTER_LOW_HP = 5;

    @Override
    public void apply(TurnContext context) {
        CombatContext combat = context.getCombatContext();
        combat.addGuaranteedReflect(REFLECT_PERCENT, REFLECT_PERCENT_LOW_HP);
        combat.addCounterAttack(combat.getPlayer().getHpRatio() < CombatContext.LOW_HP_RATIO ? COUNTER_LOW_HP : COUNTER);
    }

    @Override
    public String getDescription() {
        int lowHp = Math.round(CombatContext.LOW_HP_RATIO * 100);
        return "Contre-attaque : inflige le Coffre x" + COUNTER + " (x" + COUNTER_LOW_HP + " si vie < " + lowHp
            + "%), puis le vide\nRenvoi garanti de " + REFLECT_PERCENT + "% (" + REFLECT_PERCENT_LOW_HP
            + "% si vie < " + lowHp + "%)";
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup("CONTRE-ATTAQUE", EffectPopup.Style.DEFENSE, PopupScale.MAX_INTENSITY),
            new EffectPopup("RENVOI " + REFLECT_PERCENT + "-" + REFLECT_PERCENT_LOW_HP + "%",
                EffectPopup.Style.REFLECT, PopupScale.SECONDARY_INTENSITY)
        );
    }
}
