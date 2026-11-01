import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.BiPredicate;

/**
 * Dessine les rouleaux vendus à la boutique (assets/symbols/13 à 20, puis 22 à 28),
 * les rouleaux des lieux (21 la Pépite, 29 à 32) et ceux des Bingos imposés (33, 34), dans le
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
        // Nouveaux rouleaux (2026-11-01)
        reel("22-fangs", "bingo_fangs", fangs());
        reel("23-piggy_bank", "bingo_piggy_bank", piggyBank());
        reel("24-spiked_shield", "bingo_spiked_shield", spikedShield());
        reel("25-scythe", "bingo_scythe", scythe());
        reel("26-moon", "bingo_moon", moon());
        reel("27-demon_king", "bingo_demon_king", demonKing());
        reel("28-luck_apostle", "bingo_luck_apostle", luckApostle());
        reel("29-ticket", "bingo_ticket", ticket());
        reel("30-anchor", "bingo_anchor", anchor());
        reel("31-gold_chip", "bingo_gold_chip", goldChip());
        reel("32-flame", "bingo_flame", flame());
        reel("33-descent", "bingo_descent", descent());
        reel("34-bingo_light", "bingo_bingo_light", bingoLight());
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
    private static final Shade PINK   = new Shade(0xffff9ab8, 0xffffd0de, 0xffd0607e);
    private static final Shade GUM    = new Shade(0xffb01830, 0xffe04a60, 0xff700a1a);
    private static final Shade GREEN  = new Shade(0xff3cb44a, 0xff8ee890, 0xff1e7a2a);
    private static final Shade WHITE  = new Shade(0xfffafaff, 0xffffffff, 0xffc8d0e0);
    private static final Shade ORANGE = new Shade(0xffff7a1a, 0xffffb060, 0xffc84a0a);
    private static final Shade CRIMSON = new Shade(0xffd0202c, 0xffff6a5a, 0xff800a14);

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

    /** Crocs : deux longs crocs blancs sous une gencive rouge, une goutte de sang au bout de l'un. */
    private static int[][] fangs() {
        int[][] g = grid();
        fill(g, GUM, (x, y) -> y >= 2 && y < 7 && Math.abs(x - 12) <= 10 - (y - 2) * 0.4);
        for (double cx : new double[] {7.5, 16.5}) {
            fill(g, IVORY, (x, y) -> y >= 6 && y < 17 && Math.abs(x - cx) <= 2.6 * (17 - y) / 11.0 + 0.3);
        }
        fill(g, IVORY, (x, y) -> y >= 6 && y < 9 && Math.abs(x - 12) <= 1.6);
        fill(g, RED, (x, y) -> Math.hypot(x - 16.5, y - 18.2) <= 1.4);
        dot(g, 6, 8, 0xffffffff);
        dot(g, 15, 8, 0xffffffff);
        return g;
    }

    /** Tirelire : un cochon rose, sa fente sur le dos et une pièce d'or qui y tombe. */
    private static int[][] piggyBank() {
        int[][] g = grid();
        fill(g, PINK, (x, y) -> Math.hypot((x - 11.5) / 8.6, (y - 12) / 5.6) <= 1);                  // corps
        fill(g, PINK, (x, y) -> y >= 16 && y < 19.5 && (Math.abs(x - 7) <= 1.2 || Math.abs(x - 15) <= 1.2)); // pattes
        fill(g, PINK, (x, y) -> y >= 5 && y < 8 && x >= 13.5 && x < 16.5 && y > 5 + (16.5 - x) * 0.6);  // oreille
        fill(g, PINK, (x, y) -> x >= 19 && x < 22.5 && y >= 9.5 && y < 14);                         // groin
        fill(g, GOLD, (x, y) -> Math.hypot(x - 10.5, y - 2.5) <= 2.2);                              // la pièce
        for (int x = 8; x <= 13; x++) dot(g, x, 7, OUTLINE);                                       // la fente
        dot(g, 16, 10, OUTLINE);                                                                    // l'oeil
        dot(g, 20, 11, 0xffd0607e);
        dot(g, 21, 12, 0xffd0607e);
        return g;
    }

    /** Bouclier clouté : une rondache d'acier hérissée de pointes, l'umbo doré au centre. */
    private static int[][] spikedShield() {
        int[][] g = grid();
        double cx = 12, cy = 10;
        fill(g, STEEL, (x, y) -> {
            double r = Math.hypot(x - cx, y - cy), a = Math.atan2(y - cy, x - cx);
            double spike = Math.max(0, Math.cos(a * 8)) * 2.2;
            return r <= 6.8 + spike;
        });
        fill(g, BLUE, (x, y) -> Math.hypot(x - cx, y - cy) <= 5.2);
        fill(g, GOLD, (x, y) -> Math.hypot(x - cx, y - cy) <= 2.4);
        dot(g, 11, 9, 0xffffffff);
        return g;
    }

    /** Faux : un long manche de bois en travers, une grande lame courbe d'acier qui part de son sommet. */
    private static int[][] scythe() {
        int[][] g = grid();
        fill(g, BROWN, (x, y) -> y >= 3 && y < 19.5 && Math.abs(x - (5 + (19 - y) * 0.75)) <= 1.0);   // le manche
        fill(g, STEEL, (x, y) -> x >= 1.5 && x < 18.5 && y >= 1 && y < 9
            && Math.hypot((x - 13) / 12.5, (y - 10) / 8.5) <= 1 && Math.hypot((x - 14) / 12.5, (y - 12.5) / 9.5) > 1);
        dot(g, 3, 6, 0xfff4f8ff);
        return g;
    }

    /** Lune : un croissant jaune pâle, deux petites étoiles à côté. */
    private static int[][] moon() {
        int[][] g = grid();
        fill(g, YELLOW, (x, y) -> Math.hypot(x - 10, y - 10) <= 8.2 && Math.hypot(x - 14, y - 7.5) > 7);
        for (int[] p : new int[][] {{18, 12}, {20, 5}}) {
            dot(g, p[0], p[1], 0xffffffff);
            dot(g, p[0] - 1, p[1], 0xffffd23a);
            dot(g, p[0] + 1, p[1], 0xffffd23a);
            dot(g, p[0], p[1] - 1, 0xffffd23a);
            dot(g, p[0], p[1] + 1, 0xffffd23a);
        }
        dot(g, 6, 8, 0xffe0901a);
        dot(g, 8, 14, 0xffe0901a);
        return g;
    }

    /** Roi démon : une tête rouge aux cornes noires, coiffée d'une couronne d'or, aux yeux de braise. */
    private static int[][] demonKing() {
        int[][] g = grid();
        fill(g, COAL, (x, y) -> y >= 1 && y < 9 && (Math.abs(x - 4.5 - (9 - y) * -0.2) <= (y - 1) * 0.35 + 0.4
            || Math.abs(x - 19.5 - (9 - y) * 0.2) <= (y - 1) * 0.35 + 0.4));                        // cornes
        fill(g, CRIMSON, (x, y) -> Math.hypot((x - 12) / 7.2, (y - 12.5) / 6.8) <= 1);                 // tête
        fill(g, GOLD, (x, y) -> y >= 2 && y < 7 && x >= 8 && x < 16
            && (y >= 5 || Math.abs(x - 8.5) < 1 || Math.abs(x - 12) < 1 || Math.abs(x - 15.5) < 1)); // couronne
        for (int[] p : new int[][] {{9, 11}, {10, 11}, {14, 11}, {15, 11}}) dot(g, p[0], p[1], 0xffffd23a);
        for (int x = 9; x <= 15; x++) dot(g, x, 15, OUTLINE);                                    // la bouche
        dot(g, 10, 16, 0xffffffff);
        dot(g, 14, 16, 0xffffffff);
        dot(g, 12, 5, 0xffc0283a);
        return g;
    }

    /** Apôtre de la Chance : un trèfle à quatre feuilles ailé, une auréole d'or au-dessus. */
    private static int[][] luckApostle() {
        int[][] g = grid();
        fill(g, WHITE, (x, y) -> y >= 6 && y < 15 && x < 8 && Math.hypot((x - 7) / 6.5, (y - 6) / 7.5) <= 1
            && Math.hypot((x - 7) / 5.5, (y - 1) / 9.5) > 1);                                     // aile gauche
        fill(g, WHITE, (x, y) -> y >= 6 && y < 15 && x >= 16 && Math.hypot((x - 17) / 6.5, (y - 6) / 7.5) <= 1
            && Math.hypot((x - 17) / 5.5, (y - 1) / 9.5) > 1);                                    // aile droite
        fill(g, GREEN, (x, y) -> {
            for (double[] leaf : new double[][] {{9.8, 8.4}, {14.2, 8.4}, {9.8, 12.8}, {14.2, 12.8}}) {
                if (Math.hypot(x - leaf[0], y - leaf[1]) <= 2.6) return true;
            }
            return false;
        });
        fill(g, GREEN, (x, y) -> x >= 11.5 && x < 13 && y >= 15 && y < 19.5);                       // tige
        for (int i = 8; i <= 16; i++) dot(g, i, 10, 0xff1e7a2a);                                  // les quatre feuilles
        for (int i = 6; i <= 14; i++) dot(g, 12, i, 0xff1e7a2a);
        for (int x = 9; x <= 15; x++) {                                                           // l'auréole
            dot(g, x, 1, 0xfffff0a0);
            dot(g, x, 4, 0xffbe7814);
        }
        for (int y = 2; y <= 3; y++) {
            dot(g, 8, y, 0xfff5be32);
            dot(g, 16, y, 0xfff5be32);
        }
        for (int[] p : new int[][] {{3, 9}, {5, 11}, {20, 9}, {18, 11}}) dot(g, p[0], p[1], 0xffc8d0e0); // plumes
        return g;
    }

    /** Ticket : un ticket de fête foraine orange aux bords crantés, une étoile au milieu. */
    private static int[][] ticket() {
        int[][] g = grid();
        fill(g, ORANGE, (x, y) -> y >= 4 && y < 16 && x >= 1.5 && x < 22.5
            && Math.hypot(x - 1.5, y - 10) > 2.2 && Math.hypot(x - 22.5, y - 10) > 2.2);
        double[] px = new double[10], py = new double[10];
        for (int i = 0; i < 10; i++) {
            double r = i % 2 == 0 ? 4.2 : 1.8, angle = -Math.PI / 2 + i * Math.PI / 5;
            px[i] = 12 + r * Math.cos(angle);
            py[i] = 10.3 + r * Math.sin(angle);
        }
        fill(g, YELLOW, (x, y) -> insidePolygon(px, py, x, y));
        for (int y = 5; y < 16; y += 2) {
            dot(g, 5, y, 0xffc84a0a);
            dot(g, 18, y, 0xffc84a0a);
        }
        return g;
    }

    /** Ancre : un anneau, une tige et sa barre, deux bras courbes terminés par des pattes. */
    private static int[][] anchor() {
        int[][] g = grid();
        fill(g, STEEL, (x, y) -> {
            double ring = Math.hypot(x - 12, y - 2.8);
            if (ring <= 2.4 && ring >= 1.1) return true;
            if (Math.abs(x - 12) <= 1 && y >= 5 && y < 17) return true;                             // tige
            if (y >= 6.5 && y < 8.5 && Math.abs(x - 12) <= 5) return true;                         // barre
            double arm = Math.hypot(x - 12, y - 9.5);
            return y >= 11 && arm <= 9 && arm >= 7.3;                                               // bras
        });
        fill(g, STEEL, (x, y) -> y >= 9 && y < 13 && (Math.abs(x - 3.6) <= (13 - y) * 0.6 || Math.abs(x - 20.4) <= (13 - y) * 0.6));
        return g;
    }

    /** Jeton d'or : un jeton de casino doré, ses encoches blanches tout autour, le centre plus sombre. */
    private static int[][] goldChip() {
        int[][] g = grid();
        double cx = 12, cy = 10;
        fill(g, GOLD, (x, y) -> Math.hypot(x - cx, y - cy) <= 8.6);
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                double r = Math.hypot(x + 0.5 - cx, y + 0.5 - cy), a = Math.atan2(y + 0.5 - cy, x + 0.5 - cx);
                if (r <= 8.2 && r >= 6.4 && Math.cos(a * 8) > 0.55) g[y][x] = 0xfffffbe8;
                if (r <= 4.6 && r >= 3.8) g[y][x] = 0xffbe7814;
            }
        }
        dot(g, 10, 8, 0xffffffff);
        return g;
    }

    /** Flamme : une flamme en goutte, pointe en haut, rouge dehors, orange puis jaune au coeur. */
    private static int[][] flame() {
        int[][] g = grid();
        fill(g, CRIMSON, (x, y) -> flameShape(x, y, 1.0));
        fill(g, ORANGE, (x, y) -> flameShape(x, y, 0.68));
        fill(g, YELLOW, (x, y) -> flameShape(x, y, 0.38));
        return g;
    }

    /** La goutte de la Flamme, réduite de {@code k} autour de sa base. */
    private static boolean flameShape(double x, double y, double k) {
        double bx = 12, by = 18.5;
        x = bx + (x - bx) / k;
        y = by + (y - by) / k;
        double lean = (13 - y) * 0.18;
        if (Math.hypot(x - 12, y - 12.8) <= 6.2) return true;
        return y >= 0.5 && y < 12.8 && Math.abs(x - 12 - lean) <= 6.2 * (y - 0.5) / 12.3;
    }

    /** Descente aux enfers : une grande flèche rouge qui plonge dans un gouffre noir bordé de flammes. */
    private static int[][] descent() {
        int[][] g = grid();
        fill(g, ORANGE, (x, y) -> y >= 10 && y < 18 && (Math.abs(x - 3) <= (18 - y) * 0.35 || Math.abs(x - 21) <= (18 - y) * 0.35
            || Math.abs(x - 6.5) <= (18 - y) * 0.25 || Math.abs(x - 17.5) <= (18 - y) * 0.25));   // flammes
        fill(g, COAL, (x, y) -> Math.hypot((x - 12) / 10, (y - 17) / 2.8) <= 1);                   // le gouffre
        fill(g, CRIMSON, (x, y) -> (y >= 0.5 && y < 9 && Math.abs(x - 12) <= 1.6)
            || (y >= 8 && y < 16 && Math.abs(x - 12) <= (16 - y) * 0.8));                           // la flèche
        for (int x = 6; x <= 18; x++) dot(g, x, 18, 0xffff7a1a);                                  // la braise au fond
        return g;
    }

    /** Lumière du bingo : un soleil blanc et or qui rayonne dans toutes les directions. */
    private static int[][] bingoLight() {
        int[][] g = grid();
        double cx = 12, cy = 10;
        fill(g, YELLOW, (x, y) -> {
            double dx = x - cx, dy = y - cy, r = Math.hypot(dx, dy), a = Math.atan2(dy, dx);
            return r <= 9.6 && Math.cos(a * 8) > 0.82 && r > 5;
        });
        fill(g, GOLD, (x, y) -> Math.hypot(x - cx, y - cy) <= 5.2);
        fill(g, WHITE, (x, y) -> Math.hypot(x - cx, y - cy) <= 3.4);
        dot(g, 11, 9, 0xffffffff);
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
