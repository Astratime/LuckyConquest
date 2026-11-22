package fr.astratime.lucky.assets;

import fr.astratime.lucky.entities.enemy.EnemyKind;

import java.util.Random;

import static fr.astratime.lucky.assets.PixelCanvas.rgba;

/**
 * Dessins en pixel art du chapitre 3 de la Tour, « Les Pèlerins de la Comète » (Tour à 10 chapitres, 2026-11-22) :
 * les portraits de ses cinq ennemis (en buste, à la taille du croupier), les symboles de leurs rouleaux (16 x 16) et
 * l'illustration du chapitre.
 */
public final class PilgrimsArt {

    private static final int W = EnemyPortraits.WIDTH, H = EnemyPortraits.HEIGHT;

    private static final int SKIN = rgba("e8b48a"), SKIN_SHADE = rgba("b8845e"), OLD_SKIN = rgba("d8a88a");
    private static final int EYE = rgba("1a1018"), MOUTH = rgba("6a2a20"), WHITE = rgba("f6f0e2"), WHITE_SHADE = rgba("c8c0b4");
    private static final int GOLD = rgba("ffd54a"), GOLD_DARK = rgba("c4891e"), GOLD_LIGHT = rgba("fff6c8");
    private static final int IRON = rgba("8a94a4"), IRON_DARK = rgba("525a68"), IRON_LIGHT = rgba("c4ccd8");
    private static final int CLOAK = rgba("5a3e2a"), CLOAK_DARK = rgba("3a2618"), ROPE = rgba("b8945a");
    private static final int VIOLET = rgba("7a4ab8"), VIOLET_DARK = rgba("4a2a78"), CYAN = rgba("7ae8ff");
    private static final int WOOD = rgba("7a5232"), WOOD_DARK = rgba("4a3020"), RED = rgba("c8202c");
    private static final int ROBE = rgba("f0e6cc"), ROBE_SHADE = rgba("c8b890"), NIGHT = rgba("140e24");

    private PilgrimsArt() {}

    /** @return le portrait de l'ennemi {@code kind}, ou {@code null} s'il n'est pas du chapitre 3. */
    static int[][] of(EnemyKind kind) {
        return switch (kind) {
            case PASSEUR     -> passeur();
            case PELERIN_FER -> pelerinFer();
            case DISEUSE     -> diseuse();
            case PORTEUR     -> porteur();
            case PROPHETE    -> prophete();
            default          -> null;
        };
    }

    // -------------------------------------------------------------------------
    // Gabarit commun (comme ManorPortraits)
    // -------------------------------------------------------------------------

    private static void bust(PixelCanvas g, int coat, int coatDark, int collar) {
        g.rect(4, 29, 29, 41, coat);
        g.rect(20, 29, 29, 41, coatDark);
        g.ellipseShaded(7f, 31f, 4.5f, 3.5f, coat, coatDark, 3f);
        g.ellipseShaded(27f, 31f, 4.5f, 3.5f, coatDark, coatDark, 0f);
        if (collar != 0) g.triangle(13, 28, 21, 28, 17, 35, collar);
    }

    private static void face(PixelCanvas g, int skin, int skinShade) {
        g.rect(14, 24, 20, 28, skinShade);
        g.ellipseShaded(17f, 16f, 7f, 8.5f, skin, skinShade, 2f);
        g.rect(13, 15, 14, 16, EYE);
        g.rect(20, 15, 21, 16, EYE);
        g.set(17, 18, skinShade);
        g.line(15, 21, 19, 21, MOUTH);
    }

    private static void glowingEyes(PixelCanvas g, int glow) {
        g.rect(13, 15, 14, 16, glow);
        g.rect(20, 15, 21, 16, glow);
        g.set(13, 15, WHITE);
        g.set(20, 15, WHITE);
    }

    // -------------------------------------------------------------------------
    // Portraits
    // -------------------------------------------------------------------------

