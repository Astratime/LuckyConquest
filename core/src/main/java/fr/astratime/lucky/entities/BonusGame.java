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
 *
 * Trois autres Jeux bonus, plus modestes, sortent plus souvent (idée d'Astra, 2026-11-11) :
 * la Roue, les Coffres et le Ticket à gratter (voir {@link Kind}). Ils paient
 * des multiples d'une mise de base : la cagnotte + {@link #BASE_UNIT_SPINS} coûts de tirage.
 * Chaque Jeu bonus a un rang (Astra) : la grille est de rang 4 ; le rang 3 rapporte en moyenne
 * 80 % de la grille, le rang 2 50 %, le rang 1 20 %. La grille aligne en moyenne
 * {@link #GRID_AVERAGE_LINES} fois (machine classique) et chaque alignement paie la cagnotte et
 * environ {@link #BASE_UNIT_SPINS} tirages : elle vaut donc environ 10,8 mises de base.
 */
public final class BonusGame {

    /** Les quatre Jeux bonus, leur rang et leurs chances de sortir quand un Jeu bonus se déclenche. */
    public enum Kind {
        /** La grille de 6 x 6 : le gros lot, chaque alignement paie toute la cagnotte. */
        GRID(10, 4),
        /** La Roue de la cagnotte : un lancer, la case où elle s'arrête multiplie la mise de base. */
        WHEEL(30, 1),
        /** Les Coffres : trois coffres ouverts sur neuf, chacun un multiple de la mise de base. */
        CHESTS(30, 2),
        /** Le Ticket à gratter : la valeur qu'on trouve trois fois multiplie la mise de base. */
        SCRATCH(30, 3);

        /** Poids de tirage (sur 100). */
        public final int weight;
        /** Rang du Jeu bonus, de 1 à 4 : plus il est haut, plus il rapporte. */
        public final int rank;

        Kind(int weight, int rank) {
            this.weight = weight;
            this.rank = rank;
        }

        /** @return un Jeu bonus tiré au hasard, selon les poids. */
        public static Kind pick(Random random) {
            int roll = random.nextInt(100);
            for (Kind kind : values()) {
                if (roll < kind.weight) return kind;
                roll -= kind.weight;
            }
            return GRID;
        }
    }

    /** Mise de base des petits Jeux bonus : la cagnotte + autant de coûts de tirage. */
    public static final int BASE_UNIT_SPINS = 20;
    /** Alignements moyens d'une grille sur la machine classique (mesuré, voir les tests). */
    public static final double GRID_AVERAGE_LINES = 10.8;
    /** Les cases de la Roue, dans l'ordre (multiples de la mise de base) : 2,2 en moyenne, 20 % de la grille. */
    public static final List<Double> WHEEL_SEGMENTS = List.of(1.0, 2.0, 1.5, 3.0, 1.0, 4.0, 1.5, 2.0, 1.0, 5.0);
    /** Ce que contiennent les neuf Coffres ; trois ouverts : 5,5 mises de base en moyenne, 50 % de la grille. */
    public static final List<Double> CHEST_VALUES = List.of(0.5, 1.0, 1.0, 1.5, 2.0, 2.0, 2.5, 3.0, 3.0);
    /** Coffres ouverts. */
    public static final int CHESTS_OPENED = 3;
    /** Valeurs du Ticket à gratter et leurs poids : la gagnante vaut 8,66 mises de base en moyenne, 80 % de la grille. */
    public static final List<Double> SCRATCH_VALUES  = List.of(4.0, 6.0, 8.0, 12.0, 20.0);
    public static final List<Integer> SCRATCH_WEIGHTS = List.of(25, 25, 20, 18, 12);
    /** Cases du Ticket à gratter (3 x 3). */
    public static final int SCRATCH_CELLS = 9;

    /** Côté de la grille, en cases. */
    public static final int   SIZE           = 6;
    /** Tirages de la grille. */
    public static final int   SPINS          = 5;
    /** Symboles qui se suivent, au moins, pour faire un alignement. */
    public static final int   MIN_LINE       = 3;
    /** Une case sur autant, en moyenne, est un Joker. */
    public static final int   JOKER_ONE_IN   = 20;
    /** Chances qu'un Bingo ouvre le Jeu bonus. */
    public static final float TRIGGER_CHANCE = 0.20f;
    /** Chances qu'un Bingo ouvre le Jeu bonus dans la Tour des épreuves (choix d'Astra, 2026-10-27). */
    public static final float TOWER_TRIGGER_CHANCE = 0.30f;
    /** Bonus par symbole aligné, à la Prairie, que multiplient les cartes du tour (voir {@link #boosted}). */
    public static final int   GAINS_PER_SYMBOL = 5;

    /**
     * Un alignement de la grille finale.
     *
     * @param symbol symbole aligné ({@link Symbol#JOKER} pour une ligne de Jokers seuls)
     * @param cells  ses cases, dans l'ordre, chacune {@code {ligne, colonne}}
     * @param gains  ce qu'il rapporte
     */
    public record Line(Symbol symbol, List<int[]> cells, double gains) {
        /** @return le nombre de cases alignées. */
        public int length() { return cells.size(); }
    }

    /** Grille après chaque tirage : {@code grids.get(tirage)[ligne][colonne]}. */
    private final List<Symbol[][]> grids;
    /** Cases figées après chaque tirage (alignées) : celles-là ne relancent plus. */
    private final List<boolean[][]> frozen;
    /** Alignements de la grille finale, qui paient. */
    private final List<Line> lines;
    private final double total;
    /** Cagnotte ajoutée à chaque alignement (voir {@link #withPot}), 0 sans cagnotte. */
    private final double pot;
    private final Kind kind;
    /** Petits Jeux bonus : les valeurs montrées (cases de la Roue, Coffres, cases du Ticket), en mises de base. */
    private final List<Double> values;
    /** Petits Jeux bonus : la case où s'arrête la Roue, les Coffres ouverts, les trois cases gagnantes du Ticket. */
    private final List<Integer> picked;
    /** Petits Jeux bonus : la mise de base, et la part du total qui vient de la cagnotte. */
    private final double unit, potPaid;

    private BonusGame(List<Symbol[][]> grids, List<boolean[][]> frozen, List<Line> lines) {
        this(grids, frozen, lines, 0);
    }

    private BonusGame(List<Symbol[][]> grids, List<boolean[][]> frozen, List<Line> lines, double pot) {
        this.grids  = grids;
        this.frozen = frozen;
        this.pot    = pot;
        this.lines  = List.copyOf(lines);
        double sum = 0;
        for (Line line : lines) sum = BigNumber.add(sum, line.gains());
        this.total = sum;
        this.kind = Kind.GRID;
        this.values = List.of();
        this.picked = List.of();
        this.unit = 0;
        this.potPaid = BigNumber.cap(pot * lines.size());
    }

    private BonusGame(Kind kind, List<Double> values, List<Integer> picked, double unit, double total, double pot,
                      double potPaid) {
        this.grids = List.of();
        this.frozen = List.of();
        this.lines = List.of();
        this.kind = kind;
        this.values = List.copyOf(values);
        this.picked = List.copyOf(picked);
        this.unit = unit;
        this.total = total;
        this.pot = pot;
        this.potPaid = potPaid;
    }

    /**
     * Joue un petit Jeu bonus ({@link Kind#WHEEL}, {@link Kind#CHESTS} ou {@link Kind#SCRATCH}).
     *
     * @param pot       la cagnotte, qui repart de 0 après le jeu
     * @param spinCost  coût d'un tirage, là où se joue le combat
     * @param extra     petit bonus des cartes et du rang, ajouté à chaque gain (déjà multiplié par le tour)
     */
    public static BonusGame playSmall(Kind kind, double pot, int spinCost, double extra, Random random) {
        double unit = BigNumber.add(Math.max(0, pot), (double) BASE_UNIT_SPINS * spinCost);
        List<Double> values = new ArrayList<>();
        List<Integer> picked = new ArrayList<>();
        double paid = 0; // en mises de base
        int prizes = 1;
        switch (kind) {
            case WHEEL -> {
                values.addAll(WHEEL_SEGMENTS);
                int stop = random.nextInt(values.size());
                picked.add(stop);
                paid = values.get(stop);
            }
            case CHESTS -> {
                values.addAll(CHEST_VALUES);
                java.util.Collections.shuffle(values, random);
                List<Integer> order = new ArrayList<>();
                for (int i = 0; i < values.size(); i++) order.add(i);
                java.util.Collections.shuffle(order, random);
                for (int i = 0; i < CHESTS_OPENED; i++) {
                    picked.add(order.get(i));
                    paid += values.get(order.get(i));
                }
                prizes = CHESTS_OPENED;
            }
            case SCRATCH -> {
                double win = weighted(random);
                // Trois cases portent la valeur gagnante ; les six autres, d'autres valeurs, au plus deux fois chacune.
                List<Double> others = new ArrayList<>();
                for (double value : SCRATCH_VALUES) {
                    if (value != win) { others.add(value); others.add(value); }
                }
                java.util.Collections.shuffle(others, random);
                List<Double> cells = new ArrayList<>(List.of(win, win, win));
                cells.addAll(others.subList(0, SCRATCH_CELLS - 3));
                java.util.Collections.shuffle(cells, random);
                values.addAll(cells);
                for (int i = 0; i < cells.size(); i++) if (cells.get(i) == win) picked.add(i);
                paid = win;
            }
            default -> throw new IllegalArgumentException("La grille se joue avec play : " + kind);
        }
        double total = BigNumber.add(BigNumber.round(unit * paid), BigNumber.round(Math.max(0, extra) * prizes));
        double potPaid = BigNumber.round(Math.max(0, pot) * paid);
        return new BonusGame(kind, values, picked, unit, total, Math.max(0, pot), potPaid);
    }

    private static double weighted(Random random) {
        int roll = random.nextInt(100);
        for (int i = 0; i < SCRATCH_VALUES.size(); i++) {
            if (roll < SCRATCH_WEIGHTS.get(i)) return SCRATCH_VALUES.get(i);
            roll -= SCRATCH_WEIGHTS.get(i);
        }
        return SCRATCH_VALUES.get(0);
    }

    /** @return le Jeu bonus joué. */
    public Kind getKind() { return kind; }

    /** @return petits Jeux bonus : les valeurs montrées (cases de la Roue, Coffres, cases du Ticket), en mises de base. */
    public List<Double> getValues() { return values; }

    /** @return petits Jeux bonus : la case où s'arrête la Roue, les Coffres ouverts, les cases gagnantes du Ticket. */
    public List<Integer> getPicked() { return picked; }

    /** @return petits Jeux bonus : la mise de base (cagnotte + {@link #BASE_UNIT_SPINS} coûts de tirage). */
    public double getUnit() { return unit; }

    /** @return la part des gains qui vient de la cagnotte (elle ne se remise pas). */
    public double getPotPaid() { return potPaid; }

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

    /**
     * Le petit bonus que les cartes et le rang ajoutent au Jeu bonus. Les gains de
     * base de chaque alignement restent fixes (jamais multipliés) ; s'y ajoutent
     * {@code perSymbol} par symbole aligné et le bonus de gains du rang une fois par
     * alignement, et seule cette part reçoit les multiplicateurs de gains du tour
     * (cartes Trèfle, combinaisons, Porte-bonheur, Mise, carte Bingo).
     *
     * @param perSymbol bonus par symbole aligné, déjà à l'échelle du coût du tirage
     * @param rankPerLine bonus de gains du rang par alignement, déjà à l'échelle du coût du tirage
     * @param factor    multiplicateurs de gains du tour
     * @return ce Jeu bonus, ses gains ainsi comptés
     */
    public BonusGame boosted(int perSymbol, int rankPerLine, float factor) {
        List<Line> boosted = new ArrayList<>();
        for (Line line : lines) {
            double extra = ((double) perSymbol * line.length() + rankPerLine) * factor;
            double gains = line.gains() + extra;
            boosted.add(new Line(line.symbol(), line.cells(), BigNumber.round(gains)));
        }
        return new BonusGame(grids, frozen, boosted, pot);
    }

    /**
     * @return ce Jeu bonus où chaque alignement paie en plus toute la cagnotte
     *         {@code pot} ({@link BonusPot}) : sa valeur + la cagnotte (choix d'Astra)
     */
    public BonusGame withPot(double pot) {
        if (pot <= 0) return this;
        List<Line> paid = new ArrayList<>();
        for (Line line : lines) paid.add(new Line(line.symbol(), line.cells(), BigNumber.add(line.gains(), pot)));
        return new BonusGame(grids, frozen, paid, BigNumber.add(this.pot, pot));
    }

    /** @return la cagnotte payée par chaque alignement (petits Jeux bonus : la cagnotte jouée), 0 sans cagnotte. */
    public double getPot() { return pot; }

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
    public double getTotal() { return total; }

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
