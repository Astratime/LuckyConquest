package fr.astratime.lucky.assets;

import fr.astratime.lucky.entities.enemy.EnemyKind;

import static fr.astratime.lucky.assets.PixelCanvas.rgba;

/**
 * Portraits en pixel art des ennemis du Temple du Destin, en buste, à la taille
 * du croupier (voir {@link PlacesPortraits}).
 */
public final class TemplePortraits {

    private static final int W = EnemyPortraits.WIDTH, H = EnemyPortraits.HEIGHT;

    private static final int SKIN = rgba("e8b890"), SKIN_SHADE = rgba("c08a62");
    private static final int EYE = rgba("1a1018"), MOUTH = rgba("7a2a20");
    private static final int GOLD = rgba("ffd54a"), GOLD_DARK = rgba("c4891e"), GOLD_LIGHT = rgba("fff2a0");
    private static final int SAFFRON = rgba("f08a2a"), SAFFRON_DARK = rgba("b8541a");
    private static final int RED = rgba("c8323a"), RED_DARK = rgba("7a1a22");
    private static final int IVORY = rgba("f6f0e2"), IVORY_SHADE = rgba("c8c0b4");
    private static final int STONE = rgba("a89a88"), STONE_DARK = rgba("6e6254"), STONE_LIGHT = rgba("cabca8");
    private static final int BRONZE = rgba("c87a3a"), BRONZE_DARK = rgba("8a4a1e");
    private static final int VIOLET = rgba("6a3a8a"), VIOLET_DARK = rgba("42225a");

    private TemplePortraits() {}

    /** @return les pixels du portrait de l'ennemi {@code kind}, ou {@code null} s'il n'est pas du Temple. */
    public static int[][] pixels(EnemyKind kind) { return of(kind); }

    /** Ce qu'une cinématique peut retirer d'un portrait (voir {@link #without}). */
    public enum Prop { BELL, DIE_LEFT, DIE_RIGHT }

    /** Les accessoires retirés du portrait en cours de dessin (vide hors de {@link #without}). */
    private static java.util.Set<Prop> gone = java.util.EnumSet.noneOf(Prop.class);

    private static boolean has(Prop prop) { return !gone.contains(prop); }

    /**
     * @return le portrait de {@code kind} dessiné sans les accessoires {@code props} (ce qu'il y avait dessous
     * réapparaît) : les cinématiques en tirent le vrai morceau qui quitte le boss, jamais une copie.
     */
    public static synchronized int[][] without(EnemyKind kind, Prop... props) {
        gone = java.util.EnumSet.noneOf(Prop.class);
        java.util.Collections.addAll(gone, props);
        try {
            return of(kind);
        } finally {
            gone = java.util.EnumSet.noneOf(Prop.class);
        }
    }

    /** @return le portrait de l'ennemi {@code kind}, ou {@code null} s'il n'est pas du Temple du Destin. */
    static int[][] of(EnemyKind kind) {
        return switch (kind) {
            case MOINE_HASARD   -> moine();
            case MAITRE_CLOCHES -> maitreDesCloches();
            case ORACLE_AVEUGLE -> oracle();
            case TISSEUSE       -> tisseuse();
            case LANCEUR_DES    -> lanceur();
            case SEIGNEUR_DES   -> seigneurDesDes();
            case STATUE_ANIMEE  -> statue();
            case IDOLE_OR       -> idole();
            default             -> null;
        };
    }

    // -------------------------------------------------------------------------
    // Gabarit commun (comme ExpressPortraits)
    // -------------------------------------------------------------------------

    private static void bust(PixelCanvas g, int robe, int robeDark, int collar) {
        g.rect(4, 29, 29, 41, robe);
        g.rect(20, 29, 29, 41, robeDark);
        g.ellipseShaded(7f, 31f, 4.5f, 3.5f, robe, robeDark, 3f);
        g.ellipseShaded(27f, 31f, 4.5f, 3.5f, robeDark, robeDark, 0f);
        if (collar != 0) g.triangle(13, 28, 21, 28, 17, 35, collar);
    }

