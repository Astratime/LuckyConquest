package fr.astratime.lucky.entities;

public class Enemy {

    private final String name;
    private final int    maxHp;
    private       int    hp;

    public Enemy(String name, int maxHp) {
        this.name  = name;
        this.maxHp = maxHp;
        this.hp    = maxHp;
    }

    public void takeDamage(int damage) {
        hp = Math.max(0, hp - damage);
    }

    public boolean isDefeated() { return hp <= 0; }

    public String getName()  { return name; }
    public int    getHp()    { return hp; }
    public int    getMaxHp() { return maxHp; }
}
