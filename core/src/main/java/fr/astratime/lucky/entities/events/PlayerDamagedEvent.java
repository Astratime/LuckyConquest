package fr.astratime.lucky.entities.events;

public class PlayerDamagedEvent extends Event {
    public final int damage;

    public PlayerDamagedEvent(int damage) { this.damage = damage; }

    @Override
    public String describe() { return "Joueur -" + damage + " PV"; }
}
