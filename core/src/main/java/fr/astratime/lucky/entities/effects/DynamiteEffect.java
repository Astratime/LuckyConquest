package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Dynamite (le Gouffre) : une part des PV max de l'ennemi, infligée après le
 * tirage, sans que sa peau d'or ni sa défense n'en arrêtent rien.
 */
public class DynamiteEffect extends Effect {

    private final int percent;

    /** @param percent part des PV max de l'ennemi infligée, en % */
    public DynamiteEffect(int percent) { this.percent = percent; }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().addDynamite(percent); }

    @Override
    public String getDescription() { return Lang.f("Dégâts égaux à {0} % des PV max de l'ennemi. Ignore sa peau d'or et sa défense.",
        percent); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("DYNAMITE : {0} % DES PV", percent), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ATTACK; }
}
