package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Tamis (le Filon) : la Pépite de l'ennemi n'a pas d'effet ce tour, et les
 * gains du tirage sont multipliés.
 */
public class SieveEffect extends Effect {

    private final int gainsPercent;

    /** @param gainsPercent gains ajoutés au tirage, en % */
    public SieveEffect(int gainsPercent) { this.gainsPercent = gainsPercent; }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().restoreGains();
        context.getCombatContext().multiplyGains(1f + gainsPercent / 100f);
    }

    private String factorText() {
        float factor = 1f + gainsPercent / 100f;
        return factor == Math.round(factor) ? String.valueOf(Math.round(factor)) : Lang.decimal(String.valueOf(factor));
    }

    @Override
    public String getDescription() { return Lang.f("Les pierres redeviennent de l'or : la Pépite ennemie n'a pas d'effet ce tour. Gains x{0}.",
        factorText()); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("TAMIS : GAINS x{0}", factorText()), EffectPopup.Style.GAINS, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.GAIN; }
}
