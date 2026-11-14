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
 * Refrain (carte de la Sirène) : rejoue la carte jouée juste avant elle ce
 * tour, comme si elle était jouée une seconde fois (sans les effets qui ne se
 * doublent pas).
 */
public class RefrainEffect extends Effect {

    @Override
    public void onPlay(PlayContext context) {
        Card echo = echo(context.getPlayer().getPlayedCards());
        if (echo == null) {
            context.addPopups(List.of(new EffectPopup(Lang.t("REFRAIN : RIEN À REJOUER"), EffectPopup.Style.SPECIAL,
                PopupScale.SECONDARY_INTENSITY)));
            return;
        }
        context.addPopups(List.of(new EffectPopup(Lang.f("REFRAIN : {0}", echo.getName().toUpperCase()),
            EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY)));
        for (Effect effect : echo.getEffects()) {
            if (effect.canBeDoubled()) effect.onPlay(context);
        }
    }

    /** @return la dernière carte jouée ce tour avant le Refrain (pas un autre Refrain), ou {@code null}. */
    static Card echo(List<Card> played) {
        for (int i = played.size() - 1; i >= 0; i--) {
            Card card = played.get(i);
            if (card.getEffects().stream().noneMatch(effect -> effect instanceof RefrainEffect)) return card;
        }
        return null;
    }

    /** Aucun effet propre au moment du tirage : ceux de la carte rejouée sont déjà en attente. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return Lang.t("Rejoue la carte que tu as jouée juste avant celle-ci ce tour."); }

    @Override
    public List<EffectPopup> getPopups() { return List.of(); }

    /** Double ou rien ne fait pas rejouer deux fois la même carte. */
    @Override
    public boolean canBeDoubled() { return false; }

    @Override
    public EffectSound getSound() { return EffectSound.DOUBLE_OR_NOTHING; }
}
