package fr.astratime.lucky.assets;

import fr.astratime.lucky.entities.exploration.Dungeon;

import java.util.Random;

import static fr.astratime.lucky.assets.PixelCanvas.rgba;

/**
 * Dessins en pixel art du Manoir hanté : la carte du lieu (le manoir du baron
 * sur sa colline, sous la pleine lune, son cimetière), le décor de l'entrée de
 * ses donjons (une porte gothique), leurs emblèmes, et les symboles des
 * rouleaux de ses ennemis (16 x 16). Les portraits sont dans {@link ManorPortraits}.
 */
public final class ManorArt {

    private static final int MW = ExplorationArt.MAP_WIDTH, MH = ExplorationArt.MAP_HEIGHT;
    private static final int GOLD = rgba("ffd54a"), GOLD_DARK = rgba("c4891e"), WHITE = rgba("f6f0e2");
    private static final int BLOOD = rgba("b0101e"), BLOOD_DARK = rgba("6a0a14"), BLOOD_LIGHT = rgba("e83a4a");
    private static final int STONE = rgba("5a5a6e"), STONE_DARK = rgba("3a3a4a"), STONE_LIGHT = rgba("8a8aa0");
    private static final int GHOST = rgba("c8f0e8"), GHOST_SHADE = rgba("7ab8b0");
    private static final int WOOD = rgba("6a4a2e"), WOOD_DARK = rgba("3e2a18");
    private static final int NIGHT = rgba("140e24"), VIOLET = rgba("7a4ab8"), CANDLE = rgba("ffd27a");

    /** Entrées des donjons du Manoir (Salon de jeu, Bibliothèque, Crypte, Grenier), centre de l'entrée. */
    static final int[][] MANOIR_GATES = {{50, 46}, {110, 46}, {22, 70}, {80, 20}};

    private ManorArt() {}

    // -------------------------------------------------------------------------
    // Symboles des rouleaux ennemis
    // -------------------------------------------------------------------------

