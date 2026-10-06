package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.CardFamily;
import fr.astratime.lucky.entities.CardPlayResult;
import fr.astratime.lucky.entities.Combo;
import fr.astratime.lucky.entities.DrawResult;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.SlotMachine;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.choices.BetChoice;
import fr.astratime.lucky.entities.choices.CardChoice;
import fr.astratime.lucky.entities.choices.RiggedReelChoice;
import fr.astratime.lucky.entities.choices.RouletteChoice;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.effects.BetOnSymbolEffect;
import fr.astratime.lucky.entities.effects.BingoEffect;
import fr.astratime.lucky.entities.effects.Effect;
import fr.astratime.lucky.entities.effects.ForceReelEffect;
import fr.astratime.lucky.entities.effects.OverheatEffect;
import fr.astratime.lucky.entities.effects.PistolEffect;
import fr.astratime.lucky.entities.enemy.EnemyCards;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.exploration.PlaceRule;
import fr.astratime.lucky.i18n.Lang;
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

    /** Règle du lieu (Exploration), qui joue dans chaque combat. */
    private PlaceRule placeRule = PlaceRule.NONE;

    /** Textes du début du tour : mauvais sorts de l'ennemi et règle du lieu. */
    private final List<EffectPopup> turnNotices = new ArrayList<>();

    /** La main de ce tour est face cachée (Aveuglement de l'ennemi). */
    private boolean handHidden = false;

    /** Cartes à jouer d'office au début de ce tour (Chant de l'ennemi). */
    private int songs = 0;
    /** Taxes du Comptable ce tour : chaque carte jouée les paie toutes. */
    private int taxes = 0;

    /** Cartes au trésor jouées, pas encore comptées pour le coffre du donjon. */
    private int treasureMaps = 0;

    /** Une carte (Bingo) a bloqué la main : plus aucune carte ne peut être jouée ce tour. */
    private boolean handLocked = false;

    /** Préfixe des cartes Bingo à symbole imposé (« bingo_bell »…), offertes après un Bingo de gains de la carte « Bingo » (voir {@link #claimBonusCard()}). */
    public static final String BINGO_GIFT_PREFIX = "bingo_";

    /** La carte « Bingo » a donné un Bingo de gains : une carte Bingo (symbole au hasard) est glissée dans le deck au prochain tour. */
    private boolean bingoGiftPending = false;

    /** Cartes jouées ce tour (toutes comptent, consommables compris), limitées par {@link #getPlayLimit()}. */
    private int cardsPlayedThisTurn = 0;

    /** Double ou rien : la prochaine carte jouée ce tour compte deux fois. */
    private boolean doubleNext = false;

    /** Paris placés ce tour, pour les afficher en attendant le tirage. */
    private final List<Symbol> betsThisTurn = new ArrayList<>();

    /** Fournit un deck de départ neuf à chaque combat. */
    private final Supplier<List<Card>> starterDeck;

    /** Crée le joueur d'un nouveau combat avec son deck (rang et machine du profil). */
    private final Function<List<Card>, Player> playerFactory;

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
        this(starterDeck, cardFactory, shop, cards -> new Player(Lang.t("Joueur"), Player.BASE_HP, cards));
    }

    /**
     * @param playerFactory crée le joueur de chaque nouveau combat à partir de
     *                      son deck (avec le bonus de son rang et sa machine)
     * @see #GameController(Supplier, Function, Map)
     */
    public GameController(Supplier<List<Card>> starterDeck, Function<String, Card> cardFactory,
                          Map<String, Integer> shop, Function<List<Card>, Player> playerFactory) {
        this.playerFactory = playerFactory;
        this.shopOffers = shop.entrySet().stream()
            .map(entry -> new ShopOffer(cardFactory.apply(entry.getKey()), entry.getValue()))
            .toList();
        this.cardFactory = cardFactory;
        this.starterDeck = starterDeck;
        this.gameState   = new GameState(playerFactory.apply(starterDeck.get()), new Enemy(EnemyKind.CROUPIER));
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
        Player player = playerFactory.apply(starterDeck.get());
        this.gameState = new GameState(player, createEnemy(kind, player));
        gameState.setPlaceRule(placeRule);
        treasureMaps = 0;
        clearTurn();
    }

    /**
     * Combat suivant d'une épreuve, contre un ennemi {@code kind} : le joueur
     * garde ses PV, ses gains et toutes ses cartes (achats compris), sans effet
     * en cours ; voir {@link Player#nextCombat()}.
     */
    public void startCombat(EnemyKind kind) {
        Player player = gameState.getPlayer().nextCombat();
        this.gameState = new GameState(player, createEnemy(kind, player));
        gameState.setPlaceRule(placeRule);
        clearTurn();
    }

    /**
     * @return un ennemi {@code kind} face à {@code player} : l'Ombre du Joueur
     *         prend une copie de son deck ; le Portier tire la famille de cartes
     *         qu'il laisse dehors
     */
    private Enemy createEnemy(EnemyKind kind, Player player) {
        Enemy enemy = kind.copiesPlayerDeck() ? new Enemy(kind, EnemyCards.shadowDeck(player.getAllCards()))
            : new Enemy(kind);
        if (kind.bansFamily()) {
            CardFamily[] families = CardFamily.values();
            enemy.banFamily(families[random.nextInt(families.length)]);
        }
        return enemy;
    }

    /** Oublie tout ce qui restait du tour en cours (effets, paris, choix, compteur de cartes). */
    private void clearTurn() {
        pendingEffects.clear();
        betsThisTurn.clear();
        cardsPlayedThisTurn = 0;
        pendingChoice = null;
        handLocked    = false;
        doubleNext    = false;
        bingoGiftPending = false;
        handHidden = false;
        songs = 0;
        taxes = 0;
        turnNotices.clear();
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
        Player player = gameState.getPlayer();
        LastingEffects lasting = player.getLastingEffects();
        DrawResult drawn = player.draw(DEFAULT_DRAW_COUNT);
        List<Card> added     = new ArrayList<>(drawn.getAddedToHand());
        List<Card> discarded = new ArrayList<>(drawn.getDiscarded());
        turnNotices.clear();

        // Grignotage de l'ennemi : des cartes piochées sont rongées et partent à la défausse.
        int nibbles = lasting.takeNibbles();
        for (int i = 0; i < nibbles && !added.isEmpty(); i++) {
            Card eaten = added.remove(random.nextInt(added.size()));
            player.discardFromHand(eaten);
            discarded.add(eaten);
            turnNotices.add(new EffectPopup(Lang.f("GRIGNOTAGE : {0} RONGÉE", eaten.getName().toUpperCase()),
                EffectPopup.Style.DAMAGE, PopupScale.SECONDARY_INTENSITY));
        }
        // Scorbut (Port des Contrebandiers) : il remplace une carte piochée.
        if (gameState.getActiveRule().scurvyArrives(gameState.getTurnNumber()) && !added.isEmpty()) {
            int index = random.nextInt(added.size());
            Card replaced = added.get(index);
            Card scurvy = cardFactory.apply(PlaceRule.SCURVY_CARD);
            player.replaceInHand(replaced, scurvy);
            player.getDiscardPile().addAll(List.of(replaced));
            added.set(index, scurvy);
            discarded.add(replaced);
            turnNotices.add(new EffectPopup(Lang.t("SCORBUT !"), EffectPopup.Style.DAMAGE, PopupScale.MAX_INTENSITY));
        }
        handHidden = lasting.takeBlind();
        if (lasting.useLantern() && handHidden) { // Lanterne : la main reste visible
            handHidden = false;
            turnNotices.add(new EffectPopup(Lang.t("LANTERNE : AVEUGLEMENT DISSIPÉ"), EffectPopup.Style.DEFENSE,
                PopupScale.SECONDARY_INTENSITY));
        }
        if (handHidden) {
            turnNotices.add(new EffectPopup(Lang.t("AVEUGLEMENT : MAIN CACHÉE"), EffectPopup.Style.DAMAGE,
                PopupScale.SECONDARY_INTENSITY));
        }
        taxes = lasting.takeTaxes();
        if (taxes > 0) {
            turnNotices.add(new EffectPopup(Lang.t("TAXE : CHAQUE CARTE TE COÛTE"), EffectPopup.Style.DAMAGE,
                PopupScale.SECONDARY_INTENSITY));
        }
        CardFamily banned = gameState.getEnemy().getBannedFamily();
        if (banned != null && gameState.getTurnNumber() == 1) {
            turnNotices.add(new EffectPopup(Lang.f("LE PORTIER INTERDIT : {0}", banned.getDisplayName().toUpperCase()),
                EffectPopup.Style.DAMAGE, PopupScale.MAX_INTENSITY));
        }
        songs = lasting.takeSongs();
        if (lasting.useEarplugs() && songs > 0) { // Bouchons d'oreille : le Chant ne passe pas
            songs = 0;
            turnNotices.add(new EffectPopup(Lang.t("BOUCHONS D'OREILLE : CHANT IGNORÉ"), EffectPopup.Style.DEFENSE,
                PopupScale.SECONDARY_INTENSITY));
        }
        if (songs > 0) {
            turnNotices.add(new EffectPopup(Lang.t("CHANT : CARTE JOUÉE D'OFFICE"), EffectPopup.Style.DAMAGE,
                PopupScale.SECONDARY_INTENSITY));
        }
        return new DrawResult(added, discarded);
    }

    /** @return les textes du début du tour (Grignotage, Scorbut, Aveuglement, Chant), après {@link #drawCards()}. */
    public List<EffectPopup> getTurnNotices() { return List.copyOf(turnNotices); }

    /** @return {@code true} si la main de ce tour est face cachée (Aveuglement de l'ennemi). */
    public boolean isHandHidden() { return handHidden; }

    /**
     * Chant de l'ennemi : les cartes de la main à jouer d'office ce tour, tirées
     * au hasard parmi celles qui peuvent l'être. À appeler une fois, après la pioche.
     *
     * @return les cartes à jouer, dans l'ordre (vide sans Chant)
     */
    public List<Card> takeSongCards() {
        List<Card> chosen = new ArrayList<>();
        List<Card> hand = new ArrayList<>(gameState.getPlayer().getCurrentHand());
        for (int i = 0; i < songs && !hand.isEmpty(); i++) {
            Card card = hand.remove(random.nextInt(hand.size()));
            if (unplayableReason(card) == null) chosen.add(card);
        }
        songs = 0;
        return chosen;
    }

    /**
     * Fixe la règle du lieu (Exploration) pour ce combat et les suivants (voir
     * {@link PlaceRule}) ; aucune hors de l'Exploration.
     */
    public void setPlaceRule(PlaceRule rule) {
        placeRule = rule;
        gameState.setPlaceRule(rule);
    }

    /** @return les Cartes au trésor jouées depuis le dernier appel (le coffre du donjon donnera autant de cartes de plus). */
    public int takeTreasureMaps() {
        int maps = treasureMaps;
        treasureMaps = 0;
        return maps;
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
        return Lang.f("Indisponible : le symbole {0} est retiré des rouleaux par le Recyclage (encore {1})",
            recycled.getDisplayName(), turnsText(turns));
    }

    /**
     * @return pourquoi {@code card} ne peut pas être jouée en ce moment, ou
     *         {@code null} si elle le peut : la limite de cartes du tour est
     *         atteinte, ou (comme à l'échoppe) c'est un Bingo dont le symbole est
     *         retiré des rouleaux (Recyclage), qui reste en main tant que le
     *         symbole n'est pas revenu.
     */
    public String unplayableReason(Card card) {
        CardFamily banned = gameState.getEnemy().getBannedFamily();
        if (banned != null && banned.contains(card)) {
            return Lang.f("Le Portier laisse les cartes {0} dehors : pas de ça ici, tout le combat",
                banned.getDisplayName());
        }
        if (cardsPlayedThisTurn >= getPlayLimit()) {
            return Lang.f("Limite atteinte : {0} cartes jouées ce tour", getPlayLimit());
        }
        Symbol recycled = recycledBingoSymbol(card);
        if (recycled == null) return null;
        int turns = gameState.getPlayer().getLastingEffects().getRemovedSymbols().get(recycled);
        return Lang.f("Le symbole {0} est retiré des rouleaux par le Recyclage (encore {1}) : ce Bingo ne peut pas "
            + "être joué",
            recycled.getDisplayName(), turnsText(turns));
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
        return Lang.f(turns > 1 ? "{0} tours" : "{0} tour", turns);
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
        Enemy enemy = gameState.getEnemy();
        if (enemy.getKind().copiesPurchases()) { // le Pilleur en garde une copie : un As sombre
            Card.Suit[] suits = Card.Suit.values();
            enemy.addLoot(card.getSuit() != null ? card.getSuit() : suits[random.nextInt(suits.length)]);
        }
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
        for (int i = 0; i < taxes; i++) { // Taxe du Comptable : chaque carte jouée la paie
            int taxed = player.payTax(EnemySymbol.TAX_PERCENT, EnemySymbol.TAX_FLAT);
            playContext.addPopups(List.of(new EffectPopup(Lang.f("TAXE -{0}", taxed), EffectPopup.Style.DAMAGE,
                PopupScale.SECONDARY_INTENSITY)));
        }
        boolean doubling = doubleNext;
        playEffects(card, playContext, false);
        // Double ou rien : la carte compte deux fois, sauf si elle demande un choix (le doublement attend la suivante)
        if (doubling && playContext.getChoice() == null && !playContext.isDoubleRequested()) {
            doubleNext = false;
            playContext.addPopups(List.of(new EffectPopup(Lang.t("DOUBLE !"), EffectPopup.Style.SPECIAL,
                PopupScale.MAX_INTENSITY)));
            playEffects(card, playContext, true);
        }
        if (playContext.isDoubleRequested()) doubleNext = true;
        treasureMaps += playContext.getTreasureMaps();
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
     * Joue les effets de {@code card} ({@code again} : une seconde fois, pour
     * Double ou rien, sans ceux qui ne se doublent pas).
     */
    private static void playEffects(Card card, PlayContext playContext, boolean again) {
        for (Effect effect : card.getEffects()) {
            if (again && !effect.canBeDoubled()) continue;
            int firstPopup = playContext.getPopups().size();
            effect.onPlay(playContext);
            playContext.attachSound(firstPopup, effect.getSound()); // son de l'effet, avec son premier texte
        }
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
     * Début du tour du joueur, avant sa pioche : après un Bingo de gains obtenu
     * avec la carte « Bingo » (pas un Bingo tiré par la machine seule), une
     * carte Bingo d'un symbole tiré au hasard (pas forcément celui du Bingo, ni un
     * symbole retiré des rouleaux) est glissée dans son deck, à une place au hasard.
     *
     * @return la carte offerte, ou {@code null} s'il n'y en a pas
     */
    public Purchase claimBonusCard() {
        if (!bingoGiftPending) return null;
        bingoGiftPending = false;
        List<Symbol> symbols = getBingoGiftSymbols();
        if (symbols.isEmpty()) return null;
        Card card = cardFactory.apply(bingoGiftId(symbols.get(random.nextInt(symbols.size()))));
        gameState.getPlayer().getDeck().insertRandomly(card);
        return new Purchase(card, false);
    }

    /** @return les symboles que peut imposer la carte Bingo offerte : ceux de la machine du joueur, sauf ceux retirés. */
    private List<Symbol> getBingoGiftSymbols() {
        List<Symbol> symbols = new ArrayList<>(gameState.getPlayer().getSlotMachine().getReels());
        symbols.removeIf(gameState.getPlayer().getLastingEffects().getRemovedSymbols()::containsKey);
        return symbols;
    }

    /** @return l'id de la carte Bingo qui impose {@code symbol} (« bingo_bell »…). */
    public static String bingoGiftId(Symbol symbol) {
        return BINGO_GIFT_PREFIX + symbol.name().toLowerCase(java.util.Locale.ROOT);
    }

    /** @return le choix demandé au joueur par la dernière carte jouée, ou {@code null} si aucun. */
    public CardChoice getPendingChoice() { return pendingChoice; }

    /** @return {@code true} si plus aucune carte ne peut être jouée ce tour (Bingo). */
    public boolean isHandLocked() { return handLocked; }

    /** @return les symboles sur lesquels parier : ceux de la machine qui peuvent sortir ce tour (ni Joker, ni retirés). */
    public List<Symbol> getBetOptions() {
        List<Symbol> options = new ArrayList<>();
        for (Symbol symbol : gameState.getPlayer().getSlotMachine().getReels()) {
            if (!gameState.getPlayer().getLastingEffects().getRemovedSymbols().containsKey(symbol)) {
                options.add(symbol);
            }
        }
        return options;
    }

    /** @return les combinaisons que forment les cartes jouées ce tour, appliquées au lancer. */
    public List<Combo> getCurrentCombos() { return Combo.formed(gameState.getPlayer().getPlayedCards()); }

    /** @return les cartes que le joueur peut jouer ce tour (plus sous l'effet de Dans la manche). */
    public int getPlayLimit() {
        LastingEffects lasting = gameState.getPlayer().getLastingEffects();
        return Math.max(DEFAULT_PLAY_LIMIT, lasting.getExtraPlays()) + lasting.getBonusPlays();
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
     * Réponse au Rouleau truqué : {@code symbol} est imposé au rouleau du milieu au prochain spin.
     *
     * @return les textes à afficher
     * @throws IllegalStateException si aucun Rouleau truqué n'est en attente de choix
     */
    public List<EffectPopup> rigReel(Symbol symbol) {
        if (!(pendingChoice instanceof RiggedReelChoice)) throw new IllegalStateException("Aucun rouleau truqué en attente");
        pendingChoice = null;
        ForceReelEffect rigged = new ForceReelEffect(symbol);
        pendingEffects.add(rigged);
        return rigged.getPopups();
    }

    /** Tutoriel : le prochain tirage affiche {@code symbols}, de gauche à droite (sans texte ni son). */
    public void rigSpin(Symbol... symbols) {
        for (int reel = 0; reel < symbols.length; reel++) pendingEffects.add(new ForceReelEffect(reel, symbols[reel]));
    }

    /**
     * Tutoriel : un Bingo joué ce tour (même la carte « Bingo », au symbole tiré
     * au hasard) aligne {@code symbol}. Un Bingo de puissance 1 qui ne se lance
     * pas seul : il ne fait qu'imposer son symbole.
     */
    public void rigJackpot(Symbol symbol) {
        pendingEffects.add(new BingoEffect(1, symbol));
    }

    /** @return {@code true} si la prochaine carte jouée ce tour comptera deux fois (Double ou rien). */
    public boolean isDoubleNextPending() { return doubleNext; }

    /** @return les rouleaux qui tourneront au prochain spin : 4 avec la Machine en surchauffe, sinon 3. */
    public int getReelCount() {
        boolean overheat = pendingEffects.stream().anyMatch(effect -> effect instanceof OverheatEffect);
        return overheat ? SlotMachine.MAX_SYMBOL_COUNT : SlotMachine.SYMBOL_COUNT;
    }

    /** @return le symbole imposé au rouleau du milieu au prochain spin (Rouleau truqué ou fantôme), ou {@code null}. */
    public Symbol getForcedMiddleSymbol() {
        Symbol forced = null;
        for (Effect effect : pendingEffects) {
            if (effect instanceof ForceReelEffect force && force.getReel() == ForceReelEffect.MIDDLE_REEL) forced = force.getSymbol();
        }
        return forced;
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
                new EffectPopup(Lang.t("JOKER MAUDIT !"), EffectPopup.Style.DAMAGE, PopupScale.MAX_INTENSITY),
                EffectPopup.scaled(Lang.f("GAINS -{0}", lost), EffectPopup.Style.DAMAGE, lost, PopupScale.SPIN_GAINS)));
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
        // La carte « Bingo » (symbole au hasard), seule, offre un Bingo si son Bingo est de gains.
        boolean bingoCardPlayed = pendingEffects.stream()
            .anyMatch(effect -> effect instanceof BingoEffect bingo && bingo.getSymbol() == null);
        TurnResult result = turnEngine.playTurn(gameState, pendingEffects);
        if (bingoCardPlayed && result.getGainBingoSymbol() != null) bingoGiftPending = true;
        pendingEffects.clear();
        betsThisTurn.clear();
        cardsPlayedThisTurn = 0;
        pendingChoice = null;
        handLocked    = false;
        doubleNext    = false;
        gameState.getPlayer().restoreCards(originals); // l'effet de l'Arc-en-ciel ne dure que le tour
        originals.clear();
        handHidden = false;
        // Le Scorbut resté en main disparaît : il ne revient que par la règle du lieu.
        for (Card card : new ArrayList<>(gameState.getPlayer().getCurrentHand())) {
            if (PlaceRule.SCURVY_CARD.equals(card.getId())) gameState.getPlayer().removeFromHand(card);
        }
        gameState.getPlayer().discardHand();
        return result;
    }

    // -------------------------------------------------------------------------
    // Lecture de l'état (pour GameScreen)
    // -------------------------------------------------------------------------

    /** @return l'état courant de la partie (joueur, ennemi, numéro de tour). */
    public GameState getGameState() { return gameState; }
}
