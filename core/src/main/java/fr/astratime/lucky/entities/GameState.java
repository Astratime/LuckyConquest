package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.enemy.EnemyKind;

import java.util.List;

/**
 * Source de vérité de la partie.
 * Reçoit la liste de cartes chargée par CardLoader via GameController,
 * pour ne pas introduire de dépendance libGDX dans les entités.
 */
public class GameState {

    private final Player player;
    private final Enemy  enemy;
    private       int    turnNumber = 1;

    /**
     * Crée une nouvelle partie : le joueur démarre à pleine vie avec le deck
     * fourni, face au croupier (voir {@link EnemyKind#CROUPIER}).
     *
     * @param playerCards cartes composant le deck initial du joueur
     */
    public GameState(List<Card> playerCards) {
        this(new Player("Joueur", 100, playerCards), new Enemy(EnemyKind.CROUPIER));
    }

    /** Combat contre {@code enemy}, avec un joueur déjà constitué (combat suivant d'une épreuve). */
    public GameState(Player player, Enemy enemy) {
        this.player = player;
        this.enemy  = enemy;
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
