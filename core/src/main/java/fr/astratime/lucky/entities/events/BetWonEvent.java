package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Pari gagné : le symbole parié est sorti, les gains du joueur sont multipliés. */
public class BetWonEvent extends GainsEarnedEvent {
    /** Symbole parié. */
    public final Symbol symbol;
    /** Multiplicateur des gains (2, 3 ou 4 selon le nombre d'apparitions du symbole). */
    public final int    multiplier;

    /**
     * @param symbol     symbole parié
     * @param multiplier multiplicateur appliqué aux gains du joueur
     * @param amount     gains crédités par le pari
     */
    public BetWonEvent(Symbol symbol, int multiplier, int amount) {
        super(amount);
        this.symbol     = symbol;
        this.multiplier = multiplier;
    }

    @Override
    public String describe() { return "Pari gagne sur " + symbol + " : gains x" + multiplier + " (+" + amount + ")"; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup("PARI GAGNÉ ! GAINS x" + multiplier, EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY),
            EffectPopup.scaled("GAINS +" + amount, EffectPopup.Style.GAINS, amount, PopupScale.SPIN_GAINS));
    }
}
