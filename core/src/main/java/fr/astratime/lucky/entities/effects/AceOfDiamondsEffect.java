package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
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

    /** Puissance de la carte : 1, ou {@link fr.astratime.lucky.entities.Card#UPGRADE_FACTOR} pour sa version « + ». */
    private final float power;

    public AceOfDiamondsEffect() { this(1f); }

    /** @param power puissance : ses valeurs sont multipliées par autant (version « + » de la carte) */
    public AceOfDiamondsEffect(float power) { this.power = power; }

    private int scaled(int value) { return Math.round(value * power); }

    @Override
    public void apply(TurnContext context) {
        CombatContext combat = context.getCombatContext();
        combat.addGuaranteedReflect(scaled(REFLECT_PERCENT), scaled(REFLECT_PERCENT_LOW_HP));
        combat.addCounterAttack(scaled(combat.getPlayer().getHpRatio() < CombatContext.LOW_HP_RATIO ? COUNTER_LOW_HP : COUNTER));
    }

    @Override
    public String getDescription() {
        int lowHp = Math.round(CombatContext.LOW_HP_RATIO * 100);
        return Lang.f("Contre-attaque : vide le Coffre sur l'ennemi x{0} (x{1} sous {2}% de vie), en ignorant sa "
            + "défense. Renvoie {3}% des attaques ennemies ({4}% sous {5}% de vie)",
            scaled(COUNTER), scaled(COUNTER_LOW_HP), lowHp, scaled(REFLECT_PERCENT), scaled(REFLECT_PERCENT_LOW_HP), lowHp);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup(Lang.t("CONTRE-ATTAQUE"), EffectPopup.Style.DEFENSE, PopupScale.MAX_INTENSITY),
            new EffectPopup(Lang.f("RENVOI {0}-{1}%", scaled(REFLECT_PERCENT), scaled(REFLECT_PERCENT_LOW_HP)),
                EffectPopup.Style.REFLECT, PopupScale.SECONDARY_INTENSITY)
        );
    }

    @Override
    public EffectSound getSound() { return EffectSound.ACE_OF_DIAMONDS; }
}
