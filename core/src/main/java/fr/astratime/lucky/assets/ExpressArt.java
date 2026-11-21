package fr.astratime.lucky.assets;

import fr.astratime.lucky.entities.exploration.Dungeon;

import java.util.Random;

import static fr.astratime.lucky.assets.PixelCanvas.rgba;

/**
 * Dessins en pixel art du Rapide de Minuit : la carte du lieu (le train de nuit
 * dans la campagne), le décor de l'entrée de ses donjons (une porte de wagon),
 * leurs emblèmes, et les symboles des rouleaux de ses ennemis (16 x 16). Les
 * portraits sont dans {@link ExpressPortraits}.
 */
public final class ExpressArt {

    private static final int MW = ExplorationArt.MAP_WIDTH, MH = ExplorationArt.MAP_HEIGHT;
    private static final int GOLD = rgba("ffd54a"), GOLD_DARK = rgba("c4891e"), WHITE = rgba("ffffff");
    private static final int RED = rgba("d8202c"), RED_DARK = rgba("8a1a1a");
    private static final int COAL = rgba("26262e"), COAL_LIGHT = rgba("4a4a58");
    private static final int STEEL = rgba("dce4ee"), STEEL_DARK = rgba("8a97a8");

    /** Entrées des donjons du Rapide (Locomotive, Wagon-restaurant, Wagon-lit, Fourgon), centre de l'entrée. */
    static final int[][] RAPIDE_GATES = {{134, 40}, {98, 40}, {62, 40}, {26, 40}};

    private ExpressArt() {}

    // -------------------------------------------------------------------------
    // Symboles des rouleaux ennemis
    // -------------------------------------------------------------------------

