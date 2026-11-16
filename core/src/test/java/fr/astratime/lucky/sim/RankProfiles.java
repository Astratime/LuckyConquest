package fr.astratime.lucky.sim;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.progress.PlayerProfile;
import fr.astratime.lucky.progress.Rank;
import fr.astratime.lucky.progress.ReelShop;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

/**
 * Équipement réaliste d'un joueur à chaque rang (étape 2 du plan « Simulateur : le rendre fiable », 2026-11-16).
 *
 * <p>Le joueur vise un deck (le joueur malin ou un deck de couleur) mais ne joue que ce qu'il possède vraiment :
 * <ul>
 *   <li>sa collection de départ ({@code collection.json} : le deck de départ, Pioche +3, Aimant...) ;</li>
 *   <li>les cartes des coffres des lieux qu'il a vidés : tous les lieux sous le sien, et le sien aussi au second
 *       rang de la paire (Parieur, Habitué, Gros bonnet, Magnat, Légende). Il a fait assez de descentes pour en avoir
 *       {@link Card#UPGRADE_COST} exemplaires, donc il peut les fusionner, sauf celles de son propre lieu ;</li>
 *   <li>ce qu'il achète avec son budget de boutique : {@link #SHOP_SHARE} fois ce qu'il a payé pour ses rangs. Il
 *       achète d'abord le moins cher de ce qui lui manque : cartes de son deck visé (boutique), rouleaux
 *       ({@link #REEL_WISH}), fusions en « + » des cartes de son deck ;</li>
 *   <li>les rouleaux gagnés en vidant un lieu (Ticket, Ancre, Pépite, Jeton d'or, Flamme).</li>
 * </ul>
 * Les places vides du deck (cartes visées qu'il n'a pas) sont prises par les cartes du deck de départ, puis par ses
 * autres cartes de coffre. Sa machine garde 11 rouleaux : chaque rouleau qu'il possède remplace un rouleau classique
 * faible, dans l'ordre de {@link #REPLACED_CLASSIC}.
 *
 * <p>Ces règles sont un choix de Claude, à corriger par Astra : {@code -PshopShare=0.5} change le budget.
 */
final class RankProfiles {

    /** Budget de boutique : cette part de ce qu'il a payé pour ses rangs ({@code -PshopShare}, 1 par défaut). */
    static final double SHOP_SHARE = Double.parseDouble(System.getProperty("lucky.shopShare", "1"));

    /** Rouleaux qu'il met dans sa machine quand il les a, du préféré au moins utile. */
    static final List<Symbol> REEL_WISH = List.of(Symbol.SWORD, Symbol.STAR, Symbol.FANGS, Symbol.NUGGET, Symbol.FLAME,
        Symbol.ANCHOR, Symbol.GOLD_CHIP, Symbol.HORSESHOE, Symbol.ECU, Symbol.CROWN, Symbol.DIE, Symbol.TICKET);

    /** Rouleaux classiques qu'il retire d'abord (les plus faibles). Sept, Triple sept, Triple cerise, Lingot restent. */
    static final List<Symbol> REPLACED_CLASSIC = List.of(Symbol.BAR, Symbol.WATERMELON, Symbol.GRAPE, Symbol.CHERRY,
        Symbol.BELL, Symbol.DOUBLE_BAR, Symbol.DIAMOND);

    /** Rouleaux gagnés en vidant un lieu. */
    private static Symbol placeReel(Place place) {
        return place.getReelReward();
    }

    /**
     * Équipement d'un rang.
     *
     * @param deck    20 cartes, 1 exemplaire chacune (id de base)
     * @param plus    cartes du deck jouées en « + »
     * @param reels   les 11 rouleaux
     * @param budget  budget de boutique, en pièces
     * @param spent   ce qu'il a dépensé
     * @param bought  ce qu'il a acheté, dans l'ordre (cartes, rouleaux, fusions)
     * @param missing cartes visées qu'il n'a pas (ni coffre, ni assez de pièces)
     */
    record Profile(Map<String, Integer> deck, Set<String> plus, List<Symbol> reels, double budget, double spent,
                   List<String> bought, List<String> missing) {}

