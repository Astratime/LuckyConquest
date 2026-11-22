package fr.astratime.lucky.assets;

import fr.astratime.lucky.entities.enemy.EnemyKind;

import java.util.Random;

import static fr.astratime.lucky.assets.PixelCanvas.rgba;

/**
 * Dessins en pixel art du chapitre « L'Ascension » de la Tour des épreuves (Tour à 10 chapitres, 2026-11-22) :
 * ses ennemis (en buste, à la taille du croupier), leurs symboles et l'illustration du chapitre. Chaque méthode rend
 * une grille de couleurs RGBA8888 (0 = transparent), transformée en texture par {@link EnemyTextures}.
 */
final class AscensionArt {

    private static final int W = EnemyPortraits.WIDTH, H = EnemyPortraits.HEIGHT;

    private static final int SKIN = rgba("f0c8a0"), SKIN_SHADE = rgba("c89a72");
    private static final int EYE  = rgba("1a1018"), MOUTH = rgba("7a2a20"), WHITE = rgba("f6f0e2"), WHITE_SHADE = rgba("c8c0b4");
    private static final int GOLD = rgba("ffd54a"), GOLD_DARK = rgba("c4891e"), GOLD_LIGHT = rgba("fff6c8");
    private static final int STONE = rgba("8a8a92"), STONE_DARK = rgba("5a5a64"), STONE_LIGHT = rgba("b8b8c0");
    private static final int BLUE = rgba("2a4a9a"), BLUE_DARK = rgba("18306a"), BLUE_LIGHT = rgba("7aa8ff");

    private AscensionArt() {}

    /** @return le portrait de {@code kind}, s'il est de l'Ascension ({@code null} sinon). */
    static int[][] of(EnemyKind kind) {
        return switch (kind) {
            case LIFTIER_TOUR -> liftier();
            case ECHO         -> echo();
            case VERTIGE      -> vertige();
            case GARGOUILLE   -> gargouille();
            case ARCHITECTE   -> architecte();
            default           -> null;
        };
    }

    // -------------------------------------------------------------------------
    // Les ennemis
    // -------------------------------------------------------------------------

    /**
     * Le Liftier de la Tour : groom au calot rouge galonné d'or, veste rouge à boutons dorés, gants blancs ; derrière
     * lui, le cadran de l'ascenseur, son aiguille pointée vers le haut.
     */
    static int[][] liftier() {
        PixelCanvas g = new PixelCanvas(W, H);
        int coat = rgba("b81e2a"), coatDark = rgba("7a1018"), brass = rgba("e8b84a"), dial = rgba("f6ecd0");
        // Le voyant d'étages de l'ascenseur, en haut à gauche : une plaque de laiton, la flèche de montée allumée.
        g.rect(1, 2, 8, 9, brass);
        g.rect(2, 3, 7, 8, rgba("3a2a1a"));
        g.triangle(4, 3, 2, 6, 7, 6, rgba("ff5a3a"));
        g.rect(4, 6, 5, 8, rgba("ff5a3a"));
        g.set(4, 4, dial);
        // La veste à deux rangées de boutons.
        g.rect(4, 29, 29, 41, coat);
        g.rect(20, 29, 29, 41, coatDark);
        g.ellipseShaded(7f, 31f, 4.5f, 3.5f, coat, coatDark, 3f);
        g.ellipseShaded(27f, 31f, 4.5f, 3.5f, coatDark, coatDark, 0f);
        g.rect(4, 29, 9, 29, GOLD);
        g.rect(25, 29, 29, 29, GOLD_DARK);
        for (int y = 32; y <= 40; y += 3) { g.set(14, y, GOLD); g.set(20, y, GOLD); }
        g.rect(15, 28, 19, 30, WHITE);
        // Visage jeune, joues roses.
        g.rect(14, 24, 20, 28, SKIN_SHADE);
        g.ellipseShaded(17f, 18f, 6.5f, 7f, SKIN, SKIN_SHADE, 2f);
        g.rect(13, 17, 14, 18, EYE);
        g.rect(20, 17, 21, 18, EYE);
        g.set(12, 20, rgba("f09a8a"));
        g.set(22, 20, rgba("f09a8a"));
        g.line(15, 22, 19, 22, MOUTH);
        g.set(19, 21, MOUTH);
        // Le calot posé de travers, galon et pompon d'or.
        g.rect(11, 9, 22, 12, coat);
        g.rect(19, 9, 22, 12, coatDark);
        g.rect(11, 12, 22, 12, GOLD);
        g.set(21, 8, GOLD);
        g.set(22, 8, GOLD_LIGHT);
        g.rect(11, 13, 23, 13, rgba("2a1a14"));
        // Le gant blanc sur le levier de l'ascenseur.
        g.rect(28, 21, 29, 33, brass);
        g.ellipse(28.5f, 20f, 1.8f, 1.8f, RED());
        g.ellipse(27f, 30f, 2.5f, 2f, WHITE);
        g.set(26, 31, WHITE_SHADE);
        return g.outlined();
    }

