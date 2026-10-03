package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.enemy.EnemyCards;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    /** Retire {@code damage} points de vie, sans descendre sous 0. */
    public void takeDamage(int damage) {
        int lost = Math.min(hp, Math.max(0, damage));
        hp -= lost;
        damageTaken += lost;
    }

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

    /**
     * Début de son tour : ses Épines du tour précédent tombent.
     *
     * @return les dégâts à renvoyer au joueur (part des dégâts encaissés depuis la fin de son dernier tour)
     */
    public int collectThorns() {
        int thorns = Math.round(damageTaken * thornsPercent / 100f);
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
