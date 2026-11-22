package fr.astratime.lucky.assets;

import java.util.Random;

import static fr.astratime.lucky.assets.PixelCanvas.rgba;

/**
 * Dessins en pixel art du chapitre de la Tour « La Guerre des Tables » : son
 * illustration (deux armées de tables de jeu face à face, retranchées derrière
 * des piles de jetons, sous un ciel de poudre) et les symboles des rouleaux de
 * ses ennemis (16 x 16). Les portraits sont dans {@link WarPortraits}.
 */
public final class WarArt {

    private static final int GOLD = rgba("ffd54a"), GOLD_DARK = rgba("c4891e"), WHITE = rgba("f6f0e2");
    private static final int RED = rgba("d8283c"), RED_DARK = rgba("8a1424"), BLUE = rgba("2a5ad8"), BLUE_DARK = rgba("1a3488");
    private static final int GREEN = rgba("1f9a4a"), FELT = rgba("2e7a46"), FELT_DARK = rgba("1e5230");
    private static final int IRON = rgba("4a4a58"), IRON_DARK = rgba("2a2a34"), IRON_LIGHT = rgba("7a7a8c");
    private static final int WOOD = rgba("7a5030"), WOOD_DARK = rgba("4a2e1a");
    private static final int PAPER = rgba("efe2c0"), PAPER_DARK = rgba("c8b48a"), INK = rgba("1a1018");
    private static final int SMOKE = rgba("5a4e4a"), SMOKE_LIGHT = rgba("7a6e66"), FIRE = rgba("ff8a1e");

    private WarArt() {}

    // -------------------------------------------------------------------------
    // Symboles des rouleaux ennemis
    // -------------------------------------------------------------------------

