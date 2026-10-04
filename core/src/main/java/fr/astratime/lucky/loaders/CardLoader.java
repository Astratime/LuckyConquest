package fr.astratime.lucky.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.effects.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
            cards.add(parseCard(cardJson));
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
            JsonValue definition = definitions.get(entry.getKey());
            if (definition == null) throw new IllegalArgumentException("Carte inconnue dans le deck : " + entry.getKey());
            for (int i = 0; i < entry.getValue(); i++) cards.add(parseCard(definition));
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
            JsonValue definition = definitions.get(id);
            if (definition == null) throw new IllegalArgumentException("Carte inconnue : " + id);
            return parseCard(definition);
        };
    }

    /** @return l'objet JSON de chaque carte définie, indexé par id (dans l'ordre des fichiers). */
    private static Map<String, JsonValue> loadDefinitions(AssetReader assetReader) {
        Map<String, JsonValue> definitions = new LinkedHashMap<>();
        JsonReader reader = new JsonReader();

        for (String path : DEFINITION_FILES) {
            JsonValue root = reader.parse(assetReader.read(path));
            for (JsonValue cardJson = root.child; cardJson != null; cardJson = cardJson.next) {
                definitions.put(cardJson.getString("id"), cardJson);
            }
        }
        return definitions;
    }

    /**
     * Construit une {@link Card} à partir de son objet JSON (id, name, assetPath,
     * rank optionnel, suit optionnelle, et la liste de ses effets).
     */
    private static Card parseCard(JsonValue json) {
        String id       = json.getString("id");
        String name     = json.getString("name");
        String assetPath = json.getString("assetPath");
        int    rank     = json.getInt("rank", 1);

        String suitStr  = json.getString("suit", null);
        Card.Suit suit  = suitStr != null ? Card.Suit.valueOf(suitStr) : null;

        List<Effect> effects = new ArrayList<>();
        JsonValue effectsJson = json.get("effects");
        if (effectsJson != null) {
            for (JsonValue effectJson = effectsJson.child; effectJson != null; effectJson = effectJson.next) {
                effects.add(parseEffect(effectJson));
            }
        }

        return new Card(id, name, assetPath, effects, suit, rank, json.getBoolean("consumable", false));
    }

    /**
     * Mappe un objet JSON d'effet vers l'instance Effect correspondante.
     * Chaque type correspond à une classe d'effet du package effects.
     */
    private static Effect parseEffect(JsonValue json) {
        String type = json.getString("type");
        switch (type) {
            // --- Coeur ---
            case "HEART_DRAIN":
                return new HeartDrainEffect(json.getInt("percent"));
            case "ACE_OF_HEARTS":
                return new AceOfHeartsEffect();

            // --- Trefle ---
            case "CLUB_GAIN_ATTACK":
                return new ClubGainAttackEffect(
                    json.getInt("gainMultiplier"),
                    json.getInt("attackBonus")
                );
            case "ACE_OF_CLUBS":
                return new AceOfClubsEffect();

            // --- Carreau ---
            case "DIAMOND_REFLECT":
                return new DiamondReflectEffect(json.getInt("percent"), json.getInt("defenseBoost", 0));
            case "ACE_OF_DIAMONDS":
                return new AceOfDiamondsEffect();

            // --- Pique ---
            case "SPADE_IGNORE_DEFENSE":
                return new SpadeIgnoreDefenseEffect(json.getInt("attackBonus"), json.getInt("blades", 1));
            case "ACE_OF_SPADES":
                return new AceOfSpadesEffect();

            // --- Effets génériques ---
            case "BOOST_SYMBOL":
                return new BoostSymbolEffect(
                    Symbol.valueOf(json.getString("symbol")),
                    json.getInt("amount")
                );
            case "ATTACK":
                return new AttackEffect(json.getInt("bonus"));
            case "DEFENSE":
                return new DefenseEffect(json.getInt("bonus"));
            case "EXTRA_DRAW":
                return new ExtraDrawEffect(json.getInt("extraCards"));
            case "GAIN":
                return new GainEffect(json.getInt("amount"));
            case "MULTIPLIER":
                return new MultiplierEffect(json.getFloat("amount"));

            // --- Cartes de casino ---
            case "BINGO":
                return new BingoEffect(json.getInt("power"),
                    json.has("symbol") ? Symbol.valueOf(json.getString("symbol")) : null);
            case "MAGNET":
                return new MagnetEffect(json.getInt("percent"));
            case "RECYCLE":
                return new RecycleEffect(json.getInt("turns"));
            case "BET":
                return new BetEffect();
            case "RUSSIAN_ROULETTE":
                return new RussianRouletteEffect(json.getInt("multiplier"), json.getInt("cursedMultiplier"),
                    json.getInt("penaltyPercent"));
            case "EXTRA_PLAYS":
                return new ExtraPlaysEffect(json.getInt("plays"), json.getInt("turns"));
            case "LUCKY_CHARM":
                return new LuckyCharmEffect(json.getInt("percent"));
            case "RAINBOW":
                return new RainbowEffect(json.getString("card"));
            case "GAINS_MULTIPLIER":
                return new GainsMultiplierEffect(json.getInt("percent"), json.getInt("gauges", 1));
            case "CORRUPTION":
                return new CorruptionEffect(json.getInt("turns"));

            // --- Cartes de la boutique ---
            case "REROLL":
                return new RerollEffect();
            case "RIGGED_REEL":
                return new RiggedReelEffect();
            case "GHOST_REEL":
                return new ForceReelEffect(Symbol.JOKER);
            case "RANK_TOKEN":
                return new RankTokenEffect();
            case "ALL_IN":
                return new AllInEffect();
            case "SAFE":
                return new SafeEffect(json.getInt("percent"), json.getInt("turns"));
            case "INSURANCE":
                return new InsuranceEffect(json.getInt("percent"));
            case "BRIBE":
                return new BribeEffect();
            case "DOUBLE_OR_NOTHING":
                return new DoubleOrNothingEffect();
            case "OVERHEAT":
                return new OverheatEffect(json.getInt("hpPercent"));

            // --- Cartes des donjons (Exploration) ---
            case "BLADES_ATTACK":
                return new BladesAttackEffect(json.getInt("blades"), json.getInt("attackBonus"));
            case "SHADOW_DAGGER":
                return new ShadowDaggerEffect(json.getInt("attackPerBlade"));
            case "GUILLOTINE":
                return new GuillotineEffect(json.getInt("percentPerBlade"));
            case "FOUR_LEAF_CLOVER":
                return new FourLeafCloverEffect(json.getInt("gainMultiplier"), json.getInt("gainBoost"));
            case "FORTUNE":
                return new FortuneEffect(json.getInt("gainFactor", 1), json.getInt("attackPercent"));
            case "TRANSFUSION":
                return new TransfusionEffect(json.getInt("percent"));
            case "BLOOD_PACT":
                return new BloodPactEffect(json.getInt("hpPercent"), json.getFloat("attackFactor"));
            case "RAMPART":
                return new RampartEffect(json.getInt("shield"), json.getInt("defenseBoost"));
            case "GUARANTEED_REFLECT":
                return new GuaranteedReflectEffect(json.getInt("percent"));
            case "ROUGH_DIAMOND":
                return new RoughDiamondEffect(json.getInt("counter"));

            // ----- Lieux de l'Exploration -----
            case "SCURVY":
                return new ScurvyEffect();
            case "SPYGLASS":
                return new SpyglassEffect();
            case "TREASURE_MAP":
                return new TreasureMapEffect();
            case "HELMET":
                return new HelmetEffect();
            case "GOLD_VEIN":
                return new GoldVeinEffect(json.getInt("turns"));
            case "BUBBLE":
                return new BubbleEffect(json.getInt("turns"));
            case "BLACK_PEARL":
                return new ForceReelEffect(ForceReelEffect.LEFT_REEL, Symbol.JOKER);
            case "TRIDENT":
                return new TridentEffect();

            default:
                throw new IllegalArgumentException("Type d'effet inconnu dans le JSON : " + type);
        }
    }
}
