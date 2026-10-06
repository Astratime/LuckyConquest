package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Dans la manche : pendant quelques tours (celui où elle est jouée compris),
 * le joueur peut jouer plus de cartes par tour.
 */
public class ExtraPlaysEffect extends Effect {

    private final int plays;
    private final int turns;

    /**
     * @param plays cartes jouables par tour pendant l'effet
     * @param turns tours d'effet, celui où la carte est jouée compris
     */
    public ExtraPlaysEffect(int plays, int turns) {
        this.plays = plays;
        this.turns = turns;
    }

    /** L'effet vaut dès ce tour : il est enregistré tout de suite. */
    @Override
    public void onPlay(PlayContext context) {
        context.getLastingEffects().addExtraPlays(plays, turns);
        context.addPopups(getPopups());
    }

    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() {
        return Lang.f("{0} cartes jouables par tour pendant {1} tours (celui-ci compris)", plays, turns);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup(Lang.t("DANS LA MANCHE !"), EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY),
            new EffectPopup(Lang.f("{0} CARTES PAR TOUR ({1} TOURS)", plays, turns), EffectPopup.Style.DRAW,
                PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.EXTRA_PLAYS; }
}