    /**
     * Le Passeur du Gué : un vieux batelier sous un grand capuchon brun, une longue perche sur l'épaule, une lanterne
     * au bout, et une pièce d'or qu'il réclame entre deux doigts.
     */
    private static int[][] passeur() {
        PixelCanvas g = new PixelCanvas(W, H);
        // La perche, en travers derrière lui, et sa lanterne.
        g.line(1, 4, 31, 40, WOOD_DARK);
        g.line(2, 4, 32, 40, WOOD);
        g.rect(0, 0, 4, 3, IRON_DARK);
        g.rect(1, 1, 3, 3, GOLD);
        bust(g, CLOAK, CLOAK_DARK, 0);
        g.line(9, 30, 25, 40, ROPE);                          // la corde du bac en bandoulière
        face(g, OLD_SKIN, SKIN_SHADE);
        g.line(12, 13, 15, 14, WHITE_SHADE);                  // sourcils blancs broussailleux
        g.line(19, 14, 22, 13, WHITE_SHADE);
        g.line(15, 21, 19, 21, MOUTH);
        g.rect(13, 22, 21, 25, WHITE_SHADE);                  // la barbe courte
        g.rect(15, 21, 19, 21, MOUTH);
        // Le grand capuchon.
        g.ellipseShaded(17f, 11f, 10f, 9f, CLOAK, CLOAK_DARK, 3f);
        g.ellipse(17f, 16f, 7.5f, 8f, 0);
        face(g, OLD_SKIN, SKIN_SHADE);
        g.rect(13, 22, 21, 25, WHITE_SHADE);
        g.line(15, 22, 19, 22, MOUTH);
        g.line(12, 13, 15, 14, WHITE);
        g.line(19, 14, 22, 13, WHITE);
        g.rect(7, 12, 9, 28, CLOAK);
        g.rect(25, 12, 27, 28, CLOAK_DARK);
        // La main tendue, une pièce d'or au bout des doigts.
        g.rect(27, 30, 31, 33, OLD_SKIN);
        g.ellipse(31f, 28f, 2f, 2f, GOLD);
        g.set(31, 28, GOLD_LIGHT);
        return g.outlined();
    }

    /**
     * Le Pèlerin de fer : un heaume fermé à fente, une armure cabossée par la route, l'emblème de la comète peint sur
     * le plastron, le bâton de pèlerin et sa gourde.
     */
    private static int[][] pelerinFer() {
        PixelCanvas g = new PixelCanvas(W, H);
        g.rect(30, 6, 31, 41, WOOD);                          // le bâton
        g.ellipse(30.5f, 5f, 2.2f, 2f, WOOD_DARK);
        g.ellipse(29f, 12f, 1.8f, 2.5f, rgba("c89a4a"));     // la gourde
        bust(g, IRON, IRON_DARK, 0);
        for (int x = 6; x <= 28; x += 4) g.set(x, 32, IRON_LIGHT); // les rivets
        // L'emblème de la comète sur le plastron.
        g.ellipse(16f, 36f, 2.5f, 2.5f, GOLD);
        g.line(18, 34, 23, 30, GOLD_DARK);
        g.line(18, 35, 22, 32, GOLD);
        // Le heaume.
        g.rect(14, 24, 20, 28, IRON_DARK);
        g.ellipseShaded(17f, 15f, 8f, 9.5f, IRON, IRON_DARK, 2f);
        g.rect(10, 14, 24, 16, NIGHT);                        // la fente
        g.rect(12, 15, 13, 15, CYAN);                         // une lueur au fond
        g.rect(21, 15, 22, 15, CYAN);
        g.line(17, 6, 17, 26, IRON_LIGHT);                    // l'arête
        for (int y = 19; y <= 24; y += 2) { g.set(14, y, NIGHT); g.set(20, y, NIGHT); } // les trous d'air
        g.set(11, 10, IRON_LIGHT); g.set(12, 9, IRON_LIGHT);
        // Les bosses de la route.
        g.set(9, 34, IRON_DARK); g.set(24, 37, IRON_DARK);
        return g.outlined();
    }

