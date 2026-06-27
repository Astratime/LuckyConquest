package fr.astratime.lucky.entities;

import java.util.List;

/**
 * Unique source de vérité pour l'état du combat.
 * GameScreen lit et affiche cet état, mais ne le détient pas.
 */
public class GameState {

    // -------------------------------------------------------------------------
    // Entités de jeu
    // -------------------------------------------------------------------------

    private final DiscardPile  discardPile  = new DiscardPile();
    private final Deck         deck         = new Deck(discardPile);
    private final SlotMachine  slotMachine  = new SlotMachine();
    private final TurnContext  turnContext   = new TurnContext();

    // -------------------------------------------------------------------------
    // État du combat
    // -------------------------------------------------------------------------

    private List<Card> currentHand = List.of();

    private int playerHp    = 100;
    private int playerMaxHp = 100;
    private int enemyHp     = 100;
    private int enemyMaxHp  = 100;

    private int score      = 0;
    private int turnNumber = 1;

    private Phase currentPhase = Phase.DRAW_CARDS;

    // -------------------------------------------------------------------------
    // Phase du tour (suit la mécanique en 3 phases du design doc)
    // -------------------------------------------------------------------------

    public enum Phase {
        DRAW_CARDS,  // phase 1 : jouer des cartes pour booster la machine
        SPIN,        // phase 2 : lancer la machine à sous
        BONUS,       // phase 3 : jackpot — buff des résultats
        ENEMY_TURN,  // résolution : dégâts / actions du bot
        VICTORY,
        DEFEAT
    }

    // -------------------------------------------------------------------------
    // Transitions de tour
    // -------------------------------------------------------------------------

    /**
     * Appelé en fin de phase SPIN. Réinitialise le TurnContext et les boosts
     * de la machine, puis passe au tour suivant.
     */
    public void nextTurn() {
        turnNumber++;
        turnContext.reset();
        slotMachine.resetBoosts();
        currentPhase = Phase.DRAW_CARDS;
    }

    // -------------------------------------------------------------------------
    // Raccourcis score
    // -------------------------------------------------------------------------

    public void addScore(int points) { score += points; }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    public DiscardPile getDiscardPile()  { return discardPile; }
    public Deck        getDeck()         { return deck; }
    public SlotMachine getSlotMachine()  { return slotMachine; }
    public TurnContext getTurnContext()   { return turnContext; }

    public List<Card> getCurrentHand()              { return currentHand; }
    public void       setCurrentHand(List<Card> h)  { currentHand = h; }

    public int  getPlayerHp()    { return playerHp; }
    public int  getPlayerMaxHp() { return playerMaxHp; }
    public int  getEnemyHp()     { return enemyHp; }
    public int  getEnemyMaxHp()  { return enemyMaxHp; }
    public int  getScore()       { return score; }
    public int  getTurnNumber()  { return turnNumber; }
    public Phase getCurrentPhase() { return currentPhase; }

    public void setPlayerHp(int hp)    { playerHp    = Math.max(0, hp); }
    public void setEnemyHp(int hp)     { enemyHp     = Math.max(0, hp); }
    public void setCurrentPhase(Phase phase) { currentPhase = phase; }
}
