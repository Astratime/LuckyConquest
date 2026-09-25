package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Crédite immédiatement des gains au joueur, dès que la carte est jouée (sans multiplicateur). */
public class GainEffect extends Effect {

    private final int amount;

    /** @param amount gains crédités immédiatement. */
    public GainEffect(int amount) { this.amount = amount; }

    @Override
    public void onPlay(PlayContext context) {
        context.addGains(amount);
        context.addPopups(getPopups());
    }

    /** Aucun effet au moment du spin : les gains ont déjà été crédités quand la carte a été jouée. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return "+" + amount + " gains immédiatement"; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(EffectPopup.scaled("GAINS +" + amount, EffectPopup.Style.GAINS, amount, PopupScale.CARD_GAINS));
    }
}
