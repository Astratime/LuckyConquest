package fr.astratime.lucky.entities.events;

public class EnemyDamagedEvent extends Event {
    public final int damage;

    public EnemyDamagedEvent(int damage) { this.damage = damage; }

    @Override
    public String describe() { return "Ennemi -" + damage + " PV"; }
}
