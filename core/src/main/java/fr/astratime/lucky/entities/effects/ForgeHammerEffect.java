package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Marteau de forge (la Forge) : l'attaque du tirage augmente, plus encore
 * si un BAR ou un double BAR sort (voir {@link fr.astratime.lucky.controllers.CombatResolver}).
 */
public class ForgeHammerEffect extends Effect {

    /** Attaque ajoutée au tirage, en %. */
    public static final int ATTACK_PERCENT = 25;
    /** Attaque ajoutée au tirage si un BAR ou un double BAR sort, en % (à la place de {@link #ATTACK_PERCENT}). */
    public static final int BAR_PERCENT = 50;

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().multiplyAttack(1f + ATTACK_PERCENT / 100f, "Marteau de forge");
        context.getCombatContext().addHammer();
    }

    @Override
    public String getDescription() { return Lang.f("Attaque +{0} %. Si tu tires un BAR ou un double BAR, attaque +{1} % à la place.",
        ATTACK_PERCENT, BAR_PERCENT); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("MARTEAU : ATTAQUE +{0} %", ATTACK_PERCENT), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ATTACK; }
}
