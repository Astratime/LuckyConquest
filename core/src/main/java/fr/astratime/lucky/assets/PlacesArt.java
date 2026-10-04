package fr.astratime.lucky.assets;

import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.Place;

import java.util.Random;

import static fr.astratime.lucky.assets.PixelCanvas.rgba;

/**
 * Dessins en pixel art des lieux de l'Exploration après la prairie : la carte
 * de chaque lieu (Port des Contrebandiers, Mines d'Or, Casino Englouti),
 * l'entrée de chacun de leurs donjons, et les symboles des rouleaux de leurs
 * ennemis (16 x 16). Les portraits sont dans {@link PlacesPortraits}.
 */
public final class PlacesArt {

    private static final int MW = ExplorationArt.MAP_WIDTH, MH = ExplorationArt.MAP_HEIGHT;
    private static final int GOLD = rgba("ffd54a"), GOLD_DARK = rgba("c4891e"), WHITE = rgba("ffffff");

    private PlacesArt() {}

    // -------------------------------------------------------------------------
    // Symboles des rouleaux ennemis
    // -------------------------------------------------------------------------

    /** Grignotage : une carte rongée, la morsure en haut à droite, et deux dents de rat. */
    static int[][] nibble() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int paper = rgba("f6f0e2"), shade = rgba("d8d0c0"), red = rgba("d8202c"), tooth = rgba("fff8d8");
        g.rect(2, 1, 11, 14, paper);
        g.rect(9, 1, 11, 14, shade);
        g.ellipse(11f, 2f, 3.5f, 3.5f, 0); // la morsure
        g.ellipse(9f, 5.5f, 1.6f, 1.6f, 0);
        g.set(4, 3, red);
        g.rect(4, 7, 6, 9, red);
        g.set(5, 10, red);
        g.rect(11, 9, 12, 14, tooth); // dents de rat
        g.rect(14, 9, 15, 14, tooth);
        g.line(13, 9, 13, 13, rgba("c8b878"));
        return g.outlined();
    }

    /** Ivresse : une chope de bière qui déborde, quelques bulles. */
    static int[][] drunk() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int glass = rgba("e8a020"), glassDark = rgba("b06a10"), foam = rgba("fff8e8"), handle = rgba("c8c0b4");
        g.rect(2, 5, 10, 15, glass);
        g.rect(8, 5, 10, 15, glassDark);
        g.rect(11, 7, 14, 8, handle);
        g.rect(13, 7, 14, 13, handle);
        g.rect(11, 12, 14, 13, handle);
        g.ellipse(6f, 4f, 5f, 2.5f, foam);
        g.ellipse(3f, 6f, 1.5f, 2f, foam); // la mousse coule
        g.set(4, 10, rgba("ffe08a"));
        g.set(6, 13, rgba("ffe08a"));
        g.set(5, 8, rgba("ffe08a"));
        return g.outlined();
    }

    /** Aveuglement : l'éclat d'un phare, un soleil blanc et jaune aux rayons perçants. */
    static int[][] blind() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int yellow = rgba("ffd54a"), pale = rgba("fff4b0");
        int[][] rays = {{8, 0}, {8, 15}, {0, 8}, {15, 8}, {2, 2}, {13, 2}, {2, 13}, {13, 13}};
        for (int[] ray : rays) g.line(8, 8, ray[0], ray[1], yellow);
        g.ellipse(7.5f, 7.5f, 4.5f, 4.5f, yellow);
        g.ellipse(7.5f, 7.5f, 3f, 3f, pale);
        g.ellipse(7.5f, 7.5f, 1.5f, 1.5f, WHITE);
        return g.outlined();
    }

    /** Abordage : un grappin d'acier au bout de sa corde. */
    static int[][] boarding() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int steel = rgba("b8c0cc"), steelDark = rgba("6a7484"), rope = rgba("b8864a");
        g.line(8, 0, 8, 3, rope);
        g.line(7, 0, 7, 2, rgba("8a5a2a"));
        g.ellipse(8f, 4.5f, 1.8f, 1.8f, steelDark);
        g.set(8, 4, 0);
        g.rect(7, 6, 8, 13, steel);
        g.rect(8, 6, 8, 13, steelDark);
        // Les trois crochets.
        g.line(7, 13, 2, 9, steel);
        g.line(2, 9, 2, 7, steel);
        g.line(8, 13, 13, 9, steelDark);
        g.line(13, 9, 13, 7, steelDark);
        g.line(7, 14, 7, 15, steel);
        g.rect(1, 6, 2, 6, steel);
        g.rect(13, 6, 14, 6, steelDark);
        return g.outlined();
    }

    /** Pépite : une pépite d'or bosselée, posée sur un caillou gris. */
    static int[][] nugget() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int stone = rgba("8a8494"), stoneDark = rgba("5e5a68");
        g.ellipseShaded(8f, 12f, 7f, 3.5f, stone, stoneDark, 2f);
        g.ellipseShaded(7f, 7f, 5f, 4f, GOLD, GOLD_DARK, 2f);
        g.ellipseShaded(11f, 9f, 3f, 2.5f, GOLD, GOLD_DARK, 0f);
        g.ellipse(5f, 5f, 1.5f, 1f, rgba("fff0a0"));
        g.set(9, 4, WHITE);
        return g.outlined();
    }

    /** Forage : une mèche de foreuse en spirale, pointe en bas, et son mandrin orange. */
    static int[][] drill() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int steel = rgba("c8d0dc"), steelDark = rgba("7a8494"), orange = rgba("ff8a1e"), orangeDark = rgba("b85a10");
        g.rect(4, 0, 11, 3, orange);
        g.rect(9, 0, 11, 3, orangeDark);
        g.triangle(4, 4, 11, 4, 8, 15, steel);
        for (int y = 5; y <= 13; y += 3) g.line(4 + (y - 4) / 3, y, 11 - (y - 4) / 3, y + 2, steelDark);
        g.set(7, 15, steelDark);
        return g.outlined();
    }

    /** Enclume : une enclume noire, sa corne à gauche, et une étincelle. */
    static int[][] anvil() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int iron = rgba("4a4a56"), ironLight = rgba("7a7a88"), ironDark = rgba("2e2e38");
        g.rect(3, 5, 14, 7, iron);
        g.rect(3, 5, 14, 5, ironLight);
        g.triangle(0, 5, 3, 5, 3, 7, iron);
        g.rect(6, 8, 11, 11, ironDark);
        g.rect(4, 12, 13, 14, iron);
        g.rect(4, 12, 13, 12, ironLight);
        g.set(9, 2, rgba("ffd54a"));
        g.set(8, 1, rgba("ff8a1e"));
        g.set(11, 1, rgba("ffd54a"));
        g.set(12, 3, rgba("ff8a1e"));
        return g.outlined();
    }

    /** Chant : deux croches liées, bleu océan. */
    static int[][] song() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int note = rgba("3ac8d8"), noteDark = rgba("1e7a8a");
        g.ellipse(4f, 12.5f, 2.6f, 2f, note);
        g.ellipse(11.5f, 10.5f, 2.6f, 2f, noteDark);
        g.rect(6, 3, 6, 12, note);
        g.rect(13, 1, 13, 10, noteDark);
        g.line(6, 3, 13, 1, note);
        g.line(6, 4, 13, 2, note);
        g.line(6, 5, 13, 3, noteDark);
        return g.outlined();
    }

    /** Morsure : une pièce d'or croquée par un requin, les marques des dents sur le bord. */
    static int[][] bankBite() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.ellipseShaded(8f, 8f, 7f, 7f, GOLD, GOLD_DARK, 2f);
        g.ellipse(8f, 8f, 4.5f, 4.5f, GOLD_DARK);
        g.ellipse(7.5f, 7.5f, 3.5f, 3.5f, GOLD);
        g.rect(7, 5, 8, 10, GOLD_DARK);
        g.ellipse(14f, 4f, 4f, 4f, 0); // la morsure
        for (int[] tooth : new int[][] {{10, 2}, {11, 4}, {12, 6}, {13, 7}}) g.set(tooth[0], tooth[1], WHITE);
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Entrées des donjons
    // -------------------------------------------------------------------------

    /**
     * Entrée d'un donjon hors de la prairie, dans le style de son lieu : porte
     * de bois cordée sur les quais, galerie étayée dans la mine, arche de
     * coquillages sous la mer ; au-dessus, un écusson frappé de son emblème.
     */
    public static int[][] gate(Dungeon dungeon) {
        int size = ExplorationArt.GATE_SIZE;
        PixelCanvas g = new PixelCanvas(size, size);
        Place place = Place.of(dungeon);
        int door = rgba("1a1218"), doorLight = rgba("3a2a2e");
        int plate, plateDark;
        switch (place) {
            case PORT -> {
                int wood = rgba("8a5a2a"), woodDark = rgba("5e3a18"), rope = rgba("d8b878");
                g.rect(2, 12, 23, 25, wood);
                g.rect(17, 12, 23, 25, woodDark);
                g.triangle(0, 13, 25, 13, 13, 6, woodDark);
                for (int y = 15; y <= 24; y += 3) g.line(3, y, 22, y, woodDark);
                g.line(1, 13, 12, 7, rope);
                g.line(13, 7, 24, 13, rope);
                plate = rgba("2a5a8a");
                plateDark = rgba("1a3a5e");
            }
            case MINES -> {
                int rock = rgba("6a5444"), rockDark = rgba("4a3a2e"), beam = rgba("9a6a3a"), beamDark = rgba("6a4420");
                g.ellipseShaded(13f, 17f, 12.5f, 10f, rock, rockDark, 3f);
                g.rect(0, 17, 25, 25, rock);
                g.rect(17, 17, 25, 25, rockDark);
                g.rect(6, 13, 7, 25, beam);
                g.rect(18, 13, 19, 25, beamDark);
                g.rect(5, 12, 20, 13, beam);
                plate = rgba("a87a1e");
                plateDark = rgba("6a4a10");
            }
            default -> {
                int coral = rgba("e86a8a"), coralDark = rgba("a83a5a"), sand = rgba("e8d49a");
                g.rect(0, 23, 25, 25, sand);
                g.ellipseShaded(13f, 18f, 12f, 10f, coral, coralDark, 3f);
                g.rect(1, 18, 24, 23, coral);
                g.rect(17, 18, 24, 23, coralDark);
                for (int x = 3; x <= 22; x += 4) g.ellipse(x, 10f + Math.abs(13 - x) / 2f, 1.5f, 1.5f, rgba("ffd0dc"));
                plate = rgba("6a3aa8");
                plateDark = rgba("4a2478");
            }
        }
        // Porte en arche.
        g.ellipse(13f, 19f, 5f, 5f, door);
        g.rect(8, 19, 18, 25, door);
        g.rect(9, 20, 9, 25, doorLight);
        // Écusson et emblème.
        g.rect(8, 1, 18, 9, plateDark);
        g.rect(9, 2, 17, 8, plate);
        g.triangle(8, 9, 18, 9, 13, 13, plateDark);
        g.triangle(9, 9, 17, 9, 13, 12, plate);
        stamp(g, emblem(dungeon), 10, 2, WHITE);
        return g.outlined();
    }

    /** Emblème 7 x 7 de chaque donjon ('X' : plein). */
    private static String[] emblem(Dungeon dungeon) {
        return switch (dungeon) {
            case CALE -> new String[] {".XXXXX.", "X.....X", "XXXXXXX", "X.....X", "XXXXXXX", "X.....X", ".XXXXX."}; // tonneau
            case TAVERNE -> new String[] {"XXXXX..", "XXXXX..", "X...XXX", "X...X.X", "X...XXX", "X...X..", "XXXXX.."}; // chope
            case PHARE -> new String[] {"X.XXX.X", "..XXX..", "...X...", "..XXX..", "..X.X..", "..XXX..", ".XXXXX."}; // phare
            case GALION -> new String[] {"...X...", "..XXX..", "...X...", "...X...", "X..X..X", "XX.X.XX", ".XXXXX."}; // ancre
            case FILON -> new String[] {"...XX..", "..XXXX.", ".XXXXXX", "XXXXXXX", "XXXXXX.", ".XXXX..", "..XX..."}; // pépite
            case PUITS -> new String[] {"XXXXXXX", ".XXXXX.", "..XXX..", "..XXX..", "...X...", "...X...", "...X..."}; // mèche
            case FORGE -> new String[] {".......", "XXXXXXX", ".XXXXXX", "...XXX.", "...XXX.", "..XXXXX", "..XXXXX"}; // enclume
            case GOUFFRE -> new String[] {".XXXXX.", "XXXXXXX", "X.XXX.X", "XXXXXXX", ".XX.XX.", ".XXXXX.", ".X.X.X."}; // crâne
            case BAR -> new String[] {"XXXXXXX", ".XXXXX.", "..XXX..", "...X...", "...X...", "...X...", ".XXXXX."}; // verre
            case MACHINES -> new String[] {"XXXXXXX", ".....XX", "....XX.", "...XX..", "..XX...", "..XX...", "..XX..."}; // sept
            case COFFRES -> new String[] {".XXXXX.", "XXXXXXX", "X.....X", "XXXXXXX", "XXX.XXX", "XXXXXXX", "XXXXXXX"}; // coffre
            case VIP -> new String[] {".......", "X..X..X", "XX.X.XX", "XXXXXXX", "XXXXXXX", ".......", "XXXXXXX"}; // couronne
            default -> new String[] {"XXXXXXX", "X.....X", "X.....X", "X.....X", "X.....X", "X.....X", "XXXXXXX"};
        };
    }

    private static void stamp(PixelCanvas g, String[] rows, int x, int y, int color) {
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < rows[row].length(); col++) {
                if (rows[row].charAt(col) == 'X') g.set(x + col, y + row, color);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Cartes des lieux
    // -------------------------------------------------------------------------

    /** Entrées des donjons du Port (Cale, Taverne, Phare, Galion), centre de l'entrée, y vers le bas. */
    static final int[][] PORT_GATES   = {{26, 34}, {64, 22}, {108, 16}, {138, 58}};
    /** Entrées des donjons des Mines (Filon, Puits, Forge, Gouffre). */
    static final int[][] MINES_GATES  = {{24, 26}, {66, 40}, {112, 22}, {136, 66}};
    /** Entrées des donjons du Casino (Bar, Machines, Coffres, VIP). */
    static final int[][] CASINO_GATES = {{28, 30}, {72, 20}, {118, 32}, {80, 62}};

    /**
     * Le Port des Contrebandiers vu du ciel : les quais de planches, la mer à
     * droite et en bas, des bateaux, des caisses, un phare au bord de l'eau.
     */
    static int[][] port() {
        PixelCanvas g = new PixelCanvas(MW, MH);
        Random random = new Random(21);
        int sea = rgba("2a5a9a"), seaDark = rgba("1e4a80"), seaLight = rgba("4a7ac0");
        int plank = rgba("a8784a"), plankDark = rgba("8a5a32"), plankLight = rgba("c09060");
        for (int y = 0; y < MH; y++) {
            for (int x = 0; x < MW; x++) {
                int roll = random.nextInt(12);
                g.set(x, y, roll == 0 ? seaDark : roll == 1 ? seaLight : sea);
            }
        }
        // Les quais : une grande jetée de planches, en L.
        g.rect(0, 0, 100, 70, plank);
        g.rect(100, 0, 124, 44, plank);
        for (int y = 2; y <= 70; y += 4) g.line(0, y, 100, y, plankDark);
        for (int y = 2; y <= 44; y += 4) g.line(100, y, 124, y, plankDark);
        for (int x = 6; x <= 120; x += 13) for (int y = 0; y <= 70; y += 8) g.set(x, y + 1, plankLight);
        g.rect(0, 71, 100, 72, plankDark);
        // Pilotis qui sortent de l'eau.
        for (int x = 4; x <= 96; x += 12) g.rect(x, 73, x + 1, 75, rgba("5e3a18"));
        // Un ponton qui mène au galion, à droite.
        g.rect(124, 54, 148, 61, plank);
        for (int x = 126; x <= 148; x += 3) g.line(x, 54, x, 61, plankDark);
        // Le galion noir.
        g.ellipse(150f, 78f, 12f, 5f, rgba("2a1a12"));
        g.rect(138, 72, 160, 76, rgba("3a2416"));
        g.rect(150, 58, 150, 74, rgba("5e3a18"));
        g.triangle(151, 60, 151, 71, 159, 70, rgba("1a1a1a"));
        g.set(154, 64, WHITE);
        // Petits bateaux dans l'eau.
        int[][] boats = {{30, 86}, {70, 90}, {118, 84}};
        for (int[] b : boats) {
            g.ellipse(b[0], b[1], 6f, 2.5f, rgba("8a5a2a"));
            g.rect(b[0], b[1] - 7, b[0], b[1] - 1, rgba("5e3a18"));
            g.triangle(b[0] + 1, b[1] - 7, b[0] + 1, b[1] - 2, b[0] + 5, b[1] - 2, rgba("f0e8d8"));
        }
        // Caisses et tonneaux sur les quais.
        int crate = rgba("c8904a"), crateDark = rgba("8a5a2a");
        int[][] crates = {{8, 50}, {14, 50}, {11, 45}, {84, 40}, {90, 40}, {44, 8}, {118, 6}, {86, 60}};
        for (int[] c : crates) {
            g.rect(c[0], c[1], c[0] + 4, c[1] + 4, crate);
            g.line(c[0], c[1], c[0] + 4, c[1] + 4, crateDark);
            g.rect(c[0] + 4, c[1], c[0] + 4, c[1] + 4, crateDark);
        }
        int[][] barrels = {{52, 54}, {56, 56}, {32, 14}, {96, 26}};
        for (int[] b : barrels) {
            g.ellipseShaded(b[0], b[1], 2.5f, 2.5f, rgba("9a5428"), rgba("6a3416"), 0.5f);
            g.set(b[0], b[1], rgba("c4891e"));
        }
        // Le phare, sur son rocher.
        g.ellipse(112f, 12f, 9f, 6f, rgba("6a6a78"));
        g.rect(109, 0, 115, 10, rgba("f0e8e0"));
        g.rect(109, 3, 115, 4, rgba("d8202c"));
        g.rect(109, 7, 115, 8, rgba("d8202c"));
        // Cordages et chemins : les planches claires vers chaque donjon.
        int dirt = rgba("d8b080"), dirtDark = rgba("b89060");
        int[] fork = {70, 48};
        ExplorationArt.path(g, 60, 95, fork[0], fork[1], dirt, dirtDark);
        for (int[] gate : PORT_GATES) ExplorationArt.path(g, fork[0], fork[1], gate[0], gate[1] + 6, dirt, dirtDark);
        return g.pixels;
    }

    /**
     * Les Mines d'Or : la roche sombre, des galeries, des rails, des wagonnets
     * pleins de pièces et des veines d'or qui brillent.
     */
    static int[][] mines() {
        PixelCanvas g = new PixelCanvas(MW, MH);
        Random random = new Random(31);
        int rock = rgba("4a3a2e"), rockDark = rgba("3a2c22"), rockLight = rgba("5e4a3a");
        for (int y = 0; y < MH; y++) {
            for (int x = 0; x < MW; x++) {
                int roll = random.nextInt(10);
                g.set(x, y, roll == 0 ? rockDark : roll == 1 ? rockLight : rock);
            }
        }
        // Gros rochers et veines d'or.
        float[][] boulders = {{10, 10, 9, 6}, {150, 12, 10, 8}, {40, 80, 12, 7}, {150, 88, 12, 7}, {96, 8, 8, 5}};
        for (float[] b : boulders) g.ellipseShaded(b[0], b[1], b[2], b[3], rgba("6a5444"), rgba("2e2218"), 2f);
        for (int i = 0; i < 9; i++) {
            int x = random.nextInt(MW), y = random.nextInt(MH);
            for (int j = 0; j < 7; j++) {
                g.set(x + j, y + (j % 3) - 1, GOLD);
                if (j % 2 == 0) g.set(x + j, y + (j % 3), GOLD_DARK);
            }
        }
        // Une rivière de lave tout en bas : le gouffre.
        for (int x = 0; x < MW; x++) {
            int y = 90 + (int) Math.round(2 * Math.sin(x / 9.0));
            g.rect(x, y, x, MH - 1, rgba("e85a1e"));
            g.set(x, y, rgba("ffb84a"));
        }
        // Galeries : les chemins, comme des rails.
        int dirt = rgba("7a6048"), dirtDark = rgba("5a4434");
        int[] fork = {84, 52};
        ExplorationArt.path(g, 84, 95, fork[0], fork[1], dirt, dirtDark);
        for (int[] gate : MINES_GATES) ExplorationArt.path(g, fork[0], fork[1], gate[0], gate[1] + 6, dirt, dirtDark);
        // Traverses des rails sur les galeries.
        for (int y = 0; y < MH; y += 3) {
            for (int x = 0; x < MW; x += 3) {
                if (g.get(x, y) == dirt && g.get(x + 1, y) == dirt) g.set(x, y, rgba("9a6a3a"));
            }
        }
        // Wagonnets pleins d'or.
        int[][] carts = {{40, 44}, {100, 64}, {120, 40}};
        for (int[] c : carts) {
            g.rect(c[0], c[1], c[0] + 7, c[1] + 4, rgba("6a6a78"));
            g.rect(c[0] + 1, c[1] - 1, c[0] + 6, c[1], GOLD);
            g.set(c[0] + 3, c[1] - 2, GOLD);
            g.set(c[0] + 1, c[1] + 5, rgba("1a1a1a"));
            g.set(c[0] + 6, c[1] + 5, rgba("1a1a1a"));
        }
        // Lanternes.
        for (int[] l : new int[][] {{50, 20}, {90, 30}, {20, 60}, {130, 50}}) {
            g.ellipse(l[0], l[1], 2.5f, 2.5f, rgba("ffb84a", 140));
            g.set(l[0], l[1], rgba("fff0a0"));
        }
        return g.pixels;
    }

    /**
     * Le Casino Englouti : le fond de la mer, du sable, des coraux, des algues,
     * des tables de jeu renversées et des bulles qui montent.
     */
    static int[][] casino() {
        PixelCanvas g = new PixelCanvas(MW, MH);
        Random random = new Random(41);
        int[] water = {rgba("0e2a5a"), rgba("12346a"), rgba("16407a"), rgba("1a4a86")};
        for (int y = 0; y < MH; y++) {
            int band = Math.min(water.length - 1, y * water.length / MH);
            for (int x = 0; x < MW; x++) g.set(x, y, random.nextInt(16) == 0 ? water[Math.max(0, band - 1)] : water[band]);
        }
        // Rayons de lumière qui tombent de la surface.
        for (int i = 0; i < 4; i++) {
            int x0 = 20 + i * 38;
            for (int y = 0; y < MH; y++) {
                for (int x = x0 + y / 3; x < x0 + y / 3 + 6; x++) {
                    if (x < MW && (x + y) % 2 == 0) g.set(x, y, rgba("2a5a9a"));
                }
            }
        }
        // Sable au fond.
        for (int x = 0; x < MW; x++) {
            int y = 78 + (int) Math.round(3 * Math.sin(x / 13.0));
            g.rect(x, y, x, MH - 1, rgba("c8b47a"));
            g.set(x, y, rgba("e8d49a"));
        }
        // Coraux et algues.
        int[] coral = {rgba("e86a8a"), rgba("ff9a4a"), rgba("b04ad8")};
        for (int i = 0; i < 12; i++) {
            int x = 4 + random.nextInt(MW - 8), y = 80 + random.nextInt(10);
            int color = coral[random.nextInt(coral.length)];
            g.line(x, y, x - 2, y - 6, color);
            g.line(x, y, x + 2, y - 7, color);
            g.line(x, y, x, y - 8, color);
        }
        for (int i = 0; i < 14; i++) {
            int x = random.nextInt(MW), y = 70 + random.nextInt(20);
            for (int j = 0; j < 12; j++) g.set(x + (int) Math.round(Math.sin((y - j) / 2.0)), y - j, rgba("2e8a4a"));
        }
        // Tables de jeu renversées : feutre vert et bord de bois.
        for (int[] t : new int[][] {{46, 46}, {104, 50}, {138, 18}}) {
            g.ellipse(t[0], t[1], 8f, 4f, rgba("6a3a1a"));
            g.ellipse(t[0], t[1], 7f, 3f, rgba("1e7a3a"));
            g.set(t[0] - 2, t[1], WHITE);
            g.set(t[0] + 3, t[1] - 1, rgba("d8202c"));
        }
        // Jetons éparpillés.
        int[] chips = {rgba("d8202c"), rgba("2a5ad8"), GOLD, WHITE};
        for (int i = 0; i < 24; i++) g.set(random.nextInt(MW), 60 + random.nextInt(30), chips[random.nextInt(chips.length)]);
        // Chemins de sable clair vers chaque donjon.
        int dirt = rgba("e8d49a"), dirtDark = rgba("b8a46a");
        int[] fork = {80, 44};
        ExplorationArt.path(g, 70, 95, fork[0], fork[1], dirt, dirtDark);
        for (int[] gate : CASINO_GATES) ExplorationArt.path(g, fork[0], fork[1], gate[0], gate[1] + 6, dirt, dirtDark);
        // Bulles.
        for (int i = 0; i < 30; i++) {
            int x = random.nextInt(MW), y = random.nextInt(70);
            if (ExplorationArt.isOnPath(g, x, y, dirt, dirtDark)) continue;
            g.set(x, y, rgba("a8d4ff"));
            if (i % 3 == 0) g.set(x + 1, y - 1, rgba("d8ecff"));
        }
        return g.pixels;
    }
}
