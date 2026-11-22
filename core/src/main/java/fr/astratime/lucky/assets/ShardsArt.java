package fr.astratime.lucky.assets;

import java.util.Random;

import static fr.astratime.lucky.assets.PixelCanvas.rgba;

/**
 * Dessins en pixel art du chapitre « Les Éclats dispersés » de la Tour des épreuves (entre le Dernier Tirage et
 * le Monde sans Maître) : ses cinq ennemis (en buste, à la taille du croupier), leurs quatre symboles et
 * l'illustration du chapitre. Chaque méthode rend une grille de couleurs RGBA8888 (0 = transparent),
 * transformée en texture par {@link EnemyTextures}.
 */
public final class ShardsArt {

    private static final int W = EnemyPortraits.WIDTH, H = EnemyPortraits.HEIGHT;

    private static final int SKIN = rgba("f0c8a0"), SKIN_SHADE = rgba("c89a72");
    private static final int EYE  = rgba("1a1018"), MOUTH = rgba("7a2a20"), WHITE = rgba("f6f0e2");
    private static final int GOLD = rgba("ffd54a"), GOLD_DARK = rgba("c4891e"), GOLD_LIGHT = rgba("fff6c8");
    private static final int STEEL = rgba("b8c0cc"), STEEL_DARK = rgba("7a8494");
    private static final int WOOD = rgba("8a5a2a"), WOOD_DARK = rgba("5e3a18");
    private static final int RED = rgba("d8202c"), RED_DARK = rgba("8a1a1a");

    /**
     * Les couleurs des éclats de la vitrine du Collectionneur, dans l'ordre où il les sort : l'Héritage (bleu),
     * la Valse (rose), le Scellé (vert). Les mêmes que les symboles de ses challengers.
     */
    public static final String[] VITRINE_COLORS = {"5ab0ff", "ff6a9e", "5ee08a"};
    /** Où sont sertis les trois éclats sur la poitrine du Collectionneur (pixels de son portrait, x puis y). */
    public static final int[][] VITRINE_AT = {{11, 34}, {17, 35}, {23, 34}};

    private ShardsArt() {}

    /** Un éclat taillé (losange) de centre ({@code x}, {@code y}) dans la couleur {@code hex}. */
    private static void shard(PixelCanvas g, int x, int y, String hex) {
        int light = rgba(hex), dark = shade(hex, 0.65f);
        g.triangle(x - 2, y, x + 2, y, x, y - 3, light);
        g.triangle(x - 2, y, x + 2, y, x, y + 2, dark);
        g.triangle(x, y, x + 2, y, x, y - 3, dark);
        g.set(x - 1, y - 1, rgba("ffffff"));
    }

    /** @return {@code hex} assombri d'autant ({@code factor} de 0 à 1). */
    private static int shade(String hex, float factor) {
        int value = Integer.parseInt(hex, 16);
        int r = (int) (((value >> 16) & 0xff) * factor), gr = (int) (((value >> 8) & 0xff) * factor), b = (int) ((value & 0xff) * factor);
        return (r << 24) | (gr << 16) | (b << 8) | 0xff;
    }

    // -------------------------------------------------------------------------
    // Les ennemis
    // -------------------------------------------------------------------------