    private final Map<String, Long> boutique;
    private final List<String> starter;
    private final Map<String, Integer> collection;
    private final Function<String, Card> cards;

    RankProfiles(Map<String, Long> boutique, List<String> starter, Map<String, Integer> collection,
                 Function<String, Card> cards) {
        this.boutique = boutique;
        this.starter = starter;
        this.collection = collection;
        this.cards = cards;
    }

    /** @return le lieu associé au rang (voir {@link BalanceSimulator#placeOfRank}). */
    private static Place place(Rank rank) { return BalanceSimulator.placeOfRank(rank); }

    /** @return {@code true} pour le second rang de la paire de son lieu : il a déjà vidé ce lieu. */
    private static boolean secondOfPair(Rank rank) {
        return rank == Rank.PARIEUR || rank == Rank.HABITUE || rank == Rank.GROS_BONNET || rank == Rank.MAGNAT
            || rank == Rank.LEGENDE;
    }

    /** @return les lieux vidés à ce rang. */
    static List<Place> clearedPlaces(Rank rank) {
        List<Place> cleared = new ArrayList<>();
        if (rank == null) return cleared;
        Place own = place(rank);
        for (Place place : Place.values()) {
            if (place.ordinal() < own.ordinal() || place == own && secondOfPair(rank)) cleared.add(place);
        }
        return cleared;
    }

    /** @return ce qu'il a payé pour ses rangs jusqu'à {@code rank} compris. */
    static double rankSpending(Rank rank) {
        if (rank == null) return 0;
        double total = 0;
        for (Rank r : Rank.values()) if (r.ordinal() <= rank.ordinal()) total += r.getPrice();
        return total;
    }

