package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.enemy.EnemyCards;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * L'ennemi : points de vie, défense, et son propre jeu de cartes (jeu sombre,
 * voir {@link EnemyCards}). À son tour, il pioche {@link #HAND_SIZE} cartes,
 * en joue {@link #PLAYS_PER_TURN} pour renforcer les symboles de sa machine à
 * sous, puis toutes rejoignent sa défausse (voir
 * {@link fr.astratime.lucky.controllers.EnemyTurnResolver}).
 */
public class Enemy {

    /** Cartes piochées par l'ennemi à chaque tour. */
    public static final int HAND_SIZE      = 8;
    /** Cartes que le croupier joue parmi celles piochées (voir {@link EnemyKind#getPlaysPerTurn()}). */
    public static final int PLAYS_PER_TURN = 3;
    /** Défense de base du croupier, reformée à chacun de ses tours : elle absorbe les dégâts des attaques du joueur. */
    public static final int BASE_DEFENSE   = 30;

    private final EnemyKind   kind;
    private final String      name;
    private final int         maxHp;
    private       int         hp;
    private final DiscardPile discardPile = new DiscardPile();
    private final Deck        deck;
    /**
     * Défense restante : base et Boucliers de son dernier tour. Elle absorbe
     * les attaques du joueur et s'use à chaque coup, jusqu'à son tour suivant.
     */
    private       int         defense;
    /** Bonus d'attaque de la Rage, gardé jusqu'à la fin du combat. */
    private       int         rage;
    /** Part des dégâts du joueur renvoyée par ses Épines (en %), jusqu'à son tour suivant. */
    private       int         thornsPercent;
    /** Dégâts encaissés depuis le début de son dernier tour (pour ses Épines). */
    private       int         damageTaken;
    /** Attaque ajoutée à son prochain coup par les gains volés (Intérêts). */
    private       int         interest;
    /** Compte à rebours de son Sablier, de 0 à {@link EnemySymbol#HOURGLASS_MAX}. */
    private       int         hourglass;
    /** Sa mise (Tapis) : ses attaques sont multipliées d'autant, jusqu'à ce qu'il soit touché. */
    private       int         stake = 1;
    /** Tapis posé à son dernier tour : sa mise doublera à son tour suivant, s'il n'est pas touché d'ici là. */
    private       boolean     allIn;
    /** Sa phase (1, puis 2 pour l'Éclat Originel blessé). */
    private       int         phase = 1;
    /** Bonus d'attaque de ses Enclumes, gardé jusqu'à la fin du combat, sans limite. */
    private       int         anvil;

    /**
     * Ennemi avec le deck de départ du jeu sombre.
     *
     * @param name  nom affiché de l'ennemi
     * @param maxHp points de vie maximum (l'ennemi démarre à pleine vie)
     */
    public Enemy(String name, int maxHp) {
        this(name, maxHp, EnemyCards.starterDeck());
    }

    /** @param cards cartes composant le deck de l'ennemi (mélangées) */
    public Enemy(String name, int maxHp, List<Card> cards) {
        this(EnemyKind.CROUPIER, name, maxHp, cards);
    }

    /** Ennemi {@code kind} : son nom, ses PV, sa défense, ses rouleaux et son deck. */
    public Enemy(EnemyKind kind) {
        this(kind, kind.getDisplayName(), kind.getMaxHp(), kind.createDeck());
    }

    private Enemy(EnemyKind kind, String name, int maxHp, List<Card> cards) {
        this.kind    = kind;
        this.name    = name;
        this.maxHp   = maxHp;
        this.hp      = maxHp;
        this.defense = kind.getBaseDefense();
        this.deck    = new Deck(cards, discardPile);
    }

    /**
     * Retire {@code damage} points de vie, sans descendre sous 0. Une peau d'or
     * (Golem d'or) en encaisse la moitié tant qu'il a plus de la moitié de ses PV.
     * Un coup qui touche fait retomber sa mise (Tapis) ; un gros coup fait reculer son Sablier.
     *
     * @return les points de vie réellement retirés
     */
    public int takeDamage(int damage) {
        int lost = Math.min(hp, Math.max(0, skinned(damage)));
        hp -= lost;
        damageTaken += lost;
        if (lost <= 0) return 0;
        stake = 1;
        allIn = false;
        if (hourglass > 0 && lost * 1000L >= (long) maxHp * EnemySymbol.HOURGLASS_HIT_PER_MILLE) hourglass--;
        return lost;
    }

    /** @return les dégâts qui le touchent vraiment : la moitié, tant que sa peau d'or tient (plus de la moitié de ses PV). */
    public int skinned(int damage) {
        return hasGoldSkin() ? damage / 2 : damage;
    }

    /** @return {@code true} si sa peau d'or encaisse la moitié des coups en ce moment. */
    public boolean hasGoldSkin() { return kind.hasGoldSkin() && hp * 2L > maxHp; }

    /**
     * @return les cartes qu'il joue ce tour : celles de son type, ou une par bras
     *         qui lui reste (le Kraken perd un bras à chaque huitième de ses PV)
     */
    public int getPlaysPerTurn() {
        if (!kind.hasArms()) return kind.getPlaysPerTurn();
        return Math.max(1, (int) Math.ceil(EnemyKind.KRAKEN_ARMS * (double) hp / maxHp));
    }

    /** Ajoute {@code amount} à son Enclume (sans limite). */
    public void addAnvil(int amount) { anvil += amount; }

    /** @return l'attaque ajoutée par ses Enclumes, jusqu'à la fin du combat. */
    public int getAnvil() { return anvil; }

    /**
     * Rend {@code amount} points de vie, sans dépasser le maximum.
     *
     * @return les points de vie réellement rendus
     */
    public int heal(int amount) {
        int healed = Math.min(maxHp, hp + amount) - hp;
        hp += healed;
        return healed;
    }

    /** @return {@code true} si l'ennemi n'a plus de points de vie. */
    public boolean isDefeated() { return hp <= 0; }

    /** Ajoute {@code amount} à la défense (Bouclier de son tour). */
    public void addShieldDefense(int amount) { defense += amount; }

    /** Reforme la défense de base, sans les Boucliers du tour précédent (au début d'un nouveau tour de l'ennemi). */
    public void resetDefense() { defense = kind.getBaseDefense(); }

    /** Pot-de-vin : sa défense tombe à 0 jusqu'à son tour. @return la défense retirée */
    public int bribe() {
        int removed = defense;
        defense = 0;
        return removed;
    }

    /**
     * Début de son tour : ses Épines du tour précédent tombent.
     *
     * @return les dégâts à renvoyer au joueur (part des dégâts encaissés depuis la fin de son dernier tour)
     */
    public int collectThorns() {
        int thorns = Math.min(kind.thornsMax(), Math.round(damageTaken * thornsPercent / 100f));
        thornsPercent = 0;
        return thorns;
    }

    /** Fin de son tour : ses Épines ne compteront que les coups encaissés à partir de maintenant. */
    public void resetDamageTaken() { damageTaken = 0; }

    /** Ajoute {@code percent} à ses Épines, jusqu'à son tour suivant. */
    public void addThorns(int percent) { thornsPercent += percent; }

    /** Ajoute {@code amount} à sa Rage (plafonnée à {@link EnemySymbol#RAGE_MAX}). @return la Rage ajoutée */
    public int addRage(int amount) {
        int added = Math.min(amount, EnemySymbol.RAGE_MAX - rage);
        rage += Math.max(0, added);
        return Math.max(0, added);
    }

    /** Ses Intérêts : {@code stolen} gains volés renforcent son prochain coup. @return l'attaque ajoutée */
    public int addInterest(int stolen) {
        int added = Math.min(EnemySymbol.INTEREST_MAX - interest, stolen / EnemySymbol.INTEREST_PER_ATTACK);
        interest += Math.max(0, added);
        return Math.max(0, added);
    }

    /** Son coup part : l'attaque de ses Intérêts est dépensée. @return l'attaque à ajouter à ce coup */
    public int spendInterest() {
        int spent = interest;
        interest = 0;
        return spent;
    }

    /**
     * Son Sablier avance d'un cran.
     *
     * @return {@code true} s'il arrive au bout : il explose et repart de zéro
     */
    public boolean tickHourglass() {
        hourglass++;
        if (hourglass < EnemySymbol.HOURGLASS_MAX) return false;
        hourglass = 0;
        return true;
    }

    /** Tapis : sa mise doublera à son tour suivant, s'il n'est pas touché d'ici là. */
    public void goAllIn() { allIn = true; }

    /**
     * Début de son tour : son Tapis du tour précédent, s'il tient encore, double
     * sa mise (plafonnée à {@link EnemySymbol#ALL_IN_MAX}).
     *
     * @return {@code true} si sa mise vient de doubler
     */
    public boolean raiseStake() {
        if (!allIn) return false;
        allIn = false;
        int before = stake;
        stake = Math.min(EnemySymbol.ALL_IN_MAX, stake * 2);
        return stake > before;
    }

    /**
     * Début de son tour : l'Éclat Originel passe à sa deuxième phase sous
     * {@link EnemyKind#PHASE_TWO_RATIO} de vie.
     *
     * @return {@code true} s'il vient de changer de phase
     */
    public boolean enterPhaseTwo() {
        if (phase >= 2 || !kind.hasPhaseTwo() || getHpRatio() >= EnemyKind.PHASE_TWO_RATIO) return false;
        phase = 2;
        return true;
    }

    /**
     * Une attaque du joueur frappe la défense : elle absorbe tout ce qu'elle
     * peut et s'use d'autant.
     *
     * @param damage dégâts bruts de l'attaque
     * @return les dégâts absorbés par la défense (le reste touche l'ennemi)
     */
    public int absorb(int damage) {
        int absorbed = Math.min(defense, Math.max(0, damage));
        defense -= absorbed;
        return absorbed;
    }

    /** Pioche {@code count} cartes (la défausse est remélangée si le deck s'épuise). */
    public List<Card> draw(int count) { return deck.draw(count); }

    /** Envoie {@code cards} dans la défausse (fin du tour de l'ennemi). */
    public void discard(List<Card> cards) { discardPile.addAll(cards); }

    /** @return le type d'ennemi : ses rouleaux, sa façon de jouer, son portrait. */
    public EnemyKind getKind()     { return kind; }
    /** @return le bonus d'attaque de sa Rage. */
    public int    getRage()        { return rage; }
    /** @return l'attaque que ses Intérêts ajouteront à son prochain coup. */
    public int    getInterest()    { return interest; }
    /** @return le compte à rebours de son Sablier. */
    public int    getHourglass()   { return hourglass; }
    /** @return {@code true} si son Tapis doublera sa mise à son prochain tour. */
    public boolean isAllIn()       { return allIn; }
    /** @return sa mise : ses attaques sont multipliées d'autant (Tapis). */
    public int    getStake()       { return stake; }
    /** @return sa phase (1, ou 2 pour l'Éclat Originel blessé). */
    public int    getPhase()       { return phase; }
    /** @return les symboles de ses rouleaux et leur poids, dans sa phase. */
    public Map<EnemySymbol, Integer> getWeights() { return kind.getWeights(phase); }
    /** @return les symboles qui peuvent sortir sur ses rouleaux, dans sa phase. */
    public List<EnemySymbol> getSymbols() { return kind.getSymbols(phase); }
    /** @return la part des dégâts du joueur que renverront ses Épines, en %. */
    public int    getThornsPercent() { return thornsPercent; }
    /** @return sa défense de base, reformée à chacun de ses tours. */
    public int    getBaseDefense() { return kind.getBaseDefense(); }
    /** @return le nom affiché de l'ennemi. */
    public String getName()        { return name; }
    /** @return les points de vie actuels. */
    public int    getHp()          { return hp; }
    /** @return les points de vie maximum. */
    public int    getMaxHp()       { return maxHp; }
    /** @return la proportion de vie restante, entre 0 et 1. */
    public float  getHpRatio()     { return (float) hp / maxHp; }
    /** @return la défense restante de l'ennemi (base et Boucliers, moins ce que les attaques ont déjà usé). */
    public int    getDefense()     { return defense; }
    /** @return les cartes restantes dans le deck de l'ennemi. */
    public List<Card> getDeckCards()    { return Collections.unmodifiableList(new ArrayList<>(deck.getCards())); }
    /** @return les cartes de la défausse de l'ennemi. */
    public List<Card> getDiscardCards() { return discardPile.getCards(); }
}