    /**
     * Ramasseur d'éclats : chapeau de feutre cabossé, lunettes de soudeur relevées sur le front, barbe de
     * trois jours ; une hotte d'osier pleine d'éclats dorés sur le dos, et une longue pince qui en tient un.
     */
    public static int[][] ramasseur() {
        PixelCanvas g = new PixelCanvas(W, H);
        int coat = rgba("6e6250"), coatDark = rgba("4a4034"), hat = rgba("5a4632"), hatDark = rgba("3a2c1e");
        int wicker = rgba("c89a52"), wickerDark = rgba("8a6634"), glass = rgba("7ad0e0"), stubble = rgba("a88a6e");

        // La hotte d'osier, derrière l'épaule droite, pleine d'éclats.
        g.rect(22, 14, 31, 33, wicker);
        g.rect(28, 14, 31, 33, wickerDark);
        for (int y = 16; y <= 32; y += 3) g.line(22, y, 31, y, wickerDark);
        shard(g, 25, 12, "ffd54a");
        shard(g, 29, 11, "fff6c8");
        shard(g, 27, 9, "ffb03a");
        // Buste : vieux manteau rapiécé, bretelle de la hotte.
        g.rect(4, 29, 29, 41, coat);
        g.rect(20, 29, 29, 41, coatDark);
        g.ellipseShaded(7f, 31f, 4.5f, 3.5f, coat, coatDark, 3f);
        g.ellipseShaded(27f, 31f, 4.5f, 3.5f, coatDark, coatDark, 0f);
        g.line(21, 28, 14, 41, WOOD_DARK);
        g.line(22, 28, 15, 41, WOOD_DARK);
        g.rect(7, 35, 10, 38, rgba("8a7a5a"));
        // Visage buriné, barbe de trois jours.
        g.rect(14, 24, 20, 28, SKIN_SHADE);
        g.ellipseShaded(17f, 17f, 6.5f, 8f, SKIN, SKIN_SHADE, 2f);
        for (int x = 12; x <= 22; x += 2) { g.set(x, 21, stubble); g.set(x + 1, 23, stubble); }
        g.set(14, 24, stubble); g.set(20, 24, stubble);
        // Yeux plissés, à l'affût.
        g.line(12, 15, 15, 15, EYE);
        g.line(19, 15, 22, 15, EYE);
        g.set(14, 16, EYE); g.set(20, 16, EYE);
        g.line(15, 22, 19, 22, MOUTH);
        g.set(14, 21, MOUTH);
        // Lunettes de soudeur relevées sur le front.
        g.rect(11, 11, 23, 12, rgba("3a2a1e"));
        g.ellipse(14f, 11.5f, 2f, 1.8f, STEEL_DARK);
        g.ellipse(20f, 11.5f, 2f, 1.8f, STEEL_DARK);
        g.set(14, 11, glass); g.set(20, 11, glass);
        // Chapeau de feutre cabossé.
        g.ellipse(17f, 9f, 10f, 2f, hatDark);
        g.rect(11, 3, 23, 8, hat);
        g.rect(19, 3, 23, 8, hatDark);
        g.line(11, 3, 23, 3, rgba("6e5a42"));
        g.set(16, 3, 0); g.set(17, 4, hatDark);
        // La pince, tenue à deux mains, qui serre un éclat.
        g.ellipse(5f, 36f, 2.5f, 2.2f, SKIN);
        g.line(5, 35, 2, 22, STEEL);
        g.line(6, 35, 4, 22, STEEL_DARK);
        shard(g, 3, 19, "ffd54a");
        return g.outlined();
    }

