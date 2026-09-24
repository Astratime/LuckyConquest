package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Événement émis quand les trois symboles tirés sont identiques (jackpot). */
public class JackpotEvent extends Event {
    @Override
    public String describe() { return "JACKPOT !"; }

    /** Toujours affiché en grand. */
    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("JACKPOT !", EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY));
    }
}