    /** Pacte de sang : une coupe d'argent pleine de sang, une goutte qui en déborde. */
    static int[][] bloodPact() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int silver = rgba("dce4ee"), silverDark = rgba("8a97a8");
        g.ellipseShaded(8f, 4f, 6f, 2f, silver, silverDark, 1f);
        g.ellipse(8f, 4f, 4.8f, 1.2f, BLOOD);
        for (int y = 4; y <= 8; y++) {
            int half = 6 - (y - 4);
            g.rect(8 - half, y, 8 + half - 1, y, y < 6 ? BLOOD : silver);
            g.set(8 + half - 1, y, silverDark);
        }
        g.rect(7, 9, 8, 12, silverDark);
        g.rect(4, 13, 11, 14, silver);
        g.rect(9, 13, 11, 14, silverDark);
        g.set(5, 4, BLOOD_LIGHT);
        g.rect(13, 5, 13, 7, BLOOD);                  // la goutte qui coule
        g.set(13, 8, BLOOD_DARK);
        return g.outlined();
    }

    /** Malédiction : un grimoire noir fermé par une chaîne, un oeil violet sur la couverture. */
    static int[][] curse() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int cover = rgba("2a1a3a"), coverLight = rgba("4a2e60"), page = rgba("e8dcc0");
        g.rect(2, 2, 13, 14, cover);
        g.rect(2, 2, 3, 14, coverLight);              // le dos
        g.rect(13, 3, 14, 13, page);                  // la tranche des pages
        g.ellipse(8.5f, 8f, 3.2f, 2.2f, page);        // l'oeil
        g.ellipse(8.5f, 8f, 1.4f, 1.4f, VIOLET);
        g.set(8, 8, NIGHT);
        g.line(2, 5, 14, 11, STONE_LIGHT);            // la chaîne
        g.set(6, 7, STONE); g.set(10, 9, STONE);
        g.rect(12, 9, 13, 11, GOLD_DARK);             // le cadenas
        return g.outlined();
    }

    /** Linceul : un drap blanc qui flotte, deux trous noirs pour les yeux, le bas en lambeaux. */
    static int[][] shroud() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.ellipseShaded(8f, 6f, 5.5f, 5f, GHOST, GHOST_SHADE, 2f);
        g.rect(3, 6, 13, 12, GHOST);
        g.rect(10, 6, 13, 12, GHOST_SHADE);
        for (int x = 3; x <= 13; x += 2) g.set(x, 13, x % 4 == 1 ? GHOST : GHOST_SHADE); // les lambeaux
        g.set(4, 14, GHOST); g.set(8, 14, GHOST); g.set(12, 14, GHOST_SHADE);
        g.rect(5, 5, 6, 7, NIGHT);                    // les yeux
        g.rect(10, 5, 11, 7, NIGHT);
        g.ellipse(8f, 10f, 1.2f, 1f, NIGHT);          // la bouche ouverte
        return g.outlined();
    }

    /** Fils : la croix de bois d'un marionnettiste, trois fils qui pendent, une petite main au bout. */
    static int[][] strings() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.rect(1, 2, 14, 3, WOOD);
        g.rect(7, 0, 8, 7, WOOD);
        g.rect(1, 3, 14, 3, WOOD_DARK);
        int thread = rgba("e8e0f0");
        g.line(2, 4, 3, 12, thread);
        g.line(13, 4, 12, 12, thread);
        g.line(8, 8, 8, 11, thread);
        g.rect(2, 12, 4, 14, rgba("f0c8a0"));         // les mains de bois au bout des fils
        g.rect(11, 12, 13, 14, rgba("f0c8a0"));
        g.ellipse(8f, 13f, 1.6f, 1.6f, rgba("f0c8a0")); // la tête
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Entrée des donjons et emblèmes
    // -------------------------------------------------------------------------

    /**
     * Dessine dans {@code g} (26 x 26) une porte gothique de pierre grise, son arc brisé, deux lanternes.
     * @return les couleurs de l'écusson (clair, sombre)
     */
    static int[] gateDecor(PixelCanvas g) {
        g.rect(0, 10, 25, 25, STONE);
        g.rect(18, 10, 25, 25, STONE_DARK);
        g.triangle(0, 11, 25, 11, 13, 1, STONE_DARK);
        g.triangle(3, 11, 22, 11, 13, 4, STONE);
        for (int y = 13; y <= 25; y += 4) for (int x = (y / 4) % 2 * 3; x <= 25; x += 6) g.line(x, y, x + 4, y, STONE_DARK);
        g.rect(1, 13, 2, 16, CANDLE);
        g.rect(23, 13, 24, 16, CANDLE);
        g.set(13, 6, VIOLET);
        return new int[] {rgba("5a2a7a"), rgba("3a1a52")};
    }

    /** Emblème 7 x 7 des donjons du Manoir ('X' : plein), ou {@code null}. */
    static String[] emblem(Dungeon dungeon) {
        return switch (dungeon) {
            case SALON        -> new String[] {"XXXXXXX", "X.....X", ".X...X.", "..XXX..", "...X...", "...X...", "..XXX.."}; // coupe
            case BIBLIOTHEQUE -> new String[] {"XX.XX..", "XX.XX.X", "XX.XXXX", "XX.XXXX", "XX.XXXX", "XX.XX.X", "XXXXXXX"}; // livres
            case CRYPTE       -> new String[] {"..XXX..", ".XXXXX.", ".XX.XX.", ".X...X.", ".XX.XX.", ".XXXXX.", "XXXXXXX"}; // pierre tombale
            case GRENIER      -> new String[] {"XXXXXXX", "...X...", "X..X..X", "X.XXX.X", "X.XXX.X", "...X...", "..X.X.."}; // marionnette
            default           -> null;
        };
    }

    // -------------------------------------------------------------------------
    // Carte du lieu
    // -------------------------------------------------------------------------

    /**
     * Le Manoir hanté : une colline sous la pleine lune, le manoir du baron, ses
     * fenêtres allumées et son grenier pointu, un cimetière à gauche, un arbre
     * mort à droite, des chauves-souris ; un chemin monte jusqu'à la grille.
     */
    static int[][] manoir() {
        PixelCanvas g = new PixelCanvas(MW, MH);
        Random random = new Random(77);
        int grass = rgba("1e2a22"), grassDark = rgba("141e18"), grassLight = rgba("28362c");
        for (int y = 0; y < MH; y++) {
            for (int x = 0; x < MW; x++) {
                int roll = random.nextInt(12);
                g.set(x, y, roll == 0 ? grassDark : roll == 1 ? grassLight : grass);
            }
        }
        // Le ciel violet de nuit, la pleine lune, les étoiles.
        for (int y = 0; y < 30; y++) for (int x = 0; x < MW; x++) g.set(x, y, y < 12 ? NIGHT : rgba("1e1436"));
        for (int i = 0; i < 24; i++) g.set(random.nextInt(MW), random.nextInt(20), rgba("e8e0ff"));
        g.ellipse(138f, 9f, 6f, 6f, rgba("fff6d0"));
        g.ellipse(136f, 8f, 1.5f, 1.2f, rgba("e8dca8"));
        // La colline.
        for (int x = 0; x < MW; x++) {
            int top = 26 + (int) Math.round(6 * Math.abs((x - 80) / 80.0) * 2);
            for (int y = top; y < 34; y++) g.set(x, y, rgba("1a2420"));
        }
        // Le manoir : corps de pierre, deux ailes, toits d'ardoise, le grenier pointu au milieu.
        int slate = rgba("2a2236"), slateLight = rgba("3e3450");
        g.rect(34, 30, 126, 56, STONE);
        g.rect(96, 30, 126, 56, STONE_DARK);
        g.triangle(30, 31, 64, 31, 47, 20, slate);
        g.triangle(96, 31, 130, 31, 113, 20, slate);
        g.rect(62, 22, 98, 31, STONE);
        g.triangle(58, 23, 102, 23, 80, 4, slate);
        g.triangle(64, 23, 96, 23, 80, 8, slateLight);
        g.rect(77, 12, 83, 18, CANDLE);                       // la lucarne du grenier
        g.line(80, 12, 80, 18, WOOD_DARK);
        g.rect(118, 14, 121, 22, STONE_DARK);                 // une cheminée
        // Les fenêtres, certaines allumées, d'autres noires.
        for (int i = 0; i < 6; i++) {
            int x = 40 + i * 14;
            boolean lit = i % 3 != 1;
            g.rect(x, 34, x + 4, 40, lit ? CANDLE : NIGHT);
            g.ellipse(x + 2f, 34f, 2.5f, 1.5f, lit ? CANDLE : NIGHT);
        }
        g.rect(76, 42, 84, 56, WOOD_DARK);                    // la grande porte
        g.ellipse(80f, 42f, 4.5f, 3f, WOOD_DARK);
        g.set(82, 50, GOLD);
        // Une silhouette blanche à une fenêtre.
        g.rect(97, 35, 98, 39, GHOST);
        // Le cimetière, à gauche : tombes et croix.
        for (int i = 0; i < 5; i++) {
            int x = 6 + i * 9 + (i % 2) * 2, y = 74 + (i % 2) * 6;
            if (i % 2 == 0) {
                g.rect(x, y, x + 4, y + 6, STONE_LIGHT);
                g.ellipse(x + 2f, y, 2.5f, 2f, STONE_LIGHT);
            } else {
                g.rect(x + 2, y - 2, x + 2, y + 6, STONE_LIGHT);
                g.rect(x, y, x + 4, y, STONE_LIGHT);
            }
        }
        // L'arbre mort, à droite.
        g.rect(140, 54, 142, 80, WOOD_DARK);
        g.line(141, 62, 150, 52, WOOD_DARK);
        g.line(141, 66, 132, 58, WOOD_DARK);
        g.line(147, 55, 152, 57, WOOD_DARK);
        // Des chauves-souris devant la lune.
        for (int[] bat : new int[][] {{126, 14}, {148, 18}, {110, 8}}) {
            g.set(bat[0], bat[1], NIGHT);
            g.line(bat[0] - 2, bat[1] - 1, bat[0] + 2, bat[1] - 1, NIGHT);
        }
        // Le chemin de terre, de la grille jusqu'à chaque entrée.
        int dirt = rgba("6a5a48"), dirtDark = rgba("4a3e30");
        int[] fork = {80, 76};
        ExplorationArt.path(g, 80, 95, fork[0], fork[1], dirt, dirtDark);
        for (int[] gate : MANOIR_GATES) ExplorationArt.path(g, fork[0], fork[1], gate[0], gate[1] + 12, dirt, dirtDark);
        // La grille de fer forgé, au bas de la carte.
        for (int x = 56; x <= 104; x += 3) {
            if (Math.abs(x - 80) < 6) continue;
            g.rect(x, 84, x, 92, rgba("1a1a22"));
            g.set(x, 83, STONE_LIGHT);
        }
        g.line(56, 86, 74, 86, rgba("1a1a22"));
        g.line(86, 86, 104, 86, rgba("1a1a22"));
        return g.pixels;
    }
}