    /**
     * Héritier ruiné : haut-de-forme percé, monocle fêlé, frac élimé aux revers défraîchis ; il tend les
     * poches de son pantalon retournées, vides, et une chevalière trop grande glisse de son doigt.
     */
    public static int[][] heritier() {
        PixelCanvas g = new PixelCanvas(W, H);
        int tail = rgba("2a2a3a"), tailDark = rgba("1a1a26"), shirt = rgba("e6e0d2"), shirtDark = rgba("b8b0a2");
        int hat = rgba("26262e"), hatLight = rgba("40404a"), hair = rgba("c8a050"), bow = rgba("5a2a8a");

        // Frac élimé, plastron froissé, nœud papillon de travers.
        g.rect(4, 29, 29, 41, tail);
        g.rect(20, 29, 29, 41, tailDark);
        g.ellipseShaded(7f, 31f, 4.5f, 3.5f, tail, tailDark, 3f);
        g.ellipseShaded(27f, 31f, 4.5f, 3.5f, tailDark, tailDark, 0f);
        g.triangle(12, 28, 22, 28, 17, 41, shirt);
        g.triangle(17, 28, 22, 28, 17, 41, shirtDark);
        g.rect(14, 28, 20, 29, bow);
        g.set(14, 30, bow);
        g.set(9, 33, rgba("4a4a5a")); g.set(26, 36, rgba("4a4a5a")); // trous dans le frac
        // Les poches retournées, blanches et vides, de chaque côté.
        g.rect(3, 37, 7, 41, WHITE);
        g.rect(26, 37, 30, 41, shirtDark);
        g.set(5, 41, rgba("b8b0a2"));
        // Visage pâle, joues creuses, mèche blonde.
        g.rect(14, 24, 20, 28, SKIN_SHADE);
        g.ellipseShaded(17f, 17f, 6f, 8f, rgba("f4dcc4"), SKIN_SHADE, 2f);
        g.rect(11, 11, 14, 15, hair);
        g.set(15, 12, hair);
        // Œil triste à gauche, monocle fêlé à droite, larme.
        g.line(12, 15, 15, 16, EYE);
        g.rect(13, 17, 14, 17, EYE);
        g.ellipse(20.5f, 17f, 2.5f, 2.5f, GOLD_DARK);
        g.ellipse(20.5f, 17f, 1.6f, 1.6f, rgba("b8e0f0"));
        g.line(19, 16, 22, 18, rgba("5a7a8a"));
        g.line(22, 19, 24, 27, GOLD_DARK); // la chaîne du monocle
        g.set(13, 19, rgba("8ac8f0"));
        g.line(15, 23, 19, 22, MOUTH);
        // Haut-de-forme percé, la calotte enfoncée.
        g.rect(10, 9, 24, 10, hat);
        g.rect(12, 0, 22, 8, hat);
        g.rect(12, 0, 13, 8, hatLight);
        g.rect(12, 7, 22, 8, rgba("6a2a2a"));
        g.set(19, 3, 0); g.set(20, 3, 0); g.set(19, 4, 0); // le trou
        g.set(18, 0, 0); g.set(22, 1, 0);
        // La main tendue, la chevalière d'or qui glisse.
        g.ellipse(9f, 32f, 2.5f, 2.2f, SKIN);
        g.ellipse(9f, 34f, 1.4f, 1.2f, GOLD);
        g.set(9, 34, RED);
        return g.outlined();
    }