    private static void face(PixelCanvas g, int skin, int skinShade, boolean eyes) {
        g.rect(14, 24, 20, 28, skinShade);
        g.ellipseShaded(17f, 16f, 7f, 8.5f, skin, skinShade, 2f);
        if (eyes) {
            g.rect(13, 15, 14, 16, EYE);
            g.rect(20, 15, 21, 16, EYE);
        }
        g.set(17, 18, skinShade);
        g.line(15, 21, 19, 21, MOUTH);
    }

    /** Une robe de moine drapée en écharpe : un pan sombre de l'épaule gauche à la hanche droite. */
    private static void sash(PixelCanvas g, int color) {
        for (int k = 0; k < 12; k++) g.rect(8 + k, 29 + k, 10 + k, 29 + k, color);
    }

    // -------------------------------------------------------------------------
    // Le Parvis
    // -------------------------------------------------------------------------

    /** Moine du hasard : crâne rasé, robe safran en écharpe, un chapelet de dés au cou, une petite cloche. */
    private static int[][] moine() {
        PixelCanvas g = new PixelCanvas(W, H);
        bust(g, SAFFRON, SAFFRON_DARK, 0);
        sash(g, RED_DARK);
        face(g, SKIN, SKIN_SHADE, false);
        g.line(12, 15, 14, 16, EYE);                                  // les yeux mi-clos, sereins
        g.line(20, 16, 22, 15, EYE);
        g.ellipse(17f, 9f, 6f, 2f, rgba("f4cca4"));                  // le reflet du crâne rasé
        for (int x = 11; x <= 23; x += 3) g.rect(x, 27, x + 1, 28, IVORY); // le chapelet de dés
        g.rect(16, 30, 18, 32, IVORY);
        g.set(17, 31, RED);
        // La clochette à la main, à droite.
        g.ellipseShaded(29f, 35f, 2.5f, 2.5f, BRONZE, BRONZE_DARK, 1f);
        g.rect(27, 36, 31, 37, BRONZE);
        g.rect(29, 31, 29, 32, BRONZE_DARK);
        return g.outlined();
    }

