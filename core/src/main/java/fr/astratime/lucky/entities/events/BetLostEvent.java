package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Pari perdu : le symbole parié n'est pas sorti, le joueur perd la moitié de ses gains. */
public class BetLostEvent extends GainsLostEvent {
    /** Symbole parié. */
    public final Symbol symbol;

    /**
     * @param symbol symbole parié
     * @param amount gains perdus
     */
    public BetLostEvent(Symbol symbol, int amount) {
        super(amount);
        this.symbol = symbol;
    }

    @Override
    public String describe() { return "Pari perdu sur " + symbol + " : -" + amount + " gains"; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup(Lang.t("PARI PERDU : GAINS ÷2"), EffectPopup.Style.DAMAGE, PopupScale.MAX_INTENSITY),
            EffectPopup.scaled(Lang.f("GAINS -{0}", amount), EffectPopup.Style.DAMAGE, amount, PopupScale.SPIN_GAINS));
    }
}
