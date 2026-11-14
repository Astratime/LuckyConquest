package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Coup d'enclume (carte du Maître de forge) : des coups d'épée en plus, chacun à une part de la plus grosse attaque du tirage (comme la Lame forgée).
 */
public class AnvilEffect extends Effect {
    private final int strikes;

    /** @param strikes coups en plus */
    public AnvilEffect(int strikes) { this.strikes = strikes; }

    @Override
    public void apply(TurnContext context) { for (int i = 0; i < strikes; i++) context.getCombatContext().addForgedBlade(); }

    @Override
    public String getDescription() { return Lang.f("{0} coups d'épée en plus. Chacun frappe à {1} % de ta plus grosse attaque du tour.", strikes, ForgedBladeEffect.PERCENT); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("COUP D'ENCLUME"), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ATTACK; }
}
