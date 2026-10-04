package fr.astratime.lucky.assets;

import java.util.Random;

import static fr.astratime.lucky.assets.PixelCanvas.rgba;

/**
 * Dessins en pixel art du chapitre 2 de la Tour des épreuves, « Les Tables
 * Sacrées » : ses ennemis (en buste, à la taille du croupier), leurs symboles
 * et l'illustration du chapitre. Chaque méthode rend une grille de couleurs
 * RGBA8888 (0 = transparent), transformée en texture par {@link EnemyTextures}.
 */
final class Chapter2Art {

    private static final int W = EnemyPortraits.WIDTH, H = EnemyPortraits.HEIGHT;

    private Chapter2Art() {}

    /**
     * Croupier en chef : le croupier démoniaque, monté en grade. Ruban et bord
     * du haut-de-forme dorés, couronne de pointes d'or, épaulettes à franges
     * et monocle.
     */
    static int[][] chef() {
        int[][] g = EnemyTextures.croupierGrid();
        int gold = rgba("ffd54a"), goldDark = rgba("c4891e"), glass = rgba("c8e6f0"), chain = rgba("e8c860");
        // Ruban doré à la place du rouge, rehaussé d'une pierre.
        for (int x = 10; x <= 23; x++) { g[7][x] = gold; g[8][x] = x % 3 == 0 ? goldDark : gold; }
        g[7][16] = rgba("d8202c");
        g[7][17] = rgba("d8202c");
        // Pointes de couronne sur la calotte.
        for (int x : new int[] {11, 14, 17, 20, 23}) { g[1][x] = gold; g[2][x] = goldDark; }
        // Épaulettes à franges.
        for (int y = 28; y <= 30; y++) {
            for (int x = 4; x <= 9; x++) g[y][x] = y == 30 ? goldDark : gold;
            for (int x = 24; x <= 29; x++) g[y][x] = y == 30 ? goldDark : gold;
        }
        for (int x = 4; x <= 9; x += 2) g[31][x] = gold;
        for (int x = 25; x <= 29; x += 2) g[31][x] = gold;
        // Monocle sur l'œil droit, et sa chaînette.
        g[15][18] = goldDark; g[15][19] = goldDark; g[15][20] = goldDark; g[15][21] = goldDark; g[15][22] = goldDark;
        g[17][18] = goldDark; g[17][19] = goldDark; g[17][20] = goldDark; g[17][21] = goldDark; g[17][22] = goldDark;
        g[16][18] = goldDark; g[16][22] = goldDark;
        g[16][19] = glass;
        for (int y = 18; y <= 23; y++) g[y][22 + (y % 2)] = chain;
        return g;
    }

    /**
     * Tricheur aux Dés pipés : cheveux gominés, fine moustache, un œil qui
     * cligne, sourire en coin, gilet vert et deux dés rouges entre les doigts,
     * un as qui dépasse de la manche.
     */
    static int[][] tricheur() {
        PixelCanvas g = new PixelCanvas(W, H);
        int skin = rgba("e8b48a"), skinShade = rgba("c08a62"), hair = rgba("1e1a2e"), hairShine = rgba("4a4670");
        int vest = rgba("2e7a46"), vestDark = rgba("1e5230"), shirt = rgba("ece6da"), shirtShade = rgba("c8c0b4");
        int tie = rgba("6a2a8a"), eye = rgba("2a1e14"), mouth = rgba("7a2a20"), die = rgba("e83a3a");
        int dieDark = rgba("a01e1e"), pip = rgba("ffffff"), card = rgba("f6f0e2"), red = rgba("d8202c");

        // Buste : chemise blanche, gilet vert, lavallière violette.
        g.rect(4, 29, 29, 41, shirt);
        g.rect(20, 29, 29, 41, shirtShade);
        g.rect(4, 31, 12, 41, vest);
        g.rect(21, 31, 29, 41, vestDark);
        g.ellipseShaded(7f, 31f, 4f, 3f, shirt, shirtShade, 3f);
        g.ellipseShaded(27f, 31f, 4f, 3f, shirtShade, shirtShade, 0f);
        for (int y = 33; y <= 40; y += 3) { g.set(12, y, rgba("ffd54a")); g.set(21, y, rgba("ffd54a")); }
        g.triangle(14, 27, 20, 27, 17, 33, tie);
        g.rect(16, 27, 18, 28, rgba("8a3aaa"));
        // Cou et visage étroit.
        g.rect(14, 24, 20, 27, skinShade);
        g.ellipseShaded(17f, 16f, 6.5f, 8.5f, skin, skinShade, 2f);
        // Cheveux gominés, raie sur le côté, mèche brillante.
        g.ellipse(17f, 9.5f, 7.5f, 4.5f, hair);
        g.rect(10, 9, 11, 15, hair);
        g.rect(23, 9, 24, 13, hair);
        g.line(12, 7, 20, 6, hairShine);
        g.line(13, 8, 15, 8, hairShine);
        // Yeux : l'un plissé (clin d'œil), l'autre rusé ; sourcil levé.
        g.line(12, 13, 15, 12, hair);
        g.line(19, 13, 22, 13, hair);
        g.rect(13, 15, 14, 15, eye);
        g.line(19, 16, 22, 15, eye);
        // Nez long, fine moustache, sourire en coin.
        g.set(17, 17, skinShade);
        g.set(17, 18, skinShade);
        g.line(13, 20, 16, 19, hair);
        g.line(18, 19, 21, 20, hair);
        g.line(14, 22, 19, 22, mouth);
        g.set(20, 21, mouth);
        // Main levée avec deux dés pipés.
        g.ellipse(27f, 22f, 3f, 2.5f, skin);
        g.rect(24, 15, 28, 19, die);
        g.rect(27, 15, 28, 19, dieDark);
        g.set(25, 16, pip); g.set(26, 17, pip); g.set(27, 18, pip);
        g.rect(29, 18, 32, 21, die);
        g.rect(31, 18, 32, 21, dieDark);
        g.set(30, 19, pip); g.set(31, 20, pip);
        // As qui dépasse de la manche gauche.
        g.rect(2, 34, 5, 39, card);
        g.set(3, 36, red);
        g.set(4, 37, red);
        return g.outlined();
    }

