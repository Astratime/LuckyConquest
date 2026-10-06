package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Scorbut (règle du Port des Contrebandiers) : jouée, la carte ne fait rien,
 * sinon disparaître. C'est en main qu'elle nuit : le tirage y est divisé par
 * deux (voir {@link fr.astratime.lucky.controllers.PreparationResolver}).
 */
public class ScurvyEffect extends Effect {

    /** Rien au tirage : jouer la carte suffit à s'en débarrasser. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() {
        return Lang.t("En main, ton tirage donne moitié moins d'attaque, de défense et de gains. Joue-la pour t'en "
            + "débarrasser. Elle compte dans tes cartes du tour");
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("SCORBUT SOIGNÉ"), EffectPopup.Style.DRAIN, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public boolean canBeDoubled() { return false; }

    @Override
    public EffectSound getSound() { return EffectSound.HEART_DRAIN; }
}
