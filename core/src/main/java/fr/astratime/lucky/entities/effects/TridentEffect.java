package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Trident (Casino Englouti) : chaque attaque du tirage frappe {@link #HITS}
 * fois, à {@link #HIT_PERCENT} % : l'attaque est multipliée d'autant.
 */
public class TridentEffect extends Effect {

    /** Coups portés par chaque attaque. */
    public static final int HITS = 3;
    /** Force de chaque coup, en % de l'attaque. */
    public static final int HIT_PERCENT = 50;

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().multiplyAttack(HITS * HIT_PERCENT / 100f, "Trident");
    }

    @Override
    public String getDescription() {
        return Lang.f("Ton attaque frappe {0} fois, à {1} %. Attaque x1,5.", HITS, HIT_PERCENT);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("TRIDENT : {0} COUPS", HITS), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ATTACK; }
}
