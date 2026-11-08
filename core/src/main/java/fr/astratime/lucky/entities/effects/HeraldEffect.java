package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

/**
 * Héraut (archétype Cour royale) : il fait venir de la pioche dans la main une
 * figure (Valet, Dame ou Roi, de n'importe quelle suite ou archétype) et un As,
 * pour aider à former Paire, Brelan ou Suite.
 */
public class HeraldEffect extends Effect {

    @Override
    public void onPlay(PlayContext context) {
        List<Card> deck = context.getPlayer().getDeck().getCards();
        int called = 0;
        called += call(context, deck, card -> card.getRank() >= 11 && card.getRank() <= 13);
        called += call(context, deck, card -> card.getRank() == 1);
        context.addPopups(getPopups());
        if (called == 0) {
            context.addPopups(List.of(new EffectPopup(Lang.t("AUCUNE FIGURE DANS LA PIOCHE"), EffectPopup.Style.DRAW,
                PopupScale.SECONDARY_INTENSITY)));
        }
    }

    /** Retire de la pioche une carte à rang qui respecte {@code rank}, tirée au hasard, pour la main. @return 1 si trouvée */
    private static int call(PlayContext context, List<Card> deck, Predicate<Card> rank) {
        List<Card> matching = deck.stream().filter(Card::hasRank).filter(rank).toList();
        if (matching.isEmpty()) return 0;
        Card card = matching.get(ThreadLocalRandom.current().nextInt(matching.size()));
        deck.remove(card);
        context.fetchCard(card);
        return 1;
    }

    /** Aucun effet au moment du tirage : les cartes sont venues quand le Héraut a été joué. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() {
        return Lang.t("Fais venir de ta pioche dans ta main une figure (Valet, Dame ou Roi) et un As, s'il y en a.");
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("HÉRAUT : LA COUR ARRIVE"), EffectPopup.Style.DRAW, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.EXTRA_DRAW; }
}
