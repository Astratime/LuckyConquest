package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Événement émis quand l'ennemi gagne de la Rage : ses attaques frappent plus fort jusqu'à la fin du combat. */
public class EnemyRageEvent extends Event {
    /** Attaque ajoutée (0 si sa Rage est déjà au maximum). */
    public final int amount;
    /** Bonus d'attaque total de sa Rage. */
    public final int total;

    public EnemyRageEvent(int amount, int total) {
        this.amount = amount;
        this.total  = total;
    }

    @Override
    public String describe() { return "Ennemi rage +" + amount + " (total " + total + ")"; }

    @Override
    public List<EffectPopup> getPopups() {
        String text = amount > 0 ? "RAGE +" + total : "RAGE MAX";
        return List.of(new EffectPopup(text, EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }
}
