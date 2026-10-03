package fr.astratime.lucky.assets;

import java.util.Random;

import static fr.astratime.lucky.assets.PixelCanvas.rgba;

/**
 * Dessins en pixel art des ennemis de la Tour des épreuves (en buste, à la
 * taille du croupier), de leurs nouveaux symboles et de l'illustration du
 * chapitre 1. Chaque méthode rend une grille de couleurs RGBA8888 (0 =
 * transparent), transformée en texture par {@link EnemyTextures}.
 */
final class EnemyPortraits {

    /** Taille des bustes, comme celle du croupier. */
    static final int WIDTH = 34, HEIGHT = 42;

    private EnemyPortraits() {}

    /**
     * Gardien de la Banque : heaume d'acier à visière fendue où luisent deux
     * yeux d'or, fente de pièce sur le sommet, épaulières rondes, et un grand
     * bouclier marqué de la molette d'un coffre-fort.
     */
    static int[][] gardien() {
        PixelCanvas g = new PixelCanvas(WIDTH, HEIGHT);
        int steel = rgba("7d8ca6"), steelDark = rgba("4f5b73"), steelLight = rgba("b4c2d8");
        int gold = rgba("ffd54a"), goldDark = rgba("c4891e"), slit = rgba("120c16"), eye = rgba("ffe98a");
        int shieldFace = rgba("2e3f63"), shieldShade = rgba("223050");

        // Buste en armure, épaulières rondes.
        g.rect(4, 29, 29, 41, steelDark);
        g.rect(9, 29, 24, 41, steel);
        g.ellipseShaded(6.5f, 30f, 5f, 4f, steel, steelDark, 1f);
        g.ellipseShaded(27.5f, 30f, 5f, 4f, steel, steelDark, -1f);
        for (int x = 3; x <= 10; x++) g.set(x, 27, steelLight);
        for (int x = 24; x <= 31; x++) g.set(x, 27, steelLight);
        g.set(26, 34, gold);
        g.set(26, 38, gold);
        // Gorgerin.
        g.rect(12, 24, 22, 27, steelDark);
        for (int x = 12; x <= 22; x += 2) g.set(x, 25, steelLight);
        // Heaume : dôme, joues droites, bandeau doré, visière fendue.
        g.ellipseShaded(17f, 11f, 7.5f, 7f, steel, steelDark, 2f);
        g.rect(10, 11, 24, 23, steel);
        g.rect(20, 11, 24, 23, steelDark);
        for (int y = 5; y <= 22; y++) g.set(13, y, steelLight);
        for (int x = 10; x <= 24; x++) g.set(x, 12, x % 3 == 0 ? goldDark : gold);
        g.rect(11, 15, 23, 16, slit);
        g.rect(13, 15, 14, 15, eye);
        g.rect(20, 15, 21, 15, eye);
        g.set(13, 16, gold);
        g.set(20, 16, gold);
        for (int y = 18; y <= 21; y += 3) for (int x = 15; x <= 19; x += 2) g.set(x, y, slit);
        // Fente de pièce au sommet et pièce qui y tombe.
        g.rect(15, 4, 19, 4, slit);
        g.ellipse(17f, 1.5f, 2f, 1.5f, gold);
        g.set(16, 1, rgba("fff6c8"));
        // Grand bouclier devant le buste : liseré d'or, molette de coffre-fort.
        g.rect(2, 26, 17, 41, gold);
        g.rect(3, 27, 16, 41, shieldFace);
        g.rect(11, 27, 16, 41, shieldShade);
        g.ellipse(9.5f, 34f, 4.5f, 4.5f, goldDark);
        g.ellipse(9.5f, 34f, 3.5f, 3.5f, gold);
        g.ellipse(9.5f, 34f, 1.5f, 1.5f, goldDark);
        g.line(9, 29, 9, 31, goldDark);
        g.line(9, 37, 9, 39, goldDark);
        g.line(4, 34, 6, 34, goldDark);
        g.line(13, 34, 15, 34, goldDark);
        return g.outlined();
    }

