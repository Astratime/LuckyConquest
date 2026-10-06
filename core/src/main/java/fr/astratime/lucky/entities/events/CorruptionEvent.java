package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.i18n.Lang;
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
            new EffectPopup(Lang.f("CORRUPTION : ATTAQUE ET DÉFENSE x{0}", factor), EffectPopup.Style.SPECIAL,
                PopupScale.MAX_INTENSITY),
            EffectPopup.scaled(Lang.f("GAINS -{0}", amount), EffectPopup.Style.DAMAGE, amount, PopupScale.SPIN_GAINS));
    }
}
