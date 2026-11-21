package fr.astratime.lucky.assets;

import fr.astratime.lucky.entities.enemy.EnemyKind;

import static fr.astratime.lucky.assets.PixelCanvas.rgba;

/**
 * Portraits en pixel art des ennemis du Manoir hanté, en buste, à la taille
 * du croupier (voir {@link PlacesPortraits}).
 */
public final class ManorPortraits {

    private static final int W = EnemyPortraits.WIDTH, H = EnemyPortraits.HEIGHT;

    private static final int PALE = rgba("e6e2ee"), PALE_SHADE = rgba("b0a8c0");
    private static final int GHOST = rgba("c8f0e8"), GHOST_SHADE = rgba("7ab8b0"), GHOST_DARK = rgba("4a8a84");
    private static final int EYE = rgba("1a1018"), MOUTH = rgba("5a1a20"), WHITE = rgba("f6f0e2"), WHITE_SHADE = rgba("c8c0b4");
    private static final int GOLD = rgba("ffd54a"), GOLD_DARK = rgba("c4891e");
    private static final int BLOOD = rgba("b0101e"), BLOOD_DARK = rgba("6a0a14");
    private static final int BLACK = rgba("1a1420"), BLACK_LIGHT = rgba("3a2e4a");
    private static final int STONE = rgba("5a5a6e"), STONE_DARK = rgba("3a3a4a");
    private static final int WOOD = rgba("6a4a2e"), WOOD_DARK = rgba("3e2a18");
    private static final int VIOLET = rgba("b88aff"), EMBER = rgba("ff5a3a"), GREEN = rgba("7dff8a");

    private ManorPortraits() {}

    /** @return les pixels du portrait de l'ennemi {@code kind}, ou {@code null} s'il n'est pas du Manoir. */
    public static int[][] pixels(EnemyKind kind) { return of(kind); }

