package fr.astratime.lucky.sim;

import fr.astratime.lucky.controllers.GameController;
import fr.astratime.lucky.entities.BonusGame;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.RankBonus;
import fr.astratime.lucky.entities.SpinEconomy;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolOutcome;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.choices.BetChoice;
import fr.astratime.lucky.entities.choices.CardChoice;
import fr.astratime.lucky.entities.choices.RiggedReelChoice;
import fr.astratime.lucky.entities.choices.RouletteChoice;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GainsEarnedEvent;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.loaders.CardLoader;
import fr.astratime.lucky.progress.PlayerProfile;
import fr.astratime.lucky.progress.Rank;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Simulateur d'équilibrage : un robot simple joue des milliers de tours avec le
 * vrai moteur du jeu, et le rapport (Markdown) donne les chiffres à surveiller
 * après chaque changement de règles.
 *
 * <ol>
 *   <li><b>Tirages</b> : ce que rapporte un tirage, en coûts de tirage (gains de base :
 *       symboles, Paire et Bingo ; objectif 0 à 5 sans carte), la part
 *       des Paires et des Bingos, et du temps passé en dette ;</li>
 *   <li><b>Jeu bonus</b> : ses gains moyens, en coûts de tirage (objectif ≈ 100 sur la machine classique) ;</li>
 *   <li><b>Donjons</b> : victoires et pièces gagnées (au roi) par donjon, selon le rang du joueur ;</li>
 *   <li><b>Joueur au maximum</b> : les donjons du Port, des Mines et du Casino avec le
 *       meilleur équipement (cartes « + », rouleaux rares, échoppe) et les plus hauts rangs ;</li>
 *   <li><b>Progression</b> : le premier rang qui gagne chaque donjon, et le nombre de
 *       victoires qu'il faut pour payer chaque rang ;</li>
 *   <li><b>Pièces par rang</b> : ce que rapporte une descente en moyenne (gains, coffre, part
 *       gardée après une défaite) dans chaque donjon, à chaque rang.</li>
 * </ol>
 *
 * Le robot joue ses cartes au hasard, sans Mise. Avec le deck de départ (ou aucune
 * carte, pour les gains de base), il n'achète rien à l'échoppe ; avec un équipement
 * « au maximum », il y achète Corruption, Bingo et Roulette russe dès qu'il peut.
 * Il joue moins bien qu'un vrai joueur : les chiffres comparent deux versions du
 * jeu entre elles, ils ne prédisent pas une partie.
 *
 * Lancement : {@code ./gradlew :core:simulate} (rapport dans
 * {@code core/build/simulation/equilibrage.md}) ; {@code -Pquick} pour un essai rapide.
 */
public final class BalanceSimulator {

    /** Nombre de tours ou de parties de chaque mesure. */
    record Sizes(int spins, int bonusGames, int dungeonRuns, int maxTurns) {
        static final Sizes FULL  = new Sizes(20_000, 100_000, 100, 300);
        static final Sizes QUICK = new Sizes(500, 2_000, 2, 60);
    }

    /**
     * Équipement du robot.
     *
     * @param deck        exemplaires de chaque carte (version « + » quand elle existe),
     *                    ou {@code null} pour le deck de départ
     * @param reels       sa machine
     * @param buysAtStall {@code true} s'il achète à l'échoppe pendant le combat
     */
    record Gear(String name, Map<String, Integer> deck, List<Symbol> reels, boolean buysAtStall) {
        static final Gear STARTER = new Gear("Deck de départ", null, Symbol.classicReels(), false);
    }

    /** Les 11 rouleaux rares (boutique et Mine) : la machine la plus chère. */
    static final List<Symbol> RARE_REELS = List.of(Symbol.HORSESHOE, Symbol.ECU, Symbol.SWORD, Symbol.HEART,
        Symbol.DIE, Symbol.STAR, Symbol.BOMB, Symbol.CROWN, Symbol.NUGGET, Symbol.GOLD_BAR, Symbol.TRIPLE_CHERRY);

    /** Decks de 20 cartes (3 exemplaires au plus, la version « + » compte à part), tous en version « + ». */
    private static final Map<String, Map<String, Integer>> STRONG_DECKS = new LinkedHashMap<>();
    static {
        STRONG_DECKS.put("Piques", deck("1_pique", 3, "13_pique", 3, "12_pique", 3, "pierre_a_aiguiser", 3,
            "dague_de_l_ombre", 2, "guillotine", 2, "bribe", 2, "draw_3", 2));
        STRONG_DECKS.put("As et Rois", deck("1_pique", 2, "1_trefle", 2, "1_coeur", 2, "1_carreau", 2,
            "13_pique", 2, "13_trefle", 2, "13_coeur", 2, "in_the_sleeve", 2, "draw_3", 2, "bribe", 2));
        STRONG_DECKS.put("Gains", deck("1_trefle", 3, "13_trefle", 3, "trefle_a_quatre_feuilles", 3,
            "fortune_du_roi", 3, "cocktail_des_abysses", 2, "chope", 2, "lucky_charm", 2, "draw_3", 2));
    }

