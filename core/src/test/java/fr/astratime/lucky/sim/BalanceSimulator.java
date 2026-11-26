package fr.astratime.lucky.sim;

import fr.astratime.lucky.controllers.GameController;
import fr.astratime.lucky.entities.BonusGame;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Combo;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.RankBonus;
import fr.astratime.lucky.entities.SpinEconomy;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolOutcome;
import fr.astratime.lucky.entities.SymbolRegistry;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.choices.BetChoice;
import fr.astratime.lucky.entities.choices.CardChoice;
import fr.astratime.lucky.entities.choices.RiggedReelChoice;
import fr.astratime.lucky.entities.choices.RouletteChoice;
import fr.astratime.lucky.entities.enemy.Difficulty;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GainsEarnedEvent;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.DungeonRun;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.Arrays;
import java.util.function.BiFunction;

/**
 * Simulateur d'équilibrage : un robot simple joue des milliers de tours avec le
 * vrai moteur du jeu, et le rapport (Markdown) donne les chiffres à surveiller
 * après chaque changement de règles.
 *
 * <ol>
 *   <li><b>Tirages</b> : ce que rapporte un tirage, en coûts de tirage (gains de base :
 *       symboles, Paire et Bingo ; objectif 0 à 5 sans carte), la part
 *       des Paires et des Bingos, et du temps passé en dette ;</li>
 *   <li><b>Jeu bonus</b> : ses gains moyens, en coûts de tirage (≈ 215 depuis les 5 tirages sur la machine classique) ;</li>
 *   <li><b>Donjons</b> : victoires et pièces gagnées (au roi) par donjon, selon le rang du joueur ;</li>
 *   <li><b>Joueur au maximum</b> : les donjons du Port, des Mines et du Casino avec le
 *       meilleur équipement (cartes « + », rouleaux rares, échoppe) et les plus hauts rangs ;</li>
 *   <li><b>Progression</b> : le premier rang qui gagne chaque donjon, et le nombre de
 *       victoires qu'il faut pour payer chaque rang ;</li>
 *   <li><b>Pièces par rang</b> : ce que rapporte une descente en moyenne (gains, coffre, part
 *       gardée après une défaite) dans chaque donjon, à chaque rang.</li>
 * </ol>
 *
 * Le robot joue ses cartes au hasard (le « joueur malin » cherche les combinaisons, voir {@link #playSmartTurn}), sans Mise (avec {@code -Pstake=5}, 10 ou 15, il mise
 * ce palier à chaque tirage ; sa cagnotte reste d'une descente à l'autre du même donjon,
 * et dans la Tour elle repart de 0 à chaque chapitre). Avec le deck de départ (ou aucune
 * carte, pour les gains de base), il n'achète rien à l'échoppe ; avec un équipement
 * « au maximum », il y achète Corruption, Bingo et Roulette russe dès qu'il peut.
 * Il joue moins bien qu'un vrai joueur : les chiffres comparent deux versions du
 * jeu entre elles, ils ne prédisent pas une partie.
 *
 * Lancement : {@code ./gradlew :core:simulate} (rapport dans
 * {@code core/build/simulation/equilibrage.md}) ; {@code -Pquick} pour un essai rapide.
 */
public final class BalanceSimulator {

    /** Mise du robot à chaque tirage ({@code -Dlucky.stake=5}, 10 ou 15 ; aucune par défaut). */
    static final SpinEconomy.Stake STAKE = stakeOf(Integer.getInteger("lucky.stake", 0));

    /**
     * Ancien joueur malin ({@code -PoldBot}), pour comparer : cartes dans un ordre fixe sans chercher les
     * combinaisons, choix au hasard, échoppe réduite à Bingo, Roulette russe et Corruption, pas d'ennemi doré.
     */
    static final boolean OLD_BOT = Boolean.getBoolean("lucky.oldBot");

    /** Essai ({@code -Dlucky.potPerRun=true}) : la cagnotte repart de 0 à chaque descente d'un donjon. */
    static final boolean POT_PER_RUN = Boolean.getBoolean("lucky.potPerRun");

    private static SpinEconomy.Stake stakeOf(int percent) {
        for (SpinEconomy.Stake stake : SpinEconomy.Stake.values()) if (stake.percent == percent) return stake;
        throw new IllegalArgumentException("Mise inconnue : " + percent + " % (0, 5, 10 ou 15)");
    }

    /** Le robot pose sa Mise avant de tirer (s'il a des gains), puis tire. */
    private static TurnResult stakeAndSpin(GameController game) {
        while (game.getStake() != STAKE) {
            if (game.nextStake() == SpinEconomy.Stake.NONE && STAKE != SpinEconomy.Stake.NONE) break; // rien à miser
        }
        return game.spin();
    }

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
     * @param upgraded    {@code true} si ses cartes sont en version « + » quand elle existe
     * @param smart       {@code true} s'il joue en « joueur malin » ({@link #playSmartTurn}) au lieu du hasard
     * @param plus        cartes jouées en « + » même si {@code upgraded} est {@code false} (profils par rang)
     */
    record Gear(String name, Map<String, Integer> deck, List<Symbol> reels, boolean buysAtStall, boolean upgraded,
                boolean smart, java.util.Set<String> plus) {
        static final Gear STARTER = new Gear("Deck de départ", null, Symbol.classicReels(), false, false, false);

        Gear(String name, Map<String, Integer> deck, List<Symbol> reels, boolean buysAtStall, boolean upgraded,
             boolean smart) {
            this(name, deck, reels, buysAtStall, upgraded, smart, java.util.Set.of());
        }

        Gear(String name, Map<String, Integer> deck, List<Symbol> reels, boolean buysAtStall) {
            this(name, deck, reels, buysAtStall, true, false);
        }
    }

    /**
     * Deck du « joueur malin » (conseils d'Astra, 2026-10-30) : Coeurs pour se soigner, Piques pour les dégâts,
     * Dans la manche (6 cartes par tour), Rayon du phare et Masque du Joker (l'ennemi passe son tour), cartes à
     * plusieurs effets (Marteau du cordonnier, Chope, Cocktail des abysses, Pacte de sang).
     * Seulement des cartes qu'on peut obtenir en jeu (2026-11-20 : le 10 de Pique, introuvable, devient le Héraut).
     */
    static final Map<String, Integer> SMART_DECK = deck("1_pique", 1, "13_pique", 1, "12_pique", 1, "11_pique", 1,
        "heraut", 1, "1_coeur", 1, "13_coeur", 1, "12_coeur", 1, "in_the_sleeve", 1, "rayon_du_phare", 1,
        "masque_du_joker", 1, "marteau_du_cordonnier", 1, "chope", 1, "cocktail_des_abysses", 1, "pacte_de_sang", 1,
        "draw_3", 1, "draw_2", 1, "pierre_a_aiguiser", 1, "dague_de_l_ombre", 1, "guillotine", 1);

    /** Joueur malin, cartes normales puis toutes en « + » ; il achète Bingo à l'échoppe pour un gros tour. */
    static final Gear SMART = new Gear("Joueur malin", SMART_DECK, Symbol.classicReels(), true, false, true);
    static final Gear SMART_PLUS = new Gear("Joueur malin +", SMART_DECK, Symbol.classicReels(), true, true, true);

    /** Les 11 rouleaux rares (boutique et Mine) : la machine la plus chère. */
    static final List<Symbol> RARE_REELS = List.of(Symbol.HORSESHOE, Symbol.ECU, Symbol.SWORD, Symbol.HEART,
        Symbol.DIE, Symbol.STAR, Symbol.BOMB, Symbol.CROWN, Symbol.NUGGET, Symbol.GOLD_BAR, Symbol.TRIPLE_CHERRY);

    /** Cartes neutres communes aux decks de couleur : mêmes 8 cartes pour chacun. */
    private static final String[] SUIT_CORE = {"in_the_sleeve", "rayon_du_phare", "masque_du_joker", "draw_3", "draw_2",
        "chope", "cocktail_des_abysses", "marteau_du_cordonnier"};

    /** Decks de 20 cartes à 1 exemplaire (un par couleur, voir {@link #suitDeck}), joués en version « + ». */
    private static final Map<String, Map<String, Integer>> STRONG_DECKS = new LinkedHashMap<>();
    static {
        STRONG_DECKS.put("Piques", suitDeck("pique", "pierre_a_aiguiser", "dague_de_l_ombre", "guillotine"));
        STRONG_DECKS.put("Coeurs", suitDeck("coeur", "transfusion", "baiser_vampire", "pacte_de_sang"));
        STRONG_DECKS.put("Carreaux", suitDeck("carreau", "rempart", "miroir_taille", "diamant_brut"));
        STRONG_DECKS.put("Trèfles", suitDeck("trefle", "etoile_filante", "etoile_du_berger", "etoile_d_or"));
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
    private static final List<Rank> RANKS = new ArrayList<>(Arrays.asList(
        null, Rank.AVARE, Rank.FLAMBEUR, Rank.REQUIN, Rank.MAGNAT));

    private final Sizes sizes;
    private final CardLoader.AssetReader reader;
    private final Function<String, Card> cards;
    private final Map<String, Integer> stall;
    /** L'échoppe de l'Exploration, comme dans le jeu (pour le joueur malin). */
    private final Map<String, Integer> explorationStall;
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
        this.explorationStall = GameController.stallFor(shop, cards, false, false);
    }

