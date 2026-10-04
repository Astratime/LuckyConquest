package fr.astratime.lucky.assets;

import fr.astratime.lucky.entities.Card;

import java.util.Random;

import static fr.astratime.lucky.assets.PixelCanvas.rgba;

/**
 * Dessins en pixel art de l'Exploration : les soldats et les rois des donjons
 * (en buste, à la taille du croupier), le coffre au trésor (fermé et ouvert),
 * la carte de la prairie et l'entrée de chaque donjon. Chaque méthode rend une
 * grille de couleurs RGBA8888 (0 = transparent), transformée en texture par
 * {@link EnemyTextures}.
 */
public final class ExplorationArt {

    private static final int W = EnemyPortraits.WIDTH, H = EnemyPortraits.HEIGHT;

    /** Taille de la carte de la prairie, en pixels. */
    public static final int MAP_WIDTH = 160, MAP_HEIGHT = 96;
    /** Taille de l'entrée d'un donjon, en pixels. */
    public static final int GATE_SIZE = 26;

    private static final int SKIN  = rgba("f0c8a0"), SKIN_SHADE = rgba("c89a72");
    private static final int PAPER = rgba("f6f0e2"), PAPER_SHADE = rgba("d8d0c0");
    private static final int GOLD  = rgba("ffd54a"), GOLD_DARK = rgba("c4891e");
    private static final int EYE   = rgba("1a1018");

    private ExplorationArt() {}

    // -------------------------------------------------------------------------
    // Couleurs et symboles des couleurs
    // -------------------------------------------------------------------------

    /** @return la couleur d'uniforme des gens de la couleur {@code suit}. */
    static int accent(Card.Suit suit) {
        return switch (suit) {
            case PIQUE   -> rgba("4a4a8a");
            case TREFLE  -> rgba("2e8a4a");
            case COEUR   -> rgba("c0283a");
            case CARREAU -> rgba("e0861e");
        };
    }

    /** @return l'ombre de {@link #accent(Card.Suit)}. */
    static int accentDark(Card.Suit suit) {
        return switch (suit) {
            case PIQUE   -> rgba("2e2e5a");
            case TREFLE  -> rgba("1e5a30");
            case COEUR   -> rgba("801a26");
            case CARREAU -> rgba("a85a12");
        };
    }

    /** @return la couleur du symbole de {@code suit} sur une carte (rouge ou noir). */
    static int ink(Card.Suit suit) {
        return suit == Card.Suit.COEUR || suit == Card.Suit.CARREAU ? rgba("d8202c") : rgba("1e1a2e");
    }

    /** Symbole de chaque couleur, 7 x 7 ('X' : plein). */
    private static String[] symbol(Card.Suit suit) {
        return switch (suit) {
            case COEUR -> new String[] {
                ".XX.XX.",
                "XXXXXXX",
                "XXXXXXX",
                "XXXXXXX",
                ".XXXXX.",
                "..XXX..",
                "...X..."};
            case CARREAU -> new String[] {
                "...X...",
                "..XXX..",
                ".XXXXX.",
                "XXXXXXX",
                ".XXXXX.",
                "..XXX..",
                "...X..."};
            case PIQUE -> new String[] {
                "...X...",
                "..XXX..",
                ".XXXXX.",
                "XXXXXXX",
                "XXXXXXX",
                "...X...",
                "..XXX.."};
            case TREFLE -> new String[] {
                "..XXX..",
                "..XXX..",
                "XX.X.XX",
                "XXXXXXX",
                "XX.X.XX",
                "...X...",
                "..XXX.."};
        };
    }

