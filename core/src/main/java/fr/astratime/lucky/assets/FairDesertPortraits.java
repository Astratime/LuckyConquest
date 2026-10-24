package fr.astratime.lucky.assets;

import fr.astratime.lucky.entities.enemy.EnemyKind;

import static fr.astratime.lucky.assets.PixelCanvas.rgba;

/**
 * Portraits en pixel art des ennemis de la Fête foraine et du Désert de Braise,
 * en buste, à la taille du croupier (voir {@link PlacesPortraits}).
 */
final class FairDesertPortraits {

    private static final int W = EnemyPortraits.WIDTH, H = EnemyPortraits.HEIGHT;

    private static final int SKIN = rgba("f0c8a0"), SKIN_SHADE = rgba("c89a72");
    private static final int TAN  = rgba("c88a5a"), TAN_SHADE = rgba("9a643a");
    private static final int EYE  = rgba("1a1018"), MOUTH = rgba("7a2a20"), WHITE = rgba("f6f0e2");
    private static final int GOLD = rgba("ffd54a"), GOLD_DARK = rgba("c4891e"), GOLD_LIGHT = rgba("fff2a0");
    private static final int RED = rgba("d8202c"), RED_DARK = rgba("8a1a1a");
    private static final int BLUE = rgba("2a5ad8"), BLUE_DARK = rgba("1a3a8e");

    private FairDesertPortraits() {}