    /** @return l'équipement à ce rang pour un joueur qui vise {@code target}. */
    Profile of(Rank rank, Map<String, Integer> target) {
        double budget = SHOP_SHARE * rankSpending(rank);
        List<Place> cleared = clearedPlaces(rank);
        // ce qu'il possède : id -> exemplaires
        Map<String, Integer> owned = new LinkedHashMap<>();
        owned.putAll(collection);
        for (String id : starter) owned.merge(id, 1, Math::max);
        List<String> lootIds = new ArrayList<>();
        for (Place place : cleared) {
            boolean farmed = rank != null && place.ordinal() < place(rank).ordinal();
            for (Dungeon dungeon : place.getDungeons()) {
                for (Dungeon.Loot loot : dungeon.getLoot()) {
                    owned.merge(loot.cardId(), farmed ? Card.UPGRADE_COST : 1, Math::max);
                    lootIds.add(loot.cardId());
                }
            }
        }
        Set<Symbol> reelsOwned = new LinkedHashSet<>();
        for (Place place : cleared) if (placeReel(place) != null) reelsOwned.add(placeReel(place));

        // achats, du moins cher au plus cher : cartes visées, rouleaux, fusions des cartes visées
        record Want(String label, DoubleSupplier cost, Runnable apply) {
            double price() { return cost.getAsDouble(); }
        }
        List<Want> wants = new ArrayList<>();
        Set<String> plus = new LinkedHashSet<>();
        for (String id : target.keySet()) {
            Long price = boutique.get(id);
            if (!owned.containsKey(id) && price != null) {
                wants.add(new Want(cards.apply(id).getName(), () -> price, () -> owned.put(id, 1)));
            }
            if (cards.apply(id).isUpgradable()) {
                // fusion : 3 exemplaires (achetés en boutique s'il en manque) + son prix
                wants.add(new Want(cards.apply(id).getName() + " +", () -> {
                    int copies = owned.getOrDefault(id, 0);
                    if (copies >= Card.UPGRADE_COST) return Card.UPGRADE_PRICE;
                    Long unit = boutique.get(id);
                    return unit == null || copies == 0 ? Double.POSITIVE_INFINITY
                        : Card.UPGRADE_PRICE + unit * (double) (Card.UPGRADE_COST - copies);
                }, () -> { owned.merge(id, Card.UPGRADE_COST, Math::max); plus.add(id); }));
            }
        }
        for (Map.Entry<Symbol, Long> reel : ReelShop.getPrices().entrySet()) {
            if (REEL_WISH.contains(reel.getKey()) && !reelsOwned.contains(reel.getKey())) {
                Symbol symbol = reel.getKey();
                long price = reel.getValue();
                wants.add(new Want(symbol.getDisplayName(), () -> price, () -> reelsOwned.add(symbol)));
            }
        }
        double spent = 0;
        List<String> bought = new ArrayList<>();
        // les prix des fusions dépendent des cartes déjà achetées : il repasse jusqu'à ne plus rien pouvoir acheter
        boolean progress = true;
        while (progress) {
            progress = false;
            wants.sort(Comparator.comparingDouble(Want::price));
            for (Want want : new ArrayList<>(wants)) {
                double price = want.price();
                if (Double.isInfinite(price) || spent + price > budget) continue;
                spent += price;
                want.apply().run();
                bought.add(want.label());
                wants.remove(want);
                progress = true;
                break;
            }
        }

        // le deck : les cartes visées qu'il possède, puis le deck de départ, puis ses autres cartes de coffre
        Map<String, Integer> deck = new LinkedHashMap<>();
        List<String> missing = new ArrayList<>();
        for (String id : target.keySet()) {
            if (owned.containsKey(id)) deck.put(id, 1);
            else missing.add(cards.apply(id).getName());
        }
        for (String id : starter) if (deck.size() < PlayerProfile.DECK_SIZE) deck.putIfAbsent(id, 1);
        for (String id : collection.keySet()) if (deck.size() < PlayerProfile.DECK_SIZE) deck.putIfAbsent(id, 1);
        for (String id : lootIds) if (deck.size() < PlayerProfile.DECK_SIZE) deck.putIfAbsent(id, 1);
        plus.retainAll(deck.keySet());
        // les cartes de coffre fusionnables qu'il joue : il les fusionne aussi s'il lui reste des pièces
        for (String id : deck.keySet()) {
            if (plus.contains(id) || !cards.apply(id).isUpgradable()) continue;
            if (owned.getOrDefault(id, 0) >= Card.UPGRADE_COST && spent + Card.UPGRADE_PRICE <= budget) {
                spent += Card.UPGRADE_PRICE;
                plus.add(id);
                bought.add(cards.apply(id).getName() + " +");
            }
        }

        // la machine : 11 rouleaux, ses rouleaux à la place des classiques faibles
        List<Symbol> reels = new ArrayList<>(Symbol.classicReels());
        int replaced = 0;
        for (Symbol symbol : REEL_WISH) {
            if (!reelsOwned.contains(symbol) || replaced >= REPLACED_CLASSIC.size()) continue;
            reels.set(reels.indexOf(REPLACED_CLASSIC.get(replaced++)), symbol);
        }
        return new Profile(deck, plus, List.copyOf(reels), budget, spent, bought, missing);
    }

    /** @return le profil en quelques lignes de Markdown, pour qu'Astra le relise. */
    String describe(String title, Profile profile) {
        StringBuilder out = new StringBuilder();
        out.append(String.format(Locale.ROOT, "### %s%n%nBudget boutique %s, dépensé %s.%n%n", title,
            BalanceSimulator.abbreviated(profile.budget()), BalanceSimulator.abbreviated(profile.spent())));
        List<String> names = new ArrayList<>();
        for (String id : profile.deck().keySet()) {
            names.add(cards.apply(id).getName() + (profile.plus().contains(id) ? " +" : ""));
        }
        out.append("- Deck : ").append(String.join(", ", names)).append('\n');
        List<String> reels = new ArrayList<>();
        for (Symbol symbol : profile.reels()) reels.add(symbol.getDisplayName());
        out.append("- Machine : ").append(String.join(", ", reels)).append('\n');
        if (!profile.bought().isEmpty()) out.append("- Achats : ").append(String.join(", ", profile.bought())).append('\n');
        if (!profile.missing().isEmpty()) out.append("- Manque : ").append(String.join(", ", profile.missing())).append('\n');
        return out.append('\n').toString();
    }
}
