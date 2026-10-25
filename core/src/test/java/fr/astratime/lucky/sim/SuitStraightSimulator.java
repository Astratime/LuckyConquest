package fr.astratime.lucky.sim;

import fr.astratime.lucky.controllers.GameController;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.RankBonus;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.loaders.CardLoader;
import fr.astratime.lucky.progress.Rank;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Compare les dégâts des quatre suites (Pique, Coeur, Trèfle, Carreau) : un deck de
 * 20 cartes d'une seule suite (As au Roi, plus un second 2 à 8), joué contre une
 * cible qui encaisse tout
 * (une neuve quand elle a perdu la moitié de ses PV : les coups ne sont pas plafonnés). Chaque tour, le robot pose la plus longue suite de rangs
 * qu'il a en main (toutes de la même couleur : Suite + Couleur), complétée par ses
 * autres cartes jusqu'à la limite de cartes du tour. Il ne mise pas.
 * <p>
 * Puis, contre les vrais ennemis des lieux (le renvoi des Carreaux compte) : une seule carte
 * posée par tour, et les victoires en donjon de chaque deck d'une seule suite.
 * <p>
 * Lancement : {@code ./gradlew :core:simulateSuits} (ou -Pquick).
 */
public final class SuitStraightSimulator {

    private static final List<String> SUITS = List.of("pique", "coeur", "trefle", "carreau");
    private static final int TURNS = 8;