    /** @return le portrait de l'ennemi {@code kind}, ou {@code null} s'il n'est pas d'un de ces lieux. */
    static int[][] of(EnemyKind kind) {
        return switch (kind) {
            case CLOWN          -> clown();
            case MONSIEUR_LOYAL -> loyal();
            case SQUELETTE      -> squelette();
            case CONDUCTEUR     -> conducteur();
            case FORAIN         -> forain();
            case ROUE_FORTUNE   -> roue();
            case BONIMENTEUR    -> bonimenteur();
            case ROI_FORAIN     -> roiForain();
            case CARAVANIER     -> caravanier(false);
            case EMIR           -> caravanier(true);
            case MOMIE          -> momie();
            case PHARAON        -> pharaon();
            case SCORPION       -> scorpion();
            case DJINN          -> djinn();
            case SALAMANDRE     -> salamandre();
            case DRAGON         -> dragon();
            default             -> null;
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

    /** Haut-de-forme {@code hat}, ruban {@code band}. */
    private static void topHat(PixelCanvas g, int hat, int band) {
        g.rect(11, 1, 23, 8, hat);
        g.rect(11, 6, 23, 7, band);
        g.rect(7, 8, 27, 9, hat);
        g.rect(13, 2, 13, 5, rgba("4a4458"));
    }

    // -------------------------------------------------------------------------
    // La Fête foraine
    // -------------------------------------------------------------------------

    /** Clown jongleur : visage blanc, nez rouge, touffes de cheveux, collerette. */
    private static int[][] clown() {
        PixelCanvas g = new PixelCanvas(W, H);
        int suit = rgba("f0a020"), suitDark = rgba("b06a10"), ruff = WHITE, hair = rgba("e8402a");
        bust(g, suit, suitDark, 0);
        for (int x = 6; x <= 28; x += 4) { g.set(x, 35, BLUE); g.set(x + 2, 38, RED); }
        for (int x = 7; x <= 27; x += 3) g.ellipse(x, 28f, 2.2f, 1.6f, ruff);
        face(g, rgba("fbf6f0"), rgba("d8d0d8"));
        g.ellipse(9f, 11f, 3.5f, 3.5f, hair);
        g.ellipse(25f, 11f, 3.5f, 3.5f, hair);
        g.ellipse(17f, 8f, 3f, 2f, hair);
        g.ellipse(17f, 18f, 2f, 2f, RED);
        g.set(16, 17, rgba("ff9a9a"));
        g.line(12, 13, 15, 12, BLUE); g.line(19, 12, 22, 13, BLUE); // sourcils
        g.ellipse(17f, 21.5f, 4f, 1.5f, RED);
        g.line(14, 21, 20, 21, MOUTH);
        // Trois balles qui volent.
        g.ellipse(4f, 22f, 2f, 2f, BLUE);
        g.ellipse(30f, 18f, 2f, 2f, GOLD);
        g.ellipse(29f, 4f, 2f, 2f, RED);
        return g.outlined();
    }

    /** Monsieur Loyal : veste rouge à brandebourgs, haut-de-forme, grosse moustache, fouet. */
    private static int[][] loyal() {
        PixelCanvas g = new PixelCanvas(W, H);
        int coat = rgba("b81a2a"), coatDark = rgba("7a1018"), hair = rgba("3a2a1e");
        bust(g, coat, coatDark, WHITE);
        for (int y = 31; y <= 40; y += 3) { g.line(9, y, 13, y, GOLD); g.line(21, y, 25, y, GOLD); }
        face(g, SKIN, SKIN_SHADE);
        g.ellipse(12f, 21f, 3.5f, 1.5f, hair); // moustache en guidon
        g.ellipse(22f, 21f, 3.5f, 1.5f, hair);
        g.set(8, 20, hair); g.set(26, 20, hair);
        g.line(12, 13, 15, 13, hair); g.line(19, 13, 22, 13, hair);
        topHat(g, rgba("1a1418"), RED);
        // Le fouet qui claque.
        g.line(30, 41, 31, 26, rgba("6e4524"));
        g.line(31, 26, 25, 22, rgba("3a2a1e"));
        g.line(25, 22, 28, 18, rgba("3a2a1e"));
        g.set(28, 17, GOLD_LIGHT);
        return g.outlined();
    }

    /** Squelette farceur : crâne qui ricane, côtes et clavicules. */
    private static int[][] squelette() {
        PixelCanvas g = new PixelCanvas(W, H);
        int bone = rgba("eae4d0"), boneShade = rgba("b0a890"), dark = rgba("1a1018");
        g.rect(16, 24, 18, 41, bone); // colonne
        for (int y = 30; y <= 40; y += 3) {
            g.line(9, y, 16, y - 1, bone);
            g.line(18, y - 1, 25, y, boneShade);
        }
        g.line(5, 28, 16, 27, bone);
        g.line(18, 27, 29, 28, boneShade);
        g.ellipseShaded(17f, 14f, 8f, 9f, bone, boneShade, 2f);
        g.rect(12, 21, 22, 25, bone);
        g.ellipse(13.5f, 13f, 2.5f, 3f, dark);
        g.ellipse(20.5f, 13f, 2.5f, 3f, dark);
        g.set(13, 13, rgba("6ad86a")); g.set(20, 13, rgba("6ad86a"));
        g.triangle(16, 17, 18, 17, 17, 19, dark);
        g.line(12, 22, 22, 22, dark);
        for (int x = 13; x <= 21; x += 2) g.line(x, 21, x, 24, dark);
        // Un chapeau de fête de travers.
        g.triangle(19, 7, 27, 4, 25, -3, rgba("e86a8a"));
        g.set(25, 0, GOLD);
        return g.outlined();
    }

    /** Conducteur fantôme : visage bleu pâle, casquette, lanterne verte. */
    private static int[][] conducteur() {
        PixelCanvas g = new PixelCanvas(W, H);
        int ghost = rgba("a8c8f0"), ghostShade = rgba("6a8ac0"), uniform = rgba("2a2a4a"), uniformDark = rgba("1a1a30");
        bust(g, uniform, uniformDark, ghost);
        for (int y = 31; y <= 40; y += 3) g.set(17, y, GOLD);
        face(g, ghost, ghostShade);
        g.rect(13, 15, 14, 16, rgba("eaffea"));
        g.rect(20, 15, 21, 16, rgba("eaffea"));
        g.ellipse(17f, 22f, 2f, 1.5f, rgba("1a1a30"));
        // Casquette à visière, plaque d'or.
        g.rect(9, 4, 25, 9, uniform);
        g.rect(7, 9, 25, 10, uniformDark);
        g.rect(15, 5, 19, 7, GOLD);
        // Lanterne verte qui pend.
        g.line(29, 24, 29, 28, rgba("8a97a8"));
        g.rect(27, 28, 31, 34, rgba("4a4a5a"));
        g.rect(28, 29, 30, 33, rgba("6ad86a"));
        g.ellipse(29f, 31f, 4f, 4f, rgba("6ad86a", 80));
        return g.outlined();
    }

    /** Forain de la roue : casquette plate, marinière, moustache, manivelle à la main. */
    private static int[][] forain() {
        PixelCanvas g = new PixelCanvas(W, H);
        int stripe = rgba("2a3a8a"), cap = rgba("6a5a4a"), capDark = rgba("4a3e32"), hair = rgba("4a2a1a");
        bust(g, WHITE, rgba("c8c0b4"), 0);
        for (int y = 30; y <= 40; y += 2) g.line(4, y, 29, y, stripe);
        face(g, TAN, TAN_SHADE);
        g.line(13, 20, 21, 20, hair);
        g.line(12, 21, 14, 21, hair); g.line(20, 21, 22, 21, hair);
        g.ellipseShaded(17f, 8f, 8.5f, 3.5f, cap, capDark, 1.5f);
        g.rect(8, 9, 27, 10, capDark);
        // La manivelle de la roue.
        g.ellipse(29f, 26f, 2f, 2f, GOLD);
        g.line(29, 26, 31, 20, GOLD_DARK);
        g.rect(30, 18, 32, 20, RED);
        return g.outlined();
    }

    /** Roue de la Fortune : une roue vivante, rayons dorés, un visage au moyeu. */
    private static int[][] roue() {
        PixelCanvas g = new PixelCanvas(W, H);
        int[] wedges = {RED, WHITE, BLUE, GOLD, rgba("2a8a3a"), WHITE, rgba("e86a8a"), rgba("1a1018")};
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                double dx = x - 17, dy = y - 19, d = Math.sqrt(dx * dx + dy * dy);
                if (d > 15.5) continue;
                if (d > 14) { g.set(x, y, GOLD_DARK); continue; }
                int wedge = (int) ((Math.atan2(dy, dx) + Math.PI) / (2 * Math.PI) * wedges.length) % wedges.length;
                g.set(x, y, wedges[wedge]);
            }
        }
        for (int a = 0; a < 360; a += 45) {
            double r = Math.toRadians(a);
            g.set((int) Math.round(17 + 14.7 * Math.cos(r)), (int) Math.round(19 + 14.7 * Math.sin(r)), GOLD_LIGHT);
        }
        g.ellipseShaded(17f, 19f, 6f, 6f, GOLD, GOLD_DARK, 1.5f);
        g.rect(14, 17, 15, 18, EYE);
        g.rect(19, 17, 20, 18, EYE);
        g.line(14, 21, 20, 21, RED_DARK);
        g.set(13, 20, RED_DARK); g.set(21, 20, RED_DARK);
        // Le pied et la flèche.
        g.rect(15, 35, 19, 41, rgba("6e4524"));
        g.rect(10, 39, 24, 41, rgba("4a2e18"));
        g.triangle(14, 0, 20, 0, 17, 5, RED);
        return g.outlined();
    }

