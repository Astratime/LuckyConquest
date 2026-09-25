package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.CardPlayResult;
import fr.astratime.lucky.entities.DrawResult;
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.choices.BetChoice;
import fr.astratime.lucky.entities.choices.CardChoice;
import fr.astratime.lucky.entities.choices.RouletteChoice;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.effects.BetOnSymbolEffect;
import fr.astratime.lucky.entities.effects.Effect;
import fr.astratime.lucky.entities.effects.PistolEffect;
import fr.astratime.lucky.loaders.CardLoader;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

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

    /** Choix demandé au joueur par la dernière carte jouée, en attente de sa réponse (null si aucun). */
    private CardChoice pendingChoice;

    /** Une carte (Bingo) a bloqué la main : plus aucune carte ne peut être jouée ce tour. */
    private boolean handLocked = false;

    /** Paris placés ce tour, pour les afficher en attendant le tirage. */
    private final List<Symbol> betsThisTurn = new ArrayList<>();

    /** Fournit un deck de départ neuf à chaque combat. */
    private final Supplier<List<Card>> starterDeck;

    /** Crée une carte à partir de son id (cartes créées en combat : Arc-en-ciel, Pot de Lutin). */
    private final Function<String, Card> cardFactory;

    private final Random random = new Random();

    /** Cartes changées de suite par l'Arc-en-ciel ce tour, et la carte d'origine de chacune. */
    private final Map<Card, Card> originals = new IdentityHashMap<>();

    /** Charge le deck de départ depuis les JSON et crée une nouvelle partie (joueur + ennemi au maximum de leurs PV). */
    public GameController() {
        this(CardLoader::loadStarterDeck, CardLoader.cardFactory());
    }

    /**
     * @param starterDeck fournit les cartes du deck de départ, appelé à chaque
     *                    nouveau combat (ex : une liste fixe dans les tests)
     */
    public GameController(Supplier<List<Card>> starterDeck) {
        this(starterDeck, id -> { throw new IllegalArgumentException("Carte inconnue : " + id); });
    }

    /**
     * @param starterDeck fournit les cartes du deck de départ, à chaque nouveau combat
     * @param cardFactory crée une carte à partir de son id (cartes créées en cours de combat)
     */
    public GameController(Supplier<List<Card>> starterDeck, Function<String, Card> cardFactory) {
        this.cardFactory = cardFactory;
        this.starterDeck = starterDeck;
        this.gameState   = new GameState(starterDeck.get());
    }

    /**
     * Recommence un combat : recrée entièrement le GameState (joueur et
     * ennemi au maximum de leurs points de vie, bonus/malus effacés) et
     * vide les effets en attente du tour précédent.
     */
    public void restart() {
        this.gameState = new GameState(starterDeck.get());
        pendingEffects.clear();
        betsThisTurn.clear();
        pendingChoice = null;
        handLocked    = false;
        originals.clear();
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
        if (handLocked || pendingChoice != null || !player.playCard(card)) return CardPlayResult.none();

        PlayContext playContext = new PlayContext(player);
        card.getEffects().forEach(effect -> effect.onPlay(playContext));
        pendingEffects.addAll(playContext.getEffectsForSpin());

        if (playContext.getGains() != 0) player.addGains(playContext.getGains());
        DrawResult drawResult = playContext.getCardsToDraw() > 0
            ? player.draw(playContext.getCardsToDraw())
            : DrawResult.empty();
        pendingChoice = playContext.getChoice();
        if (playContext.isAutoSpin()) handLocked = true;
        CardPlayResult.Rainbow rainbow = playContext.getRainbowCardId() != null
            ? rainbow(player, playContext.getRainbowCardId())
            : null;
        return new CardPlayResult(drawResult, playContext.getPopups(), pendingChoice, playContext.isAutoSpin(),
            rainbow);
    }

    /**
     * Arc-en-ciel : une suite est tirée au hasard et toutes les cartes à suite
     * de la main la prennent (même rang), jusqu'à la fin du tour ; puis la carte
     * {@code addedId} est posée sur la table, ou en défausse s'il n'y a plus de place.
     */
    private CardPlayResult.Rainbow rainbow(Player player, String addedId) {
        Card.Suit suit = Card.Suit.values()[random.nextInt(Card.Suit.values().length)];
        List<CardPlayResult.Recolor> recolored = new ArrayList<>();
        for (Card card : new ArrayList<>(player.getCurrentHand())) {
            if (card.getSuit() == null) continue;
            Card recolor = cardFactory.apply(suit.cardId(card.getRank()));
            player.replaceInHand(card, recolor);
            originals.put(recolor, originals.getOrDefault(card, card)); // redevient la carte d'origine en fin de tour
            recolored.add(new CardPlayResult.Recolor(card, recolor));
        }
        Card added = cardFactory.apply(addedId);
        return new CardPlayResult.Rainbow(suit, recolored, added, player.addToHandOrDiscard(added));
    }

    /** @return le choix demandé au joueur par la dernière carte jouée, ou {@code null} si aucun. */
    public CardChoice getPendingChoice() { return pendingChoice; }

    /** @return {@code true} si plus aucune carte ne peut être jouée ce tour (Bingo). */
    public boolean isHandLocked() { return handLocked; }

    /** @return les symboles sur lesquels parier : ceux qui peuvent sortir ce tour (ni Joker, ni retirés). */
    public List<Symbol> getBetOptions() {
        List<Symbol> options = new ArrayList<>();
        for (Symbol symbol : Symbol.values()) {
            if (symbol != Symbol.JOKER
                && !gameState.getPlayer().getLastingEffects().getRemovedSymbols().containsKey(symbol)) {
                options.add(symbol);
            }
        }
        return options;
    }

    /** @return les symboles pariés ce tour, en attente du tirage. */
    public List<Symbol> getBetsThisTurn() { return List.copyOf(betsThisTurn); }

    /**
     * Réponse au Pari : le pari sur {@code symbol} est mis en attente jusqu'au spin.
     *
     * @return les textes à afficher
     * @throws IllegalStateException si aucun pari n'est en attente de choix
     */
    public List<EffectPopup> placeBet(Symbol symbol) {
        if (!(pendingChoice instanceof BetChoice)) throw new IllegalStateException("Aucun pari en attente");
        pendingChoice = null;
        BetOnSymbolEffect bet = new BetOnSymbolEffect(symbol);
        pendingEffects.add(bet);
        betsThisTurn.add(symbol);
        return bet.getPopups();
    }

    /**
     * Réponse à la Roulette russe : retourne la carte {@code index}. Le Joker
     * maudit coûte une partie des gains tout de suite ; sinon, le pistolet est
     * mis en attente jusqu'au spin.
     *
     * @return {@code true} si la carte est le Joker maudit, et les textes à afficher
     * @throws IllegalStateException si aucune roulette n'est en attente de choix
     */
    public RouletteOutcome pickRouletteCard(int index) {
        if (!(pendingChoice instanceof RouletteChoice roulette)) {
            throw new IllegalStateException("Aucune roulette en attente");
        }
        pendingChoice = null;
        if (roulette.cursed().get(index)) {
            int lost = gameState.getPlayer().consumeGainsPercent(roulette.penaltyPercent() / 100f);
            return new RouletteOutcome(true, List.of(
                new EffectPopup("JOKER MAUDIT !", EffectPopup.Style.DAMAGE, PopupScale.MAX_INTENSITY),
                EffectPopup.scaled("GAINS -" + lost, EffectPopup.Style.DAMAGE, lost, PopupScale.SPIN_GAINS)));
        }
        PistolEffect pistol = new PistolEffect(roulette.pistolMultiplier());
        pendingEffects.add(pistol);
        return new RouletteOutcome(false, pistol.getPopups());
    }

    /**
     * Carte retournée à la Roulette russe.
     *
     * @param cursed {@code true} si c'est le Joker maudit
     * @param popups textes à afficher
     */
    public record RouletteOutcome(boolean cursed, List<EffectPopup> popups) { }

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
        betsThisTurn.clear();
        pendingChoice = null;
        handLocked    = false;
        gameState.getPlayer().restoreCards(originals); // l'effet de l'Arc-en-ciel ne dure que le tour
        originals.clear();
        gameState.getPlayer().discardHand();
        return result;
    }

    // -------------------------------------------------------------------------
    // Lecture de l'état (pour GameScreen)
    // -------------------------------------------------------------------------

    /** @return l'état courant de la partie (joueur, ennemi, numéro de tour). */
    public GameState getGameState() { return gameState; }
}
