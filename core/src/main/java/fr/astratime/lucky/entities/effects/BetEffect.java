package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.choices.BetChoice;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Pari : le joueur choisit un symbole. S'il sort 1, 2 ou 3 fois, ses gains
 * sont multipliés par 2, 3 ou 4 ; sinon, il en perd la moitié. Le choix est
 * demandé à la pose ; le pari lui-même est {@link BetOnSymbolEffect}.
 */
public class BetEffect extends Effect {

    @Override
    public void onPlay(PlayContext context) {
        context.requestChoice(new BetChoice());
        context.addPopups(getPopups());
    }

    /** Aucun effet propre au spin : le pari choisi est mis en attente par GameController. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() {
        return "Pariez sur un symbole : gains x2, x3 ou x4 s'il sort 1, 2 ou 3 fois, sinon gains /2";
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("PARI !", EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY));
    }
}
