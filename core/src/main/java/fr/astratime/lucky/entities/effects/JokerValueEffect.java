package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Atelier du Joker : le symbole choisi par le joueur, que vaut le Joker à ce tirage.
 */
public class JokerValueEffect extends Effect {

    private final Symbol symbol;

    /** @param symbol ce que vaut le Joker à ce tirage */
    public JokerValueEffect(Symbol symbol) { this.symbol = symbol; }

    /** @return ce que vaut le Joker à ce tirage. */
    public Symbol getSymbol() { return symbol; }

    @Override
    public void apply(TurnContext context) { context.getSpinContext().setJokerValue(symbol); }

    @Override
    public String getDescription() { return Lang.f("Le Joker vaut {0} à ce tirage.", symbol.getDisplayName()); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("JOKER = {0}", symbol.getDisplayName()), EffectPopup.Style.SPECIAL,
            PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.GHOST_REEL; }
}
