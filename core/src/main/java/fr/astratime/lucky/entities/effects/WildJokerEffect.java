package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Joker sauvage (archétype Joker) : chaque Joker sorti ce tour ajoute de l'attaque.
 */
public class WildJokerEffect extends Effect {
    private final int percent;

    /**
     * Joker sauvage.
     * @param percent attaque en plus par Joker sorti, en %
     */
    public WildJokerEffect(int percent) {
        this.percent = percent;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addWildJoker(percent);
    }

    @Override
    public String getDescription() { return Lang.f("Chaque Joker sorti ce tour : attaque +{0} %.", percent); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("JOKER SAUVAGE : +{0} % PAR JOKER", percent), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.GHOST_REEL; }

}