    /**
     * Usurier du Comptoir : vieillard chauve à visière verte de banquier,
     * lunettes rondes, gros nez, favoris gris, et une pile de pièces d'or
     * serrée contre lui.
     */
    static int[][] usurier() {
        PixelCanvas g = new PixelCanvas(W, H);
        int skin = rgba("e0b090"), skinShade = rgba("b08060"), visor = rgba("2e8a5a"), visorDark = rgba("1e5a3a");
        int grey = rgba("c8c8d0"), greyDark = rgba("8a8a96"), lens = rgba("c8e6f0"), rim = rgba("c4891e");
        int coat = rgba("4a3a2a"), coatDark = rgba("2e2418"), shirt = rgba("ece6da"), gold = rgba("ffd54a");
        int goldDark = rgba("c4891e"), mouth = rgba("5a2a20"), eye = rgba("1a1018");

        // Redingote brune, col blanc, sangle de manchettes.
        g.rect(4, 29, 29, 41, coat);
        g.rect(20, 29, 29, 41, coatDark);
        g.ellipseShaded(7f, 31f, 4.5f, 3.5f, coat, coatDark, 3f);
        g.ellipseShaded(27f, 31f, 4.5f, 3.5f, coatDark, coatDark, 0f);
        g.triangle(13, 28, 21, 28, 17, 36, shirt);
        // Visage rond, crâne chauve et luisant, favoris gris.
        g.rect(14, 24, 20, 28, skinShade);
        g.ellipseShaded(17f, 15f, 8f, 9f, skin, skinShade, 3f);
        g.ellipse(14f, 8f, 2f, 1.2f, rgba("f6dcc4"));
        g.rect(9, 14, 10, 22, grey);
        g.rect(24, 14, 25, 22, greyDark);
        // Visière verte de banquier.
        g.rect(8, 11, 26, 12, visor);
        g.triangle(8, 13, 26, 13, 17, 16, visorDark);
        // Lunettes rondes sur de petits yeux.
        g.ellipse(13.5f, 17f, 2.5f, 2f, rim);
        g.ellipse(20.5f, 17f, 2.5f, 2f, rim);
        g.ellipse(13.5f, 17f, 1.5f, 1.2f, lens);
        g.ellipse(20.5f, 17f, 1.5f, 1.2f, lens);
        g.set(13, 17, eye);
        g.set(20, 17, eye);
        g.line(16, 17, 18, 17, rim);
        // Gros nez rouge, sourire pincé.
        g.ellipse(17f, 20f, 1.8f, 1.5f, rgba("d07a6a"));
        g.line(14, 23, 20, 23, mouth);
        g.set(13, 22, mouth);
        // Pile de pièces d'or, tenue à deux mains.
        for (int i = 0; i < 5; i++) {
            int y = 39 - i * 2;
            g.ellipse(17f, y, 5f, 1.5f, i % 2 == 0 ? gold : goldDark);
        }
        g.ellipse(17f, 30.5f, 5f, 1.5f, gold);
        g.set(15, 30, rgba("fff6c8"));
        g.ellipse(10.5f, 37f, 2f, 2f, skin);
        g.ellipse(23.5f, 37f, 2f, 2f, skinShade);
        return g.outlined();
    }

