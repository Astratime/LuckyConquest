package fr.astratime.lucky.assets;

import fr.astratime.lucky.entities.enemy.EnemyKind;

import static fr.astratime.lucky.assets.PixelCanvas.rgba;

/**
 * Portraits en pixel art des ennemis du Palace des Neiges, en buste, à la taille
 * du croupier (voir {@link PlacesPortraits}). Tous sont un peu givrés : cent ans
 * dans la neige, ça laisse des traces.
 */
public final class PalacePortraits {

    private static final int W = EnemyPortraits.WIDTH, H = EnemyPortraits.HEIGHT;

    private static final int SKIN = rgba("f0d0b8"), SKIN_SHADE = rgba("c8a088");
    private static final int FROZEN = rgba("dcecf8"), FROZEN_SHADE = rgba("9ab8d4");
    private static final int EYE = rgba("1a1018"), MOUTH = rgba("7a2a30"), WHITE = rgba("f6f8ff"), WHITE_SHADE = rgba("c8d0e0");
    private static final int GOLD = rgba("ffd54a"), GOLD_DARK = rgba("c4891e");
    private static final int RED = rgba("c8203a"), RED_DARK = rgba("7a1028");
    private static final int ICE = rgba("bfe8ff"), ICE_DARK = rgba("6aa8d8"), ICE_DEEP = rgba("2a5a8a");
    private static final int COAL = rgba("22222e"), STEEL_DARK = rgba("8a97a8");

    private PalacePortraits() {}

    /** @return les pixels du portrait de l'ennemi {@code kind}, ou {@code null} s'il n'est pas du Palace. */
    public static int[][] pixels(EnemyKind kind) { return of(kind); }

    /** @return le portrait de l'ennemi {@code kind}, ou {@code null} s'il n'est pas du Palace des Neiges. */
    static int[][] of(EnemyKind kind) {
        return switch (kind) {
            case GROOM             -> groom();
            case CONCIERGE         -> concierge();
            case PATINEUSE         -> patineuse();
            case IMPERATRICE_GIVRE -> imperatrice();
            case LIFTIER           -> liftier();
            case YETI              -> yeti();
            case MAJORDOME         -> majordome();
            case PRINCE_NEIGES     -> prince();
            default                -> null;
        };
    }

    // -------------------------------------------------------------------------
    // Gabarit commun (comme ExpressPortraits)
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

    /** Du givre sur les épaules : quelques pixels blancs. */
    private static void frost(PixelCanvas g) {
        for (int[] p : new int[][] {{5, 29}, {7, 28}, {9, 29}, {25, 29}, {27, 28}, {29, 30}, {6, 33}, {28, 34}}) g.set(p[0], p[1], WHITE);
    }

    // -------------------------------------------------------------------------
    // Le Hall
    // -------------------------------------------------------------------------

    /** Groom : livrée rouge à boutons dorés, calot rond sur le côté, sourire figé, une clé à la main. */
    private static int[][] groom() {
        PixelCanvas g = new PixelCanvas(W, H);
        bust(g, RED, RED_DARK, 0);
        for (int y = 31; y <= 40; y += 3) { g.set(14, y, GOLD); g.set(20, y, GOLD); }
        g.rect(13, 27, 21, 28, GOLD_DARK);                     // le col galonné
        frost(g);
        face(g, SKIN, SKIN_SHADE);
        g.line(14, 21, 20, 21, MOUTH);                          // le sourire figé
        g.set(14, 20, MOUTH); g.set(20, 20, MOUTH);
        g.ellipse(17f, 9f, 7f, 3f, rgba("6a3a22"));             // les cheveux
        // Le calot rond, posé de travers, et sa jugulaire.
        g.rect(16, 2, 25, 6, RED);
        g.rect(16, 6, 25, 7, GOLD_DARK);
        g.set(20, 1, GOLD);
        g.line(25, 7, 24, 18, GOLD_DARK);
        // La clé, tenue à droite.
        g.ellipse(30f, 21f, 2f, 2f, GOLD);
        g.set(30, 21, 0);
        g.line(30, 23, 30, 30, GOLD_DARK);
        g.set(31, 28, GOLD_DARK); g.set(31, 30, GOLD_DARK);
        return g.outlined();
    }