    /**
     * La Diseuse d'éclats : un voile violet constellé, des anneaux d'or aux oreilles, des yeux qui luisent, et un éclat de
     * comète qu'elle tient levé comme une boule de cristal.
     */
    private static int[][] diseuse() {
        PixelCanvas g = new PixelCanvas(W, H);
        bust(g, VIOLET, VIOLET_DARK, GOLD);
        g.ellipse(17f, 9f, 9f, 6f, VIOLET);                    // le voile
        g.rect(8, 9, 10, 30, VIOLET);
        g.rect(24, 9, 26, 30, VIOLET_DARK);
        face(g, SKIN, SKIN_SHADE);
        glowingEyes(g, CYAN);
        g.line(15, 21, 19, 21, MOUTH);
        g.rect(10, 9, 24, 10, GOLD);                           // le bandeau de pièces
        for (int x = 11; x <= 23; x += 3) g.set(x, 11, GOLD_DARK);
        g.ellipse(9f, 20f, 1.2f, 1.6f, GOLD);                  // les anneaux
        g.ellipse(25f, 20f, 1.2f, 1.6f, GOLD);
        Random random = new Random(3);
        for (int i = 0; i < 6; i++) g.set(5 + random.nextInt(24), 31 + random.nextInt(10), GOLD_LIGHT); // étoiles du voile
        // L'éclat de comète qui luit dans ses mains.
        g.rect(9, 34, 25, 37, SKIN);
        g.triangle(13, 34, 21, 34, 17, 26, CYAN);
        g.triangle(14, 34, 20, 34, 17, 30, rgba("c8faff"));
        g.set(16, 31, WHITE);
        return g.outlined();
    }

    /**
     * Le Porteur de reliques : un moine au crâne rasé, robe de bure, qui porte sur son dos une armature de bois pleine de
     * reliquaires dorés (un éclat, une main, une petite comète), attachée par des cordes.
     */
    private static int[][] porteur() {
        PixelCanvas g = new PixelCanvas(W, H);
        // L'armature, qui dépasse au-dessus de ses épaules, et ses reliquaires.
        g.rect(2, 2, 3, 30, WOOD);
        g.rect(30, 2, 31, 30, WOOD_DARK);
        g.rect(2, 2, 31, 3, WOOD);
        g.rect(2, 9, 31, 9, WOOD_DARK);
        g.rect(4, 4, 9, 8, GOLD);  g.ellipse(6.5f, 6f, 1.2f, 1.2f, CYAN);       // l'éclat
        g.rect(24, 4, 29, 8, GOLD); g.rect(26, 5, 27, 7, SKIN);                  // la main
        g.rect(4, 11, 8, 16, GOLD_DARK); g.set(6, 13, RED);
        g.rect(25, 11, 29, 16, GOLD); g.set(27, 13, GOLD_LIGHT); g.line(25, 15, 28, 12, GOLD_DARK); // la comète
        bust(g, CLOAK, CLOAK_DARK, 0);
        g.line(6, 29, 14, 40, ROPE);                           // les cordes de l'armature
        g.line(28, 29, 20, 40, ROPE);
        face(g, SKIN, SKIN_SHADE);
        g.line(15, 21, 19, 21, MOUTH);
        g.ellipse(17f, 8.5f, 6f, 2f, SKIN_SHADE);              // le crâne rasé et sa tonsure
        g.set(14, 9, rgba("f8d0a8")); g.set(15, 8, rgba("f8d0a8"));
        g.line(12, 13, 15, 13, CLOAK_DARK);
        g.line(19, 13, 22, 13, CLOAK_DARK);
        g.rect(14, 29, 20, 31, CLOAK_DARK);                    // le capuchon rabattu
        g.ellipse(17f, 36f, 1.5f, 1.5f, GOLD);                 // un pendentif
        return g.outlined();
    }

