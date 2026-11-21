package fr.astratime.lucky.assets;

import fr.astratime.lucky.entities.exploration.Dungeon;

import java.util.Random;

import static fr.astratime.lucky.assets.PixelCanvas.rgba;

/**
 * Dessins en pixel art du Temple du Destin : la carte du lieu (un temple accroché
 * aux falaises, au-dessus de la mer retirée, au coucher du soleil), le décor de
 * l'entrée de ses donjons (un portique de pierre rouge), leurs emblèmes, et les
 * symboles des rouleaux de ses ennemis (16 x 16). Les portraits sont dans
 * {@link TemplePortraits}.
 */
public final class TempleArt {

    private static final int MW = ExplorationArt.MAP_WIDTH, MH = ExplorationArt.MAP_HEIGHT;
    private static final int GOLD = rgba("ffd54a"), GOLD_DARK = rgba("c4891e"), GOLD_LIGHT = rgba("fff2a0");
    private static final int WHITE = rgba("ffffff"), IVORY = rgba("f6f0e2"), IVORY_SHADE = rgba("c8c0b4");
    private static final int RED = rgba("c8323a"), RED_DARK = rgba("7a1a22");
    private static final int BRONZE = rgba("c87a3a"), BRONZE_DARK = rgba("8a4a1e");
    private static final int STONE = rgba("a89a88"), STONE_DARK = rgba("6e6254"), STONE_LIGHT = rgba("cabca8");
    private static final int INK = rgba("1a1018");

    /** Entrées des donjons du Temple (Parvis, Jardin des oracles, Salle des dés, Sanctuaire), centre de l'entrée. */
    static final int[][] TEMPLE_GATES = {{26, 58}, {60, 44}, {100, 44}, {134, 30}};

    private TempleArt() {}

    // -------------------------------------------------------------------------
    // Symboles des rouleaux ennemis
    // -------------------------------------------------------------------------

