package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.choices.RiggedReelChoice;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Rouleau truqué : le joueur choisit le symbole du rouleau du milieu. Le choix
 * est demandé à la pose ; le symbole imposé l'est par {@link ForceReelEffect}.
 */
public class RiggedReelEffect extends Effect {

    @Override
    public void onPlay(PlayContext context) {
        context.requestChoice(new RiggedReelChoice());
        context.addPopups(getPopups());
    }

    /** Aucun effet propre au spin : le symbole choisi est mis en attente par GameController. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return "Choisis le symbole du rouleau du milieu."; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("ROULEAU TRUQUÉ !", EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.RIGGED_REEL; }
}
