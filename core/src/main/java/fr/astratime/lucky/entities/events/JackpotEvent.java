package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Événement émis quand les trois symboles tirés sont identiques (jackpot). */
public class JackpotEvent extends Event {

    /** Symbole aligné sur les trois rouleaux : il choisit la célébration du Bingo. */
    public final Symbol symbol;

    /** @param symbol symbole aligné sur les trois rouleaux (Jokers déjà remplacés) */
    public JackpotEvent(Symbol symbol) { this.symbol = symbol; }

    @Override
    public String describe() { return "JACKPOT ! (" + symbol.getDisplayName() + ")"; }

    /** Toujours affiché en grand. */
    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("JACKPOT !"), EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY));
    }
}
