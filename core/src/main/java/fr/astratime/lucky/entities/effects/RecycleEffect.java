package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Recyclage de rouleau : retire un symbole tiré au hasard (jamais le Joker)
 * des rouleaux pour les prochains tirages, celui de ce tour compris. Les
 * autres symboles sortent donc plus souvent.
 */
public class RecycleEffect extends Effect {

    private static final Random RANDOM = new Random();

    private final int turns;

    /** @param turns nombre de tirages pendant lesquels le symbole est retiré. */
    public RecycleEffect(int turns) { this.turns = turns; }

    /** Choisit le symbole et le retire tout de suite (il apparaît dans le panneau latéral). */
    @Override
    public void onPlay(PlayContext context) {
        LastingEffects lasting = context.getLastingEffects();
        List<Symbol> candidates = new ArrayList<>();
        for (Symbol symbol : Symbol.values()) {
            if (symbol != Symbol.JOKER && !lasting.getRemovedSymbols().containsKey(symbol)) candidates.add(symbol);
        }
        if (candidates.isEmpty()) {
            context.addPopups(getPopups());
            return;
        }
        Symbol removed = candidates.get(RANDOM.nextInt(candidates.size()));
        lasting.removeSymbol(removed, turns);
        context.addPopups(List.of(
            new EffectPopup("RECYCLAGE : " + removed.getDisplayName(), EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY),
            new EffectPopup("RETIRÉ " + turns + " TOURS", EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY)));
    }

    /** Aucun effet propre au spin : le symbole retiré est appliqué par PreparationResolver. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return "Retire un symbole des rouleaux pendant " + turns + " tours"; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("RECYCLAGE", EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }
}