    /**
     * Sangsue du Tapis : grand ver annelé couleur lie-de-vin dressé hors du
     * feutre, gueule ronde de lamproie hérissée de dents, petits yeux jaunes,
     * et une goutte de sang qui perle.
     */
    static int[][] sangsue() {
        PixelCanvas g = new PixelCanvas(WIDTH, HEIGHT);
        int skin = rgba("8a2a4a"), skinDark = rgba("5a1530"), ring = rgba("b0466a"), belly = rgba("c46a82");
        int maw = rgba("2a0610"), throat = rgba("a01e3c"), tooth = rgba("f4efe4"), eye = rgba("ffd54a");
        int blood = rgba("e8203a"), felt = rgba("1f6b3a"), feltDark = rgba("144a28");

        // Feutre déchiré d'où elle sort.
        g.ellipse(17f, 40f, 16f, 2.5f, feltDark);
        g.ellipse(17f, 39.5f, 13f, 1.6f, felt);
        // Corps annelé, de plus en plus large vers le bas.
        float[][] segments = {{38, 12}, {34, 11}, {30, 10}, {26, 9.5f}, {22, 9.5f}};
        for (float[] s : segments) g.ellipseShaded(17f, s[0], s[1], 3.2f, skin, skinDark, s[1] * 0.35f);
        for (float[] s : segments) {
            for (int x = Math.round(17 - s[1] + 2); x <= Math.round(17 + s[1] - 2); x++) {
                if (g.get(x, Math.round(s[0]) - 3) != 0) g.set(x, Math.round(s[0]) - 3, ring);
            }
        }
        for (int y = 22; y <= 38; y++) g.set(14, y, belly);
        // Tête ronde.
        g.ellipseShaded(17f, 13f, 11f, 10f, skin, skinDark, 4f);
        g.ellipse(13f, 6f, 3f, 1.5f, ring);
        // Gueule de lamproie : anneau de dents autour d'une gorge rouge.
        g.ellipse(17f, 14f, 6.5f, 5.5f, maw);
        g.ellipse(17f, 14f, 3f, 2.5f, throat);
        g.ellipse(17f, 14f, 1.2f, 1f, maw);
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI * 2 / 12;
            g.set((int) Math.round(17 + Math.cos(a) * 5.2), (int) Math.round(14 + Math.sin(a) * 4.3), tooth);
        }
        // Yeux jaunes à pupille fendue, de chaque côté.
        g.rect(8, 8, 9, 9, eye);
        g.rect(25, 8, 26, 9, eye);
        g.set(9, 8, maw);
        g.set(25, 8, maw);
        // Goutte de sang.
        g.set(17, 20, blood);
        g.set(17, 21, blood);
        g.rect(16, 22, 18, 23, blood);
        return g.outlined();
    }

    /**
     * Bretteur à la Mise : mousquetaire au grand chapeau à plume, loup noir sur
     * les yeux, moustache en croc, pourpoint rouge à fraise blanche, et sa
     * rapière en garde, en travers du buste.
     */
    static int[][] bretteur() {
        PixelCanvas g = new PixelCanvas(WIDTH, HEIGHT);
        int hat = rgba("4a1830"), hatLight = rgba("6e2a4a"), feather = rgba("f6f0e2"), featherShade = rgba("d8c890");
        int skin = rgba("e8b48a"), skinShade = rgba("c08a62"), mask = rgba("1a1018"), eye = rgba("ffd54a");
        int hair = rgba("3a2014"), doublet = rgba("b0202c"), doubletDark = rgba("7a1420"), ruff = rgba("f4efe4");
        int gold = rgba("ffd54a"), goldDark = rgba("c4891e"), steel = rgba("dce4ee"), steelDark = rgba("8a97a8");

        // Pourpoint, épaules arrondies, boutons dorés.
        g.rect(4, 29, 29, 41, doublet);
        g.rect(20, 29, 29, 41, doubletDark);
        g.ellipseShaded(7f, 31f, 4.5f, 3.5f, doublet, doubletDark, 2f);
        g.ellipseShaded(27f, 31f, 4.5f, 3.5f, doubletDark, doubletDark, 0f);
        for (int y = 31; y <= 40; y += 3) g.set(17, y, gold);
        // Fraise blanche.
        g.ellipse(17f, 27.5f, 8f, 2f, ruff);
        for (int x = 10; x <= 24; x += 2) g.set(x, 28, rgba("c8c0b4"));
        // Visage, loup noir et yeux dorés.
        g.ellipseShaded(17f, 18f, 6.5f, 7.5f, skin, skinShade, 2f);
        g.rect(9, 15, 25, 17, mask);
        g.set(9, 14, mask);
        g.set(25, 14, mask);
        g.rect(13, 16, 14, 16, eye);
        g.rect(20, 16, 21, 16, eye);
        // Moustache en croc et barbiche.
        g.rect(13, 21, 21, 21, hair);
        g.set(12, 20, hair);
        g.set(11, 19, hair);
        g.set(22, 20, hair);
        g.set(23, 19, hair);
        g.rect(16, 24, 18, 25, hair);
        g.set(15, 23, rgba("7a2a20"));
        g.set(16, 23, rgba("7a2a20"));
        g.set(17, 23, rgba("7a2a20"));
        // Grand chapeau : bord large, calotte, plume qui retombe.
        g.ellipse(16f, 10.5f, 14f, 2.5f, hat);
        g.ellipse(16f, 7f, 7f, 5f, hat);
        g.rect(9, 7, 23, 10, hat);
        for (int x = 5; x <= 27; x++) g.set(x, 10, hatLight);
        g.rect(9, 8, 23, 8, gold);
        g.line(21, 8, 31, 1, feather);
        g.line(22, 8, 32, 2, feather);
        g.line(22, 9, 33, 3, featherShade);
        g.line(26, 4, 33, 6, featherShade);
        // Rapière en garde, de la main gauche (en bas) à la pointe (en haut à droite).
        g.line(9, 38, 33, 23, steel);
        g.line(9, 39, 33, 24, steelDark);
        g.ellipse(9f, 38f, 2f, 2f, gold);
        g.set(9, 38, goldDark);
        g.line(6, 41, 8, 39, rgba("6e4524"));
        g.ellipse(7f, 40f, 1.5f, 1.2f, skin);
        return g.outlined();
    }

    /**
     * Comète Dorée, le boss : un astre d'or en fusion, couronné de flammes,
     * suivi de sa traînée de feu, au regard furieux et au sourire de dents d'or,
     * dans une poussière d'étoiles.
     */
    static int[][] comete() {
        PixelCanvas g = new PixelCanvas(WIDTH, HEIGHT);
        int red = rgba("c8301e"), orange = rgba("ff8a1e"), yellow = rgba("ffd54a"), core = rgba("fff6c8");
        int gold = rgba("f2b632"), goldDark = rgba("c4801e"), brow = rgba("5a1a08"), mouth = rgba("3a0c08");
        int sparkle = rgba("ffffff");

        // Traînée de feu, du haut à gauche vers l'astre.
        for (int i = 0; i <= 12; i++) {
            float t = i / 12f;
            float cx = 3 + t * 13, cy = 2 + t * 18, r = 1.5f + t * 5f;
            g.ellipse(cx, cy, r, r, t < 0.4f ? red : t < 0.75f ? orange : yellow);
        }
        // Couronne de flammes.
        int[][] flames = {{9, 20, 12, 10}, {14, 17, 17, 6}, {19, 17, 23, 8}, {24, 20, 29, 13}, {27, 25, 33, 21}};
        for (int[] f : flames) {
            g.triangle(f[0], f[1], f[0] + 5, f[1], f[2], f[3], orange);
            g.triangle(f[0] + 1, f[1], f[0] + 4, f[1], f[2], f[3] + 3, yellow);
        }
        // L'astre d'or, ombré à droite, reflet en haut à gauche.
        g.ellipseShaded(19f, 28f, 11f, 11f, gold, goldDark, 4f);
        g.ellipse(15f, 23f, 3.5f, 2.5f, yellow);
        g.ellipse(14f, 22f, 1.5f, 1f, core);
        // Regard furieux.
        g.line(12, 25, 17, 27, brow);
        g.line(12, 24, 17, 26, brow);
        g.line(21, 27, 26, 25, brow);
        g.line(21, 26, 26, 24, brow);
        g.rect(14, 28, 16, 29, core);
        g.rect(22, 28, 24, 29, core);
        g.set(15, 29, red);
        g.set(23, 29, red);
        // Sourire en dents d'or.
        g.rect(13, 33, 25, 35, mouth);
        g.set(12, 32, mouth);
        g.set(26, 32, mouth);
        for (int x = 14; x <= 24; x += 2) g.set(x, 33, yellow);
        for (int x = 15; x <= 23; x += 2) g.set(x, 35, yellow);
        // Poussière d'étoiles.
        int[][] stars = {{3, 34}, {31, 6}, {6, 40}, {30, 39}};
        for (int[] s : stars) {
            g.set(s[0], s[1], sparkle);
            g.set(s[0] - 1, s[1], yellow);
            g.set(s[0] + 1, s[1], yellow);
            g.set(s[0], s[1] - 1, yellow);
            g.set(s[0], s[1] + 1, yellow);
        }
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Symboles (16 x 16)
    // -------------------------------------------------------------------------

    /** Épines : une couronne de ronces vertes hérissée de piquants. */
    static int[][] thorns() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int vine = rgba("3e8e3a"), vineDark = rgba("235c22"), thorn = rgba("e8f0b0"), tip = rgba("d8202c");
        g.ellipse(8f, 8f, 5.5f, 5.5f, vine);
        g.ellipse(8f, 8f, 3f, 3f, 0);
        for (int y = 2; y < 14; y++) for (int x = 9; x < 14; x++) if (g.get(x, y) == vine) g.set(x, y, vineDark);
        int[][] spikes = {{8, 1}, {13, 3}, {15, 8}, {13, 13}, {8, 15}, {3, 13}, {1, 8}, {3, 3}};
        for (int[] s : spikes) {
            g.set(s[0], s[1], thorn);
            g.line(s[0], s[1], (s[0] + 8) / 2, (s[1] + 8) / 2, thorn);
        }
        g.set(8, 1, tip);
        g.set(15, 8, tip);
        return g.outlined();
    }

    /** Croc : un long croc blanc planté dans la gencive, une goutte de sang au bout. */
    static int[][] fang() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int tooth = rgba("f4efe4"), toothShade = rgba("c8c0b4"), gum = rgba("c0283a"), blood = rgba("e8203a");
        for (int y = 3; y <= 13; y++) {
            float t = (y - 3) / 10f;
            float half = 4.2f * (float) Math.pow(1f - t, 0.8f) + 0.4f;
            float center = 7.5f + 2f * t * t; // la pointe se recourbe vers la droite
            for (int x = Math.round(center - half); x <= Math.round(center + half); x++) {
                g.set(x, y, x >= Math.round(center + half) - 1 ? toothShade : tooth);
            }
        }
        g.ellipse(7.5f, 2f, 6f, 1.6f, gum);
        g.set(5, 5, rgba("ffffff"));
        g.set(5, 6, rgba("ffffff"));
        g.set(9, 14, blood);
        g.rect(8, 15, 10, 15, blood);
        return g.outlined();
    }

    /** Rage : une flamme rouge et orange au cœur jaune. */
    static int[][] rage() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int red = rgba("d8202c"), orange = rgba("ff8a1e"), yellow = rgba("ffd54a");
        g.ellipse(8f, 10.5f, 5f, 4.5f, red);
        g.triangle(3, 10, 13, 10, 8, 0, red);
        g.triangle(10, 8, 14, 9, 13, 3, red);
        g.ellipse(8f, 11.5f, 3.2f, 3f, orange);
        g.triangle(5, 11, 11, 11, 8, 4, orange);
        g.ellipse(8f, 12.5f, 1.6f, 1.6f, yellow);
        g.triangle(7, 12, 9, 12, 8, 8, yellow);
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Illustration du chapitre 1
    // -------------------------------------------------------------------------

    /** Taille de l'illustration d'un chapitre. */
    static final int ART_WIDTH = 128, ART_HEIGHT = 80;

    /**
     * Chapitre 1, « La genèse » : une comète dorée traverse un ciel étoilé et
     * s'écrase sur la Terre dans un éclat de lumière.
     */
    static int[][] genesis() {
        PixelCanvas g = new PixelCanvas(ART_WIDTH, ART_HEIGHT);
        // Ciel : dégradé de la nuit au violet, étoiles.
        int[] sky = {rgba("080a22"), rgba("0d0f2e"), rgba("15123a"), rgba("201544"), rgba("2c174a")};
        for (int y = 0; y < ART_HEIGHT; y++) {
            int band = Math.min(sky.length - 1, y * sky.length / ART_HEIGHT);
            for (int x = 0; x < ART_WIDTH; x++) g.set(x, y, sky[band]);
        }
        Random random = new Random(7);
        for (int i = 0; i < 70; i++) {
            int x = random.nextInt(ART_WIDTH), y = random.nextInt(ART_HEIGHT - 20);
            g.set(x, y, random.nextInt(4) == 0 ? rgba("ffe9a0") : rgba("c8d0ff"));
        }
        // Rayons de lumière depuis l'impact.
        int impactX = 84, impactY = 58;
        int ray = rgba("3a2c4c");
        for (int i = 0; i < 9; i++) {
            double a = Math.PI + 0.15 + i * (Math.PI - 0.3) / 8;
            g.line(impactX, impactY, (int) Math.round(impactX + Math.cos(a) * 140), (int) Math.round(impactY + Math.sin(a) * 140), ray);
        }
        // La Terre : océans, continents, halo d'atmosphère.
        float earthX = 64f, earthY = 190f, earthR = 136f;
        g.ellipse(earthX, earthY, earthR + 2.5f, earthR + 2.5f, rgba("8ad0ff"));
        g.ellipse(earthX, earthY, earthR + 1f, earthR + 1f, rgba("4a9ae0"));
        g.ellipse(earthX, earthY, earthR, earthR, rgba("1e5aa8"));
        int land = rgba("3e8e3a"), landDark = rgba("2a6a2a"), cloud = rgba("e8f0ff");
        float[][] continents = {{22, 66, 14, 5}, {50, 61, 10, 3}, {100, 64, 16, 5}, {70, 72, 12, 4}, {116, 74, 8, 4}};
        for (float[] c : continents) {
            for (int y = (int) (c[1] - c[3]); y <= c[1] + c[3]; y++) {
                for (int x = (int) (c[0] - c[2]); x <= c[0] + c[2]; x++) {
                    float dx = (x - c[0]) / c[2], dy = (y - c[1]) / c[3];
                    boolean inEarth = Math.hypot(x - earthX, y - earthY) <= earthR - 1;
                    if (inEarth && dx * dx + dy * dy <= 1f) g.set(x, y, dy > 0.3f ? landDark : land);
                }
            }
        }
        g.line(30, 60, 42, 59, cloud);
        g.line(90, 70, 104, 69, cloud);
        // Traînée de la comète, du coin en haut à gauche jusqu'à l'impact.
        int[] trail = {rgba("8a2a14"), rgba("c8461e"), rgba("ff8a1e"), rgba("ffb43a"), rgba("ffd54a")};
        int steps = 40;
        for (int i = 0; i <= steps; i++) {
            float t = i / (float) steps;
            float cx = 6 + (impactX - 6) * t, cy = 2 + (impactY - 2) * t;
            float r = 1f + t * 4.5f;
            g.ellipse(cx, cy, r, r * 0.8f, trail[Math.min(trail.length - 1, (int) (t * trail.length))]);
        }
        // Éclat de l'impact : halo, cœur blanc et étincelles.
        g.ellipse(impactX, impactY, 13f, 7f, rgba("ffb43a"));
        g.ellipse(impactX, impactY, 9f, 5f, rgba("ffd54a"));
        g.ellipse(impactX, impactY, 5f, 3f, rgba("fff6c8"));
        g.ellipse(impactX, impactY, 2f, 1.5f, rgba("ffffff"));
        int spark = rgba("ffe98a");
        int[][] sparks = {{-18, -6}, {17, -8}, {-12, -12}, {22, -2}, {6, -14}, {-22, 1}};
        for (int[] s : sparks) {
            g.set(impactX + s[0], impactY + s[1], spark);
            g.set(impactX + s[0] / 2, impactY + s[1] / 2, rgba("ffffff"));
        }
        return g.pixels;
    }
}