    /** Cartes que le robot « au maximum » achète à l'échoppe, dans cet ordre. */
    private static final List<String> STALL_PICKS = List.of("corruption", "bingo", "russian_roulette");
    /** Rangs du joueur « au maximum ». */
    private static final List<Rank> TOP_RANKS = List.of(Rank.FLAMBEUR, Rank.REQUIN, Rank.MAGNAT, Rank.ROI_DU_CASINO,
        Rank.LEGENDE);
    /** Rang du tableau qui compare les équipements un par un. */
    private static final Rank GEAR_TABLE_RANK = Rank.MAGNAT;
    /** Part de victoires à partir de laquelle un donjon compte comme gagnable. */
    private static final double WINNABLE = 0.5;

    private static Map<String, Integer> deck(Object... idsAndCopies) {
        Map<String, Integer> deck = new LinkedHashMap<>();
        for (int i = 0; i < idsAndCopies.length; i += 2) deck.put((String) idsAndCopies[i], (Integer) idsAndCopies[i + 1]);
        return deck;
    }

    private static final long SEED = 42L;
    /** Rangs comparés pour les donjons (aucun, puis quelques paliers). */
    private static final List<Rank> RANKS = new ArrayList<>(java.util.Arrays.asList(
        null, Rank.AVARE, Rank.FLAMBEUR, Rank.REQUIN, Rank.MAGNAT));

    private final Sizes sizes;
    private final CardLoader.AssetReader reader;
    private final Function<String, Card> cards;
    private final Map<String, Integer> stall;
    /** Descentes déjà simulées, par donjon, rang et équipement (les sections 3, 4 et 5 en partagent). */
    private final Map<String, DungeonStats> dungeonCache = new ConcurrentHashMap<>();
    /** Les descentes elles-mêmes (deck de départ), pour comparer des règles de pièces (section 7). */
    private final Map<String, List<Run>> runCache = new ConcurrentHashMap<>();

    /**
     * Une descente.
     *
     * @param won    soldat et roi battus
     * @param gains  gains à la fin du dernier combat joué
     * @param turns  tours joués (soldat + roi)
     * @param effort part du donjon faite, de 0 à 1 : la moitié pour le soldat, la moitié pour le roi,
     *               chacun au prorata des PV qu'il a perdus
     */
    record Run(boolean won, double gains, int turns, double effort) {}

