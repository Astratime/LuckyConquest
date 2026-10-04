package fr.astratime.lucky.progress;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ce que le joueur garde d'une partie à l'autre, enregistré dans un
 * {@link ProfileStorage} :
 * <ul>
 *   <li>sa collection : les cartes qu'il possède et leur nombre d'exemplaires
 *       (au départ, celles de assets/cards/decks/collection.json ; les coffres
 *       des donjons en ajoutent) ;</li>
 *   <li>son deck : exactement {@link #DECK_SIZE} cartes de sa collection, au
 *       plus {@link #MAX_COPIES} exemplaires de chacune (au départ, le deck de
 *       départ), joué dans tous les modes ;</li>
 *   <li>ses pièces : les gains acquis en combat, versés à la fin de chacun, et
 *       les pièces des coffres. Elles ne servent qu'à la boutique : on n'entre
 *       jamais en combat avec.</li>
 * </ul>
 */
public class PlayerProfile {

    /** Nombre de cartes d'un deck, ni plus ni moins. */
    public static final int DECK_SIZE   = 20;
    /** Exemplaires au plus d'une même carte, dans la collection comme dans le deck. */
    public static final int MAX_COPIES  = 3;
    /** Pièces données par chaque coffre de donjon. */
    public static final int CHEST_COINS = 10_000;
    /** Pièces données en plus quand la carte du coffre est déjà au maximum d'exemplaires. */
    public static final int DUPLICATE_COINS = 5_000;

    static final String KEY_COLLECTION = "collection";
    static final String KEY_DECK       = "deck";
    static final String KEY_COINS      = "coins";

    private final ProfileStorage       storage;
    private final Map<String, Integer> starterDeck;
    private final Map<String, Integer> collection = new LinkedHashMap<>();
    private final Map<String, Integer> deck       = new LinkedHashMap<>();
    private long coins;

    /**
     * Charge le profil enregistré dans {@code storage} ; au premier lancement
     * (ou si le deck enregistré n'est plus valide), le joueur a la collection
     * et le deck de départ.
     *
     * @param startingCollection cartes possédées au premier lancement, par id
     * @param starterDeck        deck de départ ({@link #DECK_SIZE} cartes de la collection), par id
     */
    public PlayerProfile(ProfileStorage storage, Map<String, Integer> startingCollection,
                         Map<String, Integer> starterDeck) {
        this.storage     = storage;
        this.starterDeck = new LinkedHashMap<>(starterDeck);
        String savedCollection = storage.get(KEY_COLLECTION);
        collection.putAll(savedCollection != null ? decode(savedCollection) : startingCollection);
        String savedDeck = storage.get(KEY_DECK);
        Map<String, Integer> loadedDeck = savedDeck != null ? decode(savedDeck) : starterDeck;
        deck.putAll(deckProblem(loadedDeck) == null ? loadedDeck : starterDeck);
        String savedCoins = storage.get(KEY_COINS);
        coins = parseCoins(savedCoins);
    }

    // -------------------------------------------------------------------------
    // Collection et deck
    // -------------------------------------------------------------------------

    /** @return les cartes possédées et leur nombre d'exemplaires, par id, dans l'ordre où elles ont été obtenues. */
    public Map<String, Integer> getCollection() { return Collections.unmodifiableMap(collection); }

    /** @return le nombre d'exemplaires possédés de la carte {@code id} (0 si aucun). */
    public int getOwnedCopies(String id) { return collection.getOrDefault(id, 0); }

    /** @return le deck du joueur : nombre d'exemplaires de chaque carte, par id. */
    public Map<String, Integer> getDeck() { return Collections.unmodifiableMap(deck); }

    /** @return le deck de départ, joué tant que le joueur n'a pas construit le sien. */
    public Map<String, Integer> getStarterDeck() { return Collections.unmodifiableMap(starterDeck); }

    /**
     * @return pourquoi {@code candidate} ne peut pas être le deck du joueur, ou
     *         {@code null} s'il est valide : exactement {@link #DECK_SIZE} cartes,
     *         toutes possédées, au plus {@link #MAX_COPIES} exemplaires de chacune
     */
    public String deckProblem(Map<String, Integer> candidate) {
        int total = 0;
        for (Map.Entry<String, Integer> entry : candidate.entrySet()) {
            int copies = entry.getValue();
            if (copies < 0) return "Nombre d'exemplaires négatif";
            if (copies > MAX_COPIES) return "Pas plus de " + MAX_COPIES + " exemplaires d'une carte";
            if (copies > getOwnedCopies(entry.getKey())) return "Carte pas assez possédée : " + entry.getKey();
            total += copies;
        }
        if (total != DECK_SIZE) return "Le deck doit faire " + DECK_SIZE + " cartes (" + total + " ici)";
        return null;
    }

    /**
     * Remplace le deck du joueur et l'enregistre.
     *
     * @throws IllegalArgumentException si {@code newDeck} n'est pas valide (voir {@link #deckProblem(Map)})
     */
    public void setDeck(Map<String, Integer> newDeck) {
        String problem = deckProblem(newDeck);
        if (problem != null) throw new IllegalArgumentException(problem);
        deck.clear();
        newDeck.forEach((id, copies) -> { if (copies > 0) deck.put(id, copies); });
        save();
    }

    // -------------------------------------------------------------------------
    // Pièces et coffres
    // -------------------------------------------------------------------------

    /** @return les pièces du joueur, pour la boutique. */
    public long getCoins() { return coins; }

    /** Ajoute {@code amount} pièces (rien si négatif) et enregistre le profil. */
    public void addCoins(long amount) {
        if (amount <= 0) return;
        coins += amount;
        save();
    }

    /**
     * Ouvre un coffre de donjon qui contient la carte {@code cardId} : un
     * exemplaire de plus dans la collection (au plus {@link #MAX_COPIES}) et
     * {@link #CHEST_COINS} pièces ; si la carte est déjà au maximum,
     * {@link #DUPLICATE_COINS} pièces en plus à la place. Le profil est enregistré.
     *
     * @return ce que le joueur a reçu
     */
    public ChestReward openChest(String cardId) {
        int owned = getOwnedCopies(cardId);
        boolean newCopy = owned < MAX_COPIES;
        int reward = CHEST_COINS + (newCopy ? 0 : DUPLICATE_COINS);
        if (newCopy) collection.put(cardId, owned + 1);
        coins += reward;
        save();
        return new ChestReward(cardId, newCopy, getOwnedCopies(cardId), reward);
    }

    /**
     * Contenu d'un coffre ouvert.
     *
     * @param cardId  la carte du coffre
     * @param newCopy {@code true} si un exemplaire a rejoint la collection
     * @param copies  exemplaires possédés de la carte après l'ouverture
     * @param coins   pièces reçues
     */
    public record ChestReward(String cardId, boolean newCopy, int copies, int coins) { }

    // -------------------------------------------------------------------------
    // Enregistrement
    // -------------------------------------------------------------------------

    /** Enregistre la collection, le deck et les pièces. */
    public void save() {
        storage.put(KEY_COLLECTION, encode(collection));
        storage.put(KEY_DECK, encode(deck));
        storage.put(KEY_COINS, String.valueOf(coins));
        storage.flush();
    }

    /** @return {@code copies} écrit "id:n,id:n" (dans l'ordre de la table). */
    static String encode(Map<String, Integer> copies) {
        StringBuilder text = new StringBuilder();
        copies.forEach((id, count) -> {
            if (count <= 0) return;
            if (text.length() > 0) text.append(',');
            text.append(id).append(':').append(count);
        });
        return text.toString();
    }

    /** @return la table écrite par {@link #encode(Map)} (les entrées illisibles sont ignorées). */
    static Map<String, Integer> decode(String text) {
        Map<String, Integer> copies = new LinkedHashMap<>();
        if (text.isBlank()) return copies;
        for (String entry : text.split(",")) {
            int colon = entry.lastIndexOf(':');
            if (colon <= 0) continue;
            try {
                int count = Integer.parseInt(entry.substring(colon + 1).trim());
                if (count > 0) copies.merge(entry.substring(0, colon).trim(), count, Integer::sum);
            } catch (NumberFormatException ignored) {
                // entrée abîmée : ignorée
            }
        }
        return copies;
    }

    /** @return {@code amount} écrit avec des espaces entre les milliers (ex : "10 000"). */
    public static String formatCoins(long amount) {
        String digits = String.valueOf(Math.abs(amount));
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && (digits.length() - i) % 3 == 0) text.append(' ');
            text.append(digits.charAt(i));
        }
        return (amount < 0 ? "-" : "") + text;
    }

    private static long parseCoins(String text) {
        if (text == null) return 0L;
        try {
            return Math.max(0L, Long.parseLong(text.trim()));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