    /**
     * Le Prophète du Cratère : une longue robe blanche et or, une barbe blanche, des yeux dorés qui brûlent, une comète
     * qui tourne au-dessus de sa tête comme une auréole, et son bâton surmonté d'un éclat.
     */
    private static int[][] prophete() {
        PixelCanvas g = new PixelCanvas(W, H);
        // L'auréole : une comète qui fait le tour de sa tête.
        g.ellipse(17f, 5f, 9f, 2.5f, GOLD_DARK);
        g.ellipse(17f, 5f, 7.5f, 1.4f, 0);
        g.ellipse(26f, 5f, 2.2f, 2.2f, GOLD);
        g.set(26, 4, GOLD_LIGHT);
        // Le bâton et son éclat.
        g.rect(1, 8, 2, 41, WOOD);
        g.triangle(0, 8, 4, 8, 2, 1, CYAN);
        g.set(2, 5, WHITE);
        bust(g, ROBE, ROBE_SHADE, GOLD);
        g.rect(15, 30, 19, 41, GOLD);                          // l'étole dorée
        g.rect(16, 30, 18, 41, GOLD_DARK);
        g.ellipse(17f, 35f, 1.5f, 1.5f, GOLD_LIGHT);
        face(g, OLD_SKIN, SKIN_SHADE);
        glowingEyes(g, GOLD);
        g.line(12, 13, 15, 12, WHITE);                         // sourcils levés
        g.line(19, 12, 22, 13, WHITE);
        // La grande barbe blanche, et la bouche ouverte qui prêche.
        g.triangle(11, 20, 23, 20, 17, 33, WHITE);
        g.rect(12, 20, 22, 25, WHITE);
        g.rect(19, 21, 22, 30, WHITE_SHADE);
        g.rect(15, 21, 19, 22, MOUTH);
        // Les cheveux blancs longs.
        g.ellipse(17f, 9f, 7.5f, 3f, WHITE);
        g.rect(9, 10, 10, 24, WHITE);
        g.rect(24, 10, 25, 24, WHITE_SHADE);
        // La main levée vers le ciel.
        g.rect(28, 20, 31, 29, ROBE_SHADE);
        g.rect(28, 16, 31, 19, OLD_SKIN);
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Symboles des rouleaux ennemis
    // -------------------------------------------------------------------------

    /** Serment : un écu de fer riveté, une comète d'or gravée dessus. */
    static int[][] ironVow() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.rect(2, 1, 13, 8, IRON);
        g.triangle(2, 8, 13, 8, 8, 15, IRON);
        g.rect(9, 1, 13, 8, IRON_DARK);
        g.triangle(8, 8, 13, 8, 8, 15, IRON_DARK);
        g.ellipse(7f, 7f, 1.8f, 1.8f, GOLD);
        g.line(8, 6, 11, 3, GOLD_DARK);
        for (int[] rivet : new int[][] {{3, 2}, {12, 2}, {3, 7}, {12, 7}}) g.set(rivet[0], rivet[1], IRON_LIGHT);
        return g.outlined();
    }

