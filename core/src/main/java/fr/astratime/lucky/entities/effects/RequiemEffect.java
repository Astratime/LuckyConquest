package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Requiem (archétype Revenant) : la défausse est remélangée dans la pioche, et
 * chaque paquet de cartes remélangées donne une Lame (Pique), avec un plafond.
 */
public class RequiemEffect extends Effect {
    private final int cardsPerBlade;
    private final int maxBlades;

    /**
     * Requiem.
     * @param cardsPerBlade cartes remélangées pour une Lame
     * @param maxBlades     Lames données au plus
     */
    public RequiemEffect(int cardsPerBlade, int maxBlades) {
        this.cardsPerBlade = cardsPerBlade;
        this.maxBlades     = maxBlades;
    }

    @Override
    public void onPlay(PlayContext context) {
        Player player = context.getPlayer();
        List<Card> back = player.getDiscardPile().drainShuffled();
        for (Card card : back) player.getDeck().insertRandomly(card);
        int blades = Math.min(maxBlades, back.size() / cardsPerBlade);
        if (blades > 0) player.getLastingEffects().addBlades(blades);
        context.addPopups(List.of(
            new EffectPopup(Lang.n(back.size(), "REQUIEM : {0} CARTE REMÉLANGÉE", "REQUIEM : {0} CARTES REMÉLANGÉES", back.size()),
                EffectPopup.Style.DRAW, PopupScale.SECONDARY_INTENSITY),
            EffectPopup.scaled(Lang.f("LAMES +{0}", blades), EffectPopup.Style.ATTACK, blades, maxBlades)));
    }

    /** Aucun effet au moment du tirage : les Lames sont déjà posées. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() {
        return Lang.f("Remélange ta défausse dans ta pioche : +1 Lame par {0} cartes remélangées, {1} au plus.", cardsPerBlade, maxBlades);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("REQUIEM"), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.RECYCLE; }
}
