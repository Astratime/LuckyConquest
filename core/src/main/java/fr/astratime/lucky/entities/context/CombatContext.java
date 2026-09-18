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

    /**
     * @param player joueur du combat en cours
     * @param enemy  ennemi du combat en cours
     */
    public CombatContext(Player player, Enemy enemy) {
        this.player = player;
        this.enemy  = enemy;
    }

    /** @return le joueur du combat en cours. */
    public Player getPlayer() { return player; }
    /** @return l'ennemi du combat en cours. */
    public Enemy  getEnemy()  { return enemy; }

    /** @return le bonus d'attaque plat accumulé ce tour. */
    public int   getAttackBonus()    { return attackBonus; }
    /** @return le bonus de bouclier plat accumulé ce tour. */
    public int   getDefenseBonus()   { return defenseBonus; }
    /** @return le multiplicateur de gains accumulé ce tour. */
    public float getGainMultiplier() { return gainMultiplier; }

    /** @return {@code true} si les attaques de ce tour ignorent la défense ennemie. */
    public boolean isIgnoreDefense()    { return ignoreDefense; }
    /** @return le pourcentage de drain de vie accumulé ce tour. */
    public int     getLifeDrainPercent() { return lifeDrainPercent; }
    /** @return {@code true} si les dégâts infligés sont convertis en gains ce tour. */
    public boolean isGainsFromDamage()  { return gainsFromDamage; }

    /** @return le bonus additif de renvoi de dégâts accordé par symbole ce tour. */
    public int     getReflectPercentBonus() { return reflectPercentBonus; }
    /** @return {@code true} si le renvoi de dégâts est conditionnel à la vie du joueur (As de Carreau). */
    public boolean isConditionalReflect()   { return conditionalReflect; }

    /** Ajoute {@code bonus} au bonus d'attaque du tour. */
    public void addAttackBonus(int bonus)         { attackBonus  += bonus; }
    /** Ajoute {@code bonus} au bonus de bouclier du tour. */
    public void addDefenseBonus(int bonus)        { defenseBonus += bonus; }
    /** Ajoute {@code amount} au multiplicateur de gains du tour. */
    public void addGainMultiplier(float amount)   { gainMultiplier += amount; }

    /** Active ou désactive l'ignorance de la défense ennemie pour ce tour. */
    public void setIgnoreDefense(boolean value)     { ignoreDefense = value; }
    /** Ajoute {@code percent} au pourcentage de drain de vie du tour. */
    public void addLifeDrainPercent(int percent)    { lifeDrainPercent += percent; }
    /** Active ou désactive la conversion des dégâts infligés en gains pour ce tour. */
    public void setGainsFromDamage(boolean value)   { gainsFromDamage = value; }

    /** Ajoute {@code percent} au bonus additif de renvoi de dégâts du tour. */
    public void addReflectPercentBonus(int percent) { reflectPercentBonus += percent; }
    /** Active ou désactive le renvoi de dégâts conditionnel à la vie du joueur. */
    public void setConditionalReflect(boolean value) { conditionalReflect = value; }
}
