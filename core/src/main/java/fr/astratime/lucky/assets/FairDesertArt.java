package fr.astratime.lucky.assets;

import fr.astratime.lucky.entities.exploration.Dungeon;

import java.util.Random;

import static fr.astratime.lucky.assets.PixelCanvas.rgba;

/**
 * Dessins en pixel art de la Fête foraine et du Désert de Braise : la carte de
 * chaque lieu, le décor de l'entrée de leurs donjons (avec leur emblème), et les
 * symboles des rouleaux de leurs ennemis (16 x 16). Les portraits sont dans
 * {@link FairDesertPortraits}.
 */
public final class FairDesertArt {

    private static final int MW = ExplorationArt.MAP_WIDTH, MH = ExplorationArt.MAP_HEIGHT;
    private static final int GOLD = rgba("ffd54a"), GOLD_DARK = rgba("c4891e"), WHITE = rgba("ffffff");
    private static final int RED = rgba("d8202c"), RED_DARK = rgba("8a1a1a");

    private FairDesertArt() {}

    // -------------------------------------------------------------------------
    // Symboles des rouleaux ennemis : la Fête foraine
    // -------------------------------------------------------------------------

    /** Jonglage : trois balles en arc au-dessus d'une main. */
    static int[][] juggle() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int glove = rgba("f6f0e2"), gloveShade = rgba("c8c0b4");
        g.ellipseShaded(3f, 7f, 2.5f, 2.5f, RED, RED_DARK, 0.8f);
        g.ellipseShaded(8f, 3f, 2.5f, 2.5f, rgba("2a8ad8"), rgba("1a5a9a"), 0.8f);
        g.ellipseShaded(13f, 7f, 2.5f, 2.5f, GOLD, GOLD_DARK, 0.8f);
        g.set(2, 6, WHITE); g.set(7, 2, WHITE); g.set(12, 6, WHITE);
        g.ellipse(8f, 13f, 4f, 2.5f, glove);
        g.rect(6, 13, 10, 15, glove);
        g.rect(9, 13, 10, 15, gloveShade);
        g.set(5, 11, glove); g.set(11, 11, glove);
        return g.outlined();
    }

    /** Numéro : un tambour rouge cerclé d'or, deux baguettes croisées. */
    static int[][] act() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int skin = rgba("f6f0e2"), wood = rgba("c8904a");
        g.rect(2, 7, 13, 14, RED);
        g.rect(10, 7, 13, 14, RED_DARK);
        g.ellipse(7.5f, 7f, 5.5f, 2f, skin);
        g.line(2, 9, 13, 9, GOLD);
        g.line(2, 14, 13, 14, GOLD);
        for (int x = 2; x <= 12; x += 2) { g.set(x, 10 + (x / 2) % 2, GOLD); g.set(x + 1, 12 - (x / 2) % 2, GOLD); }
        g.line(1, 1, 7, 6, wood);
        g.line(14, 1, 8, 6, wood);
        g.ellipse(1f, 1f, 1.2f, 1.2f, WHITE);
        g.ellipse(14f, 1f, 1.2f, 1.2f, WHITE);
        return g.outlined();
    }

    /** Frayeur : un fantôme drapé, bouche ouverte. */
    static int[][] scare() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int sheet = rgba("eef2ff"), shade = rgba("b8c0e0");
        g.ellipseShaded(8f, 6f, 5.5f, 5f, sheet, shade, 2f);
        g.rect(3, 6, 13, 13, sheet);
        g.rect(10, 6, 13, 13, shade);
        for (int x = 3; x <= 13; x += 3) g.rect(x, 14, x + 1, 15, x >= 10 ? shade : sheet);
        g.rect(5, 5, 6, 7, rgba("1a1018"));
        g.rect(9, 5, 10, 7, rgba("1a1018"));
        g.ellipse(8f, 10f, 1.5f, 2f, rgba("1a1018"));
        return g.outlined();
    }

    /** Tunnel : une arche noire où filent deux rails. */
    static int[][] tunnel() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int stone = rgba("8a7a9a"), stoneDark = rgba("5a4a6a"), dark = rgba("0a0610"), rail = rgba("b8c0cc");
        g.ellipseShaded(8f, 8f, 7.5f, 7f, stone, stoneDark, 2f);
        g.rect(0, 8, 15, 15, stone);
        g.rect(11, 8, 15, 15, stoneDark);
        g.ellipse(8f, 9f, 4.5f, 4.5f, dark);
        g.rect(4, 9, 12, 15, dark);
        g.line(6, 15, 7, 10, rail);
        g.line(10, 15, 9, 10, rail);
        g.set(8, 7, rgba("ffe08a"));
        g.set(7, 7, rgba("ffe08a"));
        return g.outlined();
    }

    /** Tourniquet : une manivelle dorée et une flèche qui tourne. */
    static int[][] turnstile() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int arrow = rgba("6ad86a"), arrowDark = rgba("2a8a3a");
        for (int a = 30; a < 300; a += 6) {
            double r = Math.toRadians(a);
            int x = (int) Math.round(8 + 6 * Math.cos(r)), y = (int) Math.round(8 - 6 * Math.sin(r));
            g.set(x, y, arrow);
            g.set(x + (x < 8 ? 1 : -1), y, arrowDark);
        }
        g.triangle(12, 1, 15, 5, 11, 6, arrow);
        g.ellipseShaded(8f, 8f, 2.5f, 2.5f, GOLD, GOLD_DARK, 0.8f);
        g.line(8, 8, 12, 11, GOLD_DARK);
        g.rect(12, 11, 13, 13, RED);
        return g.outlined();
    }

    /** Grand tour : la grande roue, ses rayons et ses nacelles. */
    static int[][] bigWheel() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int steel = rgba("dce4ee"), cabin = rgba("e86a8a"), cabin2 = rgba("2a8ad8"), foot = rgba("8a97a8");
        for (int a = 0; a < 360; a += 8) {
            double r = Math.toRadians(a);
            g.set((int) Math.round(8 + 6 * Math.cos(r)), (int) Math.round(7 - 6 * Math.sin(r)), steel);
        }
        for (int a = 0; a < 360; a += 45) {
            double r = Math.toRadians(a);
            g.line(8, 7, (int) Math.round(8 + 6 * Math.cos(r)), (int) Math.round(7 - 6 * Math.sin(r)), foot);
        }
        for (int a = 0; a < 360; a += 90) {
            double r = Math.toRadians(a + 22);
            int x = (int) Math.round(8 + 6 * Math.cos(r)), y = (int) Math.round(7 - 6 * Math.sin(r));
            g.rect(x - 1, y, x + 1, y + 1, a % 180 == 0 ? cabin : cabin2);
        }
        g.ellipse(8f, 7f, 1.2f, 1.2f, GOLD);
        g.line(8, 7, 4, 15, foot);
        g.line(8, 7, 12, 15, foot);
        return g.outlined();
    }

    /** Lot truqué : un ours en peluche, une étiquette barrée. */
    static int[][] riggedPrize() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int fur = rgba("c8904a"), furDark = rgba("8a5a2a"), muzzle = rgba("f0d8b0");
        g.ellipse(4f, 3f, 2f, 2f, fur);
        g.ellipse(11f, 3f, 2f, 2f, furDark);
        g.ellipseShaded(7.5f, 6f, 4.5f, 4f, fur, furDark, 1.5f);
        g.ellipseShaded(7.5f, 12f, 5f, 3.5f, fur, furDark, 1.5f);
        g.ellipse(7.5f, 7.5f, 2f, 1.5f, muzzle);
        g.set(6, 5, rgba("1a1018")); g.set(9, 5, rgba("1a1018"));
        g.set(7, 7, rgba("1a1018"));
        g.rect(11, 9, 15, 13, WHITE);
        g.line(11, 9, 15, 13, RED);
        g.line(15, 9, 11, 13, RED);
        return g.outlined();
    }

    /** Jeu truqué : une cible et une fléchette plantée à côté du centre. */
    static int[][] riggedGame() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.ellipse(7f, 8f, 6.5f, 6.5f, RED);
        g.ellipse(7f, 8f, 4.5f, 4.5f, WHITE);
        g.ellipse(7f, 8f, 2.5f, 2.5f, RED);
        g.ellipse(7f, 8f, 0.8f, 0.8f, WHITE);
        g.line(10, 5, 15, 0, rgba("8a5a2a"));
        g.set(10, 5, rgba("b8c0cc"));
        g.rect(14, 0, 15, 1, rgba("2a8ad8"));
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Symboles des rouleaux ennemis : le Désert de Braise
    // -------------------------------------------------------------------------

    /** Péage : une barrière rayée qui barre le chemin, et une pièce. */
    static int[][] toll() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int post = rgba("8a5a2a");
        g.rect(1, 4, 2, 15, post);
        for (int x = 3; x <= 15; x++) for (int y = 5; y <= 6; y++) g.set(x, y, (x / 2) % 2 == 0 ? RED : WHITE);
        g.ellipseShaded(9f, 11.5f, 3.5f, 3.5f, GOLD, GOLD_DARK, 1f);
        g.rect(9, 10, 9, 13, GOLD_DARK);
        return g.outlined();
    }

    /** Lettre de change : un parchemin roulé, scellé de cire rouge. */
    static int[][] bill() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int paper = rgba("f0e2b8"), paperDark = rgba("c8b080"), ink = rgba("5a3a1e");
        g.rect(3, 2, 12, 13, paper);
        g.rect(10, 2, 12, 13, paperDark);
        g.ellipse(7.5f, 2f, 5.5f, 1.5f, paperDark);
        g.ellipse(7.5f, 14f, 5.5f, 1.5f, paperDark);
        for (int y = 4; y <= 9; y += 2) g.line(5, y, 10, y, ink);
        g.ellipseShaded(10f, 11f, 2.5f, 2.5f, RED, RED_DARK, 0.8f);
        g.set(9, 10, rgba("ff8a8a"));
        return g.outlined();
    }

    /** Bandelettes : une main momifiée, bandes croisées. */
    static int[][] wrap() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int linen = rgba("e8dcc0"), linenDark = rgba("b0a080"), gap = rgba("4a3a2a");
        g.rect(4, 6, 11, 15, linen);
        for (int x = 4; x <= 11; x += 2) g.rect(x, 1, x, 6, linen);
        g.rect(12, 7, 14, 9, linen);
        for (int y = 7; y <= 15; y += 2) g.line(4, y, 11, y - 1, linenDark);
        g.line(4, 12, 11, 15, linenDark);
        g.set(6, 9, gap); g.set(9, 11, gap);
        g.line(12, 13, 15, 15, linen); // bande qui pend
        return g.outlined();
    }

    /** Venin : une goutte verte qui tombe d'un dard de scorpion. */
    static int[][] venom() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int shell = rgba("2a1a12"), shellLight = rgba("6a4430"), green = rgba("6ae04a"), greenDark = rgba("2a8a2a");
        g.line(2, 1, 9, 1, shellLight);
        g.line(9, 1, 11, 3, shellLight);
        g.rect(1, 0, 3, 2, shell);
        g.triangle(10, 3, 13, 3, 11, 7, shell);
        g.ellipseShaded(10f, 12f, 3f, 3.2f, green, greenDark, 1f);
        g.triangle(8, 11, 12, 11, 10, 7, green);
        g.set(9, 11, rgba("d8ffb8"));
        return g.outlined();
    }

    /** Mirage : un palmier qui ondule au-dessus du sable brûlant. */
    static int[][] mirage() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int trunk = rgba("b07a4a"), leaf = rgba("6ad86a", 170), sand = rgba("f0c060"), heat = rgba("ffe8b0", 200);
        for (int y = 5; y <= 12; y++) g.set(8 + (int) Math.round(Math.sin(y / 1.5)), y, trunk);
        g.line(8, 4, 3, 6, leaf);
        g.line(8, 4, 13, 6, leaf);
        g.line(8, 4, 5, 1, leaf);
        g.line(8, 4, 12, 2, leaf);
        g.rect(0, 13, 15, 15, sand);
        for (int x = 0; x < 16; x++) {
            g.set(x, 9 + (int) Math.round(Math.sin(x / 2.0)), heat);
            if (x % 2 == 0) g.set(x, 12 + (int) Math.round(Math.sin(x / 2.0 + 1)), heat);
        }
        return g.pixels;
    }

    /** Brûlure : une flamme orange au cœur jaune. */
    static int[][] burn() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int orange = rgba("ff7a1e"), red = rgba("d8301e"), yellow = rgba("ffe04a");
        g.ellipse(8f, 11f, 5.5f, 4.5f, red);
        g.triangle(3, 10, 13, 10, 8, 0, red);
        g.triangle(2, 9, 6, 9, 3, 4, red);
        g.ellipse(8f, 11.5f, 4f, 3.5f, orange);
        g.triangle(5, 11, 11, 11, 8, 3, orange);
        g.ellipse(8f, 12.5f, 2f, 2f, yellow);
        g.triangle(7, 12, 9, 12, 8, 7, yellow);
        return g.outlined();
    }

    /** Souffle de feu : une tête de dragon de profil crache un jet de flammes. */
    static int[][] fireBreath() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int scale = rgba("a82a1e"), scaleDark = rgba("6a1a12"), horn = rgba("f0e2b8");
        int orange = rgba("ff7a1e"), yellow = rgba("ffe04a");
        g.ellipseShaded(3.5f, 6f, 3.5f, 3f, scale, scaleDark, 1.5f);
        g.rect(4, 6, 7, 9, scale);
        g.rect(4, 8, 7, 9, scaleDark);
        g.line(1, 3, 0, 0, horn);
        g.line(3, 3, 3, 0, horn);
        g.set(4, 5, yellow);
        g.triangle(8, 7, 15, 3, 15, 13, orange);
        g.triangle(8, 7, 15, 6, 15, 10, yellow);
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Entrées des donjons
    // -------------------------------------------------------------------------

    /**
     * Décor de l'entrée d'un donjon de la Fête foraine (un chapiteau rayé) ou du
     * Désert de Braise (des blocs de grès) ; la porte et l'écusson sont ceux de
     * {@link PlacesArt#gate}.
     *
     * @return les couleurs {plaque, plaque sombre} de l'écusson
     */
    static int[] gateDecor(PixelCanvas g, boolean fair) {
        if (fair) {
            int stripe = rgba("f6f0e2");
            for (int x = 0; x <= 25; x++) {
                int top = 12 - Math.max(0, 6 - Math.abs(13 - x));
                for (int y = top; y <= 25; y++) g.set(x, y, (x / 3) % 2 == 0 ? RED : stripe);
            }
            g.line(0, 12, 13, 6, GOLD);
            g.line(13, 6, 25, 12, GOLD);
            for (int x = 1; x <= 24; x += 4) g.set(x, 13, GOLD);
            return new int[] {rgba("2a5ad8"), rgba("1a3a8e")};
        }
        int sandstone = rgba("e0b070"), sandstoneDark = rgba("a87a40"), sand = rgba("f0c878");
        g.rect(1, 10, 24, 25, sandstone);
        g.rect(18, 10, 24, 25, sandstoneDark);
        g.triangle(0, 11, 25, 11, 13, 4, sandstoneDark);
        for (int y = 13; y <= 24; y += 4) g.line(1, y, 24, y, sandstoneDark);
        for (int y = 13; y <= 24; y += 8) for (int x = 6; x <= 20; x += 8) g.line(x, y, x, y + 4, sandstoneDark);
        g.rect(0, 24, 25, 25, sand);
        return new int[] {rgba("d8601e"), rgba("8a3a12")};
    }

    /** Emblème 7 x 7 des donjons de la Fête foraine et du Désert ('X' : plein), ou {@code null}. */
    static String[] emblem(Dungeon dungeon) {
        return switch (dungeon) {
            case CHAPITEAU     -> new String[] {"...X...", "..XXX..", ".XXXXX.", "XXXXXXX", ".X.X.X.", ".X...X.", ".XX.XX."}; // chapiteau
            case TRAIN_FANTOME -> new String[] {".XXXXX.", "XXXXXXX", "X.X.X.X", "XXXXXXX", "XXXXXXX", "XXXXXXX", "X.X.X.X"}; // fantôme
            case GRANDE_ROUE   -> new String[] {".XXXXX.", "X..X..X", "X.XXX.X", "XXX.XXX", "X.XXX.X", "X..X..X", ".XXXXX."}; // roue
            case STANDS        -> new String[] {".XXXXX.", "X.....X", "X.XXX.X", "X.X.X.X", "X.XXX.X", "X.....X", ".XXXXX."}; // cible
            case CARAVANE      -> new String[] {".......", "..XX...", ".XXXX.X", "XXXXXXX", "X.XXXX.", "X.X..X.", "X.X..X."}; // chameau
            case TOMBEAU       -> new String[] {"...X...", "..XXX..", "..XXX..", ".XXXXX.", ".XXXXX.", "XXXXXXX", "XXXXXXX"}; // pyramide
            case OASIS         -> new String[] {"XX.X.XX", "..XXX..", ".X.X.X.", "...X...", "...X...", "..X....", "XXXXXXX"}; // palmier
            case VOLCAN        -> new String[] {"..X.X..", "...X...", ".......", "..XXX..", ".XXXXX.", "XXXXXXX", "XXXXXXX"}; // volcan
            default            -> null;
        };
    }

    // -------------------------------------------------------------------------
    // Cartes des lieux
    // -------------------------------------------------------------------------

    /** Entrées des donjons de la Fête foraine (Chapiteau, Train fantôme, Grande Roue, Stands), centre de l'entrée. */
    static final int[][] FOIRE_GATES  = {{30, 28}, {74, 18}, {122, 26}, {130, 64}};
    /** Entrées des donjons du Désert de Braise (Caravane, Tombeau, Oasis, Volcan). */
    static final int[][] DESERT_GATES = {{26, 58}, {50, 22}, {104, 54}, {136, 22}};

    /**
     * La Fête foraine, la nuit : l'herbe sombre, des guirlandes d'ampoules, un
     * chapiteau, la grande roue, des stands et des barbes à papa.
     */
    static int[][] foire() {
        PixelCanvas g = new PixelCanvas(MW, MH);
        Random random = new Random(51);
        int grass = rgba("24402a"), grassDark = rgba("1a3020"), grassLight = rgba("2e5236");
        for (int y = 0; y < MH; y++) {
            for (int x = 0; x < MW; x++) {
                int roll = random.nextInt(12);
                g.set(x, y, roll == 0 ? grassDark : roll == 1 ? grassLight : grass);
            }
        }
        // Le ciel de nuit en haut, et ses étoiles.
        for (int y = 0; y < 8; y++) for (int x = 0; x < MW; x++) g.set(x, y, rgba("10142e"));
        for (int i = 0; i < 18; i++) g.set(random.nextInt(MW), random.nextInt(7), rgba("fff2a0"));
        // La grande roue, en fond à droite.
        int steel = rgba("8a97c8");
        for (int a = 0; a < 360; a += 3) {
            double r = Math.toRadians(a);
            g.set((int) Math.round(122 + 16 * Math.cos(r)), (int) Math.round(10 - 9 * Math.sin(r)), steel);
        }
        for (int a = 0; a < 360; a += 30) {
            double r = Math.toRadians(a);
            g.line(122, 10, (int) Math.round(122 + 16 * Math.cos(r)), (int) Math.round(10 - 9 * Math.sin(r)), rgba("5a6494"));
            g.rect((int) Math.round(122 + 16 * Math.cos(r)) - 1, (int) Math.round(10 - 9 * Math.sin(r)),
                (int) Math.round(122 + 16 * Math.cos(r)) + 1, (int) Math.round(10 - 9 * Math.sin(r)) + 1,
                a % 60 == 0 ? rgba("e86a8a") : rgba("ffd54a"));
        }
        // Chapiteau rayé, à gauche.
        for (int x = 14; x <= 46; x++) {
            int top = 20 - Math.max(0, 12 - Math.abs(30 - x));
            for (int y = top; y <= 22; y++) g.set(x, y, ((x - 14) / 4) % 2 == 0 ? RED : rgba("f6f0e2"));
        }
        g.rect(30, 4, 30, 8, rgba("b8c0cc"));
        g.triangle(31, 4, 31, 7, 35, 5, GOLD);
        // Stands à auvent, en bas à droite.
        for (int[] s : new int[][] {{110, 72}, {126, 76}, {142, 70}}) {
            g.rect(s[0], s[1], s[0] + 12, s[1] + 8, rgba("8a5a2a"));
            for (int x = s[0] - 1; x <= s[0] + 13; x++) g.rect(x, s[1] - 3, x, s[1], ((x - s[0]) / 2) % 2 == 0 ? rgba("2a8ad8") : WHITE);
            g.set(s[0] + 3, s[1] + 3, GOLD);
            g.set(s[0] + 8, s[1] + 3, rgba("e86a8a"));
        }
        // Le train fantôme : des rails et une façade sombre.
        g.rect(62, 8, 86, 18, rgba("2a1a3a"));
        g.triangle(60, 9, 88, 9, 74, 2, rgba("3a2a4a"));
        g.rect(66, 12, 67, 13, rgba("6ad86a"));
        g.rect(81, 12, 82, 13, rgba("6ad86a"));
        for (int x = 40; x <= 96; x += 2) g.set(x, 34 + (int) Math.round(2 * Math.sin(x / 7.0)), rgba("b8c0cc"));
        // Barbes à papa et ballons.
        for (int[] b : new int[][] {{12, 60}, {50, 70}, {92, 44}, {150, 40}}) {
            g.ellipse(b[0], b[1], 2.5f, 2.5f, rgba("ff9ad8"));
            g.line(b[0], b[1] + 2, b[0], b[1] + 6, WHITE);
        }
        // Chemins de terre battue vers chaque donjon.
        int dirt = rgba("a88a5a"), dirtDark = rgba("7a6040");
        int[] fork = {78, 52};
        ExplorationArt.path(g, 70, 95, fork[0], fork[1], dirt, dirtDark);
        for (int[] gate : FOIRE_GATES) ExplorationArt.path(g, fork[0], fork[1], gate[0], gate[1] + 6, dirt, dirtDark);
        // Guirlandes d'ampoules au-dessus des chemins.
        int[] bulbs = {rgba("ffd54a"), rgba("ff6a4a"), rgba("6ad8ff"), rgba("ff9ad8")};
        for (int x = 0; x < MW; x += 3) {
            int y = 46 + (int) Math.round(4 * Math.abs(Math.sin(x / 26.0 * Math.PI)));
            g.set(x, y, bulbs[(x / 3) % bulbs.length]);
        }
        return g.pixels;
    }

    /**
     * Le Désert de Braise : des dunes de sable, une caravane, une pyramide, une
     * oasis bleue, et le volcan qui fume en haut à droite, sa lave qui coule.
     */
    static int[][] desert() {
        PixelCanvas g = new PixelCanvas(MW, MH);
        Random random = new Random(61);
        int sand = rgba("e8b86a"), sandDark = rgba("d0a058"), sandLight = rgba("f4cc84");
        for (int y = 0; y < MH; y++) {
            for (int x = 0; x < MW; x++) {
                int roll = random.nextInt(12);
                g.set(x, y, roll == 0 ? sandDark : roll == 1 ? sandLight : sand);
            }
        }
        // Le ciel rouge du soir en haut.
        for (int y = 0; y < 6; y++) for (int x = 0; x < MW; x++) g.set(x, y, y < 3 ? rgba("8a2a1e") : rgba("c8501e"));
        // Les dunes : des ombres en vagues.
        for (int band = 0; band < 6; band++) {
            int base = 16 + band * 14;
            for (int x = 0; x < MW; x++) {
                int y = base + (int) Math.round(3 * Math.sin((x + band * 23) / 11.0));
                g.set(x, y, sandDark);
                g.set(x, y + 1, rgba("c08c48"));
            }
        }
        // Le volcan, la lave et la fumée.
        g.triangle(112, 30, 160, 30, 138, 2, rgba("4a2a22"));
        g.triangle(126, 30, 160, 30, 142, 4, rgba("3a1e18"));
        g.rect(134, 2, 142, 4, rgba("ff7a1e"));
        for (int y = 4; y < 30; y++) g.set(138 + (int) Math.round(3 * Math.sin(y / 4.0)), y, rgba("ff9a2a"));
        for (int i = 0; i < 10; i++) g.ellipse(132 + i * 1.5f, -i * 0.5f + 1f, 2f, 1.5f, rgba("6a6a78", 160));
        // La pyramide.
        g.triangle(34, 22, 66, 22, 50, 4, rgba("e0b070"));
        g.triangle(50, 22, 66, 22, 50, 4, rgba("a87a40"));
        for (int y = 8; y <= 20; y += 4) g.line(50 - (y - 4) * 16 / 18, y, 50, y, rgba("c89858"));
        // L'oasis : un étang bleu et des palmiers.
        g.ellipse(104f, 70f, 14f, 6f, rgba("2a8ad8"));
        g.ellipse(102f, 69f, 8f, 3f, rgba("6ac0f0"));
        for (int[] p : new int[][] {{88, 62}, {120, 64}, {112, 76}}) {
            g.line(p[0], p[1], p[0] + 1, p[1] - 8, rgba("8a5a2a"));
            g.line(p[0] + 1, p[1] - 8, p[0] - 4, p[1] - 6, rgba("3a9a3a"));
            g.line(p[0] + 1, p[1] - 8, p[0] + 6, p[1] - 6, rgba("3a9a3a"));
            g.line(p[0] + 1, p[1] - 8, p[0] + 2, p[1] - 12, rgba("3a9a3a"));
        }
        // La caravane : trois chameaux et leurs ballots.
        for (int c = 0; c < 3; c++) {
            int x = 10 + c * 10, y = 72;
            g.ellipse(x, y, 3.5f, 2f, rgba("a8784a"));
            g.ellipse(x - 1, y - 2, 1.5f, 1.5f, rgba("a8784a"));
            g.line(x + 3, y - 1, x + 5, y - 4, rgba("a8784a"));
            g.rect(x - 2, y + 2, x - 2, y + 4, rgba("8a5a2a"));
            g.rect(x + 2, y + 2, x + 2, y + 4, rgba("8a5a2a"));
            g.rect(x - 1, y - 4, x + 1, y - 3, c == 1 ? RED : rgba("2a5ad8"));
        }
        // Squelettes et cailloux brûlants.
        for (int i = 0; i < 16; i++) g.set(random.nextInt(MW), 30 + random.nextInt(60), rgba("8a6a4a"));
        // Pistes de sable tassé vers chaque donjon.
        int dirt = rgba("c89858"), dirtDark = rgba("a87a40");
        int[] fork = {80, 46};
        ExplorationArt.path(g, 70, 95, fork[0], fork[1], dirt, dirtDark);
        for (int[] gate : DESERT_GATES) ExplorationArt.path(g, fork[0], fork[1], gate[0], gate[1] + 6, dirt, dirtDark);
        return g.pixels;
    }
}