    /** Glas : une cloche de bronze qui sonne, son battant qui oscille, deux traits de vibration. */
    static int[][] knell() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.rect(7, 0, 8, 1, BRONZE_DARK);
        g.ellipseShaded(7.5f, 6f, 4.5f, 4.5f, BRONZE, BRONZE_DARK, 2f);
        g.triangle(2, 6, 13, 6, 7, 6, BRONZE);
        g.rect(3, 6, 12, 11, BRONZE);
        g.rect(9, 6, 12, 11, BRONZE_DARK);
        g.rect(1, 11, 14, 12, GOLD_DARK);
        g.rect(4, 7, 4, 10, GOLD);
        g.ellipse(9f, 14f, 1.3f, 1.3f, BRONZE_DARK);                // le battant, qui sort à droite
        g.line(0, 3, 0, 6, GOLD_LIGHT);                              // les vibrations
        g.line(15, 3, 15, 6, GOLD_LIGHT);
        return g.outlined();
    }

    /** Fil du destin : une bobine de bois d'où part un fil d'or qui serpente jusqu'à un nœud. */
    static int[][] fateThread() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int wood = rgba("8a5a2a"), woodDark = rgba("5e3a18");
        g.rect(1, 2, 7, 3, wood);
        g.rect(1, 11, 7, 12, wood);
        g.rect(2, 4, 6, 10, GOLD);
        g.rect(5, 4, 6, 10, GOLD_DARK);
        for (int y = 4; y <= 10; y += 2) g.line(2, y, 6, y, GOLD_DARK);
        g.rect(1, 3, 1, 3, woodDark);
        int[][] thread = {{7, 6}, {8, 5}, {9, 5}, {10, 6}, {11, 8}, {12, 10}, {12, 11}, {11, 12}, {10, 13}, {11, 14}, {13, 14}, {14, 13}};
        for (int[] p : thread) g.set(p[0], p[1], GOLD_LIGHT);
        g.ellipse(13.5f, 13.5f, 1.2f, 1.2f, GOLD);                     // le nœud
        return g.outlined();
    }

    /** Double six : deux dés d'ivoire penchés, chacun sur sa face six. */
    static int[][] doubleSix() {
        PixelCanvas g = new PixelCanvas(16, 16);
        die(g, 0, 2);
        die(g, 7, 7);
        return g.outlined();
    }

    /** Un dé d'ivoire de 9 x 9 en ({@code x}, {@code y}), face six (deux colonnes de trois points). */
    private static void die(PixelCanvas g, int x, int y) {
        g.rect(x, y, x + 8, y + 8, IVORY);
        g.rect(x + 7, y, x + 8, y + 8, IVORY_SHADE);
        g.rect(x, y + 7, x + 8, y + 8, IVORY_SHADE);
        for (int dy = 1; dy <= 5; dy += 2) {
            g.set(x + 2, y + dy, RED);
            g.set(x + 5, y + dy, RED);
        }
    }

    /** Karma : une roue d'or à huit rayons, un rubis au moyeu. */
    static int[][] karma() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.ellipseShaded(7.5f, 7.5f, 7f, 7f, GOLD, GOLD_DARK, 2f);
        g.ellipse(7.5f, 7.5f, 5f, 5f, 0);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) if (Math.hypot(x - 7.5, y - 7.5) < 5.2) g.set(x, y, 0);
        }
        for (int a = 0; a < 360; a += 45) {
            double r = Math.toRadians(a);
            g.line(8, 8, (int) Math.round(7.5 + 5 * Math.cos(r)), (int) Math.round(7.5 - 5 * Math.sin(r)), GOLD_DARK);
        }
        g.ellipse(7.5f, 7.5f, 1.6f, 1.6f, RED);
        g.set(7, 7, rgba("ff8a8a"));
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Entrée des donjons et emblèmes
    // -------------------------------------------------------------------------

    /**
     * Dessine dans {@code g} (26 x 26) un portique de temple : deux piliers rouges, un double linteau
     * noir aux bouts relevés, des marches de pierre. @return les couleurs de l'écusson (clair, sombre)
     */
    static int[] gateDecor(PixelCanvas g) {
        g.rect(3, 12, 6, 23, RED);
        g.rect(19, 12, 22, 23, RED);
        g.rect(5, 12, 6, 23, RED_DARK);
        g.rect(21, 12, 22, 23, RED_DARK);
        g.rect(0, 7, 25, 9, INK);
        g.set(0, 6, INK); g.set(25, 6, INK);
        g.rect(2, 12, 23, 13, INK);
        g.rect(12, 9, 13, 12, INK);
        g.rect(0, 24, 25, 25, STONE);
        g.rect(2, 22, 23, 23, STONE_LIGHT);
        return new int[] {GOLD, GOLD_DARK};
    }

    /** Emblème 7 x 7 des donjons du Temple ('X' : plein), ou {@code null}. */
    static String[] emblem(Dungeon dungeon) {
        return switch (dungeon) {
            case PARVIS         -> new String[] {"...X...", "..XXX..", ".XXXXX.", ".XXXXX.", ".XXXXX.", "XXXXXXX", "...X..."}; // cloche
            case JARDIN_ORACLES -> new String[] {".......", "..XXX..", ".X...X.", "X..X..X", ".X...X.", "..XXX..", "......."}; // oeil
            case SALLE_DES_DES  -> new String[] {"XXXXXXX", "X.X.X.X", "XXXXXXX", "X.X.X.X", "XXXXXXX", "X.X.X.X", "XXXXXXX"}; // dé
            case SANCTUAIRE     -> new String[] {"...X...", "..XXX..", "..X.X..", ".XXXXX.", "..XXX..", ".XXXXX.", "XXXXXXX"}; // idole
            default             -> null;
        };
    }

    // -------------------------------------------------------------------------
    // Carte du lieu
    // -------------------------------------------------------------------------

    /**
     * Le Temple du Destin : un ciel de crépuscule, la mer retirée au loin (une bande de sable mouillé), des
     * falaises ocre en terrasses ; sur chaque terrasse un portique rouge, et tout en haut, à droite, le
     * sanctuaire au toit doré. Un escalier de pierre monte de terrasse en terrasse.
     */
    static int[][] temple() {
        PixelCanvas g = new PixelCanvas(MW, MH);
        Random random = new Random(91);
        // Le ciel du soir, du violet au orange.
        String[] sky = {"2a1430", "3a1a3a", "5a2440", "8a3440", "b84a3a", "d86a3a", "e88a4a"};
        for (int y = 0; y < 28; y++) {
            int band = Math.min(sky.length - 1, y / 4);
            for (int x = 0; x < MW; x++) g.set(x, y, rgba(sky[band]));
        }
        g.ellipse(40f, 24f, 7f, 7f, rgba("ffd27a"));                // le soleil couchant
        g.ellipse(40f, 24f, 5f, 5f, rgba("fff2c0"));
        for (int i = 0; i < 14; i++) g.set(random.nextInt(MW), random.nextInt(8), GOLD_LIGHT);
        // La mer retirée : un liseré d'eau au loin, puis le sable mouillé.
        g.rect(0, 26, MW - 1, 27, rgba("3a6a8a"));
        for (int y = 28; y < MH; y++) {
            for (int x = 0; x < MW; x++) {
                int roll = random.nextInt(10);
                g.set(x, y, roll == 0 ? rgba("a8885a") : roll == 1 ? rgba("d0b07a") : rgba("bc9c6a"));
            }
        }
        // Les falaises en terrasses, de plus en plus hautes vers la droite.
        int[][] terraces = {{0, 66, 52}, {40, 52, 84}, {80, 52, 120}, {116, 38, MW}};
        for (int[] t : terraces) {
            int top = t[1];
            for (int x = t[0]; x < t[2]; x++) {
                int edge = top + (int) Math.round(1.5 * Math.sin(x / 3.0));
                for (int y = edge; y < MH; y++) {
                    int roll = random.nextInt(9);
                    g.set(x, y, y < edge + 2 ? rgba("d8a86a") : roll == 0 ? rgba("8a5a3a") : rgba("a8703e"));
                }
            }
            for (int x = t[0]; x < t[2]; x += 5) g.line(x, top + 4, x + 2, top + 9, rgba("7a4a2a")); // les strates
        }
        // L'escalier de pierre qui monte de terrasse en terrasse.
        int[][] stairs = {{44, 70, 66}, {84, 56, 52}, {120, 42, 52}};
        for (int[] s : stairs) {
            for (int k = 0; k < 6; k++) g.rect(s[0] - 3 + k, s[1] - k * 2, s[0] + 3 + k, s[1] - k * 2 + 1,
                k % 2 == 0 ? STONE_LIGHT : STONE);
        }
        // Au loin, sur le plus haut rocher, la silhouette d'une pagode à trois toits contre le soleil.
        int px = 96, base = 26, roofDark = rgba("3a1a2a");
        g.rect(px - 2, base - 16, px + 2, base, roofDark);
        for (int k = 0; k < 3; k++) {
            int y = base - 4 - k * 6, half = 9 - k * 2;
            g.rect(px - half, y, px + half, y + 1, roofDark);
            g.set(px - half - 1, y - 1, roofDark); g.set(px + half + 1, y - 1, roofDark);
        }
        g.set(px, base - 18, GOLD);
        // Des lanternes de pierre le long du chemin.
        int[][] lanterns = {{14, 68}, {50, 54}, {76, 54}, {110, 54}, {150, 40}};
        for (int[] l : lanterns) {
            g.rect(l[0], l[1] - 4, l[0] + 1, l[1], STONE);
            g.rect(l[0] - 1, l[1] - 6, l[0] + 2, l[1] - 5, GOLD_LIGHT);
        }
        // Un chemin de sable du bas de la carte jusqu'à chaque portique.
        int dirt = rgba("e0c08a"), dirtDark = rgba("b8965e");
        int[] fork = {70, 82};
        ExplorationArt.path(g, 70, 95, fork[0], fork[1], dirt, dirtDark);
        for (int[] gate : TEMPLE_GATES) ExplorationArt.path(g, fork[0], fork[1], gate[0], gate[1] + 12, dirt, dirtDark);
        return g.pixels;
    }
}
