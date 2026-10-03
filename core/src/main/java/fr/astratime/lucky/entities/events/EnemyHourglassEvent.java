package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Événement émis quand le Sablier de l'ennemi avance ; arrivé au bout, il explose (ses dégâts suivent). */
public class EnemyHourglassEvent extends Event {
    /** Compte à rebours après ce cran (0 s'il vient d'exploser). */
    public final int count;
    /** Compte qui déclenche l'explosion. */
    public final int max;
    public final boolean exploded;

    public EnemyHourglassEvent(int count, int max, boolean exploded) {
        this.count    = count;
        this.max      = max;
        this.exploded = exploded;
    }

    @Override
    public String describe() { return exploded ? "Sablier : explosion" : "Sablier " + count + "/" + max; }

    @Override
    public List<EffectPopup> getPopups() {
        String text = exploded ? "BOUM !" : "SABLIER " + count + "/" + max;
        return List.of(new EffectPopup(text, EffectPopup.Style.SPECIAL,
            exploded ? PopupScale.SECONDARY_INTENSITY * 1.4f : PopupScale.SECONDARY_INTENSITY));
    }
}
