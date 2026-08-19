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

    /** Monnaie gagnée en combat. Servira à acheter des bonus en combat. */
    private int gains = 0;

    /** Bouclier accumulé ce tour par les DefenseAction. */
    private int shield = 0;

    /** Pourcentage de dégâts ennemis renvoyés ce tour. */
    private int reflectPercent = 0;

    public Player(String name, int maxHp, List<Card> cards) {
        this.name  = name;
        this.maxHp = maxHp;
        this.hp    = maxHp;
        this.deck  = new Deck(cards, discardPile);
    }

    public void takeDamage(int damage) { hp = Math.max(0, hp - damage); }
    public void heal(int amount)       { hp = Math.min(maxHp, hp + amount); }
    public boolean isDefeated()        { return hp <= 0; }

    public float getHpRatio() { return (float) hp / maxHp; }

    public void addGains(int amount) { gains = Math.max(0, gains + amount); }

    public int consumeGainsPercent(float percent) {
        int amount = Math.round(gains * percent);
        gains -= amount;
        return amount;
    }

    public void addShield(int amount) { shield += amount; }

    public void setReflectPercent(int percent) { reflectPercent = Math.max(reflectPercent, percent); }

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
    public int         getGains()                   { return gains; }
    public int         getShield()                  { return shield; }
    public int         getReflectPercent()          { return reflectPercent; }
}