    /** Pression : un manomètre cerclé de laiton, son aiguille dans le rouge. */
    static int[][] pressure() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.ellipseShaded(8f, 8f, 7f, 7f, GOLD, GOLD_DARK, 2f);
        g.ellipse(8f, 8f, 5.2f, 5.2f, rgba("f6f0e2"));
        for (int a = 200; a >= -20; a -= 20) {
            double r = Math.toRadians(a);
            g.set((int) Math.round(8 + 4.4 * Math.cos(r)), (int) Math.round(8 - 4.4 * Math.sin(r)), a < 40 ? RED : COAL);
        }
        g.line(8, 8, 12, 6, RED);
        g.set(8, 8, COAL);
        g.rect(7, 14, 9, 15, STEEL_DARK);
        return g.outlined();
    }

    /** Vol à la tire : une main gantée qui pince une carte. */
    static int[][] pickpocket() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int glove = rgba("3a3a4e"), gloveLight = rgba("6a6a88"), card = rgba("f6f0e2");
        g.rect(9, 1, 14, 9, card);
        g.rect(9, 1, 9, 9, rgba("c8c0b4"));
        g.ellipse(11.5f, 5f, 1.3f, 1.3f, RED);
        g.ellipseShaded(6f, 11f, 4.5f, 3.5f, glove, COAL, 1.5f);
        g.rect(7, 6, 9, 9, glove);         // le pouce
        g.rect(8, 9, 11, 10, gloveLight);  // l'index sous la carte
        g.rect(0, 12, 3, 15, glove);       // la manchette
        g.line(4, 9, 6, 8, gloveLight);
        return g.outlined();
    }

    /** Banquet : une cloche d'argent sur son plateau, la vapeur du plat qui s'échappe. */
    static int[][] banquet() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.ellipseShaded(8f, 11f, 6.5f, 6f, STEEL, STEEL_DARK, 2f);
        g.rect(0, 11, 15, 15, 0);
        for (int y = 11; y < 16; y++) for (int x = 0; x < 16; x++) g.set(x, y, 0);
        g.rect(0, 11, 15, 12, GOLD);
        g.rect(1, 13, 14, 13, GOLD_DARK);
        g.ellipse(8f, 4f, 1.3f, 1.2f, GOLD);
        g.set(5, 7, WHITE); g.set(4, 9, WHITE);
        g.set(11, 1, WHITE); g.set(12, 0, WHITE); g.set(13, 2, WHITE);
        return g.outlined();
    }

    /** Berceuse : un croissant de lune endormi et deux étoiles. */
    static int[][] lullaby() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int moon = rgba("fff2a0"), moonShade = rgba("e0c060");
        g.ellipseShaded(7f, 8f, 6.5f, 6.5f, moon, moonShade, 2f);
        g.ellipse(10.5f, 6f, 5.5f, 5.5f, 0);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                double d = Math.hypot(x + 0.5 - 10.5, y + 0.5 - 6);
                if (d <= 5.5) g.set(x, y, 0);
            }
        }
        g.line(3, 9, 5, 10, COAL);         // l'oeil fermé
        g.set(13, 11, WHITE); g.set(12, 11, moon); g.set(14, 11, moon); g.set(13, 10, moon); g.set(13, 12, moon);
        g.set(14, 2, WHITE); g.set(15, 2, moon); g.set(14, 3, moon);
        return g.outlined();
    }

    /** Braquage : un revolver au canon d'acier, crosse de bois. */
    static int[][] holdup() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int wood = rgba("8a5a2a"), woodDark = rgba("5e3a18");
        g.rect(2, 4, 14, 6, STEEL);
        g.rect(2, 6, 14, 6, STEEL_DARK);
        g.rect(13, 3, 13, 3, STEEL_DARK);  // le guidon
        g.rect(6, 5, 10, 9, STEEL_DARK);   // le barillet
        g.set(7, 6, COAL); g.set(9, 6, COAL); g.set(8, 8, COAL);
        g.triangle(4, 8, 8, 8, 2, 15, wood);
        g.triangle(6, 8, 8, 8, 5, 15, woodDark);
        g.line(8, 9, 10, 11, STEEL_DARK);  // le pontet
        g.line(10, 11, 9, 9, STEEL_DARK);
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Entrée des donjons et emblèmes
    // -------------------------------------------------------------------------

    /**
     * Dessine dans {@code g} (26 x 26) la porte d'un wagon de nuit : caisse de bois verni bordeaux, toit
     * arrondi, marchepied de fer. @return les couleurs de l'écusson (clair, sombre)
     */
    static int[] gateDecor(PixelCanvas g) {
        int varnish = rgba("6a1a2a"), varnishDark = rgba("4a0e1a"), roof = rgba("2a2a34");
        g.rect(0, 10, 25, 23, varnish);
        g.rect(18, 10, 25, 23, varnishDark);
        g.ellipse(13f, 10f, 13f, 3f, roof);
        g.line(0, 13, 25, 13, GOLD);
        for (int x = 2; x <= 23; x += 7) g.rect(x, 15, x + 3, 17, rgba("ffd27a"));
        g.rect(0, 24, 25, 25, COAL);
        g.rect(5, 24, 6, 25, STEEL_DARK);
        g.rect(19, 24, 20, 25, STEEL_DARK);
        return new int[] {rgba("1a6a4a"), rgba("0e4a32")};
    }

    /** Emblème 7 x 7 des donjons du Rapide ('X' : plein), ou {@code null}. */
    static String[] emblem(Dungeon dungeon) {
        return switch (dungeon) {
            case LOCOMOTIVE       -> new String[] {"X......", "XX.XXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX", ".X.X.X.", "......."}; // locomotive
            case WAGON_RESTAURANT -> new String[] {"...X...", "..XXX..", ".XXXXX.", "XXXXXXX", "XXXXXXX", ".......", "XXXXXXX"}; // cloche
            case WAGON_LIT        -> new String[] {"..XXX..", ".XX....", "XX.....", "XX.....", "XX.....", ".XX....", "..XXX.."}; // lune
            case FOURGON          -> new String[] {".XXXXX.", "XXXXXXX", "X.....X", "XXX.XXX", "XXX.XXX", "XXXXXXX", "XXXXXXX"}; // coffre
            default               -> null;
        };
    }

    // -------------------------------------------------------------------------
    // Carte du lieu
    // -------------------------------------------------------------------------

    /**
     * Le Rapide de Minuit : la campagne sous la lune, des collines bleues, et
     * le train de nuit arrêté sur sa voie, de la locomotive (à droite, qui fume)
     * au fourgon blindé (à gauche) ; les fenêtres de ses wagons sont allumées.
     */
    static int[][] rapide() {
        PixelCanvas g = new PixelCanvas(MW, MH);
        Random random = new Random(71);
        int grass = rgba("1e3426"), grassDark = rgba("15261c"), grassLight = rgba("28422f");
        for (int y = 0; y < MH; y++) {
            for (int x = 0; x < MW; x++) {
                int roll = random.nextInt(12);
                g.set(x, y, roll == 0 ? grassDark : roll == 1 ? grassLight : grass);
            }
        }
        // Le ciel de nuit, la lune et les étoiles, puis des collines bleues.
        for (int y = 0; y < 20; y++) for (int x = 0; x < MW; x++) g.set(x, y, rgba("0e1230"));
        for (int i = 0; i < 26; i++) g.set(random.nextInt(MW), random.nextInt(16), rgba("fff2a0"));
        g.ellipse(22f, 7f, 4f, 4f, rgba("fff6d0"));
        g.ellipse(24f, 6f, 3f, 3f, rgba("0e1230"));
        for (int x = 0; x < MW; x++) {
            int top = 14 + (int) Math.round(5 * Math.sin(x / 17.0) + 3 * Math.sin(x / 7.0));
            for (int y = top; y < 24; y++) g.set(x, y, rgba("1a2a4a"));
        }
        // Le ballast et les rails.
        g.rect(0, 47, MW - 1, 50, rgba("5a5048"));
        for (int x = 0; x < MW; x += 4) g.rect(x, 47, x + 1, 50, rgba("6a4a2a"));
        g.line(0, 48, MW - 1, 48, STEEL_DARK);
        g.line(0, 50, MW - 1, 50, STEEL_DARK);
        // Les wagons : fourgon blindé, wagon-lit, wagon-restaurant (vernis bordeaux, fenêtres allumées).
        int varnish = rgba("6a1a2a"), varnishDark = rgba("4a0e1a"), window = rgba("ffd27a"), roof = rgba("2a2a34");
        for (int i = 0; i < 3; i++) {
            int x0 = 8 + i * 36;
            boolean armored = i == 0;
            g.rect(x0, 30, x0 + 32, 45, armored ? rgba("5a6070") : varnish);
            g.rect(x0 + 24, 30, x0 + 32, 45, armored ? rgba("40444e") : varnishDark);
            g.rect(x0 - 1, 28, x0 + 33, 29, roof);
            if (armored) {
                for (int x = x0 + 2; x <= x0 + 30; x += 5) g.set(x, 32, rgba("b8c0cc"));
                g.rect(x0 + 12, 34, x0 + 20, 40, GOLD_DARK);
            } else {
                for (int x = x0 + 3; x <= x0 + 28; x += 6) g.rect(x, 33, x + 3, 36, window);
            }
            for (int w : new int[] {x0 + 5, x0 + 27}) g.ellipse(w, 46f, 2f, 2f, COAL);
            g.rect(x0 + 33, 40, x0 + 35, 41, COAL); // l'attelage
        }
        // La locomotive, à droite : chaudière noire, cheminée qui fume, lanterne.
        g.rect(118, 32, 148, 45, COAL);
        g.rect(118, 32, 148, 34, COAL_LIGHT);
        for (int x = 122; x <= 146; x += 8) g.line(x, 32, x, 45, GOLD_DARK);
        g.rect(112, 26, 122, 45, COAL);
        g.rect(114, 29, 119, 33, window);
        g.rect(140, 24, 144, 32, COAL);
        g.rect(139, 23, 145, 24, COAL_LIGHT);
        g.ellipse(150f, 38f, 2f, 2f, rgba("fff2a0"));
        g.triangle(148, 42, 154, 46, 148, 46, RED);
        for (int w : new int[] {124, 134, 144}) { g.ellipse(w, 45f, 3f, 3f, RED_DARK); g.set(w, 45, GOLD); }
        // La fumée, qui part vers la gauche.
        for (int i = 0; i < 8; i++) g.ellipse(140f - i * 9, 19f - i * 1.2f, 3f + i * 0.6f, 2.5f + i * 0.3f,
            i % 2 == 0 ? rgba("8a8a96") : rgba("6a6a78"));
        // Un chemin de terre du bas de la carte jusqu'au quai, puis le long du train vers chaque wagon.
        int dirt = rgba("8a7a5a"), dirtDark = rgba("6a5a40");
        int[] fork = {78, 66};
        ExplorationArt.path(g, 70, 95, fork[0], fork[1], dirt, dirtDark);
        for (int[] gate : RAPIDE_GATES) ExplorationArt.path(g, fork[0], fork[1], gate[0], gate[1] + 12, dirt, dirtDark);
        // Des lampadaires de quai.
        for (int x = 20; x < MW; x += 34) {
            g.rect(x, 56, x, 63, STEEL_DARK);
            g.ellipse(x, 55f, 1.3f, 1.3f, rgba("fff2a0"));
        }
        return g.pixels;
    }
}
