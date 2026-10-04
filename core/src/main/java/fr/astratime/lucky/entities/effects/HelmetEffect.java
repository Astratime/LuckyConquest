package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Casque (Mines d'Or) : le prochain coup reçu est bloqué entièrement, quel qu'il soit. */
public class HelmetEffect extends Effect {

    @Override
    public void onPlay(PlayContext context) {
        context.addHelmet();
        context.addPopups(getPopups());
    }

    /** Rien au tirage : le Casque est posé à la pose. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return "Bloque entièrement le prochain coup que tu reçois."; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("CASQUE !", EffectPopup.Style.DEFENSE, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.INSURANCE; }
}