    /**
     * Le Concierge : redingote noire, gilet bordeaux, les clés d'or croisées au revers, favoris gris
     * couverts de givre, monocle.
     */
    private static int[][] concierge() {
        PixelCanvas g = new PixelCanvas(W, H);
        bust(g, COAL, rgba("12121a"), WHITE);
        g.triangle(14, 29, 20, 29, 17, 36, RED_DARK);           // le gilet
        g.line(6, 31, 10, 35, GOLD);                             // les clés croisées au revers
        g.line(10, 31, 6, 35, GOLD);
        frost(g);
        face(g, FROZEN, FROZEN_SHADE);
        g.ellipse(10.5f, 19f, 2f, 4f, rgba("b8c0cc"));          // les favoris gris
        g.ellipse(23.5f, 19f, 2f, 4f, rgba("b8c0cc"));
        g.ellipse(17f, 9f, 7f, 2.5f, rgba("b8c0cc"));
        g.ellipse(20.5f, 15.5f, 2f, 2f, GOLD);                  // le monocle
        g.rect(20, 15, 21, 16, EYE);
        g.line(22, 17, 24, 26, GOLD_DARK);
        g.line(14, 21, 20, 21, MOUTH);
        g.set(12, 6, WHITE); g.set(23, 7, WHITE);               // du givre dans les cheveux
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // La Patinoire
    // -------------------------------------------------------------------------

    /** Patineuse : tenue bleu pâle à paillettes, chignon, cache-oreilles blancs, joues roses. */
    private static int[][] patineuse() {
        PixelCanvas g = new PixelCanvas(W, H);
        int dress = rgba("8ac8f0"), dressDark = rgba("4a8ac0");
        bust(g, dress, dressDark, 0);
        for (int[] p : new int[][] {{8, 33}, {12, 37}, {16, 32}, {22, 36}, {26, 33}, {19, 39}}) g.set(p[0], p[1], WHITE);
        g.rect(14, 27, 20, 28, WHITE);
        face(g, SKIN, SKIN_SHADE);
        g.ellipse(12f, 19f, 1.5f, 1f, rgba("ff9ab0"));          // les joues roses
        g.ellipse(22f, 19f, 1.5f, 1f, rgba("ff9ab0"));
        g.ellipse(17f, 20.8f, 1.5f, 0.8f, RED);
        g.ellipse(17f, 9f, 7.5f, 3.5f, rgba("d8a040"));         // les cheveux blonds et le chignon
        g.ellipse(17f, 4f, 3f, 2.5f, rgba("d8a040"));
        g.ellipse(9.5f, 15f, 2f, 2.5f, WHITE);                   // les cache-oreilles
        g.ellipse(24.5f, 15f, 2f, 2.5f, WHITE);
        g.line(10, 12, 24, 12, WHITE_SHADE);
        // Une lame de patin, brandie à droite.
        g.line(28, 14, 28, 26, STEEL_DARK);
        g.rect(29, 14, 31, 22, WHITE);
        return g.outlined();
    }

    /**
     * L'Impératrice du Givre : robe de glace, haute couronne de stalactites, longs cheveux blancs, peau
     * bleutée, yeux qui luisent d'un bleu pâle, un sceptre de glace.
     */
    private static int[][] imperatrice() {
        PixelCanvas g = new PixelCanvas(W, H);
        bust(g, ICE_DARK, ICE_DEEP, ICE);
        g.rect(4, 36, 29, 41, ICE_DEEP);
        for (int x = 5; x <= 28; x += 3) g.set(x, 34, WHITE);
        g.ellipse(9f, 22f, 3f, 9f, WHITE);                       // les longs cheveux blancs
        g.ellipse(25f, 22f, 3f, 9f, WHITE_SHADE);
        face(g, FROZEN, FROZEN_SHADE);
        g.rect(13, 15, 14, 16, rgba("7ad0ff"));                  // les yeux qui luisent
        g.rect(20, 15, 21, 16, rgba("7ad0ff"));
        g.line(15, 21, 19, 21, ICE_DEEP);
        g.ellipse(17f, 9f, 7.5f, 3f, WHITE);
        // La couronne de stalactites.
        g.rect(10, 6, 24, 8, ICE);
        for (int x = 10; x <= 24; x += 2) {
            int top = x == 17 || x == 16 ? 0 : (x % 4 == 0 ? 2 : 4);
            g.line(x, top, x, 6, x % 4 == 0 ? WHITE : ICE);
        }
        g.set(17, 7, rgba("7ad0ff"));
        // Le sceptre de glace, à droite.
        g.line(30, 8, 30, 40, ICE);
        g.ellipse(30f, 6f, 2f, 2.5f, WHITE);
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Le Téléphérique
    // -------------------------------------------------------------------------

    /** Liftier : anorak rouge, bonnet à pompon, lunettes de glacier relevées, barbe givrée. */
    private static int[][] liftier() {
        PixelCanvas g = new PixelCanvas(W, H);
        bust(g, rgba("d83a2a"), rgba("8a2018"), 0);
        g.line(17, 29, 17, 41, rgba("8a2018"));                   // la fermeture
        g.rect(4, 29, 29, 30, WHITE);                             // la fourrure du col
        face(g, SKIN, SKIN_SHADE);
        g.ellipse(17f, 23.5f, 5f, 3f, WHITE_SHADE);               // la barbe givrée
        g.line(15, 21, 19, 21, MOUTH);
        g.ellipse(17f, 7f, 8f, 4.5f, rgba("2a5ab8"));             // le bonnet
        for (int x = 10; x <= 24; x += 3) g.set(x, 9, WHITE);
        g.ellipse(17f, 2f, 2f, 2f, WHITE);
        g.rect(11, 10, 23, 12, COAL);                              // les lunettes relevées
        g.rect(12, 10, 15, 11, rgba("ffb020"));
        g.rect(19, 10, 22, 11, rgba("ffb020"));
        // La manette de la cabine, à droite.
        g.line(30, 16, 30, 30, STEEL_DARK);
        g.ellipse(30f, 15f, 1.8f, 1.8f, RED);
        return g.outlined();
    }

    /** Le Yéti : une énorme tête de fourrure blanche, cornes de glace, yeux bleus, crocs, mains énormes. */
    private static int[][] yeti() {
        PixelCanvas g = new PixelCanvas(W, H);
        int fur = rgba("eef2f8"), furShade = rgba("b8c4d8"), face = rgba("7a8aa8");
        g.ellipseShaded(17f, 33f, 16f, 10f, fur, furShade, 4f);   // les épaules massives
        g.ellipseShaded(17f, 17f, 11f, 12f, fur, furShade, 3f);   // la tête
        for (int[] p : new int[][] {{7, 10}, {9, 6}, {25, 6}, {27, 10}, {5, 16}, {29, 16}}) g.set(p[0], p[1], furShade);
        g.ellipse(17f, 19f, 6.5f, 6f, face);                      // le visage
        g.rect(13, 16, 15, 17, rgba("7ad0ff"));                   // les yeux bleus
        g.rect(19, 16, 21, 17, rgba("7ad0ff"));
        g.line(12, 14, 15, 15, COAL);
        g.line(22, 14, 19, 15, COAL);
        g.rect(14, 21, 20, 23, COAL);                             // la gueule ouverte
        g.set(14, 21, WHITE); g.set(20, 21, WHITE); g.set(15, 23, WHITE); g.set(19, 23, WHITE);
        // Les cornes de glace.
        g.triangle(6, 9, 10, 9, 3, 0, ICE);
        g.triangle(24, 9, 28, 9, 31, 0, ICE);
        // Les mains énormes, au premier plan.
        g.ellipseShaded(4f, 38f, 4f, 3.5f, fur, furShade, 2f);
        g.ellipseShaded(30f, 38f, 4f, 3.5f, fur, furShade, 2f);
        for (int x : new int[] {2, 4, 6, 28, 30, 32}) g.set(x, 41, ICE_DEEP);
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // La Suite royale
    // -------------------------------------------------------------------------

    /** Majordome : habit noir, plastron blanc, nœud papillon, cheveux gris plaqués, un plateau et sa coupe. */
    private static int[][] majordome() {
        PixelCanvas g = new PixelCanvas(W, H);
        bust(g, COAL, rgba("12121a"), WHITE);
        g.rect(15, 28, 19, 30, COAL);
        g.set(17, 29, ICE_DARK);
        frost(g);
        face(g, FROZEN, FROZEN_SHADE);
        g.ellipse(17f, 9f, 7.5f, 3f, rgba("a8b0bc"));
        g.line(17, 6, 17, 8, rgba("7a8494"));
        g.line(12, 13, 15, 13, rgba("7a8494"));
        g.line(19, 13, 22, 13, rgba("7a8494"));
        g.line(15, 21, 19, 21, MOUTH);
        // Le plateau d'argent et sa coupe de champagne.
        g.rect(0, 22, 9, 23, STEEL_DARK);
        g.triangle(2, 15, 8, 15, 5, 19, rgba("ffe08a"));
        g.line(5, 19, 5, 21, ICE);
        g.line(4, 21, 6, 21, ICE);
        g.line(4, 24, 5, 29, FROZEN);
        return g.outlined();
    }

    /**
     * Le Prince des Neiges : uniforme d'apparat blanc à épaulettes d'or, cape bleu nuit bordée d'hermine,
     * couronne fine, visage pâle et beau, une coupe de champagne levée.
     */
    private static int[][] prince() {
        PixelCanvas g = new PixelCanvas(W, H);
        g.rect(1, 27, 32, 41, rgba("1a2a5a"));                     // la cape
        bust(g, WHITE, WHITE_SHADE, 0);
        g.rect(3, 28, 9, 30, GOLD);                                 // les épaulettes
        g.rect(25, 28, 31, 30, GOLD_DARK);
        for (int x = 3; x <= 9; x += 2) g.set(x, 31, GOLD_DARK);
        g.line(10, 31, 24, 41, ICE_DARK);                           // l'écharpe
        for (int y = 31; y <= 40; y += 3) g.set(17, y, GOLD);
        for (int x = 1; x <= 32; x += 3) g.set(x, 27, WHITE);       // l'hermine
        face(g, FROZEN, FROZEN_SHADE);
        g.rect(13, 15, 14, 16, ICE_DEEP);
        g.rect(20, 15, 21, 16, ICE_DEEP);
        g.line(15, 21, 19, 21, rgba("8a5a80"));
        g.set(19, 20, rgba("8a5a80"));                              // le sourire en coin
        g.ellipse(17f, 9f, 8f, 3.5f, rgba("e8eef8"));               // les cheveux d'argent
        g.ellipse(10f, 13f, 2f, 3f, rgba("e8eef8"));
        // La couronne fine.
        g.rect(11, 5, 23, 6, GOLD);
        for (int x = 11; x <= 23; x += 3) g.set(x, 4, GOLD);
        g.set(17, 3, ICE); g.set(17, 4, ICE);
        // La coupe levée, à droite.
        g.triangle(26, 10, 32, 10, 29, 14, rgba("ffe08a"));
        g.line(29, 14, 29, 17, ICE);
        g.line(28, 17, 30, 17, ICE);
        g.line(29, 18, 28, 28, FROZEN);
        return g.outlined();
    }
}
