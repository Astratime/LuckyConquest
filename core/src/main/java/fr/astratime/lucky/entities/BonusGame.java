package fr.astratime.lucky.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Le Jeu bonus : après un Bingo, une fois sur dix, la machine s'ouvre sur une
 * grille de {@link #SIZE} x {@link #SIZE} cases, remplie avec les symboles des
 * rouleaux du joueur (mêmes chances qu'à la machine), plus le Joker (environ
 * une case sur {@link #JOKER_ONE_IN}), qui compte comme n'importe quel symbole.
 *
 * Elle tourne {@link #SPINS} fois : après chaque tirage, les symboles alignés se
 * figent (en doré à l'écran) et seules les autres cases relancent. Les gains sont
 * comptés à la fin, sur la dernière grille.
 *
 * Un alignement : au moins {@link #MIN_LINE} symboles identiques qui se suivent,
 * Joker compris, en ligne, en colonne ou en diagonale (toutes les diagonales d'au
 * moins {@link #MIN_LINE} cases, dans les deux sens). Un symbole peut compter dans
 * plusieurs alignements. Chaque alignement paie en gains : la base de sa longueur
 * ({@link #baseSpins}) x le coût du tirage x le facteur de sa rareté
 * ({@link #rarityFactor}) ; une ligne faite uniquement de Jokers compte comme rare.
 *
 * Tout est tiré d'avance (voir {@link #play}) : l'écran ne fait que le montrer.
 */
public final class BonusGame {

    /** Côté de la grille, en cases. */
    public static final int   SIZE           = 6;
    /** Tirages de la grille. */
    public static final int   SPINS          = 3;
    /** Symboles qui se suivent, au moins, pour faire un alignement. */
    public static final int   MIN_LINE       = 3;
    /** Une case sur autant, en moyenne, est un Joker. */
    public static final int   JOKER_ONE_IN   = 20;
    /** Chances qu'un Bingo ouvre le Jeu bonus. */
    public static final float TRIGGER_CHANCE = 0.10f;

    /**
     * Un alignement de la grille finale.
     *
     * @param symbol symbole aligné ({@link Symbol#JOKER} pour une ligne de Jokers seuls)
     * @param cells  ses cases, dans l'ordre, chacune {@code {ligne, colonne}}
     * @param gains  ce qu'il rapporte
     */
    public record Line(Symbol symbol, List<int[]> cells, int gains) {
        /** @return le nombre de cases alignées. */
        public int length() { return cells.size(); }
    }

    /** Grille après chaque tirage : {@code grids.get(tirage)[ligne][colonne]}. */
    private final List<Symbol[][]> grids;
    /** Cases figées après chaque tirage (alignées) : celles-là ne relancent plus. */
    private final List<boolean[][]> frozen;
    /** Alignements de la grille finale, qui paient. */
    private final List<Line> lines;
    private final int total;

    private BonusGame(List<Symbol[][]> grids, List<boolean[][]> frozen, List<Line> lines) {
        this.grids  = grids;
        this.frozen = frozen;
        this.lines  = List.copyOf(lines);
        int sum = 0;
        for (Line line : lines) sum += line.gains();
        this.total = sum;
    }

    /**
     * Joue un Jeu bonus entier.
     *
     * @param reels    symboles de la machine du joueur (sans le Joker)
     * @param spinCost coût d'un tirage, là où se joue le combat
     */
    public static BonusGame play(List<Symbol> reels, int spinCost, Random random) {
        List<Symbol[][]>  grids  = new ArrayList<>();
        List<boolean[][]> frozen = new ArrayList<>();
        Symbol[][]  grid = new Symbol[SIZE][SIZE];
        boolean[][] kept = new boolean[SIZE][SIZE];
        for (int spin = 0; spin < SPINS; spin++) {
            Symbol[][] next = new Symbol[SIZE][SIZE];
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    next[row][col] = kept[row][col] ? grid[row][col] : draw(reels, random);
                }
            }
            grid = next;
            kept = new boolean[SIZE][SIZE];
            for (Line line : lines(grid, spinCost)) {
                for (int[] cell : line.cells()) kept[cell[0]][cell[1]] = true;
            }
            grids.add(grid);
            frozen.add(kept);
        }
        return new BonusGame(grids, frozen, lines(grid, spinCost));
    }

    /** @return une case tirée au hasard : un Joker, une fois sur {@link #JOKER_ONE_IN}, sinon un des rouleaux. */
    private static Symbol draw(List<Symbol> reels, Random random) {
        if (random.nextInt(JOKER_ONE_IN) == 0) return Symbol.JOKER;
        return reels.get(random.nextInt(reels.size()));
    }

    /** @return la grille après le tirage {@code spin} (0 à {@link #SPINS} - 1), en copie. */
    public Symbol[][] getGrid(int spin) { return copy(grids.get(spin)); }

    /** @return {@code true} si la case ({@code row}, {@code col}) est alignée après le tirage {@code spin}. */
    public boolean isFrozen(int spin, int row, int col) { return frozen.get(spin)[row][col]; }

    /** @return les alignements de la grille finale, qui paient. */
    public List<Line> getLines() { return lines; }

    /** @return les gains du Jeu bonus. */
    public int getTotal() { return total; }

    // -------------------------------------------------------------------------
    // Alignements et gains
    // -------------------------------------------------------------------------

    /**
     * @return les alignements de {@code grid}, sur ses lignes, ses colonnes et
     *         ses diagonales d'au moins {@link #MIN_LINE} cases ; ce que chacun paie quand un tirage coûte {@code spinCost}
     */
    public static List<Line> lines(Symbol[][] grid, int spinCost) {
        List<Line> found = new ArrayList<>();
        for (List<int[]> path : paths()) {
            Symbol[] row = new Symbol[path.size()];
            for (int i = 0; i < row.length; i++) row[i] = grid[path.get(i)[0]][path.get(i)[1]];
            for (int[] run : runs(row)) {
                Symbol symbol = symbolOf(row, run[0], run[1]);
                found.add(new Line(symbol, List.copyOf(path.subList(run[0], run[1])),
                    gains(symbol, run[1] - run[0], spinCost)));
            }
        }
        return found;
    }

    /** @return les gains d'un alignement de {@code length} {@code symbol}. */
    public static int gains(Symbol symbol, int length, int spinCost) {
        return Math.round(baseSpins(length) * spinCost * rarityFactor(symbol));
    }

    /**
     * @return les gains de base d'un alignement de {@code length} cases, en coûts de tirage
     *         (réglés pour qu'un Jeu bonus rapporte en moyenne 100 coûts de tirage sur la machine classique)
     */
    public static int baseSpins(int length) {
        return switch (length) {
            case 3  -> 5;
            case 4  -> 15;
            case 5  -> 60;
            default -> length >= 6 ? 300 : 0;
        };
    }

    /** @return le facteur de rareté de {@code symbol} (le Joker seul compte comme rare). */
    public static float rarityFactor(Symbol symbol) {
        if (symbol == Symbol.JOKER) return 2.5f;
        return switch (symbol.getRarity()) {
            case COMMON -> 1f;
            case MEDIUM -> 1.5f;
            case RARE   -> 2.5f;
        };
    }

    /**
     * Les alignements d'une suite de cases {@code cells} : pour chaque symbole
     * présent, chaque plus longue suite de ce symbole et de Jokers qui le contient,
     * d'au moins {@link #MIN_LINE} cases. Une suite de Jokers seuls ne compte que
     * si aucun symbole ne la prolonge : sur toute la ligne.
     *
     * @return {@code {début, fin exclue}} de chaque alignement
     */
    static List<int[]> runs(Symbol[] cells) {
        List<int[]> runs = new ArrayList<>();
        List<Symbol> seen = new ArrayList<>();
        for (Symbol symbol : cells) {
            if (symbol == Symbol.JOKER || seen.contains(symbol)) continue;
            seen.add(symbol);
            int start = 0;
            while (start < cells.length) {
                if (cells[start] != symbol && cells[start] != Symbol.JOKER) { start++; continue; }
                int end = start;
                boolean has = false;
                while (end < cells.length && (cells[end] == symbol || cells[end] == Symbol.JOKER)) {
                    has |= cells[end] == symbol;
                    end++;
                }
                if (has && end - start >= MIN_LINE) runs.add(new int[] {start, end});
                start = end;
            }
        }
        // Les Jokers seuls : tout symbole voisin les prolongerait, ils ne comptent seuls que sur toute la ligne.
        if (seen.isEmpty() && cells.length >= MIN_LINE) runs.add(new int[] {0, cells.length});
        return runs;
    }

    /** @return le symbole (hors Joker) de la suite {@code cells[from..to)}, ou le Joker si elle n'a que des Jokers. */
    private static Symbol symbolOf(Symbol[] cells, int from, int to) {
        for (int i = from; i < to; i++) {
            if (cells[i] != Symbol.JOKER) return cells[i];
        }
        return Symbol.JOKER;
    }

    /** Les lignes, colonnes et diagonales de la grille (au moins {@link #MIN_LINE} cases), chacune ses cases dans l'ordre. */
    private static final List<List<int[]>> PATHS = buildPaths();

    /** @return les {@code 2 x SIZE} lignes et colonnes, puis les diagonales d'au moins {@link #MIN_LINE} cases. */
    static List<List<int[]>> paths() { return PATHS; }

    private static List<List<int[]>> buildPaths() {
        List<List<int[]>> paths = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            List<int[]> row = new ArrayList<>(), col = new ArrayList<>();
            for (int j = 0; j < SIZE; j++) {
                row.add(new int[] {i, j});
                col.add(new int[] {j, i});
            }
            paths.add(row);
            paths.add(col);
        }
        // Diagonales : ↘ (ligne - colonne constante), puis ↗ (ligne + colonne constante).
        for (int d = -(SIZE - MIN_LINE); d <= SIZE - MIN_LINE; d++) {
            List<int[]> down = new ArrayList<>();
            for (int row = 0; row < SIZE; row++) {
                int col = row - d;
                if (col >= 0 && col < SIZE) down.add(new int[] {row, col});
            }
            paths.add(down);
        }
        for (int s = MIN_LINE - 1; s <= 2 * (SIZE - 1) - (MIN_LINE - 1); s++) {
            List<int[]> up = new ArrayList<>();
            for (int row = SIZE - 1; row >= 0; row--) {
                int col = s - row;
                if (col >= 0 && col < SIZE) up.add(new int[] {row, col});
            }
            paths.add(up);
        }
        return List.copyOf(paths);
    }

    private static Symbol[][] copy(Symbol[][] grid) {
        Symbol[][] copy = new Symbol[grid.length][];
        for (int i = 0; i < grid.length; i++) copy[i] = grid[i].clone();
        return copy;
    }
}