    /**
     * Veuve joyeuse : robe de deuil noire, chapeau à large bord et voilette à résille, grand sourire rouge et
     * clin d'œil ; elle agite un éventail de dentelle noire et tient un bouquet de lys blancs.
     */
    public static int[][] veuve() {
        PixelCanvas g = new PixelCanvas(W, H);
        int dress = rgba("1e1a24"), dressLight = rgba("2e2838"), lace = rgba("4a4258"), veil = rgba("3a3444");
        int lips = rgba("e0283a"), lily = rgba("f6f2ea"), leaf = rgba("3a8a4a"), pearl = rgba("f0ece4");

        // Robe noire, col montant de dentelle, collier de perles.
        g.rect(4, 29, 29, 41, dress);
        g.rect(4, 29, 12, 41, dressLight);
        g.ellipseShaded(7f, 31f, 4.5f, 3.5f, dressLight, dress, 3f);
        g.ellipseShaded(27f, 31f, 4.5f, 3.5f, dress, dress, 0f);
        g.rect(13, 25, 21, 28, lace);
        for (int x = 12; x <= 22; x += 2) g.set(x, 29, pearl);
        // Visage poudré, sourire immense, clin d'œil.
        g.ellipseShaded(17f, 17f, 6.5f, 8f, rgba("f6e0d0"), SKIN_SHADE, 2f);
        g.rect(10, 12, 11, 22, dress);
        g.rect(23, 12, 24, 22, dress);
        g.line(12, 15, 15, 15, EYE);
        g.set(12, 14, EYE); g.set(15, 14, EYE);  // l'œil fermé du clin d'œil
        g.rect(19, 15, 21, 16, EYE);
        g.set(20, 15, WHITE);
        g.line(19, 13, 22, 12, EYE);
        g.set(12, 18, rgba("f0a0a8")); g.set(22, 18, rgba("f0a0a8"));
        g.line(14, 21, 20, 21, lips);
        g.line(15, 22, 19, 22, RED_DARK);
        g.set(13, 20, lips); g.set(21, 20, lips);
        // Chapeau à large bord, plume, voilette à résille sur les yeux.
        g.ellipse(17f, 9f, 12f, 2.5f, dress);
        g.ellipse(17f, 6f, 7f, 4f, dress);
        g.line(22, 5, 29, 0, lace);
        g.line(23, 6, 30, 2, lace);
        for (int x = 11; x <= 23; x += 2) { g.set(x, 11, veil); g.set(x + 1, 12, veil); }
        // L'éventail de dentelle, ouvert, à gauche.
        for (int i = 0; i < 6; i++) g.line(6, 34, 1 + i, 24 + (i == 0 || i == 5 ? 2 : 0), i % 2 == 0 ? lace : dress);
        g.ellipse(6f, 35f, 2.2f, 2f, rgba("f6e0d0"));
        // Le bouquet de lys blancs, à droite.
        g.line(25, 41, 26, 32, leaf);
        g.line(27, 41, 27, 32, leaf);
        g.ellipse(25f, 31f, 2f, 1.6f, lily);
        g.ellipse(28.5f, 30f, 2f, 1.6f, lily);
        g.ellipse(27f, 28f, 1.8f, 1.5f, lily);
        g.set(25, 31, GOLD); g.set(28, 30, GOLD); g.set(27, 28, GOLD);
        g.ellipse(27f, 36f, 2.5f, 2f, rgba("f6e0d0"));
        return g.outlined();
    }

    /**
     * Notaire du hasard : crâne dégarni et favoris gris, pince-nez sur le bout du nez, plume d'oie derrière
     * l'oreille ; il brandit un gros tampon de bois, et un acte scellé de cire rouge dépasse de sa poche.
     */
    public static int[][] notaire() {
        PixelCanvas g = new PixelCanvas(W, H);
        int suit = rgba("2a3a2e"), suitDark = rgba("1a2620"), shirt = rgba("e6e0d2"), grey = rgba("a8a8a8");
        int greyDark = rgba("7a7a7a"), paper = rgba("f0e6c8"), wax = rgba("c8202c"), ink = rgba("1e2a5a");

        // Costume vert bouteille, gilet, col cassé.
        g.rect(4, 29, 29, 41, suit);
        g.rect(20, 29, 29, 41, suitDark);
        g.ellipseShaded(7f, 31f, 4.5f, 3.5f, suit, suitDark, 3f);
        g.ellipseShaded(27f, 31f, 4.5f, 3.5f, suitDark, suitDark, 0f);
        g.triangle(13, 28, 21, 28, 17, 33, shirt);
        g.rect(16, 28, 18, 30, ink);
        // L'acte plié, scellé de cire, dans la poche de poitrine.
        g.rect(23, 30, 26, 38, paper);
        g.rect(23, 30, 23, 38, rgba("c8b890"));
        g.line(22, 34, 27, 34, wax);
        g.set(27, 35, wax); g.set(28, 36, wax);
        // Crâne dégarni, favoris gris.
        g.rect(14, 24, 20, 28, SKIN_SHADE);
        g.ellipseShaded(17f, 16f, 7f, 8.5f, SKIN, SKIN_SHADE, 2f);
        g.rect(10, 13, 11, 22, grey);
        g.rect(23, 13, 24, 22, greyDark);
        g.set(15, 9, rgba("fff0e0")); g.set(16, 9, rgba("fff0e0")); // reflet sur le crâne
        // Sourcils broussailleux, pince-nez, regard par-dessus.
        g.line(12, 14, 15, 14, grey);
        g.line(19, 14, 22, 14, grey);
        g.rect(13, 15, 14, 16, EYE);
        g.rect(20, 15, 21, 16, EYE);
        g.ellipse(14f, 18f, 1.8f, 1.2f, STEEL);
        g.ellipse(20f, 18f, 1.8f, 1.2f, STEEL);
        g.line(15, 18, 19, 18, STEEL_DARK);
        g.line(15, 22, 19, 22, MOUTH);
        g.line(13, 23, 15, 22, grey); g.line(19, 22, 21, 23, grey); // moustache tombante
        // Plume d'oie derrière l'oreille.
        g.line(24, 16, 30, 6, WHITE);
        g.line(25, 15, 31, 7, rgba("d8d0c0"));
        g.set(24, 17, ink);
        // Le gros tampon de bois, brandi à gauche.
        g.ellipse(5f, 37f, 2.5f, 2.2f, SKIN);
        g.rect(4, 26, 6, 35, WOOD);
        g.ellipse(5f, 25f, 2.5f, 2f, WOOD_DARK);
        g.rect(1, 34, 9, 36, WOOD_DARK);
        g.rect(1, 37, 9, 38, wax);
        return g.outlined();
    }

