package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.exploration.PlaceRule;

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
    /** Règle du lieu (Exploration), qui joue dans tout le combat. */
    private       PlaceRule placeRule = PlaceRule.NONE;

    /**
     * Crée une nouvelle partie : le joueur démarre à pleine vie avec le deck
     * fourni, face au croupier (voir {@link EnemyKind#CROUPIER}).
     *
     * @param playerCards cartes composant le deck initial du joueur
     */
    public GameState(List<Card> playerCards) {
        this(new Player("Joueur", Player.BASE_HP, playerCards), new Enemy(EnemyKind.CROUPIER));
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

    /** Fixe la règle du lieu du combat (voir {@link PlaceRule}). */
    public void setPlaceRule(PlaceRule rule) { placeRule = rule; }

    /** @return la règle du lieu du combat ({@link PlaceRule#NONE} hors de l'Exploration). */
    public PlaceRule getPlaceRule() { return placeRule; }

    /**
     * @return la règle du lieu qui joue en ce moment : aucune pendant une Bulle
     *         d'air du joueur
     */
    public PlaceRule getActiveRule() {
        return player.getLastingEffects().getBubbleTurns() > 0 ? PlaceRule.NONE : placeRule;
    }
}
