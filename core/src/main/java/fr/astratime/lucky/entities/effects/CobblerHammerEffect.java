package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Marteau du cordonnier (archétype Lutin) : plus d'attaque, et si l'ennemi tombe ce tour, les
 * gains du combat grossissent.
 */
public class CobblerHammerEffect extends Effect {
    private final int attackPercent;
    private final int gainsPercent;

    /**
     * Marteau du cordonnier.
     * @param attackPercent attaque en plus, en %
     * @param gainsPercent gains du combat en plus si l'ennemi tombe ce tour, en %
     */
    public CobblerHammerEffect(int attackPercent, int gainsPercent) {
        this.attackPercent = attackPercent;
        this.gainsPercent = gainsPercent;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().multiplyAttack(1f + attackPercent / 100f, "Marteau du cordonnier");
        context.getCombatContext().addCobbler(gainsPercent);
    }

    @Override
    public String getDescription() { return Lang.f("Attaque +{0} %. Si l'ennemi tombe ce tour, gains du combat +{1} %.", attackPercent, gainsPercent); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("ATTAQUE +{0} %", attackPercent), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ATTACK; }

}
