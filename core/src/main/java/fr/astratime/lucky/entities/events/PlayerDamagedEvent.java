package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.entities.effects.EffectPopup;

import java.util.List;

/** Événement émis quand le joueur subit des dégâts (riposte de l'ennemi). */
public class PlayerDamagedEvent extends Event {
    /** Dégâts effectivement infligés au joueur. */
    public final int damage;

    /** @param damage dégâts effectivement infligés au joueur. */
    public PlayerDamagedEvent(int damage) { this.damage = damage; }

    @Override
    public String describe() { return "Joueur -" + damage + " PV"; }

    /** Vie perdue par le joueur ("BLOQUÉ !" si le bouclier a tout absorbé) ; 30 donne la taille maximale. */
    @Override
    public List<EffectPopup> getPopups() {
        if (damage == 0) return List.of(new EffectPopup("BLOQUÉ !", EffectPopup.Style.DEFENSE, 0.6f));
        return List.of(EffectPopup.scaled("PV -" + damage, EffectPopup.Style.DAMAGE, damage, 30f));
    }
}
