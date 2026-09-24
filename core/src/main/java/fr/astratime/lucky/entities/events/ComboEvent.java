package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Résultat d'une carte combo au lancer de la machine : réussie si les cartes
 * jouées ce tour forment la combinaison (gains et attaque multipliés), ratée sinon.
 */
public class ComboEvent extends Event {
    /** Nom affiché de la combinaison (ex : "BRELAN"). */
    public final String  name;
    /** {@code true} si la combinaison est formée. */
    public final boolean success;
    /** Multiplicateur de gains et d'attaque accordé (si réussie). */
    public final float   factor;

    public ComboEvent(String name, boolean success, float factor) {
        this.name    = name;
        this.success = success;
        this.factor  = factor;
    }

    @Override
    public String describe() { return name + (success ? " reussi : x" + factor : " rate"); }

    @Override
    public List<EffectPopup> getPopups() {
        if (!success) {
            return List.of(new EffectPopup(name + " RATÉ", EffectPopup.Style.DAMAGE, PopupScale.SECONDARY_INTENSITY));
        }
        String times = factor == (int) factor ? String.valueOf((int) factor) : String.valueOf(factor);
        return List.of(
            new EffectPopup(name + " !", EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY),
            new EffectPopup("GAINS x" + times + "  ATTAQUE x" + times, EffectPopup.Style.GAINS,
                PopupScale.SECONDARY_INTENSITY));
    }
}
