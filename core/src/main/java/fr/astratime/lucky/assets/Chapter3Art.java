package fr.astratime.lucky.assets;

import java.util.Map;
import java.util.Random;

import static fr.astratime.lucky.assets.PixelCanvas.rgba;

/**
 * Dessins en pixel art du chapitre 3 de la Tour des épreuves, « Le Dernier
 * Tirage » : ses ennemis (en buste, à la taille du croupier), leurs symboles
 * et l'illustration du chapitre. Chaque méthode rend une grille de couleurs
 * RGBA8888 (0 = transparent), transformée en texture par {@link EnemyTextures}.
 */
final class Chapter3Art {

    private static final int W = EnemyPortraits.WIDTH, H = EnemyPortraits.HEIGHT;

    private Chapter3Art() {}

    /**
     * Gardienne du Cratère : l'armure du Gardien, forgée dans le bronze de la
     * comète. Visière aux yeux rouges, ronces sur le bouclier, et une flamme
     * de Rage au sommet du heaume.
     */
    static int[][] gardienne() {
        int[][] g = EnemyPortraits.gardien();
        Map<Integer, Integer> recolor = Map.of(
            rgba("7d8ca6"), rgba("b06a3a"),   // acier -> bronze
            rgba("4f5b73"), rgba("74401e"),
            rgba("b4c2d8"), rgba("e8a868"),
            rgba("2e3f63"), rgba("2a1a2e"),   // face du bouclier -> obsidienne
            rgba("223050"), rgba("1e1222"),
            rgba("ffe98a"), rgba("ff4a3a"));  // yeux rouges
        for (int[] row : g) for (int x = 0; x < row.length; x++) row[x] = recolor.getOrDefault(row[x], row[x]);
        int vine = rgba("3e8e3a"), thorn = rgba("e8f0b0"), red = rgba("d8202c"), orange = rgba("ff8a1e");
        // Ronces sur le bouclier.
        for (int x = 3; x <= 16; x++) {
            int y = 29 + (int) Math.round(Math.sin(x * 0.9) * 1.2);
            g[y][x] = vine;
            if (x % 3 == 0) g[y - 1][x] = thorn;
        }
        // Croc de la Sangsue en pendentif, sur le plastron.
        g[33][24] = rgba("f4efe4");
        g[34][24] = rgba("f4efe4");
        g[35][24] = rgba("c8c0b4");
        // Flamme de Rage à la place de la pièce, au sommet du heaume.
        g[0][17] = orange;
        g[1][16] = red;
        g[1][17] = orange;
        g[1][18] = red;
        g[2][16] = red;
        g[2][17] = rgba("ffd54a");
        g[2][18] = red;
        return g;
    }

    /**
     * Le Miroir : un grand miroir ovale au cadre d'or ouvragé, fêlé, où se
     * dresse une silhouette sans visage d'argent poli.
     */
    static int[][] miroir() {
        PixelCanvas g = new PixelCanvas(W, H);
        int gold = rgba("ffd54a"), goldDark = rgba("c4891e"), glass = rgba("8ab0c8"), glassDark = rgba("5a7a96");
        int shine = rgba("e8f4ff"), figure = rgba("c8d0dc"), figureDark = rgba("8a96a8"), crack = rgba("f4f8ff");
        // Pied du miroir.
        g.rect(12, 37, 22, 39, goldDark);
        g.rect(9, 40, 25, 41, gold);
        // Cadre ovale ouvragé.
        g.ellipse(17f, 19f, 14f, 18f, goldDark);
        g.ellipse(17f, 19f, 13f, 17f, gold);
        for (int i = 0; i < 16; i++) {
            double a = i * Math.PI * 2 / 16;
            g.set((int) Math.round(17 + Math.cos(a) * 13.5), (int) Math.round(19 + Math.sin(a) * 17.5), goldDark);
        }
        g.ellipse(17f, 1.5f, 3f, 1.5f, gold);
        g.set(17, 1, rgba("d8202c"));
        // La glace, plus sombre à droite.
        g.ellipseShaded(17f, 19f, 11f, 15f, glass, glassDark, 3f);
        // La silhouette sans visage.
        g.ellipseShaded(17f, 14f, 4f, 5f, figure, figureDark, 1f);
        g.rect(15, 18, 19, 20, figureDark);
        g.ellipseShaded(17f, 27f, 8f, 6f, figure, figureDark, 2f);
        g.rect(9, 27, 25, 33, figure);
        g.rect(19, 27, 25, 33, figureDark);
        // Reflets et fêlure.
        g.line(9, 9, 12, 5, shine);
        g.line(10, 12, 11, 10, shine);
        g.line(22, 6, 19, 13, crack);
        g.line(19, 13, 23, 18, crack);
        g.line(23, 18, 21, 25, crack);
        g.line(19, 13, 15, 16, crack);
        return g.outlined();
    }

