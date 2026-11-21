package fr.astratime.lucky.assets;

import fr.astratime.lucky.entities.exploration.Dungeon;

import java.util.Random;

import static fr.astratime.lucky.assets.PixelCanvas.rgba;

/**
 * Dessins en pixel art du Palace des Neiges : la carte du lieu (le grand hôtel pris
 * dans la neige, au pied de la montagne), le décor de l'entrée de ses donjons (une
 * porte tambour givrée), leurs emblèmes, et les symboles des rouleaux de ses ennemis
 * (16 x 16). Les portraits sont dans {@link PalacePortraits}.
 */
public final class PalaceArt {

    private static final int MW = ExplorationArt.MAP_WIDTH, MH = ExplorationArt.MAP_HEIGHT;
    private static final int GOLD = rgba("ffd54a"), GOLD_DARK = rgba("c4891e"), WHITE = rgba("ffffff");
    private static final int ICE = rgba("bfe8ff"), ICE_DARK = rgba("6aa8d8"), ICE_DEEP = rgba("2a5a8a");
    private static final int SNOW = rgba("f2f8ff"), SNOW_SHADE = rgba("c8d8ec");
    private static final int STEEL = rgba("dce4ee"), STEEL_DARK = rgba("8a97a8");
    private static final int WINE = rgba("7a1a3a"), STONE = rgba("e8dcc8"), STONE_DARK = rgba("b8a890");

    /** Entrées des donjons du Palace (Hall, Patinoire, Téléphérique, Suite royale), centre de l'entrée. */
    static final int[][] PALACE_GATES = {{80, 62}, {28, 58}, {136, 40}, {80, 30}};

    private PalaceArt() {}

    // -------------------------------------------------------------------------
    // Symboles des rouleaux ennemis
    // -------------------------------------------------------------------------

