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

    /** Nombre de cartes piochées par défaut à chaque appel de {@link #drawCards()}. */
    private static final int DEFAULT_DRAW_COUNT = 6;

    private       GameState  gameState;
    private final TurnEngine turnEngine = new TurnEngine();

    /**
     * Cartes chargées depuis les JSON une seule fois pour toute la durée de vie du
     * contrôleur : Card et Effect sont immuables et Deck copie la liste reçue, donc
     * rien n'empêche de réutiliser ces mêmes instances à chaque restart() plutôt que
     * de relire et re-parser les fichiers à chaque nouvelle partie.
     */
    private final List<Card> cardTemplates = CardLoader.loadAll();

    /** Effets accumulés depuis le début du tour, appliqués au moment du spin. */
    private final List<Effect> pendingEffects = new ArrayList<>();

    /**
     * Nombre de cartes à piocher au prochain appel de {@link #drawCards()}.
     * Vaut DEFAULT_DRAW_COUNT par défaut, mis à jour par le résultat de {@link #spin()}
     * quand un effet (ex : ExtraDrawEffect) l'a augmenté pour le tour suivant.
     */
    private int nextDrawCount = DEFAULT_DRAW_COUNT;

    /** Crée une nouvelle partie (joueur + ennemi au maximum de leurs PV) à partir des cartes déjà chargées. */
    public GameController() {
        this.gameState = new GameState(cardTemplates);
    }

    /**
     * Recommence un combat : recrée entièrement le GameState (joueur et
     * ennemi au maximum de leurs points de vie, bonus/malus effacés) et
     * vide les effets en attente du tour précédent.
     */
    public void restart() {
        this.gameState = new GameState(cardTemplates);
        pendingEffects.clear();
        nextDrawCount = DEFAULT_DRAW_COUNT;
    }

    // -------------------------------------------------------------------------
    // Actions du joueur
    // -------------------------------------------------------------------------

    /**
     * Phase 1 : pioche les cartes et les retourne pour affichage.
     * La main précédente (si non jouée) part à la défausse avant de piocher.
     *
     * @return la nouvelle main du joueur ({@link #nextDrawCount} cartes, DEFAULT_DRAW_COUNT
     *         sauf bonus d'un effet type ExtraDrawEffect appliqué au tour précédent)
     */
    public List<Card> drawCards() {
        Player player = gameState.getPlayer();
        player.getDiscardPile().addAll(player.getCurrentHand());
        List<Card> hand = player.getDeck().draw(nextDrawCount);
        player.setCurrentHand(hand);
        return hand;
    }

    /**
     * Le joueur joue une carte : ses effets sont mis en attente.
     * Ils seront appliqués au TurnContext lors du spin.
     *
     * @param card carte jouée par le joueur
     */
    public void playCard(Card card) {
        pendingEffects.addAll(card.getEffects());
    }

    /**
     * Fin de phase 1 / Phase 2 : applique les effets en attente,
     * lance la machine à sous, résout le combat et retourne le TurnResult.
     *
     * @return le résultat du tour (symboles tirés, événements, gains)
     */
    public TurnResult spin() {
        TurnResult result = turnEngine.playTurn(gameState, pendingEffects, DEFAULT_DRAW_COUNT);
        nextDrawCount = result.getNextDrawCount();
        pendingEffects.clear();
        return result;
    }

    // -------------------------------------------------------------------------
    // Lecture de l'état (pour GameScreen)
    // -------------------------------------------------------------------------

    /** @return l'état courant de la partie (joueur, ennemi, numéro de tour). */
    public GameState getGameState() { return gameState; }
}
