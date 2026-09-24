package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Pari placé sur un symbole (voir {@link BetEffect}), résolu par CombatResolver après le tirage. */
public class BetOnSymbolEffect extends Effect {

    private final Symbol symbol;

    /** @param symbol symbole parié. */
    public BetOnSymbolEffect(Symbol symbol) { this.symbol = symbol; }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addBet(symbol);
    }

    @Override
    public String getDescription() { return "Pari sur " + symbol.getDisplayName(); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("PARI SUR " + symbol.getDisplayName(), EffectPopup.Style.GAINS,
            PopupScale.MAX_INTENSITY));
    }
}