    public static void main(String[] args) throws IOException {
        boolean quick = List.of(args).contains("--quick");
        Path assets = Paths.get(System.getProperty("lucky.assets", "../assets")).toAbsolutePath().normalize();
        BalanceSimulator simulator = new BalanceSimulator(assets, quick ? Sizes.QUICK : Sizes.FULL);
        String report = List.of(args).contains("--tower") ? simulator.towerReport()
            : List.of(args).contains("--tune") ? simulator.tuneReport()
            : List.of(args).contains("--hardplace") ? simulator.hardPlaceReport()
            : List.of(args).contains("--rankplace") ? simulator.rankPlaceReport()
            : List.of(args).contains("--profiles") ? simulator.profilesReport()
            : List.of(args).contains("--progress") ? simulator.progressReport()
            : List.of(args).contains("--journey") ? simulator.journeyReport()
            : System.getProperty("lucky.journal") != null ? simulator.journalReport(Paths.get(System.getProperty("lucky.journal")))
            : simulator.report();
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
        towerSection(out);
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
                + "≈ 215 sur la machine classique (5 tirages).\n\n")
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
        Arrays.sort(totals);
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
        long chest = place.getChestCoins();
        double winTotal = 0, lossTotal = 0;
        int[] turns = {0};
        List<Run> played = new ArrayList<>();
        for (int run = 0; run < runs; run++) {
            int before = turns[0];
            if (POT_PER_RUN) game.getPot().take(); // essai : une cagnotte neuve à chaque descente
            game.restart(dungeon.getSoldier());
            rollGolden(game, random);
            boolean won = fight(game, random, gear, turns, false);
            double effort = 0.5 * (1 - Math.max(0, game.getGameState().getEnemy().getHpRatio()));
            if (won) {
                game.startCombat(dungeon.getKing());
                won = fight(game, random, gear, turns, true);
                effort = 0.5 + 0.5 * (1 - Math.max(0, game.getGameState().getEnemy().getHpRatio()));
            }
            played.add(new Run(won, game.getGameState().getPlayer().getGains(), turns[0] - before, won ? 1 : effort));
            long reward = PlayerProfile.combatReward(won, true, game.getGameState().getPlayer().getGains(),
                place, effort);
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
        return fight(game, random, gear, turns, false);
    }

