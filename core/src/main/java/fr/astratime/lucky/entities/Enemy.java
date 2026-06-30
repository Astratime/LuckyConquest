package fr.astratime.lucky.entities;

/**
 * Comportement basique pour l'instant : attaque et défense fixes.
 * L'IA (variation de comportement, patterns) sera implémentée plus tard ;
 * ces deux valeurs sont la base sur laquelle elle viendra se greffer.
 */
public class Enemy {

    private static final int DEFAULT_ATTACK  = 10;
    private static final int DEFAULT_DEFENSE = 30;

    private final String name;
    private final int    maxHp;
    private       int    hp;
    private       int    attackPower = DEFAULT_ATTACK;
    private       int    defense     = DEFAULT_DEFENSE;

    public Enemy(String name, int maxHp) {
        this.name  = name;
        this.maxHp = maxHp;
        this.hp    = maxHp;
    }

    public void takeDamage(int damage) {
        hp = Math.max(0, hp - damage);
    }

    public boolean isDefeated() { return hp <= 0; }

    public String getName()        { return name; }
    public int    getHp()          { return hp; }
    public int    getMaxHp()       { return maxHp; }
    public int    getAttackPower() { return attackPower; }
    public int    getDefense()     { return defense; }
}
