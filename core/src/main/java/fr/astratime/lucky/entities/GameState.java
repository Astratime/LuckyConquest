package fr.astratime.lucky.entities;

import java.util.List;

public class GameState {

    // -------------------------------------------------------------------------
    // Entités de jeu
    // -------------------------------------------------------------------------

    private final DiscardPile discardPile = new DiscardPile();
    private final Deck        deck        = new Deck(discardPile);
    private final SlotMachine slotMachine = new SlotMachine();
    private final TurnContext turnContext  = new TurnContext();
    private final Enemy       enemy       = new Enemy("Ennemi", 100);

    // -------------------------------------------------------------------------
    // État du combat
    // -------------------------------------------------------------------------

    private List<Card> currentHand = List.of();

    private int playerHp    = 100;
    private int playerMaxHp = 100;

    private int score      = 0;
    private int turnNumber = 1;

    private Phase currentPhase = Phase.DRAW_CARDS;

    // -------------------------------------------------------------------------
    // Phase du tour
    // -------------------------------------------------------------------------

    public enum Phase {
        DRAW_CARDS,
        SPIN,
        BONUS,
        ENEMY_TURN,
        VICTORY,
        DEFEAT
    }

    // -------------------------------------------------------------------------
    // Transitions de tour
    // -------------------------------------------------------------------------

    public void nextTurn() {
        turnNumber++;
        turnContext.reset();
        slotMachine.resetBoosts();
        currentPhase = Phase.DRAW_CARDS;
    }

    // -------------------------------------------------------------------------
    // Raccourcis
    // -------------------------------------------------------------------------

    public void addScore(int points) { score += points; }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    public DiscardPile getDiscardPile()             { return discardPile; }
    public Deck        getDeck()                    { return deck; }
    public SlotMachine getSlotMachine()             { return slotMachine; }
    public TurnContext getTurnContext()              { return turnContext; }
    public Enemy       getEnemy()                   { return enemy; }

    public List<Card>  getCurrentHand()             { return currentHand; }
    public void        setCurrentHand(List<Card> h) { currentHand = h; }

    public int   getPlayerHp()     { return playerHp; }
    public int   getPlayerMaxHp()  { return playerMaxHp; }
    public int   getScore()        { return score; }
    public int   getTurnNumber()   { return turnNumber; }
    public Phase getCurrentPhase() { return currentPhase; }

    public void setPlayerHp(int hp)              { playerHp     = Math.max(0, hp); }
    public void setCurrentPhase(Phase phase)     { currentPhase = phase; }
}
