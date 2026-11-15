package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Ronces (carte du Roi de Carreau) : une part des attaques de l'ennemi à son prochain tour lui est renvoyée.
 */
public class ThornsEffect extends Effect {
    private final int percent;

    /** @param percent part de ses attaques renvoyée, en % */
    public ThornsEffect(int percent) { this.percent = percent; }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().addGuaranteedReflect(percent, percent); }

    @Override
    public String getDescription() { return Lang.f("À son prochain tour, l'ennemi se pique : {0} % de ses attaques lui sont renvoyées.", percent); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("RONCES : RENVOI {0} %", percent), EffectPopup.Style.REFLECT, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.DIAMOND_REFLECT; }
}
