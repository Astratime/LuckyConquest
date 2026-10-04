package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Impose un symbole au rouleau du milieu : celui choisi au Rouleau truqué, ou
 * un Joker (Rouleau fantôme).
 */
public class ForceReelEffect extends Effect {

    /** Rouleau imposé : celui du milieu. */
    public static final int MIDDLE_REEL = 1;

    private final Symbol symbol;

    /** @param symbol symbole imposé au rouleau du milieu */
    public ForceReelEffect(Symbol symbol) { this.symbol = symbol; }

    @Override
    public void apply(TurnContext context) {
        context.getSpinContext().forceReel(MIDDLE_REEL, symbol);
    }

    /** @return le symbole imposé au rouleau du milieu. */
    public Symbol getSymbol() { return symbol; }

    @Override
    public String getDescription() {
        return symbol == Symbol.JOKER
            ? "Le rouleau du milieu devient un Joker."
            : "Le rouleau du milieu affiche " + symbol.getDisplayName() + ".";
    }

    @Override
    public List<EffectPopup> getPopups() {
        String text = symbol == Symbol.JOKER ? "ROULEAU FANTÔME : JOKER" : "MILIEU : " + symbol.getDisplayName();
        return List.of(new EffectPopup(text, EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return symbol == Symbol.JOKER ? EffectSound.GHOST_REEL : EffectSound.RIGGED_REEL; }
}
