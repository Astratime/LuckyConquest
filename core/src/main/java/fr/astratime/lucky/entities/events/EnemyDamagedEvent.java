package fr.astratime.lucky.entities.events;

/** Événement émis quand l'ennemi subit des dégâts (attaque du joueur). */
public class EnemyDamagedEvent extends Event {
    /** Dégâts effectivement infligés à l'ennemi (après défense). */
    public final int damage;

    /** @param damage dégâts effectivement infligés à l'ennemi. */
    public EnemyDamagedEvent(int damage) { this.damage = damage; }

    @Override
    public String describe() { return "Ennemi -" + damage + " PV"; }
}
