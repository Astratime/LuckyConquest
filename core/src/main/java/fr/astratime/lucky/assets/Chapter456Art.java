package fr.astratime.lucky.assets;

import java.util.Random;

import static fr.astratime.lucky.assets.PixelCanvas.rgba;

/**
 * Dessins en pixel art des chapitres 4 à 6 de la Tour des épreuves : « Le
 * Monde sans Maître », « La Maison » et « Le Jackpot ». Leurs ennemis (en
 * buste, à la taille du croupier), leurs symboles et les illustrations des
 * chapitres. Chaque méthode rend une grille de couleurs RGBA8888 (0 =
 * transparent), transformée en texture par {@link EnemyTextures}.
 */
final class Chapter456Art {

    private static final int W = EnemyPortraits.WIDTH, H = EnemyPortraits.HEIGHT;

    private static final int SKIN = rgba("f0c8a0"), SKIN_SHADE = rgba("c89a72");
    private static final int EYE  = rgba("1a1018"), MOUTH = rgba("7a2a20"), WHITE = rgba("f6f0e2");
    private static final int GOLD = rgba("ffd54a"), GOLD_DARK = rgba("c4891e"), GOLD_LIGHT = rgba("fff6c8");
    private static final int STEEL = rgba("b8c0cc"), STEEL_DARK = rgba("7a8494"), STEEL_LIGHT = rgba("e8eef8");
    private static final int WOOD = rgba("8a5a2a"), WOOD_DARK = rgba("5e3a18");
    private static final int RED = rgba("d8202c"), RED_DARK = rgba("8a1a1a");

    private Chapter456Art() {}

    // -------------------------------------------------------------------------
    // Chapitre 4, « Le Monde sans Maître »
    // -------------------------------------------------------------------------

    /**
     * Pilleur : capuche brune, bandana rouge sur le bas du visage, regard
     * fuyant ; il porte sur l'épaule un sac de toile d'où dépassent des cartes
     * volées à la boutique, et un éclat d'or pend à sa ceinture.
     */
    static int[][] pilleur() {
        PixelCanvas g = new PixelCanvas(W, H);
        int cloth = rgba("6a5a48"), clothDark = rgba("463a2e"), hood = rgba("5a4a3a"), hoodDark = rgba("3a2e24");
        int bandana = rgba("b02a2a"), bandanaDark = rgba("7a1a1a"), sack = rgba("c8a868"), sackDark = rgba("8a6e40");
        int patch = rgba("8a7a5a"), blue = rgba("2a5ab0");

        // Buste : tunique rapiécée, ombrée à droite.
        g.rect(4, 29, 29, 41, cloth);
        g.rect(20, 29, 29, 41, clothDark);
        g.ellipseShaded(7f, 31f, 4.5f, 3.5f, cloth, clothDark, 3f);
        g.ellipseShaded(27f, 31f, 4.5f, 3.5f, clothDark, clothDark, 0f);
        g.rect(22, 34, 25, 37, patch);
        g.line(4, 38, 29, 38, WOOD_DARK); // ceinture
        // Éclat d'or volé, pendu à la ceinture.
        g.triangle(14, 39, 18, 39, 16, 35, GOLD);
        g.triangle(14, 39, 18, 39, 16, 41, GOLD_DARK);
        g.set(16, 37, GOLD_LIGHT);
        // Capuche qui tombe sur les épaules.
        g.rect(8, 16, 26, 30, hood);
        g.rect(20, 16, 26, 30, hoodDark);
        g.ellipseShaded(17f, 14f, 9.5f, 10f, hood, hoodDark, 3f);
        // Visage dans l'ombre de la capuche.
        g.ellipseShaded(17f, 17f, 6f, 7f, SKIN, SKIN_SHADE, 2f);
        g.ellipse(17f, 10f, 7f, 2.5f, hood);
        g.line(12, 12, 22, 12, hoodDark);
        // Regard fuyant, sourcils froncés.
        g.line(12, 13, 15, 14, hoodDark);
        g.line(19, 14, 22, 13, hoodDark);
        g.rect(14, 15, 15, 16, EYE);
        g.rect(20, 15, 21, 16, EYE);
        // Bandana rouge noué sur le nez, pointe vers le bas.
        g.rect(10, 18, 24, 21, bandana);
        g.rect(20, 18, 24, 21, bandanaDark);
        g.triangle(11, 21, 23, 21, 17, 27, bandana);
        g.triangle(17, 21, 23, 21, 17, 27, bandanaDark);
        g.set(13, 19, rgba("e8e0d0"));
        g.set(16, 20, rgba("e8e0d0"));
        // Cartes volées qui dépassent du sac.
        g.rect(2, 8, 6, 14, WHITE);
        g.rect(2, 8, 6, 8, GOLD);
        g.set(4, 11, RED);
        g.rect(7, 6, 11, 13, WHITE);
        g.rect(7, 6, 11, 6, GOLD);
        g.set(9, 9, blue);
        g.set(9, 10, blue);
        // Sac de toile sur l'épaule gauche, tenu à pleine main.
        g.ellipseShaded(6.5f, 21f, 6f, 7f, sack, sackDark, 2f);
        g.ellipse(6.5f, 14.5f, 4.5f, 1.5f, sackDark);
        g.line(2, 17, 11, 17, WOOD_DARK);
        g.line(4, 22, 6, 24, sackDark);
        g.ellipse(10f, 26f, 2.2f, 2.2f, SKIN);
        return g.outlined();
    }

    /**
     * Faussaire : visière verte, une loupe d'horloger vissée sur l'œil, joue
     * tachée d'encre ; il tient en éventail trois faux billets verts, les
     * doigts noirs d'encre.
     */
    static int[][] faussaire() {
        PixelCanvas g = new PixelCanvas(W, H);
        int shirt = rgba("e0dace"), shirtDark = rgba("b4ac9e"), vest = rgba("3a3a4a"), vestDark = rgba("26263a");
        int visor = rgba("3ec070"), visorDark = rgba("1e7a46"), hair = rgba("2a1e1a"), ink = rgba("1e2a5a");
        int note = rgba("8ac88a"), noteDark = rgba("4e8a52"), noteInk = rgba("2e5a32");

        // Chemise aux manches retroussées, gilet sombre.
        g.rect(4, 29, 29, 41, shirt);
        g.rect(20, 29, 29, 41, shirtDark);
        g.ellipseShaded(7f, 31f, 4.5f, 3.5f, shirt, shirtDark, 3f);
        g.ellipseShaded(27f, 31f, 4.5f, 3.5f, shirtDark, shirtDark, 0f);
        g.rect(10, 29, 24, 41, vest);
        g.rect(19, 29, 24, 41, vestDark);
        g.triangle(14, 28, 20, 28, 17, 33, shirt);
        g.set(7, 33, ink); g.set(8, 34, ink); // taches d'encre sur la manche
        // Cou, visage maigre, cheveux noirs.
        g.rect(14, 24, 20, 28, SKIN_SHADE);
        g.ellipseShaded(17f, 16f, 6.5f, 8.5f, SKIN, SKIN_SHADE, 2f);
        g.rect(10, 10, 11, 17, hair);
        g.rect(23, 10, 24, 15, hair);
        // Visière verte de faussaire.
        g.ellipse(17f, 9f, 7.5f, 3f, hair);
        g.rect(9, 9, 25, 10, visor);
        g.triangle(8, 11, 26, 11, 17, 14, visorDark);
        g.line(9, 9, 25, 9, rgba("8ae8aa"));
        // Œil plissé à gauche, loupe d'horloger noire à droite.
        g.line(12, 16, 15, 16, EYE);
        g.rect(19, 14, 24, 18, rgba("1e1a24"));
        g.rect(20, 15, 23, 17, rgba("6ab0c8"));
        g.set(20, 15, rgba("e8f4ff"));
        // Tache d'encre sur la joue, bouche pincée.
        g.set(12, 19, ink); g.set(13, 20, ink); g.set(12, 20, ink);
        g.line(15, 22, 19, 22, MOUTH);
        g.set(19, 21, MOUTH);
        // Trois faux billets en éventail.
        int[][] notes = {{5, 30}, {10, 32}, {15, 34}};
        for (int[] n : notes) {
            g.rect(n[0], n[1], n[0] + 12, n[1] + 6, noteDark);
            g.rect(n[0] + 1, n[1] + 1, n[0] + 11, n[1] + 5, note);
            g.ellipse(n[0] + 6, n[1] + 3, 1.8f, 1.8f, noteInk);
            g.set(n[0] + 2, n[1] + 2, noteInk);
            g.set(n[0] + 10, n[1] + 4, noteInk);
        }
        g.set(24, 37, ink); g.set(25, 38, ink); g.set(24, 38, ink); // encre qui bave
        // Mains aux doigts noircis.
        g.ellipse(5f, 37f, 2.5f, 2.2f, SKIN);
        g.set(4, 35, ink); g.set(6, 35, ink);
        g.ellipse(28f, 38f, 2.5f, 2.2f, SKIN_SHADE);
        g.set(27, 36, ink); g.set(29, 36, ink);
        return g.outlined();
    }

