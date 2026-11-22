package fr.astratime.lucky.assets;

import fr.astratime.lucky.entities.enemy.EnemyKind;

import static fr.astratime.lucky.assets.PixelCanvas.rgba;

/**
 * Portraits en pixel art des ennemis du chapitre « La Guerre des Tables », en buste,
 * à la taille du croupier (voir {@link ManorPortraits}). Chaque soldat porte la
 * tenue d'une armée de joueurs : un uniforme, des galons de jetons, un képi.
 */
public final class WarPortraits {

    private static final int W = EnemyPortraits.WIDTH, H = EnemyPortraits.HEIGHT;

    private static final int SKIN = rgba("e8b890"), SKIN_SHADE = rgba("b8865e"), SKIN_DARK = rgba("8a5e3e");
    private static final int EYE = rgba("1a1018"), MOUTH = rgba("6a2a24"), WHITE = rgba("f6f0e2"), WHITE_SHADE = rgba("c8c0b4");
    private static final int GOLD = rgba("ffd54a"), GOLD_DARK = rgba("c4891e");
    private static final int RED = rgba("c8283c"), RED_DARK = rgba("7a1424"), BLUE = rgba("2a4aa8"), BLUE_DARK = rgba("16285e");
    private static final int GREEN = rgba("2e6a3a"), GREEN_DARK = rgba("1a3e22");
    private static final int BLACK = rgba("1a1420"), BLACK_LIGHT = rgba("3a3044");
    private static final int IRON = rgba("5a5a6a"), IRON_LIGHT = rgba("8a8a9c"), BROWN = rgba("5a3a22"), BROWN_DARK = rgba("3a2416");
    private static final int HAIR_GREY = rgba("a8a4a0"), SOOT = rgba("3a3434");

    private WarPortraits() {}

    /** @return le portrait de l'ennemi {@code kind}, ou {@code null} s'il n'est pas de la Guerre des Tables. */
    static int[][] of(EnemyKind kind) {
        return switch (kind) {
            case SERGENT    -> sergent();
            case CANONNIER  -> canonnier();
            case ESPIONNE   -> espionne();
            case MERCENAIRE -> mercenaire();
            case GENERAL    -> general();
            default         -> null;
        };
    }

    // -------------------------------------------------------------------------
    // Gabarit commun
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

    /** Des épaulettes dorées à franges, sur les deux épaules. */
    private static void epaulettes(PixelCanvas g) {
        g.rect(3, 28, 10, 29, GOLD);
        g.rect(24, 28, 31, 29, GOLD_DARK);
        for (int x = 3; x <= 10; x += 2) g.set(x, 30, GOLD_DARK);
        for (int x = 24; x <= 31; x += 2) g.set(x, 30, GOLD_DARK);
    }

    /** Une rangée de jetons cousus en galons sur la poitrine, de {@code x0} à {@code x1}. */
    private static void chipMedals(PixelCanvas g, int x0, int x1, int y) {
        int[] colors = {RED, BLUE, GOLD, rgba("2e9a4a")};
        for (int x = x0, k = 0; x <= x1; x += 3, k++) {
            g.ellipse(x, y, 1.2f, 1.2f, colors[k % colors.length]);
            g.set(x, y, WHITE);
        }
    }

    // -------------------------------------------------------------------------
    // Les ennemis
    // -------------------------------------------------------------------------

    /**
     * Le Sergent de table : uniforme rouge à brandebourgs, képi à visière, grosse moustache,
     * il souffle dans un sifflet ; une pile de jetons cousue en galons sur la manche.
     */
    private static int[][] sergent() {
        PixelCanvas g = new PixelCanvas(W, H);
        bust(g, RED, RED_DARK, WHITE);
        for (int y = 31; y <= 39; y += 3) g.line(13, y, 21, y, GOLD);   // les brandebourgs
        chipMedals(g, 6, 9, 36);
        face(g, SKIN, SKIN_SHADE);
        g.line(12, 13, 15, 13, BROWN_DARK);                               // sourcils froncés
        g.line(19, 13, 22, 13, BROWN_DARK);
        g.rect(12, 20, 22, 21, BROWN);                                     // la grosse moustache
        g.set(11, 21, BROWN); g.set(23, 21, BROWN);
        g.rect(16, 22, 18, 23, IRON_LIGHT);                                // le sifflet
        g.line(18, 23, 22, 27, GOLD_DARK);                                 // sa chaînette
        // Le képi : calot rouge, bandeau noir, visière, pompon.
        g.rect(10, 4, 24, 9, RED);
        g.rect(20, 4, 24, 9, RED_DARK);
        g.rect(10, 9, 24, 10, BLACK);
        g.rect(8, 11, 26, 11, BLACK_LIGHT);
        g.ellipse(17f, 3f, 1.6f, 1.4f, GOLD);
        g.ellipse(17f, 7f, 1.6f, 1.6f, WHITE);                             // l'insigne : un jeton
        g.set(17, 7, RED);
        return g.outlined();
    }

