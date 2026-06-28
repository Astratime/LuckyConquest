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

    public Player(String name, int maxHp) {
        this.name  = name;
        this.maxHp = maxHp;
        this.hp    = maxHp;
        this.deck  = new Deck(discardPile);
    }

    public void takeDamage(int damage) { hp = Math.max(0, hp - damage); }
    public void heal(int amount)       { hp = Math.min(maxHp, hp + amount); }
    public boolean isDefeated()        { return hp <= 0; }

    public String      getName()                   { return name; }
    public int         getHp()                     { return hp; }
    public int         getMaxHp()                  { return maxHp; }
    public DiscardPile getDiscardPile()             { return discardPile; }
    public Deck        getDeck()                    { return deck; }
    public SlotMachine getSlotMachine()             { return slotMachine; }
    public List<Card>  getCurrentHand()             { return currentHand; }
    public void        setCurrentHand(List<Card> h) { currentHand = h; }
}
