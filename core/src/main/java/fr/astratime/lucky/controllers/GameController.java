package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.effects.Effect;
import fr.astratime.lucky.loaders.CardLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Gère la progression globale de la partie : possède le GameState,
 * orchestre les tours via TurnEngine, et expose à GameScreen uniquement
 * les opérations nécessaires (drawCards, playCard, spin).
 *
 * C'est ici qu'est appelé CardLoader — après initialisation de libGDX,
 * ce qui garantit que Gdx.files est disponible.
 */
public class GameController {

    private static final int DEFAULT_DRAW_COUNT = 6;

    private final GameState  gameState;
    private final TurnEngine turnEngine = new TurnEngine();

    /** Effets accumulés depuis le début du tour, appliqués au moment du spin. */
    private final List<Effect> pendingEffects = new ArrayList<>();

    public GameController() {
        List<Card> cards = CardLoader.loadAll();
        this.gameState = new GameState(cards);
    }

    // -------------------------------------------------------------------------
    // Actions du joueur
    // -------------------------------------------------------------------------

    /** Phase 1 : pioche les cartes et les retourne pour affichage. */
    public List<Card> drawCards() {
        Player player = gameState.getPlayer();
        player.getDiscardPile().addAll(player.getCurrentHand());
        List<Card> hand = player.getDeck().draw(DEFAULT_DRAW_COUNT);
        player.setCurrentHand(hand);
        return hand;
    }

    /**
     * Le joueur joue une carte : ses effets sont mis en attente.
     * Ils seront appliqués au TurnContext lors du spin.
     */
    public void playCard(Card card) {
        pendingEffects.addAll(card.getEffects());
    }

    /**
     * Fin de phase 1 / Phase 2 : applique les effets en attente,
     * lance la machine à sous, résout le combat et retourne le TurnResult.
     */
    public TurnResult spin() {
        TurnResult result = turnEngine.playTurn(gameState, pendingEffects);
        pendingEffects.clear();
        return result;
    }

    // -------------------------------------------------------------------------
    // Lecture de l'état (pour GameScreen)
    // -------------------------------------------------------------------------

    public GameState getGameState() { return gameState; }
}