    /**
     * Collectionneur : grand et maigre, cheveux argent tirés en arrière, loupe de joaillier vissée sur l'œil,
     * longue redingote de velours prune ; trois éclats sont sertis sur sa poitrine (bleu, rose, vert). Derrière
     * lui, sa vitrine de verre : des étagères d'éclats étiquetés.
     */
    public static int[][] collectionneur() { return collectionneur(VITRINE_AT.length); }

    /** Le Collectionneur avec seulement ses {@code gems} premiers éclats sertis (les autres sertis restent vides). */
    public static int[][] collectionneur(int gems) {
        PixelCanvas g = new PixelCanvas(W, H);
        int velvet = rgba("5a2a5e"), velvetDark = rgba("3a1a3e"), silver = rgba("d8dce4"), silverDark = rgba("9aa0aa");
        int glassFrame = rgba("8a6a3a"), glassPane = rgba("2a3a4a"), label = rgba("f0e6c8"), cravat = rgba("e6e0d2");

        // La vitrine derrière lui : cadre de bois doré, verre sombre, étagères d'éclats.
        g.rect(0, 2, 33, 30, glassFrame);
        g.rect(1, 3, 32, 29, glassPane);
        for (int y = 10; y <= 24; y += 7) g.line(1, y, 32, y, glassFrame);
        String[] colors = {"ffd54a", "e0283a", "fff6c8", "c8a0ff", "5ab0ff", "ff8a1e"};
        int c = 0;
        for (int y = 8; y <= 22; y += 7) {
            for (int x = 3; x <= 30; x += 27) {
                shard(g, x, y, colors[c++ % colors.length]);
                g.set(x - 1, y + 2, label); g.set(x, y + 2, label);
            }
        }
        g.line(2, 4, 6, 4, rgba("6a8aa0")); // reflet sur le verre
        // Longue redingote de velours, revers brodés d'argent.
        g.rect(4, 29, 29, 41, velvet);
        g.rect(20, 29, 29, 41, velvetDark);
        g.ellipseShaded(7f, 31f, 4.5f, 3.5f, velvet, velvetDark, 3f);
        g.ellipseShaded(27f, 31f, 4.5f, 3.5f, velvetDark, velvetDark, 0f);
        g.line(12, 29, 9, 41, silverDark);
        g.line(22, 29, 25, 41, silverDark);
        g.triangle(14, 28, 20, 28, 17, 32, cravat);
        g.set(17, 30, GOLD);
        // Les trois éclats de sa vitrine personnelle, sertis sur sa poitrine.
        for (int i = 0; i < VITRINE_AT.length; i++) {
            g.ellipse(VITRINE_AT[i][0], VITRINE_AT[i][1], 3f, 3f, GOLD_DARK);
            if (i < gems) shard(g, VITRINE_AT[i][0], VITRINE_AT[i][1] + 1, VITRINE_COLORS[i]);
            else g.ellipse(VITRINE_AT[i][0], VITRINE_AT[i][1], 1.5f, 1.5f, rgba("2a1a14")); // le serti vide
        }
        // Visage long et maigre, cheveux argent tirés en arrière.
        g.rect(15, 24, 19, 28, SKIN_SHADE);
        g.ellipseShaded(17f, 16f, 5.5f, 8.5f, SKIN, SKIN_SHADE, 1.5f);
        g.ellipse(17f, 8.5f, 6f, 2.5f, silver);
        g.rect(11, 8, 12, 15, silver);
        g.rect(22, 8, 23, 15, silverDark);
        g.line(13, 8, 21, 8, rgba("f4f6fa"));
        // Œil gauche mi-clos, loupe de joaillier noire vissée sur l'œil droit, qui brille.
        g.line(13, 15, 15, 15, EYE);
        g.set(14, 16, EYE);
        g.ellipse(LOUPE_X - 0.5f, LOUPE_Y - 0.5f, 2.6f, 2.6f, rgba("1e1a24"));
        g.ellipse(LOUPE_X - 0.5f, LOUPE_Y - 0.5f, 1.6f, 1.6f, rgba("8ad0ff"));
        g.set(19, 15, rgba("ffffff"));
        g.set(17, 19, SKIN_SHADE);
        g.line(15, 22, 19, 22, MOUTH);
        g.set(19, 21, MOUTH); // sourire en coin
        // Ses longs doigts gantés, qui tiennent une pince à éclats.
        g.ellipse(6f, 37f, 2.2f, 2f, rgba("e6e0d2"));
        g.line(6, 36, 9, 29, GOLD_DARK);
        g.set(9, 28, GOLD_LIGHT);
        return g.outlined();
    }

