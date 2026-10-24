package fr.astratime.lucky.entities.context;

import fr.astratime.lucky.entities.BigNumber;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.SpinEconomy;
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
    /** Nom d'une étape du détail d'un symbole dont l'origine n'est pas précisée. */
    private static final String OTHER_EFFECTS = "Autres effets";

    private final Player player;
    private final Enemy enemy;
    private final int   spinCost;

    private long  attackBonus    = 0;   // en long : l'As de Trèfle et la Fortune en ajoutent beaucoup
    private int   defenseBonus   = 0;   // shield additionnel par DefenseAction (cartes Carreau)
    private float gainMultiplier = 1f;  // multiplicateur des gains (cartes Trèfle)

    private float gainFactor     = 1f;  // facteur appliqué à tous les gains du tirage (combos, Porte-bonheur)
    private float attackFactor   = 1f;  // facteur appliqué aux dégâts de chaque symbole (combos)
    private int   symbolPower    = 1;   // facteur de la valeur de chaque symbole : dégâts, bouclier, gains (Bingo)
    private float defenseFactor  = 1f;  // facteur du bouclier de chaque symbole (Corruption)
    private int   counterAttack  = 0;   // As de Carreau : multiplicateur du Coffre infligé en contre-attaque (0 : aucune)
    private int   executionPercent = 0; // Guillotine : % des PV restants de l'ennemi infligés d'un coup (0 : aucun)

    /** Ce qui a changé l'attaque, le bouclier et les gains des symboles ce tour, dans l'ordre (voir {@link #getSteps}). */
    private final List<PowerStep> steps = new ArrayList<>();
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
    // ----- Archétypes de deck -----
    private int   wildJokerPercent = 0; // Joker sauvage : attaque en plus par Joker sorti, en %
    private int   jackStep      = 0;  // Diable en boîte : Joker en plus (tout le combat) par Joker sorti
    private int   jackMax       = 0;  // Diable en boîte : Joker en plus au plus
    private int   jokerLaughs   = 0;  // Rire du Joker : cartes de plus à la prochaine pioche si un Joker sort
    private float foolCrownFactor = 1f; // Couronne du Fou : gains d'un Bingo fait avec un Joker
    private final List<String> rainbowsEnds = new ArrayList<>(); // Bout de l'arc-en-ciel : cartes glissées dans la pioche
    private int   cobblerPercent = 0; // Marteau du cordonnier : gains du combat en plus si l'ennemi tombe ce tour, en %
    private int   prankSpins    = 0;  // Farce du Lutin : gains (en coûts de tirage) si toute sa défense tombe
    private int   prankBlades   = 0;  // Farce du Lutin : Lames si toute sa défense tombe
    private int   jigSpinsPerBlade = 0; // Gigue du Lutin : coûts de tirage gagnés pour une Lame (0 : aucune Gigue)
    private int   jigMaxBlades  = 0;  // Gigue du Lutin : Lames au plus
    private int   variableRate  = 0;  // Taux variable : Créances par symbole de gains sorti
    private float collectionHpPercent = 0f; // Recouvrement : % des PV max de l'ennemi par Créance encaissée
    private int   collectionSpins = 0; // Recouvrement : gains (en coûts de tirage) par Créance encaissée
    private int   bluffPercent  = 0;  // Coup de bluff : attaque en plus par malus de l'ennemi, en %
    private int   bluffMax      = 0;  // Coup de bluff : attaque en plus au plus, en %
    private boolean martingale  = false; // Martingale jouée ce tour
    private int   martingaleAttack = 0;  // Martingale : attaque par tirage raté au tirage suivant, en %
    private int   martingalePair   = 0;  // Martingale : chance de paire par tirage raté au tirage suivant, en %
    /** Défense de l'ennemi au début du tour, avant les cartes (Farce du Lutin). */
    private final int enemyDefenseAtStart;

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
        this.spinCost = enemy != null ? enemy.getKind().getSpinCost() : SpinEconomy.BASE_SPIN_COST;
        this.enemyDefenseAtStart = enemy != null ? enemy.getDefense() : 0;
    }

    // ----- Archétypes de deck -----

    /** Joker sauvage : chaque Joker sorti ce tour ajoute {@code percent} % d'attaque. */
    public void addWildJoker(int percent) { wildJokerPercent += percent; }
    /** @return l'attaque en plus par Joker sorti ce tour, en % (0 : aucun Joker sauvage). */
    public int getWildJokerPercent() { return wildJokerPercent; }

    /** Rire du Joker : {@code cards} cartes de plus à la prochaine pioche si un Joker sort. */
    /** Diable en boîte : chaque Joker sorti ce tour donne {@code step} Joker de plus jusqu'à la fin du combat. */
    public void addJackInTheBox(int step, int max) {
        jackStep += step;
        jackMax = Math.max(jackMax, max);
    }
    /** @return le Joker gagné par Joker sorti (0 : aucun Diable en boîte). */
    public int getJackStep() { return jackStep; }
    /** @return le Joker gagné au plus avec le Diable en boîte. */
    public int getJackMax() { return jackMax; }

    public void addJokerLaugh(int cards) { jokerLaughs += cards; }
    /** @return les cartes de plus à la prochaine pioche si un Joker sort ce tour. */
    public int getJokerLaughs() { return jokerLaughs; }

    /** Couronne du Fou : un Bingo fait avec un Joker multiplie les gains du tirage par {@code factor}. */
    public void addFoolCrown(float factor) { foolCrownFactor *= factor; }
    /** @return le multiplicateur des gains d'un Bingo fait avec un Joker (1 : aucune Couronne). */
    public float getFoolCrownFactor() { return foolCrownFactor; }

    /** Bout de l'arc-en-ciel : la carte {@code cardId} rejoint la pioche si deux symboles d'attaque sortent ensemble. */
    public void addRainbowsEnd(String cardId) { rainbowsEnds.add(cardId); }
    /** @return les cartes du Bout de l'arc-en-ciel joué ce tour, par id. */
    public List<String> getRainbowsEnds() { return Collections.unmodifiableList(rainbowsEnds); }

    /** Marteau du cordonnier : si l'ennemi tombe ce tour, gains du combat +{@code percent} %. */
    public void addCobbler(int percent) { cobblerPercent += percent; }
    /** @return les gains du combat en plus si l'ennemi tombe ce tour, en %. */
    public int getCobblerPercent() { return cobblerPercent; }

    /** Farce du Lutin : si toute la défense de l'ennemi tombe ce tour, {@code spins} coûts de tirage et {@code blades} Lames. */
    public void addPrank(int spins, int blades) {
        prankSpins  += spins;
        prankBlades += blades;
    }
    /** @return les gains de la Farce du Lutin, en coûts de tirage. */
    public int getPrankSpins() { return prankSpins; }
    /** @return les Lames de la Farce du Lutin. */
    public int getPrankBlades() { return prankBlades; }
    /** @return la défense de l'ennemi au début du tour, avant les cartes. */
    public int getEnemyDefenseAtStart() { return enemyDefenseAtStart; }

    /** Gigue du Lutin : une Lame par {@code spinsPerBlade} coûts de tirage gagnés ce tour, {@code maxBlades} au plus. */
    public void addJig(int spinsPerBlade, int maxBlades) {
        jigSpinsPerBlade = jigSpinsPerBlade == 0 ? spinsPerBlade : Math.min(jigSpinsPerBlade, spinsPerBlade);
        jigMaxBlades += maxBlades;
    }
    /** @return les coûts de tirage gagnés pour une Lame (0 : aucune Gigue du Lutin). */
    public int getJigSpinsPerBlade() { return jigSpinsPerBlade; }
    /** @return les Lames de la Gigue du Lutin au plus. */
    public int getJigMaxBlades() { return jigMaxBlades; }

    /** Taux variable : {@code perSymbol} Créances par symbole de gains sorti ce tour. */
    public void addVariableRate(int perSymbol) { variableRate += perSymbol; }
    /** @return les Créances par symbole de gains sorti ce tour. */
    public int getVariableRate() { return variableRate; }

    /** Recouvrement : les Créances sont encaissées ({@code hpPercent} % des PV max et {@code spins} coûts de tirage chacune). */
    public void addDebtCollection(float hpPercent, int spins) {
        collectionHpPercent += hpPercent;
        collectionSpins     += spins;
    }
    /** @return les PV max de l'ennemi retirés par Créance encaissée, en % (0 : aucun Recouvrement). */
    public float getCollectionHpPercent() { return collectionHpPercent; }
    /** @return les gains par Créance encaissée, en coûts de tirage. */
    public int getCollectionSpins() { return collectionSpins; }

    /** Coup de bluff : {@code percent} % d'attaque par malus de l'ennemi, {@code max} % au plus. */
    public void addBluffCall(int percent, int max) {
        bluffPercent += percent;
        bluffMax     += max;
    }
    /** @return l'attaque en plus par malus de l'ennemi, en %. */
    public int getBluffPercent() { return bluffPercent; }
    /** @return l'attaque en plus du Coup de bluff au plus, en %. */
    public int getBluffMax() { return bluffMax; }

    /** Martingale : si ce tirage n'a pas de paire, le suivant aura {@code attackPercent} % d'attaque et {@code pairPercent} % de paire en plus. */
    public void addMartingale(int attackPercent, int pairPercent) {
        martingale       = true;
        martingaleAttack = Math.max(martingaleAttack, attackPercent);
        martingalePair   = Math.max(martingalePair, pairPercent);
    }
    /** @return {@code true} si une Martingale a été jouée ce tour. */
    public boolean isMartingale() { return martingale; }
    /** @return l'attaque de la Martingale par tirage raté, en %. */
    public int getMartingaleAttack() { return martingaleAttack; }
    /** @return la chance de paire de la Martingale par tirage raté, en %. */
    public int getMartingalePair() { return martingalePair; }

    /** @return le coût d'un tirage dans ce combat, en gains (voir {@link SpinEconomy}). */
    public int getSpinCost() { return spinCost; }

    /** @return le joueur du combat en cours. */
    public Player getPlayer() { return player; }
    /** @return l'ennemi du combat en cours. */
    public Enemy  getEnemy()  { return enemy; }

    /** @return le bonus d'attaque plat accumulé ce tour. */
    public long  getAttackBonus()    { return attackBonus; }
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

    /** Ce que change une étape du calcul : l'attaque, le bouclier, les gains, ou les trois (valeur des symboles). */
    public enum Target { ATTACK, DEFENSE, GAINS, SYMBOLS }

    /**
     * Une étape du calcul d'un symbole, pour son détail au survol du rouleau :
     * {@code label} ajoute {@code bonus} (bonus plat) ou multiplie par {@code factor}.
     */
    public record PowerStep(Target target, String label, int bonus, float factor) {
        /** @return {@code true} pour un multiplicateur, {@code false} pour un bonus plat. */
        public boolean isFactor() { return bonus == 0; }
    }

    /** @return ce qui a changé {@code target} ce tour (valeur des symboles comprise), dans l'ordre. */
    public List<PowerStep> getSteps(Target target) {
        return steps.stream().filter(step -> step.target() == target || step.target() == Target.SYMBOLS).toList();
    }

    /** Ajoute {@code bonus} au bonus d'attaque du tour. */
    public void addAttackBonus(int bonus)         { addAttackBonus(bonus, OTHER_EFFECTS); }
    /** Ajoute {@code bonus} au bonus d'attaque du tour, venu de {@code label} (détail du symbole). */
    public void addAttackBonus(int bonus, String label) {
        attackBonus = BigNumber.toLong(BigNumber.add(attackBonus, bonus));
        if (bonus != 0) steps.add(new PowerStep(Target.ATTACK, label, bonus, 1f));
    }
    /** Ajoute {@code bonus} au bonus de bouclier du tour. */
    public void addDefenseBonus(int bonus)        { addDefenseBonus(bonus, OTHER_EFFECTS); }
    /** Ajoute {@code bonus} au bonus de bouclier du tour, venu de {@code label} (détail du symbole). */
    public void addDefenseBonus(int bonus, String label) {
        defenseBonus += bonus;
        if (bonus != 0) steps.add(new PowerStep(Target.DEFENSE, label, bonus, 1f));
    }
    /** Ajoute {@code amount} au multiplicateur de gains du tour. */
    public void addGainMultiplier(float amount)   { gainMultiplier += amount; }

    /** Multiplie tous les gains du tirage par {@code factor}. */
    public void multiplyGains(float factor)       { multiplyGains(factor, OTHER_EFFECTS); }
    /** Multiplie tous les gains du tirage par {@code factor}, venu de {@code label} (détail du symbole). */
    public void multiplyGains(float factor, String label) {
        gainFactor *= factor;
        if (factor != 1f) steps.add(new PowerStep(Target.GAINS, label, 0, factor));
    }
    /** Multiplie les dégâts de chaque symbole par {@code factor}. */
    public void multiplyAttack(float factor)      { multiplyAttack(factor, OTHER_EFFECTS); }
    /** Multiplie les dégâts de chaque symbole par {@code factor}, venu de {@code label} (détail du symbole). */
    public void multiplyAttack(float factor, String label) {
        attackFactor *= factor;
        if (factor != 1f) steps.add(new PowerStep(Target.ATTACK, label, 0, factor));
    }
    /** Multiplie le bouclier de chaque symbole par {@code factor}. */
    public void multiplyDefense(float factor)     { multiplyDefense(factor, OTHER_EFFECTS); }
    /** Multiplie le bouclier de chaque symbole par {@code factor}, venu de {@code label} (détail du symbole). */
    public void multiplyDefense(float factor, String label) {
        defenseFactor *= factor;
        if (factor != 1f) steps.add(new PowerStep(Target.DEFENSE, label, 0, factor));
    }
    /** Contre-attaque (As de Carreau) : le Coffre, multiplié par {@code factor}, est infligé à l'ennemi. */
    public void addCounterAttack(int factor)      { counterAttack += factor; }
    /** Guillotine : ajoute {@code percent} % des PV restants de l'ennemi, infligés d'un coup après le tirage. */
    public void addExecution(int percent)         { executionPercent += percent; }
    /** Multiplie la valeur de chaque symbole (dégâts, bouclier, gains) par {@code factor}. */
    public void multiplySymbolPower(int factor)   { multiplySymbolPower(factor, OTHER_EFFECTS); }
    /** Comme {@link #multiplySymbolPower(int)}, venu de {@code label} (détail du symbole). */
    public void multiplySymbolPower(int factor, String label) {
        symbolPower *= factor;
        if (factor != 1) steps.add(new PowerStep(Target.SYMBOLS, label, 0, factor));
    }
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
