package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Couronne du Fou (archétype Joker) : un Bingo fait avec au moins un Joker multiplie les gains du tirage.
 */
public class FoolCrownEffect extends Effect {
    private final int factor;

    /**
     * Couronne du Fou.
     * @param factor multiplicateur des gains du tirage
     */
    public FoolCrownEffect(int factor) {
        this.factor = factor;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addFoolCrown(factor);
    }

    @Override
    public String getDescription() { return Lang.f("Un Bingo fait avec au moins un Joker : gains du tirage x{0}.", factor); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("COURONNE DU FOU : BINGO AU JOKER x{0}", factor), EffectPopup.Style.GAINS, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.BINGO; }

}
