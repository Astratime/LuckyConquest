package fr.astratime.lucky.entities.context;

import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contexte de combat pour le tour en cours.
 * Contient les participants (joueur, ennemi) et les modificateurs de combat
 * issus des effets de cartes. Transmis à CombatResolver puis lu par chaque
 * Action lors de sa résolution — c'est le seul canal par lequel les cartes
 * influencent le combat.
 */
public class CombatContext {

    /** Seuil de vie (en proportion) sous lequel le renvoi garanti de l'As de Carreau est renforcé. */
    public static final float LOW_HP_RATIO = 0.2f;

    private final Player player;
    private final Enemy enemy;

    private int   attackBonus    = 0;
    private int   defenseBonus   = 0;   // shield additionnel par DefenseAction (cartes Carreau)
    private float gainMultiplier = 1f;  // multiplicateur des gains (cartes Trèfle)

    private float gainFactor     = 1f;  // facteur appliqué à tous les gains du tirage (combos, Porte-bonheur)
    private float attackFactor   = 1f;  // facteur appliqué aux dégâts de chaque symbole (combos)
    private int   symbolPower    = 1;   // facteur de la valeur de chaque symbole : dégâts, bouclier, gains (Bingo)
    private float defenseFactor  = 1f;  // facteur du bouclier de chaque symbole (Corruption)
    private int   counterAttack  = 0;   // As de Carreau : multiplicateur du Coffre infligé en contre-attaque (0 : aucune)

    private final List<Symbol>  bets        = new ArrayList<>(); // Pari : symboles sur lesquels le joueur a parié
    private final List<Integer> pistolShots = new ArrayList<>(); // Roulette russe : multiplicateur de chaque tir de pistolet

    private boolean ignoreDefense  = false; // Pique : les attaques ignorent la défense ennemie
    private int     lifeDrainPercent = 0;   // Coeur : % des dégâts infligés rendus en soin
    private boolean gainsFromDamage  = false; // As de Pique : convertit les dégâts infligés en gains

    private int     reflectPercentBonus  = 0;     // Carreau : renvoi additionné sur toutes les cartes, actif si un symbole de défense sort
    private boolean defenseSymbolDrawn   = false; // un symbole de défense est sorti ce tour (active reflectPercentBonus)
    private int     guaranteedReflectPercent      = 0; // As de Carreau : renvoi garanti, sans symbole de défense
    private int     guaranteedReflectLowHpPercent = 0; // As de Carreau : renvoi garanti si le joueur est sous LOW_HP_RATIO

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

    /** @return le facteur appliqué à tous les gains du tirage (1 si aucun). */
    public float getGainFactor()     { return gainFactor; }
    /** @return le facteur appliqué aux dégâts de chaque symbole (1 si aucun). */
    public float getAttackFactor()   { return attackFactor; }
    /** @return le facteur du bouclier de chaque symbole (1 si aucun). */
    public float getDefenseFactor()  { return defenseFactor; }
    /** @return le multiplicateur du Coffre infligé en contre-attaque ce tour (0 si aucune). */
    public int   getCounterAttack()  { return counterAttack; }
    /** @return le facteur de la valeur de chaque symbole : dégâts, bouclier et gains (1 si aucun). */
    public int   getSymbolPower()    { return symbolPower; }
    /** @return les symboles sur lesquels le joueur a parié ce tour (vue non modifiable). */
    public List<Symbol>  getBets()        { return Collections.unmodifiableList(bets); }
    /** @return le multiplicateur de chaque tir de pistolet de ce tour (vue non modifiable). */
    public List<Integer> getPistolShots() { return Collections.unmodifiableList(pistolShots); }

    /** @return {@code true} si les attaques de ce tour ignorent la défense ennemie. */
    public boolean isIgnoreDefense()    { return ignoreDefense; }
    /** @return le pourcentage de drain de vie accumulé ce tour. */
    public int     getLifeDrainPercent() { return lifeDrainPercent; }
    /** @return {@code true} si les dégâts infligés sont convertis en gains ce tour. */
    public boolean isGainsFromDamage()  { return gainsFromDamage; }