    /**
     * L'Écho : une silhouette pâle, bouche ouverte, entourée d'ondes ; deux reflets d'elle-même, de plus en plus
     * effacés, la suivent de côté.
     */
    static int[][] echo() {
        PixelCanvas g = new PixelCanvas(W, H);
        int pale = rgba("c8d8f0"), paleShade = rgba("8aa0c8"), ghost = rgba("5a6a9a"), ghostDark = rgba("3a466e");
        int wave = rgba("9ae8ff");
        // Les reflets, derrière.
        for (int k = 2; k >= 1; k--) {
            int dx = k * 4, color = k == 2 ? ghostDark : ghost;
            g.ellipse(17f + dx, 16f, 6.5f, 8f, color);
            g.rect(7 + dx, 29, 27 + dx, 41, color);
        }
        // Le corps, comme drapé.
        g.rect(5, 29, 27, 41, pale);
        g.rect(19, 29, 27, 41, paleShade);
        g.ellipseShaded(8f, 31f, 4.5f, 3.5f, pale, paleShade, 3f);
        g.ellipseShaded(25f, 31f, 4.5f, 3.5f, paleShade, paleShade, 0f);
        for (int y = 32; y <= 40; y += 2) g.set(16, y, paleShade);
        // La tête : yeux vides, bouche ronde.
        g.rect(13, 24, 19, 28, paleShade);
        g.ellipseShaded(16f, 16f, 6.5f, 8f, pale, paleShade, 2f);
        g.rect(12, 14, 14, 16, ghostDark);
        g.rect(18, 14, 20, 16, ghostDark);
        g.set(13, 15, wave);
        g.set(19, 15, wave);
        g.ellipse(16f, 21.5f, 1.8f, 2.2f, ghostDark);
        // Les ondes qui sortent de sa bouche.
        for (int r = 0; r < 3; r++) {
            int x = 3 - r * 0, y0 = 18 - r * 3, y1 = 25 + r * 3;
            g.line(x + r, y0, x, (y0 + y1) / 2, wave);
            g.line(x, (y0 + y1) / 2, x + r, y1, wave);
        }
        g.line(6, 19, 5, 21, wave);
        g.line(5, 22, 6, 24, wave);
        return g.outlined();
    }