    /**
     * Cartomancienne : foulard violet bordé de piécettes d'or, longs cheveux
     * noirs, anneaux aux oreilles et pierre au front ; les mains autour d'une
     * boule de cristal, une carte de tarot à l'œil flotte à côté d'elle.
     */
    static int[][] cartomancienne() {
        PixelCanvas g = new PixelCanvas(W, H);
        int skin = rgba("d8a070"), skinShade = rgba("a87448"), scarf = rgba("7a2a9a"), scarfDark = rgba("4e1a6a");
        int hair = rgba("1e1420"), shawl = rgba("5a2a7a"), shawlDark = rgba("3a1a52"), gem = rgba("2ec0e0");
        int ball = rgba("b490f0"), ballDark = rgba("7a50c0"), ballLight = rgba("e8d8ff"), lid = rgba("9a4ab0");

        // Longs cheveux noirs derrière les épaules.
        g.rect(8, 14, 26, 31, hair);
        // Châle violet étoilé.
        g.rect(4, 29, 29, 41, shawl);
        g.rect(20, 29, 29, 41, shawlDark);
        g.ellipseShaded(7f, 31f, 4.5f, 3.5f, shawl, shawlDark, 3f);
        g.ellipseShaded(27f, 31f, 4.5f, 3.5f, shawlDark, shawlDark, 0f);
        g.triangle(13, 28, 21, 28, 17, 33, skin);
        g.set(6, 34, GOLD); g.set(26, 32, GOLD); g.set(28, 39, GOLD); g.set(5, 40, GOLD);
        // Cou et visage.
        g.rect(14, 24, 20, 28, skinShade);
        g.ellipseShaded(17f, 17f, 6.5f, 8f, skin, skinShade, 2f);
        // Foulard noué, bordé de piécettes d'or.
        g.ellipseShaded(17f, 10f, 9.5f, 6f, scarf, scarfDark, 2f);
        g.rect(7, 10, 27, 12, scarf);
        g.rect(22, 10, 27, 12, scarfDark);
        for (int x = 8; x <= 26; x += 2) g.set(x, 13, GOLD);
        g.rect(7, 13, 9, 26, scarf);
        g.rect(25, 13, 27, 26, scarfDark);
        g.set(17, 7, GOLD);
        g.set(17, 14, gem);
        // Yeux fardés de violet, cils marqués.
        g.rect(12, 16, 15, 16, lid);
        g.rect(19, 16, 22, 16, lid);
        g.rect(13, 17, 14, 18, EYE);
        g.rect(20, 17, 21, 18, EYE);
        g.set(12, 17, EYE); g.set(22, 17, EYE);
        g.line(15, 22, 19, 22, rgba("a8283a"));
        g.set(17, 23, rgba("a8283a"));
        // Anneaux d'or aux oreilles.
        g.ellipse(10f, 22f, 1.6f, 1.6f, GOLD);
        g.set(10, 22, skinShade);
        g.ellipse(24f, 22f, 1.6f, 1.6f, GOLD_DARK);
        g.set(24, 22, skinShade);
        // Boule de cristal sur son socle, tenue à deux mains.
        g.rect(12, 39, 22, 41, GOLD_DARK);
        g.rect(13, 38, 21, 38, GOLD);
        g.ellipseShaded(17f, 33f, 6f, 5.5f, ball, ballDark, 2f);
        g.ellipse(15f, 31f, 2f, 1.5f, ballLight);
        g.line(15, 35, 19, 33, ballLight);
        g.ellipse(10f, 34f, 2f, 2.5f, skin);
        g.ellipse(24f, 34f, 2f, 2.5f, skinShade);
        // Carte de tarot flottante, marquée d'un œil.
        g.rect(28, 2, 33, 10, WHITE);
        g.rect(29, 3, 32, 9, scarf);
        g.rect(30, 5, 31, 6, ballLight);
        g.set(30, 6, EYE);
        g.set(30, 1, GOLD_LIGHT);
        return g.outlined();
    }

    /**
     * Duelliste : grand chapeau à large bord et longue plume rouge, moustache
     * fine et barbiche, col de dentelle ; une rapière à la main gauche et, à
     * droite, un as de cœur glissé entre deux doigts.
     */
    static int[][] duelliste() {
        PixelCanvas g = new PixelCanvas(W, H);
        int doublet = rgba("8a1e3a"), doubletDark = rgba("5a1226"), hat = rgba("2a2a46"), hatDark = rgba("1a1a2e");
        int plume = rgba("ff4a5a"), plumeLight = rgba("ffb4b4"), lace = rgba("f4efe4"), laceShade = rgba("c8c0b4");
        int beard = rgba("3a2414");

        // Pourpoint grenat, boutons d'or.
        g.rect(4, 29, 29, 41, doublet);
        g.rect(20, 29, 29, 41, doubletDark);
        g.ellipseShaded(7f, 31f, 4.5f, 3.5f, doublet, doubletDark, 3f);
        g.ellipseShaded(27f, 31f, 4.5f, 3.5f, doubletDark, doubletDark, 0f);
        for (int y = 32; y <= 40; y += 3) g.set(17, y, GOLD);
        g.line(6, 36, 28, 40, GOLD_DARK); // baudrier
        // Col de dentelle rabattu.
        g.rect(11, 27, 23, 30, lace);
        g.rect(18, 27, 23, 30, laceShade);
        for (int x = 11; x <= 23; x += 2) g.set(x, 31, lace);
        // Cou et visage.
        g.rect(14, 24, 20, 27, SKIN_SHADE);
        g.ellipseShaded(17f, 17f, 6.5f, 8f, SKIN, SKIN_SHADE, 2f);
        g.line(11, 12, 23, 12, SKIN_SHADE); // ombre du bord
        // Regard assuré, moustache relevée, barbiche.
        g.line(12, 14, 15, 14, beard);
        g.line(19, 14, 22, 14, beard);
        g.rect(13, 16, 14, 16, EYE);
        g.rect(20, 16, 21, 16, EYE);
        g.set(17, 19, SKIN_SHADE);
        g.line(13, 20, 16, 21, beard);
        g.line(18, 21, 21, 20, beard);
        g.set(12, 19, beard); g.set(22, 19, beard);
        g.line(15, 22, 19, 22, MOUTH);
        g.rect(16, 23, 18, 25, beard);
        // Chapeau à large bord, ruban rouge.
        g.ellipseShaded(17f, 6f, 7f, 5f, hat, hatDark, 2f);
        g.ellipse(17f, 10f, 14f, 2.5f, hat);
        g.rect(23, 9, 31, 11, hatDark);
        g.rect(10, 7, 24, 8, RED);
        // Longue plume qui balaie vers la droite.
        g.line(21, 6, 27, 1, plume);
        g.line(22, 7, 29, 2, plume);
        g.line(23, 7, 31, 3, plumeLight);
        g.line(24, 8, 32, 5, plume);
        g.set(28, 0, plume);
        // Rapière dressée à gauche : lame, coquille d'or, poignée.
        g.line(3, 6, 3, 33, STEEL_LIGHT);
        g.line(4, 6, 4, 33, STEEL_DARK);
        g.set(3, 5, STEEL_LIGHT);
        g.ellipse(3.5f, 34.5f, 3f, 1.6f, GOLD);
        g.ellipse(4.5f, 35f, 1.5f, 1f, GOLD_DARK);
        g.ellipse(4f, 37.5f, 2.2f, 2f, SKIN);
        g.set(4, 40, GOLD);
        // Main droite levée, un as de cœur entre les doigts.
        g.rect(25, 13, 30, 20, WHITE);
        g.rect(29, 13, 30, 20, laceShade);
        g.ellipse(27f, 16f, 1.2f, 1f, RED);
        g.set(27, 17, RED);
        g.ellipse(27f, 22f, 2.5f, 2.2f, SKIN);
        g.set(26, 20, SKIN); g.set(28, 20, SKIN_SHADE);
        return g.outlined();
    }

