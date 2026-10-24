package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Rire du Joker (archétype Joker) : si un Joker sort, le joueur pioche plus de cartes au tour suivant.
 */
public class JokerLaughEffect extends Effect {
    private final int cards;

    /**
     * Rire du Joker.
     * @param cards cartes piochées en plus au prochain tour
     */
    public JokerLaughEffect(int cards) {
        this.cards = cards;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addJokerLaugh(cards);
    }

    @Override
    public String getDescription() { return Lang.f("Si un Joker sort ce tour, tu pioches {0} cartes de plus au prochain tour.", cards); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("RIRE DU JOKER"), EffectPopup.Style.DRAW, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.EXTRA_DRAW; }

}