    /** Taille de l'éclat serti {@code i} sur sa poitrine ; son coin haut-gauche est au pixel {@link #chestGemLeft}, {@link #chestGemTop}. */
    /** Le centre de sa loupe de joaillier, en pixels du portrait (bord gauche et haut des pixels : x 20,5 = milieu du pixel 20). */
    public static final float LOUPE_X = 20.5f, LOUPE_Y = 16f;

    public static final int CHEST_GEM_W = 5, CHEST_GEM_H = 6;
    public static int chestGemLeft(int i) { return VITRINE_AT[i][0] - 2; }
    public static int chestGemTop(int i)  { return VITRINE_AT[i][1] - 2; }

    /** @return l'éclat {@code i} de sa poitrine, seul : exactement les pixels de son portrait (pour le faire voler). */
    public static int[][] chestGem(int i) {
        PixelCanvas g = new PixelCanvas(CHEST_GEM_W, CHEST_GEM_H);
        shard(g, 2, 3, VITRINE_COLORS[i]);
        return g.pixels;
    }

    // -------------------------------------------------------------------------
    // Les symboles (16 x 16)
    // -------------------------------------------------------------------------

    /** Éclat : un grand éclat doré qui brille, deux petits qui le rejoignent. */
    public static int[][] shardSymbol() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.triangle(4, 8, 11, 8, 8, 1, GOLD);
        g.triangle(4, 8, 11, 8, 8, 14, GOLD_DARK);
        g.triangle(8, 8, 11, 8, 8, 1, rgba("e8b43a"));
        g.line(6, 6, 8, 3, GOLD_LIGHT);
        g.set(7, 5, rgba("ffffff"));
        g.triangle(0, 4, 3, 4, 1, 1, GOLD_LIGHT);
        g.triangle(12, 12, 15, 12, 14, 15, GOLD);
        return g.outlined();
    }

    /** Héritage : un bouclier bleu fendu en deux, une moitié qui s'en va. */
    public static int[][] legacy() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int blue = rgba("5ab0ff"), blueDark = rgba("2a6ab0");
        g.rect(1, 2, 6, 9, blue);
        g.triangle(1, 9, 6, 9, 6, 14, blue);
        g.rect(1, 2, 6, 3, rgba("a8d8ff"));
        g.rect(9, 1, 14, 8, blueDark);
        g.triangle(9, 8, 14, 8, 9, 13, blueDark);
        g.line(7, 3, 8, 12, rgba("1a1018"));
        g.set(11, 4, GOLD); g.set(12, 5, GOLD);
        return g.outlined();
    }

    /** Valse : un éventail noir de veuve et un cœur rose qui danse. */
    public static int[][] waltz() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int black = rgba("2e2838"), lace = rgba("5a5068"), pink = rgba("ff6a9e");
        for (int i = 0; i < 7; i++) g.line(8, 14, 1 + i * 2, 4 + Math.abs(3 - i), i % 2 == 0 ? lace : black);
        g.ellipse(8f, 14f, 1.5f, 1.2f, GOLD);
        g.ellipse(11f, 3f, 1.6f, 1.6f, pink);
        g.ellipse(14f, 3f, 1.6f, 1.6f, pink);
        g.triangle(10, 4, 15, 4, 12, 8, pink);
        g.set(11, 2, rgba("ffd0e0"));
        return g.outlined();
    }

    /** Scellé : un sceau de cire rouge, tamponné sur un acte, une main de moins gravée dessus. */
    public static int[][] seal() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int paper = rgba("f0e6c8"), paperDark = rgba("c8b890");
        g.rect(1, 1, 12, 14, paper);
        g.rect(10, 1, 12, 14, paperDark);
        for (int y = 3; y <= 7; y += 2) g.line(3, y, 9, y, paperDark);
        g.ellipse(10f, 11f, 4.5f, 4f, RED);
        g.ellipse(10f, 11f, 3f, 2.5f, RED_DARK);
        g.line(8, 11, 12, 11, rgba("ffb0a0")); // le « moins » gravé dans la cire
        g.set(7, 8, rgba("ff6a5a"));
        return g.outlined();
    }

    /** Couleur de chaque symbole du chapitre, pour les éclats des cinématiques (Éclat, Héritage, Valse, Scellé). */
    public static final String[] SYMBOL_COLORS = {"ffd54a", "5ab0ff", "ff6a9e", "5ee08a"};

    // -------------------------------------------------------------------------
    // L'illustration du chapitre (128 x 80)
    // -------------------------------------------------------------------------

    /**
     * Les Éclats dispersés : la nuit après la chute de l'Éclat Originel. Au loin, le cratère fume encore ; sur la
     * plaine, des éclats de toutes les couleurs brillent dans l'herbe brûlée. Des silhouettes de ramasseurs, lanterne
     * à la main, les cherchent. Au premier plan, une vitrine de verre, ouverte, attend.
     */
    static int[][] scatteredShards() {
        int w = EnemyPortraits.ART_WIDTH, h = EnemyPortraits.ART_HEIGHT;
        PixelCanvas g = new PixelCanvas(w, h);
        int[] sky = {rgba("0a0a1e"), rgba("12122a"), rgba("1a1836"), rgba("241e42"), rgba("32244a"), rgba("44284e")};
        for (int y = 0; y < h; y++) {
            int band = Math.min(sky.length - 1, y * sky.length / 50);
            for (int x = 0; x < w; x++) g.set(x, y, sky[band]);
        }
        Random random = new Random(55);
        for (int i = 0; i < 40; i++) g.set(random.nextInt(w), random.nextInt(30), rgba(random.nextBoolean() ? "c8c8e8" : "fff6c8"));
        // Le cratère au loin, et sa colonne de fumée lumineuse.
        int hill = rgba("1a1426"), hillLight = rgba("2a2036");
        g.ellipse(64f, 50f, 60f, 10f, hill);
        g.ellipse(64f, 44f, 16f, 4f, hillLight);
        g.ellipse(64f, 44f, 11f, 2.5f, rgba("ffb03a"));
        g.ellipse(64f, 44f, 6f, 1.5f, rgba("fff6c8"));
        for (int i = 0; i < 9; i++) {
            float y = 40 - i * 4.5f, x = 64 + (i % 2 == 0 ? -1 : 2) + i * 0.8f;
            g.ellipse(x, y, 3f + i * 0.9f, 2.5f + i * 0.4f, i < 3 ? rgba("6a4a5a") : rgba("3a2e4a"));
        }
        // La plaine d'herbe brûlée.
        g.rect(0, 50, w - 1, h - 1, rgba("1e1a1a"));
        g.rect(0, 50, w - 1, 51, rgba("2e2626"));
        for (int i = 0; i < 90; i++) g.set(random.nextInt(w), 52 + random.nextInt(28), rgba(random.nextBoolean() ? "2a2420" : "362e26"));
        // Les éclats qui brillent dans l'herbe, de toutes les couleurs.
        String[] colors = {"ffd54a", "e0283a", "fff6c8", "5ab0ff", "ff6a9e", "5ee08a", "c8a0ff"};
        int[][] at = {{8, 58}, {22, 66}, {37, 56}, {48, 72}, {74, 60}, {86, 70}, {98, 57}, {112, 64}, {120, 74}, {30, 76}, {58, 64}};
        for (int i = 0; i < at.length; i++) {
            String hex = colors[i % colors.length];
            g.set(at[i][0] - 2, at[i][1], rgba(hex, 120)); g.set(at[i][0] + 2, at[i][1], rgba(hex, 120));
            g.set(at[i][0], at[i][1] - 3, rgba(hex, 120));
            shard(g, at[i][0], at[i][1], hex);
        }
        // Des silhouettes de ramasseurs, penchés, lanterne à la main.
        int figure = rgba("0e0a12");
        int[][] people = {{18, 52}, {44, 51}, {92, 52}};
        for (int[] p : people) {
            g.ellipse(p[0], p[1] - 6, 1.8f, 1.8f, figure);
            g.triangle(p[0] - 3, p[1] + 2, p[0] + 3, p[1] + 2, p[0] + 1, p[1] - 5, figure);
            g.line(p[0] + 2, p[1] - 2, p[0] + 5, p[1] + 1, figure);
            g.ellipse(p[0] + 5, p[1] + 2, 1.2f, 1.4f, rgba("ffc93a"));
        }
        // Au premier plan, à droite, la vitrine du Collectionneur, ouverte, qui attend ses éclats.
        int frame = rgba("8a6a3a"), frameDark = rgba("5a4224"), pane = rgba("2a3a4a", 200);
        g.rect(102, 30, 124, 78, frameDark);
        g.rect(104, 32, 122, 76, pane);
        for (int y = 42; y <= 66; y += 12) g.line(104, y, 122, y, frame);
        g.rect(102, 30, 124, 31, frame);
        g.line(104, 33, 107, 33, rgba("6a8aa0"));
        int[][] slots = {{108, 39}, {116, 39}, {108, 51}, {116, 63}};
        for (int[] s : slots) g.ellipse(s[0], s[1], 2f, 1.5f, rgba("4a5a6a"));
        shard(g, 116, 51, "fff6c8");
        g.line(96, 30, 102, 34, frame); // la porte de verre, ouverte
        g.line(96, 30, 96, 74, frame);
        g.line(96, 74, 102, 78, frame);
        return g.pixels;
    }
}
