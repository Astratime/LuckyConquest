package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.entities.Combo;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Combinaison formée par les cartes jouées ce tour, révélée au lancer de la
 * machine : les gains et l'attaque du tirage sont multipliés.
 */
public class ComboEvent extends Event {
    /** Combinaison formée. */
    public final Combo combo;

    public ComboEvent(Combo combo) {
        this.combo = combo;
    }

    @Override
    public String describe() { return combo.getDisplayName() + " : x" + combo.formatFactor(); }

    @Override
    public List<EffectPopup> getPopups() {
        String times = combo.formatFactor();
        return List.of(
            new EffectPopup(combo.getDisplayName() + " !", EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY),
            new EffectPopup(Lang.f("GAINS x{0}  ATTAQUE x{1}", times, times), EffectPopup.Style.GAINS,
                PopupScale.SECONDARY_INTENSITY));
    }
}
