package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Jackpot englouti (la Salle des machines) : si les rouleaux font un Bingo,
 * gains et attaque du tirage sont multipliés (voir {@link fr.astratime.lucky.controllers.CombatResolver}).
 */
public class SunkenJackpotEffect extends Effect {

    /** Multiplicateur des gains sur un Bingo. */
    public static final int GAINS_FACTOR = 10;
    /** Multiplicateur de l'attaque sur un Bingo. */
    public static final int ATTACK_FACTOR = 3;

    @Override
    public void apply(TurnContext context) { context.getCombatContext().addSunkenJackpot(); }

    @Override
    public String getDescription() { return "Si tes rouleaux font un Bingo ce tour : gains x" + GAINS_FACTOR + " et attaque x" + ATTACK_FACTOR + "."; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("JACKPOT ENGLOUTI ?", EffectPopup.Style.GAINS, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.BINGO; }
}
