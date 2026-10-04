package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.CardPlayResult;
import fr.astratime.lucky.entities.Combo;
import fr.astratime.lucky.entities.DrawResult;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.choices.BetChoice;
import fr.astratime.lucky.entities.choices.CardChoice;
import fr.astratime.lucky.entities.choices.RouletteChoice;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.effects.BetOnSymbolEffect;
import fr.astratime.lucky.entities.effects.BingoEffect;
import fr.astratime.lucky.entities.effects.Effect;
import fr.astratime.lucky.entities.effects.PistolEffect;
import fr.astratime.lucky.entities.enemy.EnemyKind;
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
    /** Cartes que le joueur peut jouer par tour (plus sous l'effet de Dans la manche). */
    public static final int DEFAULT_PLAY_LIMIT = 4;

    private       GameState  gameState;
    private final TurnEngine turnEngine = new TurnEngine();

    /** Effets accumulés depuis le début du tour, appliqués au moment du spin. */
    private final List<Effect> pendingEffects = new ArrayList<>();

    /** Choix demandé au joueur par la dernière carte jouée, en attente de sa réponse (null si aucun). */
    private CardChoice pendingChoice;

    /** Une carte (Bingo) a bloqué la main : plus aucune carte ne peut être jouée ce tour. */
    private boolean handLocked = false;

    /** Carte offerte au prochain tour après un Bingo de bouclier (voir {@link #claimBonusCard()}). */
    public static final String SHIELD_BINGO_GIFT = "bingo";

    /** Un Bingo de bouclier a été tiré : un Bingo est offert au prochain tour du joueur. */
    private boolean bingoGiftPending = false;

    /** Cartes jouées ce tour (toutes comptent, consommables compris), limitées par {@link #getPlayLimit()}. */
    private int cardsPlayedThisTurn = 0;

    /** Paris placés ce tour, pour les afficher en attendant le tirage. */
    private final List<Symbol> betsThisTurn = new ArrayList<>();

    /** Fournit un deck de départ neuf à chaque combat. */
    private final Supplier<List<Card>> starterDeck;

    /** Crée une carte à partir de son id (cartes créées en combat : Arc-en-ciel, Pot de Lutin, achats). */
    private final Function<String, Card> cardFactory;

    /** Cartes proposées à l'échoppe. */
    private final List<ShopOffer> shopOffers;

    private final Random random = new Random();

    /** Cartes changées de suite par l'Arc-en-ciel ce tour, et la carte d'origine de chacune. */
    private final Map<Card, Card> originals = new IdentityHashMap<>();

    /** Charge le deck de départ depuis les JSON et crée une nouvelle partie (joueur + ennemi au maximum de leurs PV). */
    public GameController() {
        this(CardLoader::loadStarterDeck, CardLoader.cardFactory(), CardLoader.loadShop());
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
        this(starterDeck, cardFactory, Map.of());
    }

    /**
     * @param shop prix de chaque carte proposée à l'échoppe, par id (dans l'ordre d'affichage)
     * @see #GameController(Supplier, Function)
     */
    public GameController(Supplier<List<Card>> starterDeck, Function<String, Card> cardFactory,
                          Map<String, Integer> shop) {
        this.shopOffers = shop.entrySet().stream()
            .map(entry -> new ShopOffer(cardFactory.apply(entry.getKey()), entry.getValue()))
            .toList();
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
        restart(EnemyKind.CROUPIER);
    }

    /** Comme {@link #restart()}, contre un ennemi {@code kind} (premier combat d'un chapitre). */
    public void restart(EnemyKind kind) {
        GameState fresh = new GameState(starterDeck.get());
        this.gameState = new GameState(fresh.getPlayer(), new Enemy(kind));
        clearTurn();
    }

    /**
     * Combat suivant d'une épreuve, contre un ennemi {@code kind} : le joueur
     * garde ses PV, ses gains et toutes ses cartes (achats compris), sans effet
     * en cours ; voir {@link Player#nextCombat()}.
     */
    public void startCombat(EnemyKind kind) {
        this.gameState = new GameState(gameState.getPlayer().nextCombat(), new Enemy(kind));
        clearTurn();
    }

    /** Oublie tout ce qui restait du tour en cours (effets, paris, choix, compteur de cartes). */
    private void clearTurn() {
        pendingEffects.clear();
        betsThisTurn.clear();
        cardsPlayedThisTurn = 0;
        pendingChoice = null;
        handLocked    = false;
        bingoGiftPending = false;
        originals.clear();
    }

    // -------------------------------------------------------------------------
    // Actions du joueur
    // -------------------------------------------------------------------------

    /**
     * Phase 1 : pioche la main du tour. La main est normalement vide
     * (défaussée par {@link #spin()}), sauf si une carte y a été posée entre
     * deux tours (achat à l'échoppe) : elle y reste.
     *
     * @return les cartes ajoutées à la main (DEFAULT_DRAW_COUNT, ou moins si
     *         deck et défausse sont épuisés ou si la main est pleine)
     */
    public DrawResult drawCards() {
        return gameState.getPlayer().draw(DEFAULT_DRAW_COUNT);
    }

    // -------------------------------------------------------------------------
    // Échoppe
    // -------------------------------------------------------------------------

    /**
     * Carte proposée à l'échoppe.
     *
     * @param card  aperçu de la carte (l'achat en crée une nouvelle instance)
     * @param price prix, en gains
     */
    public record ShopOffer(Card card, int price) { }

    /**
     * Carte achetée.
     *
     * @param card        la carte achetée
     * @param addedToHand {@code true} si elle est posée sur la table, {@code false} si elle part dans le deck
     */
    public record Purchase(Card card, boolean addedToHand) { }

    /** @return les cartes proposées à l'échoppe. */
    public List<ShopOffer> getShopOffers() { return shopOffers; }

    /**
     * @return pourquoi {@code offer} ne peut pas être achetée en ce moment (hors
     *         manque de gains), ou {@code null} si elle est disponible. Un Bingo
     *         dont le symbole est retiré des rouleaux (Recyclage) ne peut pas
     *         sortir : il n'est pas en vente tant que le symbole n'est pas revenu.
     */
    public String unavailableReason(ShopOffer offer) {
        Symbol recycled = recycledBingoSymbol(offer.card());
        if (recycled == null) return null;
        int turns = gameState.getPlayer().getLastingEffects().getRemovedSymbols().get(recycled);
        return "Indisponible : le symbole " + recycled.getDisplayName()
            + " est retiré des rouleaux par le Recyclage (encore " + turnsText(turns) + ")";
    }

    /**
     * @return pourquoi {@code card} ne peut pas être jouée en ce moment, ou
     *         {@code null} si elle le peut : la limite de cartes du tour est
     *         atteinte, ou (comme à l'échoppe) c'est un Bingo dont le symbole est
     *         retiré des rouleaux (Recyclage), qui reste en main tant que le
     *         symbole n'est pas revenu.
     */
    public String unplayableReason(Card card) {
        if (cardsPlayedThisTurn >= getPlayLimit()) {
            return "Limite atteinte : " + getPlayLimit() + " cartes jouées ce tour";
        }
        Symbol recycled = recycledBingoSymbol(card);
        if (recycled == null) return null;
        int turns = gameState.getPlayer().getLastingEffects().getRemovedSymbols().get(recycled);
        return "Le symbole " + recycled.getDisplayName() + " est retiré des rouleaux par le Recyclage (encore "
            + turnsText(turns) + ") : ce Bingo ne peut pas être joué";
    }

    /** @return le symbole imposé par le Bingo de {@code card} s'il est retiré des rouleaux, sinon {@code null}. */
    private Symbol recycledBingoSymbol(Card card) {
        Map<Symbol, Integer> removed = gameState.getPlayer().getLastingEffects().getRemovedSymbols();
        for (Effect effect : card.getEffects()) {
            if (effect instanceof BingoEffect bingo && removed.containsKey(bingo.getSymbol())) return bingo.getSymbol();
        }
        return null;
    }

    private static String turnsText(int turns) {
        return turns + (turns > 1 ? " tours" : " tour");
    }

    /**
     * Achète {@code offer} : son prix est retiré des gains, puis la carte est
     * posée sur la table s'il y a de la place, sinon glissée dans le deck.
     *
     * @return l'achat, ou {@code null} si la carte est indisponible (voir
     *         {@link #unavailableReason}) ou si les gains ne suffisent pas
     */
    public Purchase buy(ShopOffer offer) {
        Player player = gameState.getPlayer();
        if (unavailableReason(offer) != null || player.getGains() < offer.price()) return null;
        player.addGains(-offer.price());
        Card card = cardFactory.apply(offer.card().getId());
        return new Purchase(card, player.addToHandOrDeck(card));
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
        if (handLocked || pendingChoice != null || unplayableReason(card) != null || !player.playCard(card)) {
            return CardPlayResult.none();
        }

        cardsPlayedThisTurn++;
        PlayContext playContext = new PlayContext(player);
        for (Effect effect : card.getEffects()) {
            int firstPopup = playContext.getPopups().size();
            effect.onPlay(playContext);
            playContext.attachSound(firstPopup, effect.getSound()); // son de l'effet, avec son premier texte
        }
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

    /**
     * Début du tour du joueur, avant sa pioche : après un Bingo de bouclier, une
     * carte Bingo (symbole au hasard) lui est offerte, posée sur la table s'il y a
     * de la place, sinon glissée dans le deck.
     *
     * @return la carte offerte, ou {@code null} s'il n'y en a pas
     */
    public Purchase claimBonusCard() {
        if (!bingoGiftPending) return null;
        bingoGiftPending = false;
        Card card = cardFactory.apply(SHIELD_BINGO_GIFT);
        return new Purchase(card, gameState.getPlayer().addToHandOrDeck(card));
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

    /** @return les combinaisons que forment les cartes jouées ce tour, appliquées au lancer. */
    public List<Combo> getCurrentCombos() { return Combo.formed(gameState.getPlayer().getPlayedCards()); }

    /** @return les cartes que le joueur peut jouer ce tour (plus sous l'effet de Dans la manche). */
    public int getPlayLimit() {
        return Math.max(DEFAULT_PLAY_LIMIT, gameState.getPlayer().getLastingEffects().getExtraPlays());
    }

    /** @return les cartes jouées ce tour. */
    public int getCardsPlayedThisTurn() { return cardsPlayedThisTurn; }

    /** @return {@code true} si le joueur a atteint la limite de cartes du tour. */
    public boolean isPlayLimitReached() { return cardsPlayedThisTurn >= getPlayLimit(); }

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
     * Réponse à la Roulette russe : retourne la carte {@code index}. Le
     * pistolet est mis en attente jusqu'au spin, quelle que soit la carte (moins
     * fort avec le Joker maudit, qui coûte en plus une partie des gains tout de suite).
     *
     * @return {@code true} si la carte est le Joker maudit, et les textes à afficher
     * @throws IllegalStateException si aucune roulette n'est en attente de choix
     */
    public RouletteOutcome pickRouletteCard(int index) {
        if (!(pendingChoice instanceof RouletteChoice roulette)) {
            throw new IllegalStateException("Aucune roulette en attente");
        }
        pendingChoice = null;
        boolean cursed = roulette.cursed().get(index);
        PistolEffect pistol = new PistolEffect(cursed ? roulette.cursedMultiplier() : roulette.pistolMultiplier());
        pendingEffects.add(pistol);
        if (cursed) {
            int lost = gameState.getPlayer().consumeGainsPercent(roulette.penaltyPercent() / 100f);
            List<EffectPopup> popups = new ArrayList<>(List.of(
                new EffectPopup("JOKER MAUDIT !", EffectPopup.Style.DAMAGE, PopupScale.MAX_INTENSITY),
                EffectPopup.scaled("GAINS -" + lost, EffectPopup.Style.DAMAGE, lost, PopupScale.SPIN_GAINS)));
            popups.addAll(pistol.getPopups());
            return new RouletteOutcome(true, popups);
        }
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
        if (result.isShieldBingo()) bingoGiftPending = true;
        pendingEffects.clear();
        betsThisTurn.clear();
        cardsPlayedThisTurn = 0;
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
