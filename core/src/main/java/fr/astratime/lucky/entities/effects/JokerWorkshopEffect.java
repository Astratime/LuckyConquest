package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.choices.JokerWorkshopChoice;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Atelier du Joker (archétype Joker) : le joueur choisit ce que vaut le Joker à ce tirage. Le choix
 * est demandé à la pose ; la valeur est imposée par {@link JokerValueEffect}.
 */
public class JokerWorkshopEffect extends Effect {

    @Override
    public void onPlay(PlayContext context) {
        context.requestChoice(new JokerWorkshopChoice());
        context.addPopups(getPopups());
    }

    /** Aucun effet propre au spin : le symbole choisi est mis en attente par GameController. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return Lang.t("Choisis ce que vaut le Joker à ce tirage."); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("ATELIER DU JOKER"), EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.RIGGED_REEL; }
}
