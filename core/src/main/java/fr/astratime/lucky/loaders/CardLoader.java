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

/**
 * Charge les cartes depuis les fichiers JSON dans assets/cards/definitions/.
 * Un fichier par suite (plus un pour les cartes spéciales) — format : tableau
 * d'objets carte, chaque carte ayant un id, un nom, un assetPath, une suite
 * optionnelle, un rang optionnel et une liste d'effets.
 *
 * La composition du deck de départ est décrite à part, dans
 * assets/cards/decks/starter.json : liste d'ids de cartes, chacun avec son
 * nombre d'exemplaires dans le deck ("copies", 1 si absent).
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
        "cards/definitions/special.json"
    };

    private static final String STARTER_DECK_FILE = "cards/decks/starter.json";

    /** Charge et retourne une instance de chaque carte définie (toutes suites et cartes spéciales). */
    public static List<Card> loadAll() {
        List<Card> cards = new ArrayList<>();
        for (JsonValue cardJson : loadDefinitions().values()) {
            cards.add(parseCard(cardJson));
        }
        Gdx.app.log("CardLoader", cards.size() + " cartes chargees.");
        return cards;
    }

    /**
     * Charge le deck de départ décrit par STARTER_DECK_FILE. Chaque exemplaire
     * est une instance de {@link Card} distincte, même pour une carte présente
     * en plusieurs exemplaires.
     *
     * @throws IllegalArgumentException si le deck référence un id de carte inconnu
     */
    public static List<Card> loadStarterDeck() {
        Map<String, JsonValue> definitions = loadDefinitions();
        List<Card> cards = new ArrayList<>();

        JsonValue root = new JsonReader().parse(Gdx.files.internal(STARTER_DECK_FILE));
        for (JsonValue entry = root.child; entry != null; entry = entry.next) {
            String id = entry.getString("id");
            JsonValue definition = definitions.get(id);
            if (definition == null) {
                throw new IllegalArgumentException("Carte inconnue dans " + STARTER_DECK_FILE + " : " + id);
            }
            int copies = entry.getInt("copies", 1);
            for (int i = 0; i < copies; i++) {
                cards.add(parseCard(definition));
            }
        }

        Gdx.app.log("CardLoader", "Deck de depart : " + cards.size() + " cartes.");
        return cards;
    }

    /** @return l'objet JSON de chaque carte définie, indexé par id (dans l'ordre des fichiers). */
    private static Map<String, JsonValue> loadDefinitions() {
        Map<String, JsonValue> definitions = new LinkedHashMap<>();
        JsonReader reader = new JsonReader();

        for (String path : DEFINITION_FILES) {
            JsonValue root = reader.parse(Gdx.files.internal(path));
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

        return new Card(id, name, assetPath, effects, suit, rank);
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
                return new DiamondReflectEffect(json.getInt("percent"));
            case "ACE_OF_DIAMONDS":
                return new AceOfDiamondsEffect();

            // --- Pique ---
            case "SPADE_IGNORE_DEFENSE":
                return new SpadeIgnoreDefenseEffect(json.getInt("attackBonus"));
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

            default:
                throw new IllegalArgumentException("Type d'effet inconnu dans le JSON : " + type);
        }
    }
}
