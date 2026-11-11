package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.BonusGame;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.RankBonus;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.effects.BingoEffect;
import fr.astratime.lucky.entities.effects.MultiplierEffect;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;

/** Le Jeu bonus dans un tour : il suit un Bingo, ses gains sont crédités, et le tutoriel n'en a pas. */
class BonusGameTurnTest {

    private static List<Card> plainCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 20; i++) cards.add(new Card("c" + i, "c" + i, "c.png", List.of(), null, 1));
        return cards;
    }

    private static GameState fight() {
        GameController controller = new GameController(BonusGameTurnTest::plainCards);
        controller.restart(EnemyKind.ENTRAINEMENT);
        return controller.getGameState();
    }

    @Test
    void theTestCardOpensTheBonusGameAndItsGainsAreCredited() {
        GameState state = fight();
        TurnEngine engine = new TurnEngine();
        double before = state.getPlayer().getGains();

        TurnResult result = engine.playTurn(state, List.of(new BingoEffect(1, Symbol.SWORD, true)));

        BonusGame bonus = result.getBonusGame();
        assertNotNull(bonus);
        assertTrue(result.isJackpot());
        double fromTurn = result.getEvents().stream()
            .filter(e -> e instanceof fr.astratime.lucky.entities.events.GainsEarnedEvent)
            .mapToDouble(e -> ((fr.astratime.lucky.entities.events.GainsEarnedEvent) e).amount).sum();
        assertEquals(before + fromTurn + bonus.getTotal(), state.getPlayer().getGains(),
            "les gains du Jeu bonus s'ajoutent à ceux du tirage");
    }

    @Test
    void noBingoNoBonusGame() {
        GameState state = fight();
        TurnEngine engine = new TurnEngine();
        for (int i = 0; i < 200; i++) {
            TurnResult result = engine.playTurn(state, List.of());
            if (!result.isJackpot()) assertNull(result.getBonusGame());
            if (state.getEnemy().isDefeated() || state.getPlayer().isDefeated()) state = fight();
        }
    }

    @Test
    void aBingoOpensItAboutOneTimeInTen() {
        TurnEngine engine = new TurnEngine();
        int opened = 0, bingos = 400;
        for (int i = 0; i < bingos; i++) {
            if (engine.playTurn(fight(), List.of(new BingoEffect(1, Symbol.SWORD))).getBonusGame() != null) opened++;
        }
        assertEquals(bingos * BonusGame.TRIGGER_CHANCE, opened, 25);
    }

    @Test
    void onlyTheBonusPartOfTheGridIsMultiplied() {
        RankBonus rank = new RankBonus(0, 0, 0, 70);
        GameController controller = new GameController(BonusGameTurnTest::plainCards, id -> null, Map.of(),
            cards -> new Player("Joueur", Player.BASE_HP, cards, rank, Symbol.classicReels()));
        controller.restart(EnemyKind.ENTRAINEMENT);
        TurnEngine engine = new TurnEngine();
        engine.setBonusGameKind(BonusGame.Kind.GRID);
        TurnResult result = engine.playTurn(controller.getGameState(),
            List.of(new MultiplierEffect(9), new BingoEffect(1, Symbol.SWORD, true)));

        BonusGame bonus = result.getBonusGame();
        long expected = 0;
        for (BonusGame.Line line : bonus.getLines()) {
            // la base reste fixe ; 5 par symbole aligné et le rang par alignement, multipliés par les Trèfle (1 + 9 = x10)
            expected += BonusGame.gains(line.symbol(), line.length(), 100)
                + (BonusGame.GAINS_PER_SYMBOL * line.length() + 70L) * 10;
        }
        assertEquals(expected, bonus.getTotal());
    }

    @Test
    void theTutorialHasNoBonusGame() {
        TurnEngine engine = new TurnEngine();
        engine.setBonusGameEnabled(false);
        TurnResult result = engine.playTurn(fight(), List.of(new BingoEffect(1, Symbol.SWORD, true)));
        assertTrue(result.isJackpot());
        assertNull(result.getBonusGame());
    }

    @Test
    void theGridComesOutOneTimeInTenAndTheSmallGamesThreeTimesInTen() {
        java.util.Random random = new java.util.Random(3);
        java.util.Map<BonusGame.Kind, Integer> seen = new java.util.EnumMap<>(BonusGame.Kind.class);
        int games = 20_000;
        for (int i = 0; i < games; i++) seen.merge(BonusGame.Kind.pick(random), 1, Integer::sum);
        assertEquals(games * 0.10, seen.get(BonusGame.Kind.GRID), games * 0.01);
        for (BonusGame.Kind kind : List.of(BonusGame.Kind.WHEEL, BonusGame.Kind.CHESTS, BonusGame.Kind.SCRATCH)) {
            assertEquals(games * 0.30, seen.get(kind), games * 0.015, kind.name());
        }
    }

    @Test
    void theGridAlignsAboutElevenTimes() {
        java.util.Random random = new java.util.Random(9);
        double lines = 0;
        int games = 3_000;
        for (int i = 0; i < games; i++) lines += BonusGame.play(Symbol.classicReels(), 100, random).getLines().size();
        assertEquals(BonusGame.GRID_AVERAGE_LINES, lines / games, 0.6);
    }

    @Test
    void eachRankPaysItsShareOfTheGrid() {
        assertEquals(4, BonusGame.Kind.GRID.rank);
        java.util.Random random = new java.util.Random(5);
        double pot = 1_000_000;
        java.util.Map<BonusGame.Kind, Double> share = java.util.Map.of(
            BonusGame.Kind.SCRATCH, 0.8, BonusGame.Kind.CHESTS, 0.5, BonusGame.Kind.WHEEL, 0.2);
        for (BonusGame.Kind kind : share.keySet()) {
            double sum = 0, potSum = 0;
            int games = 8_000;
            for (int i = 0; i < games; i++) {
                BonusGame game = BonusGame.playSmall(kind, pot, 100, 0, random);
                assertEquals(kind, game.getKind());
                assertEquals(pot + BonusGame.BASE_UNIT_SPINS * 100, game.getUnit(), 1e-6);
                assertTrue(game.getPotPaid() <= game.getTotal());
                sum += game.getTotal();
                potSum += game.getPotPaid();
            }
            double units = sum / games / (pot + 2_000);
            assertEquals(share.get(kind), units / BonusGame.GRID_AVERAGE_LINES, 0.05,
                kind + " (rang " + kind.rank + ") : " + units + " mises de base");
            assertEquals(potSum / games, sum / games * pot / (pot + 2_000), sum / games * 0.01, "la part de la cagnotte");
        }
    }

    @Test
    void theScratchTicketShowsTheWinningValueThreeTimes() {
        java.util.Random random = new java.util.Random(11);
        for (int i = 0; i < 200; i++) {
            BonusGame game = BonusGame.playSmall(BonusGame.Kind.SCRATCH, 0, 100, 0, random);
            assertEquals(BonusGame.SCRATCH_CELLS, game.getValues().size());
            assertEquals(3, game.getPicked().size());
            double win = game.getValues().get(game.getPicked().get(0));
            assertEquals(3, game.getValues().stream().filter(v -> v == win).count(), "la gagnante trois fois");
            for (double value : game.getValues()) {
                assertTrue(game.getValues().stream().filter(v -> v == value).count() <= 3);
            }
            assertEquals(win * game.getUnit(), game.getTotal(), 1e-6);
            assertEquals(0, game.getPotPaid(), 1e-6, "sans cagnotte");
        }
    }

    @Test
    void theSmallGamesEmptyThePotAndItsPartIsNotStakeable() {
        GameController controller = new GameController(BonusGameTurnTest::plainCards);
        controller.restart(EnemyKind.CROUPIER);
        controller.setBonusGameChance(1f);
        controller.setBonusGameKind(BonusGame.Kind.WHEEL);
        controller.getPot().add(50_000);
        controller.drawCards();
        controller.rigSpin(Symbol.SEVEN, Symbol.SEVEN, Symbol.SEVEN);
        BonusGame bonus = controller.spin().getBonusGame();
        assertNotNull(bonus);
        assertEquals(BonusGame.Kind.WHEEL, bonus.getKind());
        assertEquals(0, controller.getPot().getAmount(), "la cagnotte repart de 0");
        double segment = BonusGame.WHEEL_SEGMENTS.get(bonus.getPicked().get(0));
        assertEquals(50_000 * segment, bonus.getPotPaid(), 1e-6);
        Player player = controller.getGameState().getPlayer();
        assertEquals(Math.max(0, player.getGains() - bonus.getPotPaid()), player.getStakeableGains(), 1e-6);
    }
}