    /**
     * Le Vertige : un homme maigre penché en avant, au haut-de-forme qui glisse ; ses yeux sont deux spirales, une
     * spirale verte et mauve tourne derrière lui.
     */
    static int[][] vertige() {
        PixelCanvas g = new PixelCanvas(W, H);
        int purple = rgba("6a2a8a"), purpleDark = rgba("441a5e"), green = rgba("6ad88a"), spiral = rgba("b88aff");
        int skin = rgba("d8e8c8"), skinShade = rgba("a8c09a");
        // La spirale derrière lui.
        for (int i = 0; i < 260; i++) {
            double a = i * 0.12, r = 1 + i * 0.06;
            int x = 17 + (int) Math.round(r * Math.cos(a)), y = 18 + (int) Math.round(r * Math.sin(a) * 0.9);
            if (y < 28) g.set(x, y, i % 40 < 20 ? spiral : green);
        }
        // Un corps étroit, penché.
        g.rect(8, 30, 26, 41, purple);
        g.rect(19, 30, 26, 41, purpleDark);
        g.ellipseShaded(10f, 31f, 4f, 3f, purple, purpleDark, 3f);
        g.ellipseShaded(25f, 31f, 4f, 3f, purpleDark, purpleDark, 0f);
        g.triangle(14, 29, 20, 29, 17, 36, WHITE);
        g.rect(16, 30, 18, 33, green);
        // Le visage long et pâle, les yeux en spirale.
        g.rect(15, 25, 19, 29, skinShade);
        g.ellipseShaded(17f, 18f, 5.5f, 8f, skin, skinShade, 2f);
        for (int side = 0; side < 2; side++) {
            int cx = side == 0 ? 14 : 20;
            g.rect(cx - 1, 15, cx + 1, 17, WHITE);
            g.set(cx, 16, EYE);
            g.set(cx + 1, 15, EYE);
            g.set(cx - 1, 17, EYE);
        }
        g.line(15, 22, 19, 23, MOUTH);
        // Le haut-de-forme qui glisse de sa tête.
        g.rect(13, 4, 22, 9, rgba("1a1420"));
        g.rect(11, 9, 24, 10, rgba("1a1420"));
        g.rect(13, 7, 22, 8, green);
        for (int y = 4; y <= 10; y++) g.set(24 - (10 - y) / 3, y, 0);
        // Sa main tremblante tendue vers le vide.
        g.ellipse(4f, 34f, 2.2f, 2f, skin);
        g.line(2, 31, 1, 30, skin);
        g.line(5, 31, 6, 30, skin);
        return g.outlined();
    }

    /**
     * La Gargouille : bête de pierre accroupie, cornes, ailes de chauve-souris déployées, crocs, yeux jaunes ; une
     * fissure lui traverse le front.
     */
    static int[][] gargouille() {
        PixelCanvas g = new PixelCanvas(W, H);
        int eye = rgba("ffe04a"), moss = rgba("5a7a4a");
        // Les ailes, derrière.
        g.triangle(0, 12, 12, 26, 0, 34, STONE_DARK);
        g.triangle(33, 12, 21, 26, 33, 34, STONE_DARK);
        for (int k = 0; k < 3; k++) {
            g.line(0, 12, 3 + k * 3, 34, STONE);
            g.line(33, 12, 30 - k * 3, 34, STONE);
        }
        // Le corps accroupi, les pattes griffues devant.
        g.ellipseShaded(17f, 35f, 11f, 7f, STONE, STONE_DARK, 3f);
        g.rect(9, 37, 13, 41, STONE);
        g.rect(21, 37, 25, 41, STONE_DARK);
        for (int x = 9; x <= 13; x += 2) g.set(x, 41, STONE_LIGHT);
        for (int x = 21; x <= 25; x += 2) g.set(x, 41, STONE_LIGHT);
        g.set(15, 33, moss);
        g.set(16, 34, moss);
        // La tête, large, mâchoire en avant.
        g.ellipseShaded(17f, 19f, 8f, 7.5f, STONE, STONE_DARK, 2f);
        g.rect(11, 22, 23, 27, STONE);
        g.rect(19, 22, 23, 27, STONE_DARK);
        g.line(10, 15, 15, 17, STONE_DARK);
        g.line(24, 15, 19, 17, STONE_DARK);
        g.rect(12, 17, 14, 18, eye);
        g.rect(20, 17, 22, 18, eye);
        g.set(13, 17, GOLD_LIGHT);
        g.set(21, 17, GOLD_LIGHT);
        g.rect(13, 24, 21, 25, rgba("2a2228"));
        for (int x = 13; x <= 21; x += 2) g.set(x, 24, WHITE);
        g.set(13, 26, WHITE);
        g.set(21, 26, WHITE);
        // Les cornes.
        g.triangle(9, 14, 12, 13, 7, 5, STONE_LIGHT);
        g.triangle(25, 14, 22, 13, 27, 5, STONE);
        // La fissure du front.
        g.line(17, 12, 16, 14, STONE_DARK);
        g.line(16, 14, 18, 16, STONE_DARK);
        return g.outlined();
    }

