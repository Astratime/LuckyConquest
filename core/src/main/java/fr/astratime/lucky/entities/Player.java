package fr.astratime.lucky.entities;

import java.util.List;

public class Player {

    private final String      name;
    private final DiscardPile discardPile = new DiscardPile();
    private final Deck        deck;
    private final SlotMachine slotMachine = new SlotMachine();
    private       List<Card>  currentHand = List.of();
    private       int         hp;
    private final int         maxHp;

    /** Monnaie gagnée en combat (jackpot/paire, GainAction). Servira à acheter des bonus en combat. */
    private int gains = 0;

    /**
     * Bouclier accumulé ce tour par les DefenseAction.
     * Sera consommé par le tour ennemi (non implémenté pour l'instant).
     */
    private int shield = 0;

    /**
     * Pourcentage de dégâts ennemis renvoyés ce tour (DefenseAction + cartes Carreau).
     * Sera consommé par le tour ennemi (non implémenté pour l'instant).
     */
    private int reflectPercent = 0;

    public Player(String name, int maxHp) {
        this.name  = name;
        this.maxHp = maxHp;
        this.hp    = maxHp;
        this.deck  = new Deck(discardPile);
    }

    public void takeDamage(int damage) { hp = Math.max(0, hp - damage); }
    public void heal(int amount)       { hp = Math.min(maxHp, hp + amount); }
    public boolean isDefeated()        { return hp <= 0; }

    /** Ratio de vie actuel (0.0 à 1.0), utilisé par les effets conditionnels (ex: As de Carreau). */
    public float getHpRatio() { return (float) hp / maxHp; }

    public void addGains(int amount) { gains = Math.max(0, gains + amount); }

    /** Consomme un pourcentage des gains actuels et retourne le montant consommé. */
    public int consumeGainsPercent(float percent) {
        int amount = Math.round(gains * percent);
        gains -= amount;
        return amount;
    }

    public void addShield(int amount) { shield += amount; }

    /** Le reflect retenu est toujours le plus élevé proposé ce tour. */
    public void setReflectPercent(int percent) { reflectPercent = Math.max(reflectPercent, percent); }

    /** À appeler en fin de résolution du tour ennemi (pas encore implémenté). */
    public void resetTurnDefenses() {
        shield         = 0;
        reflectPercent = 0;
    }

    public String      getName()                   { return name; }
    public int         getHp()                     { return hp; }
    public int         getMaxHp()                  { return maxHp; }
    public DiscardPile getDiscardPile()             { return discardPile; }
    public Deck        getDeck()                    { return deck; }
    public SlotMachine getSlotMachine()             { return slotMachine; }
    public List<Card>  getCurrentHand()             { return currentHand; }
    public void        setCurrentHand(List<Card> h) { currentHand = h; }
    public int         getGains()                  { return gains; }
    public int         getShield()                  { return shield; }
    public int         getReflectPercent()          { return reflectPercent; }
}
