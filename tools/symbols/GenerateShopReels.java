import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.BiPredicate;

/**
 * Dessine les rouleaux vendus à la boutique (assets/symbols/13 à 20) et le
 * Rouleau de la Mine (21, la Pépite), dans le
 * style des rouleaux classiques : 24 x 20 pixels de 6 px sur fond gris clair,
 * contour sombre, bord éclairé en haut à gauche et ombré en bas à droite.
 * Dessine aussi leur carte Bingo de test (assets/cards/test/), sur le modèle de
 * celle de la Cloche, et la nouvelle carte de la boutique
 * (Gains +2000) à partir d'une carte existante.
 *
 * Lancement, depuis la racine du dépôt : java tools/symbols/GenerateShopReels.java
 */
public class GenerateShopReels {

    private static final int W = 24, H = 20, PIXEL = 6, CARD_PIXEL = 12;
    private static final int BACKGROUND = 0xfff1f1f1, OUTLINE = 0xff28161e;

    public static void main(String[] args) throws Exception {
        reel("13-horseshoe", "bingo_horseshoe", horseshoe());
        reel("14-ecu", "bingo_ecu", ecu());
        reel("15-sword", "bingo_sword", sword());
        reel("16-heart", "bingo_heart", heart());
        reel("17-die", "bingo_die", die());
        reel("18-star", "bingo_star", star());
        reel("19-bomb", "bingo_bomb", bomb());
        reel("20-crown", "bingo_crown", crown());
        reel("21-nugget", "bingo_nugget", nugget());
        recolor("gain_500", "gain_2000", new int[] {0xff460a1a, 0xff681228, 0xff540e20},
            new int[] {0xff0a1e46, 0xff123c68, 0xff0e2c54});
    }

    // -------------------------------------------------------------------------
    // Rouleaux
    // -------------------------------------------------------------------------

    /** Couleurs d'un rouleau : base, éclairée (haut/gauche), ombrée (bas/droite). */
    private record Shade(int base, int light, int dark) { }

    private static final Shade GOLD   = new Shade(0xfff5be32, 0xfffff0a0, 0xffbe7814);
    private static final Shade STEEL  = new Shade(0xffc8d0dc, 0xfff4f8ff, 0xff7c8696);
    private static final Shade BLUE   = new Shade(0xff2f62d0, 0xff6f9cff, 0xff1a3a8a);
    private static final Shade RED    = new Shade(0xffe02a44, 0xffff8fa0, 0xff9a1028);
    private static final Shade IVORY  = new Shade(0xfff6f2e8, 0xffffffff, 0xffc8c0b0);
    private static final Shade COAL   = new Shade(0xff3a3a4e, 0xff6a6a88, 0xff1e1e2c);
    private static final Shade BROWN  = new Shade(0xff8a5a2a, 0xffb07a40, 0xff5e3a18);
    private static final Shade YELLOW = new Shade(0xffffd23a, 0xfffff6b0, 0xffe0901a);

    /** Grille de couleurs du rouleau ({@code 0} : vide). */
    private static int[][] grid() { return new int[H][W]; }