    /** Rassemblement : un clairon doré, sa flamme rouge marquée d'un pique. */
    static int[][] rally() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.rect(2, 9, 9, 10, GOLD);                        // le tube
        g.rect(2, 10, 9, 10, GOLD_DARK);
        g.rect(1, 8, 2, 11, GOLD_DARK);                   // l'embouchure
        g.triangle(9, 9, 14, 5, 14, 14, GOLD);            // le pavillon
        g.line(14, 5, 14, 14, GOLD_DARK);
        g.set(11, 9, WHITE);
        g.rect(4, 11, 8, 14, RED);                        // la flamme qui pend du tube
        g.set(4, 14, 0); g.set(8, 14, 0); g.set(6, 14, RED_DARK);
        g.set(6, 12, INK);                                // le pique
        g.rect(5, 13, 7, 13, INK);
        g.line(5, 2, 7, 5, WHITE);                        // les notes qui sonnent
        g.line(9, 1, 10, 4, WHITE);
        return g.outlined();
    }

    /** Canon à jetons : un petit canon de fer sur son affût de bois, un jeton rouge en guise de boulet. */
    static int[][] cannon() {
        PixelCanvas g = new PixelCanvas(16, 16);
        for (int k = 0; k <= 8; k++) {                    // le fût, incliné vers le haut à droite
            g.rect(2 + k, 8 - k / 2, 3 + k, 10 - k / 2, k < 2 ? IRON_DARK : IRON);
            g.set(2 + k, 8 - k / 2, IRON_LIGHT);
        }
        g.rect(11, 3, 12, 6, IRON_DARK);                  // la bouche
        g.rect(3, 11, 10, 12, WOOD);                      // l'affût
        g.ellipseShaded(5f, 13f, 2.2f, 2.2f, WOOD, WOOD_DARK, 1f);
        g.ellipseShaded(10f, 13f, 2.2f, 2.2f, WOOD, WOOD_DARK, 1f);
        g.set(5, 13, IRON_DARK); g.set(10, 13, IRON_DARK);
        g.ellipse(13.5f, 2f, 1.8f, 1.6f, RED);           // le jeton qui part
        g.set(13, 2, WHITE);
        return g.outlined();
    }

    /** Espionnage : une loupe posée sur une carte à jouer retournée, un oeil dans le verre. */
    static int[][] spy() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.rect(1, 3, 9, 14, BLUE_DARK);                   // le dos de la carte
        g.rect(2, 4, 8, 13, BLUE);
        for (int y = 5; y <= 12; y += 2) for (int x = 3; x <= 7; x += 2) g.set(x, y, BLUE_DARK);
        g.ellipseShaded(9.5f, 7f, 4.5f, 4.5f, IRON_LIGHT, IRON, 2f); // le cercle de la loupe
        g.ellipse(9.5f, 7f, 3.2f, 3.2f, rgba("c8e4f0"));
        g.ellipse(9.5f, 7f, 2.4f, 1.4f, WHITE);           // l'oeil
        g.ellipse(9.5f, 7f, 1f, 1f, INK);
        g.set(8, 5, WHITE);
        g.line(12, 10, 15, 14, WOOD_DARK);                // le manche
        g.line(13, 10, 15, 13, WOOD);
        return g.outlined();
    }

    /** Contrat : un parchemin roulé, son sceau de cire rouge, une pièce d'or posée dessus. */
    static int[][] contract() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.rect(3, 2, 12, 13, PAPER);
        g.rect(10, 2, 12, 13, PAPER_DARK);
        g.ellipseShaded(7.5f, 2f, 5.5f, 1.3f, PAPER, PAPER_DARK, 0.5f); // le rouleau du haut
        g.ellipseShaded(7.5f, 13.5f, 5.5f, 1.3f, PAPER, PAPER_DARK, 0.5f);
        for (int y = 4; y <= 9; y += 2) g.line(5, y, 10, y, PAPER_DARK); // les lignes du texte
        g.line(5, 11, 8, 11, INK);                        // la signature
        g.ellipse(11f, 11f, 2f, 2f, RED);                 // le sceau
        g.set(11, 11, RED_DARK);
        g.ellipseShaded(4f, 12f, 2.4f, 2.4f, GOLD, GOLD_DARK, 1f); // la prime
        g.set(3, 11, WHITE);
        return g.outlined();
    }

    /** Va-tout : deux grosses piles de jetons poussées en avant, une flèche rouge dessus. */
    static int[][] allOut() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int[][] stacks = {{3, 6}, {12, 5}};
        int[] colors = {RED, BLUE, GREEN, GOLD};
        for (int[] s : stacks) {
            for (int k = 0; k < s[1]; k++) {
                int y = 14 - k * 2, color = colors[(k + s[0]) % colors.length];
                g.rect(s[0] - 2, y - 1, s[0] + 2, y, color);
                g.set(s[0], y, WHITE);
            }
        }
        g.line(1, 3, 12, 3, RED);                         // la flèche : tout part au milieu
        g.line(1, 2, 12, 2, RED);
        g.triangle(12, 0, 12, 5, 15, 2, RED);
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Illustration du chapitre
    // -------------------------------------------------------------------------

    /**
     * « La Guerre des Tables » : au petit matin, deux armées de tables de jeu se font face
     * sur un champ de feutre ; chacune s'abrite derrière une tranchée de piles de jetons,
     * sous ses étendards (Pique, Coeur). Au milieu, des cartes éparpillées et un canon qui
     * tire un jeton ; la fumée de poudre monte dans le ciel.
     */
    static int[][] warOfTables() {
        int w = EnemyPortraits.ART_WIDTH, h = EnemyPortraits.ART_HEIGHT;
        PixelCanvas g = new PixelCanvas(w, h);
        // Ciel d'aube voilé de poudre.
        int[] sky = {rgba("2a2438"), rgba("3a2e44"), rgba("54404e"), rgba("7a5650"), rgba("a87454"), rgba("d09a62")};
        for (int y = 0; y < h; y++) {
            int band = Math.min(sky.length - 1, y * sky.length / 46);
            for (int x = 0; x < w; x++) g.set(x, y, sky[band]);
        }
        Random random = new Random(7);
        g.ellipse(64f, 44f, 10f, 6f, rgba("f0c070"));       // le soleil bas, à travers la fumée
        // Les nuages de poudre.
        for (int i = 0; i < 14; i++) {
            float x = 8 + i * 9f + random.nextInt(4), y = 18 + random.nextInt(14);
            g.ellipse(x, y, 7f + random.nextInt(4), 3f + random.nextInt(2), i % 3 == 0 ? SMOKE_LIGHT : SMOKE);
        }
        // Le champ de bataille : un immense tapis de feutre, ses lignes de jeu.
        g.rect(0, 46, w - 1, h - 1, FELT);
        for (int y = 46; y < h; y += 7) g.rect(0, y, w - 1, y, FELT_DARK);
        g.line(0, 62, w - 1, 58, rgba("e8dcc0"));
        // Les deux armées : des tables rondes en rangs, chacune avec ses joueurs-soldats.
        drawArmy(g, 4, 34, 50, RED, RED_DARK, random);
        drawArmy(g, 78, 122, 50, BLUE, BLUE_DARK, random);
        // Les tranchées de jetons devant chaque armée.
        for (int x = 2; x <= 46; x += 4) chips(g, x, 63, 2 + random.nextInt(3), random);
        for (int x = 82; x <= 126; x += 4) chips(g, x, 63, 2 + random.nextInt(3), random);
        // Le no man's land : cartes éparpillées, un canon qui tire un jeton.
        int[][] litter = {{52, 70}, {60, 74}, {70, 69}, {76, 76}, {56, 64}, {68, 66}};
        for (int[] l : litter) {
            g.rect(l[0], l[1], l[0] + 2, l[1] + 3, WHITE);
            g.set(l[0] + 1, l[1] + 1, random.nextBoolean() ? RED : INK);
        }
        g.rect(40, 56, 47, 57, IRON);                       // le canon, côté rouge
        g.rect(46, 54, 49, 56, IRON_DARK);
        g.ellipse(41f, 59f, 2f, 2f, WOOD_DARK);
        g.ellipse(46f, 59f, 2f, 2f, WOOD_DARK);
        g.ellipse(53f, 52f, 3f, 2f, SMOKE_LIGHT);           // le coup de feu
        g.set(51, 53, FIRE);
        g.ellipse(62f, 46f, 1.6f, 1.4f, RED);               // le jeton en vol
        g.set(62, 46, WHITE);
        // Les étendards : un Pique côté rouge, un Coeur côté bleu.
        banner(g, 20, 22, RED, true);
        banner(g, 104, 22, BLUE, false);
        // Les braises de poudre.
        for (int i = 0; i < 24; i++) g.set(random.nextInt(w), 30 + random.nextInt(18), random.nextBoolean() ? FIRE : GOLD);
        return g.pixels;
    }

    /** Une armée de tables rondes de {@code x0} à {@code x1}, au sol {@code ground}, et ses joueurs en silhouette. */
    private static void drawArmy(PixelCanvas g, int x0, int x1, int ground, int coat, int coatDark, Random random) {
        for (int x = x0 + 4; x <= x1 - 4; x += 10) {
            int y = ground + (x / 10) % 2 * 3;
            g.rect(x - 1, y, x, y + 4, WOOD_DARK);                       // le pied
            g.ellipse(x, y, 5f, 1.6f, WOOD);                              // le plateau
            g.ellipse(x, y - 0.4f, 4f, 1f, FELT_DARK);
            for (int side = -1; side <= 1; side += 2) {                   // deux joueurs-soldats
                int sx = x + side * 4;
                g.rect(sx - 1, y - 7, sx, y - 6, rgba("e8b890"));         // la tête
                g.rect(sx - 1, y - 5, sx + 1, y - 1, random.nextBoolean() ? coat : coatDark);
                g.rect(sx - 1, y - 9, sx + 1, y - 8, coatDark);           // le képi
            }
        }
    }

    /** Une pile de {@code count} jetons, posée en {@code (x, ground)}. */
    private static void chips(PixelCanvas g, int x, int ground, int count, Random random) {
        int[] colors = {RED, BLUE, GREEN, GOLD, WHITE};
        for (int k = 0; k < count; k++) {
            int y = ground - k * 2;
            g.rect(x - 2, y - 1, x + 1, y, colors[random.nextInt(colors.length)]);
            g.set(x - 1, y, WHITE);
        }
    }

    /** Un étendard planté en {@code (x, top)} : sa hampe, son drapeau, une couleur de carte dessus. */
    private static void banner(PixelCanvas g, int x, int top, int color, boolean spade) {
        g.rect(x, top, x, top + 30, WOOD_DARK);
        g.rect(x + 1, top + 1, x + 13, top + 9, color);
        g.rect(x + 1, top + 8, x + 13, top + 9, color == RED ? RED_DARK : BLUE_DARK);
        int cx = x + 7, cy = top + 5;
        if (spade) {                                                       // un pique noir
            g.ellipse(cx - 1.5f, cy + 0.5f, 1.6f, 1.4f, INK);
            g.ellipse(cx + 1.5f, cy + 0.5f, 1.6f, 1.4f, INK);
            g.triangle(cx - 3, cy + 1, cx + 3, cy + 1, cx, cy - 3, INK);
            g.rect(cx, cy + 2, cx, cy + 2, INK);
            g.rect(cx - 1, cy + 3, cx + 1, cy + 3, INK);
        } else {                                                           // un coeur blanc
            g.ellipse(cx - 1.5f, cy - 1f, 1.6f, 1.4f, WHITE);
            g.ellipse(cx + 1.5f, cy - 1f, 1.6f, 1.4f, WHITE);
            g.triangle(cx - 3, cy - 1, cx + 3, cy - 1, cx, cy + 3, WHITE);
        }
        g.set(x, top - 1, GOLD);
    }
}