    /**
     * L'Horloger : une tête en cadran d'horloge (aiguilles sur midi moins cinq),
     * lunettes-loupes de cuivre, redingote brune et un sablier dans la main.
     */
    static int[][] horloger() {
        PixelCanvas g = new PixelCanvas(W, H);
        int face = rgba("f4efe4"), faceShade = rgba("d0c8b4"), brass = rgba("d8a040"), brassDark = rgba("8a5a1e");
        int hand = rgba("1a1018"), coat = rgba("5a3a24"), coatDark = rgba("3a2414"), vest = rgba("8a2a2a");
        int lens = rgba("8ad0ff"), glass = rgba("c8e6f0"), sand = rgba("ffd54a"), wood = rgba("7a4a24");

        // Redingote et gilet bordeaux, chaîne de montre.
        g.rect(4, 29, 29, 41, coat);
        g.rect(20, 29, 29, 41, coatDark);
        g.ellipseShaded(7f, 31f, 4.5f, 3.5f, coat, coatDark, 3f);
        g.ellipseShaded(27f, 31f, 4.5f, 3.5f, coatDark, coatDark, 0f);
        g.rect(13, 29, 21, 41, vest);
        for (int x = 13; x <= 18; x++) g.set(x, 34 + (x == 15 || x == 16 ? 1 : 0), brass);
        // Cou en ressort.
        for (int y = 25; y <= 28; y++) g.rect(15, y, 19, y, y % 2 == 0 ? brass : brassDark);
        // Tête en cadran : boîtier de laiton, chiffres en points, aiguilles.
        g.ellipse(17f, 14f, 11f, 11f, brassDark);
        g.ellipse(17f, 14f, 10f, 10f, brass);
        g.ellipseShaded(17f, 14f, 8f, 8f, face, faceShade, 3f);
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI / 6;
            g.set((int) Math.round(17 + Math.sin(a) * 6.5), (int) Math.round(14 - Math.cos(a) * 6.5), hand);
        }
        g.line(17, 14, 17, 8, hand);
        g.line(17, 14, 15, 9, hand);
        g.set(17, 14, rgba("d8202c"));
        // Remontoir au sommet.
        g.rect(16, 1, 18, 3, brass);
        g.rect(15, 0, 19, 0, brassDark);
        // Lunettes-loupes de cuivre sur le côté du cadran.
        g.ellipse(26f, 12f, 3f, 3f, brassDark);
        g.ellipse(26f, 12f, 2f, 2f, lens);
        g.set(25, 11, rgba("ffffff"));
        // Sablier dans la main gauche.
        g.rect(3, 22, 9, 22, wood);
        g.rect(3, 31, 9, 31, wood);
        g.triangle(4, 23, 8, 23, 6, 26, glass);
        g.triangle(4, 30, 8, 30, 6, 27, glass);
        g.triangle(5, 23, 7, 23, 6, 25, sand);
        g.rect(5, 29, 7, 30, sand);
        g.set(6, 27, sand);
        g.set(6, 28, sand);
        g.ellipse(6f, 33f, 2.5f, 1.5f, rgba("e8b48a"));
        return g.outlined();
    }

    /**
     * Le Joueur Fou : tignasse en bataille, yeux exorbités de tailles
     * différentes, sourire immense, cravate dénouée, et des piles de jetons
     * qu'il pousse devant lui.
     */
    static int[][] fou() {
        PixelCanvas g = new PixelCanvas(W, H);
        int skin = rgba("f0c098"), skinShade = rgba("c89070"), hair = rgba("e8742a"), hairDark = rgba("a8481a");
        int shirt = rgba("ece6da"), shirtShade = rgba("c8c0b4"), tie = rgba("2a5ac8"), eye = rgba("ffffff");
        int pupil = rgba("1a1018"), mouth = rgba("3a0c14"), teeth = rgba("f4efe4"), sweat = rgba("8ad0ff");
        int red = rgba("d8202c"), black = rgba("2a2a3a"), green = rgba("2e9a4a"), edge = rgba("f4efe4");

        // Chemise froissée, cravate dénouée.
        g.rect(4, 29, 29, 41, shirt);
        g.rect(20, 29, 29, 41, shirtShade);
        g.ellipseShaded(7f, 31f, 4.5f, 3.5f, shirt, shirtShade, 3f);
        g.ellipseShaded(27f, 31f, 4.5f, 3.5f, shirtShade, shirtShade, 0f);
        g.line(15, 28, 18, 36, tie);
        g.line(16, 28, 19, 36, tie);
        g.line(18, 28, 14, 33, tie);
        // Cou et visage.
        g.rect(14, 24, 20, 28, skinShade);
        g.ellipseShaded(17f, 17f, 7f, 8f, skin, skinShade, 2f);
        // Tignasse en bataille.
        int[][] spikes = {{8, 12, 6, 3}, {11, 9, 10, 1}, {15, 8, 15, 0}, {19, 8, 21, 0}, {22, 9, 26, 2}, {24, 12, 29, 7}};
        g.ellipse(17f, 10f, 8f, 4f, hair);
        for (int[] s : spikes) g.triangle(s[0], s[1], s[0] + 4, s[1], s[2], s[3], hair);
        g.line(12, 10, 22, 9, hairDark);
        // Yeux exorbités, l'un plus grand que l'autre.
        g.ellipse(13f, 15f, 3f, 3f, eye);
        g.ellipse(21.5f, 15.5f, 2f, 2f, eye);
        g.rect(13, 15, 14, 16, pupil);
        g.set(21, 16, pupil);
        // Sourire immense plein de dents.
        g.ellipse(17f, 21.5f, 5.5f, 2.5f, mouth);
        for (int x = 12; x <= 22; x += 2) g.set(x, 20, teeth);
        for (int x = 13; x <= 21; x += 2) g.set(x, 23, teeth);
        g.set(25, 13, sweat);
        g.set(25, 14, sweat);
        // Bord de la table, et les piles de jetons qu'il pousse dessus.
        g.rect(0, 39, W - 1, 41, rgba("1f6b3a"));
        g.rect(0, 39, W - 1, 39, rgba("2e8a4e"));
        int[][] stacks = {{5, 4, 0}, {12, 6, 1}, {21, 5, 2}, {28, 3, 0}};
        for (int[] s : stacks) {
            int color = s[2] == 0 ? red : s[2] == 1 ? black : green;
            for (int i = 0; i < s[1]; i++) {
                int y = 38 - i * 2;
                g.rect(s[0] - 3, y - 1, s[0] + 3, y, color);
                g.rect(s[0] - 3, y, s[0] + 3, y, PixelCanvas.OUTLINE);
                g.set(s[0] - 1, y - 1, edge);
                g.set(s[0] + 1, y - 1, edge);
            }
        }
        g.ellipse(9f, 33f, 2f, 1.8f, skin);
        g.ellipse(25f, 34f, 2f, 1.8f, skinShade);
        return g.outlined();
    }

    /**
     * Éclat Originel, le boss final : un soleil d'or aux rayons acérés, dont le
     * cœur est la fenêtre d'une machine à sous (trois 7), et trois petits
     * rouleaux en orbite autour de lui.
     */
    static int[][] eclat() {
        PixelCanvas g = new PixelCanvas(W, H);
        int gold = rgba("ffd54a"), goldDark = rgba("c4801e"), orange = rgba("ff8a1e"), core = rgba("fff6c8");
        int window = rgba("1a0f0f"), seven = rgba("d8202c"), reel = rgba("f4efe4"), reelShade = rgba("c8c0b4");
        // Rayons acérés.
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI / 6;
            double side = Math.PI / 24;
            int tipX = (int) Math.round(17 + Math.cos(a) * 16), tipY = (int) Math.round(20 + Math.sin(a) * 16);
            int lx = (int) Math.round(17 + Math.cos(a - side) * 9), ly = (int) Math.round(20 + Math.sin(a - side) * 9);
            int rx = (int) Math.round(17 + Math.cos(a + side) * 9), ry = (int) Math.round(20 + Math.sin(a + side) * 9);
            g.triangle(lx, ly, rx, ry, tipX, tipY, i % 2 == 0 ? orange : gold);
        }
        // Le soleil, ombré à droite.
        g.ellipseShaded(17f, 20f, 10f, 10f, gold, goldDark, 4f);
        g.ellipse(13f, 15f, 2.5f, 1.8f, core);
        // Sa fenêtre de machine à sous : trois 7 rouges.
        g.rect(9, 17, 25, 23, goldDark);
        g.rect(10, 18, 24, 22, window);
        for (int i = 0; i < 3; i++) {
            int x0 = 11 + i * 5;
            g.rect(x0, 18, x0 + 3, 22, reel);
            g.rect(x0, 19, x0 + 2, 19, seven);
            g.line(x0 + 2, 19, x0 + 1, 22, seven);
        }
        // Trois petits rouleaux en orbite.
        int[][] orbit = {{3, 6}, {30, 9}, {5, 36}};
        for (int[] o : orbit) {
            g.rect(o[0] - 2, o[1] - 3, o[0] + 2, o[1] + 3, reel);
            g.rect(o[0] + 1, o[1] - 3, o[0] + 2, o[1] + 3, reelShade);
            g.set(o[0], o[1], seven);
            g.set(o[0] - 1, o[1] - 1, seven);
            g.set(o[0], o[1] - 1, seven);
        }
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Symboles (16 x 16)
    // -------------------------------------------------------------------------

    /** Reflet : un miroir à main dressé, cadre doré ouvragé, glace bleutée barrée de reflets. */
    static int[][] mirror() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int gold = rgba("ffd54a"), goldDark = rgba("c4891e"), glass = rgba("8ab0c8"), glassDark = rgba("5a7a96");
        int shine = rgba("e8f4ff"), gem = rgba("d8202c");
        g.rect(7, 11, 8, 15, goldDark);
        g.rect(7, 11, 7, 15, gold);
        g.rect(6, 15, 9, 15, gold);
        g.ellipse(7.5f, 5.5f, 5.5f, 5.5f, goldDark);
        g.ellipse(7.5f, 5.5f, 4.6f, 4.6f, gold);
        g.ellipseShaded(7.5f, 5.5f, 3.6f, 3.6f, glass, glassDark, 1f);
        g.line(5, 6, 8, 3, shine);
        g.line(6, 8, 9, 5, shine);
        g.set(7, 0, gem);
        g.set(8, 0, gem);
        return g.outlined();
    }

    /** Sablier : deux bulbes de verre dans un cadre de bois, le sable d'or qui coule. */
    static int[][] hourglass() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int wood = rgba("7a4a24"), woodLight = rgba("a8703a"), glass = rgba("c8e6f0"), sand = rgba("ffd54a");
        g.rect(2, 0, 13, 1, wood);
        g.rect(2, 14, 13, 15, wood);
        g.rect(2, 2, 2, 13, woodLight);
        g.rect(13, 2, 13, 13, wood);
        g.triangle(4, 2, 11, 2, 8, 7, glass);
        g.triangle(4, 13, 11, 13, 8, 8, glass);
        g.triangle(6, 3, 9, 3, 8, 6, sand);
        g.rect(7, 7, 8, 10, sand);
        g.triangle(5, 13, 10, 13, 8, 11, sand);
        return g.outlined();
    }

    /** Tapis : deux piles de jetons poussées en avant, et un jeton posé à plat sur le dessus. */
    static int[][] allIn() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int red = rgba("d8202c"), redDark = rgba("8a1018"), black = rgba("3a3a4e"), blackDark = rgba("1a1a26");
        int edge = rgba("f4efe4"), gold = rgba("ffd54a");
        int[][] stacks = {{4, 5, 0}, {11, 7, 1}};
        for (int[] s : stacks) {
            int color = s[1] == 5 ? red : black, dark = s[1] == 5 ? redDark : blackDark;
            for (int i = 0; i < s[1]; i++) {
                int y = 14 - i * 2;
                g.ellipse(s[0], y, 3.5f, 1.5f, i % 2 == 0 ? color : dark);
                g.set(s[0] - 2, y, edge);
                g.set(s[0] + 2, y, edge);
            }
            int top = 14 - (s[1] - 1) * 2;
            g.ellipse(s[0], top - 0.5f, 3.5f, 1.5f, color);
            g.ellipse(s[0], top - 0.5f, 1.5f, 0.6f, edge);
        }
        g.ellipse(13f, 2f, 2.2f, 2f, gold);
        g.set(13, 2, rgba("fff6c8"));
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Illustration du chapitre 3
    // -------------------------------------------------------------------------

    /**
     * Chapitre 3, « Le Dernier Tirage » : le cratère de la comète, vu d'en
     * haut ; au fond, un cœur doré pulse comme une machine à sous, et des
     * rouleaux géants flottent autour.
     */
    static int[][] crater() {
        int w = EnemyPortraits.ART_WIDTH, h = EnemyPortraits.ART_HEIGHT;
        PixelCanvas g = new PixelCanvas(w, h);
        // Terre brûlée tout autour.
        g.rect(0, 0, w - 1, h - 1, rgba("2a1a14"));
        Random random = new Random(5);
        for (int i = 0; i < 160; i++) g.set(random.nextInt(w), random.nextInt(h), rgba("3a261c"));
        // Anneaux du cratère, du bord au fond, et lueur dorée qui monte.
        float cx = 64f, cy = 42f;
        int[] rings = {rgba("4a3020"), rgba("3a2216"), rgba("2e1a12"), rgba("24140e"), rgba("3a2410"), rgba("6a4416"),
            rgba("a8701e"), rgba("e8a830")};
        for (int i = 0; i < rings.length; i++) {
            float r = 54f - i * 5.5f;
            g.ellipse(cx, cy, r, r * 0.62f, rings[i]);
        }
        // Rayons de lumière qui sortent du fond.
        int ray = rgba("6a4818");
        for (int i = 0; i < 10; i++) {
            double a = i * Math.PI / 5 + 0.2;
            g.line((int) cx, (int) cy, (int) Math.round(cx + Math.cos(a) * 60), (int) Math.round(cy + Math.sin(a) * 38), ray);
        }
        // Le cœur doré, une machine à sous ronde : fenêtre de trois rouleaux à 7.
        g.ellipse(cx, cy, 14f, 11f, rgba("ffb43a"));
        g.ellipse(cx, cy, 12f, 9.5f, rgba("ffd54a"));
        g.ellipse(cx - 4, cy - 4, 3f, 2f, rgba("fff6c8"));
        g.rect(53, 38, 75, 46, rgba("c4801e"));
        g.rect(54, 39, 74, 45, rgba("1a0f0f"));
        for (int i = 0; i < 3; i++) {
            int x0 = 55 + i * 7;
            g.rect(x0, 39, x0 + 5, 45, rgba("f4efe4"));
            g.rect(x0 + 1, 40, x0 + 4, 40, rgba("d8202c"));
            g.line(x0 + 4, 40, x0 + 2, 44, rgba("d8202c"));
        }
        // Rouleaux géants qui flottent autour, inclinés.
        int[][] reels = {{16, 14, 0}, {108, 18, 1}, {22, 64, 1}, {104, 64, 0}};
        for (int[] r : reels) {
            int x0 = r[0] - 7, y0 = r[1] - 8;
            g.rect(x0 - 1, y0 - 1, x0 + 14, y0 + 16, rgba("1a0f0f"));
            g.rect(x0, y0, x0 + 13, y0 + 15, rgba("f4efe4"));
            g.rect(x0 + 9, y0, x0 + 13, y0 + 15, rgba("c8c0b4"));
            g.rect(x0, y0 + 4, x0 + 13, y0 + 4, rgba("8a8a96"));
            g.rect(x0, y0 + 11, x0 + 13, y0 + 11, rgba("8a8a96"));
            if (r[2] == 0) {
                g.rect(x0 + 3, y0 + 6, x0 + 9, y0 + 6, rgba("d8202c"));
                g.line(x0 + 9, y0 + 6, x0 + 6, y0 + 10, rgba("d8202c"));
            } else {
                g.ellipse(x0 + 5, y0 + 8, 1.8f, 1.8f, rgba("d8202c"));
                g.ellipse(x0 + 8, y0 + 8, 1.8f, 1.8f, rgba("d8202c"));
                g.line(x0 + 6, y0 + 6, x0 + 8, y0 + 5, rgba("3e8e3a"));
            }
        }
        // Étincelles.
        for (int i = 0; i < 18; i++) {
            int x = (int) (cx + random.nextGaussian() * 22), y = (int) (cy + random.nextGaussian() * 12);
            g.set(x, y, rgba("fff6c8"));
        }
        return g.pixels;
    }
}
