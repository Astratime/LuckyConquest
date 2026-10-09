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
    /** Renvoi des cartes Carreau au plus, en % des PV max de l'ennemi, par tour ennemi. */
    public static final float MAX_REFLECT_HP_PERCENT = 12f;

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
    private int   executionPercent = 0; // Guillotine : % des PV restants de l'ennemi infligés d'un coup (0 : aucun)

    private final List<Symbol>  bets        = new ArrayList<>(); // Pari : symboles sur lesquels le joueur a parié
    private final List<Integer> pistolShots = new ArrayList<>(); // Roulette russe : part (%) du meilleur coup rejouée par chaque tir

    private int     rankFactor = 1;      // Jeton de rang : multiplicateur du bonus du rang
    private int     allIn      = 0;      // Tapis : nombre de mises de tous les gains sur une paire

    private boolean stoneGains     = false; // Pépite de l'ennemi : les symboles de gain ne rapportent rien

    // ----- Cartes des coffres des lieux -----
    private float comboBonus    = 0f; // Chope : ajouté au multiplicateur de chaque combinaison
    private int   hammers       = 0;  // Marteau de forge : bonus d'attaque renforcé si un BAR sort
    private int   levers        = 0;  // Levier rouillé : bonus d'attaque si le tirage fait une paire
    private int   sunkenJackpots = 0; // Jackpot englouti : gains et attaque multipliés sur un Bingo
    private int   forgedBlades  = 0;  // Lame forgée : coups d'épée en plus, à une part de la plus grosse attaque
    private int   dynamitePercent = 0; // Dynamite : % des PV max de l'ennemi infligés, sans peau ni défense
    private int   goldenHearts  = 0;  // Cœur d'or : les gains du tirage frappent aussi l'ennemi
    private int     piercePercent  = 0;     // Pique : part (%) de chaque coup qui traverse la défense ennemie
    private int     lifeDrainPercent = 0;   // Coeur : % des dégâts infligés rendus en soin
    private boolean gainsFromDamage  = false; // As de Pique : convertit les dégâts infligés en gains

    private float   reflectHpPercent     = 0f;    // Carreau : renvoi en % des PV max ennemis, additionné, actif si un symbole de défense sort
    private boolean defenseSymbolDrawn   = false; // un symbole de défense est sorti ce tour (active reflectHpPercent)
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
    /** @return la part des PV restants de l'ennemi infligée d'un coup par la Guillotine, en % (0 : aucune). */
    public int   getExecutionPercent() { return executionPercent; }
    /** @return le facteur de la valeur de chaque symbole : dégâts, bouclier et gains (1 si aucun). */
    public int   getSymbolPower()    { return symbolPower; }
    /** @return les symboles sur lesquels le joueur a parié ce tour (vue non modifiable). */
    public List<Symbol>  getBets()        { return Collections.unmodifiableList(bets); }
    /** @return la part (%) du meilleur coup rejouée par chaque tir de pistolet de ce tour (vue non modifiable). */
    public List<Integer> getPistolShots() { return Collections.unmodifiableList(pistolShots); }

    /** @return le multiplicateur du bonus du rang ce tour (1 si aucun Jeton de rang). */
    public int getRankFactor() { return rankFactor; }
    /** Jeton de rang : multiplie le bonus du rang par {@code factor} ce tour. */
    public void multiplyRankBonus(int factor) { rankFactor *= factor; }

    /** @return le nombre de Tapis joués ce tour (0 : aucune mise). */
    public int getAllIn() { return allIn; }
    /** Tapis : tous les gains sont misés sur une paire. */
    public void addAllIn() { allIn++; }

    /** @return {@code true} si les symboles de gain ne rapportent rien ce tour (Pépite de l'ennemi). */
    public boolean isStoneGains() { return stoneGains; }
    /** Pépite de l'ennemi : les symboles de gain de ce tour deviennent des pierres. */
    public void turnGainsToStone() { stoneGains = true; }
    /** Tamis : les pierres de ce tour redeviennent de l'or. */
    public void restoreGains() { stoneGains = false; }

    /** Chope : chaque combinaison de ce tour compte {@code bonus} de plus au multiplicateur. */
    public void addComboBonus(float bonus) { comboBonus += bonus; }
    /** @return ce qu'ajoute la Chope au multiplicateur de chaque combinaison (0 si aucune). */
    public float getComboBonus() { return comboBonus; }

    /** Marteau de forge : son bonus d'attaque grandit si un BAR ou un double BAR sort. */
    public void addHammer() { hammers++; }
    /** @return les Marteaux de forge joués ce tour. */
    public int getHammers() { return hammers; }

    /** Levier rouillé : bonus d'attaque si le tirage fait une paire. */
    public void addLever() { levers++; }
    /** @return les Leviers rouillés joués ce tour. */
    public int getLevers() { return levers; }

    /** Jackpot englouti : gains et attaque multipliés si les rouleaux font un Bingo. */
    public void addSunkenJackpot() { sunkenJackpots++; }
    /** @return les Jackpots engloutis joués ce tour. */
    public int getSunkenJackpots() { return sunkenJackpots; }

    /** Lame forgée : un coup d'épée de plus après le tirage. */
    public void addForgedBlade() { forgedBlades++; }
    /** @return les Lames forgées jouées ce tour. */
    public int getForgedBlades() { return forgedBlades; }

    /** Dynamite : {@code percent} % des PV max de l'ennemi de plus, infligés après le tirage. */
    public void addDynamite(int percent) { dynamitePercent += percent; }
    /** @return la part des PV max de l'ennemi infligée par la Dynamite, en %. */
    public int getDynamitePercent() { return dynamitePercent; }

    /** Cœur d'or : les gains gagnés ce tour frappent aussi l'ennemi. */
    public void addGoldenHeart() { goldenHearts++; }
    /** @return les Cœurs d'or joués ce tour. */
    public int getGoldenHearts() { return goldenHearts; }

    /** @return {@code true} si les attaques de ce tour ignorent toute la défense ennemie. */
    public boolean isIgnoreDefense()    { return piercePercent >= 100; }
    /** @return la part (%) de chaque coup de ce tour qui traverse la défense ennemie (100 : elle est ignorée). */
    public int     getPiercePercent()   { return piercePercent; }
    /** @return le pourcentage de drain de vie accumulé ce tour. */
    public int     getLifeDrainPercent() { return lifeDrainPercent; }
    /** @return {@code true} si les dégâts infligés sont convertis en gains ce tour. */
    public boolean isGainsFromDamage()  { return gainsFromDamage; }

    /** @return le renvoi des cartes Carreau en % des PV max ennemis, additionné sur toutes les cartes jouées ce tour. */
    public float   getReflectHpBonus()      { return reflectHpPercent; }
    /** @return {@code true} si un symbole de défense est sorti ce tour. */
    public boolean isDefenseSymbolDrawn()   { return defenseSymbolDrawn; }

    /**
     * Pourcentage de l'attaque ennemie renvoyé lors de la riposte : le renvoi
     * garanti (As de Carreau, Miroir taillé), selon la vie actuelle du joueur.
     */
    public int getTotalReflectPercent() {
        return player.getHpRatio() < LOW_HP_RATIO ? guaranteedReflectLowHpPercent : guaranteedReflectPercent;
    }

    /**
     * Renvoi des cartes Carreau (2 à Roi), en % des PV max de l'ennemi : actif si
     * au moins un symbole de défense est sorti ce tour, plafonné à {@link #MAX_REFLECT_HP_PERCENT}.
     */
    public float getReflectHpPercent() {
        return defenseSymbolDrawn ? Math.min(MAX_REFLECT_HP_PERCENT, reflectHpPercent) : 0f;
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
    /** Guillotine : ajoute {@code percent} % des PV restants de l'ennemi, infligés d'un coup après le tirage. */
    public void addExecution(int percent)         { executionPercent += percent; }
    /** Multiplie la valeur de chaque symbole (dégâts, bouclier, gains) par {@code factor}. */
    public void multiplySymbolPower(int factor)   { symbolPower  *= factor; }
    /** Parie sur l'apparition de {@code symbol} au tirage. */
    public void addBet(Symbol symbol)             { bets.add(symbol); }
    /** Ajoute un tir de pistolet qui rejoue {@code percent} % du coup le plus fort du tour. */
    public void addPistolShot(int percent)        { pistolShots.add(percent); }

    /** Active ou désactive l'ignorance de toute la défense ennemie pour ce tour. */
    public void setIgnoreDefense(boolean value)     { piercePercent = value ? 100 : 0; }
    /** Pique : {@code percent} % de chaque coup traverse la défense ; seul le meilleur perçage du tour compte. */
    public void pierceDefense(int percent)          { piercePercent = Math.max(piercePercent, Math.min(100, percent)); }
    /** Ajoute {@code percent} au pourcentage de drain de vie du tour. */
    public void addLifeDrainPercent(int percent)    { lifeDrainPercent += percent; }
    /** Active ou désactive la conversion des dégâts infligés en gains pour ce tour. */
    public void setGainsFromDamage(boolean value)   { gainsFromDamage = value; }

    /** Ajoute {@code percent} % des PV max ennemis au renvoi des cartes Carreau (actif si un symbole de défense sort). */
    public void addReflectHpPercent(float percent)  { reflectHpPercent += percent; }
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