    /** Dessine le symbole de {@code suit} avec son coin haut gauche en ({@code x}, {@code y}), agrandi {@code scale} fois. */
    private static void stamp(PixelCanvas g, Card.Suit suit, int x, int y, int scale, int color) {
        String[] rows = symbol(suit);
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < rows[row].length(); col++) {
                if (rows[row].charAt(col) != 'X') continue;
                g.rect(x + col * scale, y + row * scale, x + col * scale + scale - 1, y + row * scale + scale - 1, color);
            }
        }
    }

    /** Petit symbole 3 x 3 (coins d'une carte). */
    private static void pip(PixelCanvas g, Card.Suit suit, int x, int y, int color) {
        switch (suit) {
            case COEUR -> { g.set(x, y, color); g.set(x + 2, y, color); g.rect(x, y + 1, x + 2, y + 1, color); g.set(x + 1, y + 2, color); }
            case CARREAU -> { g.set(x + 1, y, color); g.rect(x, y + 1, x + 2, y + 1, color); g.set(x + 1, y + 2, color); }
            case PIQUE -> { g.set(x + 1, y, color); g.rect(x, y + 1, x + 2, y + 1, color); g.set(x + 1, y + 2, color); g.set(x, y + 2, color); g.set(x + 2, y + 2, color); }
            case TREFLE -> { g.set(x + 1, y, color); g.set(x, y + 1, color); g.set(x + 2, y + 1, color); g.set(x + 1, y + 1, color); g.set(x + 1, y + 2, color); }
        }
    }

    // -------------------------------------------------------------------------
    // Ennemis
    // -------------------------------------------------------------------------

    /**
     * Soldat d'une couleur : un garde au corps de carte à jouer, frappé du grand
     * symbole de sa couleur, casque à plumet, et une lance à la main.
     */
    public static int[][] soldier(Card.Suit suit) {
        PixelCanvas g = new PixelCanvas(W, H);
        int accent = accent(suit), accentDark = accentDark(suit), ink = ink(suit);
        int steel = rgba("b8c0cc"), steelDark = rgba("7a8494"), wood = rgba("8a5a2a"), woodDark = rgba("5e3a18");

        // Corps de carte : papier, bordure à la couleur de l'uniforme, ombre à droite.
        g.rect(7, 19, 26, 41, accentDark);
        g.rect(8, 20, 25, 41, PAPER);
        g.rect(22, 20, 25, 41, PAPER_SHADE);
        stamp(g, suit, 10, 25, 2, ink);
        pip(g, suit, 9, 21, ink);
        pip(g, suit, 22, 37, ink);
        // Épaulettes à la couleur de l'uniforme.
        g.rect(4, 19, 8, 22, accent);
        g.rect(25, 19, 29, 22, accentDark);
        // Bras : manches et mains (la droite tient la lance).
        g.rect(4, 23, 6, 32, accent);
        g.rect(27, 23, 29, 28, accentDark);
        g.rect(4, 33, 6, 34, SKIN);
        g.rect(28, 28, 31, 30, SKIN);
        // Lance : hampe de bois et fer d'acier.
        g.rect(30, 6, 30, 41, wood);
        g.rect(31, 6, 31, 41, woodDark);
        g.triangle(28, 6, 33, 6, 30, 0, steel);
        g.set(31, 2, steelDark);
        g.set(31, 4, steelDark);
        g.rect(29, 7, 32, 7, accent);
        // Cou et visage.
        g.rect(15, 17, 19, 19, SKIN_SHADE);
        g.ellipseShaded(17f, 13f, 5.5f, 5f, SKIN, SKIN_SHADE, 2f);
        g.rect(14, 13, 15, 13, EYE);
        g.rect(19, 13, 20, 13, EYE);
        g.line(15, 16, 19, 16, rgba("8a3a2a"));
        // Casque d'acier, nasal et plumet.
        g.ellipseShaded(17f, 8.5f, 7f, 4.5f, steel, steelDark, 2f);
        g.rect(10, 9, 24, 10, steelDark);
        g.rect(17, 10, 17, 14, steelDark);
        g.ellipse(17f, 2.5f, 2f, 2.5f, accent);
        g.ellipse(18f, 1.5f, 1.5f, 1.5f, accentDark);
        return g.outlined();
    }

    /**
     * Roi d'une couleur : couronne d'or, longue barbe, manteau à la couleur de sa
     * couleur avec un col d'hermine, plastron frappé de son symbole et sceptre.
     */
    public static int[][] king(Card.Suit suit) {
        PixelCanvas g = new PixelCanvas(W, H);
        int accent = accent(suit), accentDark = accentDark(suit), ink = ink(suit);
        int beard = rgba("e8e4dc"), beardShade = rgba("b8b2a8"), gem = suit == Card.Suit.TREFLE ? rgba("3ad86a")
            : suit == Card.Suit.PIQUE ? rgba("6a7aff") : rgba("ff3a4a");

        // Manteau royal, épaules larges, ombré à droite.
        g.rect(3, 27, 30, 41, accent);
        g.rect(20, 27, 30, 41, accentDark);
        g.ellipseShaded(7f, 29f, 5f, 3.5f, accent, accentDark, 5f);
        g.ellipseShaded(27f, 29f, 5f, 3.5f, accentDark, accentDark, 0f);
        // Col d'hermine : blanc moucheté de noir.
        g.rect(6, 25, 28, 27, PAPER);
        for (int x = 8; x <= 26; x += 4) g.set(x, 26, EYE);
        // Plastron : une carte frappée du symbole.
        g.rect(11, 29, 23, 41, GOLD_DARK);
        g.rect(12, 30, 22, 41, PAPER);
        g.rect(20, 30, 22, 41, PAPER_SHADE);
        stamp(g, suit, 14, 32, 1, ink);
        // Cou, visage, yeux sévères.
        g.rect(14, 22, 20, 25, SKIN_SHADE);
        g.ellipseShaded(17f, 14f, 6.5f, 6.5f, SKIN, SKIN_SHADE, 2f);
        g.line(12, 12, 15, 13, EYE);
        g.line(19, 13, 22, 12, EYE);
        g.rect(13, 14, 14, 14, EYE);
        g.rect(20, 14, 21, 14, EYE);
        g.rect(16, 15, 18, 17, SKIN_SHADE);
        // Barbe et moustache.
        g.ellipseShaded(17f, 21f, 7f, 5f, beard, beardShade, 2f);
        g.triangle(12, 22, 22, 22, 17, 29, beard);
        g.triangle(17, 22, 22, 22, 17, 29, beardShade);
        g.line(13, 18, 16, 19, beardShade);
        g.line(18, 19, 21, 18, beardShade);
        g.line(15, 20, 19, 20, rgba("8a3a2a"));
        // Couronne : bandeau, pointes et pierres.
        g.rect(10, 6, 24, 9, GOLD);
        g.rect(10, 9, 24, 9, GOLD_DARK);
        for (int x : new int[] {10, 14, 17, 20, 24}) {
            g.rect(x, 3, x, 5, GOLD);
            g.set(x, 2, GOLD_DARK);
        }
        g.rect(12, 4, 12, 5, GOLD);
        g.rect(22, 4, 22, 5, GOLD);
        g.set(13, 7, gem);
        g.set(17, 7, rgba("ffffff"));
        g.set(21, 7, gem);
        // Sceptre d'or surmonté du symbole.
        g.rect(29, 14, 30, 40, GOLD);
        g.rect(30, 14, 30, 40, GOLD_DARK);
        g.rect(27, 31, 31, 33, SKIN);
        PixelCanvas orb = new PixelCanvas(7, 7);
        stamp(orb, suit, 0, 0, 1, accent);
        for (int y = 0; y < 7; y++) for (int x = 0; x < 7; x++) if (orb.get(x, y) != 0) g.set(26 + x, 6 + y, GOLD);
        g.set(29, 9, gem);
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Coffre au trésor
    // -------------------------------------------------------------------------

    private static final int CHEST_W = 34, CHEST_H = 30;

    /** Coffre au trésor fermé : bois sombre, ferrures dorées et serrure. */
    public static int[][] chestClosed() {
        PixelCanvas g = new PixelCanvas(CHEST_W, CHEST_H);
        chestBody(g);
        int wood = rgba("9a5428"), woodLight = rgba("b86a34"), woodDark = rgba("6a3416");
        // Couvercle bombé.
        g.ellipseShaded(16.5f, 13f, 14.5f, 8f, wood, woodDark, 6f);
        g.rect(2, 13, 31, 15, wood);
        g.rect(25, 13, 31, 15, woodDark);
        for (int x = 6; x <= 22; x += 5) g.line(x, 7, x, 15, woodLight);
        // Ferrures du couvercle.
        g.rect(6, 5, 8, 15, GOLD);
        g.rect(25, 5, 27, 15, GOLD_DARK);
        g.rect(2, 15, 31, 16, GOLD);
        g.rect(25, 15, 31, 16, GOLD_DARK);
        // Serrure.
        g.rect(14, 14, 19, 20, GOLD);
        g.rect(18, 14, 19, 20, GOLD_DARK);
        g.rect(16, 16, 17, 17, EYE);
        g.set(16, 18, EYE);
        return g.outlined();
    }

    /** Coffre au trésor ouvert : le couvercle rabattu en arrière et l'or qui brille dedans. */
    public static int[][] chestOpen() {
        PixelCanvas g = new PixelCanvas(CHEST_W, CHEST_H);
        int woodDark = rgba("6a3416"), inside = rgba("3a1a0c");
        // Couvercle ouvert, vu de l'intérieur.
        g.rect(3, 1, 30, 11, woodDark);
        g.rect(4, 2, 29, 10, inside);
        g.rect(6, 1, 8, 11, GOLD_DARK);
        g.rect(25, 1, 27, 11, GOLD_DARK);
        chestBody(g);
        // Or qui déborde : pièces et éclats.
        g.ellipse(16.5f, 15f, 13f, 3.5f, GOLD);
        g.ellipse(12f, 13.5f, 4f, 2f, rgba("ffe98a"));
        g.ellipse(22f, 14f, 3.5f, 2f, rgba("ffe98a"));
        for (int x = 6; x <= 27; x += 3) g.set(x, 15 + (x % 2), GOLD_DARK);
        g.set(9, 12, rgba("ffffff"));
        g.set(24, 12, rgba("ffffff"));
        g.set(17, 11, rgba("ff3a4a"));
        return g.outlined();
    }

    /** Caisse du coffre (sous le couvercle), commune aux deux dessins. */
    private static void chestBody(PixelCanvas g) {
        int wood = rgba("9a5428"), woodLight = rgba("b86a34"), woodDark = rgba("6a3416");
        g.rect(2, 16, 31, 28, wood);
        g.rect(25, 16, 31, 28, woodDark);
        for (int y = 19; y <= 26; y += 4) g.line(3, y, 24, y, woodLight);
        g.rect(6, 16, 8, 28, GOLD);
        g.rect(25, 16, 27, 28, GOLD_DARK);
        g.rect(2, 27, 31, 28, GOLD_DARK);
    }

    // -------------------------------------------------------------------------
    // Carte de la prairie
    // -------------------------------------------------------------------------

    /**
     * Positions des entrées des donjons sur la carte de la prairie, en pixels de
     * la carte (centre de l'entrée, y vers le bas), dans l'ordre Pique, Trèfle,
     * Cœur, Carreau.
     */
    public static final int[][] GATES = {{30, 30}, {74, 22}, {120, 32}, {134, 70}};

    /** @return les positions des entrées des donjons du lieu {@code place}, dans l'ordre de ses donjons. */
    public static int[][] gates(fr.astratime.lucky.entities.exploration.Place place) {
        return switch (place) {
            case PRAIRIE -> GATES;
            case PORT    -> PlacesArt.PORT_GATES;
            case MINES   -> PlacesArt.MINES_GATES;
            case CASINO  -> PlacesArt.CASINO_GATES;
        };
    }

    /** Départ du chemin, en bas de la carte. */
    private static final int[] PATH_START = {70, 95};

    /**
     * La prairie vue du ciel : herbe, collines, un étang, des arbres, des fleurs,
     * et un chemin de terre qui part du bas et mène à chacun des quatre donjons.
     */
    public static int[][] prairie() {
        PixelCanvas g = new PixelCanvas(MAP_WIDTH, MAP_HEIGHT);
        Random random = new Random(11);
        int grass = rgba("5aa83a"), grassDark = rgba("4a9232"), grassLight = rgba("70be4a");
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                int roll = random.nextInt(14);
                g.set(x, y, roll == 0 ? grassDark : roll == 1 ? grassLight : grass);
            }
        }
        // Collines douces.
        int hill = rgba("4e9a34"), hillLight = rgba("66b244");
        float[][] hills = {{18, 14, 22, 12}, {92, 10, 26, 10}, {150, 40, 18, 14}, {10, 78, 18, 12}};
        for (float[] h : hills) {
            g.ellipse(h[0], h[1], h[2], h[3], hill);
            g.ellipse(h[0] - h[2] * 0.25f, h[1] - h[3] * 0.3f, h[2] * 0.55f, h[3] * 0.45f, hillLight);
        }
        // Étang.
        g.ellipse(46f, 64f, 13f, 7f, rgba("c8b47a"));
        g.ellipse(46f, 64f, 12f, 6f, rgba("3a7ad0"));
        g.ellipse(43f, 62f, 6f, 2.5f, rgba("5a9ae8"));
        g.line(40, 61, 44, 61, rgba("a8d4ff"));
        for (int[] lily : new int[][] {{50, 66}, {53, 63}}) {
            g.set(lily[0], lily[1], rgba("2e8a3a"));
            g.set(lily[0] + 1, lily[1], rgba("2e8a3a"));
            g.set(lily[0], lily[1] - 1, rgba("ff9ad0"));
        }
        // Chemins de terre, du bas de la carte vers chaque donjon.
        int dirt = rgba("c8a46a"), dirtDark = rgba("a8844e");
        int[] fork = {84, 56};
        path(g, PATH_START[0], PATH_START[1], fork[0], fork[1], dirt, dirtDark);
        for (int[] gate : GATES) path(g, fork[0], fork[1], gate[0], gate[1] + 6, dirt, dirtDark);
        // Arbres (feuillage rond, tronc), en évitant les chemins.
        int trunk = rgba("6a4020"), leaf = rgba("2e7a2a"), leafDark = rgba("1e5a1e"), leafLight = rgba("4a9a3a");
        int[][] trees = {{8, 44}, {14, 52}, {56, 36}, {100, 52}, {108, 62}, {150, 14}, {142, 88}, {24, 88},
            {120, 86}, {62, 10}, {4, 22}, {96, 82}, {154, 58}, {38, 46}, {20, 64}, {30, 72}, {112, 8},
            {136, 46}, {156, 76}, {84, 34}, {6, 90}, {128, 52}, {50, 84}, {44, 8}, {148, 28}};
        for (int[] t : trees) {
            if (isOnPath(g, t[0], t[1], dirt, dirtDark)) continue;
            g.rect(t[0], t[1] + 2, t[0] + 1, t[1] + 4, trunk);
            g.ellipseShaded(t[0] + 0.5f, t[1], 4f, 3.5f, leaf, leafDark, 1f);
            g.ellipse(t[0] - 1f, t[1] - 1f, 1.5f, 1.2f, leafLight);
        }
        // Fleurs.
        int[] petals = {rgba("ff5a6a"), rgba("ffe14a"), rgba("ffffff"), rgba("c88aff")};
        for (int i = 0; i < 90; i++) {
            int x = random.nextInt(MAP_WIDTH), y = random.nextInt(MAP_HEIGHT);
            if (isOnPath(g, x, y, dirt, dirtDark)) continue;
            int color = g.get(x, y);
            if (color != grass && color != grassDark && color != grassLight && color != hill && color != hillLight) continue;
            g.set(x, y, petals[random.nextInt(petals.length)]);
        }
        return g.pixels;
    }

    /**
     * Chemin de terre de 3 pixels de large, bordé d'une ombre, en courbe de
     * ({@code x0}, {@code y0}) à ({@code x1}, {@code y1}).
     */
    static void path(PixelCanvas g, int x0, int y0, int x1, int y1, int dirt, int dirtDark) {
        // Point de contrôle : le milieu, poussé sur le côté, pour un chemin qui serpente.
        float mx = (x0 + x1) / 2f, my = (y0 + y1) / 2f;
        float nx = -(y1 - y0), ny = x1 - x0;
        float length = (float) Math.max(1.0, Math.hypot(nx, ny));
        float cx = mx + nx / length * 8f, cy = my + ny / length * 8f;
        int steps = 2 * Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0)) + 1;
        for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i <= steps; i++) {
                float t = i / (float) steps, u = 1f - t;
                int x = Math.round(u * u * x0 + 2 * u * t * cx + t * t * x1);
                int y = Math.round(u * u * y0 + 2 * u * t * cy + t * t * y1);
                if (pass == 0) g.rect(x - 2, y - 1, x + 2, y + 2, dirtDark);
                else g.rect(x - 1, y - 1, x + 1, y + 1, dirt);
            }
        }
    }

    static boolean isOnPath(PixelCanvas g, int x, int y, int dirt, int dirtDark) {
        for (int dy = -4; dy <= 5; dy++) {
            for (int dx = -4; dx <= 5; dx++) {
                int color = g.get(x + dx, y + dy);
                if (color == dirt || color == dirtDark) return true;
            }
        }
        return false;
    }

    /**
     * Entrée d'un donjon : une arche de pierre dans une butte, une porte sombre,
     * et au-dessus un écusson à la couleur du donjon frappé de son symbole.
     */
    public static int[][] gate(Card.Suit suit) {
        PixelCanvas g = new PixelCanvas(GATE_SIZE, GATE_SIZE);
        int stone = rgba("9a9aa6"), stoneDark = rgba("6a6a78"), stoneLight = rgba("c0c0ca");
        int door = rgba("1a1218"), doorLight = rgba("3a2a2e");
        // Butte de pierre.
        g.ellipseShaded(13f, 17f, 12f, 9f, stone, stoneDark, 3f);
        g.rect(1, 17, 25, 25, stone);
        g.rect(17, 17, 25, 25, stoneDark);
        for (int[] brick : new int[][] {{4, 20}, {9, 23}, {19, 21}, {21, 24}, {6, 15}, {20, 14}}) {
            g.line(brick[0], brick[1], brick[0] + 2, brick[1], stoneLight);
        }
        // Porte en arche.
        g.ellipse(13f, 19f, 5f, 5f, door);
        g.rect(8, 19, 18, 25, door);
        g.rect(9, 20, 9, 25, doorLight);
        // Écusson de la couleur, au-dessus de la porte.
        int accent = accent(suit), accentDark = accentDark(suit);
        g.rect(8, 2, 18, 10, accentDark);
        g.rect(9, 3, 17, 9, accent);
        g.triangle(8, 10, 18, 10, 13, 14, accentDark);
        g.triangle(9, 10, 17, 10, 13, 13, accent);
        stamp(g, suit, 10, 4, 1, rgba("ffffff"));
        return g.outlined();
    }
}
