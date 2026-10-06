package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Rayon du phare (le Phare) : l'ennemi, ébloui, passe son prochain tour.
 */
public class DazzleEffect extends Effect {

    @Override
    public void apply(TurnContext context) { context.getCombatContext().getEnemy().dazzle(); }

    @Override
    public String getDescription() { return Lang.t("L'ennemi est ébloui. Il passe son prochain tour."); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("ÉBLOUI !"), EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }

    @Override
    public boolean canBeDoubled() { return false; }

    @Override
    public EffectSound getSound() { return EffectSound.INSURANCE; }
}