    /** @return le renvoi de dégâts des cartes Carreau, additionné sur toutes les cartes jouées ce tour. */
    public int     getReflectPercentBonus() { return reflectPercentBonus; }
    /** @return {@code true} si un symbole de défense est sorti ce tour. */
    public boolean isDefenseSymbolDrawn()   { return defenseSymbolDrawn; }

    /**
     * Pourcentage de l'attaque ennemie renvoyé lors de la riposte : renvoi garanti
     * (As de Carreau, selon la vie actuelle du joueur) + renvoi des cartes Carreau
     * si au moins un symbole de défense est sorti ce tour.
     */
    public int getTotalReflectPercent() {
        int guaranteed = player.getHpRatio() < LOW_HP_RATIO ? guaranteedReflectLowHpPercent : guaranteedReflectPercent;
        return guaranteed + (defenseSymbolDrawn ? reflectPercentBonus : 0);
    }

    /** Ajoute {@code bonus} au bonus d'attaque du tour. */
    public void addAttackBonus(int bonus)         { attackBonus  += bonus; }
    /** Ajoute {@code bonus} au bonus de bouclier du tour. */
    public void addDefenseBonus(int bonus)        { defenseBonus += bonus; }
    /** Ajoute {@code amount} au multiplicateur de gains du tour. */
    public void addGainMultiplier(float amount)   { gainMultiplier += amount; }

    /** Multiplie tous les gains du tirage par {@code factor}. */
    public void multiplyGains(float factor)       { gainFactor   *= factor; }
    /** Multiplie les dégâts de chaque symbole par {@code factor}. */
    public void multiplyAttack(float factor)      { attackFactor *= factor; }
    /** Multiplie le bouclier de chaque symbole par {@code factor}. */
    public void multiplyDefense(float factor)     { defenseFactor *= factor; }
    /** Contre-attaque (As de Carreau) : le Coffre, multiplié par {@code factor}, est infligé à l'ennemi. */
    public void addCounterAttack(int factor)      { counterAttack += factor; }
    /** Multiplie la valeur de chaque symbole (dégâts, bouclier, gains) par {@code factor}. */
    public void multiplySymbolPower(int factor)   { symbolPower  *= factor; }
    /** Parie sur l'apparition de {@code symbol} au tirage. */
    public void addBet(Symbol symbol)             { bets.add(symbol); }
    /** Ajoute un tir de pistolet qui multiplie par {@code multiplier} les dégâts d'un symbole d'attaque. */
    public void addPistolShot(int multiplier)     { pistolShots.add(multiplier); }

    /** Active ou désactive l'ignorance de la défense ennemie pour ce tour. */
    public void setIgnoreDefense(boolean value)     { ignoreDefense = value; }
    /** Ajoute {@code percent} au pourcentage de drain de vie du tour. */
    public void addLifeDrainPercent(int percent)    { lifeDrainPercent += percent; }
    /** Active ou désactive la conversion des dégâts infligés en gains pour ce tour. */
    public void setGainsFromDamage(boolean value)   { gainsFromDamage = value; }

    /** Ajoute {@code percent} au renvoi des cartes Carreau (actif si un symbole de défense sort). */
    public void addReflectPercentBonus(int percent) { reflectPercentBonus += percent; }
    /** Signale qu'un symbole de défense est sorti ce tour : le renvoi des cartes Carreau s'active. */
    public void markDefenseSymbolDrawn()            { defenseSymbolDrawn = true; }

    /**
     * Ajoute un renvoi garanti, actif même sans symbole de défense (As de Carreau).
     *
     * @param percent      renvoi appliqué normalement
     * @param lowHpPercent renvoi appliqué si le joueur est sous {@link #LOW_HP_RATIO} de sa vie
     */
    public void addGuaranteedReflect(int percent, int lowHpPercent) {
        guaranteedReflectPercent      += percent;
        guaranteedReflectLowHpPercent += lowHpPercent;
    }
}
