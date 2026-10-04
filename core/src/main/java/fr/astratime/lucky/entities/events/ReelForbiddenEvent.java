package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Événement émis quand le Rouleau interdit de l'ennemi bloque un rouleau du joueur pour son prochain tirage. */
public class ReelForbiddenEvent extends Event {
    /** Rouleau bloqué (0 à gauche). */
    public final int reel;

    public ReelForbiddenEvent(int reel) { this.reel = reel; }

    @Override
    public String describe() { return "Rouleau interdit : rouleau " + (reel + 1) + " bloque au prochain tirage"; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("ROULEAU INTERDIT !", EffectPopup.Style.DAMAGE, PopupScale.SECONDARY_INTENSITY * 1.4f));
    }
}
