package fr.astratime.lucky.entities;

import java.util.List;

/**
 * Source de vérité de la partie.
 * Reçoit la liste de cartes chargée par CardLoader via GameController,
 * pour ne pas introduire de dépendance libGDX dans les entités.
 */
public class GameState {

    private final Player player;
    private final Enemy  enemy  = new Enemy("Ennemi", 5000);
    private       int    turnNumber = 1;

    /**
     * Crée une nouvelle partie : le joueur démarre à pleine vie avec le deck
     * fourni, l'ennemi est généré avec des statistiques fixes.
     *
     * @param playerCards cartes composant le deck initial du joueur
     */
    public GameState(List<Card> playerCards) {
        this.player = new Player("Joueur", 100, playerCards);
    }

    /** Incrémente le numéro de tour, appelé à la fin de chaque tour résolu. */
    public void nextTurn() { turnNumber++; }

    /** @return le joueur de la partie en cours. */
    public Player getPlayer()     { return player; }
    /** @return l'ennemi de la partie en cours. */
    public Enemy  getEnemy()      { return enemy; }
    /** @return le numéro du tour en cours (démarre à 1). */
    public int    getTurnNumber() { return turnNumber; }
}
