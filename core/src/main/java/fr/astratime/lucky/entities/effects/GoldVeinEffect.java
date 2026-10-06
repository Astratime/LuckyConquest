package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Veine d'or (Mines d'Or) : les gains des prochains tirages, celui-ci compris,
 * sont multipliés par {@link #FACTOR} (voir
 * {@link fr.astratime.lucky.controllers.PreparationResolver}).
 */
public class GoldVeinEffect extends Effect {

    /** Multiplicateur des gains sous la Veine d'or. */
    public static final int FACTOR = 3;

    private final int turns;

    /** @param turns tirages sous la Veine d'or, celui-ci compris */
    public GoldVeinEffect(int turns) { this.turns = turns; }

    @Override
    public void onPlay(PlayContext context) {
        context.getLastingEffects().addGoldVein(turns);
        context.addPopups(getPopups());
    }

    /** Rien de plus au tirage : la Veine d'or agit tant qu'elle dure. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return Lang.f("Tes gains x{0} pendant {1} tours.", FACTOR, turns); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("VEINE D'OR : GAINS x{0}", FACTOR), EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.LUCKY_CHARM; }
}
