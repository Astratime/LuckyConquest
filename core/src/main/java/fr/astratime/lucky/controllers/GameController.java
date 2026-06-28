package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.effects.Effect;

import java.util.ArrayList;
import java.util.List;

/**
 * Gère la progression globale de la partie : possède le GameState,
 * orchestre les tours via TurnEngine, et expose à GameScreen uniquement
 * les opérations nécessaires (draw, playCard, spin).
 *
 * Accumule les effets des cartes jouées entre le début de la phase 1
 * et le lancement de la machine (fin de phase 1 / début phase 2).
 */
public class GameController {

    private static final int DEFAULT_DRAW_COUNT = 3;

    private final GameState  gameState  = new GameState();
    private final TurnEngine turnEngine = new TurnEngine();

    /** Effets accumulés depuis le début du tour, appliqués au moment du spin. */
    private final List<Effect> pendingEffects = new ArrayList<>();

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
