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
