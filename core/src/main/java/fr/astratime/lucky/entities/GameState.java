package fr.astratime.lucky.entities;

/**
 * Source de vérité de la partie : participants et progression globale.
 * Les gains (monnaie en combat) appartiennent au Player, pas au GameState —
 * voir Player.getGains(). GameState ne garde que ce qui dépasse un seul joueur :
 * le tour global et, à terme, l'état de la run (salle, étage, etc.).
 */
public class GameState {

    private final Player player = new Player("Joueur", 100);
    private final Enemy  enemy  = new Enemy("Ennemi", 100);

    private int turnNumber = 1;

    public void nextTurn() { turnNumber++; }

    public Player getPlayer()     { return player; }
    public Enemy  getEnemy()      { return enemy; }
    public int    getTurnNumber() { return turnNumber; }
}