    public static void main(String[] args) throws Exception {
        boolean quick = args.length > 0 && args[0].equals("--quick");
        int fights = quick ? 300 : 3000;
        Path assets = Path.of(System.getProperty("lucky.assets", "../assets"));
        Function<String, Card> cards = CardLoader.cardFactory(path -> {
            try {
                return Files.readString(assets.resolve(path), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        System.out.printf(Locale.ROOT, "Dégâts moyens sur %d tours (%d combats par suite), sans gains de départ%n",
            TURNS, fights);
        System.out.println("| Rang | Pique | Coeur | Trèfle | Carreau |");
        System.out.println("|---|---:|---:|---:|---:|");
        for (String rankName : List.of("AUCUN", "FLAMBEUR", "MAGNAT", "LEGENDE")) {
            RankBonus bonus = "AUCUN".equals(rankName) ? RankBonus.NONE : Rank.valueOf(rankName).getBonus();
            StringBuilder row = new StringBuilder("| " + rankName + " |");
            for (String suit : SUITS) {
                double total = 0;
                for (int i = 0; i < fights; i++) total += fight(cards, suit, bonus);
                row.append(String.format(Locale.ROOT, " %,.0f |", total / fights));
            }
            System.out.println(row);
        }
        oneCard(cards, quick ? 300 : 3000);
        dungeons(cards, quick ? 20 : 200);
    }

    /**
     * Une seule carte posée par tour (le deck n'a qu'elle), contre le soldat de chaque lieu,
     * qui attaque : dégâts moyens par tour (coups, renvoi, contre-attaque), en % de ses PV max.
     * Mesure ce que vaut une carte de chaque suite dans un deck mélangé.
     */
    static void oneCard(Function<String, Card> cards, int turns) {
        List<Place> places = List.of(Place.values());
        for (String rankName : List.of("AUCUN", "MAGNAT")) {
            RankBonus bonus = "AUCUN".equals(rankName) ? RankBonus.NONE : Rank.valueOf(rankName).getBonus();
            System.out.printf(Locale.ROOT, "%nUne carte par tour, %s : dégâts moyens par tour, en %% des PV max du soldat (%d tours)%n",
                rankName, turns);
            StringBuilder head = new StringBuilder("| Carte |"), line = new StringBuilder("|---|");
            for (Place place : places) { head.append(' ').append(place.getShortName()).append(" |"); line.append("---:|"); }
            System.out.println(head);
            System.out.println(line);
            List<String> ids = new ArrayList<>(List.of("aucune"));
            for (String suit : SUITS) for (int rank : new int[] {2, 7, 13, 1}) ids.add(rank + "_" + suit);
            List<String> rows = ids.parallelStream().map(id -> {
                StringBuilder row = new StringBuilder("| " + id + " |");
                for (Place place : places) {
                    row.append(String.format(Locale.ROOT, " %.2f |",
                        oneCardDamage(cards, id, bonus, place, place.getDungeons().get(0), turns)));
                }
                return row.toString();
            }).toList();
            rows.forEach(System.out::println);
        }
    }

    /** @return les dégâts moyens par tour en % des PV max, en posant {@code id} chaque tour ("aucune" : rien). */
    static double oneCardDamage(Function<String, Card> cards, String id, RankBonus bonus, Place place, Dungeon dungeon,
                                int turns) {
        List<Card> deck = new ArrayList<>();
        if (!id.equals("aucune")) for (int i = 0; i < 20; i++) deck.add(cards.apply(id));
        GameController game = new GameController(() -> new ArrayList<>(deck), cards, Map.of(),
            list -> new Player("Joueur", Player.BASE_HP, list, bonus, Symbol.classicReels()));
        game.setPlaceRule(place.getRule());
        game.restart(dungeon.getSoldier());
        double percent = 0;
        for (int turn = 0; turn < turns; turn++) {
            GameState state = game.getGameState();
            if (state.getPlayer().isDefeated() || state.getEnemy().isDefeated()) {
                game.restart(dungeon.getSoldier());
                state = game.getGameState();
            }
            game.drawCards();
            for (Card card : state.getPlayer().getCurrentHand()) {
                if (game.unplayableReason(card) == null) { game.playCard(card); break; }
            }
            double maxHp = game.getGameState().getEnemy().getMaxHp();
            TurnResult result = game.spin();
            Set<Event> seen = Collections.newSetFromMap(new IdentityHashMap<>());
            seen.addAll(result.getEvents());
            seen.addAll(result.getPistolEvents());
            seen.addAll(result.getEnemyTurnEvents());
            if (result.getEnemyTurn() != null) seen.addAll(result.getEnemyTurn().events());
            double damage = 0;
            for (Event event : seen) {
                if (event instanceof EnemyDamagedEvent hit) damage += hit.damage;
                if (event instanceof fr.astratime.lucky.entities.events.DamageReflectedEvent hit) damage += hit.damage;
            }
            percent += Math.min(100, 100 * damage / maxHp);
        }
        return percent / turns;
    }

    /**
     * Donjons de l'Exploration (soldat puis roi, vrais ennemis qui attaquent : le renvoi des
     * Carreaux compte) : part de victoires de chaque deck d'une seule suite, par rang.
     */
    static void dungeons(Function<String, Card> cards, int runs) {
        List<String> ranks = List.of("AUCUN", "FLAMBEUR", "MAGNAT", "LEGENDE");
        System.out.printf(Locale.ROOT, "%nVictoires en donjon (soldat puis roi, %d descentes), deck d'une seule suite%n", runs);
        for (String rankName : ranks) {
            RankBonus bonus = "AUCUN".equals(rankName) ? RankBonus.NONE : Rank.valueOf(rankName).getBonus();
            System.out.printf(Locale.ROOT, "%n%s%n| Donjon | Pique | Coeur | Trèfle | Carreau |%n|---|---:|---:|---:|---:|%n", rankName);
            for (Place place : Place.values()) {
                for (Dungeon dungeon : place.getDungeons()) {
                    String[] cells = SUITS.parallelStream().map(suit -> {
                        int wins = 0;
                        for (int run = 0; run < runs; run++) if (descend(cards, suit, bonus, place, dungeon)) wins++;
                        return String.format(Locale.ROOT, " %.0f %% |", 100.0 * wins / runs);
                    }).toArray(String[]::new);
                    System.out.println("| " + place.getShortName() + " : " + dungeon.getShortName() + " |" + String.join("", cells));
                }
            }
        }
    }

    /** @return {@code true} si le soldat puis le roi de {@code dungeon} sont battus avec un deck de {@code suit}. */
    static boolean descend(Function<String, Card> cards, String suit, RankBonus bonus, Place place, Dungeon dungeon) {
        List<Card> deck = new ArrayList<>();
        for (int rank = 1; rank <= 13; rank++) deck.add(cards.apply(rank + "_" + suit));
        for (int rank = 2; rank <= 8; rank++) deck.add(cards.apply(rank + "_" + suit));
        GameController game = new GameController(() -> new ArrayList<>(deck), cards, Map.of(),
            list -> new Player("Joueur", Player.BASE_HP, list, bonus, Symbol.classicReels()));
        game.setPlaceRule(place.getRule());
        game.restart(dungeon.getSoldier());
        if (!playOut(game)) return false;
        game.startCombat(dungeon.getKing());
        return playOut(game);
    }

    private static boolean playOut(GameController game) {
        GameState state = game.getGameState();
        for (int turn = 0; turn < 300 && !state.getPlayer().isDefeated() && !state.getEnemy().isDefeated(); turn++) {
            game.drawCards();
            for (Card card : choose(state.getPlayer().getCurrentHand(), game.getPlayLimit())) {
                if (game.isPlayLimitReached() || game.isHandLocked()) break;
                if (game.unplayableReason(card) == null) game.playCard(card);
            }
            game.spin();
            state = game.getGameState();
        }
        return state.getEnemy().isDefeated();
    }

    /** @return les dégâts infligés en {@link #TURNS} tours avec un deck de la suite {@code suit}. */
    static double fight(Function<String, Card> cards, String suit, RankBonus bonus) throws Exception {
        List<Card> deck = new ArrayList<>();
        for (int rank = 1; rank <= 13; rank++) deck.add(cards.apply(rank + "_" + suit));
        for (int rank = 2; rank <= 8; rank++) deck.add(cards.apply(rank + "_" + suit));
        GameController controller = new GameController(() -> new ArrayList<>(deck), cards, Map.of(),
            list -> new Player("Joueur", Player.BASE_HP, list, bonus, Symbol.classicReels()));
        Player player = controller.getGameState().getPlayer();
        Field field = GameController.class.getDeclaredField("gameState");
        field.setAccessible(true);
        field.set(controller, new GameState(player, new Enemy("Cible", Integer.MAX_VALUE)));

        double damage = 0;
        for (int turn = 0; turn < TURNS && !player.isDefeated(); turn++) {
            Enemy target = controller.getGameState().getEnemy();
            if (target.getHp() < Integer.MAX_VALUE / 2) { // une cible neuve : elle ne tombe jamais
                field.set(controller, new GameState(player, new Enemy("Cible", Integer.MAX_VALUE)));
            }
            controller.drawCards();
            for (Card card : choose(player.getCurrentHand(), controller.getPlayLimit())) {
                if (controller.isPlayLimitReached()) break;
                if (controller.unplayableReason(card) == null) controller.playCard(card);
            }
            TurnResult result = controller.spin();
            Set<Event> seen = Collections.newSetFromMap(new IdentityHashMap<>());
            seen.addAll(result.getEvents());
            seen.addAll(result.getPistolEvents());
            for (Event event : seen) if (event instanceof EnemyDamagedEvent hit) damage += hit.damage;
        }
        return damage;
    }

    /** La plus longue suite de rangs en main (l'As avant le 2 ou après le Roi), puis les plus hauts rangs. */
    static List<Card> choose(List<Card> hand, int limit) {
        List<Card> best = List.of();
        for (int start = 1; start <= 13; start++) {
            List<Card> run = new ArrayList<>();
            for (int r = start; r <= 14 && run.size() < limit; r++) {
                int rank = r == 14 ? 1 : r;
                Card card = hand.stream().filter(c -> c.getRank() == rank && !run.contains(c)).findFirst().orElse(null);
                if (card == null) break;
                run.add(card);
            }
            if (run.size() > best.size()) best = run;
        }
        List<Card> chosen = new ArrayList<>(best.size() >= 3 ? best : List.of());
        hand.stream().filter(c -> !chosen.contains(c))
            .sorted(Comparator.comparingInt((Card c) -> c.getRank() == 1 ? 14 : c.getRank()).reversed())
            .forEach(c -> { if (chosen.size() < limit) chosen.add(c); });
        return chosen;
    }

    private SuitStraightSimulator() { }
}
