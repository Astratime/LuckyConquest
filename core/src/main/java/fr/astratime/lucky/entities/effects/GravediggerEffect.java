package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.DiscardPile;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Fossoyeur (archétype Revenant) : la dernière carte arrivée dans la défausse
 * (celle du dessus) revient dans la main.
 */
public class GravediggerEffect extends Effect {

    @Override
    public void onPlay(PlayContext context) {
        DiscardPile discard = context.getPlayer().getDiscardPile();
        List<Card> cards = discard.getCards();
        Card top = cards.isEmpty() ? null : cards.get(cards.size() - 1);
        if (top == null || !discard.remove(top)) {
            context.addPopups(List.of(new EffectPopup(Lang.t("FOSSOYEUR : DÉFAUSSE VIDE"), EffectPopup.Style.DRAW,
                PopupScale.SECONDARY_INTENSITY)));
            return;
        }
        context.fetchCard(top);
        context.addPopups(List.of(new EffectPopup(Lang.f("FOSSOYEUR : {0}", top.getName().toUpperCase()), EffectPopup.Style.DRAW,
            PopupScale.SECONDARY_INTENSITY)));
    }

    /** Aucun effet au moment du tirage : la carte est revenue quand le Fossoyeur a été joué. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return Lang.t("Reprends dans ta main la dernière carte de ta défausse (celle du dessus)."); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("FOSSOYEUR"), EffectPopup.Style.DRAW, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.EXTRA_DRAW; }
}
