package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Lampe à carbure (le Gouffre) : le prochain coup de grisou ne touche pas le joueur.
 */
public class CarbideLampEffect extends Effect {

    @Override
    public void onPlay(PlayContext context) {
        context.getLastingEffects().addLamp();
        context.addPopups(getPopups());
    }

    /** Rien de plus au tirage. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return "Annule le prochain coup de grisou."; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("LAMPE : GRISOU ÉVITÉ", EffectPopup.Style.DEFENSE, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.INSURANCE; }
}
