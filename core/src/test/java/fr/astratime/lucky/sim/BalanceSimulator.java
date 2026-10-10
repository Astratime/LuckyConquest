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
import java.util.Random;
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
 *   <li><b>Donjons</b> : victoires et pièces gagnées (au roi) par donjon, selon le rang du joueur.</li>
 * </ol>
 *
 * Le robot joue le deck de départ (ou aucune carte, pour les gains de base) : ses
 * cartes au hasard, sans échoppe ni Mise.
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
        static final Sizes QUICK = new Sizes(500, 2_000, 4, 150);
    }

    private static final long SEED = 42L;
    /** Rangs comparés pour les donjons (aucun, puis quelques paliers). */
    private static final List<Rank> RANKS = new ArrayList<>(java.util.Arrays.asList(
        null, Rank.AVARE, Rank.FLAMBEUR, Rank.REQUIN, Rank.MAGNAT));

    private final Sizes sizes;
    private final CardLoader.AssetReader reader;
    private final Function<String, Card> cards;

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
        spinSection(out);
        bonusSection(out);
        dungeonSection(out);
        return out.toString();
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
        GameController game = controller(RankBonus.NONE);
        game.restart(kind);
        int cost = kind.getSpinCost();
        List<Double> ratios = new ArrayList<>();
        double sum = 0;
        int pairs = 0, bingos = 0, debt = 0;
        for (int turn = 0; turn < turns; turn++) {
            if (isOver(game.getGameState())) game.restart(kind);
            TurnResult result = playTurn(game, random, playCards);
            if (game.getDebt() != SpinEconomy.Debt.NONE) debt++;
            int gains = result.getGainsFromPairOrJackpot();
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

    /** Descentes d'un donjon : part de victoires et pièces des victoires. */
    record DungeonStats(double winRate, long medianCoins) {}

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
        Random random = new Random(seed);
        GameController game = controller(rank == null ? RankBonus.NONE : rank.getBonus());
        game.setPlaceRule(place.getRule());
        List<Long> coins = new ArrayList<>();
        for (int run = 0; run < runs; run++) {
            game.restart(dungeon.getSoldier());
            if (!fight(game, random)) continue;
            game.startCombat(dungeon.getKing());
            if (!fight(game, random)) continue;
            coins.add((long) PlayerProfile.combatReward(true, true, game.getGameState().getPlayer().getGains()));
        }
        Collections.sort(coins);
        return new DungeonStats(coins.size() / (double) runs, coins.isEmpty() ? 0 : coins.get(coins.size() / 2));
    }

    /** Joue le combat en cours jusqu'au bout (au plus {@link Sizes#maxTurns} tours). @return {@code true} si gagné. */
    private boolean fight(GameController game, Random random) {
        for (int turn = 0; turn < sizes.maxTurns() && !isOver(game.getGameState()); turn++) playTurn(game, random, true);
        return game.getGameState().getEnemy().isDefeated();
    }

    // -------------------------------------------------------------------------
    // Le robot
    // -------------------------------------------------------------------------

    private GameController controller(RankBonus bonus) {
        return new GameController(() -> CardLoader.loadStarterDeck(reader), cards, Map.of(),
            deck -> new Player("Robot", Player.BASE_HP, deck, bonus, Symbol.classicReels()));
    }

    /** Un tour du robot : il pioche, joue ses cartes au hasard (choix au hasard) si {@code playCards}, puis tire. */
    private static TurnResult playTurn(GameController game, Random random, boolean playCards) {
        game.drawCards();
        if (!playCards) return game.spin();
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
