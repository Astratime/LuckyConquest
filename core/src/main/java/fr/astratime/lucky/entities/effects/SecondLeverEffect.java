package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Second levier (archétype Relanceur) : si le tirage fait une paire sans Bingo, le rouleau qui manque
 * au Bingo relance (voir {@link fr.astratime.lucky.controllers.TurnEngine}).
 */
public class SecondLeverEffect extends Effect {
    private final int tries;

    /**
     * Second levier.
     * @param tries relances du rouleau qui manque au Bingo
     */
    public SecondLeverEffect(int tries) {
        this.tries = tries;
    }

    @Override
    public void apply(TurnContext context) {
        context.getSpinContext().addSecondLevers(tries);
    }

    @Override
    public String getDescription() { return Lang.f("Si le tirage fait une paire sans Bingo, le rouleau qui manque relance jusqu'à {0} fois pour tenter le Bingo.", tries); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("SECOND LEVIER"), EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.REROLL; }

}