    /** Comme {@link #fight(GameController, Random, Gear, int[])} ; {@code king} : le roi du donjon. */
    private boolean fight(GameController game, Random random, Gear gear, int[] turns, boolean king) {
        for (int turn = 0; turn < sizes.maxTurns() && !isOver(game.getGameState()); turn++) {
            if (gear.smart()) playSmartTurn(game, random, king);
            else playTurn(game, random, true, gear.buysAtStall());
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
            .append("(Prairie 500, Port 1 500, Mines 4 500, Casino 13 500 coûts de tirage, sans la carte en double ni ")
            .append("Carte au trésor). Défaite : ").append(PlayerProfile.DEFEAT_KEEP_PERCENT).append(" % des gains au ")
            .append("moment de la défaite. Moyenne sur toutes les descentes, défaites comprises.\n\n")
            .append("Coffres : ");
        List<String> chests = new ArrayList<>();
        for (Place place : Place.values()) {
            chests.add(place.getShortName() + " " + PlayerProfile.formatCoins(place.getChestCoins()));
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
    // 7. Meilleur donjon par rang
    // -------------------------------------------------------------------------

    /** Une règle de pièces : ce que rapporte une descente {@code run} dans le lieu {@code place}. */
    record Policy(String name, String text, BiFunction<Run, Place, Double> coins) {}

    /**
     * @param chestSpins coût du coffre de chaque lieu, en coûts de tirage (dans l'ordre de {@link Place})
     * @return la règle actuelle (gains + coffre ; défaite : 20 % des gains, au plus 500 tirages, x la part faite)
     *         avec ces coffres
     */
    static Policy withChests(String name, int... chestSpins) {
        double keep = PlayerProfile.DEFEAT_KEEP_PERCENT / 100.0;
        StringBuilder text = new StringBuilder("coffres de ");
        for (int i = 0; i < chestSpins.length; i++) {
            text.append(i == 0 ? "" : " / ").append(chestSpins[i]);
        }
        StringBuilder names = new StringBuilder();
        for (Place place : Place.values()) names.append(names.isEmpty() ? "" : " / ").append(place.getShortName());
        text.append(" coûts de tirage (").append(names).append(')');
        return new Policy(name, text.toString(), (run, place) -> {
            double chest = (double) chestSpins[place.ordinal()] * place.getSpinCost();
            double gains = Math.max(0, run.gains());
            return run.won() ? gains + chest : Math.min(gains * keep, place.getDefeatCap()) * run.effort();
        });
    }

    static List<Policy> policies() {
        int[] now = new int[Place.values().length];
        for (Place place : Place.values()) now[place.ordinal()] = place.getChestSpins();
        return List.of(withChests("Actuelle", now));
    }

    /** Meilleur donjon pour un rang et une règle : pièces par tour. */
    record Best(String name, double coinsPerTurn, double winRate) {}

    private Best best(Policy policy, Rank rank) {
        Best best = new Best("-", 0, 0);
        for (Place place : Place.values()) {
            for (Dungeon dungeon : place.getDungeons()) {
                DungeonStats stats = dungeon(dungeon, place, rank, Gear.STARTER, sizes.dungeonRuns(), SEED);
                List<Run> runs = runCache.get(dungeon.name() + '/' + rank + '/' + Gear.STARTER.name() + '/'
                    + sizes.dungeonRuns() + '/' + SEED);
                double coins = 0;
                int turns = 0;
                for (Run run : runs) {
                    coins += policy.coins().apply(run, place);
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
        out.append("## 7. Meilleur donjon par rang\n\n")
            .append("Deck de départ. Pour chaque rang, le donjon qui rapporte ")
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
    static String abbreviated(double coins) {
        if (coins >= 1e9) return String.format(Locale.FRANCE, "%.1f G", coins / 1e9);
        if (coins >= 1e6) return String.format(Locale.FRANCE, "%.1f M", coins / 1e6);
        if (coins >= 1e4) return String.format(Locale.FRANCE, "%.0f k", coins / 1e3);
        return PlayerProfile.formatCoins(Math.round(coins));
    }

    // -------------------------------------------------------------------------
    // 8. Tour des épreuves
    // -------------------------------------------------------------------------

    /**
     * Réglage des lieux (option --tune) : victoires et pièces par descente et par tour, deck de départ,
     * pour chaque donjon et chaque rang ; -Dproto.places=FOIRE,DESERT pour quelques lieux seulement.
     */
    String tuneReport() {
        String only = System.getProperty("proto.places", "");
        List<Rank> ranks = allRanks();
        List<Runnable> jobs = new ArrayList<>();
        List<Place> places = new ArrayList<>();
        for (Place place : Place.values()) if (only.isEmpty() || only.contains(place.name())) places.add(place);
        for (Place place : places) for (Dungeon dungeon : place.getDungeons()) for (Rank rank : ranks)
            jobs.add(() -> dungeon(dungeon, place, rank, Gear.STARTER, sizes.dungeonRuns(), SEED));
        jobs.parallelStream().forEach(Runnable::run);
        StringBuilder out = new StringBuilder("| Donjon |");
        for (Rank rank : ranks) out.append(' ').append(title(rank)).append(" |");
        out.append('\n');
        for (Place place : places) for (Dungeon dungeon : place.getDungeons()) {
            out.append("| ").append(place.getShortName()).append(" : ").append(dungeon.getShortName()).append(" |");
            for (Rank rank : ranks) {
                DungeonStats st = dungeon(dungeon, place, rank, Gear.STARTER, sizes.dungeonRuns(), SEED);
                out.append(String.format(Locale.ROOT, " %.0f%% %s %s/t |", st.winRate() * 100,
                    PlayerProfile.formatCoins((long) st.averageCoins()),
                    PlayerProfile.formatCoins((long) (st.averageCoins() / Math.max(1, st.averageTurns())))));
            }
            out.append('\n');
        }
        return out.toString();
    }

    /** @return le rapport de la Tour seule (victoires par chapitre et par rang). */
    String towerReport() {
        StringBuilder out = new StringBuilder();
        towerSection(out);
        return out.toString();
    }

    /** Part de chapitres gagnés (premier ennemi, un challenger au hasard, boss), mode normal. */
    double towerWinRate(fr.astratime.lucky.entities.tower.Chapter chapter, Rank rank, Gear gear, int runs, long seed) {
        Random random = new Random(seed);
        GameController game = controller(rank == null ? RankBonus.NONE : rank.getBonus(), gear);
        game.setPlaceRule(fr.astratime.lucky.entities.exploration.PlaceRule.NONE);
        game.setBonusGameChance(fr.astratime.lucky.entities.BonusGame.TOWER_TRIGGER_CHANCE);
        int wins = 0;
        int[] turns = {0};
        for (int run = 0; run < runs; run++) {
            game.getPot().take(); // la cagnotte de la Tour s'efface à chaque chapitre
            game.restart(chapter.getFirstEnemy());
            if (!fight(game, random, gear, turns)) continue;
            game.startCombat(chapter.getChallengers().get(random.nextInt(chapter.getChallengers().size())));
            if (!fight(game, random, gear, turns)) continue;
            game.startCombat(chapter.getBoss());
            if (fight(game, random, gear, turns)) wins++;
        }
        return wins / (double) runs;
    }

    private void towerSection(StringBuilder out) {
        out.append("## 8. Tour des épreuves\n\n")
            .append("Part des chapitres gagnés (premier ennemi, un challenger au hasard, puis le boss), mode normal.\n\n");
        List<Rank> ranks = allRanks();
        fr.astratime.lucky.entities.tower.Chapter[] chapters = fr.astratime.lucky.entities.tower.Chapter.values();
        Map<String, Double> starter = new ConcurrentHashMap<>(), top = new ConcurrentHashMap<>();
        Map<String, String> topGear = new ConcurrentHashMap<>();
        List<Runnable> jobs = new ArrayList<>();
        for (var chapter : chapters) {
            for (Rank rank : ranks) {
                String key = chapter.name() + '/' + rank;
                jobs.add(() -> starter.put(key, towerWinRate(chapter, rank, Gear.STARTER, sizes.dungeonRuns(), SEED)));
                if (rank == null || rank.ordinal() < Rank.FLAMBEUR.ordinal()) continue;
                for (Gear gear : topGears()) {
                    jobs.add(() -> {
                        double rate = towerWinRate(chapter, rank, gear, sizes.dungeonRuns() / 2, SEED);
                        synchronized (top) {
                            if (rate > top.getOrDefault(key, -1.0)) { top.put(key, rate); topGear.put(key, gear.name()); }
                        }
                    });
                }
            }
        }
        jobs.parallelStream().forEach(Runnable::run);
        for (int pass = 0; pass < 2; pass++) {
            Map<String, Double> rates = pass == 0 ? starter : top;
            out.append(pass == 0 ? "### Deck de départ\n\n" : "### Meilleur équipement au maximum\n\n");
            out.append("| Chapitre |");
            for (Rank rank : ranks) out.append(' ').append(title(rank)).append(" |");
            out.append("\n|---|");
            for (int i = 0; i < ranks.size(); i++) out.append("---:|");
            out.append('\n');
            for (var chapter : chapters) {
                out.append("| ").append(chapter.getLabel()).append(" |");
                for (Rank rank : ranks) {
                    Double rate = rates.get(chapter.name() + '/' + rank);
                    out.append(rate == null ? " - |" : String.format(Locale.ROOT, " %.0f %% |", rate * 100));
                }
                out.append('\n');
            }
            out.append('\n');
        }
    }

    // -------------------------------------------------------------------------
    // Gains par rang dans son lieu : ./gradlew :core:simulate -Prankplace -Pstake=15 (-PrpRuns=300)
    // -------------------------------------------------------------------------

    /** Lieu associé à chaque rang : celui du jeu (dix paliers, 2026-11-20). */
    static Place placeOfRank(Rank rank) {
        return fr.astratime.lucky.progress.PlayerProfile.placeOf(rank);
    }

    /** Totaux d'un donjon (somme sur les descentes). */
    static final class RP {
        int runs, wins, soldierWins, bonusGames, turns, soldierFights, kingFights;
        double soldierGains, kingGains, coinsWin, coinsLoss, bonusGains, bonusGainsWin, finalGainsWin, potEnd;
        final List<Double> runCoins = new ArrayList<>();
        /**
         * Ce qu'un joueur touche d'habitude par descente sur {@code span} descentes : la médiane, sur 2 000 tirages
         * au sort, de la moyenne de {@code span} descentes. La moyenne simple est tirée par de rares Jeux bonus énormes.
         */
        double typical(int span) {
            if (runCoins.isEmpty()) return 0;
            Random random = new Random(7);
            double[] means = new double[2_000];
            for (int i = 0; i < means.length; i++) {
                double sum = 0;
                for (int j = 0; j < span; j++) sum += runCoins.get(random.nextInt(runCoins.size()));
                means[i] = sum / span;
            }
            Arrays.sort(means);
            return means[means.length / 2];
        }
        double median() {
            List<Double> c = new ArrayList<>(runCoins); Collections.sort(c);
            return c.isEmpty() ? 0 : c.get(c.size() / 2);
        }
        void add(RP o) {
            runCoins.addAll(o.runCoins);
            runs += o.runs; wins += o.wins; soldierWins += o.soldierWins; bonusGames += o.bonusGames; turns += o.turns;
            soldierFights += o.soldierFights; kingFights += o.kingFights;
            soldierGains += o.soldierGains; kingGains += o.kingGains; coinsWin += o.coinsWin; coinsLoss += o.coinsLoss;
            bonusGains += o.bonusGains; bonusGainsWin += o.bonusGainsWin; finalGainsWin += o.finalGainsWin; potEnd += o.potEnd;
        }
    }

    private boolean fightTracked(GameController game, Random random, Gear gear, int[] turns, boolean king, double[] bonus) {
        for (int turn = 0; turn < sizes.maxTurns() && !isOver(game.getGameState()); turn++) {
            TurnResult result = gear.smart() ? playSmartTurn(game, random, king) : playTurn(game, random, true, gear.buysAtStall());
            if (result != null && result.getBonusGame() != null) {
                bonus[0] += result.getBonusGame().getTotal(); bonus[1]++;
                if (POT_LOG != null) synchronized (POT_LOG) { POT_LOG.add(new double[] {result.getBonusGame().getPot(),
                    result.getBonusGame().getLines().size(), result.getBonusGame().getTotal(), game.getSpinCost()}); }
            }
            turns[0]++;
        }
        return game.getGameState().getEnemy().isDefeated();
    }

    /** Les 4 donjons du lieu à tour de rôle, avec une seule cagnotte pour le lieu (comme dans le jeu). */
    Map<String, RP> rankPlace(Place place, Rank rank, Gear gear, boolean bonusOn, int runs, long seed) {
        return rankPlace(place, false, rank, gear, bonusOn, runs, seed);
    }

    /** Comme {@link #rankPlace(Place, Rank, Gear, boolean, int, long)}, en Exploration difficile si {@code hard}. */
    Map<String, RP> rankPlace(Place place, boolean hard, Rank rank, Gear gear, boolean bonusOn, int runs, long seed) {
        return rankPlace(place, hard, rank, PRESTIGE, gear, bonusOn, runs, seed);
    }

    /** Comme {@link #rankPlace(Place, boolean, Rank, Gear, boolean, int, long)}, au Prestige {@code prestige}. */
    Map<String, RP> rankPlace(Place place, boolean hard, Rank rank, int prestige, Gear gear, boolean bonusOn, int runs,
                              long seed) {
        Random random = new Random(seed);
        GameController game = controller(Rank.bonusAt(rank, prestige), gear);
        game.setDifficulty(Difficulty.of(hard));
        game.setPlaceRule(place.getRule());
        game.setBonusGameEnabled(bonusOn);
        Map<String, RP> byDungeon = new LinkedHashMap<>();
        for (Dungeon d : place.getDungeons()) byDungeon.put(d.getShortName(), new RP());
        long chest = place.getChestCoins(hard);
        List<Dungeon> dungeons = place.getDungeons();
        for (int run = 0; run < runs * dungeons.size(); run++) {
            Dungeon dungeon = dungeons.get(run % dungeons.size());
            RP rp = byDungeon.get(dungeon.getShortName());
            int[] turns = {0};
            double[] bonus = {0, 0};
            game.restart(dungeon.getSoldier());
            rollGolden(game, random);
            double g0 = game.getGameState().getPlayer().getGains();
            rp.soldierFights++;
            boolean won = fightTracked(game, random, gear, turns, false, bonus);
            double g1 = game.getGameState().getPlayer().getGains();
            double effort = 0.5 * (1 - Math.max(0, game.getGameState().getEnemy().getHpRatio()));
            if (won) {
                rp.soldierWins++;
                rp.soldierGains += g1 - g0;
                game.startCombat(dungeon.getKing());
                double k0 = game.getGameState().getPlayer().getGains();
                rp.kingFights++;
                won = fightTracked(game, random, gear, turns, true, bonus);
                double k1 = game.getGameState().getPlayer().getGains();
                if (won) rp.kingGains += k1 - k0;
                effort = 0.5 + 0.5 * (1 - Math.max(0, game.getGameState().getEnemy().getHpRatio()));
            }
            double gains = game.getGameState().getPlayer().getGains();
            int share = PlayerProfile.coinsPercent(rank, place, hard); // lieu en dessous de celui du rang
            long reward = PlayerProfile.share(PlayerProfile.combatReward(won, true, gains, place, hard, won ? 1 : effort), share);
            long chestShare = PlayerProfile.share(chest, share);
            rp.runs++;
            rp.runCoins.add((double) reward + (won ? chestShare : 0));
            rp.turns += turns[0];
            rp.bonusGames += (int) bonus[1];
            rp.bonusGains += bonus[0];
            if (won) {
                rp.wins++;
                rp.coinsWin += reward + chestShare;
                rp.bonusGainsWin += bonus[0];
                rp.finalGainsWin += Math.max(0, gains);
            } else {
                rp.coinsLoss += reward;
            }
            rp.potEnd += game.getPot().getAmount();
        }
        return byDungeon;
    }

    /**
     * Deck de 20 cartes à 1 exemplaire dominé par une couleur : son As et ses Roi à 9, ses 3 cartes spéciales,
     * trois cartes d'appoint (As, Roi, Dame de Coeur pour tenir ; de Pique pour le deck Coeur) et {@link #SUIT_CORE}.
     * Un deck d'une seule couleur ne gagne rien, quelle qu'elle soit : il lui manque le soin ou les dégâts.
     */
    private static Map<String, Integer> suitDeck(String suit, String... specials) {
        Map<String, Integer> deck = new LinkedHashMap<>();
        for (int rank : new int[] {1, 13, 12, 11, 10, 9}) deck.put(rank + "_" + suit, 1);
        for (String id : specials) deck.put(id, 1);
        String support = suit.equals("coeur") ? "pique" : "coeur";
        for (int rank : new int[] {1, 13, 12}) deck.put(rank + "_" + support, 1);
        for (String id : SUIT_CORE) deck.put(id, 1);
        if (deck.size() != 20) throw new IllegalStateException(suit + " : " + deck.size() + " cartes");
        return deck;
    }

    /**
     * Les robots de référence pour les prix (2026-11-13) : le deck de départ, le joueur malin mixte, et un joueur
     * malin par couleur. Les prix se règlent sur la médiane des quatre couleurs, pas sur la meilleure.
     */
    static final List<Gear> SUIT_GEARS = List.of(
        Gear.STARTER,
        SMART,
        new Gear("Pique", STRONG_DECKS.get("Piques"), Symbol.classicReels(), true, false, true),
        new Gear("Coeur", STRONG_DECKS.get("Coeurs"), Symbol.classicReels(), true, false, true),
        new Gear("Carreau", STRONG_DECKS.get("Carreaux"), Symbol.classicReels(), true, false, true),
        new Gear("Trefle", STRONG_DECKS.get("Trèfles"), Symbol.classicReels(), true, false, true));

    /** Niveau de Prestige du robot dans le rapport « rang dans son lieu » ({@code -Plevel=N} ; 0 par défaut). */
    static final int PRESTIGE = Integer.getInteger("lucky.prestige", 0);

    static final List<double[]> POT_LOG = System.getProperty("lucky.potLog") != null ? new ArrayList<>() : null;

    // -------------------------------------------------------------------------
    // Trois joueurs par rang : ./gradlew :core:simulate -Pprofiles -Pstake=15 (-PrpRuns=100 -PshopShare=1)
    // -------------------------------------------------------------------------

    /** Decks visés par les profils par rang (voir {@link RankProfiles}). */
    static Map<String, Map<String, Integer>> profileTargets() {
        Map<String, Map<String, Integer>> targets = new LinkedHashMap<>();
        targets.put("Malin", SMART_DECK);
        targets.put("Pique", STRONG_DECKS.get("Piques"));
        targets.put("Coeur", STRONG_DECKS.get("Coeurs"));
        targets.put("Trefle", STRONG_DECKS.get("Trèfles"));
        targets.put("Carreau", STRONG_DECKS.get("Carreaux"));
        return targets;
    }

    /**
     * Pour chaque rang, dans son lieu, trois joueurs : <b>débutant</b> (deck de départ, cartes au hasard),
     * <b>moyen</b> (ce qu'il possède vraiment à ce rang, {@link RankProfiles}, joué en joueur malin) et <b>expert</b>
     * (le deck visé complet, tout en « + », la machine du moyen, joué en joueur malin). Le moyen et l'expert
     * sont donnés pour chaque deck visé. Les profils sont écrits dans profils_par_rang.md, pour relecture.
     */
    // -------------------------------------------------------------------------
    // Joueur qui progresse : ./gradlew :core:simulate -Pprogress (-Pwhat=place,tower,croupier -Plevels=0,5,10 -PrpRuns=40)
    // -------------------------------------------------------------------------

    /** Équipement d'un Légende : profils moyen et expert de chaque deck visé (voir {@link RankProfiles}). */
    Map<String, Gear> legendGears() {
        Map<String, Long> boutique = CardLoader.loadBoutique(reader);
        List<String> starter = new ArrayList<>();
        for (Card card : CardLoader.loadStarterDeck(reader)) starter.add(Card.baseId(card.getId()));
        RankProfiles profiles = new RankProfiles(boutique, starter, CardLoader.loadStartingCollection(reader), cards);
        Map<String, Gear> g = new LinkedHashMap<>();
        String only = System.getProperty("lucky.onlyGear");
        for (Map.Entry<String, Map<String, Integer>> target : profileTargets().entrySet()) {
            RankProfiles.Profile profile = profiles.of(Rank.LEGENDE, target.getValue());
            Gear moyen = new Gear("Moyen " + target.getKey(), profile.deck(), profile.reels(), true, false, true, profile.plus());
            Gear expert = new Gear("Expert " + target.getKey(), target.getValue(), profile.reels(), true, true, true);
            for (Gear x : List.of(moyen, expert)) if (only == null || List.of(only.split(",")).contains(x.name())) g.put(x.name(), x);
        }
        return g;
    }

    /** Part de chapitres de l'Histoire difficile gagnés (ou de combats contre le Croupier si {@code croupier}) par un Légende au Prestige {@code prestige}. */
    double hardTowerWinRate(fr.astratime.lucky.entities.tower.Chapter chapter, int prestige, Gear gear, int runs, long seed, boolean croupier, double[] turnsOut) {
        Random random = new Random(seed);
        GameController game = controller(Rank.bonusAt(Rank.LEGENDE, prestige), gear);
        game.setHardMode(true);
        game.setPlaceRule(fr.astratime.lucky.entities.exploration.PlaceRule.NONE);
        game.setBonusGameChance(fr.astratime.lucky.entities.BonusGame.TOWER_TRIGGER_CHANCE);
        int wins = 0;
        int[] turns = {0};
        for (int run = 0; run < runs; run++) {
            game.getPot().take();
            if (croupier) {
                game.restart(EnemyKind.GRAND_CROUPIER);
                if (fight(game, random, gear, turns, true)) wins++;
                continue;
            }
            game.restart(chapter.getFirstEnemy());
            if (!fight(game, random, gear, turns)) continue;
            game.startCombat(chapter.getChallengers().get(random.nextInt(chapter.getChallengers().size())));
            if (!fight(game, random, gear, turns)) continue;
            game.startCombat(chapter.getBoss());
            if (fight(game, random, gear, turns, true)) wins++;
        }
        turnsOut[0] = turns[0] / (double) runs;
        return wins / (double) runs;
    }

    /**
     * Un Légende du Prestige 0 au X : le lieu de l'Exploration difficile qui donne son prochain sceau de Prestige, chaque
     * chapitre de l'Histoire difficile et le Croupier (victoires en %, tours par partie).
     */
    String progressReport() {
        int runs = Integer.getInteger("lucky.rpRuns", 40);
        String what = System.getProperty("lucky.what", "place,tower,croupier");
        Map<String, Gear> gears = legendGears();
        List<String> lines = Collections.synchronizedList(new ArrayList<>());
        List<Runnable> jobs = new ArrayList<>();
        List<Integer> levels = new ArrayList<>();
        for (String l : System.getProperty("lucky.levels", "0,1,2,3,4,5,6,7,8,9,10").split(",")) levels.add(Integer.parseInt(l));
        for (Gear gear : gears.values()) {
            if (what.contains("place")) for (int p : levels) {
                if (p > 9) continue;
                Place place = Place.atTier(p + 1);
                jobs.add(() -> {
                    RP total = new RP();
                    rankPlace(place, true, Rank.LEGENDE, p, gear, true, runs, SEED).values().forEach(total::add);
                    lines.add(String.format(Locale.ROOT, "place\t%s\tP%d\t%s\t%.0f\t%.1f", gear.name(), p, place.getShortName(),
                        100.0 * total.wins / total.runs, total.turns / (double) total.runs));
                });
            }
            if (what.contains("tower")) for (var ch : fr.astratime.lucky.entities.tower.Chapter.values()) for (int p : levels) {
                jobs.add(() -> {
                    double[] t = {0};
                    double r = hardTowerWinRate(ch, p, gear, runs, SEED, false, t);
                    lines.add(String.format(Locale.ROOT, "tower\t%s\tP%d\tch%d\t%.0f\t%.1f", gear.name(), p, ch.getNumber(), 100 * r, t[0]));
                });
            }
            if (what.contains("croupier")) for (int p : levels) {
                jobs.add(() -> {
                    double[] t = {0};
                    double r = hardTowerWinRate(null, p, gear, runs, SEED, true, t);
                    lines.add(String.format(Locale.ROOT, "croupier\t%s\tP%d\t-\t%.0f\t%.1f", gear.name(), p, 100 * r, t[0]));
                });
            }
        }
        jobs.parallelStream().forEach(Runnable::run);
        Collections.sort(lines);
        return "kind\tgear\tprestige\twhere\twinPercent\tturns\n" + String.join("\n", lines) + "\n";
    }

    String profilesReport() {
        int runs = Integer.getInteger("lucky.rpRuns", 100);
        Map<String, Long> boutique = CardLoader.loadBoutique(reader);
        // les cartes réservées à l'Exploration difficile ne s'achètent pas pendant la montée en rangs (mode normal)
        boutique.keySet().removeAll(CardLoader.loadHardGates(reader).keySet());
        List<String> starter = new ArrayList<>();
        for (Card card : CardLoader.loadStarterDeck(reader)) starter.add(Card.baseId(card.getId()));
        RankProfiles profiles = new RankProfiles(boutique, starter, CardLoader.loadStartingCollection(reader), cards);
        StringBuilder describe = new StringBuilder("# Profils par rang\n\n");
        List<Object[]> cases = new ArrayList<>(); // rang, joueur, deck visé, gear
        for (Rank rank : allRanks()) {
            if (System.getProperty("lucky.onlyRank") != null && !List.of(System.getProperty("lucky.onlyRank").split(",")).contains(title(rank))) continue;
            cases.add(new Object[] {rank, "débutant", "-", Gear.STARTER});
            describe.append("## ").append(title(rank)).append(" (").append(placeOfRank(rank).getShortName()).append(")\n\n");
            for (Map.Entry<String, Map<String, Integer>> target : profileTargets().entrySet()) {
                RankProfiles.Profile profile = profiles.of(rank, target.getValue());
                describe.append(profiles.describe(target.getKey(), profile));
                cases.add(new Object[] {rank, "moyen", target.getKey(), new Gear("Moyen " + target.getKey(), profile.deck(),
                    profile.reels(), true, false, true, profile.plus())});
                // expert : le deck visé complet, tout en « + », la même machine que le moyen
                cases.add(new Object[] {rank, "expert", target.getKey(), new Gear("Expert " + target.getKey(),
                    target.getValue(), profile.reels(), true, true, true)});
            }
        }
        Map<Integer, RP> results = new ConcurrentHashMap<>();
        List<Runnable> jobs = new ArrayList<>();
        for (int i = 0; i < cases.size(); i++) {
            int index = i;
            Object[] c = cases.get(i);
            jobs.add(() -> {
                RP total = new RP();
                rankPlace(placeOfRank((Rank) c[0]), (Rank) c[0], (Gear) c[3], true, runs, SEED).values().forEach(total::add);
                results.put(index, total);
            });
        }
        jobs.parallelStream().forEach(Runnable::run);
        StringBuilder out = new StringBuilder("rank\tplace\tplayer\ttarget\truns\twinPercent\tavgCoinsRun\tmedianCoinsRun\ttypical30\tturnsPerRun\tdescentsForNextRank\n");
        for (int i = 0; i < cases.size(); i++) {
            Object[] c = cases.get(i);
            Rank rank = (Rank) c[0];
            RP rp = results.get(i);
            Rank next = rank == null ? Rank.values()[0] : rank.ordinal() + 1 < Rank.values().length ? Rank.values()[rank.ordinal() + 1] : null;
            double typical = rp.typical(30);
            out.append(String.format(Locale.ROOT, "%s\t%s\t%s\t%s\t%d\t%.1f\t%.0f\t%.0f\t%.0f\t%.1f\t%s%n", title(rank),
                placeOfRank(rank).getShortName(), c[1], c[2], rp.runs, 100.0 * rp.wins / rp.runs,
                (rp.coinsWin + rp.coinsLoss) / rp.runs, rp.median(), typical, rp.turns / (double) rp.runs,
                next == null || typical <= 0 ? "-" : String.format(Locale.ROOT, "%.0f", next.getPrice() / typical)));
        }
        try {
            Path profilesOut = Paths.get(System.getProperty("lucky.simulation.out", "build/simulation/equilibrage.md"))
                .resolveSibling("profils_par_rang.md");
            if (profilesOut.getParent() != null) Files.createDirectories(profilesOut.getParent());
            Files.writeString(profilesOut, describe.toString(), StandardCharsets.UTF_8);
            System.out.println("Profils écrits dans " + profilesOut.toAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toString();
    }

    // -------------------------------------------------------------------------
    // Journal des descentes : ./gradlew :core:simulate -Pjournal=<fichier> -Pstake=15 (-PrpRuns=100)
    // -------------------------------------------------------------------------

    /** Une descente du journal ({@link fr.astratime.lucky.progress.DescentLog}) : soldat, puis roi s'il l'a battu. */
    record LoggedDescent(String group, Place place, Dungeon dungeon, boolean hard, Rank rank, int prestige,
                         Map<String, Integer> deck, List<Symbol> machine, int stake, boolean won, double coins) {}

    /** @return les descentes du journal {@code file}, dans l'ordre (un soldat battu sans roi joué ensuite ne compte pas). */
    static List<LoggedDescent> readJournal(Path file) throws IOException {
        List<LoggedDescent> descents = new ArrayList<>();
        String[] pending = null;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.isBlank() || line.startsWith("date;")) continue;
            String[] f = line.split(";", -1);
            if (f.length < 15) continue;
            boolean king = f[6].equals("roi");
            boolean won = Boolean.parseBoolean(f[7]);
            if (!king) {
                pending = null;
                if (won) pending = f; // le roi suit
                else descents.add(descent(f, false, Double.parseDouble(f[10])));
            } else if (pending != null && pending[2].equals(f[2])) {
                descents.add(descent(f, won, Double.parseDouble(f[10])));
                pending = null;
            }
        }
        return descents;
    }

    private static LoggedDescent descent(String[] f, boolean won, double coins) {
        Map<String, Integer> deck = new LinkedHashMap<>();
        for (String card : f[13].split("\\|")) {
            if (card.isEmpty()) continue;
            int colon = card.lastIndexOf(':');
            deck.put(card.substring(0, colon), Integer.parseInt(card.substring(colon + 1)));
        }
        List<Symbol> machine = new ArrayList<>();
        for (String symbol : f[14].split("\\|")) if (!symbol.isEmpty()) machine.add(Symbol.valueOf(symbol));
        Rank rank = f[4].isEmpty() ? null : Rank.valueOf(f[4]);
        String group = String.join(";", f[1], f[3], f[4], f[5], f[12], f[13], f[14]);
        return new LoggedDescent(group, Place.valueOf(f[1]), Dungeon.valueOf(f[2]), Boolean.parseBoolean(f[3]), rank,
            Integer.parseInt(f[5]), deck, machine, Integer.parseInt(f[12]), won, coins);
    }

    /**
     * Compare le vrai joueur du journal au joueur malin : pour chaque équipement du journal (lieu, difficulté, rang,
     * Prestige, Mise, deck, machine) et chaque donjon joué, le joueur malin fait {@code -PrpRuns} descentes avec
     * exactement ce deck et cette machine. Victoires et pièces par descente, réelles et simulées.
     */
    String journalReport(Path file) throws IOException {
        int runs = Integer.getInteger("lucky.rpRuns", 100);
        List<LoggedDescent> descents = readJournal(file);
        Map<String, List<LoggedDescent>> byCase = new LinkedHashMap<>();
        for (LoggedDescent d : descents) byCase.computeIfAbsent(d.group() + ';' + d.dungeon(), k -> new ArrayList<>()).add(d);
        Map<String, RP> simulated = new ConcurrentHashMap<>();
        Map<String, Gear> gears = new LinkedHashMap<>();
        for (List<LoggedDescent> list : byCase.values()) {
            LoggedDescent first = list.get(0);
            gears.computeIfAbsent(first.group(), g -> new Gear("Journal " + gears.size(), first.deck(), first.machine(),
                true, false, true));
        }
        List<Runnable> jobs = new ArrayList<>();
        for (Map.Entry<String, Gear> g : gears.entrySet()) {
            LoggedDescent first = descents.stream().filter(d -> d.group().equals(g.getKey())).findFirst().orElseThrow();
            jobs.add(() -> rankPlace(first.place(), first.hard(), first.rank(), first.prestige(), g.getValue(), true, runs, SEED)
                .forEach((name, rp) -> simulated.put(g.getKey() + ';' + name, rp)));
        }
        jobs.parallelStream().forEach(Runnable::run);
        StringBuilder out = new StringBuilder(String.format(Locale.ROOT,
            "# Journal des descentes comparé au joueur malin%n%n%d descentes lues dans %s ; Mise du robot %d %%, %d descentes simulées par donjon.%n%n",
            descents.size(), file.getFileName(), STAKE.percent, runs));
        out.append("| Équipement | Lieu | Rang | Donjon | Descentes réelles | Victoires réelles | Victoires simulées | Pièces réelles / descente | Pièces simulées / descente |\n");
        out.append("| --- | --- | --- | --- | --- | --- | --- | --- | --- |\n");
        int realTotal = 0, realWins = 0;
        double simWinSum = 0;
        for (Map.Entry<String, List<LoggedDescent>> c : byCase.entrySet()) {
            List<LoggedDescent> list = c.getValue();
            LoggedDescent first = list.get(0);
            RP rp = simulated.get(first.group() + ';' + first.dungeon().getShortName());
            int wins = (int) list.stream().filter(LoggedDescent::won).count();
            double coins = list.stream().mapToDouble(LoggedDescent::coins).average().orElse(0);
            double simWin = rp == null || rp.runs == 0 ? 0 : 100.0 * rp.wins / rp.runs;
            double simCoins = rp == null || rp.runs == 0 ? 0 : (rp.coinsWin + rp.coinsLoss) / rp.runs;
            realTotal += list.size();
            realWins += wins;
            simWinSum += simWin * list.size();
            String gear = gears.get(first.group()).name() + (first.hard() ? " (difficile)" : "")
                + (first.stake() != STAKE.percent ? " Mise réelle " + first.stake() + " %" : "");
            out.append(String.format(Locale.ROOT, "| %s | %s | %s | %s | %d | %.0f %% | %.0f %% | %s | %s |%n", gear,
                first.place().getShortName(), title(first.rank()), first.dungeon().getShortName(), list.size(),
                100.0 * wins / list.size(), simWin, abbreviated(coins), abbreviated(simCoins)));
        }
        if (realTotal > 0) {
            out.append(String.format(Locale.ROOT, "%nEn tout : %.0f %% de victoires réelles, %.0f %% simulées (même mélange de donjons). "
                + "Avec %d descentes réelles, la marge d'erreur est d'environ ±%.0f points.%n",
                100.0 * realWins / realTotal, simWinSum / realTotal, realTotal, 100 / Math.sqrt(realTotal)));
        }
        out.append("\n## Équipements\n\n");
        for (Map.Entry<String, Gear> g : gears.entrySet()) {
            out.append("- ").append(g.getValue().name()).append(" : ").append(String.join(", ", g.getValue().deck().keySet()))
                .append(" ; machine ").append(g.getValue().reels()).append('\n');
        }
        return out.toString();
    }

    // -------------------------------------------------------------------------
    // Exploration difficile : ./gradlew :core:simulate -Phardplace -Pstake=15 (-PrpRuns=100 -Plevel=N -PhardRank=Roi)
    // -------------------------------------------------------------------------

    /**
     * Chaque lieu en mode normal (le Désert, la référence) puis en difficile, avec le rang {@code lucky.hardRank}
     * (Légende par défaut) et le Prestige {@code -Plevel} : victoires, pièces par descente et par tour.
     */
    String hardPlaceReport() {
        int runs = Integer.getInteger("lucky.rpRuns", 100);
        String rankName = System.getProperty("lucky.hardRank", "Légende");
        Rank rank = allRanks().stream().filter(r -> title(r).equals(rankName)).findFirst().orElse(Rank.LEGENDE);
        List<Gear> gears = SUIT_GEARS.subList(1, SUIT_GEARS.size()); // les robots malins
        List<Object[]> cases = new ArrayList<>();
        cases.add(new Object[] {Place.DESERT, false});
        for (Place place : Place.values()) cases.add(new Object[] {place, true});
        Map<String, RP> results = new ConcurrentHashMap<>();
        List<Runnable> jobs = new ArrayList<>();
        for (Gear gear : gears) for (Object[] c : cases) {
            if (System.getProperty("lucky.onlyGear") != null && !List.of(System.getProperty("lucky.onlyGear").split(",")).contains(gear.name())) continue;
            Place place = (Place) c[0];
            boolean hard = (boolean) c[1];
            jobs.add(() -> {
                RP total = new RP();
                rankPlace(place, hard, rank, gear, true, runs, SEED).values().forEach(total::add);
                results.put(gear.name() + '|' + place + '|' + hard, total);
            });
        }
        jobs.parallelStream().forEach(Runnable::run);
        StringBuilder out = new StringBuilder("gear\trank\tprestige\tplace\thard\tspinCost\truns\twinPercent\tavgCoinsRun"
            + "\tmedianCoinsRun\ttypical30\tturnsPerRun\tcoinsPerTurn\n");
        for (Gear gear : gears) for (Object[] c : cases) {
            RP rp = results.get(gear.name() + '|' + c[0] + '|' + c[1]);
            if (rp == null) continue;
            Place place = (Place) c[0];
            boolean hard = (boolean) c[1];
            double coins = (rp.coinsWin + rp.coinsLoss) / rp.runs;
            out.append(String.format(Locale.ROOT, "%s\t%s\t%d\t%s\t%b\t%d\t%d\t%.1f\t%.0f\t%.0f\t%.0f\t%.1f\t%.0f%n",
                gear.name(), title(rank), PRESTIGE, place.getShortName(), hard, place.getSpinCost(hard), rp.runs,
                100.0 * rp.wins / rp.runs, coins, rp.median(), rp.typical(30), rp.turns / (double) rp.runs,
                coins / Math.max(1, rp.turns / (double) rp.runs)));
        }
        return out.toString();
    }

    /**
     * Parcours d'un Prestige (Astra, 2026-11-26) : au Prestige N, le joueur repart sans rang et rachète ses rangs dans
     * le lieu le plus rentable qu'il a ouvert, le lieu normal de son rang ou un lieu difficile déjà vidé (les N sceaux,
     * de la prairie au lieu N). Pour chaque rang : le meilleur lieu, ses victoires, les pièces d'une descente typique
     * (robots malins Malin, Pique, Coeur, Trèfle mis ensemble) et le nombre de descentes pour le rang suivant au prix
     * actuel. {@code ./gradlew :core:simulate -Pjourney -Pstake=15 (-Plevels=1,5,10 -PrpRuns=60)}
     */
    String journeyReport() {
        int runs = Integer.getInteger("lucky.rpRuns", 60);
        List<Integer> levels = new ArrayList<>();
        for (String l : System.getProperty("lucky.levels", "1,2,3,4,5,6,7,8,9,10").split(",")) levels.add(Integer.parseInt(l));
        List<Gear> gears = new ArrayList<>();
        for (Gear gear : SUIT_GEARS) if (gear != Gear.STARTER && !gear.name().equals("Carreau")) gears.add(gear);
        List<Rank> ranks = new ArrayList<>(allRanks());
        ranks.remove(Rank.LEGENDE);
        Map<String, RP> results = new ConcurrentHashMap<>();
        List<Runnable> jobs = new ArrayList<>();
        for (int level : levels) for (Rank rank : ranks) {
            List<Object[]> candidates = new ArrayList<>();
            candidates.add(new Object[] {placeOfRank(rank), false});
            for (int tier = 1; tier <= level; tier++) candidates.add(new Object[] {Place.atTier(tier), true});
            for (Object[] c : candidates) for (Gear gear : gears) {
                Place place = (Place) c[0];
                boolean hard = (boolean) c[1];
                String key = level + "|" + title(rank) + "|" + place + "|" + hard;
                jobs.add(() -> {
                    RP total = new RP();
                    rankPlace(place, hard, rank, level, gear, true, runs, SEED).values().forEach(total::add);
                    results.merge(key, total, (a, b) -> { RP m = new RP(); m.add(a); m.add(b); return m; });
                });
            }
        }
        jobs.parallelStream().forEach(Runnable::run);
        StringBuilder out = new StringBuilder("prestige\trank\tplace\thard\twinPercent\ttypical30\tbest\tnextRank\tnextPrice\tdescents\n");
        for (int level : levels) for (Rank rank : ranks) {
            Rank next = rank == null ? Rank.values()[0] : Rank.values()[rank.ordinal() + 1];
            String best = null;
            double bestTypical = -1;
            List<String> rows = new ArrayList<>();
            for (Map.Entry<String, RP> e : results.entrySet()) {
                String[] k = e.getKey().split("\\|");
                if (!k[0].equals(String.valueOf(level)) || !k[1].equals(title(rank))) continue;
                double typical = e.getValue().typical(30);
                if (typical > bestTypical) { bestTypical = typical; best = e.getKey(); }
            }
            for (Map.Entry<String, RP> e : results.entrySet()) {
                String[] k = e.getKey().split("\\|");
                if (!k[0].equals(String.valueOf(level)) || !k[1].equals(title(rank))) continue;
                RP rp = e.getValue();
                double typical = rp.typical(30);
                long price = next.getPrice(level);
                rows.add(String.format(Locale.ROOT, "%d\t%s\t%s\t%s\t%.0f\t%.0f\t%s\t%s\t%d\t%.1f", level, title(rank),
                    Place.valueOf(k[2]).getShortName(), k[3], 100.0 * rp.wins / rp.runs, typical, e.getKey().equals(best) ? "*" : "",
                    title(next), price, typical > 0 ? price / typical : Double.POSITIVE_INFINITY));
            }
            Collections.sort(rows);
            rows.forEach(r -> out.append(r).append('\n'));
        }
        return out.toString();
    }

    String rankPlaceReport() {
        int runs = Integer.getInteger("lucky.rpRuns", 200);
        List<Gear> gears = SUIT_GEARS;
        List<Rank> ranks = allRanks();
        Map<String, RP> results = new ConcurrentHashMap<>();
        List<Runnable> jobs = new ArrayList<>();
        for (Gear gear : gears) for (Rank rank : ranks) for (boolean on : new boolean[] {true}) {
            if (System.getProperty("lucky.onlyRank") != null && !List.of(System.getProperty("lucky.onlyRank").split(",")).contains(title(rank))) continue;
            if (System.getProperty("lucky.onlyGear") != null && !List.of(System.getProperty("lucky.onlyGear").split(",")).contains(gear.name())) continue;
            if (POT_LOG != null && !on) continue;
            Place place = placeOfRank(rank);
            jobs.add(() -> rankPlace(place, rank, gear, on, runs, SEED).forEach((name, rp) ->
                results.put(gear.name() + '|' + title(rank) + '|' + on + '|' + name, rp)));
        }
        jobs.parallelStream().forEach(Runnable::run);
        if (POT_LOG != null) {
            StringBuilder log = new StringBuilder("pot\tlines\ttotal\tspinCost\n");
            for (double[] row : POT_LOG) log.append(String.format(Locale.ROOT, "%.0f\t%.0f\t%.0f\t%.0f%n", row[0], row[1], row[2], row[3]));
            return log.toString();
        }
        StringBuilder out = new StringBuilder();
        out.append("gear\trank\tplace\tdungeon\tbonusOn\truns\twins\tsoldierWins\tkingFights\tavgSoldierGains\tavgKingGains"
            + "\tavgCoinsWin\tavgCoinsLoss\tavgCoinsRun\tbonusGamesPerRun\tavgBonusGainsPerRun\tbonusShareOfWinGains\tturnsPerRun\tnextPrice\tmedianCoinsRun\tavgPot\ttypical30\n");
        for (Gear gear : gears) for (Rank rank : ranks) for (boolean on : new boolean[] {true}) {
            if (!results.containsKey(gear.name() + '|' + title(rank) + '|' + on + '|' + placeOfRank(rank).getDungeons().get(0).getShortName())) continue;
            Place place = placeOfRank(rank);
            RP total = new RP();
            List<String> names = new ArrayList<>();
            for (Dungeon dungeon : place.getDungeons()) names.add(dungeon.getShortName());
            names.add("TOUS");
            for (String name : names) {
                RP rp;
                if (name.equals("TOUS")) rp = total;
                else { rp = results.get(gear.name() + '|' + title(rank) + '|' + on + '|' + name); total.add(rp); }
                Rank next = rank == null ? Rank.values()[0] : rank.ordinal() + 1 < Rank.values().length ? Rank.values()[rank.ordinal() + 1] : null;
                out.append(String.format(Locale.ROOT, "%s\t%s\t%s\t%s\t%b\t%d\t%d\t%d\t%d\t%.0f\t%.0f\t%.0f\t%.0f\t%.0f\t%.3f\t%.0f\t%.3f\t%.1f\t%d\t%.0f\t%.0f\t%.0f%n",
                    gear.name(), title(rank), place.getShortName(), name, on, rp.runs, rp.wins, rp.soldierWins, rp.kingFights,
                    rp.soldierWins == 0 ? 0 : rp.soldierGains / rp.soldierWins,
                    rp.wins == 0 ? 0 : rp.kingGains / rp.wins,
                    rp.wins == 0 ? 0 : rp.coinsWin / rp.wins,
                    rp.runs - rp.wins == 0 ? 0 : rp.coinsLoss / (rp.runs - rp.wins),
                    (rp.coinsWin + rp.coinsLoss) / rp.runs,
                    rp.bonusGames / (double) rp.runs, rp.bonusGains / rp.runs,
                    rp.finalGainsWin == 0 ? 0 : rp.bonusGainsWin / rp.finalGainsWin,
                    rp.turns / (double) rp.runs, next == null ? 0 : next.getPrice(), rp.median(), rp.potEnd / rp.runs, rp.typical(30)));
            }
        }
        return out.toString();
    }

    // -------------------------------------------------------------------------
    // Le robot
    // -------------------------------------------------------------------------

    private GameController controller(RankBonus bonus, Gear gear) {
        return new GameController(() -> gear.deck() == null ? CardLoader.loadStarterDeck(reader) : build(gear.deck(), gear.upgraded(), gear.plus()),
            cards, !gear.buysAtStall() ? Map.of() : gear.smart() && !OLD_BOT ? explorationStall : stall,
            deck -> new Player("Robot", Player.BASE_HP, deck, bonus, gear.reels()));
    }

    /** @return les cartes de {@code deck}, chacune dans sa version « + » quand elle existe. */
    private List<Card> build(Map<String, Integer> deck, boolean upgraded) {
        return build(deck, upgraded, java.util.Set.of());
    }

    /** Comme {@link #build(Map, boolean)} ; les cartes de {@code plus} sont aussi en « + ». */
    private List<Card> build(Map<String, Integer> deck, boolean upgraded, java.util.Set<String> plus) {
        List<Card> built = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : deck.entrySet()) {
            boolean up = upgraded || plus.contains(entry.getKey());
            String id = up && cards.apply(entry.getKey()).isUpgradable() ? Card.upgradedId(entry.getKey()) : entry.getKey();
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
        if (!playCards) return stakeAndSpin(game);
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
            resolveChoice(game, random);
        }
        return stakeAndSpin(game);
    }

    /** Le soldat est doré une fois sur {@link DungeonRun#GOLDEN_CHANCE}, comme dans le jeu (PV x2). */
    private static void rollGolden(GameController game, Random random) {
        if (!OLD_BOT && random.nextInt(DungeonRun.GOLDEN_CHANCE) == 0) game.getGameState().getEnemy().gild();
    }

    /**
     * Le joueur malin répond au choix qu'une carte vient d'ouvrir : pari, rouleau truqué et Atelier du Joker sur un
     * de ses symboles d'attaque, Calibrage sur un symbole qui n'attaque ni ne rapporte ; la roulette russe reste au
     * hasard (ses cartes sont face cachée).
     */
    private static void resolveSmartChoice(GameController game, Random random) {
        CardChoice choice = game.getPendingChoice();
        if (OLD_BOT || choice == null || choice instanceof RouletteChoice) {
            resolveChoice(game, random);
            return;
        }
        Player player = game.getGameState().getPlayer();
        List<Symbol> options = game.getBetOptions();
        List<Symbol> attacks = new ArrayList<>(options);
        attacks.retainAll(SymbolRegistry.getAttackSymbols(player));
        List<Symbol> useless = new ArrayList<>(options);
        useless.removeIf(symbol -> SymbolRegistry.getAttackSymbols().contains(symbol) || SymbolRegistry.isGainSymbol(symbol));
        if (choice instanceof fr.astratime.lucky.entities.choices.CalibrationChoice) {
            game.calibrate(pick(useless.isEmpty() ? options : useless, random));
            return;
        }
        Symbol best = pick(attacks.isEmpty() ? options : attacks, random);
        if (choice instanceof BetChoice) game.placeBet(best);
        else if (choice instanceof RiggedReelChoice) game.rigReel(best);
        else if (choice instanceof fr.astratime.lucky.entities.choices.JokerWorkshopChoice) game.shapeJoker(best);
        else resolveChoice(game, random);
    }

    /** Le robot répond au hasard au choix qu'une carte vient d'ouvrir (pari, roulette, rouleau truqué...). */
    private static void resolveChoice(GameController game, Random random) {
        {
            CardChoice choice = game.getPendingChoice();
            if (choice instanceof BetChoice) {
                game.placeBet(pick(game.getBetOptions(), random));
            } else if (choice instanceof RouletteChoice roulette) {
                game.pickRouletteCard(random.nextInt(roulette.cursed().size()));
            } else if (choice instanceof RiggedReelChoice) {
                game.rigReel(pick(game.getBetOptions(), random));
            } else if (choice instanceof fr.astratime.lucky.entities.choices.JokerWorkshopChoice) {
                game.shapeJoker(pick(game.getBetOptions(), random));
            } else if (choice instanceof fr.astratime.lucky.entities.choices.CalibrationChoice) {
                game.calibrate(pick(game.getBetOptions(), random));
            }
        }
    }

    /** Cartes d'attaque du joueur malin : elles comptent pour décider d'un gros tour. */
    private static boolean isAttackCard(Card card) {
        String id = Card.baseId(card.getId());
        return card.getSuit() != null || id.equals("marteau_du_cordonnier") || id.equals("pacte_de_sang")
            || id.equals("pierre_a_aiguiser") || id.equals("etoile_filante");
    }

    /** @return l'ordre de jeu d'une carte pour le joueur malin (petit d'abord), ou -1 s'il la garde. */
    private static int smartOrder(GameController game, Card card, boolean king) {
        String id = Card.baseId(card.getId());
        Player player = game.getGameState().getPlayer();
        switch (id) {
            case "in_the_sleeve":
                return game.getPlayLimit() >= 6 ? -1 : 0;
            case "cocktail_des_abysses": case "draw_3": case "draw_2": case "joker_de_cour":
                return 1;
            case "rayon_du_phare": case "masque_du_joker": // l'ennemi passe son tour : gardé pour le roi ou en danger
                return king || player.getHpRatio() < 0.5f ? 2 : -1;
            case "pacte_de_sang":
                return player.getHpRatio() > 0.6f ? 3 : -1;
            case "marteau_du_cordonnier": case "chope": case "pierre_a_aiguiser":
                return 3;
            case "heraut": case "fossoyeur": // Valet à l'As : ils font venir des cartes
                return 1;
            case "douze_coups": // il attend d'avoir assez de Ressorts (ou le roi)
                return king || player.getLastingEffects().getCourtAndClock().getSprings() >= 6 ? 6 : -1;
            case "1_trefle": case "etoile_filante": case "etoile_du_berger": case "etoile_d_or":
                // il attend que la Bonne étoile garantisse le Bingo (la carte ajoute elle-même 10 points)
                return player.getLastingEffects().getLuckyStar() >= 20 ? 6 : -1;
            case "bingo":
                return 9;
            case "golden_reel": case "corruption": case "russian_roulette": // achetées pour ce tour : toujours jouées
                return OLD_BOT ? 5 : 8;
            default:
                return id.startsWith("1_") ? 4 : 5;
        }
    }

    /**
     * Parmi les cartes que le joueur malin veut bien jouer, celles qui forment la plus grosse combinaison
     * (Couleur, Paire, Brelan, Suite, Carré s'additionnent) dans les {@code slots} cartes qu'il peut encore jouer ;
     * à égalité, celles qu'il joue d'habitude en premier ({@link #smartOrder}). Le Bingo acheté reste à part.
     *
     * @return la main à jouer, dans l'ordre de {@link #smartOrder}
     */
    private static List<Card> bestComboHand(GameController game, List<Card> hand, boolean king, int slots,
                                            boolean boughtBingo) {
        Player player = game.getGameState().getPlayer();
        List<Card> candidates = new ArrayList<>();
        List<Card> bingos = new ArrayList<>();
        for (Card card : hand) {
            int order = smartOrder(game, card, king);
            if (order < 0 || game.unplayableReason(card) != null) continue;
            (order >= 8 && (order == 8 || boughtBingo) ? bingos : candidates).add(card);
        }
        int k = Math.min(slots - bingos.size(), candidates.size());
        List<Card> chosen;
        if (k <= 0) chosen = new ArrayList<>();
        else if (candidates.size() <= k || candidates.size() > 16) chosen = new ArrayList<>(candidates);
        else {
            List<Card> played = player.getPlayedCards();
            int n = candidates.size();
            int bestMask = 0;
            double bestScore = Double.NEGATIVE_INFINITY;
            for (int mask = 0; mask < (1 << n); mask++) {
                if (Integer.bitCount(mask) != k) continue;
                List<Card> pickCards = new ArrayList<>(played);
                double order = 0;
                for (int i = 0; i < n; i++) {
                    if ((mask & (1 << i)) == 0) continue;
                    pickCards.add(candidates.get(i));
                    order += smartOrder(game, candidates.get(i), king);
                }
                double score = Combo.totalFactor(Combo.formed(pickCards)) * 1000 - order;
                if (score > bestScore) { bestScore = score; bestMask = mask; }
            }
            chosen = new ArrayList<>();
            for (int i = 0; i < n; i++) if ((bestMask & (1 << i)) != 0) chosen.add(candidates.get(i));
        }
        chosen.addAll(bingos);
        chosen.sort(Comparator.comparingInt(card -> smartOrder(game, card, king)));
        return chosen;
    }

    /** @return les places à garder pour les cartes achetées à l'échoppe pour ce tour et pas encore jouées. */
    private static int reserved(GameController game, Player player, boolean king, boolean boughtBingo) {
        int count = 0;
        for (Card card : player.getCurrentHand()) {
            int order = smartOrder(game, card, king);
            if (order == 8 || order == 9 && boughtBingo) count++;
        }
        return count;
    }

    /** Achète {@code id} à l'échoppe s'il en garde de quoi payer 3 tirages ; @return {@code true} si la carte est sur la table. */
    private static boolean buy(GameController game, String id, double maxShareOfGains) {
        Player player = game.getGameState().getPlayer();
        for (GameController.ShopOffer offer : game.getShopOffers()) {
            if (!offer.card().getId().equals(id) || game.unavailableReason(offer) != null) continue;
            if (player.getGains() - offer.price() < 3L * game.getSpinCost()) return false;
            if (offer.price() > maxShareOfGains * player.getGains()) return false;
            GameController.Purchase purchase = game.buy(offer);
            return purchase != null && purchase.addedToHand();
        }
        return false;
    }

    /**
     * Un tour du « joueur malin » : il joue d'abord Dans la manche et les pioches, garde Rayon du phare et Masque
     * du Joker pour le roi (ou quand il a moins de la moitié de ses PV), puis choisit les cartes qui font la plus
     * grosse combinaison ({@link #bestComboHand}). Quand il peut encore jouer au moins 3 cartes d'attaque, il fait
     * un gros tour : Bingo de l'échoppe (joué en dernier), Rouleau doré, et contre un roi Corruption et Roulette
     * russe. Il ne fait pas de gros tour quand l'ennemi a atteint son seuil de phase (Garde : il ne perd plus de PV
     * avant son tour), et achète la Main du croupier quand il lui reste des cartes à jouer. Pari, rouleau truqué :
     * sur un de ses symboles d'attaque ({@link #resolveSmartChoice}).
     *
     * <p>Avec {@code -PoldBot} : l'ancien joueur malin (ordre fixe, achète seulement Bingo, choix au hasard).
     */
    private static TurnResult playSmartTurn(GameController game, Random random, boolean king) {
        game.drawCards();
        Player player = game.getGameState().getPlayer();
        Enemy enemy = game.getGameState().getEnemy();
        boolean boughtBingo = false;
        for (int pass = 0; pass < 2; pass++) {
            // pass 0 : préparation (Dans la manche, pioches) ; pass 1 : le reste
            List<Card> hand = new ArrayList<>(player.getCurrentHand());
            Collections.shuffle(hand, random);
            hand.sort(Comparator.comparingInt(card -> smartOrder(game, card, king)));
            if (pass == 1 && !boughtBingo && !(OLD_BOT ? false : enemy.isPhaseWallReached())) {
                int slots = game.getPlayLimit() - game.getCardsPlayedThisTurn();
                long attacks = hand.stream().filter(BalanceSimulator::isAttackCard)
                    .filter(card -> game.unplayableReason(card) == null).count();
                if (slots >= 4 && attacks >= 3) {
                    if (OLD_BOT) {
                        for (GameController.ShopOffer offer : game.getShopOffers()) {
                            if (offer.card().getId().equals("bingo") && game.unavailableReason(offer) == null
                                && player.getGains() - offer.price() >= 3L * game.getSpinCost()) {
                                GameController.Purchase purchase = game.buy(offer);
                                boughtBingo = purchase != null && purchase.addedToHand();
                            }
                        }
                    } else {
                        // il garde au moins 3 places pour ses cartes d'attaque
                        int extras = slots - 3;
                        boughtBingo = extras > 0 && buy(game, "bingo", 0.5);
                        if (boughtBingo) extras--;
                        List<String> picks = king && enemy.getHpRatio() > 0.3f
                            ? List.of("corruption", "russian_roulette", "golden_reel") : List.of("golden_reel");
                        for (String id : picks) if (extras > 0 && buy(game, id, 0.2)) extras--;
                    }
                }
                hand = new ArrayList<>(player.getCurrentHand());
                Collections.shuffle(hand, random);
                hand.sort(Comparator.comparingInt(card -> smartOrder(game, card, king)));
            }
            if (pass == 1 && !OLD_BOT) {
                hand = bestComboHand(game, hand, king, game.getPlayLimit() - game.getCardsPlayedThisTurn(), boughtBingo);
            }
            for (int i = 0; i < hand.size(); i++) {
                Card card = hand.get(i);
                if (game.isPlayLimitReached() || game.isHandLocked()) break;
                if (!player.getCurrentHand().contains(card)) continue;
                int order = smartOrder(game, card, king);
                if (order < 0 || (pass == 0 && order > 1)) continue;
                if (order == 9 && !boughtBingo) continue;
                // garde une place pour Bingo (et les cartes achetées pour ce tour)
                if (boughtBingo && order != 9 && game.getPlayLimit() - game.getCardsPlayedThisTurn() <= 1) continue;
                if (!OLD_BOT && order < 8 && game.getPlayLimit() - game.getCardsPlayedThisTurn() <= reserved(game, player, king, boughtBingo)) continue;
                if (game.unplayableReason(card) != null) continue;
                game.playCard(card);
                resolveSmartChoice(game, random);
            }
            if (pass == 1 && !OLD_BOT && !game.isPlayLimitReached() && !game.isHandLocked()) {
                // cartes piochées par la Main du croupier ou les pioches : il complète sa main
                for (Card card : bestComboHand(game, new ArrayList<>(player.getCurrentHand()), king,
                        game.getPlayLimit() - game.getCardsPlayedThisTurn(), false)) {
                    if (game.isPlayLimitReached() || game.isHandLocked()) break;
                    if (smartOrder(game, card, king) == 9 && !boughtBingo) continue;
                    if (game.unplayableReason(card) != null) continue;
                    game.playCard(card);
                    resolveSmartChoice(game, random);
                }
            }
        }
        return stakeAndSpin(game);
    }

    private static <T> T pick(List<T> options, Random random) { return options.get(random.nextInt(options.size())); }

    private static boolean isOver(GameState state) {
        return state.getPlayer().isDefeated() || state.getEnemy().isDefeated();
    }
}
