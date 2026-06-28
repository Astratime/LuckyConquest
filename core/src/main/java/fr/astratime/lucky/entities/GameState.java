package fr.astratime.lucky.entities;

/**
 * Source de vérité de la partie : participants et progression globale.
 * N'inclut plus TurnContext (géré par TurnEngine pour chaque tour)
 * ni les boosts de machine (gérés par SpinContext).
 * Appartient au GameController pour toute la durée de la partie.
 */
public class GameState {

    private final Player player = new Player("Joueur", 100);
    private final Enemy  enemy  = new Enemy("Ennemi", 100);

    private int score      = 0;
    private int turnNumber = 1;

    public void addScore(int points) { score += points; }
    public void nextTurn()           { turnNumber++; }

    public Player getPlayer()     { return player; }
    public Enemy  getEnemy()      { return enemy; }
    public int    getScore()      { return score; }
    public int    getTurnNumber() { return turnNumber; }
}
