package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Corruption active ce tour : elle consomme une part des gains ; attaque et défense des symboles sont multipliées. */
public class CorruptionEvent extends GainsLostEvent {
    /** Multiplicateur de l'attaque et de la défense des symboles. */
    public final int factor;

    public CorruptionEvent(int amount, int factor) {
        super(amount);
        this.factor = factor;
    }

    @Override
    public String describe() { return "Corruption : -" + amount + " gains, attaque et defense x" + factor; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup("CORRUPTION : ATTAQUE ET DÉFENSE x" + factor, EffectPopup.Style.SPECIAL,
                PopupScale.MAX_INTENSITY),
            EffectPopup.scaled("GAINS -" + amount, EffectPopup.Style.DAMAGE, amount, PopupScale.SPIN_GAINS));
    }
}
