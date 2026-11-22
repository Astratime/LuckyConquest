package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.exploration.PlaceRule;
import fr.astratime.lucky.i18n.Lang;

import java.util.List;
import java.util.Random;

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
    /** Présage (Temple du Destin) : le symbole annoncé pour le tirage de ce tour, ou {@code null}. */
    private       Symbol    omen;

    /**
     * Crée une nouvelle partie : le joueur démarre à pleine vie avec le deck
     * fourni, face au croupier (voir {@link EnemyKind#CROUPIER}).
     *
     * @param playerCards cartes composant le deck initial du joueur
     */
    public GameState(List<Card> playerCards) {
        this(new Player(Lang.t("Joueur"), Player.BASE_HP, playerCards), new Enemy(EnemyKind.CROUPIER));
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
     * Présage : le temple annonce, pour le tirage de ce tour, le symbole d'un rouleau de la machine du joueur pris au
     * hasard (un symbole présent sur plusieurs rouleaux a plus de chances d'être annoncé). Rien sans la règle.
     */
    public void announceOmen(Random random) {
        omen = null;
        if (!getActiveRule().hasOmen()) return;
        List<Symbol> reels = player.getSlotMachine().getReels();
        if (!reels.isEmpty()) omen = reels.get(random.nextInt(reels.size()));
    }

    /** @return le symbole annoncé par le Présage pour le tirage de ce tour, ou {@code null}. */
    public Symbol getOmen() { return omen; }

    /** Le tirage a eu lieu : le Présage ne vaut plus. @return le symbole qui était annoncé, ou {@code null} */
    public Symbol takeOmen() { Symbol announced = omen; omen = null; return announced; }

    /**
     * @return la règle du lieu qui joue en ce moment : aucune pendant une Bulle
     *         d'air du joueur
     */
    public PlaceRule getActiveRule() {
        return player.getLastingEffects().getBubbleTurns() > 0 ? PlaceRule.NONE : placeRule;
    }
}