    /**
     * Le Prétendant : il s'est couronné lui-même, couronne de travers ; air
     * hautain, cape rouge à col d'hermine, pourpoint violet. Trois éclats d'or
     * de la comète brillent autour de lui, le troisième serti sur sa poitrine.
     */
    static int[][] pretendant() {
        PixelCanvas g = new PixelCanvas(W, H);
        int cape = rgba("b0202c"), capeDark = rgba("7a1420"), robe = rgba("5a2a8a"), robeDark = rgba("3a1a5e");
        int ermine = rgba("f4efe4"), spot = rgba("1a1018"), hair = rgba("8a3a1a"), hairDark = rgba("5a2210");
        int skin = rgba("f0d0b8"), skinShade = rgba("c8a088"), halo = rgba("fff2a0");

        // Grande cape rouge qui déborde des épaules.
        g.rect(1, 29, 32, 41, cape);
        g.rect(22, 29, 32, 41, capeDark);
        g.ellipseShaded(5f, 31f, 5f, 4f, cape, capeDark, 4f);
        g.ellipseShaded(28f, 31f, 5f, 4f, capeDark, capeDark, 0f);
        // Pourpoint violet et col d'hermine.
        g.rect(10, 30, 24, 41, robe);
        g.rect(19, 30, 24, 41, robeDark);
        g.rect(5, 27, 28, 30, ermine);
        g.rect(22, 27, 28, 30, rgba("d0c8bc"));
        for (int x = 7; x <= 27; x += 4) g.set(x, 28 + (x / 4) % 2, spot);
        // Cou, visage hautain, menton levé.
        g.rect(14, 24, 20, 27, skinShade);
        g.ellipseShaded(17f, 17f, 6.5f, 8f, skin, skinShade, 2f);
        g.rect(10, 11, 11, 20, hair);
        g.rect(23, 11, 24, 18, hairDark);
        g.ellipse(17f, 10f, 7.5f, 3f, hair);
        // Paupières lourdes, un sourcil levé, sourire satisfait.
        g.line(12, 14, 15, 13, hairDark);
        g.line(19, 12, 22, 13, hairDark);
        g.line(12, 16, 15, 16, skinShade);
        g.line(19, 16, 22, 16, skinShade);
        g.rect(13, 17, 14, 17, EYE);
        g.rect(20, 17, 21, 17, EYE);
        g.set(17, 19, skinShade);
        g.line(15, 22, 19, 22, MOUTH);
        g.set(20, 21, MOUTH);
        // Couronne posée de travers, trop grande pour lui.
        g.line(10, 9, 24, 6, GOLD);
        g.line(10, 8, 24, 5, GOLD);
        g.line(10, 10, 24, 7, GOLD_DARK);
        g.triangle(10, 8, 13, 8, 10, 3, GOLD);
        g.triangle(15, 7, 19, 6, 16, 1, GOLD);
        g.triangle(21, 6, 24, 5, 24, 0, GOLD_DARK);
        g.set(17, 7, RED);
        // Trois éclats d'or : deux flottent de chaque côté, le troisième serti sur la poitrine.
        int[][] shards = {{4, 13}, {30, 15}, {17, 36}};
        for (int[] s : shards) {
            int x = s[0], y = s[1];
            g.set(x - 3, y, halo); g.set(x + 3, y, halo);
            g.set(x, y - 6, halo); g.set(x, y + 5, halo);
            g.triangle(x - 2, y, x + 2, y, x, y - 4, GOLD);
            g.triangle(x - 2, y, x + 2, y, x, y + 3, GOLD_DARK);
            g.triangle(x, y, x + 2, y, x, y - 4, GOLD_DARK);
            g.set(x - 1, y - 1, GOLD_LIGHT);
            g.set(x, y - 2, rgba("ffffff"));
        }
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Chapitre 5, « La Maison »
    // -------------------------------------------------------------------------

    /**
     * Portier : un colosse en livrée bordeaux, casquette galonnée, mâchoire
     * carrée et bras croisés ; deux poteaux de laiton tendent devant lui un
     * cordon de velours rouge.
     */
    static int[][] portier() {
        PixelCanvas g = new PixelCanvas(W, H);
        int coat = rgba("7a1a2e"), coatDark = rgba("4e1020"), brow = rgba("2a1a14"), velvet = rgba("c8203a");
        int velvetDark = rgba("8a1428"), brass = rgba("e8b84a"), brassDark = rgba("a87a24");

        // Carrure énorme, épaulettes à franges.
        g.rect(1, 28, 32, 41, coat);
        g.rect(21, 28, 32, 41, coatDark);
        g.ellipseShaded(5f, 30f, 5f, 4f, coat, coatDark, 4f);
        g.ellipseShaded(28f, 30f, 5f, 4f, coatDark, coatDark, 0f);
        g.rect(2, 27, 8, 28, GOLD);
        g.rect(25, 27, 31, 28, GOLD_DARK);
        for (int x = 2; x <= 8; x += 2) g.set(x, 29, GOLD);
        for (int x = 25; x <= 31; x += 2) g.set(x, 29, GOLD_DARK);
        // Bras croisés : l'un passe sur l'autre, poings fermés.
        g.rect(9, 35, 29, 38, coatDark);
        g.rect(11, 35, 12, 38, GOLD_DARK);
        g.ellipse(8f, 36.5f, 2.5f, 2f, SKIN);
        g.rect(4, 31, 24, 34, coat);
        g.rect(21, 31, 22, 34, GOLD);
        g.ellipse(25.5f, 32.5f, 2.5f, 2f, SKIN_SHADE);
        // Cou épais, visage à mâchoire carrée.
        g.rect(12, 23, 22, 27, SKIN_SHADE);
        g.ellipseShaded(17f, 15f, 7.5f, 7.5f, SKIN, SKIN_SHADE, 3f);
        g.rect(10, 16, 24, 23, SKIN);
        g.rect(21, 16, 24, 23, SKIN_SHADE);
        // Sourcils épais froncés, petits yeux, bouche sévère.
        g.line(11, 14, 15, 15, brow);
        g.line(19, 15, 23, 14, brow);
        g.rect(13, 16, 14, 17, EYE);
        g.rect(20, 16, 21, 17, EYE);
        g.rect(16, 18, 18, 19, SKIN_SHADE);
        g.line(14, 21, 20, 21, MOUTH);
        g.line(15, 23, 19, 23, SKIN_SHADE);
        // Casquette galonnée, visière noire et insigne doré.
        g.rect(9, 6, 25, 10, coat);
        g.rect(21, 6, 25, 10, coatDark);
        g.ellipse(17f, 6f, 8f, 2.5f, coat);
        g.rect(9, 10, 25, 10, GOLD);
        g.rect(9, 11, 25, 12, rgba("1a1418"));
        g.ellipse(17f, 7.5f, 1.5f, 1.5f, GOLD);
        // Cordon de velours tendu entre deux poteaux de laiton.
        for (int x = 2; x <= 31; x++) {
            float t = (x - 16.5f) / 14.5f;
            int y = Math.round(40 - 6 * t * t);
            g.set(x, y, velvet);
            g.set(x, y + 1, velvetDark);
        }
        g.rect(0, 33, 2, 41, brass);
        g.rect(2, 33, 2, 41, brassDark);
        g.ellipse(1f, 32f, 1.8f, 1.8f, brass);
        g.rect(31, 33, 33, 41, brass);
        g.rect(33, 33, 33, 41, brassDark);
        g.ellipse(32f, 32f, 1.8f, 1.8f, brass);
        return g.outlined();
    }

    /**
     * Comptable : raie au milieu, grosses lunettes rondes, plume d'oie
     * derrière l'oreille ; chemise blanche aux fixe-manches rouges, il tient
     * grand ouvert son registre de comptes.
     */
    static int[][] comptable() {
        PixelCanvas g = new PixelCanvas(W, H);
        int shirt = rgba("ece6da"), shirtDark = rgba("c0b8aa"), vest = rgba("6a4a2a"), vestDark = rgba("4a321c");
        int hair = rgba("6a4a2a"), hairDark = rgba("4a321c"), lens = rgba("c8e6f0"), rim = rgba("2a2230");
        int page = rgba("f4ecd4"), pageShade = rgba("d8ccb0"), lineInk = rgba("9a9aa8"), cover = rgba("2e5a8a");

        // Chemise blanche, gilet brun, fixe-manches rouges.
        g.rect(4, 29, 29, 41, shirt);
        g.rect(20, 29, 29, 41, shirtDark);
        g.ellipseShaded(7f, 31f, 4.5f, 3.5f, shirt, shirtDark, 3f);
        g.ellipseShaded(27f, 31f, 4.5f, 3.5f, shirtDark, shirtDark, 0f);
        g.rect(10, 29, 24, 41, vest);
        g.rect(19, 29, 24, 41, vestDark);
        g.triangle(14, 28, 20, 28, 17, 32, shirt);
        g.rect(16, 28, 18, 29, RED_DARK); // nœud papillon
        g.rect(3, 31, 8, 32, RED);
        g.rect(26, 31, 30, 32, RED_DARK);
        // Cou, visage, cheveux plaqués à la raie.
        g.rect(14, 24, 20, 28, SKIN_SHADE);
        g.ellipseShaded(17f, 16f, 6.5f, 8.5f, SKIN, SKIN_SHADE, 2f);
        g.ellipse(17f, 9.5f, 7.5f, 3.5f, hair);
        g.rect(10, 9, 11, 14, hair);
        g.rect(23, 9, 24, 13, hairDark);
        g.line(17, 7, 17, 10, SKIN);
        // Grosses lunettes rondes.
        g.ellipse(13.5f, 16f, 3f, 2.8f, rim);
        g.ellipse(20.5f, 16f, 3f, 2.8f, rim);
        g.ellipse(13.5f, 16f, 2f, 1.8f, lens);
        g.ellipse(20.5f, 16f, 2f, 1.8f, lens);
        g.set(17, 16, rim);
        g.set(14, 16, EYE);
        g.set(21, 16, EYE);
        g.set(12, 15, rgba("ffffff"));
        g.line(15, 22, 19, 22, MOUTH);
        // Plume d'oie derrière l'oreille.
        g.line(24, 15, 30, 4, WHITE);
        g.line(25, 15, 31, 5, rgba("d8d0c0"));
        g.line(26, 12, 31, 3, WHITE);
        g.set(24, 16, rgba("1e2a5a"));
        // Registre ouvert : pages lignées, chiffres rouges et noirs.
        g.rect(6, 33, 28, 41, cover);
        g.rect(7, 33, 16, 40, page);
        g.rect(18, 33, 27, 40, pageShade);
        g.line(17, 33, 17, 41, rgba("1e3a5a"));
        for (int y = 35; y <= 39; y += 2) {
            g.line(8, y, 15, y, lineInk);
            g.line(19, y, 26, y, lineInk);
        }
        g.set(14, 35, EYE); g.set(13, 37, RED); g.set(25, 37, EYE); g.set(24, 39, RED);
        g.ellipse(5f, 37f, 2f, 2.2f, SKIN);
        g.ellipse(29f, 37f, 2f, 2.2f, SKIN_SHADE);
        return g.outlined();
    }

    /**
     * Directeur des jeux : smoking noir, nœud papillon et œillet rouge,
     * cheveux argent gominés, sourire à la dent d'or ; il brandit le
     * règlement, dont une ligne barrée vient d'être réécrite en rouge.
     */
    static int[][] directeur() {
        PixelCanvas g = new PixelCanvas(W, H);
        int tux = rgba("22222e"), tuxDark = rgba("14141c"), shirt = rgba("f4efe4"), hair = rgba("b8b8c8");
        int hairDark = rgba("7a7a8a"), paper = rgba("f0e0b0"), paperShade = rgba("d0bc88"), roll = rgba("b8985a");
        int text = rgba("6a5a40"), satin = rgba("3a3a4e");

        // Smoking : revers de satin, chemise blanche, nœud papillon, œillet.
        g.rect(4, 29, 29, 41, tux);
        g.rect(20, 29, 29, 41, tuxDark);
        g.ellipseShaded(7f, 31f, 4.5f, 3.5f, tux, tuxDark, 3f);
        g.ellipseShaded(27f, 31f, 4.5f, 3.5f, tuxDark, tuxDark, 0f);
        g.triangle(11, 28, 23, 28, 17, 40, shirt);
        g.line(11, 29, 15, 38, satin);
        g.line(23, 29, 19, 38, satin);
        g.triangle(13, 28, 16, 29, 13, 30, EYE);
        g.triangle(21, 28, 18, 29, 21, 30, EYE);
        g.rect(16, 28, 18, 30, EYE);
        g.set(17, 33, EYE); g.set(17, 36, EYE);
        g.ellipse(9f, 32f, 1.5f, 1.5f, RED);
        g.set(9, 34, rgba("2e7a46"));
        // Cou, visage, cheveux argentés plaqués en arrière.
        g.rect(12, 24, 18, 28, SKIN_SHADE);
        g.ellipseShaded(15f, 16f, 6.5f, 8.5f, SKIN, SKIN_SHADE, 2f);
        g.ellipse(15f, 9f, 7.5f, 3.5f, hair);
        g.rect(8, 9, 9, 14, hair);
        g.rect(21, 9, 22, 12, hairDark);
        g.line(10, 8, 18, 7, rgba("e8e8f0"));
        // Sourcils arqués, regard rusé, fine moustache, dent d'or.
        g.line(10, 13, 13, 12, hairDark);
        g.line(17, 12, 20, 13, hairDark);
        g.rect(11, 15, 12, 15, EYE);
        g.rect(17, 15, 18, 15, EYE);
        g.set(15, 18, SKIN_SHADE);
        g.line(12, 20, 14, 20, hairDark);
        g.line(16, 20, 18, 20, hairDark);
        g.line(12, 22, 18, 22, MOUTH);
        g.set(11, 21, MOUTH); g.set(19, 21, MOUTH);
        g.set(16, 22, GOLD);
        // Le règlement déroulé : lignes de texte, une ligne barrée réécrite en rouge.
        g.rect(23, 11, 32, 30, paper);
        g.rect(30, 11, 32, 30, paperShade);
        g.rect(22, 9, 33, 11, roll);
        g.rect(22, 30, 33, 32, roll);
        g.set(22, 10, WOOD_DARK); g.set(33, 10, WOOD_DARK);
        g.set(22, 31, WOOD_DARK); g.set(33, 31, WOOD_DARK);
        for (int y = 14; y <= 27; y += 3) g.line(25, y, 30, y, text);
        g.line(24, 20, 31, 20, RED);
        g.line(25, 22, 30, 22, RED);
        g.ellipse(27.5f, 25.5f, 1.8f, 1.5f, RED);
        g.set(27, 25, paper);
        g.set(29, 24, RED);
        // Main gantée qui tient le parchemin.
        g.ellipse(24f, 34f, 2.5f, 2.2f, WHITE);
        g.rect(24, 32, 25, 33, WHITE);
        return g.outlined();
    }

    /**
     * Sécurité : crâne rasé, lunettes noires, oreillette au fil torsadé,
     * costume noir et badge doré ; sa lampe torche balaie le bas de l'image
     * d'un faisceau jaune.
     */
    static int[][] securite() {
        PixelCanvas g = new PixelCanvas(W, H);
        int suit = rgba("2a2a36"), suitDark = rgba("1a1a24"), shirt = rgba("ece6da"), stubble = rgba("8a7060");
        int glasses = rgba("12121a"), glint = rgba("6a7aa8"), wire = rgba("c8c8d8");
        int beam = rgba("ffe98a"), beamCore = rgba("fff8d8"), torch = rgba("3a3a4a");

        // Costume noir, chemise, cravate, badge doré.
        g.rect(3, 29, 30, 41, suit);
        g.rect(20, 29, 30, 41, suitDark);
        g.ellipseShaded(6f, 31f, 4.5f, 3.5f, suit, suitDark, 3f);
        g.ellipseShaded(28f, 31f, 4.5f, 3.5f, suitDark, suitDark, 0f);
        g.triangle(13, 28, 21, 28, 17, 35, shirt);
        g.rect(16, 29, 17, 36, glasses);
        g.ellipse(23f, 33f, 1.5f, 1.5f, GOLD);
        // Cou large, visage carré, crâne rasé.
        g.rect(13, 23, 21, 28, SKIN_SHADE);
        g.ellipseShaded(17f, 15f, 7f, 8f, SKIN, SKIN_SHADE, 2f);
        g.rect(11, 16, 23, 22, SKIN);
        g.rect(20, 16, 23, 22, SKIN_SHADE);
        g.ellipse(17f, 9f, 6.5f, 2.5f, stubble);
        // Lunettes noires, reflet bleuté.
        g.rect(10, 14, 24, 15, glasses);
        g.rect(11, 16, 15, 17, glasses);
        g.rect(19, 16, 23, 17, glasses);
        g.set(12, 15, glint); g.set(20, 15, glint);
        g.line(14, 21, 20, 21, MOUTH);
        // Oreillette et fil torsadé jusqu'au col.
        g.rect(9, 17, 10, 18, wire);
        for (int y = 19; y <= 28; y++) g.set(y % 2 == 0 ? 9 : 10, y, wire);
        // Lampe torche tenue bas, faisceau vers la gauche.
        g.triangle(21, 36, 2, 30, 2, 41, beam);
        g.triangle(21, 36, 6, 33, 6, 39, beamCore);
        g.rect(21, 34, 28, 37, torch);
        g.rect(20, 33, 21, 38, STEEL);
        g.ellipse(27f, 38f, 2.5f, 2.2f, SKIN);
        return g.outlined();
    }

    /**
     * La Maison : le boss n'est pas un homme mais le manoir lui-même, vivant.
     * Façade d'or au fronton marqué d'un blason (le bouclier), aile gauche à
     * l'épée (la salle de jeu, qui attaque), aile droite à la porte de coffre
     * (le coffre, qui soigne). Ses yeux brillent dans deux fenêtres en forme
     * de cartes, et sa porte est une fente de machine à sous.
     */
    static int[][] maison() {
        PixelCanvas g = new PixelCanvas(W, H);
        int wall = rgba("f0c040"), wallDark = rgba("b8861e"), wing = rgba("d8a838"), wingDark = rgba("9a6a18");
        int roof = rgba("8a3a2a"), roofDark = rgba("5a2418"), shield = rgba("2a4a9a"), card = rgba("f6f0e2");
        int cloud = rgba("ffffff"), cloudShade = rgba("c8d0ec"), vault = rgba("8a94a8"), vaultDark = rgba("5a6478");
        int slot = rgba("1a0f0f"), iris = rgba("d8202c");

        // Ailes basses, toits pointus.
        g.rect(0, 20, 7, 38, wing);
        g.rect(26, 20, 33, 38, wingDark);
        g.triangle(0, 20, 7, 20, 3, 14, roof);
        g.triangle(26, 20, 33, 20, 30, 14, roofDark);
        // Aile gauche : la salle de jeu, une épée sur bannière rouge.
        g.rect(1, 23, 6, 32, RED);
        g.triangle(1, 32, 6, 32, 3, 35, RED);
        g.line(3, 24, 3, 30, STEEL_LIGHT);
        g.line(4, 24, 4, 30, STEEL);
        g.line(1, 30, 6, 30, GOLD);
        g.rect(3, 31, 4, 32, WOOD_DARK);
        // Aile droite : le coffre, une porte ronde à molette.
        g.ellipse(29.5f, 28f, 3.5f, 3.5f, vaultDark);
        g.ellipse(29.5f, 28f, 2.5f, 2.5f, vault);
        g.line(28, 28, 31, 28, vaultDark);
        g.line(29, 26, 30, 30, vaultDark);
        g.set(29, 28, GOLD);
        // Corps du manoir, ombré à droite.
        g.rect(7, 13, 26, 38, wall);
        g.rect(22, 13, 26, 38, wallDark);
        g.line(7, 13, 7, 38, GOLD_LIGHT);
        // Fronton triangulaire et son blason (le bouclier).
        g.triangle(5, 13, 28, 13, 17, 2, wall);
        g.triangle(17, 2, 28, 13, 17, 13, wallDark);
        g.line(5, 13, 28, 13, roof);
        g.rect(15, 5, 19, 9, shield);
        g.triangle(15, 9, 19, 9, 17, 11, shield);
        g.line(17, 5, 17, 10, GOLD);
        g.line(15, 7, 19, 7, GOLD);
        g.set(17, 0, GOLD); g.set(17, 1, GOLD);
        // Deux fenêtres-cartes où roulent des yeux, sous des sourcils de corniche.
        g.line(8, 15, 14, 17, roofDark);
        g.line(25, 15, 19, 17, roofDark);
        int[] eyes = {9, 19};
        for (int x0 : eyes) {
            g.rect(x0, 18, x0 + 5, 25, card);
            g.rect(x0, 18, x0 + 5, 18, RED);
            g.rect(x0, 25, x0 + 5, 25, RED);
            g.ellipse(x0 + 2.5f, 21.5f, 2f, 2f, iris);
            g.rect(x0 + 2, 21, x0 + 3, 22, slot);
            g.set(x0 + 1, 20, rgba("ffffff"));
        }
        // La porte : une fente de machine à sous dans un cadre doré.
        g.rect(13, 28, 21, 38, GOLD_DARK);
        g.ellipse(17f, 28f, 4f, 2f, GOLD_DARK);
        g.rect(14, 28, 20, 38, GOLD);
        g.rect(16, 29, 18, 37, slot);
        g.set(16, 30, rgba("6a4a2a"));
        g.rect(12, 38, 22, 39, wallDark);
        // Nuages sous les fondations.
        g.ellipse(5f, 40f, 6f, 2.5f, cloud);
        g.ellipse(16f, 40.5f, 6f, 2f, cloud);
        g.ellipse(28f, 40f, 6f, 2.5f, cloudShade);
        g.ellipse(23f, 41f, 4f, 1.5f, cloudShade);
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Chapitre 6, « Le Jackpot »
    // -------------------------------------------------------------------------

    /**
     * Gardien du Levier : armure d'acier sombre bordée d'or, heaume clos à
     * fente en T qui luit de bleu, étoile au cimier ; il tient comme une
     * hallebarde un levier géant de machine à sous, boule rouge en haut.
     */
    static int[][] gardienLevier() {
        PixelCanvas g = new PixelCanvas(W, H);
        int iron = rgba("5a5a7e"), ironDark = rgba("36365a"), ironLight = rgba("8a8ab0"), glow = rgba("7ae8ff");
        int chrome = rgba("e0e8f4"), chromeDark = rgba("8a94a8"), ball = rgba("e8203a"), ballDark = rgba("a0101e");

        // Buste en armure, épaulières bordées d'or.
        g.rect(3, 29, 30, 41, ironDark);
        g.rect(8, 29, 21, 41, iron);
        g.ellipseShaded(6f, 30f, 5.5f, 4.5f, iron, ironDark, 1f);
        g.ellipseShaded(27f, 30f, 5.5f, 4.5f, iron, ironDark, -1f);
        g.line(1, 31, 11, 31, GOLD);
        g.line(22, 31, 32, 31, GOLD_DARK);
        g.ellipse(15f, 36f, 2f, 2f, GOLD); // étoile gravée sur la cuirasse
        g.set(15, 33, GOLD); g.set(15, 39, GOLD); g.set(12, 36, GOLD); g.set(18, 36, GOLD);
        // Gorgerin.
        g.rect(11, 25, 23, 28, ironDark);
        g.line(11, 26, 23, 26, GOLD_DARK);
        // Heaume clos à sommet arrondi, fente en T lumineuse.
        g.ellipseShaded(17f, 9f, 7f, 5f, iron, ironDark, 2f);
        g.rect(10, 9, 24, 24, iron);
        g.rect(20, 9, 24, 24, ironDark);
        g.line(10, 9, 10, 24, ironLight);
        g.rect(11, 13, 23, 15, rgba("120c1e"));
        g.rect(16, 15, 18, 21, rgba("120c1e"));
        g.line(12, 14, 15, 14, glow);
        g.line(19, 14, 22, 14, glow);
        g.line(10, 23, 24, 23, GOLD_DARK);
        for (int y = 17; y <= 21; y += 2) { g.set(13, y, ironDark); g.set(21, y, rgba("26264a")); }
        // Étoile d'or au cimier.
        g.rect(16, 2, 18, 4, GOLD);
        g.set(17, 1, GOLD); g.set(15, 3, GOLD); g.set(19, 3, GOLD);
        g.set(17, 3, GOLD_LIGHT);
        // Le levier géant : tige chromée, bague dorée, boule rouge.
        g.rect(28, 6, 29, 41, chrome);
        g.rect(30, 6, 30, 41, chromeDark);
        g.rect(27, 14, 31, 15, GOLD);
        g.ellipseShaded(29f, 4f, 3.5f, 3.5f, ball, ballDark, 1f);
        g.set(28, 2, rgba("ffb4b4"));
        // Gantelet serré sur le levier.
        g.rect(26, 26, 31, 30, ironLight);
        g.rect(29, 26, 31, 30, iron);
        return g.outlined();
    }

    /**
     * L'Ombre : la silhouette sombre du joueur lui-même, chapeau mou et
     * épaules de flambeur, d'un noir violet où brillent deux yeux blancs et un
     * sourire ; il tient un éventail de cartes pâles, et le bas se dissout
     * en fumée.
     */
    static int[][] ombre() {
        PixelCanvas g = new PixelCanvas(W, H);
        int dark = rgba("24162e"), darker = rgba("140a1c"), rim = rgba("5a3a7e"), eye = rgba("ffffff");
        int eyeGlow = rgba("b8a0ff"), card = rgba("e0d8ff"), suit = rgba("4a1a7a");

        // Buste de flambeur, liseré violet à gauche.
        g.rect(4, 29, 29, 39, dark);
        g.rect(20, 29, 29, 39, darker);
        g.ellipseShaded(7f, 31f, 4.5f, 3.5f, dark, darker, 3f);
        g.ellipseShaded(27f, 31f, 4.5f, 3.5f, darker, darker, 0f);
        g.line(3, 31, 3, 38, rim);
        g.line(4, 28, 7, 28, rim);
        // Le bas qui part en fumée.
        int[] wisps = {4, 7, 9, 12, 15, 16, 19, 22, 24, 27, 29};
        for (int x : wisps) g.rect(x, 40, x, 40 + (x % 3 == 0 ? 1 : 0), x > 19 ? darker : dark);
        for (int x = 5; x <= 28; x += 5) g.set(x, 39, 0);
        // Cou et tête, sans visage.
        g.rect(14, 24, 20, 28, darker);
        g.ellipseShaded(17f, 16f, 6.5f, 8.5f, dark, darker, 2f);
        g.line(10, 13, 10, 20, rim);
        // Chapeau mou.
        g.ellipseShaded(17f, 6f, 6.5f, 4f, dark, darker, 2f);
        g.ellipse(17f, 9.5f, 11f, 2f, dark);
        g.rect(22, 8, 28, 11, darker);
        g.rect(11, 7, 23, 8, rgba("3a2450"));
        g.line(6, 9, 12, 9, rim);
        // Yeux blancs luisants, sourire pâle.
        g.set(12, 15, eyeGlow); g.set(15, 15, eyeGlow);
        g.set(19, 15, eyeGlow); g.set(22, 15, eyeGlow);
        g.rect(13, 14, 14, 16, eye);
        g.rect(20, 14, 21, 16, eye);
        g.line(14, 21, 20, 21, eyeGlow);
        g.set(13, 20, eyeGlow); g.set(21, 20, eyeGlow);
        // Éventail de cartes pâles dans la main droite.
        g.rect(19, 27, 24, 34, card);
        g.rect(22, 25, 27, 32, card);
        g.rect(25, 24, 30, 31, card);
        g.line(24, 27, 24, 34, rgba("9a90c8"));
        g.line(27, 25, 27, 32, rgba("9a90c8"));
        g.set(21, 30, suit); g.set(21, 31, suit);
        g.set(24, 28, RED);
        g.ellipse(28f, 27f, 1f, 1f, suit);
        g.ellipse(25f, 35f, 3f, 2.5f, dark);
        return g.outlined();
    }

    /**
     * Banqueroute : le banquier ruiné. Haut-de-forme cabossé et déchiré,
     * monocle fêlé, visage creusé et triste ; costume en lambeaux rapiécé,
     * poches retournées qui pendent, et deux pièces fendues qui tombent.
     */
    static int[][] banqueroute() {
        PixelCanvas g = new PixelCanvas(W, H);
        int suit = rgba("5a5048"), suitDark = rgba("3a342e"), patch = rgba("7a6a4a"), lining = rgba("ece6da");
        int liningShade = rgba("b8b0a2"), skin = rgba("d8c0a8"), skinShade = rgba("a89078"), hat = rgba("3a3a44");
        int hatDark = rgba("24242c"), bag = rgba("9a7a90"), coin = rgba("b89a4a"), coinDark = rgba("7a6428");

        // Costume en lambeaux, bas déchiré, pièce rapportée.
        g.rect(4, 29, 29, 39, suit);
        g.rect(20, 29, 29, 39, suitDark);
        g.ellipseShaded(7f, 31f, 4.5f, 3.5f, suit, suitDark, 3f);
        g.ellipseShaded(27f, 31f, 4.5f, 3.5f, suitDark, suitDark, 0f);
        for (int x = 4; x <= 29; x++) {
            int rag = (x * 7) % 4;
            for (int y = 40; y <= 39 + rag; y++) g.set(x, y, x >= 20 ? suitDark : suit);
        }
        g.triangle(13, 28, 21, 28, 17, 34, lining);
        g.rect(16, 29, 17, 33, rgba("6a2a2a"));
        g.rect(22, 31, 25, 34, patch);
        g.set(9, 33, rgba("1a1018")); g.set(10, 34, rgba("1a1018")); // trou
        // Poches retournées qui pendent des deux côtés.
        g.triangle(3, 35, 9, 35, 5, 41, lining);
        g.line(6, 36, 6, 39, liningShade);
        g.triangle(24, 35, 30, 35, 28, 41, liningShade);
        // Cou, visage creusé, cernes, barbe mal rasée.
        g.rect(14, 24, 20, 28, skinShade);
        g.ellipseShaded(17f, 17f, 6f, 8f, skin, skinShade, 2f);
        g.line(12, 13, 15, 14, hatDark);
        g.line(19, 14, 22, 13, hatDark);
        g.rect(13, 15, 14, 16, EYE);
        g.line(12, 17, 15, 17, bag);
        g.line(19, 17, 22, 17, bag);
        g.set(13, 20, skinShade); g.set(21, 20, skinShade); // joues creuses
        g.line(15, 22, 19, 22, MOUTH);
        g.set(14, 23, MOUTH); g.set(20, 23, MOUTH);
        for (int x = 13; x <= 21; x += 2) g.set(x, 24, rgba("8a7a6a"));
        // Monocle fêlé sur l'œil droit.
        g.ellipse(20.5f, 15.5f, 2.5f, 2.5f, GOLD_DARK);
        g.ellipse(20.5f, 15.5f, 1.6f, 1.6f, rgba("c8e6f0"));
        g.line(19, 14, 22, 17, rgba("6a7a8a"));
        g.set(20, 16, EYE);
        g.line(23, 17, 24, 24, GOLD_DARK);
        // Haut-de-forme cabossé, de travers, déchiré.
        g.rect(10, 1, 23, 9, hat);
        g.rect(19, 1, 23, 9, hatDark);
        g.triangle(10, 1, 14, 1, 10, 4, 0); // bord enfoncé
        g.rect(11, 3, 11, 4, hat);
        g.rect(7, 10, 26, 11, hatDark);
        g.line(7, 10, 18, 10, hat);
        g.rect(13, 7, 23, 8, rgba("5a2a2a"));
        g.rect(16, 3, 17, 5, rgba("120c16")); // déchirure
        g.set(18, 4, rgba("120c16"));
        g.set(22, 0, hatDark);
        // Deux pièces fendues qui tombent.
        int[][] coins = {{3, 18}, {30, 22}};
        for (int[] c : coins) {
            g.ellipseShaded(c[0], c[1], 2.5f, 2.5f, coin, coinDark, 0.5f);
            g.line(c[0] - 1, c[1] - 2, c[0], c[1], rgba("2a1e10"));
            g.line(c[0], c[1], c[0] + 1, c[1] + 2, rgba("2a1e10"));
        }
        return g.outlined();
    }

    /**
     * Temps Mort : silhouette en robe à capuche pointue ; dans l'ombre de la
     * capuche, un cadran d'horloge en guise de visage ; ses mains osseuses
     * tiennent un sablier dont tout le sable est déjà tombé.
     */
    static int[][] tempsMort() {
        PixelCanvas g = new PixelCanvas(W, H);
        int robe = rgba("3a3e58"), robeDark = rgba("24263a"), hollow = rgba("0c0c16"), face = rgba("e8e4d8");
        int faceShade = rgba("b8b4a8"), tick = rgba("2a2a3a"), hand = rgba("c8c4d8"), glass = rgba("c8e6f0");
        int sand = rgba("ffd54a");

        // Robe ample.
        g.rect(3, 29, 30, 41, robe);
        g.rect(20, 29, 30, 41, robeDark);
        g.ellipseShaded(7f, 31f, 5f, 4f, robe, robeDark, 3f);
        g.ellipseShaded(27f, 31f, 5f, 4f, robeDark, robeDark, 0f);
        g.line(17, 29, 17, 41, robeDark);
        // Capuche pointue et son ombre creuse.
        g.ellipseShaded(17f, 17f, 10f, 11f, robe, robeDark, 3f);
        g.triangle(9, 10, 22, 10, 19, 0, robe);
        g.triangle(19, 0, 22, 10, 19, 10, robeDark);
        g.rect(7, 18, 27, 30, robe);
        g.rect(21, 18, 27, 30, robeDark);
        g.ellipse(17f, 18f, 7f, 8f, hollow);
        // Le cadran d'horloge pour visage : graduations, aiguilles.
        g.ellipseShaded(17f, 18f, 5.5f, 5.5f, face, faceShade, 2f);
        g.ellipse(17f, 18f, 5.5f, 5.5f, face);
        for (int y = 13; y <= 23; y++) for (int x = 20; x <= 23; x++) if (g.get(x, y) == face) g.set(x, y, faceShade);
        g.set(17, 13, tick); g.set(17, 23, tick); g.set(12, 18, tick); g.set(22, 18, tick);
        g.set(14, 14, tick); g.set(20, 14, tick); g.set(14, 22, tick); g.set(20, 22, tick);
        g.line(17, 18, 17, 14, tick);
        g.line(17, 18, 20, 20, RED);
        g.set(17, 18, GOLD);
        // Sablier, tout le sable en bas : le temps est écoulé.
        g.rect(11, 29, 23, 30, WOOD_DARK);
        g.rect(11, 40, 23, 41, WOOD_DARK);
        g.rect(12, 31, 12, 39, WOOD);
        g.rect(22, 31, 22, 39, WOOD_DARK);
        g.triangle(13, 31, 21, 31, 17, 35, glass);
        g.triangle(13, 39, 21, 39, 17, 35, glass);
        g.triangle(13, 39, 21, 39, 17, 36, sand);
        g.set(17, 34, sand);
        // Mains osseuses agrippées au sablier.
        g.rect(9, 33, 11, 36, hand);
        g.set(11, 37, hand);
        g.rect(23, 33, 25, 36, rgba("9a96aa"));
        g.set(23, 37, rgba("9a96aa"));
        return g.outlined();
    }

    /**
     * La Machine Originelle : une immense machine à sous d'or et de lumière.
     * Fronton en arc semé d'ampoules et d'une étoile, cinq fenêtres de
     * rouleaux alignant des 7, un levier à boule rouge, un plateau débordant
     * de pièces, et des étoiles qui scintillent autour.
     */
    static int[][] machineOriginelle() {
        PixelCanvas g = new PixelCanvas(W, H);
        int body = rgba("ffd54a"), bodyDark = rgba("c4891e"), bodyDeep = rgba("8a5a12"), light = rgba("fff6c8");
        int panel = rgba("1a0f0f"), reel = rgba("f4efe4"), reelShade = rgba("c8c0b4"), seven = rgba("d8202c");
        int bulb = rgba("ffffff"), bulbOff = rgba("ff8a1e"), ball = rgba("e8203a"), ballDark = rgba("a0101e");
        int star = rgba("fff2a0");

        // Étoiles qui scintillent autour.
        int[][] stars = {{1, 2}, {30, 30}, {1, 26}};
        for (int[] s : stars) {
            g.set(s[0], s[1], star);
            g.set(s[0] + 1, s[1], light);
            g.set(s[0], s[1] + 1, light);
        }
        // Fronton en arc, ampoules tout autour, grande étoile au centre.
        g.ellipseShaded(15f, 10f, 13f, 9f, body, bodyDark, 6f);
        g.ellipse(15f, 10f, 10.5f, 6.5f, bodyDeep);
        for (int i = 0; i <= 10; i++) {
            double a = Math.PI + i * Math.PI / 10;
            g.set((int) Math.round(15 + Math.cos(a) * 11.8), (int) Math.round(10 + Math.sin(a) * 7.8), i % 2 == 0 ? bulb : bulbOff);
        }
        g.triangle(13, 8, 17, 8, 15, 3, star);
        g.triangle(13, 8, 17, 8, 15, 12, star);
        g.triangle(15, 6, 15, 10, 10, 8, star);
        g.triangle(15, 6, 15, 10, 20, 8, star);
        g.set(15, 8, bulb);
        // Corps de la machine, reflets de lumière à gauche.
        g.rect(1, 13, 29, 37, body);
        g.rect(24, 13, 29, 37, bodyDark);
        g.line(2, 14, 2, 36, light);
        g.line(3, 14, 3, 20, light);
        // Cinq rouleaux alignant des 7, flèches dorées sur la ligne gagnante.
        g.rect(3, 15, 28, 25, panel);
        for (int i = 0; i < 5; i++) {
            int x0 = 4 + i * 5;
            g.rect(x0, 16, x0 + 3, 24, reel);
            g.rect(x0 + 3, 16, x0 + 3, 24, reelShade);
            g.line(x0, 18, x0 + 3, 18, seven);
            g.line(x0 + 3, 19, x0 + 1, 22, seven);
        }
        g.set(2, 20, rgba("ffb43a"));
        g.set(1, 20, rgba("ffb43a"));
        // Rangée de lumières, puis plateau débordant de pièces.
        for (int x = 3; x <= 28; x += 2) g.set(x, 27, x % 4 == 1 ? bulb : bulbOff);
        g.rect(5, 30, 25, 35, bodyDeep);
        g.rect(6, 31, 24, 35, panel);
        int[][] coins = {{8, 33}, {12, 32}, {16, 33}, {20, 32}, {23, 34}, {10, 35}, {18, 35}};
        for (int[] c : coins) {
            g.ellipse(c[0], c[1], 2f, 1.2f, GOLD);
            g.set(c[0] - 1, c[1], light);
        }
        // Socle large, pièces qui débordent.
        g.rect(0, 38, 31, 41, bodyDark);
        g.rect(0, 38, 31, 38, body);
        g.rect(26, 39, 31, 41, bodyDeep);
        g.ellipse(6f, 40f, 2f, 1f, GOLD);
        g.ellipse(13f, 40.5f, 2f, 1f, light);
        // Le levier sur le flanc droit, boule rouge en haut.
        g.rect(29, 22, 32, 28, bodyDeep);
        g.rect(31, 6, 32, 23, rgba("e0e8f4"));
        g.rect(32, 6, 32, 23, rgba("8a94a8"));
        g.ellipseShaded(31.5f, 4f, 2.5f, 2.5f, ball, ballDark, 0.5f);
        g.set(31, 3, rgba("ffb4b4"));
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Symboles (16 x 16)
    // -------------------------------------------------------------------------

    /** Faux billet : un billet verdâtre au portrait brouillé, barré d'une croix rouge. */
    static int[][] fakeMoney() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int note = rgba("8ac88a"), noteDark = rgba("4e8a52"), noteShade = rgba("6aa86e"), ink = rgba("2e5a32");
        g.rect(0, 3, 15, 12, noteDark);
        g.rect(1, 4, 14, 11, note);
        g.rect(11, 4, 14, 11, noteShade);
        g.ellipse(7.5f, 7.5f, 2.5f, 2.5f, ink);
        g.ellipse(7.5f, 7f, 1.2f, 1.2f, note);
        g.set(2, 5, ink); g.set(13, 10, ink); g.set(3, 10, ink); g.set(12, 5, ink);
        g.line(1, 1, 14, 14, RED);
        g.line(2, 1, 15, 14, RED);
        g.line(14, 1, 1, 14, RED);
        g.line(15, 1, 2, 14, RED);
        return g.outlined();
    }

    /** Prédiction : une boule de cristal violette sur son socle d'or, un œil qui regarde au fond. */
    static int[][] prediction() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int ball = rgba("b490f0"), ballDark = rgba("7a50c0"), white = rgba("f6f0e2"), iris = rgba("4a1a8a");
        g.rect(3, 13, 12, 15, GOLD_DARK);
        g.rect(4, 12, 11, 13, GOLD);
        g.ellipseShaded(7.5f, 6.5f, 6f, 6f, ball, ballDark, 2f);
        g.ellipse(7.5f, 7f, 3.5f, 2f, white);
        g.ellipse(7.5f, 7f, 1.6f, 1.6f, iris);
        g.set(7, 7, EYE);
        g.set(8, 7, EYE);
        g.set(4, 3, rgba("ffffff"));
        g.set(5, 2, rgba("e8d8ff"));
        return g.outlined();
    }

    /** Duel : deux rapières croisées, lames d'acier et coquilles d'or. */
    static int[][] duel() {
        PixelCanvas g = new PixelCanvas(16, 16);
        // Rapière de gauche : pointe en haut à gauche, poignée en bas à droite.
        g.line(1, 1, 10, 10, STEEL_LIGHT);
        g.line(2, 1, 10, 9, STEEL_DARK);
        g.line(9, 12, 12, 9, GOLD);
        g.line(11, 11, 13, 13, WOOD);
        g.set(14, 14, GOLD);
        g.set(15, 15, GOLD_DARK);
        // Rapière de droite, en miroir.
        g.line(14, 1, 5, 10, STEEL_LIGHT);
        g.line(14, 2, 6, 10, STEEL_DARK);
        g.line(3, 9, 6, 12, GOLD);
        g.line(4, 11, 2, 13, WOOD);
        g.set(1, 14, GOLD);
        g.set(0, 15, GOLD_DARK);
        return g.outlined();
    }

    /** Taxe : un tampon de bois au-dessus d'un papier frappé d'un cachet rouge pour cent. */
    static int[][] tax() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int paper = rgba("f6f0e2"), shade = rgba("d8d0c0");
        g.rect(1, 6, 14, 15, paper);
        g.rect(12, 6, 14, 15, shade);
        g.rect(3, 7, 12, 15, RED);
        g.rect(4, 8, 11, 14, paper);
        g.line(10, 8, 5, 14, RED);
        g.rect(5, 9, 6, 10, RED);
        g.rect(9, 12, 10, 13, RED);
        g.ellipseShaded(7.5f, 1.5f, 2.5f, 1.5f, WOOD, WOOD_DARK, 0.5f);
        g.rect(7, 3, 8, 3, WOOD_DARK);
        g.rect(3, 4, 12, 5, RED_DARK);
        g.rect(3, 4, 12, 4, WOOD);
        return g.outlined();
    }