    /**
     * Le Maître des cloches : un vieux moine au long sourcil blanc, sous un grand bourdon de bronze qui lui
     * fait comme un chapeau ; une corde de cloche passée à l'épaule.
     */
    private static int[][] maitreDesCloches() {
        PixelCanvas g = new PixelCanvas(W, H);
        bust(g, RED, RED_DARK, GOLD);
        face(g, SKIN, SKIN_SHADE, true);
        g.line(11, 13, 15, 14, IVORY);                                // les sourcils blancs, longs
        g.line(19, 14, 23, 13, IVORY);
        g.rect(14, 23, 20, 27, IVORY);                                // la barbe blanche
        g.triangle(14, 27, 20, 27, 17, 31, IVORY);
        g.line(15, 21, 19, 21, MOUTH);
        if (!has(Prop.BELL)) {                                        // sans son bourdon : un crâne chauve, deux touffes blanches
            g.ellipseShaded(17f, 9f, 7f, 2.5f, SKIN, SKIN_SHADE, 2f);
            g.ellipse(10f, 10.5f, 1.5f, 1.5f, IVORY);
            g.ellipse(24f, 10.5f, 1.5f, 1.5f, IVORY);
        }
        if (has(Prop.BELL)) {                                         // le bourdon de bronze, posé sur sa tête
            g.ellipseShaded(17f, 4f, 7f, 4f, BRONZE, BRONZE_DARK, 2f);
            g.rect(9, 4, 25, 8, BRONZE);
            g.rect(20, 4, 25, 8, BRONZE_DARK);
            g.rect(7, 8, 27, 9, GOLD_DARK);
            g.line(11, 6, 23, 6, GOLD);
            g.rect(16, 0, 18, 1, BRONZE_DARK);
        }
        // La corde de la cloche, sur l'épaule.
        for (int y = 9; y <= 40; y++) g.set(28 + (y % 3 == 0 ? 1 : 0), y, rgba("d8b878"));
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Le Jardin des oracles
    // -------------------------------------------------------------------------

    /** Oracle aveugle : les yeux bandés d'une étoffe violette brodée d'un œil d'or, des cheveux gris, un collier de perles. */
    private static int[][] oracle() {
        PixelCanvas g = new PixelCanvas(W, H);
        bust(g, VIOLET, VIOLET_DARK, 0);
        g.ellipse(17f, 12f, 8.5f, 8f, rgba("9a9aa8"));                // les cheveux gris
        face(g, SKIN, SKIN_SHADE, false);
        g.rect(9, 14, 25, 17, VIOLET);                                // le bandeau
        g.rect(9, 17, 25, 17, VIOLET_DARK);
        g.ellipse(17f, 15.5f, 2f, 1.2f, GOLD);                        // l'œil brodé
        g.set(17, 15, EYE);
        g.line(25, 16, 28, 22, VIOLET);                               // les pans du bandeau
        for (int x = 11; x <= 23; x += 2) g.set(x, 28, IVORY);       // les perles
        return g.outlined();
    }

    /**
     * La Tisseuse de destins : une dame aux cheveux noirs relevés, piqués d'aiguilles d'or ; ses yeux sont deux
     * bobines d'or, et des fils d'or tombent de ses mains en travers du buste.
     */
    private static int[][] tisseuse() {
        PixelCanvas g = new PixelCanvas(W, H);
        int hair = rgba("1e1424");
        bust(g, rgba("2a3a6a"), rgba("1a2448"), GOLD);
        g.ellipse(17f, 7f, 7f, 5f, hair);                             // le chignon
        g.ellipse(17f, 13f, 8f, 6f, hair);
        g.line(10, 1, 14, 6, GOLD);                                   // les aiguilles
        g.line(24, 1, 20, 6, GOLD);
        face(g, rgba("f0d0b8"), rgba("c8a08a"), false);
        g.ellipse(13.5f, 15.5f, 1.5f, 1.5f, GOLD);                    // ses yeux, deux bobines d'or
        g.ellipse(20.5f, 15.5f, 1.5f, 1.5f, GOLD);
        g.set(13, 15, GOLD_LIGHT); g.set(20, 15, GOLD_LIGHT);
        g.line(15, 21, 19, 21, RED);
        // Les fils d'or, de ses mains jusqu'en bas.
        for (int k = 0; k < 3; k++) g.line(6 + k * 4, 30, 14 + k * 7, 41, GOLD_LIGHT);
        g.line(28, 30, 22, 41, GOLD);
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // La Salle des dés
    // -------------------------------------------------------------------------

    /** Lanceur de dés : bandeau rouge, sourire en coin, un dé d'ivoire entre les doigts levés. */
    private static int[][] lanceur() {
        PixelCanvas g = new PixelCanvas(W, H);
        bust(g, rgba("3a5a3a"), rgba("243a24"), IVORY);
        g.ellipse(17f, 11f, 8f, 6f, rgba("3a2418"));                  // les cheveux
        face(g, SKIN, SKIN_SHADE, true);
        g.rect(9, 9, 25, 10, RED);                                    // le bandeau
        g.line(25, 10, 28, 13, RED);
        g.line(15, 21, 19, 20, MOUTH);                                // le sourire en coin
        // Le dé levé, à droite, dans sa main.
        g.ellipseShaded(28f, 31f, 3f, 3f, SKIN, SKIN_SHADE, 1f);
        g.rect(26, 23, 31, 28, IVORY);
        g.rect(30, 23, 31, 28, IVORY_SHADE);
        g.set(27, 24, RED); g.set(29, 26, RED); g.set(27, 26, RED); g.set(29, 24, RED);
        return g.outlined();
    }

    /**
     * Le Seigneur des Dés : un seigneur à la couronne en forme de deux dés d'or, la moustache fine, un
     * manteau rouge brodé de points de dés ; un sourire de joueur qui ne perd jamais.
     */
    private static int[][] seigneurDesDes() {
        PixelCanvas g = new PixelCanvas(W, H);
        bust(g, RED, RED_DARK, IVORY);
        for (int x = 6; x <= 28; x += 5) for (int y = 32; y <= 40; y += 4) g.set(x, y, IVORY); // les points de dés
        face(g, SKIN, SKIN_SHADE, true);
        g.line(13, 19, 16, 20, rgba("2a1a10"));                       // la moustache
        g.line(18, 20, 21, 19, rgba("2a1a10"));
        g.line(15, 22, 19, 22, MOUTH);
        // La couronne : deux dés d'or penchés.
        for (int[] d : new int[][] {{9, 1}, {18, 2}}) {
            if (!has(d[0] == 9 ? Prop.DIE_LEFT : Prop.DIE_RIGHT)) continue;
            g.rect(d[0], d[1], d[0] + 7, d[1] + 6, GOLD);
            g.rect(d[0] + 6, d[1], d[0] + 7, d[1] + 6, GOLD_DARK);
            g.set(d[0] + 2, d[1] + 2, RED_DARK); g.set(d[0] + 4, d[1] + 4, RED_DARK);
        }
        g.rect(9, 8, 25, 9, GOLD_DARK);
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Le Sanctuaire
    // -------------------------------------------------------------------------

    /** Statue animée : un gardien de pierre fissurée, des yeux qui luisent d'or, de la mousse sur l'épaule. */
    private static int[][] statue() {
        PixelCanvas g = new PixelCanvas(W, H);
        bust(g, STONE, STONE_DARK, 0);
        g.rect(9, 6, 25, 26, STONE);                                  // la tête carrée
        g.rect(20, 6, 25, 26, STONE_DARK);
        g.rect(9, 6, 25, 7, STONE_LIGHT);
        g.rect(11, 13, 23, 14, STONE_DARK);                           // les arcades
        g.rect(12, 15, 14, 16, GOLD); g.rect(20, 15, 22, 16, GOLD);  // les yeux qui luisent
        g.rect(13, 20, 21, 22, STONE_DARK);                           // la bouche en grille
        for (int x = 14; x <= 20; x += 2) g.set(x, 21, STONE);
        g.line(11, 8, 15, 12, STONE_DARK);                            // une fissure
        g.line(15, 12, 13, 17, STONE_DARK);
        g.ellipse(6f, 30f, 3f, 1.5f, rgba("4a7a3a"));                 // la mousse
        return g.outlined();
    }

    /**
     * L'Idole d'Or : une idole aux quatre bras, coiffée d'une haute tiare, un troisième œil de rubis au front ;
     * dans ses mains, une roue du Karma et un dé.
     */
    private static int[][] idole() {
        PixelCanvas g = new PixelCanvas(W, H);
        bust(g, GOLD, GOLD_DARK, 0);
        // Les bras du haut, levés de chaque côté.
        g.rect(1, 18, 4, 30, GOLD_DARK);
        g.rect(30, 18, 33, 30, GOLD_DARK);
        g.ellipseShaded(2.5f, 17f, 2.5f, 2.5f, GOLD, GOLD_DARK, 1f);
        g.ellipseShaded(31.5f, 17f, 2.5f, 2.5f, GOLD, GOLD_DARK, 1f);
        g.ellipse(2.5f, 13f, 2.5f, 2.5f, RED);                        // la roue du Karma (simplifiée)
        g.set(2, 13, GOLD_LIGHT);
        g.rect(30, 11, 33, 14, IVORY);                                // le dé
        g.set(31, 12, RED);
        face(g, GOLD, GOLD_DARK, false);
        g.rect(13, 15, 14, 16, rgba("3a2a08"));                       // les yeux mi-clos
        g.rect(20, 15, 21, 16, rgba("3a2a08"));
        g.ellipse(17f, 11.5f, 1.2f, 1.5f, RED);                       // le troisième œil
        g.line(15, 21, 19, 21, GOLD_DARK);
        // La tiare.
        g.triangle(10, 9, 24, 9, 17, -2, GOLD);
        g.triangle(20, 9, 24, 9, 17, -2, GOLD_DARK);
        g.rect(10, 8, 24, 9, GOLD_DARK);
        g.set(17, 4, RED);
        g.set(17, 1, GOLD_LIGHT);
        // Les mains du bas, jointes sur le buste.
        g.ellipseShaded(17f, 34f, 4f, 2.5f, GOLD, GOLD_DARK, 2f);
        return g.outlined();
    }
}