    /**
     * Le Canonnier à jetons : bras nus noircis de poudre, bonnet de laine, lunettes de protection
     * sur le front, un boulet-jeton sous le bras ; une mèche fumante derrière l'oreille.
     */
    private static int[][] canonnier() {
        PixelCanvas g = new PixelCanvas(W, H);
        bust(g, BLUE, BLUE_DARK, 0);
        g.rect(12, 29, 22, 41, rgba("d8ccb0"));                            // le maillot rayé
        for (int y = 30; y <= 40; y += 2) g.line(12, y, 22, y, BLUE_DARK);
        g.line(11, 29, 11, 41, BROWN_DARK);                                // les bretelles
        g.line(23, 29, 23, 41, BROWN_DARK);
        face(g, SKIN_SHADE, SKIN_DARK);
        g.set(13, 19, SOOT); g.set(21, 18, SOOT); g.set(15, 23, SOOT);      // la poudre sur les joues
        g.line(14, 21, 20, 21, MOUTH);
        g.set(19, 22, WHITE);                                              // une dent en or, et un sourire de travers
        g.set(15, 22, GOLD);
        // Les lunettes relevées sur le front.
        g.ellipse(13.5f, 11f, 2.3f, 1.8f, IRON);
        g.ellipse(20.5f, 11f, 2.3f, 1.8f, IRON);
        g.ellipse(13.5f, 11f, 1.3f, 1f, rgba("a8d8e8"));
        g.ellipse(20.5f, 11f, 1.3f, 1f, rgba("a8d8e8"));
        g.line(9, 11, 25, 11, BROWN_DARK);
        // Le bonnet de laine.
        g.ellipseShaded(17f, 6f, 8f, 4f, RED, RED_DARK, 2f);
        g.rect(9, 7, 25, 8, RED_DARK);
        // Le boulet-jeton sous son bras, et la mèche fumante.
        g.ellipseShaded(5f, 36f, 4.5f, 4.5f, RED, RED_DARK, 1f);
        g.ellipse(5f, 36f, 2.5f, 2.5f, WHITE);
        g.ellipse(5f, 36f, 1.4f, 1.4f, RED);
        g.line(25, 14, 29, 9, BROWN);
        g.set(29, 8, rgba("ff8a1e"));
        g.set(30, 6, rgba("8a8a8a")); g.set(29, 4, rgba("6a6a6a"));
        return g.outlined();
    }

    /**
     * L'Espionne : imperméable gris au col relevé, chapeau à large bord penché sur un oeil,
     * loup noir, lèvres rouges ; elle cache une carte entre deux doigts gantés.
     */
    private static int[][] espionne() {
        PixelCanvas g = new PixelCanvas(W, H);
        int coat = rgba("6a6a72"), coatDark = rgba("44444c");
        bust(g, coat, coatDark, BLACK);
        g.triangle(8, 24, 13, 30, 8, 32, coat);                           // le col relevé
        g.triangle(26, 24, 21, 30, 26, 32, coatDark);
        g.line(17, 33, 17, 41, coatDark);                                 // la fermeture et la ceinture
        g.rect(4, 37, 29, 38, BLACK_LIGHT);
        g.set(17, 37, GOLD);
        // Les cheveux noirs au carré.
        g.ellipse(17f, 14f, 8.5f, 9f, BLACK);
        face(g, rgba("f0d0b8"), rgba("c8a088"));
        g.rect(9, 16, 10, 24, BLACK);
        g.rect(24, 16, 25, 24, BLACK);
        // Le loup noir sur les yeux.
        g.rect(11, 14, 23, 17, BLACK);
        g.set(13, 15, WHITE); g.set(20, 15, WHITE);
        g.rect(15, 21, 19, 21, rgba("d8203a"));                           // les lèvres rouges
        g.set(16, 22, rgba("a01428"));
        // Le chapeau à large bord, penché.
        g.line(4, 9, 30, 6, BLACK_LIGHT);
        g.line(4, 10, 30, 7, BLACK);
        g.rect(10, 2, 24, 8, BLACK_LIGHT);
        g.rect(10, 7, 24, 8, rgba("8a1424"));                             // le ruban
        // La main gantée et sa carte.
        g.rect(26, 30, 30, 33, BLACK);
        g.rect(28, 23, 32, 30, WHITE);
        g.rect(31, 23, 32, 30, WHITE_SHADE);
        g.set(30, 26, rgba("d8203a"));
        return g.outlined();
    }

