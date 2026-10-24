package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Intimidation (archétype Bluffeur) : au prochain tour de l'ennemi, ses attaques baissent
 * (moitié moins sur un boss ou un roi).
 */
public class IntimidationEffect extends Effect {
    private final int percent;

    /**
     * Intimidation.
     * @param percent baisse des attaques de l'ennemi, en %
     */
    public IntimidationEffect(int percent) {
        this.percent = percent;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().getEnemy().intimidate(percent);
    }

    @Override
    public String getDescription() { return Lang.f("Au prochain tour de l'ennemi, ses attaques baissent de {0} % (moitié moins sur un boss ou un roi).", percent); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("INTIMIDATION : ATTAQUE -{0} %", percent), EffectPopup.Style.DEFENSE, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.INSURANCE; }

    @Override
    public boolean canBeDoubled() { return false; }

}
