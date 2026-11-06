package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Main du croupier (échoppe) : jusqu'à la fin du tour, chaque carte jouée après
 * elle fait piocher une carte (voir GameController#playCard). Doublée (Double ou
 * rien), elle en fait piocher deux.
 */
public class DealerHandEffect extends Effect {

    @Override
    public void onPlay(PlayContext context) {
        context.requestDrawPerPlay();
        context.addPopups(getPopups());
    }

    /** Aucun effet au tirage : la pioche se fait quand les cartes suivantes sont jouées. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return Lang.t("Jusqu'à la fin du tour, chaque carte jouée fait piocher 1 carte"); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup(Lang.t("MAIN DU CROUPIER !"), EffectPopup.Style.DRAW, PopupScale.MAX_INTENSITY),
            new EffectPopup(Lang.t("+1 CARTE PAR CARTE JOUÉE"), EffectPopup.Style.DRAW, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.EXTRA_DRAW; }
}