    BalanceSimulator(Path assets, Sizes sizes) {
        this.sizes = sizes;
        this.reader = path -> {
            try {
                return Files.readString(assets.resolve(path), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
        this.cards = CardLoader.cardFactory(reader);
        Map<String, Integer> shop = CardLoader.loadShop(reader);
        this.stall = new LinkedHashMap<>();
        for (String id : STALL_PICKS) stall.put(id, Objects.requireNonNull(shop.get(id), id));
    }

    public static void main(String[] args) throws IOException {
        boolean quick = List.of(args).contains("--quick");
        Path assets = Paths.get(System.getProperty("lucky.assets", "../assets")).toAbsolutePath().normalize();
        String report = new BalanceSimulator(assets, quick ? Sizes.QUICK : Sizes.FULL).report();
        System.out.println(report);
        Path out = Paths.get(System.getProperty("lucky.simulation.out", "build/simulation/equilibrage.md"));
        if (out.getParent() != null) Files.createDirectories(out.getParent());
        Files.writeString(out, report, StandardCharsets.UTF_8);
        System.out.println("Rapport écrit dans " + out.toAbsolutePath());
    }

    /** @return le rapport complet, en Markdown. */
    String report() {
        StringBuilder out = new StringBuilder("# Simulation d'équilibrage\n\n");
        out.append(String.format(Locale.ROOT, "%d tirages par combat, %d Jeux bonus, %d descentes par donjon et par rang.%n%n",
            sizes.spins(), sizes.bonusGames(), sizes.dungeonRuns()));
        simulateDungeons();
        spinSection(out);
        bonusSection(out);
        dungeonSection(out);
        topGearSection(out);
        progressionSection(out);
        coinsByRankSection(out);
        policySection(out);
        return out.toString();
    }

    /** Simule d'avance, en parallèle, toutes les descentes des sections 3 à 5 (gardées en cache). */
    private void simulateDungeons() {
        List<Runnable> jobs = new ArrayList<>();
        for (Place place : Place.values()) {
            for (Dungeon dungeon : place.getDungeons()) {
                for (Rank rank : allRanks()) {
                    jobs.add(() -> dungeon(dungeon, place, rank, Gear.STARTER, sizes.dungeonRuns(), SEED));
                }
                if (place == Place.PRAIRIE) continue;
                for (Rank rank : TOP_RANKS) {
                    for (Gear gear : topGears()) {
                        jobs.add(() -> dungeon(dungeon, place, rank, gear, sizes.dungeonRuns(), SEED));
                    }
                }
            }
        }
        jobs.parallelStream().forEach(Runnable::run);
    }

    // -------------------------------------------------------------------------
    // 1. Tirages
    // -------------------------------------------------------------------------

    /** Ce que rapportent les tirages d'un combat. */
    record SpinStats(double average, double median, double pairRate, double bingoRate, double debtRate) {}

    private void spinSection(StringBuilder out) {
        out.append("## 1. Tirages\n\n")
            .append("Gains d'un tirage (symboles, Paire, Bingo), en coûts de tirage. Objectif d'Astra : 0 à 5 « sans carte » "
                + "(gains de base). Avec le deck, les combinaisons et les cartes multiplient les gains.\n\n")
            .append("| Combat | Coût | Cartes | Moyenne | Médiane | Paires | Bingos | En dette |\n")
            .append("|---|---:|---|---:|---:|---:|---:|---:|\n");
        Map<String, EnemyKind> fights = new LinkedHashMap<>();
        fights.put("Entraînement", EnemyKind.ENTRAINEMENT);
        for (Place place : Place.values()) fights.put(place.getShortName(), place.getDungeons().get(0).getSoldier());
        fights.put("Tour ch. 1", EnemyKind.CROUPIER);
        fights.put("Tour ch. 3", EnemyKind.GARDIENNE);
        fights.put("Tour ch. 6", EnemyKind.GARDIEN_LEVIER);
        for (Map.Entry<String, EnemyKind> fight : fights.entrySet()) {
            for (boolean playCards : new boolean[] {false, true}) {
                SpinStats stats = spins(fight.getValue(), playCards, sizes.spins(), SEED);
                out.append(String.format(Locale.ROOT, "| %s | %d | %s | %.2f | %.2f | %.1f %% | %.2f %% | %.1f %% |%n",
                    fight.getKey(), fight.getValue().getSpinCost(), playCards ? "deck de départ" : "sans carte",
                    stats.average(), stats.median(), stats.pairRate() * 100, stats.bingoRate() * 100,
                    stats.debtRate() * 100));
            }
        }
        out.append('\n');
    }

    /**
     * @param playCards {@code false} : le robot tire sans jouer de carte (gains de base)
     * @return les tirages de {@code turns} tours contre {@code kind} (le combat recommence quand il se termine)
     */
    SpinStats spins(EnemyKind kind, boolean playCards, int turns, long seed) {
        Random random = new Random(seed);
        GameController game = controller(RankBonus.NONE, Gear.STARTER);
        game.restart(kind);
        int cost = kind.getSpinCost();
        List<Double> ratios = new ArrayList<>();
        double sum = 0;
        int pairs = 0, bingos = 0, debt = 0;
        for (int turn = 0; turn < turns; turn++) {
            if (isOver(game.getGameState())) game.restart(kind);
            TurnResult result = playTurn(game, random, playCards, false);
            if (game.getDebt() != SpinEconomy.Debt.NONE) debt++;
            double gains = result.getGainsFromPairOrJackpot();
            for (SymbolOutcome outcome : result.getSymbolOutcomes()) {
                for (Event event : outcome.getEvents()) if (event instanceof GainsEarnedEvent earned) gains += earned.amount;
            }
            if (result.isJackpot()) bingos++;
            else if (result.isPair()) pairs++;
            double ratio = gains / (double) cost;
            ratios.add(ratio);
            sum += ratio;
        }
        Collections.sort(ratios);
        return new SpinStats(sum / turns, ratios.get(turns / 2), pairs / (double) turns, bingos / (double) turns,
            debt / (double) turns);
    }

    // -------------------------------------------------------------------------
    // 2. Jeu bonus
    // -------------------------------------------------------------------------

    private void bonusSection(StringBuilder out) {
        out.append("## 2. Jeu bonus\n\n")
            .append("Gains d'un Jeu bonus, en coûts de tirage (part fixe, sans le bonus multiplié). Objectif d'Astra : "
                + "≈ 100 sur la machine classique.\n\n")
            .append("| Machine | Moyenne | Médiane | 9 sur 10 sous | Sans ligne |\n")
            .append("|---|---:|---:|---:|---:|\n");
        bonusRow(out, "Classique", Symbol.classicReels());
        bonusRow(out, "Rouleaux rares", List.of(Symbol.HORSESHOE, Symbol.ECU, Symbol.SWORD, Symbol.HEART, Symbol.DIE,
            Symbol.STAR, Symbol.BOMB, Symbol.CROWN, Symbol.TRIPLE_SEVEN, Symbol.GOLD_BAR, Symbol.TRIPLE_CHERRY));
        out.append('\n');
    }

    private void bonusRow(StringBuilder out, String name, List<Symbol> reels) {
        double[] totals = bonusGames(reels, sizes.bonusGames(), SEED);
        double sum = 0;
        int empty = 0;
        for (double total : totals) {
            sum += total;
            if (total == 0) empty++;
        }
        out.append(String.format(Locale.ROOT, "| %s | %.2f | %.2f | %.2f | %.0f %% |%n", name, sum / totals.length,
            totals[totals.length / 2], totals[totals.length * 9 / 10], empty * 100.0 / totals.length));
    }

    /** @return les gains de {@code games} Jeux bonus sur la machine {@code reels}, en coûts de tirage, triés. */
    static double[] bonusGames(List<Symbol> reels, int games, long seed) {
        Random random = new Random(seed);
        double[] totals = new double[games];
        for (int i = 0; i < games; i++) totals[i] = BonusGame.play(reels, 100, random).getTotal() / 100.0;
        java.util.Arrays.sort(totals);
        return totals;
    }

    // -------------------------------------------------------------------------
    // 3. Donjons
    // -------------------------------------------------------------------------

    /**
     * Descentes d'un donjon.
     *
     * @param winRate      part de victoires (soldat puis roi)
     * @param medianCoins  pièces médianes d'une victoire, sans le coffre (les gains au roi)
     * @param averageCoins pièces moyennes d'une descente, défaites comprises : gains et coffre
     *                     après une victoire, {@link PlayerProfile#DEFEAT_KEEP_PERCENT} % des gains après une défaite
     * @param averageWin   pièces moyennes d'une victoire, coffre compris
     * @param averageLoss  pièces moyennes d'une défaite
     */
    record DungeonStats(double winRate, long medianCoins, double averageCoins, double averageWin, double averageLoss,
                        double averageTurns) {
        /** @return les pièces moyennes par tour joué (la rentabilité en temps). */
        double coinsPerTurn() { return averageTurns == 0 ? 0 : averageCoins / averageTurns; }
    }

    private void dungeonSection(StringBuilder out) {
        out.append("## 3. Donjons\n\n")
            .append("Victoires (soldat puis roi) et pièces médianes d'une victoire, sans le coffre. Deck de départ, sans échoppe.\n\n")
            .append("| Donjon |");
        for (Rank rank : RANKS) out.append(' ').append(rank == null ? "Sans rang" : rank.getTitle()).append(" |");
        out.append("\n|---|");
        for (int i = 0; i < RANKS.size(); i++) out.append("---:|");
        out.append('\n');
        for (Place place : Place.values()) {
            for (Dungeon dungeon : place.getDungeons()) {
                out.append("| ").append(place.getShortName()).append(" : ").append(dungeon.getShortName()).append(" |");
                for (Rank rank : RANKS) {
                    DungeonStats stats = dungeon(dungeon, place, rank, sizes.dungeonRuns(), SEED);
                    out.append(String.format(Locale.ROOT, " %.0f %% / %s |", stats.winRate() * 100,
                        stats.winRate() == 0 ? "-" : PlayerProfile.formatCoins(stats.medianCoins())));
                }
                out.append('\n');
            }
        }
        out.append('\n');
    }

    /** @return {@code runs} descentes du donjon {@code dungeon} avec le rang {@code rank} ({@code null} : aucun). */
    DungeonStats dungeon(Dungeon dungeon, Place place, Rank rank, int runs, long seed) {
        return dungeon(dungeon, place, rank, Gear.STARTER, runs, seed);
    }

    /** Comme {@link #dungeon(Dungeon, Place, Rank, int, long)}, avec l'équipement {@code gear}. */
    DungeonStats dungeon(Dungeon dungeon, Place place, Rank rank, Gear gear, int runs, long seed) {
        String key = dungeon.name() + '/' + rank + '/' + gear.name() + '/' + runs + '/' + seed;
        DungeonStats cached = dungeonCache.get(key);
        if (cached != null) return cached;
        Random random = new Random(seed);
        GameController game = controller(rank == null ? RankBonus.NONE : rank.getBonus(), gear);
        game.setPlaceRule(place.getRule());
        List<Long> coins = new ArrayList<>();
        long chest = PlayerProfile.chestCoins(place.getSpinCost());
        double winTotal = 0, lossTotal = 0;
        int[] turns = {0};
        List<Run> played = new ArrayList<>();
        for (int run = 0; run < runs; run++) {
            int before = turns[0];
            game.restart(dungeon.getSoldier());
            boolean won = fight(game, random, gear, turns);
            double effort = 0.5 * (1 - Math.max(0, game.getGameState().getEnemy().getHpRatio()));
            if (won) {
                game.startCombat(dungeon.getKing());
                won = fight(game, random, gear, turns);
                effort = 0.5 + 0.5 * (1 - Math.max(0, game.getGameState().getEnemy().getHpRatio()));
            }
            played.add(new Run(won, game.getGameState().getPlayer().getGains(), turns[0] - before, won ? 1 : effort));
            long reward = PlayerProfile.combatReward(won, true, game.getGameState().getPlayer().getGains(),
                place.getSpinCost(), effort);
            if (!won) {
                lossTotal += reward;
                continue;
            }
            coins.add(reward);
            winTotal += reward + chest;
        }
        Collections.sort(coins);
        int wins = coins.size(), losses = runs - wins;
        DungeonStats stats = new DungeonStats(wins / (double) runs,
            coins.isEmpty() ? 0 : coins.get(coins.size() / 2), (winTotal + lossTotal) / runs,
            wins == 0 ? 0 : winTotal / wins, losses == 0 ? 0 : lossTotal / losses, turns[0] / (double) runs);
        dungeonCache.put(key, stats);
        runCache.put(key, played);
        return stats;
    }

    /**
     * Joue le combat en cours jusqu'au bout (au plus {@link Sizes#maxTurns} tours), en comptant ses tours dans {@code turns[0]}.
     *
     * @return {@code true} si gagné
     */
    private boolean fight(GameController game, Random random, Gear gear, int[] turns) {
        for (int turn = 0; turn < sizes.maxTurns() && !isOver(game.getGameState()); turn++) {
            playTurn(game, random, true, gear.buysAtStall());
            turns[0]++;
        }
        return game.getGameState().getEnemy().isDefeated();
    }

    // -------------------------------------------------------------------------
    // 4. Joueur au maximum
    // -------------------------------------------------------------------------

    /** @return les équipements « au maximum » : chaque deck fort, sur la machine classique et sur les rouleaux rares. */
    static List<Gear> topGears() {
        List<Gear> gears = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> deck : STRONG_DECKS.entrySet()) {
            gears.add(new Gear(deck.getKey() + " +, classique", deck.getValue(), Symbol.classicReels(), true));
            gears.add(new Gear(deck.getKey() + " +, rares", deck.getValue(), RARE_REELS, true));
        }
        return gears;
    }

    /** Meilleur équipement pour un donjon et un rang. */
    record BestGear(Gear gear, DungeonStats stats) {}

    private BestGear best(Dungeon dungeon, Place place, Rank rank) {
        BestGear best = null;
        for (Gear gear : topGears()) {
            DungeonStats stats = dungeon(dungeon, place, rank, gear, sizes.dungeonRuns(), SEED);
            if (best == null || stats.winRate() > best.stats().winRate()) best = new BestGear(gear, stats);
        }
        return best;
    }

    private void topGearSection(StringBuilder out) {
        List<Gear> gears = topGears();
        out.append("## 4. Joueur au maximum\n\n")
            .append("Le robot a le meilleur équipement : un deck de 20 cartes toutes en version « + », la machine "
                + "classique ou les 11 rouleaux rares, et il achète Corruption, Bingo et Roulette russe à l'échoppe "
                + "dès qu'il garde de quoi payer 3 tirages. Il joue toujours ses cartes au hasard : un vrai joueur "
                + "fait mieux.\n\n")
            .append("Decks : ");
        List<String> deckTexts = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> deck : STRONG_DECKS.entrySet()) {
            List<String> parts = new ArrayList<>();
            for (Map.Entry<String, Integer> card : deck.getValue().entrySet()) {
                parts.add(card.getValue() + " " + cards.apply(card.getKey()).getName());
            }
            deckTexts.add("**" + deck.getKey() + "** (" + String.join(", ", parts) + ")");
        }
        out.append(String.join(" ; ", deckTexts)).append(".\n\n");

        out.append("### Meilleur équipement par rang\n\n")
            .append("Victoires avec le meilleur des ").append(gears.size()).append(" équipements, et lequel.\n\n| Donjon |");
        for (Rank rank : TOP_RANKS) out.append(' ').append(rank.getTitle()).append(" |");
        out.append("\n|---|");
        for (int i = 0; i < TOP_RANKS.size(); i++) out.append("---|");
        out.append('\n');
        for (Place place : laterPlaces()) {
            for (Dungeon dungeon : place.getDungeons()) {
                out.append("| ").append(place.getShortName()).append(" : ").append(dungeon.getShortName()).append(" |");
                for (Rank rank : TOP_RANKS) {
                    BestGear best = best(dungeon, place, rank);
                    out.append(String.format(Locale.ROOT, " %.0f %% (%s) |", best.stats().winRate() * 100,
                        best.gear().name()));
                }
                out.append('\n');
            }
        }

        Rank top = GEAR_TABLE_RANK;
        out.append("\n### Chaque équipement, rang ").append(top.getTitle()).append("\n\n| Donjon |");
        for (Gear gear : gears) out.append(' ').append(gear.name()).append(" |");
        out.append("\n|---|");
        for (int i = 0; i < gears.size(); i++) out.append("---:|");
        out.append('\n');
        for (Place place : laterPlaces()) {
            for (Dungeon dungeon : place.getDungeons()) {
                out.append("| ").append(place.getShortName()).append(" : ").append(dungeon.getShortName()).append(" |");
                for (Gear gear : gears) {
                    DungeonStats stats = dungeon(dungeon, place, top, gear, sizes.dungeonRuns(), SEED);
                    out.append(String.format(Locale.ROOT, " %.0f %% |", stats.winRate() * 100));
                }
                out.append('\n');
            }
        }
        out.append('\n');
    }

    /** @return les lieux après la Prairie (ceux que le deck de départ ne gagne pas). */
    private static List<Place> laterPlaces() {
        List<Place> places = new ArrayList<>(List.of(Place.values()));
        places.remove(Place.PRAIRIE);
        return places;
    }

    // -------------------------------------------------------------------------
    // 5. Progression
    // -------------------------------------------------------------------------

    /** @return tous les rangs, en commençant par « aucun » ({@code null}). */
    private static List<Rank> allRanks() {
        List<Rank> ranks = new ArrayList<>();
        ranks.add(null);
        ranks.addAll(List.of(Rank.values()));
        return ranks;
    }

    private static String title(Rank rank) { return rank == null ? "Sans rang" : rank.getTitle(); }

    /** @return le premier rang qui gagne {@code dungeon} au moins une fois sur deux avec le deck de départ, ou « jamais ». */
    private String firstWinningRank(Dungeon dungeon, Place place) {
        for (Rank rank : allRanks()) {
            if (dungeon(dungeon, place, rank, Gear.STARTER, sizes.dungeonRuns(), SEED).winRate() >= WINNABLE) {
                return title(rank);
            }
        }
        return "jamais";
    }

    /** Ce que rapporte une descente en moyenne : gains et coffre des victoires, part gardée des défaites. */
    private double expectedCoins(Dungeon dungeon, Place place, Rank rank) {
        return dungeon(dungeon, place, rank, Gear.STARTER, sizes.dungeonRuns(), SEED).averageCoins();
    }

    private void progressionSection(StringBuilder out) {
        out.append("## 5. Progression\n\n")
            .append("### Premier rang qui gagne chaque donjon\n\n")
            .append("Premier rang avec au moins ").append(Math.round(WINNABLE * 100))
            .append(" % de victoires. « jamais » : même Légende du Jackpot n'y arrive pas.\n\n")
            .append("Équipement au maximum : rangs testés ").append(String.join(", ", TOP_RANKS.stream().map(Rank::getTitle).toList()))
            .append(".\n\n| Donjon | Deck de départ | Équipement au maximum |\n|---|---|---|\n");
        for (Place place : Place.values()) {
            for (Dungeon dungeon : place.getDungeons()) {
                String top = "-";
                if (place != Place.PRAIRIE) {
                    Rank found = null;
                    for (Rank rank : TOP_RANKS) {
                        if (best(dungeon, place, rank).stats().winRate() >= WINNABLE) { found = rank; break; }
                    }
                    top = found != null ? title(found) : "jamais";
                }
                out.append("| ").append(place.getShortName()).append(" : ").append(dungeon.getShortName())
                    .append(" | ").append(firstWinningRank(dungeon, place))
                    .append(" | ").append(top).append(" |\n");
            }
        }

        out.append("\n### Prix des rangs\n\n")
            .append("Pour payer chaque rang, le joueur fait le donjon qui rapporte le plus avec le rang d'avant "
                + "(deck de départ, coffre compris). Descentes = prix / pièces moyennes d'une descente "
                + "(défaites comprises, elles rapportent " + PlayerProfile.DEFEAT_KEEP_PERCENT + " % des gains).\n\n")
            .append("| Rang | Prix | Meilleur donjon avec le rang d'avant | Pièces par descente | Descentes |\n")
            .append("|---|---:|---|---:|---:|\n");
        Rank previous = null;
        for (Rank rank : Rank.values()) {
            String bestName = "-";
            double bestCoins = 0;
            for (Place place : Place.values()) {
                for (Dungeon dungeon : place.getDungeons()) {
                    double coins = expectedCoins(dungeon, place, previous);
                    if (coins > bestCoins) {
                        bestCoins = coins;
                        bestName = place.getShortName() + " : " + dungeon.getShortName();
                    }
                }
            }
            out.append(String.format(Locale.ROOT, "| %s | %s | %s | %s | %s |%n", rank.getTitle(),
                PlayerProfile.formatCoins(rank.getPrice()), bestName,
                bestCoins == 0 ? "-" : PlayerProfile.formatCoins(Math.round(bestCoins)),
                bestCoins == 0 ? "-" : String.valueOf((long) Math.ceil(rank.getPrice() / bestCoins))));
            previous = rank;
        }
        out.append('\n');
    }

    // -------------------------------------------------------------------------
    // 6. Pièces par rang
    // -------------------------------------------------------------------------

    private void coinsByRankSection(StringBuilder out) {
        List<Rank> ranks = allRanks();
        out.append("## 6. Pièces moyennes d'une descente, par rang\n\n")
            .append("Deck de départ, sans échoppe. Une descente = le soldat puis le roi. Victoire : gains du combat + coffre ")
            .append("(").append(PlayerProfile.CHEST_SPINS).append(" coûts de tirage du lieu, sans la carte en double ni ")
            .append("Carte au trésor). Défaite : ").append(PlayerProfile.DEFEAT_KEEP_PERCENT).append(" % des gains au ")
            .append("moment de la défaite. Moyenne sur toutes les descentes, défaites comprises.\n\n")
            .append("Coffres : ");
        List<String> chests = new ArrayList<>();
        for (Place place : Place.values()) {
            chests.add(place.getShortName() + " " + PlayerProfile.formatCoins(PlayerProfile.chestCoins(place.getSpinCost())));
        }
        out.append(String.join(", ", chests)).append(".\n\n");

        out.append("### Pièces moyennes par descente (victoires)\n\n| Donjon |");
        for (Rank rank : ranks) out.append(' ').append(title(rank)).append(" |");
        out.append("\n|---|");
        for (int i = 0; i < ranks.size(); i++) out.append("---:|");
        out.append('\n');
        for (Place place : Place.values()) {
            for (Dungeon dungeon : place.getDungeons()) {
                out.append("| ").append(place.getShortName()).append(" : ").append(dungeon.getShortName()).append(" |");
                for (Rank rank : ranks) {
                    DungeonStats stats = dungeon(dungeon, place, rank, Gear.STARTER, sizes.dungeonRuns(), SEED);
                    out.append(String.format(Locale.ROOT, " %s (%.0f %%) |", abbreviated(stats.averageCoins()),
                        stats.winRate() * 100));
                }
                out.append('\n');
            }
        }

        out.append("\n### Pièces par tour joué (rentabilité en temps)\n\n")
            .append("Pièces moyennes d'une descente / tours moyens d'une descente (soldat + roi, défaites comprises).\n\n| Donjon |");
        for (Rank rank : ranks) out.append(' ').append(title(rank)).append(" |");
        out.append("\n|---|");
        for (int i = 0; i < ranks.size(); i++) out.append("---:|");
        out.append('\n');
        for (Place place : Place.values()) {
            for (Dungeon dungeon : place.getDungeons()) {
                out.append("| ").append(place.getShortName()).append(" : ").append(dungeon.getShortName()).append(" |");
                for (Rank rank : ranks) {
                    DungeonStats stats = dungeon(dungeon, place, rank, Gear.STARTER, sizes.dungeonRuns(), SEED);
                    out.append(String.format(Locale.ROOT, " %s (%.0f t) |", abbreviated(stats.coinsPerTurn()),
                        stats.averageTurns()));
                }
                out.append('\n');
            }
        }

        out.append("\n### Meilleur donjon par tour joué, par rang\n\n")
            .append("| Rang | Meilleur donjon | Victoires | Pièces par tour | Rang suivant | Tours pour le payer |\n|---|---|---:|---:|---|---:|\n");
        for (int i = 0; i < ranks.size(); i++) {
            Rank rank = ranks.get(i);
            String bestName = "-";
            double best = 0, bestWins = 0;
            for (Place place : Place.values()) {
                for (Dungeon dungeon : place.getDungeons()) {
                    DungeonStats stats = dungeon(dungeon, place, rank, Gear.STARTER, sizes.dungeonRuns(), SEED);
                    if (stats.coinsPerTurn() > best) {
                        best = stats.coinsPerTurn();
                        bestWins = stats.winRate();
                        bestName = place.getShortName() + " : " + dungeon.getShortName();
                    }
                }
            }
            Rank next = i + 1 < ranks.size() ? ranks.get(i + 1) : null;
            out.append(String.format(Locale.ROOT, "| %s | %s | %.0f %% | %s | %s | %s |%n", title(rank), bestName,
                bestWins * 100, best == 0 ? "-" : abbreviated(best),
                next == null ? "-" : next.getTitle() + " (" + PlayerProfile.formatCoins(next.getPrice()) + ")",
                next == null || best == 0 ? "-" : String.valueOf((long) Math.ceil(next.getPrice() / best))));
        }

        out.append("\n### Détail : victoire / défaite\n\n")
            .append("Pièces moyennes d'une victoire (coffre compris) / d'une défaite (part gardée). « - » : jamais arrivé.\n\n")
            .append("| Donjon |");
        for (Rank rank : ranks) out.append(' ').append(title(rank)).append(" |");
        out.append("\n|---|");
        for (int i = 0; i < ranks.size(); i++) out.append("---:|");
        out.append('\n');
        for (Place place : Place.values()) {
            for (Dungeon dungeon : place.getDungeons()) {
                out.append("| ").append(place.getShortName()).append(" : ").append(dungeon.getShortName()).append(" |");
                for (Rank rank : ranks) {
                    DungeonStats stats = dungeon(dungeon, place, rank, Gear.STARTER, sizes.dungeonRuns(), SEED);
                    out.append(' ').append(stats.winRate() == 0 ? "-" : abbreviated(stats.averageWin())).append(" / ")
                        .append(stats.winRate() == 1 ? "-" : abbreviated(stats.averageLoss())).append(" |");
                }
                out.append('\n');
            }
        }

        out.append("\n### Meilleur lieu par rang\n\n")
            .append("Le donjon qui rapporte le plus en moyenne à chaque rang, et combien de descentes paient le rang suivant.\n\n")
            .append("| Rang | Meilleur donjon | Victoires | Pièces par descente | Rang suivant | Descentes |\n|---|---|---:|---:|---|---:|\n");
        for (int i = 0; i < ranks.size(); i++) {
            Rank rank = ranks.get(i);
            String bestName = "-";
            double best = 0, bestWins = 0;
            for (Place place : Place.values()) {
                for (Dungeon dungeon : place.getDungeons()) {
                    double coins = expectedCoins(dungeon, place, rank);
                    if (coins > best) {
                        best = coins;
                        bestWins = dungeon(dungeon, place, rank, Gear.STARTER, sizes.dungeonRuns(), SEED).winRate();
                        bestName = place.getShortName() + " : " + dungeon.getShortName();
                    }
                }
            }
            Rank next = i + 1 < ranks.size() ? ranks.get(i + 1) : null;
            out.append(String.format(Locale.ROOT, "| %s | %s | %.0f %% | %s | %s | %s |%n", title(rank), bestName,
                bestWins * 100,
                best == 0 ? "-" : PlayerProfile.formatCoins(Math.round(best)),
                next == null ? "-" : next.getTitle() + " (" + PlayerProfile.formatCoins(next.getPrice()) + ")",
                next == null || best == 0 ? "-" : String.valueOf((long) Math.ceil(next.getPrice() / best))));
        }
        out.append('\n');
    }

    // -------------------------------------------------------------------------
    // 7. Règles de pièces comparées
    // -------------------------------------------------------------------------

    /** Une règle de pièces : ce que rapporte une descente {@code run} dans un lieu dont le coffre vaut {@code chest}. */
    record Policy(String name, String text, java.util.function.BiFunction<Run, Long, Double> coins) {}

    static List<Policy> policies() {
        double keep = PlayerProfile.DEFEAT_KEEP_PERCENT / 100.0;
        return List.of(
            new Policy("Sans l'effort", "victoire : gains + coffre ; défaite : 20 % des gains, au plus le coffre",
                (run, chest) -> run.won() ? Math.max(0, run.gains()) + chest
                    : Math.min(Math.max(0, run.gains()) * keep, chest)),
            new Policy("Actuelle", "comme « Sans l'effort », la part gardée en défaite x la part du donjon faite",
                (run, chest) -> run.won() ? Math.max(0, run.gains()) + chest
                    : Math.min(Math.max(0, run.gains()) * keep, chest) * run.effort()));
    }

    /** Meilleur donjon pour un rang et une règle : pièces par tour. */
    record Best(String name, double coinsPerTurn, double winRate) {}

    private Best best(Policy policy, Rank rank) {
        Best best = new Best("-", 0, 0);
        for (Place place : Place.values()) {
            long chest = PlayerProfile.chestCoins(place.getSpinCost());
            for (Dungeon dungeon : place.getDungeons()) {
                DungeonStats stats = dungeon(dungeon, place, rank, Gear.STARTER, sizes.dungeonRuns(), SEED);
                List<Run> runs = runCache.get(dungeon.name() + '/' + rank + '/' + Gear.STARTER.name() + '/'
                    + sizes.dungeonRuns() + '/' + SEED);
                double coins = 0;
                int turns = 0;
                for (Run run : runs) {
                    coins += policy.coins().apply(run, chest);
                    turns += run.turns();
                }
                double perTurn = turns == 0 ? 0 : coins / turns;
                if (perTurn > best.coinsPerTurn()) {
                    best = new Best(place.getShortName() + " : " + dungeon.getShortName(), perTurn, stats.winRate());
                }
            }
        }
        return best;
    }

    private void policySection(StringBuilder out) {
        List<Policy> policies = policies();
        List<Rank> ranks = allRanks();
        out.append("## 7. Règles de pièces comparées\n\n")
            .append("Mêmes descentes (deck de départ), autres règles de pièces. Pour chaque rang, le donjon qui rapporte ")
            .append("le plus par tour joué. « Tours » : tours pour payer tous les rangs dans l'ordre, en jouant ")
            .append("toujours le meilleur donjon du rang qu'on a.\n\n");
        for (Policy policy : policies) out.append("- **").append(policy.name()).append("** : ").append(policy.text()).append('\n');
        out.append("\n| Rang |");
        for (Policy policy : policies) out.append(' ').append(policy.name()).append(" |");
        out.append("\n|---|");
        for (int i = 0; i < policies.size(); i++) out.append("---|");
        out.append('\n');
        double[] totals = new double[policies.size()];
        for (int i = 0; i < ranks.size(); i++) {
            Rank rank = ranks.get(i);
            Rank next = i + 1 < ranks.size() ? ranks.get(i + 1) : null;
            out.append("| ").append(title(rank)).append(" |");
            for (int p = 0; p < policies.size(); p++) {
                Best best = best(policies.get(p), rank);
                if (next != null && best.coinsPerTurn() > 0) totals[p] += next.getPrice() / best.coinsPerTurn();
                out.append(String.format(Locale.ROOT, " %s, %s/tour (%.0f %% vict.) |", best.name(),
                    abbreviated(best.coinsPerTurn()), best.winRate() * 100));
            }
            out.append('\n');
        }
        out.append("| **Tours pour tous les rangs** |");
        for (double total : totals) out.append(String.format(Locale.ROOT, " **%,.0f** |", total).replace(',', ' '));
        out.append("\n\n");
    }

    /** @return {@code coins} en abrégé (« 52 k », « 1,3 M »). */
    private static String abbreviated(double coins) {
        if (coins >= 1e9) return String.format(Locale.FRANCE, "%.1f G", coins / 1e9);
        if (coins >= 1e6) return String.format(Locale.FRANCE, "%.1f M", coins / 1e6);
        if (coins >= 1e4) return String.format(Locale.FRANCE, "%.0f k", coins / 1e3);
        return PlayerProfile.formatCoins(Math.round(coins));
    }

    // -------------------------------------------------------------------------
    // Le robot
    // -------------------------------------------------------------------------

    private GameController controller(RankBonus bonus, Gear gear) {
        return new GameController(() -> gear.deck() == null ? CardLoader.loadStarterDeck(reader) : build(gear.deck()),
            cards, gear.buysAtStall() ? stall : Map.of(),
            deck -> new Player("Robot", Player.BASE_HP, deck, bonus, gear.reels()));
    }

    /** @return les cartes de {@code deck}, chacune dans sa version « + » quand elle existe. */
    private List<Card> build(Map<String, Integer> deck) {
        List<Card> built = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : deck.entrySet()) {
            String id = cards.apply(entry.getKey()).isUpgradable() ? Card.upgradedId(entry.getKey()) : entry.getKey();
            for (int i = 0; i < entry.getValue(); i++) built.add(cards.apply(id));
        }
        return built;
    }