    /**
     * L'Architecte : un vieillard à longue barbe blanche, en robe bleu nuit brodée d'un compas d'or, monocle à l'œil ;
     * il tient un compas ouvert et un plan roulé, une équerre d'or sur l'épaule.
     */
    static int[][] architecte() {
        PixelCanvas g = new PixelCanvas(W, H);
        int robe = rgba("1e2a5a"), robeDark = rgba("121a3a"), paper = rgba("8ab8ff"), paperDark = rgba("4a7ad0");
        // L'équerre d'or derrière son épaule gauche.
        g.rect(1, 14, 2, 30, GOLD);
        g.rect(1, 29, 10, 30, GOLD_DARK);
        // La robe et son col.
        g.rect(3, 29, 30, 41, robe);
        g.rect(20, 29, 30, 41, robeDark);
        g.ellipseShaded(7f, 31f, 4.5f, 3.5f, robe, robeDark, 3f);
        g.ellipseShaded(27f, 31f, 4.5f, 3.5f, robeDark, robeDark, 0f);
        g.rect(3, 29, 9, 29, GOLD_DARK);
        g.rect(24, 29, 30, 29, GOLD_DARK);
        // Le compas d'or brodé sur la poitrine.
        g.set(24, 33, GOLD);
        g.line(24, 33, 22, 38, GOLD);
        g.line(24, 33, 26, 38, GOLD);
        g.line(22, 36, 26, 36, GOLD_DARK);
        // Le visage ridé.
        g.rect(14, 23, 20, 27, SKIN_SHADE);
        g.ellipseShaded(17f, 15f, 6.5f, 7.5f, SKIN, SKIN_SHADE, 2f);
        g.line(12, 12, 15, 12, WHITE);
        g.line(19, 12, 22, 12, WHITE);
        g.rect(13, 14, 14, 15, EYE);
        g.rect(20, 14, 21, 15, rgba("ffd54a"));
        g.set(20, 14, GOLD_LIGHT);
        g.ellipse(20.5f, 14.5f, 2.2f, 2.2f, 0);                // le monocle : un cerceau d'or autour de l'œil
        for (int k = 0; k < 12; k++) {
            double a = k * Math.PI / 6;
            g.set(20 + (int) Math.round(2.2 * Math.cos(a)), 14 + (int) Math.round(2.2 * Math.sin(a)), GOLD);
        }
        g.rect(20, 14, 21, 15, GOLD);
        g.set(20, 14, GOLD_LIGHT);
        g.line(22, 16, 23, 22, GOLD_DARK);                     // la chaîne du monocle
        g.set(17, 17, SKIN_SHADE);
        // La longue barbe blanche.
        g.triangle(11, 19, 23, 19, 17, 34, WHITE);
        g.rect(12, 19, 22, 24, WHITE);
        g.line(17, 22, 17, 31, WHITE_SHADE);
        g.line(15, 21, 19, 21, MOUTH);
        // Les cheveux blancs sur les côtés, crâne dégarni.
        g.ellipse(10.5f, 14f, 2f, 3.5f, WHITE);
        g.ellipse(23.5f, 14f, 2f, 3.5f, WHITE_SHADE);
        // Le plan roulé sous le bras droit.
        g.rect(3, 33, 15, 38, paper);
        g.rect(3, 37, 15, 38, paperDark);
        g.ellipse(3f, 35.5f, 1.8f, 2.6f, rgba("d8e8ff"));
        g.ellipse(3f, 35.5f, 0.8f, 1.2f, paperDark);
        for (int x = 6; x <= 14; x += 3) g.line(x, 34, x, 36, WHITE);
        // Le compas ouvert, tenu dans la main gauche.
        g.ellipse(29f, 26f, 2.3f, 2f, SKIN);
        g.line(29, 24, 26, 14, STONE_LIGHT);
        g.line(29, 24, 32, 14, STONE_LIGHT);
        g.ellipse(29f, 23f, 1.2f, 1.2f, GOLD);
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Les symboles (16 x 16)
    // -------------------------------------------------------------------------

    /** Ascenseur : une cabine aux portes dorées entrouvertes, la flèche de montée allumée au-dessus. */
    static int[][] elevator() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int brass = rgba("e8b84a"), dark = rgba("3a2a1a"), light = rgba("fff0a0");
        g.rect(2, 4, 13, 15, brass);
        g.rect(3, 5, 12, 15, dark);
        g.rect(3, 5, 7, 15, GOLD_DARK);
        g.rect(9, 5, 12, 15, GOLD_DARK);
        g.rect(8, 5, 8, 15, light);
        g.line(5, 6, 5, 14, GOLD);
        g.line(10, 6, 10, 14, GOLD);
        g.triangle(8, 0, 5, 3, 11, 3, rgba("ff5a3a"));
        return g.outlined();
    }

