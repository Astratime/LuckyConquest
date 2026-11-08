package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Roi des ombres (archétype Revenant) : il rejoue les effets de la dernière
 * carte jouée au tour d'avant (sauf un autre Roi des ombres). Le joueur choisit
 * donc ce qu'il rejouera en gardant sa meilleure carte pour la fin du tour.
 */
public class ShadowKingEffect extends Effect {

    @Override
    public void onPlay(PlayContext context) {
        Card echo = echo(context.getPlayer().getLastingEffects().getCourtAndClock().getLastPlayed());
        if (echo == null) {
            context.addPopups(List.of(new EffectPopup(Lang.t("ROI DES OMBRES : RIEN À REJOUER"), EffectPopup.Style.SPECIAL,
                PopupScale.SECONDARY_INTENSITY)));
            return;
        }
        context.addPopups(List.of(new EffectPopup(Lang.f("ROI DES OMBRES : {0}", echo.getName().toUpperCase()),
            EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY)));
        for (Effect effect : echo.getEffects()) effect.onPlay(context);
    }

    /** @return la dernière carte de {@code played} qui n'est pas un Roi des ombres, ou {@code null}. */
    static Card echo(List<Card> played) {
        for (int i = played.size() - 1; i >= 0; i--) {
            Card card = played.get(i);
            if (card.getEffects().stream().noneMatch(effect -> effect instanceof ShadowKingEffect)) return card;
        }
        return null;
    }

    /** Aucun effet propre au moment du tirage : ceux de la carte rejouée sont déjà en attente. */
    @Override
    public void apply(TurnContext context) { }

    /** Double ou rien ne fait pas rejouer deux fois la même carte. */
    @Override
    public boolean canBeDoubled() { return false; }

    @Override
    public String getDescription() {
        return Lang.t("Rejoue les effets de la dernière carte que tu as jouée au tour d'avant.");
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("ROI DES OMBRES"), EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.GHOST_REEL; }
}
