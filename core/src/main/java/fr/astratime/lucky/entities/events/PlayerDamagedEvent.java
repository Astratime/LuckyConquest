package fr.astratime.lucky.entities.events;

/** Événement émis quand le joueur subit des dégâts (riposte de l'ennemi). */
public class PlayerDamagedEvent extends Event {
    /** Dégâts effectivement infligés au joueur. */
    public final int damage;

    /** @param damage dégâts effectivement infligés au joueur. */
    public PlayerDamagedEvent(int damage) { this.damage = damage; }

    @Override
    public String describe() { return "Joueur -" + damage + " PV"; }
}