    /** Écho : une bouche qui parle et trois ondes qui s'éloignent, de plus en plus pâles. */
    static int[][] echoSymbol() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int[] waves = {rgba("e8f8ff"), rgba("9ae8ff"), rgba("5aa8d8")};
        g.ellipse(3f, 8f, 2.5f, 3f, rgba("c8d8f0"));
        g.ellipse(3.5f, 8f, 1f, 1.5f, rgba("2a3050"));
        for (int r = 0; r < 3; r++) {
            int x = 7 + r * 3, half = 3 + r * 2;
            for (int dy = -half; dy <= half; dy++) {
                int dx = (int) Math.round(Math.sqrt(Math.max(0, half * half - dy * dy)) * 0.5);
                g.set(x + dx - half / 2, 8 + dy, waves[r]);
            }
        }
        return g.outlined();
    }

    /** Vertige : une spirale mauve et verte qui s'enroule vers son centre. */
    static int[][] vertigo() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int purple = rgba("b88aff"), green = rgba("6ad88a");
        g.ellipse(7.5f, 7.5f, 7.4f, 7.4f, rgba("2a1a3a"));
        for (int i = 0; i < 160; i++) {
            double a = i * 0.16, r = i * 0.045;
            g.set(8 + (int) Math.round(r * Math.cos(a)), 8 + (int) Math.round(r * Math.sin(a)), i < 80 ? green : purple);
        }
        return g.outlined();
    }

    /** Pétrification : un bloc de pierre taillé, fendu, une main de pierre qui en sort. */
    static int[][] stone() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.rect(1, 6, 14, 15, STONE);
        g.rect(10, 6, 14, 15, STONE_DARK);
        g.rect(1, 6, 14, 6, STONE_LIGHT);
        g.line(6, 6, 8, 10, STONE_DARK);
        g.line(8, 10, 6, 14, STONE_DARK);
        g.rect(4, 1, 7, 5, STONE_LIGHT);                       // la main pétrifiée qui dépasse
        for (int x = 4; x <= 7; x++) g.set(x, 0, x % 2 == 0 ? STONE : 0);
        g.set(8, 3, STONE);
        return g.outlined();
    }

    /** Plan : une feuille bleue de plan, une tour dessinée au trait blanc, un compas posé dessus. */
    static int[][] blueprint() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.rect(0, 2, 13, 15, BLUE);
        g.rect(11, 2, 13, 15, BLUE_DARK);
        g.rect(4, 6, 8, 14, 0);
        g.line(4, 14, 4, 6, BLUE_LIGHT);
        g.line(8, 14, 8, 6, BLUE_LIGHT);
        g.line(4, 6, 6, 3, BLUE_LIGHT);
        g.line(8, 6, 6, 3, BLUE_LIGHT);
        for (int y = 8; y <= 14; y += 3) g.line(4, y, 8, y, BLUE_LIGHT);
        g.rect(5, 7, 7, 7, BLUE);
        g.rect(5, 9, 7, 10, BLUE);
        g.rect(5, 12, 7, 13, BLUE);
        g.line(14, 1, 11, 8, GOLD);
        g.line(14, 1, 15, 8, GOLD);
        g.set(14, 1, GOLD_LIGHT);
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // L'illustration du chapitre (128 x 80)
    // -------------------------------------------------------------------------

    /**
     * L'Ascension : la Tour monte à travers les nuages ; un escalier de jetons s'enroule autour d'elle, ses fenêtres
     * s'allument étage par étage, et tout en haut la lueur de la Machine perce le ciel.
     */
    static int[][] ascension() {
        int w = EnemyPortraits.ART_WIDTH, h = EnemyPortraits.ART_HEIGHT;
        PixelCanvas g = new PixelCanvas(w, h);
        int[] sky = {rgba("0a0a24"), rgba("14123a"), rgba("221a4e"), rgba("3a2460"), rgba("5a3068"), rgba("8a4a6a"),
            rgba("c07a6a")};
        for (int y = 0; y < h; y++) {
            int band = Math.min(sky.length - 1, y * sky.length / h);
            for (int x = 0; x < w; x++) g.set(x, y, sky[band]);
        }
        Random random = new Random(19);
        for (int i = 0; i < 40; i++) g.set(random.nextInt(w), random.nextInt(34), rgba("e8e0ff"));
        // La lueur de la Machine, au sommet.
        g.ellipse(64f, 4f, 30f, 12f, rgba("5a3a3a"));
        g.ellipse(64f, 4f, 18f, 8f, rgba("a8643a"));
        g.ellipse(64f, 4f, 8f, 4f, rgba("ffc05a"));
        // La Tour : de pierre, de plus en plus étroite vers le haut.
        int wall = rgba("6a6478"), wallDark = rgba("46425a"), wallLight = rgba("8a84a0"), window = rgba("ffd27a");
        for (int y = 8; y < h; y++) {
            int half = 6 + (y - 8) / 5;
            for (int x = 64 - half; x <= 64 + half; x++) g.set(x, y, x > 64 + half / 3 ? wallDark : x < 64 - half / 2 ? wallLight : wall);
        }
        g.triangle(57, 9, 71, 9, 64, 1, rgba("3a2a3a"));
        g.set(64, 0, GOLD);
        for (int y = 14; y < h - 6; y += 7) {                           // les fenêtres, allumées étage par étage
            int half = 6 + (y - 8) / 5;
            for (int x = 64 - half + 3; x <= 64 + half - 3; x += 5) {
                g.rect(x, y, x + 1, y + 2, y < 40 ? window : rgba("8a6a4a"));
            }
        }
        // L'escalier de jetons qui s'enroule autour de la Tour.
        int[] chips = {rgba("c8283c"), rgba("2a5ad8"), rgba("1f9a4a"), rgba("e8b020")};
        for (int i = 0; i < 90; i++) {
            double t = i / 90.0;
            int y = (int) Math.round(h - 4 - t * (h - 16));
            int half = 6 + (y - 8) / 5 + 3;
            double a = t * Math.PI * 7;
            if (Math.sin(a) < -0.2) continue;                           // la partie cachée derrière la Tour
            int x = 64 + (int) Math.round(Math.cos(a) * half);
            g.rect(x - 1, y, x + 1, y, chips[i % chips.length]);
            g.set(x, y - 1, WHITE);
        }
        // Les nuages, au pied de la Tour et en écharpe.
        int cloud = rgba("e8d8e0"), cloudShade = rgba("b8a0b8");
        for (int k = 0; k < 10; k++) {
            float cx = 6 + k * 13, cy = 70 + (k % 3) * 3;
            g.ellipse(cx, cy, 11f, 5f, k % 2 == 0 ? cloud : cloudShade);
        }
        for (int k = 0; k < 4; k++) g.ellipse(20 + k * 30f, 44 + (k % 2) * 4f, 9f, 2.5f, cloudShade);
        return g.pixels;
    }

    private static int RED() { return rgba("d8202c"); }
}
