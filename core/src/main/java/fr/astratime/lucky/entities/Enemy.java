package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.enemy.EnemyCards;

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
    /** Cartes que l'ennemi joue parmi celles piochées. */
    public static final int PLAYS_PER_TURN = 3;
    /** Défense de base, qui réduit les dégâts reçus des attaques du joueur. */
    public static final int BASE_DEFENSE   = 30;

    private final String      name;
    private final int         maxHp;
    private       int         hp;
    private final DiscardPile discardPile = new DiscardPile();
    private final Deck        deck;
    /** Défense des Boucliers de son dernier tour : elle vaut pendant le tour suivant du joueur. */
    private       int         shieldDefense;

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
        this.name  = name;
        this.maxHp = maxHp;
        this.hp    = maxHp;
        this.deck  = new Deck(cards, discardPile);
    }

    /** Retire {@code damage} points de vie, sans descendre sous 0. */
    public void takeDamage(int damage) {
        hp = Math.max(0, hp - damage);
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

    /** Ajoute {@code defense} à la défense des Boucliers de ce tour. */
    public void addShieldDefense(int defense) { shieldDefense += defense; }

    /** Retire la défense des Boucliers du tour précédent (au début d'un nouveau tour de l'ennemi). */
    public void resetShieldDefense() { shieldDefense = 0; }

    /** Pioche {@code count} cartes (la défausse est remélangée si le deck s'épuise). */
    public List<Card> draw(int count) { return deck.draw(count); }

    /** Envoie {@code cards} dans la défausse (fin du tour de l'ennemi). */
    public void discard(List<Card> cards) { discardPile.addAll(cards); }

    /** @return le nom affiché de l'ennemi. */
    public String getName()        { return name; }
    /** @return les points de vie actuels. */
    public int    getHp()          { return hp; }
    /** @return les points de vie maximum. */
    public int    getMaxHp()       { return maxHp; }
    /** @return la proportion de vie restante, entre 0 et 1. */
    public float  getHpRatio()     { return (float) hp / maxHp; }
    /** @return la défense de l'ennemi (base et Boucliers), qui réduit les dégâts des attaques du joueur. */
    public int    getDefense()     { return BASE_DEFENSE + shieldDefense; }
    /** @return la défense ajoutée par les Boucliers de son dernier tour. */
    public int    getShieldDefense() { return shieldDefense; }
    /** @return les cartes restantes dans le deck de l'ennemi. */
    public List<Card> getDeckCards()    { return Collections.unmodifiableList(new ArrayList<>(deck.getCards())); }
    /** @return les cartes de la défausse de l'ennemi. */
    public List<Card> getDiscardCards() { return discardPile.getCards(); }
}