    /**
     * Roulette Vivante : une roue de roulette dressée sur son pied, cases
     * rouges et noires, un œil unique en guise de bille au centre, et une
     * bouche pleine de dents sous le moyeu.
     */
    static int[][] roulette() {
        PixelCanvas g = new PixelCanvas(W, H);
        int wood = rgba("7a4a24"), woodDark = rgba("4e2e14"), gold = rgba("ffd54a"), goldDark = rgba("c4891e");
        int red = rgba("d8202c"), black = rgba("1e1a24"), green = rgba("2e9a4a"), white = rgba("f4efe4");
        int iris = rgba("ffd54a"), pupil = rgba("1a0f0f"), mouth = rgba("3a0c14");

        // Pied de bois.
        g.rect(14, 33, 20, 38, woodDark);
        g.ellipse(17f, 40f, 9f, 2f, wood);
        // Jante dorée et cases rouges et noires.
        g.ellipse(17f, 18f, 15f, 15f, goldDark);
        g.ellipse(17f, 18f, 14f, 14f, gold);
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                double dx = x - 17, dy = y - 18, d = Math.hypot(dx, dy);
                if (d > 12.8 || d < 8.5) continue;
                int pocket = (int) Math.floor((Math.atan2(dy, dx) + Math.PI) / (2 * Math.PI) * 18);
                g.set(x, y, pocket == 0 ? green : pocket % 2 == 0 ? red : black);
            }
        }
        // Moyeu en bois et ses rayons dorés.
        g.ellipse(17f, 18f, 8f, 8f, wood);
        g.ellipse(17f, 18f, 8f, 8f, wood);
        for (int i = 0; i < 4; i++) {
            double a = i * Math.PI / 2 + Math.PI / 4;
            g.line(17, 18, (int) Math.round(17 + Math.cos(a) * 8), (int) Math.round(18 + Math.sin(a) * 8), goldDark);
        }
        // L'œil-bille au centre.
        g.ellipse(17f, 16f, 4f, 3f, white);
        g.ellipse(17f, 16f, 2f, 2f, iris);
        g.set(17, 16, pupil);
        g.set(16, 15, white);
        // Bouche dentue sous le moyeu.
        g.rect(13, 21, 21, 23, mouth);
        for (int x = 13; x <= 21; x += 2) g.set(x, 21, white);
        for (int x = 14; x <= 20; x += 2) g.set(x, 23, white);
        return g.outlined();
    }

    /**
     * Reine des Tables : croupière couronnée, longue chevelure pourpre, des
     * dés à la place des yeux, lèvres rouges, robe rouge à haut col d'or et
     * un sceptre surmonté d'un jeton.
     */
    static int[][] reine() {
        PixelCanvas g = new PixelCanvas(W, H);
        int skin = rgba("f0c8b0"), skinShade = rgba("c89a84"), hair = rgba("6a1e6a"), hairDark = rgba("441244");
        int gold = rgba("ffd54a"), goldDark = rgba("c4891e"), gem = rgba("2ec0e0"), dress = rgba("b0202c");
        int dressDark = rgba("7a1420"), die = rgba("f4efe4"), pip = rgba("1a0f0f"), lips = rgba("d8202c");
        int chip = rgba("d8202c"), chipEdge = rgba("f4efe4");

        // Chevelure qui tombe sur les épaules (derrière le visage).
        g.ellipse(17f, 17f, 11f, 11f, hair);
        g.rect(6, 17, 28, 34, hair);
        g.rect(22, 17, 28, 34, hairDark);
        // Robe rouge, haut col d'or.
        g.rect(4, 31, 29, 41, dress);
        g.rect(20, 31, 29, 41, dressDark);
        g.rect(11, 27, 23, 30, gold);
        for (int x = 11; x <= 23; x += 2) g.set(x, 29, goldDark);
        g.set(17, 33, gem);
        g.rect(16, 34, 18, 34, gold);
        // Visage, cou.
        g.rect(14, 24, 20, 27, skinShade);
        g.ellipseShaded(17f, 17f, 6.5f, 8f, skin, skinShade, 2f);
        // Yeux en dés : un 5 et un 3.
        g.rect(11, 14, 15, 18, die);
        g.set(11, 14, pip); g.set(15, 14, pip); g.set(13, 16, pip); g.set(11, 18, pip); g.set(15, 18, pip);
        g.rect(19, 14, 23, 18, die);
        g.set(19, 14, pip); g.set(21, 16, pip); g.set(23, 18, pip);
        // Lèvres rouges.
        g.rect(15, 21, 19, 22, lips);
        g.set(17, 21, rgba("8a1018"));
        // Couronne d'or à trois pointes, pierres bleues.
        g.rect(10, 7, 24, 9, gold);
        g.triangle(10, 7, 13, 7, 11, 2, gold);
        g.triangle(15, 7, 19, 7, 17, 0, gold);
        g.triangle(21, 7, 24, 7, 23, 2, gold);
        for (int x = 10; x <= 24; x++) g.set(x, 9, goldDark);
        g.set(17, 4, gem);
        g.set(12, 8, gem);
        g.set(22, 8, gem);
        // Sceptre au jeton, tenu à droite.
        g.line(29, 41, 30, 14, goldDark);
        g.ellipse(30f, 11f, 3f, 3f, chip);
        g.ellipse(30f, 11f, 1.5f, 1.5f, chipEdge);
        g.set(30, 11, chip);
        g.ellipse(29f, 33f, 2f, 1.8f, skin);
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Symboles (16 x 16)
    // -------------------------------------------------------------------------

    /** Dé pipé : un dé rouge incliné et ses points blancs. */
    static int[][] loadedDie() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int face = rgba("e83a3a"), side = rgba("a01e1e"), top = rgba("ff7a6a"), pip = rgba("ffffff");
        g.rect(2, 5, 10, 13, face);
        g.triangle(2, 5, 10, 5, 13, 2, top);
        g.triangle(2, 5, 5, 2, 13, 2, top);
        g.rect(11, 2, 13, 10, side);
        g.triangle(10, 5, 13, 2, 13, 5, side);
        g.triangle(10, 13, 13, 10, 10, 10, side);
        g.rect(10, 5, 10, 13, side);
        g.set(4, 7, pip); g.set(6, 9, pip); g.set(8, 11, pip);
        g.set(4, 11, pip); g.set(8, 7, pip);
        g.set(7, 3, pip);
        g.set(12, 6, pip); g.set(12, 9, pip);
        return g.outlined();
    }

    /** Intérêts : une grosse pièce d'or frappée d'un signe pour cent. */
    static int[][] interest() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int gold = rgba("ffd54a"), goldDark = rgba("c4891e"), shine = rgba("fff6c8"), mark = rgba("8a5a12");
        g.ellipseShaded(7.5f, 7.5f, 7f, 7f, gold, goldDark, 2f);
        g.ellipse(7.5f, 7.5f, 5.2f, 5.2f, goldDark);
        g.ellipse(7.5f, 7.5f, 4.6f, 4.6f, gold);
        g.line(10, 4, 5, 11, mark);
        g.rect(4, 4, 5, 5, mark);
        g.rect(10, 10, 11, 11, mark);
        g.set(4, 2, shine);
        g.set(3, 3, shine);
        return g.outlined();
    }

    /** Zéro : une petite roulette vue de dessus, la case verte du zéro en haut, et sa bille. */
    static int[][] zero() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int gold = rgba("ffd54a"), red = rgba("d8202c"), black = rgba("1e1a24"), green = rgba("2e9a4a");
        int wood = rgba("7a4a24"), ball = rgba("ffffff");
        g.ellipse(7.5f, 7.5f, 7.5f, 7.5f, gold);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                double dx = x - 7.5, dy = y - 7.5, d = Math.hypot(dx, dy);
                if (d > 6.6 || d < 3.6) continue;
                int pocket = (int) Math.floor((Math.atan2(dy, dx) + Math.PI * 2.5 + Math.PI / 12) / (2 * Math.PI) * 12) % 12;
                g.set(x, y, pocket == 0 ? green : pocket % 2 == 0 ? red : black);
            }
        }
        g.ellipse(7.5f, 7.5f, 3f, 3f, wood);
        g.ellipse(7.5f, 7.5f, 1f, 1f, gold);
        g.set(8, 2, ball);
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Illustration du chapitre 2
    // -------------------------------------------------------------------------

    /**
     * Chapitre 2, « Les Tables Sacrées » : une cité en ruine sous la nuit ; au
     * centre, une immense roulette dorée sert de place publique, et des éclats
     * de la comète brillent parmi les jetons.
     */
    static int[][] tables() {
        int w = EnemyPortraits.ART_WIDTH, h = EnemyPortraits.ART_HEIGHT;
        PixelCanvas g = new PixelCanvas(w, h);
        int[] sky = {rgba("0a0820"), rgba("120c2c"), rgba("1c1238"), rgba("2a1640"), rgba("3a1a40")};
        for (int y = 0; y < h; y++) {
            int band = Math.min(sky.length - 1, y * sky.length / (h - 20));
            for (int x = 0; x < w; x++) g.set(x, y, sky[band]);
        }
        Random random = new Random(11);
        for (int i = 0; i < 50; i++) g.set(random.nextInt(w), random.nextInt(30), rgba("c8d0ff"));
        // Lune pâle.
        g.ellipse(104f, 12f, 6f, 6f, rgba("f0e8c8"));
        g.ellipse(106f, 11f, 5f, 5f, sky[0]);
        // Silhouettes de la cité en ruine : tours brisées, dômes, fenêtres allumées.
        int ruin = rgba("1a1426"), ruinLight = rgba("2a2236"), window = rgba("ffb43a");
        int[][] towers = {{0, 30, 10}, {9, 38, 8}, {16, 24, 7}, {24, 34, 9}, {36, 42, 6}, {86, 40, 7},
            {94, 28, 9}, {104, 36, 8}, {113, 22, 7}, {121, 33, 7}};
        for (int[] t : towers) {
            g.rect(t[0], t[1], t[0] + t[2], 58, ruin);
            g.rect(t[0], t[1], t[0], 58, ruinLight);
            // Sommet brisé.
            g.triangle(t[0], t[1], t[0] + t[2], t[1], t[0] + t[2] / 3, t[1] - 4, ruin);
            for (int y = t[1] + 4; y < 56; y += 6) {
                if (random.nextInt(3) == 0) g.set(t[0] + 2 + random.nextInt(Math.max(1, t[2] - 3)), y, window);
            }
        }
        g.ellipse(70f, 44f, 9f, 7f, ruin);
        g.rect(61, 44, 79, 58, ruin);
        g.ellipse(52f, 48f, 6f, 5f, ruin);
        g.rect(46, 48, 58, 58, ruin);
        // Sol de la place.
        g.rect(0, 58, w - 1, h - 1, rgba("1e1a2a"));
        // La grande roulette, en perspective : jante d'or, cases rouges et noires, moyeu.
        float cx = 64f, cy = 66f, rx = 46f, ry = 12f;
        g.ellipse(cx, cy + 2, rx + 2, ry + 2, rgba("5a3a14"));
        g.ellipse(cx, cy, rx + 2, ry + 1.5f, rgba("c4891e"));
        g.ellipse(cx, cy, rx, ry, rgba("ffd54a"));
        for (int y = (int) (cy - ry); y <= cy + ry; y++) {
            for (int x = (int) (cx - rx); x <= cx + rx; x++) {
                double dx = (x - cx) / rx, dy = (y - cy) / ry, d = Math.hypot(dx, dy);
                if (d > 0.9 || d < 0.6) continue;
                int pocket = (int) Math.floor((Math.atan2(dy, dx) + Math.PI) / (2 * Math.PI) * 30);
                g.set(x, y, pocket == 0 ? rgba("2e9a4a") : pocket % 2 == 0 ? rgba("d8202c") : rgba("1e1a24"));
            }
        }
        g.ellipse(cx, cy, rx * 0.58f, ry * 0.58f, rgba("7a4a24"));
        g.ellipse(cx, cy, rx * 0.2f, ry * 0.25f, rgba("ffd54a"));
        g.rect(63, 55, 65, 66, rgba("c4891e"));
        g.ellipse(64f, 54f, 2f, 2f, rgba("ffd54a"));
        g.ellipse(cx + 28, cy - 6, 1.5f, 1.2f, rgba("ffffff"));
        // Éclats de la comète qui brillent parmi les jetons, autour de la roue.
        int[][] chips = {{14, 72, 0}, {22, 76, 1}, {108, 73, 0}, {116, 77, 1}, {6, 77, 1}, {122, 70, 0}};
        for (int[] c : chips) {
            g.ellipse(c[0], c[1], 3f, 1.4f, c[2] == 0 ? rgba("d8202c") : rgba("2a2a3a"));
            g.ellipse(c[0], c[1], 1.4f, 0.6f, rgba("f4efe4"));
        }
        int[][] shards = {{30, 70}, {98, 69}, {18, 66}, {112, 64}, {64, 77}};
        for (int[] s : shards) {
            g.set(s[0], s[1], rgba("fff6c8"));
            g.set(s[0] - 1, s[1], rgba("ffd54a"));
            g.set(s[0] + 1, s[1], rgba("ffd54a"));
            g.set(s[0], s[1] - 1, rgba("ffd54a"));
            g.set(s[0], s[1] + 1, rgba("ffd54a"));
        }
        return g.pixels;
    }
}
