package fr.astratime.lucky.entities.events;

/** Événement émis quand le bouclier de renvoi du joueur (Carreau) renvoie des dégâts à l'ennemi. */
public class DamageReflectedEvent extends Event {
    /** Dégâts renvoyés à l'ennemi. */
    public final int damage;

    /** @param damage dégâts renvoyés à l'ennemi. */
    public DamageReflectedEvent(int damage) { this.damage = damage; }

    @Override
    public String describe() { return "Renvoi -" + damage + " PV a l'ennemi"; }
}
