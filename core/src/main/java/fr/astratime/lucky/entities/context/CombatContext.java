package fr.astratime.lucky.entities.context;

import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;

/**
 * Contexte de combat pour le tour en cours.
 * Contient les participants (joueur, ennemi) et les modificateurs de combat
 * issus des effets de cartes. Transmis à CombatResolver puis lu par chaque
 * Action lors de sa résolution — c'est le seul canal par lequel les cartes
 * influencent le combat.
 */
public class CombatContext {

    private final Player player;
    private final Enemy enemy;

    private int   attackBonus    = 0;
    private int   defenseBonus   = 0;   // shield additionnel par DefenseAction (cartes Carreau)
    private float gainMultiplier = 1f;  // multiplicateur des gains (cartes Trèfle)

    private boolean ignoreDefense  = false; // Pique : les attaques ignorent la défense ennemie
    private int     lifeDrainPercent = 0;   // Coeur : % des dégâts infligés rendus en soin
    private boolean gainsFromDamage  = false; // As de Pique : convertit les dégâts infligés en gains

    private int     reflectPercentBonus  = 0;     // Carreau : ajout additif au reflect par DefenseAction
    private boolean conditionalReflect   = false; // As de Carreau : reflect 50%/100% selon la vie du joueur

    public CombatContext(Player player, Enemy enemy) {
        this.player = player;
        this.enemy  = enemy;
    }

    public Player getPlayer() { return player; }
    public Enemy  getEnemy()  { return enemy; }

    public int   getAttackBonus()    { return attackBonus; }
    public int   getDefenseBonus()   { return defenseBonus; }
    public float getGainMultiplier() { return gainMultiplier; }

    public boolean isIgnoreDefense()    { return ignoreDefense; }
    public int     getLifeDrainPercent() { return lifeDrainPercent; }
    public boolean isGainsFromDamage()  { return gainsFromDamage; }

    public int     getReflectPercentBonus() { return reflectPercentBonus; }
    public boolean isConditionalReflect()   { return conditionalReflect; }

    public void addAttackBonus(int bonus)         { attackBonus  += bonus; }
    public void addDefenseBonus(int bonus)        { defenseBonus += bonus; }
    public void addGainMultiplier(float amount)   { gainMultiplier += amount; }

    public void setIgnoreDefense(boolean value)     { ignoreDefense = value; }
    public void addLifeDrainPercent(int percent)    { lifeDrainPercent += percent; }
    public void setGainsFromDamage(boolean value)   { gainsFromDamage = value; }

    public void addReflectPercentBonus(int percent) { reflectPercentBonus += percent; }
    public void setConditionalReflect(boolean value) { conditionalReflect = value; }
}