    /**
     * Le Mercenaire : armure de cuir rapiécée de feutre vert, cicatrice sur l'oeil, barbe de trois
     * jours, un bandeau ; une bourse de pièces à la ceinture et un sabre sur l'épaule.
     */
    private static int[][] mercenaire() {
        PixelCanvas g = new PixelCanvas(W, H);
        bust(g, BROWN, BROWN_DARK, 0);
        g.rect(9, 31, 15, 36, GREEN);                                     // une pièce de feutre cousue
        g.rect(9, 31, 15, 31, GREEN_DARK);
        for (int y = 32; y <= 36; y += 2) g.set(12, y, WHITE_SHADE);
        g.line(19, 30, 26, 41, BLACK_LIGHT);                              // la bandoulière
        g.ellipseShaded(25f, 38f, 3f, 2.5f, GOLD, GOLD_DARK, 0.5f);       // la bourse
        g.set(24, 36, BROWN_DARK);
        face(g, SKIN, SKIN_SHADE);
        for (int x = 13; x <= 21; x++) for (int y = 20; y <= 25; y++) if ((x + y) % 2 == 0) g.set(x, y, SKIN_DARK); // la barbe
        g.line(15, 21, 19, 21, MOUTH);
        g.line(19, 12, 22, 19, rgba("b8605a"));                           // la cicatrice
        g.rect(20, 15, 21, 16, rgba("c8c0b4"));                           // l'oeil blanc, balafré
        // Le bandeau et ses cheveux en bataille.
        g.ellipse(17f, 8f, 8f, 4f, BROWN_DARK);
        g.rect(9, 10, 25, 11, RED);
        g.line(25, 11, 29, 14, RED);
        g.line(25, 10, 29, 12, RED_DARK);
        // Le sabre posé sur l'épaule gauche.
        g.line(2, 41, 8, 22, IRON_LIGHT);
        g.line(3, 41, 9, 22, IRON);
        g.rect(6, 27, 9, 28, GOLD_DARK);
        return g.outlined();
    }

    /**
     * Le Général Va-Tout : grand uniforme noir à épaulettes d'or, plastron couvert de médailles-jetons,
     * bicorne à plumet rouge, favoris gris et regard fou ; il tient deux piles de jetons, prêt à tout miser.
     */
    private static int[][] general() {
        PixelCanvas g = new PixelCanvas(W, H);
        bust(g, BLACK_LIGHT, BLACK, WHITE);
        epaulettes(g);
        g.rect(15, 30, 19, 41, RED);                                       // le grand cordon
        chipMedals(g, 7, 13, 33);
        chipMedals(g, 7, 13, 37);
        chipMedals(g, 21, 27, 35);
        face(g, SKIN, SKIN_SHADE);
        g.rect(9, 17, 11, 24, HAIR_GREY);                                  // les favoris gris
        g.rect(23, 17, 25, 24, HAIR_GREY);
        g.rect(13, 15, 14, 16, GOLD);                                      // le regard fou : des pupilles d'or
        g.rect(20, 15, 21, 16, GOLD);
        g.set(13, 15, EYE); g.set(21, 15, EYE);
        g.line(11, 12, 15, 14, HAIR_GREY);
        g.line(19, 14, 23, 12, HAIR_GREY);
        g.line(14, 20, 20, 20, HAIR_GREY);                                 // la moustache en guidon
        g.set(13, 19, HAIR_GREY); g.set(21, 19, HAIR_GREY);
        g.line(15, 22, 19, 22, MOUTH);
        g.set(14, 21, MOUTH); g.set(20, 21, MOUTH);                         // un sourire de joueur
        // Le bicorne, de travers, et son plumet rouge.
        g.ellipseShaded(17f, 7f, 12f, 3.5f, BLACK, BLACK_LIGHT, 4f);
        g.triangle(9, 7, 25, 7, 17, 0, BLACK);
        g.line(6, 8, 28, 8, GOLD);
        g.ellipse(17f, 6f, 1.6f, 1.6f, rgba("f0f0f0"));                    // la cocarde : un jeton blanc
        g.set(17, 6, RED);
        g.ellipse(23f, 2f, 3f, 1.6f, RED);
        g.ellipse(26f, 1f, 2f, 1.2f, RED_DARK);
        // Les deux piles de jetons, une dans chaque main.
        int[] chips = {RED, BLUE, GOLD, RED, BLUE};
        for (int k = 0; k < chips.length; k++) {
            g.rect(0, 40 - k * 2, 4, 41 - k * 2, chips[k]);
            g.set(2, 41 - k * 2, WHITE);
            g.rect(29, 40 - k * 2, 33, 41 - k * 2, chips[(k + 2) % chips.length]);
            g.set(31, 41 - k * 2, WHITE);
        }
        return g.outlined();
    }
}
