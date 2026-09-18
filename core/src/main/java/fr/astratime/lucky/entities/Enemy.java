package fr.astratime.lucky.entities;

/**
 * Comportement basique pour l'instant : attaque et défense fixes.
 * L'IA (variation de comportement, patterns) sera implémentée plus tard ;
 * ces deux valeurs sont la base sur laquelle elle viendra se greffer.
 */
public class Enemy {

    /** Dégâts infligés par défaut par la riposte de l'ennemi (voir {@link #getAttackPower()}). */
    private static final int DEFAULT_ATTACK  = 10;
    /** Défense par défaut, qui réduit les dégâts reçus des attaques du joueur. */
    private static final int DEFAULT_DEFENSE = 30;

    private final String name;
    private final int    maxHp;
    private       int    hp;
    private       int    attackPower = DEFAULT_ATTACK;
    private       int    defense     = DEFAULT_DEFENSE;

    /**
     * @param name  nom affiché de l'ennemi
     * @param maxHp points de vie maximum (l'ennemi démarre à pleine vie)
     */
    public Enemy(String name, int maxHp) {
        this.name  = name;
        this.maxHp = maxHp;
        this.hp    = maxHp;
    }

    /** Retire {@code damage} points de vie, sans descendre sous 0. */
    public void takeDamage(int damage) {
        hp = Math.max(0, hp - damage);
    }

    /** @return {@code true} si l'ennemi n'a plus de points de vie. */
    public boolean isDefeated() { return hp <= 0; }

    /** @return le nom affiché de l'ennemi. */
    public String getName()        { return name; }
    /** @return les points de vie actuels. */
    public int    getHp()          { return hp; }
    /** @return les points de vie maximum. */
    public int    getMaxHp()       { return maxHp; }
    /** @return les dégâts infligés au joueur lors de la riposte de l'ennemi. */
    public int    getAttackPower() { return attackPower; }
    /** @return la défense de l'ennemi, qui réduit les dégâts des attaques du joueur. */
    public int    getDefense()     { return defense; }
}