    /** Bonimenteur : canotier, nœud papillon, sourire en or, porte-voix. */
    private static int[][] bonimenteur() {
        PixelCanvas g = new PixelCanvas(W, H);
        int jacket = rgba("e86a2a"), jacketDark = rgba("a8441a"), straw = rgba("f0d890"), strawDark = rgba("c8a860");
        bust(g, jacket, jacketDark, WHITE);
        for (int x = 4; x <= 29; x += 3) g.line(x, 29, x, 41, jacketDark);
        g.triangle(13, 28, 17, 30, 13, 32, RED); // nœud papillon
        g.triangle(21, 28, 17, 30, 21, 32, RED);
        face(g, SKIN, SKIN_SHADE);
        g.line(14, 21, 20, 21, MOUTH);
        g.rect(16, 22, 18, 22, GOLD); // dent en or
        g.set(20, 15, SKIN); // un clin d'œil
        g.line(19, 16, 21, 16, EYE);
        g.rect(9, 5, 25, 8, straw);
        g.rect(9, 7, 25, 8, RED);
        g.rect(5, 9, 29, 10, strawDark);
        // Le porte-voix.
        g.triangle(1, 18, 1, 30, 9, 24, rgba("dce4ee"));
        g.rect(8, 23, 11, 25, rgba("8a97a8"));
        return g.outlined();
    }

