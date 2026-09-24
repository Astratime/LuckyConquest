package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Événement émis quand le joueur subit des dégâts (riposte de l'ennemi). */
public class PlayerDamagedEvent extends Event {
    /** Dégâts effectivement infligés au joueur. */
    public final int damage;

    /** @param damage dégâts effectivement infligés au joueur. */
    public PlayerDamagedEvent(int damage) { this.damage = damage; }

    @Override
    public String describe() { return "Joueur -" + damage + " PV"; }

    /** Vie perdue par le joueur ("BLOQUÉ !" si le bouclier a tout absorbé), taille maximale selon {@link PopupScale}. */
    @Override
    public List<EffectPopup> getPopups() {
        if (damage == 0) return List.of(new EffectPopup("BLOQUÉ !", EffectPopup.Style.DEFENSE, PopupScale.SECONDARY_INTENSITY));
        return List.of(EffectPopup.scaled("PV -" + damage, EffectPopup.Style.DAMAGE, damage, PopupScale.SPIN_LIFE_LOST));
    }
}
