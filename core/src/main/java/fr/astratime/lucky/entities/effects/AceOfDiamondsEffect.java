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

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addGuaranteedReflect(REFLECT_PERCENT, REFLECT_PERCENT_LOW_HP);
    }

    @Override
    public String getDescription() {
        return "Renvoi garanti de " + REFLECT_PERCENT + "% des degats ennemis ("
            + REFLECT_PERCENT_LOW_HP + "% si vie < " + Math.round(CombatContext.LOW_HP_RATIO * 100) + "%)";
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup("RENVOI " + REFLECT_PERCENT + "-" + REFLECT_PERCENT_LOW_HP + "%",
                EffectPopup.Style.REFLECT, PopupScale.MAX_INTENSITY)
        );
    }
}