    /** Remplit les pixels où {@code inside} est vrai, éclairés et ombrés sur leurs bords. */
    private static void fill(int[][] g, Shade shade, BiPredicate<Double, Double> inside) {
        boolean[][] in = new boolean[H][W];
        for (int y = 0; y < H; y++) for (int x = 0; x < W; x++) in[y][x] = inside.test(x + 0.5, y + 0.5);
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                if (!in[y][x]) continue;
                boolean lightEdge = y == 0 || x == 0 || !in[y - 1][x] || !in[y][x - 1];
                boolean darkEdge  = y == H - 1 || x == W - 1 || !in[y + 1][x] || !in[y][x + 1];
                g[y][x] = darkEdge ? shade.dark() : lightEdge ? shade.light() : shade.base();
            }
        }
    }

    /** Pose un pixel de {@code color} (détails : clous, pips, joyaux). */
    private static void dot(int[][] g, int x, int y, int color) { g[y][x] = color; }

    private static int[][] horseshoe() {
        int[][] g = grid();
        double cx = 12, cy = 9.5;
        fill(g, GOLD, (x, y) -> {
            double dx = x - cx, dy = y - cy, r = Math.hypot(dx, dy);
            boolean ring = y >= cy && r <= 8.6 && r >= 4.2;
            boolean arms = y >= 2 && y < cy && Math.abs(dx) <= 8.6 && Math.abs(dx) >= 4.2;
            return ring || arms;
        });
        for (int[] nail : new int[][] {{5, 4}, {5, 8}, {18, 4}, {18, 8}, {7, 14}, {16, 14}}) {
            dot(g, nail[0], nail[1], OUTLINE);
        }
        return g;
    }

    private static int[][] ecu() {
        int[][] g = grid();
        BiPredicate<Double, Double> shield = (x, y) -> {
            if (y < 1.5 || y > 18.5) return false;
            double half = y < 10 ? 8 : 8 * (18.5 - y) / 8.5;
            return Math.abs(x - 12) <= half;
        };
        fill(g, GOLD, shield);
        fill(g, BLUE, (x, y) -> shield.test(x, y) && y > 2.5 && y < 17 && Math.abs(x - 12) <= (y < 10 ? 6.5 : 6.5 * (17 - y) / 7));
        fill(g, GOLD, (x, y) -> y > 4 && y < 15 && Math.abs(x - 12) <= 1 || y > 7 && y < 9.5 && Math.abs(x - 12) <= 4.5);
        return g;
    }

    private static int[][] sword() {
        int[][] g = grid();
        fill(g, STEEL, (x, y) -> y >= 1 && y < 11.5 && Math.abs(x - 12) <= (y < 2.5 ? 0.6 : 1.6));
        fill(g, GOLD, (x, y) -> y >= 11.5 && y < 13.5 && Math.abs(x - 12) <= 6);
        fill(g, BROWN, (x, y) -> y >= 13.5 && y < 16.5 && Math.abs(x - 12) <= 1);
        fill(g, GOLD, (x, y) -> y >= 16.5 && y < 18 && Math.abs(x - 12) <= 2);
        for (int y = 3; y < 11; y++) dot(g, 12, y, 0xff9aa4b4); // arête de la lame
        return g;
    }

    private static int[][] heart() {
        int[][] g = grid();
        fill(g, RED, (x, y) -> {
            double u = (x - 12) / 8.0, v = -(y - 10.3) / 6.8;
            double a = u * u + v * v - 1;
            return a * a * a - u * u * v * v * v <= 0;
        });
        dot(g, 7, 7, 0xffffffff);
        dot(g, 8, 6, 0xffffffff);
        return g;
    }

    private static int[][] die() {
        int[][] g = grid();
        fill(g, IVORY, (x, y) -> {
            double dx = Math.max(0, Math.abs(x - 12) - 6), dy = Math.max(0, Math.abs(y - 10) - 6);
            return Math.hypot(dx, dy) <= 2;
        });
        int pip = 0xffc0283a;
        for (int[] p : new int[][] {{8, 6}, {15, 6}, {8, 13}, {15, 13}, {11, 9}}) {
            dot(g, p[0], p[1], pip);
            dot(g, p[0] + 1, p[1], pip);
            dot(g, p[0], p[1] + 1, pip);
            dot(g, p[0] + 1, p[1] + 1, pip);
        }
        return g;
    }

    private static int[][] star() {
        int[][] g = grid();
        double cx = 12, cy = 10.5, outer = 9.8, inner = 4.2;
        double[] px = new double[10], py = new double[10];
        for (int i = 0; i < 10; i++) {
            double r = i % 2 == 0 ? outer : inner, angle = -Math.PI / 2 + i * Math.PI / 5;
            px[i] = cx + r * Math.cos(angle);
            py[i] = cy + r * Math.sin(angle);
        }
        fill(g, YELLOW, (x, y) -> insidePolygon(px, py, x, y));
        return g;
    }

    private static int[][] bomb() {
        int[][] g = grid();
        fill(g, COAL, (x, y) -> Math.hypot(x - 11, y - 12) <= 7.3);
        fill(g, STEEL, (x, y) -> x >= 14 && x < 17 && y >= 3 && y < 5.5);
        for (int[] p : new int[][] {{17, 3}, {18, 2}, {19, 2}}) dot(g, p[0], p[1], 0xffe8a040);
        for (int[] p : new int[][] {{20, 1}, {21, 0}, {21, 2}, {22, 1}}) dot(g, p[0], p[1], 0xffffd23a);
        dot(g, 21, 1, 0xffffffff);
        dot(g, 8, 8, 0xffffffff);
        return g;
    }

    private static int[][] crown() {
        int[][] g = grid();
        fill(g, GOLD, (x, y) -> {
            if (y >= 12.5 && y < 17.5) return x >= 3 && x < 21;
            if (y < 3 || y >= 12.5 || x < 3 || x >= 21) return false;
            double t = (12.5 - y) / 9.5; // 0 en bas des pointes, 1 au sommet
            for (double peak : new double[] {4, 12, 20}) {
                if (Math.abs(x - peak) <= 3.2 * (1 - t) + 0.6) return true;
            }
            return y >= 9;
        });
        dot(g, 7, 14, 0xffc0283a);
        dot(g, 8, 14, 0xffc0283a);
        dot(g, 11, 14, 0xff2f62d0);
        dot(g, 12, 14, 0xff2f62d0);
        dot(g, 15, 14, 0xffc0283a);
        dot(g, 16, 14, 0xffc0283a);
        for (double peak : new double[] {4, 12, 20}) dot(g, (int) peak, 2, 0xffffffff);
        return g;
    }

    /** Pépite : un bloc d'or bosselé, encore pris dans sa roche en bas. */
    private static int[][] nugget() {
        int[][] g = grid();
        fill(g, BROWN, (x, y) -> y >= 13 && Math.hypot((x - 12) / 1.6, y - 19) <= 6.4);
        double[][] lumps = {{8, 9, 3.6}, {13, 6, 3.3}, {16, 10, 3.7}, {11, 12, 4.0}, {6, 13, 2.6}, {17, 14, 2.4}};
        fill(g, GOLD, (x, y) -> {
            for (double[] l : lumps) if (Math.hypot(x - l[0], y - l[1]) <= l[2]) return true;
            return false;
        });
        for (int[] p : new int[][] {{7, 7}, {8, 6}, {12, 4}, {13, 4}, {15, 8}}) dot(g, p[0], p[1], 0xffffffff);
        for (int[] p : new int[][] {{11, 9}, {14, 12}, {8, 12}}) dot(g, p[0], p[1], 0xffbe7814);
        for (int[] p : new int[][] {{5, 17}, {19, 18}, {8, 18}}) dot(g, p[0], p[1], 0xff5e3a18);
        return g;
    }

    private static boolean insidePolygon(double[] px, double[] py, double x, double y) {
        boolean inside = false;
        for (int i = 0, j = px.length - 1; i < px.length; j = i++) {
            if ((py[i] > y) != (py[j] > y) && x < (px[j] - px[i]) * (y - py[i]) / (py[j] - py[i]) + px[i]) {
                inside = !inside;
            }
        }
        return inside;
    }

    /** Ajoute le contour sombre autour du dessin. */
    private static int[][] outline(int[][] g) {
        int[][] out = new int[H][W];
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                out[y][x] = g[y][x];
                if (g[y][x] != 0) continue;
                for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                    int nx = x + d[0], ny = y + d[1];
                    if (nx >= 0 && ny >= 0 && nx < W && ny < H && g[ny][nx] != 0 && g[ny][nx] != OUTLINE) {
                        out[y][x] = OUTLINE;
                    }
                }
            }
        }
        return out;
    }

    /** Écrit le rouleau et sa carte Bingo de test (le modèle : celle de la Cloche). */
    private static void reel(String name, String bingo, int[][] art) throws Exception {
        int[][] g = outline(art);
        BufferedImage image = new BufferedImage(W * PIXEL, H * PIXEL, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) block(image, x, y, PIXEL, g[y][x] == 0 ? BACKGROUND : g[y][x]);
        }
        ImageIO.write(image, "png", new File("assets/symbols/" + name + ".png"));

        BufferedImage card = ImageIO.read(new File("assets/cards/test/bingo_bell.png"));
        int panel = card.getRGB(8 * CARD_PIXEL + 1, 16 * CARD_PIXEL + 1);
        for (int y = 15; y <= 38; y++) for (int x = 7; x <= 32; x++) block(card, x, y, CARD_PIXEL, panel);
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) if (g[y][x] != 0) block(card, 8 + x, 17 + y, CARD_PIXEL, g[y][x]);
        }
        ImageIO.write(card, "png", new File("assets/cards/test/" + bingo + ".png"));
    }

    // -------------------------------------------------------------------------
    // Cartes de la boutique
    // -------------------------------------------------------------------------

    /** Copie la carte {@code from} en remplaçant chaque couleur de {@code before} par celle de {@code after}. */
    private static void recolor(String from, String to, int[] before, int[] after) throws Exception {
        BufferedImage card = ImageIO.read(new File("assets/cards/special/" + from + ".png"));
        for (int y = 0; y < card.getHeight(); y++) {
            for (int x = 0; x < card.getWidth(); x++) {
                int color = card.getRGB(x, y);
                for (int i = 0; i < before.length; i++) if (color == before[i]) card.setRGB(x, y, after[i]);
            }
        }
        ImageIO.write(card, "png", new File("assets/cards/special/" + to + ".png"));
    }

    private static void block(BufferedImage image, int x, int y, int size, int color) {
        for (int dy = 0; dy < size; dy++) for (int dx = 0; dx < size; dx++) image.setRGB(x * size + dx, y * size + dy, color);
    }
}
