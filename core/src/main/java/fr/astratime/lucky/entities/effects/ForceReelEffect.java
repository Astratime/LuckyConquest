package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
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

    /** Rouleau de gauche (Perle noire). */
    public static final int LEFT_REEL = 0;

    private final int    reel;
    private final Symbol symbol;

    /** @param symbol symbole imposé au rouleau du milieu */
    public ForceReelEffect(Symbol symbol) { this(MIDDLE_REEL, symbol); }

    /** @param reel rouleau imposé (0 : celui de gauche) */
    public ForceReelEffect(int reel, Symbol symbol) {
        this.reel   = reel;
        this.symbol = symbol;
    }

    @Override
    public void apply(TurnContext context) {
        context.getSpinContext().forceReel(reel, symbol);
    }

    /** @return le symbole imposé au rouleau. */
    public Symbol getSymbol() { return symbol; }

    /** @return le rouleau imposé (1 : celui du milieu). */
    public int getReel() { return reel; }

    @Override
    public String getDescription() {
        String where = reel == MIDDLE_REEL ? Lang.t("Le rouleau du milieu") : Lang.t("Le rouleau de gauche");
        return symbol == Symbol.JOKER
            ? Lang.f("{0} devient un Joker.", where)
            : Lang.f("{0} affiche {1}.", where, symbol.getDisplayName());
    }

    @Override
    public List<EffectPopup> getPopups() {
        String text = reel != MIDDLE_REEL ? (symbol == Symbol.JOKER ? Lang.t("PERLE NOIRE : JOKER") : Lang.f("GAUCHE : {0}",
            symbol.getDisplayName()))
            : symbol == Symbol.JOKER ? Lang.t("ROULEAU FANTÔME : JOKER") : Lang.f("MILIEU : {0}", symbol.getDisplayName());
        return List.of(new EffectPopup(text, EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return symbol == Symbol.JOKER ? EffectSound.GHOST_REEL : EffectSound.RIGGED_REEL; }
}