    /** Roi des forains : couronne, manteau de fourrure, chevalière, rictus doré. */
    private static int[][] roiForain() {
        PixelCanvas g = new PixelCanvas(W, H);
        int coat = rgba("6a2a8a"), coatDark = rgba("4a1a62"), fur = rgba("f0ece0"), beard = rgba("5a3a1e");
        bust(g, coat, coatDark, 0);
        g.rect(4, 28, 11, 41, fur);
        g.rect(23, 28, 29, 41, rgba("d8d0c0"));
        for (int y = 30; y <= 40; y += 3) { g.set(7, y, EYE); g.set(26, y + 1, EYE); }
        face(g, SKIN, SKIN_SHADE);
        g.ellipse(17f, 24f, 6f, 3.5f, beard);
        g.line(14, 21, 20, 21, GOLD);
        g.rect(9, 3, 25, 8, GOLD);
        g.triangle(9, 3, 12, 3, 10, -1, GOLD);
        g.triangle(15, 3, 19, 3, 17, -2, GOLD);
        g.triangle(22, 3, 25, 3, 24, -1, GOLD);
        g.rect(9, 7, 25, 8, GOLD_DARK);
        g.set(13, 5, RED); g.set(17, 5, BLUE); g.set(21, 5, RED);
        // Une main qui tient une carte truquée.
        g.ellipse(29f, 30f, 2.5f, 2f, SKIN);
        g.rect(27, 22, 32, 28, WHITE);
        g.set(29, 24, RED); g.set(30, 26, RED);
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Le Désert de Braise
    // -------------------------------------------------------------------------

    /** Caravanier : turban, barbe, foulard et bâton. L'Émir : turban à joyau, robe d'or. */
    private static int[][] caravanier(boolean emir) {
        PixelCanvas g = new PixelCanvas(W, H);
        int robe = emir ? GOLD : rgba("d8c8a0"), robeDark = emir ? GOLD_DARK : rgba("a8987a");
        int turban = emir ? WHITE : rgba("2a5ad8"), turbanDark = emir ? rgba("c8c0b4") : BLUE_DARK, beard = rgba("2a1a12");
        bust(g, robe, robeDark, emir ? RED : rgba("8a5a2a"));
        if (emir) for (int y = 31; y <= 40; y += 3) { g.set(13, y, RED); g.set(21, y, RED); }
        face(g, TAN, TAN_SHADE);
        g.ellipse(17f, 24f, 6f, 4.5f, beard);
        g.line(15, 21, 19, 21, rgba("5a2a1a"));
        g.ellipseShaded(17f, 7f, 9f, 5f, turban, turbanDark, 2f);
        for (int x = 9; x <= 25; x += 3) g.line(x, 3, x + 2, 11, turbanDark);
        if (emir) {
            g.ellipse(17f, 7f, 2f, 2.5f, RED);
            g.set(16, 6, rgba("ff9a9a"));
            g.triangle(16, 4, 18, 4, 17, -1, WHITE); // plume
        } else {
            g.line(30, 41, 30, 14, rgba("8a5a2a")); // bâton
            g.ellipse(30f, 13f, 1.5f, 1.5f, rgba("6e4524"));
        }
        return g.outlined();
    }

    /** Momie : tête et buste bandés, yeux qui luisent dans les fentes. */
    private static int[][] momie() {
        PixelCanvas g = new PixelCanvas(W, H);
        int linen = rgba("e0d4b4"), linenDark = rgba("a8987a"), gap = rgba("3a2a1e"), glow = rgba("ffd54a");
        bust(g, linen, linenDark, 0);
        g.ellipseShaded(17f, 15f, 8f, 9.5f, linen, linenDark, 2f);
        g.rect(14, 24, 20, 28, linenDark);
        for (int y = 6; y <= 40; y += 3) {
            int tilt = (y / 3) % 2 == 0 ? 1 : -1;
            g.line(5, y + tilt, 29, y - tilt, linenDark);
        }
        g.rect(11, 14, 23, 16, gap);
        g.rect(13, 15, 14, 15, glow);
        g.rect(20, 15, 21, 15, glow);
        g.line(26, 30, 33, 36, linen); // une bande qui pend
        return g.outlined();
    }

    /** Pharaon maudit : coiffe rayée bleu et or, barbe postiche, collier. */
    private static int[][] pharaon() {
        PixelCanvas g = new PixelCanvas(W, H);
        bust(g, rgba("f0e8d8"), rgba("c8c0b0"), 0);
        for (int x = 6; x <= 28; x++) for (int y = 28; y <= 32; y++) {
            if (Math.abs(x - 17) <= 11 - (y - 28)) g.set(x, y, (y % 2 == 0) ? GOLD : BLUE);
        }
        // La coiffe (némès), rayée, qui tombe sur les épaules.
        for (int y = 2; y <= 28; y++) {
            int half = y < 10 ? 6 + y / 2 : 11;
            for (int x = 17 - half; x <= 17 + half; x++) g.set(x, y, (y / 2) % 2 == 0 ? GOLD : BLUE_DARK);
        }
        g.ellipseShaded(17f, 16f, 6f, 7.5f, rgba("c89a5a"), rgba("9a6a3a"), 2f);
        g.rect(12, 14, 15, 15, EYE);
        g.rect(19, 14, 22, 15, EYE);
        g.set(11, 15, EYE); g.set(23, 15, EYE);
        g.line(15, 20, 19, 20, MOUTH);
        g.rect(16, 22, 18, 27, GOLD); // barbe postiche
        g.ellipse(17f, 4f, 1.5f, 2f, RED); // le cobra
        g.set(17, 2, rgba("2a8a3a"));
        return g.outlined();
    }

    /** Scorpion des dunes : pinces levées, queue recourbée au dard vert. */
    private static int[][] scorpion() {
        PixelCanvas g = new PixelCanvas(W, H);
        int shell = rgba("8a3a1e"), shellDark = rgba("5a2210"), sting = rgba("6ae04a");
        g.ellipseShaded(17f, 33f, 9f, 6f, shell, shellDark, 2f);
        for (int x = 9; x <= 25; x += 4) g.line(x, 29, x, 37, shellDark);
        for (int k = 0; k < 3; k++) { // pattes
            g.line(9, 32 + k * 2, 3, 36 + k * 2, shellDark);
            g.line(25, 32 + k * 2, 31, 36 + k * 2, shellDark);
        }
        g.ellipseShaded(17f, 25f, 5f, 3.5f, shell, shellDark, 1f);
        g.rect(14, 24, 15, 24, rgba("1a1018")); g.rect(19, 24, 20, 24, rgba("1a1018"));
        // Pinces.
        g.line(12, 25, 6, 18, shell);
        g.line(22, 25, 28, 18, shell);
        g.ellipseShaded(5f, 16f, 3f, 2.5f, shell, shellDark, 1f);
        g.ellipseShaded(29f, 16f, 3f, 2.5f, shell, shellDark, 1f);
        g.set(5, 14, 0); g.set(29, 14, 0);
        // Queue en anneaux, dard.
        int[][] tail = {{17, 28}, {19, 22}, {22, 17}, {23, 11}, {21, 6}, {17, 4}};
        for (int[] t : tail) g.ellipseShaded(t[0], t[1], 2.5f, 2.5f, shell, shellDark, 1f);
        g.triangle(13, 3, 16, 2, 13, 7, sting);
        return g.outlined();
    }

    /** Djinn du Mirage : peau bleue, crête de cheveux, anneaux d'or, corps de fumée. */
    private static int[][] djinn() {
        PixelCanvas g = new PixelCanvas(W, H);
        int skin = rgba("4a8ad8"), skinShade = rgba("2a5aa8"), smoke = rgba("8ab8f0", 200), hair = rgba("1a1a3a");
        for (int y = 28; y < H; y++) { // fumée qui tourbillonne
            int half = Math.max(2, 12 - (y - 28));
            int shift = (int) Math.round(3 * Math.sin(y / 3.0));
            g.rect(17 - half + shift, y, 17 + half + shift, y, smoke);
        }
        g.ellipseShaded(8f, 30f, 5f, 3.5f, skin, skinShade, 2f);
        g.ellipseShaded(26f, 30f, 5f, 3.5f, skinShade, skinShade, 0f);
        g.rect(9, 32, 25, 36, skin);
        face(g, skin, skinShade);
        g.rect(13, 15, 14, 16, GOLD_LIGHT);
        g.rect(20, 15, 21, 16, GOLD_LIGHT);
        g.line(14, 21, 20, 21, rgba("1a1a3a"));
        g.line(13, 20, 14, 21, rgba("1a1a3a"));
        g.line(21, 20, 20, 21, rgba("1a1a3a"));
        g.ellipse(17f, 5f, 2.5f, 3f, hair); // crête
        g.line(17, 2, 21, -1, hair);
        g.rect(13, 7, 21, 8, GOLD); // bandeau
        g.ellipse(9.5f, 18f, 1.2f, 1.6f, GOLD); // anneaux
        g.ellipse(24.5f, 18f, 1.2f, 1.6f, GOLD);
        g.rect(5, 33, 7, 34, GOLD); g.rect(27, 33, 29, 34, GOLD); // bracelets
        return g.outlined();
    }

    /** Salamandre de lave : lézard rouge aux taches qui brûlent, langue fourchue. */
    private static int[][] salamandre() {
        PixelCanvas g = new PixelCanvas(W, H);
        int scale = rgba("c8401e"), scaleDark = rgba("8a2210"), lava = rgba("ffb84a"), belly = rgba("f08a3a");
        g.ellipseShaded(17f, 34f, 10f, 7f, scale, scaleDark, 3f);
        g.ellipse(17f, 36f, 5f, 4f, belly);
        g.line(4, 36, 0, 41, scale); g.line(30, 36, 33, 41, scaleDark);
        g.rect(14, 24, 20, 28, scaleDark);
        g.ellipseShaded(17f, 18f, 9f, 7f, scale, scaleDark, 2f);
        g.ellipse(17f, 23f, 6f, 2.5f, scale);
        g.ellipse(11f, 13f, 2.5f, 2.5f, GOLD);
        g.ellipse(23f, 13f, 2.5f, 2.5f, GOLD);
        g.rect(11, 12, 11, 14, EYE); g.rect(23, 12, 23, 14, EYE);
        g.line(12, 23, 22, 23, scaleDark);
        g.line(17, 24, 17, 28, rgba("ff6a8a")); // langue
        g.set(16, 29, rgba("ff6a8a")); g.set(18, 29, rgba("ff6a8a"));
        for (int[] spot : new int[][] {{12, 31}, {21, 30}, {25, 35}, {9, 37}, {15, 18}, {20, 16}}) {
            g.ellipse(spot[0], spot[1], 1.2f, 1.2f, lava);
        }
        return g.outlined();
    }

    /** Ignar, le Dragon du Volcan : cornes, écailles rouges, yeux d'or, braises aux naseaux. */
    private static int[][] dragon() {
        PixelCanvas g = new PixelCanvas(W, H);
        int scale = rgba("a82a1e"), scaleDark = rgba("6a1a12"), horn = rgba("f0e2b8"), hornDark = rgba("b8a880");
        int belly = rgba("e8a04a"), eye = rgba("ffd54a"), ember = rgba("ff7a1e");
        bust(g, scale, scaleDark, belly);
        for (int y = 30; y <= 40; y += 2) g.line(13, y, 21, y, rgba("c8803a"));
        g.rect(13, 22, 21, 29, scaleDark);
        g.ellipseShaded(17f, 15f, 9.5f, 8f, scale, scaleDark, 2f);
        g.ellipseShaded(17f, 22f, 6.5f, 4f, scale, scaleDark, 1f); // museau
        // Cornes recourbées et crête.
        g.line(9, 9, 4, 2, horn); g.line(10, 9, 5, 2, hornDark); g.line(4, 2, 2, 4, horn);
        g.line(25, 9, 30, 2, horn); g.line(24, 9, 29, 2, hornDark); g.line(30, 2, 32, 4, horn);
        g.triangle(15, 7, 19, 7, 17, 3, scaleDark);
        // Yeux fendus d'or.
        g.rect(11, 13, 14, 15, eye);
        g.rect(20, 13, 23, 15, eye);
        g.rect(12, 13, 12, 15, EYE);
        g.rect(22, 13, 22, 15, EYE);
        g.line(10, 12, 15, 13, scaleDark); g.line(19, 13, 24, 12, scaleDark);
        // Naseaux qui fument, crocs.
        g.set(15, 21, EYE); g.set(19, 21, EYE);
        g.set(14, 19, ember); g.set(20, 19, ember);
        g.line(12, 25, 22, 25, EYE);
        g.set(13, 26, WHITE); g.set(16, 26, WHITE); g.set(18, 26, WHITE); g.set(21, 26, WHITE);
        // Ailes repliées dans le dos.
        g.triangle(0, 26, 5, 30, 3, 14, scaleDark);
        g.triangle(33, 26, 29, 30, 31, 14, scaleDark);
        return g.outlined();
    }
}
