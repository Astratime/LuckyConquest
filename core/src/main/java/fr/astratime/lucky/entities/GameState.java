package fr.astratime.lucky.entities;

public class GameState {

    // -------------------------------------------------------------------------
    // Participants au combat
    // -------------------------------------------------------------------------

    private final Player player = new Player("Joueur", 100);
    private final Enemy  enemy  = new Enemy("Ennemi", 100);

    // -------------------------------------------------------------------------
    // État du tour
    // -------------------------------------------------------------------------

    private final TurnContext turnContext = new TurnContext();

    // -------------------------------------------------------------------------
    // Progression
    // -------------------------------------------------------------------------

    private int   score      = 0;
    private int   turnNumber = 1;
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
    // Transitions
    // -------------------------------------------------------------------------

    public void nextTurn() {
        turnNumber++;
        turnContext.reset();
        player.getSlotMachine().resetBoosts();
        currentPhase = Phase.DRAW_CARDS;
    }

    public void addScore(int points) { score += points; }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public Player      getPlayer()       { return player; }
    public Enemy       getEnemy()        { return enemy; }
    public TurnContext getTurnContext()   { return turnContext; }
    public int         getScore()        { return score; }
    public int         getTurnNumber()   { return turnNumber; }
    public Phase       getCurrentPhase() { return currentPhase; }

    public void setCurrentPhase(Phase phase) { currentPhase = phase; }
}