    /** @return le portrait de l'ennemi {@code kind}, ou {@code null} s'il n'est pas du Manoir hanté. */
    static int[][] of(EnemyKind kind) {
        return switch (kind) {
            case VALET_FANTOME  -> valetFantome();
            case COMTE_MINUIT   -> comteMinuit();
            case LIVRE_VIVANT   -> livreVivant();
            case BIBLIOTHECAIRE -> bibliothecaire();
            case GOULE          -> goule();
            case FOSSOYEUR      -> fossoyeur();
            case POUPEE_HANTEE  -> poupeeHantee();
            case MARIONNETTISTE -> marionnettiste();
            default             -> null;
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

    /** Des yeux qui luisent de la couleur {@code glow}. */
    private static void glowingEyes(PixelCanvas g, int glow) {
        g.rect(13, 15, 14, 16, glow);
        g.rect(20, 15, 21, 16, glow);
        g.set(13, 15, WHITE);
        g.set(20, 15, WHITE);
    }

    // -------------------------------------------------------------------------
    // Le Salon de jeu
    // -------------------------------------------------------------------------

    /** Valet fantôme : livrée de valet d'un autre siècle, translucide, perruque poudrée, une carte entre les doigts. */
    private static int[][] valetFantome() {
        PixelCanvas g = new PixelCanvas(W, H);
        bust(g, GHOST_SHADE, GHOST_DARK, WHITE);
        g.rect(15, 28, 19, 31, WHITE);                       // le jabot
        for (int y = 32; y <= 40; y += 3) { g.set(12, y, GOLD); g.set(22, y, GOLD); }
        face(g, GHOST, GHOST_SHADE);
        glowingEyes(g, rgba("4ae8d8"));
        g.line(15, 21, 19, 22, GHOST_DARK);                   // un sourire triste
        // La perruque poudrée à rouleaux.
        g.ellipse(17f, 8f, 8f, 3.5f, WHITE);
        g.ellipse(9f, 13f, 2.2f, 2f, WHITE);
        g.ellipse(9f, 17f, 2.2f, 2f, WHITE_SHADE);
        g.ellipse(25f, 13f, 2.2f, 2f, WHITE_SHADE);
        g.ellipse(25f, 17f, 2.2f, 2f, WHITE_SHADE);
        // La carte qu'il tend, un As de Coeur.
        g.rect(0, 20, 6, 28, WHITE);
        g.ellipse(3f, 24f, 1.3f, 1.3f, BLOOD);
        g.line(6, 27, 8, 30, GHOST);
        // Le bas de son corps se défait en brume.
        for (int x = 4; x <= 29; x += 3) g.set(x, 41, 0);
        return g.outlined();
    }

    /**
     * Le Comte de Minuit : le baron devenu vampire. Peau grise, cheveux noirs en pointe, cape au grand col rouge,
     * crocs, une chevalière d'or et une montre à gousset arrêtée à minuit.
     */
    private static int[][] comteMinuit() {
        PixelCanvas g = new PixelCanvas(W, H);
        bust(g, BLACK, rgba("120e18"), WHITE);
        // Le grand col de la cape, rouge dedans.
        g.triangle(3, 22, 11, 30, 4, 32, BLOOD);
        g.triangle(31, 22, 23, 30, 30, 32, BLOOD_DARK);
        g.rect(15, 28, 19, 30, BLOOD);                       // la lavallière
        g.set(17, 29, GOLD);
        face(g, PALE, PALE_SHADE);
        glowingEyes(g, rgba("ff3a3a"));
        g.line(12, 13, 15, 14, BLACK);                         // sourcils froncés
        g.line(19, 14, 22, 13, BLACK);
        g.line(14, 21, 20, 21, MOUTH);
        g.set(15, 22, WHITE); g.set(19, 22, WHITE);            // les crocs
        // Les cheveux noirs plaqués en pointe sur le front.
        g.ellipse(17f, 8f, 8f, 3.5f, BLACK);
        g.triangle(13, 9, 21, 9, 17, 13, BLACK);
        g.rect(9, 8, 10, 14, BLACK);
        g.rect(24, 8, 25, 14, BLACK);
        // La montre à gousset, aiguilles sur minuit, et sa chaîne d'or.
        g.ellipse(26f, 37f, 2.5f, 2.5f, GOLD);
        g.ellipse(26f, 37f, 1.6f, 1.6f, WHITE);
        g.line(26, 35, 26, 37, BLACK);
        g.line(20, 34, 24, 36, GOLD_DARK);
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // La Bibliothèque
    // -------------------------------------------------------------------------

    /** Livre vivant : un gros grimoire ouvert comme une gueule, des pages-dents, un oeil sur la couverture. */
    private static int[][] livreVivant() {
        PixelCanvas g = new PixelCanvas(W, H);
        int cover = rgba("5a2a1a"), coverDark = rgba("3a1a10"), page = rgba("e8dcc0"), pageShade = rgba("bcae90");
        // La couverture du bas et les pages, qui forment une mâchoire.
        g.rect(3, 26, 31, 38, cover);
        g.rect(22, 26, 31, 38, coverDark);
        g.rect(5, 24, 29, 27, page);
        for (int x = 6; x <= 28; x += 3) g.triangle(x, 24, x + 2, 24, x + 1, 21, page); // les dents du bas
        // La couverture du haut, levée, et ses pages.
        g.triangle(3, 18, 31, 18, 17, 4, cover);
        g.rect(3, 16, 31, 18, cover);
        g.rect(22, 10, 31, 18, coverDark);
        g.rect(5, 18, 29, 19, pageShade);
        for (int x = 6; x <= 28; x += 3) g.triangle(x, 19, x + 2, 19, x + 1, 22, pageShade); // les dents du haut
        // Le gouffre entre les pages, une langue de marque-page rouge.
        g.rect(7, 20, 27, 23, rgba("1a0a0a"));
        g.rect(16, 22, 18, 30, BLOOD);
        g.triangle(16, 30, 18, 30, 17, 32, BLOOD_DARK);
        // L'oeil sur la couverture, et les coins de laiton.
        g.ellipse(17f, 12f, 3.2f, 2.2f, page);
        g.ellipse(17f, 12f, 1.4f, 1.4f, rgba("ff9a2a"));
        g.set(17, 12, EYE);
        for (int[] c : new int[][] {{4, 17}, {30, 17}, {4, 37}, {30, 37}}) g.rect(c[0] - 1, c[1] - 1, c[0], c[1], GOLD_DARK);
        // Des pages volantes autour de lui.
        g.rect(0, 6, 3, 9, page);
        g.rect(31, 2, 33, 5, page);
        return g.outlined();
    }

    /** La Bibliothécaire spectrale : chignon serré, lunettes rondes, châle gris, le doigt sur la bouche (chut). */
    private static int[][] bibliothecaire() {
        PixelCanvas g = new PixelCanvas(W, H);
        int shawl = rgba("5a5a72"), shawlDark = rgba("3a3a52");
        bust(g, shawl, shawlDark, rgba("e8dcc0"));
        for (int x = 6; x <= 28; x += 4) g.line(x, 30, x - 2, 41, shawlDark); // les plis du châle
        face(g, GHOST, GHOST_SHADE);
        glowingEyes(g, VIOLET);
        // Les lunettes rondes cerclées d'or, et leur chaînette.
        g.ellipse(13.5f, 15.5f, 2.6f, 2.2f, 0);
        for (int[] c : new int[][] {{13, 15}, {20, 15}}) {
            for (int a = 0; a < 360; a += 30) {
                double r = Math.toRadians(a);
                g.set((int) Math.round(c[0] + 0.5 + 2.4 * Math.cos(r)), (int) Math.round(c[1] + 0.5 + 2 * Math.sin(r)), GOLD_DARK);
            }
        }
        glowingEyes(g, VIOLET);
        g.line(16, 15, 18, 15, GOLD_DARK);
        g.line(10, 16, 9, 26, GOLD);
        // Le chignon serré, gris.
        g.ellipse(17f, 8f, 7.5f, 3.5f, rgba("8a8aa0"));
        g.ellipse(17f, 3f, 3.5f, 3f, rgba("8a8aa0"));
        g.line(15, 2, 19, 5, rgba("c4891e"));                  // une épingle
        // Le doigt posé sur les lèvres : chut.
        g.rect(16, 19, 17, 24, GHOST);
        g.rect(15, 24, 18, 28, GHOST_SHADE);
        // Un livre serré contre elle.
        g.rect(22, 32, 29, 40, rgba("5a2a1a"));
        g.rect(22, 32, 23, 40, GOLD_DARK);
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // La Crypte
    // -------------------------------------------------------------------------

    /** Goule : peau verdâtre, crâne chauve et cabossé, yeux jaunes, haillons, des doigts griffus. */
    private static int[][] goule() {
        PixelCanvas g = new PixelCanvas(W, H);
        int rag = rgba("4a4a3a"), ragDark = rgba("2e2e22"), skin = rgba("8aa878"), skinShade = rgba("5a7a4e");
        bust(g, rag, ragDark, 0);
        for (int x = 5; x <= 29; x += 4) g.triangle(x, 41, x + 3, 41, x + 1, 37, 0);     // les haillons déchirés
        face(g, skin, skinShade);
        glowingEyes(g, rgba("ffe040"));
        g.ellipse(17f, 7.5f, 7.5f, 3f, skin);                                          // le crâne chauve
        g.set(12, 7, skinShade); g.set(22, 9, skinShade); g.set(18, 5, skinShade);
        g.rect(14, 20, 20, 22, rgba("2a1a12"));                                          // la bouche
        for (int x = 14; x <= 20; x += 2) g.set(x, 20, WHITE);
        g.ellipse(9.5f, 13f, 1.5f, 3f, skinShade);                                      // les oreilles pointues
        g.ellipse(24.5f, 13f, 1.5f, 3f, skinShade);
        // Les mains griffues levées.
        for (int side : new int[] {0, 1}) {
            int x0 = side == 0 ? 0 : 29;
            g.rect(x0, 22, x0 + 4, 28, skin);
            for (int k = 0; k < 3; k++) g.line(x0 + k * 2, 22, x0 + k * 2 + (side == 0 ? -1 : 1), 18, WHITE_SHADE);
        }
        // De la terre de la tombe sur les épaules.
        g.set(8, 29, WOOD); g.set(25, 30, WOOD); g.set(13, 33, WOOD_DARK);
        return g.outlined();
    }

    /** Le Fossoyeur : haut-de-forme cabossé, longue figure grise, lanterne verte, pelle sur l'épaule. */
    private static int[][] fossoyeur() {
        PixelCanvas g = new PixelCanvas(W, H);
        int coat = rgba("2e2a26"), coatDark = rgba("1a1814");
        bust(g, coat, coatDark, rgba("8a8070"));
        face(g, PALE_SHADE, rgba("8a8098"));
        g.line(12, 15, 15, 16, EYE);                           // des paupières lourdes
        g.line(19, 16, 22, 15, EYE);
        g.line(14, 22, 20, 21, MOUTH);
        g.rect(15, 23, 19, 25, rgba("6a6a6a"));                // la barbe de trois jours
        // Le haut-de-forme cabossé, penché.
        g.rect(11, 0, 23, 8, coatDark);
        g.rect(8, 8, 26, 9, coat);
        g.line(11, 3, 23, 1, coat);
        g.rect(11, 6, 23, 7, BLOOD_DARK);                      // le ruban
        // La pelle sur l'épaule droite.
        g.line(30, 2, 27, 30, WOOD);
        g.rect(28, 0, 33, 4, STONE);
        g.rect(29, 1, 32, 3, rgba("8a8aa0"));
        // La lanterne verte, tenue bas à gauche.
        g.line(3, 27, 3, 31, STONE_DARK);
        g.rect(1, 31, 5, 37, STONE_DARK);
        g.rect(2, 32, 4, 36, GREEN);
        g.set(3, 34, WHITE);
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Le Grenier
    // -------------------------------------------------------------------------

    /** Poupée hantée : visage de porcelaine fêlé, joues roses, anglaises blondes, robe à dentelle, yeux de verre. */
    private static int[][] poupeeHantee() {
        PixelCanvas g = new PixelCanvas(W, H);
        int dress = rgba("d87aa0"), dressDark = rgba("a04a70");
        bust(g, dress, dressDark, WHITE);
        for (int x = 5; x <= 29; x += 2) g.set(x, 29, WHITE);  // la dentelle
        g.ellipse(17f, 33f, 2f, 1.5f, rgba("ff9ac0"));         // un noeud
        face(g, rgba("f8f0f0"), rgba("d8c8d0"));
        g.ellipse(13.5f, 15.5f, 2f, 2f, rgba("4a8ad8"));       // les grands yeux de verre
        g.ellipse(20.5f, 15.5f, 2f, 2f, rgba("4a8ad8"));
        g.set(13, 15, WHITE); g.set(20, 15, WHITE);
        g.set(14, 16, EYE); g.set(21, 16, EYE);
        g.ellipse(11f, 19f, 1.5f, 1f, rgba("ff9ab0"));         // les joues roses
        g.ellipse(23f, 19f, 1.5f, 1f, rgba("ff9ab0"));
        g.ellipse(17f, 21f, 1.2f, 0.8f, BLOOD);                // la petite bouche
        g.line(20, 8, 18, 13, rgba("8a7a80"));                 // la fêlure
        g.line(18, 13, 19, 17, rgba("8a7a80"));
        // Les anglaises blondes et le bonnet.
        int curl = rgba("f0d070"), curlDark = rgba("c8a040");
        g.ellipse(17f, 8f, 8f, 3.5f, curl);
        for (int y = 11; y <= 23; y += 4) {
            g.ellipse(9f, y, 1.8f, 2f, y % 8 == 3 ? curl : curlDark);
            g.ellipse(25f, y, 1.8f, 2f, curlDark);
        }
        g.ellipse(17f, 5f, 9f, 2.5f, WHITE);
        g.rect(8, 5, 26, 6, WHITE_SHADE);
        // Le fil qui la tient, au-dessus de la tête.
        g.line(17, 0, 17, 3, rgba("e8e0f0"));
        return g.outlined();
    }

    /** Le Marionnettiste : cagoule noire, visage blanc de masque souriant, sa croix de bois et ses fils dans les mains. */
    private static int[][] marionnettiste() {
        PixelCanvas g = new PixelCanvas(W, H);
        bust(g, BLACK, rgba("120e18"), 0);
        g.rect(13, 28, 21, 31, rgba("8a1a2a"));                // le col
        // La cagoule noire, et son masque blanc souriant.
        g.ellipse(17f, 14f, 9f, 11f, BLACK_LIGHT);
        g.ellipseShaded(17f, 16f, 6f, 7.5f, WHITE, WHITE_SHADE, 2f);
        g.line(12, 15, 15, 14, EYE);                           // yeux en croissant
        g.line(19, 14, 22, 15, EYE);
        g.set(13, 15, EMBER); g.set(21, 15, EMBER);
        g.line(13, 20, 17, 22, EYE);                           // le grand sourire
        g.line(17, 22, 21, 20, EYE);
        g.set(12, 19, BLOOD); g.set(22, 19, BLOOD);
        // La croix de bois, tenue au-dessus, et les fils qui descendent.
        g.rect(4, 2, 30, 3, WOOD);
        g.rect(16, 0, 18, 6, WOOD_DARK);
        int thread = rgba("e8e0f0");
        for (int x : new int[] {5, 11, 23, 29}) g.line(x, 4, x + (x < 17 ? -1 : 1), 22, thread);
        // Ses mains gantées de blanc, qui tiennent la croix.
        g.rect(2, 3, 5, 6, WHITE);
        g.rect(29, 3, 32, 6, WHITE);
        g.line(3, 7, 6, 29, BLACK_LIGHT);
        g.line(31, 7, 28, 29, BLACK_LIGHT);
        return g.outlined();
    }
}