    /** Voyance : un éclat de cristal qui luit, un oeil ouvert au milieu. */
    static int[][] sight() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.triangle(2, 9, 13, 9, 8, 0, CYAN);
        g.triangle(2, 9, 13, 9, 8, 15, rgba("4ab8d8"));
        g.ellipse(8f, 9f, 3.5f, 2f, WHITE);
        g.ellipse(8f, 9f, 1.4f, 1.4f, VIOLET);
        g.set(8, 9, NIGHT);
        g.set(6, 4, WHITE);
        return g.outlined();
    }

    /** Relique : un reliquaire doré en forme de chapelle, une petite comète derrière sa vitre. */
    static int[][] relic() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.rect(2, 6, 13, 14, GOLD);
        g.rect(10, 6, 13, 14, GOLD_DARK);
        g.triangle(1, 6, 14, 6, 8, 1, GOLD);
        g.rect(7, 0, 8, 1, GOLD_LIGHT);
        g.rect(4, 8, 11, 12, NIGHT);                           // la vitre
        g.ellipse(6f, 11f, 1.2f, 1.2f, CYAN);
        g.line(7, 10, 10, 8, rgba("4ab8d8"));
        g.rect(1, 14, 14, 15, GOLD_DARK);
        return g.outlined();
    }

    /** Pèlerinage : un bâton de pèlerin avec sa gourde, et des traces de pas qui montent vers une étoile. */
    static int[][] pilgrimage() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.line(3, 15, 6, 1, WOOD);
        g.line(4, 15, 7, 1, WOOD_DARK);
        g.ellipse(7f, 4f, 1.8f, 2.4f, rgba("c89a4a"));         // la gourde
        g.line(6, 2, 8, 6, ROPE);
        for (int[] step : new int[][] {{10, 14}, {12, 11}, {10, 8}, {12, 5}}) g.rect(step[0], step[1], step[0] + 1, step[1] + 1, ROBE_SHADE);
        g.ellipse(13f, 1.5f, 1.6f, 1.4f, GOLD);
        g.set(13, 1, GOLD_LIGHT);
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Illustration du chapitre 3
    // -------------------------------------------------------------------------

    /**
     * Chapitre 3, « Les Pèlerins de la Comète » : une route de nuit qui serpente vers le cratère, une longue file de
     * pèlerins qui portent des lanternes, et au loin la lueur dorée du cratère sous la queue d'une comète.
     */
    static int[][] pilgrims() {
        int w = EnemyPortraits.ART_WIDTH, h = EnemyPortraits.ART_HEIGHT;
        PixelCanvas g = new PixelCanvas(w, h);
        int[] sky = {rgba("0c0a1e"), rgba("141030"), rgba("1e1640"), rgba("2a1c48"), rgba("3a2448")};
        for (int y = 0; y < h; y++) {
            int band = Math.min(sky.length - 1, y * sky.length / 40);
            for (int x = 0; x < w; x++) g.set(x, y, sky[band]);
        }
        Random random = new Random(11);
        for (int i = 0; i < 50; i++) g.set(random.nextInt(w), random.nextInt(28), random.nextBoolean() ? WHITE : rgba("c8d0ff"));
        // La comète et sa queue.
        g.line(30, 4, 90, 16, rgba("6a5a8a"));
        g.line(40, 5, 90, 15, rgba("a898c8"));
        g.line(60, 10, 91, 16, GOLD_LIGHT);
        g.ellipse(93f, 17f, 2.5f, 2.2f, GOLD);
        g.set(93, 16, WHITE);
        // La lueur du cratère, à l'horizon, et les collines.
        g.ellipse(96f, 40f, 30f, 10f, rgba("6a4416"));
        g.ellipse(96f, 40f, 20f, 6f, rgba("a8701e"));
        g.ellipse(96f, 40f, 10f, 3f, rgba("ffd54a"));
        g.rect(0, 40, w - 1, h - 1, rgba("1a1414"));
        for (int x = 0; x < w; x++) {
            int top = 38 + (int) Math.round(Math.sin(x * 0.09) * 2 + Math.sin(x * 0.23) * 1.2);
            if (x > 80 && x < 112) top = 41;
            g.rect(x, top, x, 44, rgba("221a1a"));
        }
        // La route qui serpente du premier plan vers le cratère.
        for (int y = 42; y < h; y++) {
            float u = (y - 42) / (float) (h - 42);
            float cx = 96 - u * 60 + (float) Math.sin(u * 5) * 10;
            float half = 2 + u * 14;
            g.rect(Math.round(cx - half), y, Math.round(cx + half), y, rgba("3a2e28"));
            g.set(Math.round(cx - half), y, rgba("2a2020"));
        }
        // La file des pèlerins, de plus en plus petits vers le cratère, chacun avec sa lanterne.
        for (int k = 0; k < 14; k++) {
            float u = 1f - k / 14f;
            int y = 42 + Math.round(u * (h - 46));
            float cx = 96 - u * 60 + (float) Math.sin(u * 5) * 10 + (k % 2 == 0 ? -3 : 3) * u;
            int size = Math.max(1, Math.round(1 + u * 5));
            int x = Math.round(cx);
            g.rect(x - size / 2, y - size * 2, x + size / 2, y, k % 3 == 0 ? CLOAK : ROBE_SHADE);
            g.rect(x - size / 2, y - size * 2 - Math.max(1, size / 2), x + size / 2, y - size * 2, k % 3 == 0 ? CLOAK_DARK : ROBE);
            g.set(x + size / 2 + 1, y - size, GOLD);
            if (size >= 3) g.set(x + size / 2 + 1, y - size - 1, GOLD_LIGHT);
        }
        // Le Prophète en tête, presque au cratère : une silhouette blanche et son auréole.
        g.rect(85, 36, 86, 40, WHITE);
        g.set(85, 35, GOLD);
        g.set(86, 35, GOLD);
        return g.pixels;
    }
}
