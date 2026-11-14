package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Fata Morgana (carte du Djinn) : chaque symbole du tirage compte plusieurs fois.
 */
public class MirageEffect extends Effect {
    private final int factor;

    /** @param factor multiplicateur des symboles */
    public MirageEffect(int factor) { this.factor = factor; }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().multiplySymbolPower(factor, "Fata Morgana"); }

    @Override
    public String getDescription() { return Lang.f("Ce tour, le mirage multiplie chaque symbole du tirage : x{0}.", factor); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("FATA MORGANA : SYMBOLES x{0}", factor), EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.BINGO; }
}