    /** Clé : une grande clé d'hôtel en laiton, avec son pompon rouge. */
    static int[][] key() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.ellipseShaded(4.5f, 4.5f, 3.6f, 3.6f, GOLD, GOLD_DARK, 1.5f);
        g.ellipse(4.5f, 4.5f, 1.4f, 1.4f, 0);
        g.line(6, 6, 13, 13, GOLD);
        g.line(7, 6, 14, 13, GOLD_DARK);
        g.rect(11, 13, 12, 15, GOLD_DARK);  // le panneton
        g.rect(13, 11, 15, 12, GOLD);
        g.set(1, 9, rgba("d8202c")); g.set(1, 10, rgba("d8202c")); g.set(2, 11, rgba("8a1a1a"));
        g.set(2, 8, rgba("d8202c"));
        return g.outlined();
    }

    /** Gel : un cristal de glace à six branches. */
    static int[][] freeze() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.line(8, 1, 8, 14, ICE);
        g.line(2, 4, 14, 11, ICE);
        g.line(2, 11, 14, 4, ICE);
        g.line(6, 2, 8, 4, WHITE); g.line(10, 2, 8, 4, WHITE);
        g.line(6, 13, 8, 11, WHITE); g.line(10, 13, 8, 11, WHITE);
        g.set(3, 6, WHITE); g.set(13, 6, WHITE); g.set(3, 9, WHITE); g.set(13, 9, WHITE);
        g.ellipse(8f, 7.5f, 1.6f, 1.6f, ICE_DARK);
        g.set(8, 7, WHITE);
        return g.outlined();
    }

    /** Avalanche : une boule de neige qui dévale une pente, des flocons derrière elle. */
    static int[][] avalanche() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.triangle(0, 15, 15, 15, 0, 6, STONE_DARK);
        g.triangle(0, 15, 15, 15, 0, 9, SNOW_SHADE);
        g.ellipseShaded(10f, 9f, 4.6f, 4.6f, SNOW, SNOW_SHADE, 2f);
        g.set(8, 7, WHITE); g.set(9, 6, WHITE);
        g.set(2, 2, SNOW); g.set(4, 4, SNOW); g.set(1, 5, SNOW); g.set(5, 1, SNOW);
        return g.outlined();
    }

    /** Champagne glacé : une coupe de champagne givrée, ses bulles qui montent. */
    static int[][] champagne() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int wine = rgba("ffe08a"), wineDark = rgba("d8b050");
        g.triangle(2, 3, 13, 3, 8, 9, wine);
        g.triangle(8, 3, 13, 3, 8, 9, wineDark);
        g.line(2, 3, 13, 3, ICE);
        g.rect(7, 9, 8, 12, ICE);
        g.rect(4, 13, 11, 14, ICE_DARK);
        g.set(6, 0, WHITE); g.set(9, 1, WHITE); g.set(7, 2, WHITE);
        g.set(3, 4, WHITE); g.set(12, 4, WHITE);
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Entrée des donjons et emblèmes
    // -------------------------------------------------------------------------

    /**
     * Dessine dans {@code g} (26 x 26) la porte d'un grand hôtel : façade de pierre crème, auvent bordeaux,
     * porte tambour de verre givré, neige sur l'auvent. @return les couleurs de l'écusson (clair, sombre)
     */
    static int[] gateDecor(PixelCanvas g) {
        g.rect(0, 8, 25, 25, STONE);
        g.rect(19, 8, 25, 25, STONE_DARK);
        g.rect(0, 8, 25, 11, WINE);            // l'auvent
        g.rect(0, 7, 25, 7, SNOW);             // la neige dessus
        for (int x = 1; x < 26; x += 4) g.set(x, 12, WINE);
        g.rect(7, 13, 18, 25, ICE_DARK);       // la porte tambour
        g.rect(8, 14, 12, 25, ICE);
        g.line(12, 13, 12, 25, GOLD_DARK);
        g.rect(0, 24, 25, 25, SNOW_SHADE);
        return new int[] {rgba("8ac8f0"), rgba("2a5a8a")};
    }

    /** Emblème 7 x 7 des donjons du Palace ('X' : plein), ou {@code null}. */
    static String[] emblem(Dungeon dungeon) {
        return switch (dungeon) {
            case HALL         -> new String[] {"...X...", "..XXX..", ".X...X.", "X.....X", "XXXXXXX", ".......", "XXXXXXX"}; // sonnette
            case PATINOIRE    -> new String[] {".X.....", ".X.....", ".X.....", ".XXXXX.", ".XXXXXX", "......X", "XXXXXXX"}; // patin
            case TELEPHERIQUE -> new String[] {"XXXXXXX", "...X...", "..XXX..", ".XXXXX.", ".X.X.X.", ".XXXXX.", "......."}; // cabine
            case SUITE_ROYALE -> new String[] {"X.X.X.X", "XXXXXXX", "XXXXXXX", ".......", "X.....X", ".XXXXX.", "...X..."}; // couronne et coupe
            default           -> null;
        };
    }

    // -------------------------------------------------------------------------
    // Carte du lieu
    // -------------------------------------------------------------------------

    /**
     * Le Palace des Neiges : la montagne enneigée sous un ciel d'hiver, le grand hôtel aux fenêtres allumées
     * au centre, la patinoire gelée à gauche, le câble du téléphérique qui monte à droite vers le sommet.
     */
    static int[][] palace() {
        PixelCanvas g = new PixelCanvas(MW, MH);
        Random random = new Random(53);
        for (int y = 0; y < MH; y++) {
            for (int x = 0; x < MW; x++) {
                int roll = random.nextInt(14);
                g.set(x, y, roll == 0 ? SNOW_SHADE : roll == 1 ? WHITE : SNOW);
            }
        }
        // Le ciel d'hiver, puis la montagne blanche.
        for (int y = 0; y < 22; y++) for (int x = 0; x < MW; x++) g.set(x, y, y < 10 ? rgba("5a7aa8") : rgba("7a9ac4"));
        g.triangle(96, 30, 160, 30, 140, 2, rgba("8aa0bc"));
        g.triangle(120, 12, 152, 12, 140, 2, SNOW);
        g.triangle(0, 30, 70, 30, 30, 8, rgba("9ab0ca"));
        g.triangle(18, 16, 42, 16, 30, 8, SNOW);
        for (int x = 0; x < MW; x++) {
            int top = 24 + (int) Math.round(2 * Math.sin(x / 9.0));
            for (int y = top; y < 30; y++) g.set(x, y, SNOW_SHADE);
        }
        // Le téléphérique : câble du palace jusqu'au sommet, et sa cabine rouge.
        g.line(104, 40, 140, 6, STEEL_DARK);
        g.rect(124, 18, 128, 22, rgba("d8202c"));
        g.line(126, 16, 126, 18, STEEL_DARK);
        g.rect(132, 34, 140, 44, STONE_DARK);  // la gare du téléphérique
        g.rect(131, 33, 141, 33, SNOW);
        // Le grand hôtel : façade crème, toits verts couverts de neige, rangées de fenêtres allumées.
        g.rect(54, 26, 106, 58, STONE);
        g.rect(92, 26, 106, 58, STONE_DARK);
        g.rect(66, 14, 94, 26, STONE);
        g.triangle(52, 26, 108, 26, 80, 16, rgba("2a5a4a"));
        g.triangle(64, 14, 96, 14, 80, 6, rgba("2a5a4a"));
        g.line(52, 26, 80, 16, SNOW); g.line(80, 16, 108, 26, SNOW);
        g.line(64, 14, 80, 6, SNOW); g.line(80, 6, 96, 14, SNOW);
        int window = rgba("ffd27a");
        for (int y = 30; y <= 50; y += 7) for (int x = 58; x <= 102; x += 6) g.rect(x, y, x + 2, y + 3, window);
        for (int x = 70; x <= 90; x += 6) g.rect(x, 18, x + 2, 21, window);
        g.rect(74, 52, 86, 58, WINE);           // l'auvent de l'entrée
        g.rect(77, 54, 83, 58, ICE_DARK);
        // La patinoire gelée, à gauche, et ses sapins.
        g.ellipse(28f, 52f, 20f, 8f, ICE);
        g.ellipse(24f, 50f, 9f, 3f, rgba("e0f4ff"));
        for (int[] tree : new int[][] {{8, 40}, {48, 40}, {14, 68}, {146, 60}, {120, 72}}) {
            g.triangle(tree[0] - 5, tree[1] + 8, tree[0] + 5, tree[1] + 8, tree[0], tree[1] - 4, rgba("1e4a3a"));
            g.line(tree[0] - 3, tree[1] + 3, tree[0], tree[1] - 2, SNOW);
        }
        // Un chemin damé du bas de la carte jusqu'à l'hôtel, puis vers chaque entrée.
        int path = rgba("b8c4d4"), pathDark = rgba("98a4b8");
        int[] fork = {80, 72};
        ExplorationArt.path(g, 80, 95, fork[0], fork[1], path, pathDark);
        for (int[] gate : PALACE_GATES) ExplorationArt.path(g, fork[0], fork[1], gate[0], gate[1] + 6, path, pathDark);
        // De la neige qui tombe.
        for (int i = 0; i < 40; i++) g.set(random.nextInt(MW), random.nextInt(MH), WHITE);
        return g.pixels;
    }
}
