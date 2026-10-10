package fr.astratime.lucky.entities;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static fr.astratime.lucky.entities.Symbol.*;
import static org.junit.jupiter.api.Assertions.*;

class BonusGameTest {

    /** Une grille sans aucun alignement : six symboles qui se décalent d'une ligne à l'autre. */
    private static Symbol[][] emptyGrid() {
        Symbol[] cycle = {CHERRY, BAR, GRAPE, BELL, SEVEN, DIAMOND};
        Symbol[][] grid = new Symbol[6][6];
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) grid[row][col] = cycle[(col + 2 * row) % 6];
        }
        return grid;
    }

    @Test
    void theGridHasTwentySixPaths() {
        // 6 lignes, 6 colonnes, et 7 diagonales d'au moins 3 cases dans chaque sens.
        assertEquals(26, BonusGame.paths().size());
        assertEquals(14, BonusGame.paths().stream().filter(path -> path.size() < 6
            || path.get(0)[0] != path.get(1)[0] && path.get(0)[1] != path.get(1)[1]).count());
    }

    @Test
    void aGridWithoutThreeInARowPaysNothing() {
        assertTrue(BonusGame.lines(emptyGrid(), 100).isEmpty());
    }

    @Test
    void fourSevensInARowPayTwoThousandTwoHundredFiftyAtThePrairie() {
        Symbol[][] grid = emptyGrid();
        grid[0] = new Symbol[] {SEVEN, SEVEN, SEVEN, SEVEN, BAR, CHERRY};
        List<BonusGame.Line> lines = BonusGame.lines(grid, 100);

        BonusGame.Line line = lines.stream().filter(l -> l.symbol() == SEVEN && l.length() == 4).findFirst().orElseThrow();
        assertEquals(2_250, line.gains()); // 15 C x 100 x 1,5
    }

    @Test
    void sixCrownsPaySeventyFiveThousand() {
        assertEquals(75_000, BonusGame.gains(CROWN, 6, 100)); // 300 C x 100 x 2,5
        assertEquals(500, BonusGame.gains(CHERRY, 3, 100));
        assertEquals(6_000, BonusGame.gains(BELL, 5, 100));
    }

    @Test
    void aBonusGamePaysAboutOneHundredSpinCostsOnAverage() {
        java.util.Random random = new java.util.Random(1);
        long total = 0;
        int games = 20_000;
        for (int i = 0; i < games; i++) total += BonusGame.play(Symbol.classicReels(), 100, random).getTotal();
        assertEquals(100.0, total / (double) games / 100, 5.0); // en coûts de tirage, machine classique
    }

    @Test
    void theTurnMultipliersOnlyBoostTheBonusPartOfEachLine() {
        BonusGame game = BonusGame.play(Symbol.classicReels(), 100, new Random(4));
        BonusGame boosted = game.boosted(5, 70, 2f);
        long expected = 0;
        for (int i = 0; i < game.getLines().size(); i++) {
            BonusGame.Line line = game.getLines().get(i);
            int gains = line.gains() + (5 * line.length() + 70) * 2;
            assertEquals(gains, boosted.getLines().get(i).gains());
            expected += gains;
        }
        assertEquals(expected, boosted.getTotal());
        assertEquals(game.getTotal(), game.boosted(0, 0, 1000f).getTotal());
    }

    @Test
    void theJokerCountsAsAnySymbol() {
        List<int[]> runs = BonusGame.runs(new Symbol[] {CHERRY, JOKER, CHERRY, BAR, BELL, GRAPE});
        assertEquals(1, runs.size());
        assertArrayEquals(new int[] {0, 3}, runs.get(0));
    }

    @Test
    void aJokerBetweenTwoSymbolsCompletesBothLines() {
        List<int[]> runs = BonusGame.runs(new Symbol[] {CHERRY, CHERRY, JOKER, BAR, BAR, GRAPE});
        assertEquals(2, runs.size());
    }

    @Test
    void aLineOfJokersAloneCountsAsRare() {
        Symbol[][] grid = emptyGrid();
        grid[2] = new Symbol[] {JOKER, JOKER, JOKER, JOKER, JOKER, JOKER};
        BonusGame.Line line = BonusGame.lines(grid, 100).stream()
            .filter(l -> l.symbol() == JOKER).findFirst().orElseThrow();
        assertEquals(6, line.length());
        assertEquals(75_000, line.gains());
    }

    @Test
    void jokersNextToASymbolTakeItsValue() {
        List<int[]> runs = BonusGame.runs(new Symbol[] {JOKER, JOKER, JOKER, CHERRY, BAR, GRAPE});
        assertEquals(1, runs.size());
        assertArrayEquals(new int[] {0, 4}, runs.get(0));
    }

    @Test
    void crossingLinesPayEachTime() {
        Symbol[][] grid = emptyGrid();
        for (int i = 0; i < 3; i++) {
            grid[0][i] = TRIPLE_SEVEN; // en ligne
            grid[i][0] = TRIPLE_SEVEN; // en colonne, la case du coin compte deux fois
        }
        long sevens = BonusGame.lines(grid, 100).stream().filter(l -> l.symbol() == TRIPLE_SEVEN).count();
        assertEquals(2, sevens);
    }

    @Test
    void diagonalsCountInBothDirections() {
        Symbol[][] grid = emptyGrid();
        grid[3][0] = GOLD_BAR;
        grid[4][1] = GOLD_BAR;
        grid[5][2] = GOLD_BAR;
        grid[0][5] = HEART;
        grid[1][4] = HEART;
        grid[2][3] = HEART;
        List<BonusGame.Line> lines = BonusGame.lines(grid, 100);
        assertTrue(lines.stream().anyMatch(l -> l.symbol() == GOLD_BAR && l.length() == 3));
        assertTrue(lines.stream().anyMatch(l -> l.symbol() == HEART && l.length() == 3));
    }

    @Test
    void alignedCellsStayFrozenForTheNextSpins() {
        for (long seed = 0; seed < 50; seed++) {
            BonusGame game = BonusGame.play(Symbol.classicReels(), 100, new Random(seed));
            for (int spin = 0; spin + 1 < BonusGame.SPINS; spin++) {
                Symbol[][] before = game.getGrid(spin), after = game.getGrid(spin + 1);
                for (int row = 0; row < 6; row++) {
                    for (int col = 0; col < 6; col++) {
                        if (game.isFrozen(spin, row, col)) assertEquals(before[row][col], after[row][col]);
                    }
                }
            }
        }
    }

    @Test
    void theTotalIsTheSumOfTheFinalLines() {
        BonusGame game = BonusGame.play(Symbol.classicReels(), 500, new Random(3));
        assertEquals(game.getLines().stream().mapToInt(BonusGame.Line::gains).sum(), game.getTotal());
        assertEquals(BonusGame.lines(game.getGrid(BonusGame.SPINS - 1), 500).size(), game.getLines().size());
    }

    @Test
    void theGridOnlyHoldsThePlayersReelsAndJokers() {
        List<Symbol> reels = List.of(CHERRY, SEVEN, CROWN);
        BonusGame game = BonusGame.play(reels, 100, new Random(11));
        int jokers = 0;
        for (int spin = 0; spin < BonusGame.SPINS; spin++) {
            for (Symbol[] row : game.getGrid(spin)) {
                for (Symbol symbol : row) {
                    assertTrue(symbol == JOKER || reels.contains(symbol), symbol + " n'est pas dans la machine");
                }
            }
        }
        for (long seed = 0; seed < 200; seed++) {
            for (Symbol[] row : BonusGame.play(Symbol.classicReels(), 100, new Random(seed)).getGrid(0)) {
                for (Symbol symbol : row) if (symbol == JOKER) jokers++;
            }
        }
        // environ une case sur 20 : 200 grilles de 36 cases
        assertEquals(200 * 36 / 20, jokers, 80);
    }
}
