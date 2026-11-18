package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Brèche (Astra, 2026-11-18) : le surplus de dégâts arrêté par la Garde d'un boss ou d'un roi le frappe juste
 * après son changement de phase.
 */
public class BreachEvent extends Event {
    /** Dégâts infligés par la Brèche. */
    public final long damage;

    /** @param damage dégâts infligés par la Brèche */
    public BreachEvent(long damage) { this.damage = damage; }

    @Override
    public String describe() { return "Brèche -" + damage + " PV"; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(EffectPopup.scaled(Lang.f("BRÈCHE : -{0} PV", Lang.big(damage)), EffectPopup.Style.ATTACK, damage,
            PopupScale.SPIN_REFLECT));
    }
}
