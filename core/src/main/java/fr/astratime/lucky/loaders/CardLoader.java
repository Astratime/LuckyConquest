package fr.astratime.lucky.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.effects.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * Charge les cartes depuis les fichiers JSON dans assets/cards/definitions/.
 * Un fichier par suite (plus un pour les cartes spéciales) — format : tableau
 * d'objets carte, chaque carte ayant un id, un nom, un assetPath, une suite
 * optionnelle, un rang optionnel et une liste d'effets.
 *
 * La composition du deck de départ (20 cartes, joué tant que le joueur n'a
 * pas construit le sien) est décrite à part, dans assets/cards/decks/starter.json,
 * et les cartes que le joueur possède au premier lancement dans
 * assets/cards/decks/collection.json : liste d'ids de cartes, chacun avec son
 * nombre d'exemplaires ("copies", 1 si absent).
 *
 * Chaque carte qui a au moins une valeur à améliorer a aussi sa version « + »
 * (id suivi de « + », voir {@link Card#upgradedId}) : les mêmes effets, valeurs x1,5.
 *
 * Ajouter une nouvelle carte = ajouter une entrée dans le JSON correspondant.
 * Ajouter un nouveau type d'effet = ajouter un case dans parseEffect().
 *
 * Doit être appelé après l'initialisation de libGDX (Gdx.files disponible).
 */
public class CardLoader {

    private static final String[] DEFINITION_FILES = {
        "cards/definitions/coeur.json",
        "cards/definitions/trefle.json",
        "cards/definitions/carreau.json",
        "cards/definitions/pique.json",
        "cards/definitions/special.json",
        "cards/definitions/donjons.json",
        "cards/definitions/lieux.json",
        "cards/definitions/archetypes.json",
        "cards/definitions/test.json"
    };

    private static final String STARTER_DECK_FILE = "cards/decks/starter.json";
    private static final String COLLECTION_FILE   = "cards/decks/collection.json";
    private static final String SHOP_FILE         = "cards/decks/shop.json";
    private static final String BOUTIQUE_FILE     = "cards/decks/boutique.json";

    /** Lit le contenu texte d'un fichier d'assets à partir de son chemin (ex : "cards/decks/starter.json"). */
    public interface AssetReader {
        String read(String path);
    }

    /** Lecture par défaut : fichiers internes de libGDX (nécessite libGDX initialisé). */
    private static final AssetReader GDX_READER = path -> Gdx.files.internal(path).readString("UTF-8");

    /** Charge et retourne une instance de chaque carte définie (toutes suites et cartes spéciales). */
    public static List<Card> loadAll() {
        List<Card> cards = loadAll(GDX_READER);
        Gdx.app.log("CardLoader", cards.size() + " cartes chargees.");
        return cards;
    }

    /** Comme {@link #loadAll()}, en lisant les fichiers avec {@code reader} (ex : tests sans libGDX). */
    public static List<Card> loadAll(AssetReader reader) {
        List<Card> cards = new ArrayList<>();
        for (JsonValue cardJson : loadDefinitions(reader).values()) {
            cards.add(parseCard(cardJson, false));
        }
        return cards;
    }

    /**
     * Charge le deck de départ décrit par STARTER_DECK_FILE : celui du joueur
     * tant qu'il n'a pas construit le sien (voir {@link #loadDeck(Map)}).
     *
     * @throws IllegalArgumentException si le deck référence un id de carte inconnu
     */
    public static List<Card> loadStarterDeck() {
        List<Card> cards = loadStarterDeck(GDX_READER);
        Gdx.app.log("CardLoader", "Deck de depart : " + cards.size() + " cartes.");
        return cards;
    }

    /** Comme {@link #loadStarterDeck()}, en lisant les fichiers avec {@code reader} (ex : tests sans libGDX). */
    public static List<Card> loadStarterDeck(AssetReader reader) {
        return loadDeck(loadCopies(reader, STARTER_DECK_FILE), reader);
    }

    /** @return le nombre d'exemplaires de chaque carte du deck de départ, par id. */
    public static Map<String, Integer> loadStarterDeckCopies() {
        return loadCopies(GDX_READER, STARTER_DECK_FILE);
    }

    /** Comme {@link #loadStarterDeckCopies()}, en lisant les fichiers avec {@code reader}. */
    public static Map<String, Integer> loadStarterDeckCopies(AssetReader reader) {
        return loadCopies(reader, STARTER_DECK_FILE);
    }

    /**
     * @return les cartes que possède le joueur au premier lancement (décrites par
     *         COLLECTION_FILE) : nombre d'exemplaires par id, dans l'ordre du fichier
     */
    public static Map<String, Integer> loadStartingCollection() {
        return loadCopies(GDX_READER, COLLECTION_FILE);
    }

    /** Comme {@link #loadStartingCollection()}, en lisant les fichiers avec {@code reader}. */
    public static Map<String, Integer> loadStartingCollection(AssetReader reader) {
        return loadCopies(reader, COLLECTION_FILE);
    }

    /**
     * Crée les cartes d'un deck : chaque exemplaire est une instance de
     * {@link Card} distincte, même pour une carte présente en plusieurs exemplaires.
     *
     * @param copies nombre d'exemplaires de chaque carte, par id
     * @throws IllegalArgumentException si un id de carte est inconnu
     */
    public static List<Card> loadDeck(Map<String, Integer> copies) {
        return loadDeck(copies, GDX_READER);
    }

    /** Comme {@link #loadDeck(Map)}, en lisant les fichiers avec {@code reader} (ex : tests sans libGDX). */
    public static List<Card> loadDeck(Map<String, Integer> copies, AssetReader reader) {
        Map<String, JsonValue> definitions = loadDefinitions(reader);
        List<Card> cards = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : copies.entrySet()) {
            if (parseCard(definitions, entry.getKey()) == null) {
                throw new IllegalArgumentException("Carte inconnue dans le deck : " + entry.getKey());
            }
            for (int i = 0; i < entry.getValue(); i++) cards.add(parseCard(definitions, entry.getKey()));
        }
        return cards;
    }

    /** @return le nombre d'exemplaires de chaque carte du fichier {@code path} ("copies", 1 si absent), par id. */
    private static Map<String, Integer> loadCopies(AssetReader reader, String path) {
        Map<String, Integer> copies = new LinkedHashMap<>();
        JsonValue root = new JsonReader().parse(reader.read(path));
        for (JsonValue entry = root.child; entry != null; entry = entry.next) {
            copies.merge(entry.getString("id"), entry.getInt("copies", 1), Integer::sum);
        }
        return copies;
    }

    /** @return le prix de chaque carte proposée à l'échoppe, par id, dans l'ordre du fichier {@code SHOP_FILE}. */
    public static Map<String, Integer> loadShop() {
        return loadShop(GDX_READER);
    }

    /** Comme {@link #loadShop()}, en lisant les fichiers avec {@code reader} (ex : tests sans libGDX). */
    public static Map<String, Integer> loadShop(AssetReader reader) {
        Map<String, Integer> shop = new LinkedHashMap<>();
        JsonValue root = new JsonReader().parse(reader.read(SHOP_FILE));
        for (JsonValue entry = root.child; entry != null; entry = entry.next) {
            shop.put(entry.getString("id"), entry.getInt("price"));
        }
        return shop;
    }

    /**
     * @return le prix en pièces de chaque carte vendue à la boutique (onglet
     *         « Cartes »), par id, dans l'ordre du fichier {@code BOUTIQUE_FILE}
     */
    public static Map<String, Long> loadBoutique() {
        return loadBoutique(GDX_READER);
    }

    /** Comme {@link #loadBoutique()}, en lisant les fichiers avec {@code reader} (ex : tests sans libGDX). */
    public static Map<String, Long> loadBoutique(AssetReader reader) {
        Map<String, Long> boutique = new LinkedHashMap<>();
        JsonValue root = new JsonReader().parse(reader.read(BOUTIQUE_FILE));
        for (JsonValue entry = root.child; entry != null; entry = entry.next) {
            boutique.put(entry.getString("id"), entry.getLong("price"));
        }
        return boutique;
    }

    /**
     * @return une fabrique de cartes : pour un id, une nouvelle instance de la
     *         carte définie (ex : carte créée en cours de combat)
     * @throws IllegalArgumentException (à l'appel de la fabrique) pour un id inconnu
     */
    public static Function<String, Card> cardFactory() {
        return cardFactory(GDX_READER);
    }

    /** Comme {@link #cardFactory()}, en lisant les fichiers avec {@code reader} (ex : tests sans libGDX). */
    public static Function<String, Card> cardFactory(AssetReader reader) {
        Map<String, JsonValue> definitions = loadDefinitions(reader);
        return id -> {
            Card card = parseCard(definitions, id);
            if (card == null) throw new IllegalArgumentException("Carte inconnue : " + id);
            return card;
        };
    }

    /**
     * Définitions déjà lues, par lecteur : les fichiers ne sont analysés qu'une fois, au
     * lieu de l'être à chaque combat (les cartes, elles, restent créées neuves à chaque
     * fois). Les objets JSON ne sont que lus ensuite, jamais modifiés.
     */
    private static final Map<AssetReader, Map<String, JsonValue>> DEFINITIONS_CACHE =
        Collections.synchronizedMap(new WeakHashMap<>());

    /** @return l'objet JSON de chaque carte définie, indexé par id (dans l'ordre des fichiers). */
    private static Map<String, JsonValue> loadDefinitions(AssetReader assetReader) {
        Map<String, JsonValue> cached = DEFINITIONS_CACHE.get(assetReader);
        if (cached != null) return cached;
        Map<String, JsonValue> definitions = readDefinitions(assetReader);
        DEFINITIONS_CACHE.put(assetReader, definitions);
        return definitions;
    }

    private static Map<String, JsonValue> readDefinitions(AssetReader assetReader) {
        Map<String, JsonValue> definitions = new LinkedHashMap<>();
        JsonReader reader = new JsonReader();

        for (String path : DEFINITION_FILES) {
            JsonValue root = reader.parse(assetReader.read(path));
            for (JsonValue cardJson = root.child; cardJson != null; cardJson = cardJson.next) {
                definitions.put(cardJson.getString("id"), cardJson);
            }
        }
        return Collections.unmodifiableMap(definitions);
    }

    /**
     * Construit une {@link Card} à partir de son objet JSON (id, name, assetPath,
     * rank optionnel, suit optionnelle, et la liste de ses effets).
     *
     * @param upgraded {@code true} pour sa version « + » (id suivi de {@link Card#UPGRADE_SUFFIX}, valeurs +50 %)
     */
    private static Card parseCard(JsonValue json, boolean upgraded) {
        String id       = json.getString("id");
        String name     = json.getString("name");
        String assetPath = json.getString("assetPath");
        // Une carte d'archétype sans rang écrit n'en a pas (0) : elle ne compte pas pour les combinaisons.
        int    rank     = json.getInt("rank", json.has("archetype") ? 0 : 1);

        String suitStr  = json.getString("suit", null);
        Card.Suit suit  = suitStr != null ? Card.Suit.valueOf(suitStr) : null;

        List<Effect> effects = new ArrayList<>();
        int improved = 0;
        JsonValue effectsJson = json.get("effects");
        if (effectsJson != null) {
            for (JsonValue effectJson = effectsJson.child; effectJson != null; effectJson = effectJson.next) {
                Values values = new Values(effectJson, upgraded);
                effects.add(parseEffect(effectJson, values));
                improved += values.reads;
            }
        }

        String archetype = json.getString("archetype", null);
        return new Card(upgraded ? Card.upgradedId(id) : id, name, assetPath, effects, suit, rank,
            json.getBoolean("consumable", false), upgraded, improved > 0,
            archetype != null ? fr.astratime.lucky.entities.Archetype.valueOf(archetype) : null);
    }

    /** @return la carte {@code id} (sa version « + » si {@code id} finit par « + »), ou {@code null} si elle n'existe pas */
    private static Card parseCard(Map<String, JsonValue> definitions, String id) {
        JsonValue definition = definitions.get(Card.baseId(id));
        if (definition == null) return null;
        if (!Card.isUpgradedId(id)) return parseCard(definition, false);
        Card upgraded = parseCard(definition, true);
        return upgraded.isUpgradable() ? upgraded : null;
    }

    /** Fabrique d'un effet à partir de son objet JSON et de ses valeurs (améliorées pour une carte « + »). */
    @FunctionalInterface
    private interface EffectFactory {
        Effect create(JsonValue json, Values v);
    }

    /**
     * Fabrique de chaque type d'effet écrit dans le JSON ({@code "type"}) : chaque type
     * correspond à une classe d'effet du package effects. Les valeurs lues avec
     * {@code v} sont celles qu'améliore la version « + » de la carte.
     */
    private static final Map<String, EffectFactory> EFFECTS = effectFactories();

    private static Map<String, EffectFactory> effectFactories() {
        Map<String, EffectFactory> effects = new HashMap<>();
        suitEffects(effects);
        genericEffects(effects);
        casinoEffects(effects);
        shopEffects(effects);
        dungeonEffects(effects);
        placeEffects(effects);
        placeChestEffects(effects);
        archetypeEffects(effects);
        return Collections.unmodifiableMap(effects);
    }

    private static void add(Map<String, EffectFactory> effects, String type, EffectFactory factory) {
        if (effects.put(type, factory) != null) throw new IllegalStateException("Type d'effet en double : " + type);
    }

    /** Effets des cartes de chaque couleur. */
    private static void suitEffects(Map<String, EffectFactory> effects) {
        // --- Coeur ---
        add(effects, "HEART_DRAIN", (json, v) -> new HeartDrainEffect(v.i("percent")));
        add(effects, "ACE_OF_HEARTS", (json, v) -> new AceOfHeartsEffect(v.power()));

        // --- Trefle ---
        add(effects, "CLUB_GAIN_ATTACK", (json, v) -> new ClubGainAttackEffect(
            v.i("gainMultiplier"),
            v.i("attackBonus")
        ));
        add(effects, "ACE_OF_CLUBS", (json, v) -> new AceOfClubsEffect(v.power()));

        // --- Carreau ---
        add(effects, "DIAMOND_REFLECT", (json, v) -> new DiamondReflectEffect(v.f("hpPercent"), v.i("defenseBoost", 0)));
        add(effects, "ACE_OF_DIAMONDS", (json, v) -> new AceOfDiamondsEffect(v.power()));

        // --- Pique ---
        add(effects, "SPADE_IGNORE_DEFENSE", (json, v) -> new SpadeIgnoreDefenseEffect(v.i("attackBonus"), v.i("blades", 1),
            Math.min(100, v.i("piercePercent"))));
        add(effects, "ACE_OF_SPADES", (json, v) -> new AceOfSpadesEffect(v.power()));
    }

    /** Effets génériques, partagés par plusieurs cartes. */
    private static void genericEffects(Map<String, EffectFactory> effects) {
        add(effects, "BOOST_SYMBOL", (json, v) -> new BoostSymbolEffect(
            Symbol.valueOf(json.getString("symbol")),
            v.i("amount")
        ));
        add(effects, "ATTACK", (json, v) -> new AttackEffect(v.i("bonus")));
        add(effects, "DEFENSE", (json, v) -> new DefenseEffect(v.i("bonus")));
        add(effects, "EXTRA_DRAW", (json, v) -> new ExtraDrawEffect(v.i("extraCards")));
        add(effects, "GAIN", (json, v) -> new GainEffect(v.i("amount")));
        add(effects, "MULTIPLIER", (json, v) -> new MultiplierEffect(v.f("amount")));
    }

    /** Cartes de casino. */
    private static void casinoEffects(Map<String, EffectFactory> effects) {
        add(effects, "BINGO", (json, v) -> new BingoEffect(json.getInt("power"),
            json.has("symbol") ? Symbol.valueOf(json.getString("symbol")) : null,
            json.getBoolean("bonusGame", false)));
        add(effects, "MAGNET", (json, v) -> new MagnetEffect(v.i("percent")));
        add(effects, "RECYCLE", (json, v) -> new RecycleEffect(v.i("turns")));
        add(effects, "BET", (json, v) -> new BetEffect());
        add(effects, "RUSSIAN_ROULETTE", (json, v) -> new RussianRouletteEffect(json.getInt("percent"), json.getInt("cursedPercent"),
            json.getInt("penaltyPercent")));
        add(effects, "EXTRA_PLAYS", (json, v) -> new ExtraPlaysEffect(v.i("plays"), json.getInt("turns")));
        add(effects, "LUCKY_CHARM", (json, v) -> new LuckyCharmEffect(v.i("percent")));
        add(effects, "RAINBOW", (json, v) -> new RainbowEffect(v.id(json.getString("card"))));
        add(effects, "SPIN_GAINS", (json, v) -> new SpinGainsEffect(v.i("spins"), json.getInt("gauges", 1)));
        add(effects, "GAINS_MULTIPLIER", (json, v) -> new GainsMultiplierEffect(v.i("percent"), json.getInt("gauges", 1)));
        add(effects, "CORRUPTION", (json, v) -> new CorruptionEffect(json.getInt("turns")));
    }

    /** Cartes de la boutique. */
    private static void shopEffects(Map<String, EffectFactory> effects) {
        add(effects, "REROLL", (json, v) -> new RerollEffect());
        add(effects, "RIGGED_REEL", (json, v) -> new RiggedReelEffect());
        add(effects, "GHOST_REEL", (json, v) -> new ForceReelEffect(Symbol.JOKER));
        add(effects, "RANK_TOKEN", (json, v) -> new RankTokenEffect());
        add(effects, "ALL_IN", (json, v) -> new AllInEffect());
        add(effects, "SAFE", (json, v) -> new SafeEffect(v.i("percent"), json.getInt("turns")));
        add(effects, "INSURANCE", (json, v) -> new InsuranceEffect(v.lower("percent")));
        add(effects, "BRIBE", (json, v) -> new BribeEffect());
        add(effects, "DOUBLE_OR_NOTHING", (json, v) -> new DoubleOrNothingEffect());
        add(effects, "OVERHEAT", (json, v) -> new OverheatEffect(json.getInt("hpPercent")));
    }

    /** Cartes des donjons (Exploration). */
    private static void dungeonEffects(Map<String, EffectFactory> effects) {
        add(effects, "BLADES_ATTACK", (json, v) -> new BladesAttackEffect(v.i("blades"), v.i("attackBonus")));
        add(effects, "SHADOW_DAGGER", (json, v) -> new ShadowDaggerEffect(v.i("attackPerBlade")));
        add(effects, "GUILLOTINE", (json, v) -> new GuillotineEffect(v.i("percentPerBlade")));
        add(effects, "FOUR_LEAF_CLOVER", (json, v) -> new FourLeafCloverEffect(v.i("gainMultiplier"), v.i("gainBoost")));
        add(effects, "FORTUNE", (json, v) -> new FortuneEffect(json.has("gainPercent") ? v.i("gainPercent") : 0, v.i("attackPercent"),
            json.has("attackCapSpins") ? v.i("attackCapSpins") : 0));
        add(effects, "TRANSFUSION", (json, v) -> new TransfusionEffect(v.i("percent")));
        add(effects, "BLOOD_PACT", (json, v) -> new BloodPactEffect(json.getInt("hpPercent"), v.f("attackFactor")));
        add(effects, "RAMPART", (json, v) -> new RampartEffect(v.i("shield"), v.i("defenseBoost")));
        add(effects, "GUARANTEED_REFLECT", (json, v) -> new GuaranteedReflectEffect(v.i("percent")));
        add(effects, "ROUGH_DIAMOND", (json, v) -> new RoughDiamondEffect(v.i("counter")));
    }

    /** Cartes des lieux de l'Exploration. */
    private static void placeEffects(Map<String, EffectFactory> effects) {
        add(effects, "SCURVY", (json, v) -> new ScurvyEffect());
        add(effects, "SPYGLASS", (json, v) -> new SpyglassEffect());
        add(effects, "TREASURE_MAP", (json, v) -> new TreasureMapEffect());
        add(effects, "HELMET", (json, v) -> new HelmetEffect());
        add(effects, "GOLD_VEIN", (json, v) -> new GoldVeinEffect(v.i("turns")));
        add(effects, "BUBBLE", (json, v) -> new BubbleEffect(v.i("turns")));
        add(effects, "BLACK_PEARL", (json, v) -> new ForceReelEffect(ForceReelEffect.LEFT_REEL, Symbol.JOKER));
        add(effects, "TRIDENT", (json, v) -> new TridentEffect());
    }

    /** Cartes des coffres des donjons des lieux. */
    private static void placeChestEffects(Map<String, EffectFactory> effects) {
        add(effects, "RAT_TRAP", (json, v) -> new RatTrapEffect());
        add(effects, "GAINS_PERCENT", (json, v) -> new GainsPercentEffect(v.i("percent")));
        add(effects, "COMBO_BONUS", (json, v) -> new ComboBonusEffect(v.f("bonus")));
        add(effects, "BEST_OF_TWO", (json, v) -> new BestOfTwoEffect());
        add(effects, "POWDER_KEG", (json, v) -> new PowderKegEffect(v.i("factor"), json.getInt("hpPercent")));
        add(effects, "LANTERN", (json, v) -> new LanternEffect(v.i("draws"), v.i("defensePercent")));
        add(effects, "DAZZLE", (json, v) -> new DazzleEffect());
        add(effects, "CUTLASS", (json, v) -> new CutlassEffect(v.i("attackPercent")));
        add(effects, "BLACK_FLAG", (json, v) -> new BlackFlagEffect());
        add(effects, "MUTINY", (json, v) -> new MutinyEffect());
        add(effects, "SIEVE", (json, v) -> new SieveEffect(v.i("gainsPercent")));
        add(effects, "PROP", (json, v) -> new PropEffect(v.i("turns"), v.i("defensePercent")));
        add(effects, "ROPE", (json, v) -> new RopeEffect());
        add(effects, "FORGE_HAMMER", (json, v) -> new ForgeHammerEffect());
        add(effects, "TEMPER", (json, v) -> new TemperEffect(v.i("percent")));
        add(effects, "FORGED_BLADE", (json, v) -> new ForgedBladeEffect());
        add(effects, "DYNAMITE", (json, v) -> new DynamiteEffect(v.i("percent")));
        add(effects, "CARBIDE_LAMP", (json, v) -> new CarbideLampEffect());
        add(effects, "GOLDEN_HEART", (json, v) -> new GoldenHeartEffect());
        add(effects, "EARPLUGS", (json, v) -> new EarplugsEffect(v.i("draws")));
        add(effects, "BONUS_PLAYS", (json, v) -> new BonusPlaysEffect(v.i("plays")));
        add(effects, "LOADED_COIN", (json, v) -> new LoadedCoinEffect());
        add(effects, "RUSTY_LEVER", (json, v) -> new RustyLeverEffect());
        add(effects, "SUNKEN_JACKPOT", (json, v) -> new SunkenJackpotEffect());
        add(effects, "SHARK_CAGE", (json, v) -> new SharkCageEffect(v.i("turns")));
        add(effects, "HARPOON", (json, v) -> new HarpoonEffect(v.i("attackPercent")));
        add(effects, "ANCHOR", (json, v) -> new AnchorEffect(v.i("turns"), v.i("defensePercent")));
    }

    /** Cartes des archétypes de deck. */
    private static void archetypeEffects(Map<String, EffectFactory> effects) {
        add(effects, "WILD_JOKER", (json, v) -> new WildJokerEffect(v.i("attackPercent")));
        add(effects, "JOKER_LAUGH", (json, v) -> new JokerLaughEffect(v.i("extraCards")));
        add(effects, "FOOL_CROWN", (json, v) -> new FoolCrownEffect(v.i("factor")));
        add(effects, "JOKER_WORKSHOP", (json, v) -> new JokerWorkshopEffect());
        add(effects, "ARCHETYPE_STATS", (json, v) -> new ArchetypeStatsEffect(v.i("attack", 0), v.i("defense", 0), v.f("gains", 0f)));
        add(effects, "WILD_CARD", (json, v) -> new WildCardEffect());
        add(effects, "LEPRECHAUN_HAT", (json, v) -> new LeprechaunHatEffect(json.getInt("turns"), v.i("percent")));
        add(effects, "LEPRECHAUN_JIG", (json, v) -> new LeprechaunJigEffect(v.lower("spinsPerBlade"), v.i("maxBlades")));
        add(effects, "RAINBOWS_END", (json, v) -> new RainbowsEndEffect(v.id(json.getString("card"))));
        add(effects, "COBBLER_HAMMER", (json, v) -> new CobblerHammerEffect(v.i("attackPercent"), v.i("gainsPercent")));
        add(effects, "LEPRECHAUN_PRANK", (json, v) -> new LeprechaunPrankEffect(v.i("spins"), v.i("blades")));
        add(effects, "CLAIMS", (json, v) -> new ClaimsEffect(v.i("count")));
        add(effects, "VARIABLE_RATE", (json, v) -> new VariableRateEffect(v.i("perSymbol")));
        add(effects, "COMPOUND_INTEREST", (json, v) -> new CompoundInterestEffect(json.getInt("turns"), v.i("percent")));
        add(effects, "DEBT_COLLECTION", (json, v) -> new DebtCollectionEffect(v.f("hpPercent"), v.i("spins")));
        add(effects, "ANNUITY", (json, v) -> new AnnuityEffect(json.getInt("turns"), json.getInt("claims"), v.i("spins")));
        add(effects, "INTIMIDATION", (json, v) -> new IntimidationEffect(v.i("percent")));
        add(effects, "MARKED_CARDS", (json, v) -> new MarkedCardsEffect(v.i("turns")));
        add(effects, "RED_HERRING", (json, v) -> new RedHerringEffect());
        add(effects, "SLEIGHT_OF_HAND", (json, v) -> new SleightOfHandEffect());
        add(effects, "BLUFF_CALL", (json, v) -> new BluffCallEffect(v.i("percent"), v.i("maxPercent")));
        add(effects, "HELD_REEL", (json, v) -> new HeldReelEffect());
        add(effects, "MACHINE_MEMORY", (json, v) -> new MachineMemoryEffect(v.i("amount")));
        add(effects, "CALIBRATION", (json, v) -> new CalibrationEffect(json.getInt("turns"), v.i("percent")));
        add(effects, "SECOND_LEVER", (json, v) -> new SecondLeverEffect(v.i("tries")));
        add(effects, "JACK_IN_THE_BOX", (json, v) -> new JackInTheBoxEffect(v.i("step"), v.i("max")));
        add(effects, "KNOCKDOWN", (json, v) -> new KnockdownEffect());
        add(effects, "PHARAOH_SEAL", (json, v) -> new PharaohSealEffect(v.i("claims"), json.getInt("turns")));
        add(effects, "DRAGON_HOARD", (json, v) -> new DragonHoardEffect(json.getInt("turns"), v.i("spins"), v.i("step"), v.i("max")));
        add(effects, "MARTINGALE", (json, v) -> new MartingaleEffect(v.i("attackPercent"), v.i("pairPercent")));
    }

    /** @return l'effet décrit par {@code json} ({@code v} : ses valeurs, améliorées pour une carte « + ») */
    private static Effect parseEffect(JsonValue json, Values v) {
        String type = json.getString("type");
        EffectFactory factory = EFFECTS.get(type);
        if (factory == null) throw new IllegalArgumentException("Type d'effet inconnu dans le JSON : " + type);
        return factory.create(json, v);
    }

    /**
     * Lit les valeurs d'un effet qu'améliore la version « + » d'une carte :
     * multipliées par {@link Card#UPGRADE_FACTOR} pour une carte « + », telles
     * quelles sinon. Compte celles qu'il a lues : une carte n'a de version « + »
     * que si au moins une de ses valeurs s'améliore.
     */
    private static final class Values {
        private final JsonValue json;
        private final boolean   upgraded;
        private int             reads;

        Values(JsonValue json, boolean upgraded) {
            this.json     = json;
            this.upgraded = upgraded;
        }

        float factor() { return upgraded ? Card.UPGRADE_FACTOR : 1f; }

        /** Valeur entière ; une carte « + » prend {@code key + "Plus"} s'il est écrit, sinon la valeur x1,5 arrondie. */
        int i(String key) {
            reads++;
            if (upgraded && json.has(key + "Plus")) return json.getInt(key + "Plus");
            return Math.round(json.getInt(key) * factor());
        }

        int i(String key, int fallback) { reads++; return Math.round(json.getInt(key, fallback) * factor()); }

        float f(String key) { return f(key, json.getFloat(key)); }

        /** Valeur décimale, arrondie au dixième pour une carte « + » (3,25 % devient 4,9 %). */
        float f(String key, float fallback) {
            reads++;
            float value = json.getFloat(key, fallback);
            return upgraded ? Math.round(value * factor() * 10f) / 10f : value;
        }

        /** Une valeur dont la plus petite est la meilleure (ex : PV perdus au plus) : divisée par le facteur. */
        int lower(String key) { reads++; return Math.round(json.getInt(key) / factor()); }

        /** La puissance d'un effet sans valeur écrite (les As) : 1 ou le facteur. */
        float power() { reads++; return factor(); }

        /** Une carte créée par l'effet : sa version « + » pour une carte « + ». */
        String id(String cardId) { reads++; return upgraded ? Card.upgradedId(cardId) : cardId; }
    }
}
