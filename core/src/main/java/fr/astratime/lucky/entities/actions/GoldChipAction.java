package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolRegistry;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.i18n.Lang;

/** Jeton d'or : des gains, doublés si un autre symbole de gains sort dans le même tirage. */
public final class GoldChipAction extends GainAction {

    /** Multiplicateur du Jeton d'or quand il n'est pas seul à rapporter des gains. */
    public static final int FACTOR = 2;

    public GoldChipAction(int baseGain) { super(baseGain); }

    @Override
    protected double gainFactor(CombatContext context) {
        int gainSymbols = 0;
        for (Symbol symbol : context.getDrawnSymbols()) if (SymbolRegistry.isGainSymbol(symbol)) gainSymbols++;
        return gainSymbols >= 2 ? FACTOR : 1.0; // lui, et au moins un autre
    }

    @Override
    public String getDescription() {
        return Lang.f("{0} gains de base, x{1} si un autre symbole de gains sort avec lui", getBaseGain(), FACTOR);
    }
}