    /**
     * Un tour du robot : il pioche, achète à l'échoppe si {@code buysAtStall} (en gardant
     * de quoi payer 3 tirages), joue ses cartes au hasard (choix au hasard) si {@code playCards}, puis tire.
     */
    private static TurnResult playTurn(GameController game, Random random, boolean playCards, boolean buysAtStall) {
        game.drawCards();
        if (!playCards) return game.spin();
        if (buysAtStall) {
            Player player = game.getGameState().getPlayer();
            for (GameController.ShopOffer offer : game.getShopOffers()) {
                if (game.unavailableReason(offer) == null
                    && player.getGains() - offer.price() >= 3L * game.getSpinCost()) {
                    game.buy(offer);
                }
            }
        }
        List<Card> hand = new ArrayList<>(game.getGameState().getPlayer().getCurrentHand());
        Collections.shuffle(hand, random);
        for (Card card : hand) {
            if (game.isPlayLimitReached() || game.isHandLocked()) break;
            if (game.unplayableReason(card) != null) continue;
            game.playCard(card);
            CardChoice choice = game.getPendingChoice();
            if (choice instanceof BetChoice) {
                game.placeBet(pick(game.getBetOptions(), random));
            } else if (choice instanceof RouletteChoice roulette) {
                game.pickRouletteCard(random.nextInt(roulette.cursed().size()));
            } else if (choice instanceof RiggedReelChoice) {
                game.rigReel(pick(game.getBetOptions(), random));
            }
        }
        return game.spin();
    }

    private static <T> T pick(List<T> options, Random random) { return options.get(random.nextInt(options.size())); }

    private static boolean isOver(GameState state) {
        return state.getPlayer().isDefeated() || state.getEnemy().isDefeated();
    }
}
