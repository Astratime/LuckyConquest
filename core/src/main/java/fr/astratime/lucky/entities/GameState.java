package fr.astratime.lucky.entities;

import java.util.List;

/**
 * Source de vérité de la partie.
 * Reçoit la liste de cartes chargée par CardLoader via GameController,
 * pour ne pas introduire de dépendance libGDX dans les entités.
 */
public class GameState {

    private final Player player;
    private final Enemy  enemy  = new Enemy("Ennemi", 100);
    private       int    turnNumber = 1;

    public GameState(List<Card> playerCards) {
        this.player = new Player("Joueur", 100, playerCards);
    }

    public void nextTurn() { turnNumber++; }

    public Player getPlayer()     { return player; }
    public Enemy  getEnemy()      { return enemy; }
    public int    getTurnNumber() { return turnNumber; }
}
