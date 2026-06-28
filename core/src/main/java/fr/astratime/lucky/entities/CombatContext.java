package fr.astratime.lucky.entities;

/**
 * Contexte de combat pour le tour en cours.
 * Contient les participants (joueur, ennemi) et les modificateurs de combat
 * issus des effets de cartes (bonus d'attaque, de défense, multiplicateur).
 * Transmis à CombatResolver — il ne reçoit rien d'autre du GameState.
 */
public class CombatContext {

    private final Player player;
    private final Enemy  enemy;
    private int   attackBonus  = 0;
    private int   defenseBonus = 0;
    private float multiplier   = 1f;

    public CombatContext(Player player, Enemy enemy) {
        this.player = player;
        this.enemy  = enemy;
    }

    public Player getPlayer()    { return player; }
    public Enemy  getEnemy()     { return enemy; }
    public int    getAttackBonus()  { return attackBonus; }
    public int    getDefenseBonus() { return defenseBonus; }
    public float  getMultiplier()   { return multiplier; }

    public void addAttackBonus(int bonus)       { attackBonus  += bonus; }
    public void addDefenseBonus(int bonus)      { defenseBonus += bonus; }
    public void setMultiplier(float multiplier) { this.multiplier = multiplier; }
}
