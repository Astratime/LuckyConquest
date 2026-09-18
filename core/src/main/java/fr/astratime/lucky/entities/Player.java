package fr.astratime.lucky.entities;

import java.util.List;

/**
 * Le joueur : points de vie, deck/défausse/main courante, machine à sous
 * personnelle, gains accumulés et modificateurs de combat du tour
 * (bouclier, renvoi de dégâts).
 */
public class Player {

    private final String      name;
    private final DiscardPile discardPile = new DiscardPile();
    private final Deck        deck;
    private final SlotMachine slotMachine = new SlotMachine();
    private       List<Card>  currentHand = List.of();
    private       int         hp;
    private final int         maxHp;

    /** Monnaie gagnée en combat. Servira à acheter des bonus en combat. */
    private int gains = 0;

    /** Bouclier accumulé ce tour par les DefenseAction. */
    private int shield = 0;

    /** Pourcentage de dégâts ennemis renvoyés ce tour. */
    private int reflectPercent = 0;

    /**
     * @param name     nom affiché du joueur
     * @param maxHp    points de vie maximum (le joueur démarre à pleine vie)
     * @param cards    cartes composant le deck initial (mélangées à la construction du Deck)
     */
    public Player(String name, int maxHp, List<Card> cards) {
        this.name  = name;
        this.maxHp = maxHp;
        this.hp    = maxHp;
        this.deck  = new Deck(cards, discardPile);
    }

    /**
     * Inflige des dégâts au joueur : le bouclier accumulé absorbe les dégâts
     * en priorité, seul l'excédent (s'il y en a) retire des points de vie,
     * sans jamais descendre sous 0.
     *
     * @param damage dégâts bruts, avant absorption par le bouclier
     * @return les dégâts effectivement retirés des points de vie (après bouclier)
     */
    public int takeDamage(int damage) {
        int absorbed  = Math.min(shield, damage);
        shield -= absorbed;
        int remaining = damage - absorbed;
        int actualLoss = Math.min(hp, remaining);
        hp -= actualLoss;
        return actualLoss;
    }
    /** Rend {@code amount} points de vie, sans dépasser le maximum. */
    public void heal(int amount)       { hp = Math.min(maxHp, hp + amount); }
    /** @return {@code true} si le joueur n'a plus de points de vie. */
    public boolean isDefeated()        { return hp <= 0; }

    /** @return la proportion de vie restante, entre 0 et 1 (utilisé par les effets conditionnels). */
    public float getHpRatio() { return (float) hp / maxHp; }

    /** Ajoute {@code amount} aux gains accumulés, sans jamais passer sous 0. */
    public void addGains(int amount) { gains = Math.max(0, gains + amount); }

    /**
     * Consomme un pourcentage des gains actuels (ex : coût d'un As de Trèfle).
     *
     * @param percent pourcentage à consommer (0.3f = 30%)
     * @return le montant effectivement consommé
     */
    public int consumeGainsPercent(float percent) {
        int amount = Math.round(gains * percent);
        gains -= amount;
        return amount;
    }

    /** Ajoute {@code amount} au bouclier accumulé ce tour. */
    public void addShield(int amount) { shield += amount; }

    /** Fixe le pourcentage de renvoi de dégâts, sans jamais le réduire (garde le meilleur bonus du tour). */
    public void setReflectPercent(int percent) { reflectPercent = Math.max(reflectPercent, percent); }

    /** Réinitialise le bouclier et le renvoi de dégâts en fin de tour. */
    public void resetTurnDefenses() {
        shield         = 0;
        reflectPercent = 0;
    }

    /** @return le nom affiché du joueur. */
    public String      getName()                   { return name; }
    /** @return les points de vie actuels. */
    public int         getHp()                     { return hp; }
    /** @return les points de vie maximum. */
    public int         getMaxHp()                  { return maxHp; }
    /** @return la défausse du joueur. */
    public DiscardPile getDiscardPile()             { return discardPile; }
    /** @return le deck du joueur. */
    public Deck        getDeck()                    { return deck; }
    /** @return la machine à sous personnelle du joueur. */
    public SlotMachine getSlotMachine()             { return slotMachine; }
    /** @return la main actuellement en jeu. */
    public List<Card>  getCurrentHand()             { return currentHand; }
    /** Remplace la main courante (après une pioche). */
    public void        setCurrentHand(List<Card> h) { currentHand = h; }
    /** @return les gains accumulés. */
    public int         getGains()                   { return gains; }
    /** @return le bouclier accumulé ce tour. */
    public int         getShield()                  { return shield; }
    /** @return le pourcentage de renvoi de dégâts accumulé ce tour. */
    public int         getReflectPercent()          { return reflectPercent; }
}
