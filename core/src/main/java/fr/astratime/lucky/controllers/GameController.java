package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.CardPlayResult;
import fr.astratime.lucky.entities.DrawResult;
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.context.PlayContext;
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

    /** Nombre de cartes piochées à chaque début de tour par {@link #drawCards()}. */
    public static final int DEFAULT_DRAW_COUNT = 6;

    private       GameState  gameState;
    private final TurnEngine turnEngine = new TurnEngine();

    /** Effets accumulés depuis le début du tour, appliqués au moment du spin. */
    private final List<Effect> pendingEffects = new ArrayList<>();

    /** Charge le deck de départ depuis les JSON et crée une nouvelle partie (joueur + ennemi au maximum de leurs PV). */
    public GameController() {
        this.gameState = new GameState(CardLoader.loadStarterDeck());
    }

    /**
     * Recommence un combat : recrée entièrement le GameState (joueur et
     * ennemi au maximum de leurs points de vie, bonus/malus effacés) et
     * vide les effets en attente du tour précédent.
     */
    public void restart() {
        this.gameState = new GameState(CardLoader.loadStarterDeck());
        pendingEffects.clear();
    }

    // -------------------------------------------------------------------------
    // Actions du joueur
    // -------------------------------------------------------------------------

    /**
     * Phase 1 : pioche la main du tour. Normalement la main est déjà vide
     * (défaussée par {@link #spin()}) ; par sécurité, ce qui y resterait part
     * à la défausse avant de piocher.
     *
     * @return les cartes ajoutées à la main (DEFAULT_DRAW_COUNT, ou moins si
     *         deck et défausse sont épuisés)
     */
    public DrawResult drawCards() {
        Player player = gameState.getPlayer();
        player.discardHand();
        return player.draw(DEFAULT_DRAW_COUNT);
    }

    /**
     * Le joueur joue une carte : elle quitte la main, ses effets immédiats
     * (ex : pioche, gains) sont appliqués tout de suite, et ses effets de tour
     * sont mis en attente jusqu'au spin.
     *
     * @param card carte jouée par le joueur
     * @return les cartes piochées par ses effets immédiats et les textes à afficher
     */
    public CardPlayResult playCard(Card card) {
        Player player = gameState.getPlayer();
        if (!player.playCard(card)) return CardPlayResult.none();

        PlayContext playContext = new PlayContext(player);
        card.getEffects().forEach(effect -> effect.onPlay(playContext));
        pendingEffects.addAll(playContext.getEffectsForSpin());

        if (playContext.getGains() != 0) player.addGains(playContext.getGains());
        DrawResult drawResult = playContext.getCardsToDraw() > 0
            ? player.draw(playContext.getCardsToDraw())
            : DrawResult.empty();
        return new CardPlayResult(drawResult, playContext.getPopups());
    }

    /**
     * Fin de phase 1 / Phase 2 : applique les effets en attente,
     * lance la machine à sous, résout le combat, puis envoie à la défausse
     * toutes les cartes du tour (jouées et restées sur la table).
     *
     * @return le résultat du tour (symboles tirés, événements, gains)
     */
    public TurnResult spin() {
        TurnResult result = turnEngine.playTurn(gameState, pendingEffects);
        pendingEffects.clear();
        gameState.getPlayer().discardHand();
        return result;
    }

    // -------------------------------------------------------------------------
    // Lecture de l'état (pour GameScreen)
    // -------------------------------------------------------------------------

    /** @return l'état courant de la partie (joueur, ennemi, numéro de tour). */
    public GameState getGameState() { return gameState; }
}
