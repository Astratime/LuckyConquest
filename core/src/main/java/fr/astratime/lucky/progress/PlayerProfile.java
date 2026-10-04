package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.RankBonus;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.Place;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
 *   <li>son {@link Rank rang}, acheté à la boutique, rang après rang ;</li>
 *   <li>ses rouleaux : les 11 classiques et ceux achetés à la boutique
 *       ({@link ReelShop}) ; sa machine en compte exactement {@link Symbol#MACHINE_SIZE},
 *       tous différents (au départ, les 11 classiques).</li>
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
    static final String KEY_RANK       = "rank";
    static final String KEY_REELS      = "reels";
    static final String KEY_MACHINE    = "machine";
    static final String KEY_DUNGEONS   = "dungeons";

    private final ProfileStorage       storage;
    private final Map<String, Integer> starterDeck;
    private final Map<String, Integer> collection = new LinkedHashMap<>();
    private final Map<String, Integer> deck       = new LinkedHashMap<>();
    private long coins;
    /** Nombre de rangs achetés (0 : aucun rang). */
    private int  ranks;
    /** Rouleaux achetés à la boutique, dans l'ordre d'achat. */
    private final List<Symbol> boughtReels = new ArrayList<>();
    /** Rouleaux de la machine du joueur. */
    private final List<Symbol> machine     = new ArrayList<>();
    /** Donjons de l'Exploration vidés (leur chef battu), par nom. */
    private final java.util.Set<String> clearedDungeons = new java.util.LinkedHashSet<>();

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
        ranks = Math.max(0, Math.min(Rank.values().length, (int) parseCoins(storage.get(KEY_RANK))));
        for (Symbol symbol : decodeSymbols(storage.get(KEY_REELS))) {
            if (!symbol.isClassic() && symbol != Symbol.JOKER && !boughtReels.contains(symbol)) boughtReels.add(symbol);
        }
        List<Symbol> savedMachine = decodeSymbols(storage.get(KEY_MACHINE));
        machine.addAll(machineProblem(savedMachine) == null ? savedMachine : Symbol.classicReels());
        String savedDungeons = storage.get(KEY_DUNGEONS);
        if (savedDungeons != null) {
            for (String name : savedDungeons.split(",")) if (!name.isBlank()) clearedDungeons.add(name.trim());
        } else {
            // Profil d'avant les nouveaux lieux : un donjon dont on possède une carte a été vidé.
            for (Dungeon dungeon : Place.PRAIRIE.getDungeons()) {
                if (dungeon.getLoot().stream().anyMatch(loot -> collection.containsKey(loot.cardId()))) {
                    clearedDungeons.add(dungeon.name());
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Exploration
    // -------------------------------------------------------------------------

    /**
     * Le chef du donjon {@code dungeon} (son nom) est battu : le donjon est vidé,
     * pour toujours. Le dernier donjon d'un lieu qui a un rouleau en récompense
     * (les Mines d'Or) donne ce rouleau.
     *
     * @return le rouleau gagné à l'instant, ou {@code null}
     */
    public Symbol clearDungeon(String dungeon) {
        if (!clearedDungeons.add(dungeon)) return null;
        Symbol earned = null;
        for (Place place : Place.values()) {
            Symbol reel = place.getReelReward();
            if (reel == null || ownsReel(reel) || !place.getDungeons().stream().allMatch(this::isCleared)) continue;
            boughtReels.add(reel);
            earned = reel;
        }
        save();
        return earned;
    }

    /** @return {@code true} si le donjon {@code dungeon} a déjà été vidé. */
    public boolean isCleared(Dungeon dungeon) { return clearedDungeons.contains(dungeon.name()); }

    /** @return {@code true} si le lieu {@code place} est ouvert : le premier, ou tous les donjons du précédent vidés. */
    public boolean isOpen(Place place) {
        Place previous = place.getPrevious();
        return previous == null || previous.getDungeons().stream().allMatch(this::isCleared);
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

    /**
     * Pièces versées à la fin d'un combat de l'Exploration : les gains affichés
     * dans le cadre « Gains », seulement après la victoire contre le roi du
     * donjon. Une défaite ne rapporte rien, le soldat non plus : ses gains sont
     * gardés pour le roi. L'Entraînement et la Tour des épreuves ne rapportent
     * aucune pièce.
     *
     * @param victory   l'ennemi est vaincu
     * @param lastFight c'était le dernier combat du donjon (le roi)
     * @param gains     les gains du joueur à la fin du combat
     * @return les pièces à ajouter au profil
     */
    public static int combatReward(boolean victory, boolean lastFight, int gains) {
        return victory && lastFight ? Math.max(0, gains) : 0;
    }

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
    // Boutique : rangs, cartes, rouleaux
    // -------------------------------------------------------------------------

    /** @return le rang du joueur, ou {@code null} s'il n'en a acheté aucun. */
    public Rank getRank() { return ranks == 0 ? null : Rank.values()[ranks - 1]; }

    /** @return le prochain rang à acheter, ou {@code null} s'il les a tous. */
    public Rank getNextRank() { return ranks >= Rank.values().length ? null : Rank.values()[ranks]; }

    /** @return les bonus de son rang en combat ({@link RankBonus#NONE} sans rang). */
    public RankBonus getRankBonus() { return ranks == 0 ? RankBonus.NONE : getRank().getBonus(); }

    /**
     * Achète le rang suivant (les rangs s'achètent dans l'ordre) et enregistre le profil.
     *
     * @return {@code false} si tous les rangs sont achetés ou si les pièces manquent
     */
    public boolean buyNextRank() {
        Rank next = getNextRank();
        if (next == null || coins < next.getPrice()) return false;
        coins -= next.getPrice();
        ranks++;
        save();
        return true;
    }

    /**
     * Achète un exemplaire de la carte {@code id} pour {@code price} pièces : il
     * rejoint la collection (au plus {@link #MAX_COPIES}), et peut ensuite entrer
     * dans le deck. Le profil est enregistré.
     *
     * @return {@code false} si la carte est déjà au maximum ou si les pièces manquent
     */
    public boolean buyCard(String id, long price) {
        int owned = getOwnedCopies(id);
        if (owned >= MAX_COPIES || price < 0 || coins < price) return false;
        coins -= price;
        collection.put(id, owned + 1);
        save();
        return true;
    }

    /** @return les rouleaux possédés : les 11 classiques, puis ceux achetés, dans l'ordre d'achat. */
    public List<Symbol> getOwnedReels() {
        List<Symbol> owned = new ArrayList<>(Symbol.classicReels());
        owned.addAll(boughtReels);
        return Collections.unmodifiableList(owned);
    }

    /** @return {@code true} si le joueur possède le rouleau {@code symbol}. */
    public boolean ownsReel(Symbol symbol) { return symbol.isClassic() || boughtReels.contains(symbol); }

    /**
     * Achète le rouleau {@code symbol} pour {@code price} pièces, puis enregistre le profil.
     *
     * @return {@code false} s'il est déjà possédé, si c'est le Joker ou si les pièces manquent
     */
    public boolean buyReel(Symbol symbol, long price) {
        if (symbol == Symbol.JOKER || ownsReel(symbol) || price < 0 || coins < price) return false;
        coins -= price;
        boughtReels.add(symbol);
        save();
        return true;
    }

    /** @return les rouleaux de la machine du joueur, dans l'ordre. */
    public List<Symbol> getMachine() { return Collections.unmodifiableList(machine); }

    /**
     * @return pourquoi {@code candidate} ne peut pas être la machine du joueur, ou
     *         {@code null} si elle est valide : exactement {@link Symbol#MACHINE_SIZE}
     *         rouleaux possédés, tous différents, sans le Joker
     */
    public String machineProblem(List<Symbol> candidate) {
        if (new HashSet<>(candidate).size() != candidate.size()) return "Un rouleau ne peut être mis qu'une fois";
        for (Symbol symbol : candidate) {
            if (symbol == null || symbol == Symbol.JOKER) return "Le Joker ne se met pas dans la machine";
            if (!ownsReel(symbol)) return "Rouleau pas possédé : " + symbol.getDisplayName();
        }
        if (candidate.size() != Symbol.MACHINE_SIZE) {
            return "La machine doit avoir " + Symbol.MACHINE_SIZE + " rouleaux (" + candidate.size() + " ici)";
        }
        return null;
    }

    /**
     * Remplace la machine du joueur et l'enregistre.
     *
     * @throws IllegalArgumentException si {@code newMachine} n'est pas valide (voir {@link #machineProblem(List)})
     */
    public void setMachine(List<Symbol> newMachine) {
        String problem = machineProblem(newMachine);
        if (problem != null) throw new IllegalArgumentException(problem);
        machine.clear();
        machine.addAll(newMachine);
        save();
    }

    // -------------------------------------------------------------------------
    // Enregistrement
    // -------------------------------------------------------------------------

    /** Enregistre la collection, le deck, les pièces, le rang, les rouleaux et les donjons vidés. */
    public void save() {
        storage.put(KEY_COLLECTION, encode(collection));
        storage.put(KEY_DECK, encode(deck));
        storage.put(KEY_COINS, String.valueOf(coins));
        storage.put(KEY_RANK, String.valueOf(ranks));
        storage.put(KEY_REELS, encodeSymbols(boughtReels));
        storage.put(KEY_MACHINE, encodeSymbols(machine));
        storage.put(KEY_DUNGEONS, String.join(",", clearedDungeons));
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

    /** @return {@code symbols} écrits "NOM,NOM". */
    static String encodeSymbols(List<Symbol> symbols) {
        return String.join(",", symbols.stream().map(Symbol::name).toList());
    }

    /** @return les symboles écrits par {@link #encodeSymbols(List)} (les noms inconnus sont ignorés). */
    static List<Symbol> decodeSymbols(String text) {
        List<Symbol> symbols = new ArrayList<>();
        if (text == null || text.isBlank()) return symbols;
        for (String name : text.split(",")) {
            try {
                symbols.add(Symbol.valueOf(name.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
                // nom abîmé : ignoré
            }
        }
        return symbols;
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