    /** Nouvelle règle : un parchemin déroulé, quelques lignes, et un sceau de cire rouge. */
    static int[][] newRule() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int paper = rgba("f0e0b0"), shade = rgba("d0bc88"), roll = rgba("b8985a"), text = rgba("6a5a40");
        g.rect(3, 2, 12, 13, paper);
        g.rect(10, 2, 12, 13, shade);
        g.rect(2, 0, 13, 2, roll);
        g.rect(2, 13, 13, 15, roll);
        g.set(2, 1, WOOD_DARK); g.set(13, 1, WOOD_DARK);
        for (int y = 4; y <= 10; y += 2) g.line(4, y, y == 8 ? 7 : 10, y, text);
        g.triangle(9, 13, 11, 13, 9, 16, RED_DARK);
        g.triangle(12, 13, 14, 13, 14, 16, RED_DARK);
        g.ellipseShaded(11.5f, 11f, 3f, 3f, RED, RED_DARK, 1f);
        g.set(11, 10, rgba("ff7a6a"));
        return g.outlined();
    }

    /** Fouille : une loupe cerclée d'or, la lentille bleutée, le manche de bois. */
    static int[][] frisk() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int lens = rgba("c8e6f0"), lensDark = rgba("8ab0c8");
        g.line(9, 10, 14, 15, WOOD);
        g.line(10, 9, 15, 14, WOOD_DARK);
        g.line(10, 10, 15, 15, WOOD);
        g.ellipse(6f, 6f, 5.5f, 5.5f, GOLD_DARK);
        g.ellipse(6f, 6f, 4.6f, 4.6f, GOLD);
        g.ellipseShaded(6f, 6f, 3.6f, 3.6f, lens, lensDark, 1f);
        g.line(4, 6, 6, 4, rgba("ffffff"));
        return g.outlined();
    }

    /** Banqueroute : une tirelire cochon fendue en deux, une pièce qui s'en échappe. */
    static int[][] bankruptcy() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int pig = rgba("f0a0b0"), pigDark = rgba("c87080"), snout = rgba("e07a90");
        g.rect(3, 11, 4, 14, pigDark);
        g.rect(10, 11, 11, 14, pigDark);
        g.line(0, 6, 1, 8, pigDark); // queue en tire-bouchon
        g.set(0, 7, pigDark);
        g.ellipseShaded(7.5f, 8.5f, 6f, 4.5f, pig, pigDark, 2f);
        g.triangle(9, 5, 12, 5, 10, 1, pig);
        g.rect(13, 6, 15, 10, snout);
        g.set(14, 7, rgba("8a3a4a"));
        g.set(14, 9, rgba("8a3a4a"));
        g.set(11, 7, EYE);
        g.rect(5, 4, 8, 4, rgba("4a1a2a"));
        // La fêlure, de haut en bas.
        int[][] crack = {{5, 4}, {6, 5}, {5, 6}, {6, 7}, {7, 8}, {6, 9}, {7, 10}, {6, 11}, {7, 12}, {7, 13}};
        for (int[] c : crack) g.set(c[0], c[1], 0);
        g.ellipse(2f, 14f, 1.8f, 1.2f, GOLD);
        g.set(1, 14, GOLD_LIGHT);
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Illustrations des chapitres 4 à 6
    // -------------------------------------------------------------------------

    /**
     * Chapitre 4, « Le Monde sans Maître » : une cité de tables de jeu en
     * flammes sous un ciel de fumée ; des tables renversées, et des joueurs
     * qui se disputent les éclats d'or de la comète.
     */
    static int[][] worldWithoutMaster() {
        int w = EnemyPortraits.ART_WIDTH, h = EnemyPortraits.ART_HEIGHT;
        PixelCanvas g = new PixelCanvas(w, h);
        // Ciel de nuit rougi par l'incendie.
        int[] sky = {rgba("120810"), rgba("1e0c14"), rgba("2e1018"), rgba("441618"), rgba("5e2014"), rgba("7a3014")};
        for (int y = 0; y < h; y++) {
            int band = Math.min(sky.length - 1, y * sky.length / 52);
            for (int x = 0; x < w; x++) g.set(x, y, sky[band]);
        }
        Random random = new Random(4);
        for (int i = 0; i < 25; i++) g.set(random.nextInt(w), random.nextInt(14), rgba("c8b0b0"));
        // Colonnes de fumée qui montent et s'étalent.
        int smoke = rgba("3a2a2a"), smokeLight = rgba("4e3834");
        int[] sources = {18, 50, 98, 120};
        for (int s : sources) {
            for (int i = 0; i < 9; i++) {
                float y = 40 - i * 5f, x = s + i * 2.2f + (i % 2) * 2;
                g.ellipse(x, y, 4f + i * 0.9f, 3f + i * 0.4f, i % 3 == 0 ? smokeLight : smoke);
            }
        }
        // La cité de tables : tours en silhouette, fenêtres en feu.
        int ruin = rgba("1a0c10"), ruinLight = rgba("2e1a1e"), window = rgba("ff8a1e");
        int[][] towers = {{2, 34, 10}, {14, 28, 8}, {24, 38, 10}, {40, 32, 7}, {78, 36, 8}, {88, 30, 9}, {100, 38, 8},
            {110, 26, 9}, {120, 34, 8}};
        for (int[] t : towers) {
            g.rect(t[0], t[1], t[0] + t[2], 56, ruin);
            g.rect(t[0], t[1], t[0], 56, ruinLight);
            for (int y = t[1] + 3; y < 54; y += 5) {
                for (int x = t[0] + 2; x < t[0] + t[2] - 1; x += 3) if (random.nextInt(3) > 0) g.set(x, y, window);
            }
        }
        // Flammes sur les toits.
        int[][] fires = {{6, 34}, {18, 28}, {44, 32}, {92, 30}, {114, 26}, {124, 34}};
        for (int[] f : fires) {
            g.triangle(f[0] - 5, f[1], f[0] + 5, f[1], f[0], f[1] - 11, RED);
            g.triangle(f[0] - 3, f[1], f[0] + 4, f[1], f[0] + 1, f[1] - 8, rgba("ff8a1e"));
            g.triangle(f[0] - 2, f[1], f[0] + 2, f[1], f[0], f[1] - 5, GOLD);
        }
        // Le sol, rougi par les braises.
        g.rect(0, 56, w - 1, h - 1, rgba("2a1410"));
        g.rect(0, 56, w - 1, 57, rgba("4a2014"));
        for (int i = 0; i < 60; i++) g.set(random.nextInt(w), 58 + random.nextInt(22), rgba("3e1c14"));
        // Une grande table de jeu encore debout, en flammes.
        int felt = rgba("2e7a46"), feltDark = rgba("1e5230");
        g.rect(8, 64, 9, 72, WOOD_DARK);
        g.rect(30, 64, 31, 72, WOOD_DARK);
        g.ellipse(20f, 63f, 15f, 4f, WOOD);
        g.ellipse(20f, 62.5f, 13f, 3f, felt);
        g.ellipse(24f, 63f, 6f, 2f, feltDark);
        g.triangle(22, 61, 32, 61, 28, 50, RED);
        g.triangle(24, 61, 31, 61, 28, 54, rgba("ff8a1e"));
        g.triangle(26, 61, 30, 61, 28, 57, GOLD);
        // Tables renversées : dessous de bois, pieds en l'air.
        int[][] flipped = {{94, 62}, {58, 70}};
        for (int[] t : flipped) {
            g.rect(t[0], t[1], t[0] + 20, t[1] + 4, WOOD_DARK);
            g.rect(t[0], t[1], t[0] + 20, t[1], WOOD);
            g.rect(t[0], t[1] + 5, t[0] + 20, t[1] + 6, felt);
            g.rect(t[0] + 2, t[1] - 6, t[0] + 3, t[1] - 1, WOOD_DARK);
            g.rect(t[0] + 17, t[1] - 6, t[0] + 18, t[1] - 1, WOOD_DARK);
            g.rect(t[0] + 10, t[1] - 4, t[0] + 11, t[1] - 1, WOOD_DARK);
        }
        // Cartes et jetons éparpillés.
        int[][] litter = {{40, 74}, {46, 66}, {84, 75}, {118, 72}, {6, 76}, {72, 62}};
        for (int i = 0; i < litter.length; i++) {
            int[] l = litter[i];
            if (i % 2 == 0) g.rect(l[0], l[1], l[0] + 2, l[1] + 3, WHITE);
            else g.ellipse(l[0], l[1], 2f, 1f, RED);
        }
        // Les joueurs qui se disputent les éclats d'or.
        int body = rgba("0e0608");
        int[][] fighters = {{56, 58, 1}, {72, 58, -1}, {104, 58, 1}, {118, 58, -1}, {42, 58, 1}};
        for (int[] f : fighters) {
            int x = f[0], y = f[1], d = f[2];
            g.ellipse(x, y - 11, 2f, 2f, body);
            g.rect(x - 1, y - 9, x + 1, y - 4, body);
            g.line(x - 1, y - 3, x - 3, y, body);
            g.line(x + 1, y - 3, x + 3, y, body);
            g.line(x, y - 8, x + 5 * d, y - 11, body);
            g.line(x, y - 7, x + 5 * d, y - 9, body);
        }
        int[][] shards = {{64, 47}, {111, 47}, {47, 50}};
        for (int[] s : shards) {
            g.ellipse(s[0], s[1], 4f, 4f, rgba("8a5a14"));
            g.ellipse(s[0], s[1], 2.5f, 2.5f, rgba("c4891e"));
            g.triangle(s[0] - 2, s[1], s[0] + 2, s[1], s[0], s[1] - 4, GOLD);
            g.triangle(s[0] - 2, s[1], s[0] + 2, s[1], s[0], s[1] + 3, GOLD_DARK);
            g.set(s[0], s[1] - 1, GOLD_LIGHT);
        }
        // Braises qui volent.
        for (int i = 0; i < 40; i++) {
            g.set(random.nextInt(w), 10 + random.nextInt(50), random.nextInt(2) == 0 ? rgba("ff8a1e") : GOLD);
        }
        return g.pixels;
    }

    /**
     * Chapitre 5, « La Maison » : un immense manoir d'or posé sur une mer de
     * nuages au crépuscule ; ses fenêtres ont la forme de cartes à jouer, et sa
     * porte est la fente d'une machine à sous, où tombe une pièce.
     */
    static int[][] house() {
        int w = EnemyPortraits.ART_WIDTH, h = EnemyPortraits.ART_HEIGHT;
        PixelCanvas g = new PixelCanvas(w, h);
        // Ciel du soir, du bleu nuit à l'orangé au ras des nuages.
        int[] sky = {rgba("1a1a4a"), rgba("2a225a"), rgba("4a2a6a"), rgba("7a3a7a"), rgba("b85a7a"), rgba("e88a6a"),
            rgba("ffb46a")};
        for (int y = 0; y < h; y++) {
            int band = Math.min(sky.length - 1, y * sky.length / 62);
            for (int x = 0; x < w; x++) g.set(x, y, sky[band]);
        }
        Random random = new Random(9);
        for (int i = 0; i < 30; i++) g.set(random.nextInt(w), random.nextInt(20), rgba("e8e0ff"));
        // Halo doré derrière le manoir.
        g.ellipse(64f, 38f, 52f, 30f, rgba("c87a6a"));
        g.ellipse(64f, 38f, 40f, 24f, rgba("e89a6a"));
        int wall = rgba("ffd54a"), wallDark = rgba("c4891e"), wing = rgba("e8b83a"), wingDark = rgba("a8741a");
        int roof = rgba("8a3a2a"), roofDark = rgba("5a2418"), card = rgba("f6f0e2"), black = rgba("1a1a24");
        // Tours aux extrémités, toits pointus.
        int[] towers = {8, 112};
        for (int x0 : towers) {
            g.rect(x0, 26, x0 + 8, 62, x0 < 64 ? wing : wingDark);
            g.triangle(x0 - 1, 26, x0 + 9, 26, x0 + 4, 14, x0 < 64 ? roof : roofDark);
            g.set(x0 + 4, 13, GOLD);
            g.set(x0 + 4, 12, GOLD);
            g.rect(x0 + 3, 30, x0 + 5, 34, card);
            g.set(x0 + 4, 32, RED);
        }
        // Ailes, toit en terrasse à balustres.
        g.rect(16, 34, 42, 62, wing);
        g.rect(86, 34, 112, 62, wingDark);
        g.rect(16, 32, 42, 33, wingDark);
        g.rect(86, 32, 112, 33, roofDark);
        for (int x = 17; x <= 111; x += 3) if (x < 42 || x > 86) g.rect(x, 30, x, 31, x < 64 ? wing : wingDark);
        // Corps central et son fronton.
        g.rect(42, 24, 86, 62, wall);
        g.rect(76, 24, 86, 62, wallDark);
        g.triangle(38, 24, 90, 24, 64, 8, wall);
        g.triangle(64, 8, 90, 24, 64, 24, wallDark);
        g.line(38, 24, 90, 24, roof);
        g.line(38, 25, 90, 25, roofDark);
        // Blason sur le fronton : un trèfle d'or sur fond rouge.
        g.ellipse(64f, 17f, 4f, 4f, RED);
        g.ellipse(64f, 15.5f, 1.4f, 1.4f, GOLD);
        g.ellipse(62.5f, 18f, 1.4f, 1.4f, GOLD);
        g.ellipse(65.5f, 18f, 1.4f, 1.4f, GOLD);
        g.set(64, 20, GOLD);
        g.set(64, 7, GOLD); g.set(64, 6, GOLD_LIGHT);
        // Colonnes de la façade.
        for (int x : new int[] {45, 52, 75, 82}) {
            g.rect(x, 27, x + 1, 62, GOLD_LIGHT);
            g.rect(x + 2, 27, x + 2, 62, wallDark);
        }
        // Fenêtres en forme de cartes à jouer : cœurs rouges et piques noirs.
        int[][] windows = {{20, 38}, {28, 38}, {36, 38}, {20, 50}, {28, 50}, {36, 50}, {88, 38}, {96, 38}, {104, 38},
            {88, 50}, {96, 50}, {104, 50}, {57, 29}, {67, 29}};
        for (int i = 0; i < windows.length; i++) {
            int x0 = windows[i][0], y0 = windows[i][1];
            int behind = g.get(x0 - 1, y0);
            g.rect(x0, y0, x0 + 4, y0 + 6, card);
            // Coins arrondis, comme une vraie carte.
            g.set(x0, y0, behind); g.set(x0 + 4, y0, behind); g.set(x0, y0 + 6, behind); g.set(x0 + 4, y0 + 6, behind);
            boolean heart = (i + i / 3) % 2 == 0;
            int pip = heart ? RED : black;
            g.rect(x0 + 1, y0 + 2, x0 + 3, y0 + 3, pip);
            g.set(x0 + 2, y0 + 4, pip);
            g.set(x0 + 2, y0 + (heart ? 2 : 1), pip);
            if (heart) g.set(x0 + 2, y0 + 1, card);
        }
        // La porte : une fente de machine à sous dans un grand cadre d'or, une pièce qui tombe.
        g.rect(56, 38, 72, 62, wallDark);
        g.ellipse(64f, 38f, 8f, 4f, wallDark);
        g.rect(58, 39, 70, 62, GOLD);
        g.ellipse(64f, 39f, 6f, 3f, GOLD);
        g.rect(62, 42, 66, 60, black);
        g.rect(63, 43, 63, 59, rgba("3a2a3a"));
        g.ellipse(64f, 40f, 2.5f, 1f, GOLD_LIGHT);
        g.rect(52, 62, 76, 63, wallDark);
        g.rect(54, 64, 74, 65, wall);
        // Mer de nuages qui avale les fondations.
        int cloud = rgba("fff4f0"), cloudPink = rgba("f0c8d0"), cloudShade = rgba("c8a0c0");
        float[][] puffs = {{4, 70, 14, 8}, {24, 66, 12, 6}, {42, 72, 16, 8}, {64, 70, 14, 6}, {86, 72, 16, 8},
            {104, 66, 12, 6}, {124, 70, 14, 8}, {14, 78, 16, 6}, {64, 79, 22, 6}, {112, 78, 16, 6}};
        for (float[] p : puffs) g.ellipse(p[0], p[1] + 2, p[2], p[3], cloudShade);
        for (float[] p : puffs) g.ellipse(p[0], p[1], p[2], p[3], cloudPink);
        for (float[] p : puffs) g.ellipse(p[0] - 2, p[1] - 1.5f, p[2] * 0.7f, p[3] * 0.6f, cloud);
        return g.pixels;
    }

    /**
     * Chapitre 6, « Le Jackpot » : le sommet de la tour, parmi les étoiles ;
     * une immense machine à sous d'or et de lumière y trône, cinq rouleaux
     * alignés sur des 7, et son levier attend qu'on le tire.
     */
    static int[][] jackpot() {
        int w = EnemyPortraits.ART_WIDTH, h = EnemyPortraits.ART_HEIGHT;
        PixelCanvas g = new PixelCanvas(w, h);
        // Ciel profond et étoilé.
        int[] sky = {rgba("06061a"), rgba("0c0a26"), rgba("140e32"), rgba("1e123e"), rgba("2a1648")};
        for (int y = 0; y < h; y++) {
            int band = Math.min(sky.length - 1, y * sky.length / h);
            for (int x = 0; x < w; x++) g.set(x, y, sky[band]);
        }
        Random random = new Random(6);
        for (int i = 0; i < 110; i++) {
            g.set(random.nextInt(w), random.nextInt(h - 12), random.nextInt(4) == 0 ? rgba("ffe9a0") : rgba("c8d0ff"));
        }
        // Rayons de lumière qui partent de la machine.
        int ray = rgba("3a2a58"), rayGold = rgba("5a4430");
        for (int i = 0; i < 16; i++) {
            double a = i * Math.PI / 8 + 0.1;
            g.line(64, 38, (int) Math.round(64 + Math.cos(a) * 90), (int) Math.round(38 + Math.sin(a) * 60), i % 2 == 0 ? ray : rayGold);
        }
        // Halo autour de la machine.
        g.ellipse(64f, 38f, 36f, 28f, rgba("3a2448"));
        g.ellipse(64f, 38f, 30f, 24f, rgba("5a3a4a"));
        // Grandes étoiles à quatre branches.
        int[][] sparkles = {{14, 12}, {108, 8}, {22, 40}, {116, 34}, {94, 20}, {34, 18}};
        for (int[] s : sparkles) {
            g.line(s[0] - 3, s[1], s[0] + 3, s[1], rgba("c8d0ff"));
            g.line(s[0], s[1] - 3, s[0], s[1] + 3, rgba("c8d0ff"));
            g.set(s[0], s[1], rgba("ffffff"));
        }
        // Le sommet de la tour : plateforme de pierre crénelée.
        int stone = rgba("4a4a6a"), stoneDark = rgba("2e2e48"), stoneLight = rgba("6a6a8e");
        g.rect(8, 66, 120, 79, stone);
        g.rect(90, 66, 120, 79, stoneDark);
        for (int x = 8; x <= 116; x += 10) {
            g.rect(x, 61, x + 5, 65, x < 90 ? stone : stoneDark);
            g.line(x, 61, x + 5, 61, stoneLight);
        }
        for (int y = 70; y <= 78; y += 4) g.line(8, y, 120, y, stoneDark);
        for (int x = 14; x < 120; x += 12) g.line(x, 66, x, 79, stoneDark);
        // La machine : fronton en arc couronné d'ampoules.
        int body = rgba("ffd54a"), bodyDark = rgba("c4891e"), bodyDeep = rgba("8a5a12"), light = rgba("fff6c8");
        int panel = rgba("1a0f0f"), reel = rgba("f4efe4"), reelShade = rgba("c8c0b4"), seven = rgba("d8202c");
        g.ellipseShaded(63f, 20f, 24f, 13f, body, bodyDark, 12f);
        g.ellipse(63f, 20f, 20f, 10f, bodyDeep);
        for (int i = 0; i <= 16; i++) {
            double a = Math.PI + i * Math.PI / 16;
            g.set((int) Math.round(63 + Math.cos(a) * 22), (int) Math.round(20 + Math.sin(a) * 11.5),
                i % 2 == 0 ? rgba("ffffff") : rgba("ff8a1e"));
        }
        g.triangle(56, 18, 70, 18, 63, 10, light);
        g.triangle(56, 18, 70, 18, 63, 26, light);
        g.triangle(58, 22, 68, 22, 63, 14, rgba("ffffff"));
        // Corps de la machine, reflets de lumière à gauche.
        g.rect(39, 22, 87, 62, body);
        g.rect(79, 22, 87, 62, bodyDark);
        g.line(40, 23, 40, 61, light);
        g.line(41, 23, 41, 40, light);
        // Cinq rouleaux sur des 7, ligne gagnante.
        g.rect(43, 28, 83, 44, panel);
        for (int i = 0; i < 5; i++) {
            int x0 = 44 + i * 8;
            g.rect(x0, 29, x0 + 6, 43, reel);
            g.rect(x0 + 5, 29, x0 + 6, 43, reelShade);
            g.rect(x0 + 1, 32, x0 + 5, 33, seven);
            g.line(x0 + 5, 34, x0 + 2, 40, seven);
            g.line(x0 + 4, 34, x0 + 1, 40, seven);
        }
        g.line(42, 36, 84, 36, rgba("ffb43a"));
        for (int i = 0; i < 5; i++) {
            int x0 = 44 + i * 8;
            g.set(x0 + 3, 36, seven); g.set(x0 + 4, 36, seven);
        }
        // Lumières, plateau à pièces et socle.
        for (int x = 43; x <= 83; x += 3) g.set(x, 47, x % 6 == 1 ? rgba("ffffff") : rgba("ff8a1e"));
        g.rect(47, 51, 79, 58, bodyDeep);
        g.rect(48, 52, 78, 58, panel);
        for (int i = 0; i < 9; i++) {
            int x = 51 + i * 3 + (i % 2), y = 56 - (i % 3);
            g.ellipse(x, y, 2f, 1.2f, GOLD);
            g.set(x - 1, y, light);
        }
        g.rect(35, 60, 91, 66, bodyDark);
        g.rect(35, 60, 91, 60, body);
        g.rect(80, 61, 91, 66, bodyDeep);
        // Le levier, à droite, qui attend qu'on le tire.
        g.rect(87, 40, 92, 48, bodyDeep);
        g.rect(93, 14, 95, 44, rgba("e0e8f4"));
        g.rect(95, 14, 95, 44, rgba("8a94a8"));
        g.line(88, 44, 93, 44, rgba("e0e8f4"));
        g.ellipseShaded(94f, 11f, 4f, 4f, rgba("e8203a"), rgba("a0101e"), 1f);
        g.set(92, 9, rgba("ffb4b4"));
        g.set(93, 9, rgba("ffb4b4"));
        // Éclats de lumière autour de la machine.
        for (int i = 0; i < 24; i++) {
            int x = (int) (64 + random.nextGaussian() * 30), y = (int) (38 + random.nextGaussian() * 18);
            if (g.get(x, y) != body && g.get(x, y) != bodyDark) g.set(x, y, light);
        }
        return g.pixels;
    }
}
