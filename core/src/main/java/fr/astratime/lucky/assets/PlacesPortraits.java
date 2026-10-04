package fr.astratime.lucky.assets;

import fr.astratime.lucky.entities.enemy.EnemyKind;

import static fr.astratime.lucky.assets.PixelCanvas.rgba;

/**
 * Portraits en pixel art des ennemis des lieux de l'Exploration ouverts après
 * la prairie : le Port des Contrebandiers, les Mines d'Or et le Casino
 * Englouti. En buste, à la taille du croupier ; chaque méthode rend une grille
 * de couleurs RGBA8888 (0 = transparent), transformée en texture par
 * {@link EnemyTextures}.
 */
final class PlacesPortraits {

    private static final int W = EnemyPortraits.WIDTH, H = EnemyPortraits.HEIGHT;

    private static final int SKIN = rgba("f0c8a0"), SKIN_SHADE = rgba("c89a72");
    private static final int TAN  = rgba("d8a070"), TAN_SHADE = rgba("a87448");
    private static final int EYE  = rgba("1a1018"), MOUTH = rgba("7a2a20"), WHITE = rgba("f6f0e2");
    private static final int GOLD = rgba("ffd54a"), GOLD_DARK = rgba("c4891e"), GOLD_LIGHT = rgba("fff2a0");
    private static final int STEEL = rgba("b8c0cc"), STEEL_DARK = rgba("7a8494");
    private static final int WOOD = rgba("8a5a2a"), WOOD_DARK = rgba("5e3a18");
    private static final int RED = rgba("d8202c"), RED_DARK = rgba("8a1a1a");

    private PlacesPortraits() {}

    /** @return le portrait de l'ennemi {@code kind}, ou {@code null} s'il n'est pas d'un de ces lieux. */
    static int[][] of(EnemyKind kind) {
        return switch (kind) {
            case RAT_CALES       -> rat(false);
            case CAPITAINE_RAT   -> rat(true);
            case BUVEUR          -> buveur();
            case TAVERNIER       -> tavernier();
            case GUETTEUR        -> guetteur();
            case GARDIEN_PHARE   -> gardienPhare();
            case PIRATE          -> pirate(false);
            case CAPITAINE_NOIR  -> pirate(true);
            case CHERCHEUR_OR    -> chercheur();
            case BARON_OR        -> baron();
            case FOREUR          -> foreur(false);
            case GRAND_FOREUR    -> foreur(true);
            case FORGERON        -> forgeron(false);
            case MAITRE_FORGE    -> forgeron(true);
            case GOLEM_OR        -> golem(false);
            case COEUR_MONTAGNE  -> golem(true);
            case BARMAN_NOYE     -> barman();
            case SIRENE          -> sirene();
            case BANDIT_MANCHOT  -> bandit(false);
            case JACKPOT_VIVANT  -> bandit(true);
            case REQUIN          -> requin(false);
            case REQUIN_BANQUIER -> requin(true);
            case PIEUVRE         -> pieuvre(false);
            case KRAKEN          -> pieuvre(true);
            default              -> null;
        };
    }

    // -------------------------------------------------------------------------
    // Gabarit commun : buste, cou et visage
    // -------------------------------------------------------------------------

    /** Buste : veste {@code coat} (ombrée à droite) et col {@code collar}. */
    private static void bust(PixelCanvas g, int coat, int coatDark, int collar) {
        g.rect(4, 29, 29, 41, coat);
        g.rect(20, 29, 29, 41, coatDark);
        g.ellipseShaded(7f, 31f, 4.5f, 3.5f, coat, coatDark, 3f);
        g.ellipseShaded(27f, 31f, 4.5f, 3.5f, coatDark, coatDark, 0f);
        if (collar != 0) g.triangle(13, 28, 21, 28, 17, 35, collar);
    }

    /** Cou et visage ovale, yeux ronds et bouche droite. */
    private static void face(PixelCanvas g, int skin, int skinShade) {
        g.rect(14, 24, 20, 28, skinShade);
        g.ellipseShaded(17f, 16f, 7f, 8.5f, skin, skinShade, 2f);
        g.rect(13, 15, 14, 16, EYE);
        g.rect(20, 15, 21, 16, EYE);
        g.set(17, 18, skinShade);
        g.line(15, 21, 19, 21, MOUTH);
    }

    /** Tricorne de couleur {@code hat}, bordé de {@code trim}. */
    private static void tricorn(PixelCanvas g, int hat, int hatDark, int trim) {
        g.ellipseShaded(17f, 7f, 6f, 4f, hat, hatDark, 2f);
        g.triangle(6, 10, 28, 10, 17, 5, hat);
        g.triangle(4, 11, 10, 11, 5, 5, hat);
        g.triangle(24, 11, 30, 11, 29, 5, hatDark);
        g.line(5, 11, 29, 11, trim);
    }

    /** Casque de mineur : calotte {@code color} et lampe allumée. */
    private static void minerHelmet(PixelCanvas g, int color, int colorDark) {
        g.ellipseShaded(17f, 9f, 8f, 5f, color, colorDark, 2f);
        g.rect(8, 10, 26, 11, colorDark);
        g.rect(15, 4, 19, 7, STEEL_DARK);
        g.rect(16, 5, 18, 6, GOLD_LIGHT);
    }

    // -------------------------------------------------------------------------
    // Le Port des Contrebandiers
    // -------------------------------------------------------------------------

    /**
     * Rat des cales : un gros rat gris debout, oreilles roses, longues
     * moustaches et dents qui dépassent. Le Capitaine Rat porte un tricorne,
     * un manteau rouge et un sabre.
     */
    private static int[][] rat(boolean captain) {
        PixelCanvas g = new PixelCanvas(W, H);
        int fur = rgba("8a8494"), furDark = rgba("5e5a68"), pink = rgba("e89aa8"), belly = rgba("c8c0c8");
        int coat = captain ? rgba("a82a2a") : 0, coatDark = rgba("6e1a1a");

        // Corps et ventre clair, ou manteau rouge à boutons d'or.
        g.ellipseShaded(17f, 36f, 12f, 8f, fur, furDark, 4f);
        g.ellipse(16f, 37f, 6f, 5f, belly);
        if (captain) {
            bust(g, coat, coatDark, belly);
            for (int y = 31; y <= 40; y += 3) { g.set(13, y, GOLD); g.set(21, y, GOLD); }
        }
        // Queue rose qui s'enroule.
        g.line(28, 40, 32, 34, pink);
        g.line(32, 34, 31, 28, pink);
        // Tête pointue, oreilles rondes, museau rose.
        g.ellipseShaded(17f, 18f, 8f, 7f, fur, furDark, 2f);
        g.triangle(10, 19, 24, 19, 17, 27, fur);
        g.ellipse(9f, 11f, 3.5f, 3.5f, fur);
        g.ellipse(9f, 11f, 2f, 2f, pink);
        g.ellipse(25f, 11f, 3.5f, 3.5f, furDark);
        g.ellipse(25f, 11f, 2f, 2f, pink);
        g.rect(16, 25, 18, 26, pink);
        // Yeux rouges, moustaches, deux incisives.
        g.rect(13, 17, 14, 18, RED);
        g.rect(20, 17, 21, 18, RED);
        g.line(9, 22, 15, 24, belly);
        g.line(19, 24, 25, 22, belly);
        g.line(9, 25, 15, 25, belly);
        g.line(19, 25, 25, 25, belly);
        g.rect(16, 27, 18, 28, WHITE);
        if (captain) {
            tricorn(g, rgba("2a2230"), rgba("1a141e"), GOLD);
            g.rect(7, 4, 9, 6, WHITE); // plume
            // Sabre courbe tenu à droite.
            g.line(29, 38, 32, 22, STEEL);
            g.line(30, 38, 33, 23, STEEL_DARK);
            g.rect(27, 37, 31, 38, GOLD);
        }
        return g.outlined();
    }

    /**
     * Buveur de rhum : joues et nez rouges, yeux mi-clos, barbe de trois jours,
     * chemise ouverte, et une chope qui déborde.
     */
    private static int[][] buveur() {
        PixelCanvas g = new PixelCanvas(W, H);
        int shirt = rgba("d8c8a0"), shirtDark = rgba("a8987a"), stubble = rgba("8a7a6a"), hair = rgba("5a3a1e");
        bust(g, shirt, shirtDark, SKIN);
        face(g, SKIN, SKIN_SHADE);
        // Cheveux en bataille.
        g.ellipse(17f, 9f, 7.5f, 3.5f, hair);
        g.set(11, 6, hair); g.set(15, 5, hair); g.set(21, 5, hair);
        // Yeux mi-clos, joues et nez rouges, barbe naissante.
        g.line(12, 15, 15, 15, SKIN);
        g.line(19, 15, 22, 15, SKIN);
        g.line(12, 15, 15, 15, EYE);
        g.line(19, 15, 22, 15, EYE);
        g.ellipse(17f, 18f, 1.5f, 1.5f, rgba("e05a5a"));
        g.set(12, 18, rgba("f08080")); g.set(22, 18, rgba("f08080"));
        for (int x = 12; x <= 22; x += 2) { g.set(x, 23, stubble); g.set(x + 1, 22, stubble); }
        g.line(15, 21, 19, 20, MOUTH);
        // Chope de bois cerclée, mousse qui déborde.
        g.rect(23, 25, 30, 35, WOOD);
        g.rect(28, 25, 30, 35, WOOD_DARK);
        g.rect(23, 28, 30, 28, STEEL_DARK);
        g.rect(23, 32, 30, 32, STEEL_DARK);
        g.ellipse(26.5f, 24f, 4.5f, 2f, WHITE);
        g.set(24, 26, WHITE);
        g.rect(31, 27, 32, 33, WOOD_DARK);
        return g.outlined();
    }

    /**
     * Tavernier : gros bonhomme chauve, moustache en guidon, tablier blanc taché
     * et un torchon sur l'épaule ; il tient un tonnelet.
     */
    private static int[][] tavernier() {
        PixelCanvas g = new PixelCanvas(W, H);
        int shirt = rgba("b04a3a"), shirtDark = rgba("7e3026"), apron = rgba("ece6da"), stain = rgba("c8a878");
        int mustache = rgba("3a2414");
        bust(g, shirt, shirtDark, 0);
        g.rect(9, 30, 25, 41, apron);
        g.set(13, 35, stain); g.set(14, 36, stain); g.set(20, 39, stain);
        g.line(9, 30, 13, 27, apron);
        g.line(25, 30, 21, 27, apron);
        g.rect(25, 28, 29, 31, rgba("e0d070"));
        g.rect(14, 24, 20, 28, SKIN_SHADE);
        g.ellipseShaded(17f, 16f, 8f, 9f, SKIN, SKIN_SHADE, 3f);
        g.ellipse(14f, 9f, 2f, 1.2f, rgba("f8dcc0"));
        g.rect(13, 15, 14, 15, EYE);
        g.rect(20, 15, 21, 15, EYE);
        g.line(12, 13, 15, 13, mustache);
        g.line(19, 13, 22, 13, mustache);
        g.ellipse(17f, 18f, 1.5f, 1.2f, SKIN_SHADE);
        // Moustache en guidon.
        g.rect(12, 20, 22, 21, mustache);
        g.set(11, 19, mustache); g.set(10, 18, mustache);
        g.set(23, 19, mustache); g.set(24, 18, mustache);
        g.line(15, 23, 19, 23, MOUTH);
        // Tonnelet sous le bras.
        g.ellipseShaded(5f, 36f, 4f, 5f, WOOD, WOOD_DARK, 1f);
        g.line(1, 34, 9, 34, STEEL_DARK);
        g.line(1, 38, 9, 38, STEEL_DARK);
        return g.outlined();
    }

    /**
     * Guetteur : marin au bonnet rayé, vareuse bleue, qui colle une longue-vue
     * de cuivre à son œil.
     */
    private static int[][] guetteur() {
        PixelCanvas g = new PixelCanvas(W, H);
        int coat = rgba("2a4a8a"), coatDark = rgba("1a2e5a"), stripe = rgba("ece6da"), cap = rgba("c83a3a");
        int brass = rgba("d8a040"), brassDark = rgba("8a6020");
        bust(g, coat, coatDark, stripe);
        for (int y = 29; y <= 35; y += 2) g.line(14, y, 20, y, coat);
        face(g, TAN, TAN_SHADE);
        // Bonnet rouge à pompon.
        g.ellipseShaded(17f, 9f, 7.5f, 4f, cap, rgba("8a2020"), 2f);
        g.rect(9, 10, 25, 11, stripe);
        g.ellipse(17f, 4f, 2f, 2f, stripe);
        // Longue-vue sur l'œil droit, tenue des deux mains.
        g.rect(21, 14, 32, 17, brass);
        g.rect(21, 16, 32, 17, brassDark);
        g.rect(25, 13, 26, 18, brassDark);
        g.rect(30, 13, 33, 18, brass);
        g.ellipse(24f, 20f, 2.5f, 2f, TAN);
        g.ellipse(29f, 20f, 2.5f, 2f, TAN_SHADE);
        return g.outlined();
    }

    /**
     * Gardien du Phare : vieux loup de mer en ciré jaune, suroît sur la tête,
     * barbe blanche, et une lanterne qui éclaire fort.
     */
    private static int[][] gardienPhare() {
        PixelCanvas g = new PixelCanvas(W, H);
        int coat = rgba("f0c020"), coatDark = rgba("b08a10"), beard = rgba("ece6e0"), beardShade = rgba("c0b8b0");
        int glass = rgba("fff6a0"), light = rgba("ffe060");
        bust(g, coat, coatDark, 0);
        for (int y = 31; y <= 40; y += 3) g.set(17, y, STEEL_DARK);
        face(g, TAN, TAN_SHADE);
        // Grande barbe blanche.
        g.ellipseShaded(17f, 24f, 7f, 6f, beard, beardShade, 2f);
        g.line(15, 21, 19, 21, MOUTH);
        g.line(12, 13, 15, 13, beard);
        g.line(19, 13, 22, 13, beard);
        // Suroît : bord large qui tombe à l'arrière.
        g.ellipseShaded(17f, 8f, 7f, 4f, coat, coatDark, 2f);
        g.rect(7, 10, 27, 11, coatDark);
        g.triangle(24, 10, 30, 10, 29, 16, coatDark);
        // Lanterne levée, halo de lumière.
        g.ellipse(4f, 15f, 4f, 4f, light);
        g.rect(2, 12, 6, 18, glass);
        g.rect(1, 11, 7, 11, STEEL_DARK);
        g.rect(1, 19, 7, 19, STEEL_DARK);
        g.rect(3, 9, 5, 10, STEEL_DARK);
        g.rect(3, 20, 5, 28, coat);
        return g.outlined();
    }

    /**
     * Pirate : bandana rouge, bandeau sur l'œil, anneau d'or à l'oreille, barbe
     * noire et sabre. Le Capitaine Noir porte un grand tricorne à tête de mort,
     * un manteau noir à galons d'or et une barbe tressée.
     */
    private static int[][] pirate(boolean captain) {
        PixelCanvas g = new PixelCanvas(W, H);
        int coat = captain ? rgba("2a2230") : rgba("e8e0d0"), coatDark = captain ? rgba("161220") : rgba("b8b0a0");
        int beard = rgba("2a1a14");
        bust(g, coat, coatDark, captain ? RED : 0);
        if (captain) {
            g.line(9, 29, 9, 41, GOLD);
            g.line(25, 29, 25, 41, GOLD);
            g.rect(3, 28, 9, 29, GOLD);
            g.rect(25, 28, 31, 29, GOLD_DARK);
        } else {
            for (int y = 31; y <= 41; y += 3) g.line(4, y, 29, y, rgba("c83a3a"));
        }
        face(g, TAN, TAN_SHADE);
        // Bandeau sur l'œil gauche.
        g.line(9, 11, 25, 17, EYE);
        g.rect(12, 14, 15, 17, EYE);
        // Barbe noire (tressée pour le capitaine).
        g.ellipse(17f, 23f, 6f, 3.5f, beard);
        g.line(15, 21, 19, 21, MOUTH);
        if (captain) {
            g.rect(16, 26, 18, 31, beard);
            g.set(17, 28, RED); g.set(17, 31, RED);
        }
        // Anneau d'or.
        g.set(24, 19, GOLD); g.set(25, 20, GOLD); g.set(24, 21, GOLD);
        if (captain) {
            tricorn(g, rgba("1a141e"), rgba("0e0a12"), GOLD);
            g.rect(15, 6, 19, 8, WHITE); // tête de mort
            g.set(16, 7, EYE); g.set(18, 7, EYE);
            g.set(15, 9, WHITE); g.set(19, 9, WHITE);
        } else {
            g.ellipseShaded(17f, 9f, 7.5f, 3.5f, RED, RED_DARK, 2f);
            g.rect(24, 9, 27, 11, RED);
            g.rect(26, 11, 28, 14, RED_DARK);
        }
        // Sabre levé.
        g.line(30, 40, 30, 14, STEEL);
        g.line(31, 40, 31, 15, STEEL_DARK);
        g.set(29, 14, STEEL);
        g.rect(28, 36, 33, 37, GOLD);
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Les Mines d'Or
    // -------------------------------------------------------------------------

    /**
     * Chercheur d'or : casque à lampe, visage noirci de suie, salopette et une
     * batée pleine de pépites.
     */
    private static int[][] chercheur() {
        PixelCanvas g = new PixelCanvas(W, H);
        int overall = rgba("3a5a8a"), overallDark = rgba("243a5e"), shirt = rgba("c8a070"), soot = rgba("6a5a50");
        bust(g, shirt, rgba("9a7a50"), 0);
        g.rect(9, 32, 25, 41, overall);
        g.rect(20, 32, 25, 41, overallDark);
        g.rect(10, 29, 11, 32, overall);
        g.rect(23, 29, 24, 32, overallDark);
        face(g, SKIN, SKIN_SHADE);
        g.set(12, 19, soot); g.set(13, 20, soot); g.set(22, 18, soot); g.set(21, 22, soot);
        minerHelmet(g, rgba("d8a020"), rgba("a07810"));
        // Batée de pépites tenue devant lui.
        g.ellipseShaded(17f, 37f, 9f, 3f, STEEL_DARK, rgba("5a6474"), 4f);
        g.ellipse(17f, 36f, 7f, 1.5f, rgba("4a5464"));
        g.set(14, 36, GOLD); g.set(17, 35, GOLD); g.set(19, 36, GOLD_LIGHT); g.set(21, 36, GOLD);
        return g.outlined();
    }

    /**
     * Baron de l'Or : haut-de-forme doré, monocle, favoris, redingote violette,
     * chaîne de montre et canne à pommeau d'or. Sa peau même brille d'or.
     */
    private static int[][] baron() {
        PixelCanvas g = new PixelCanvas(W, H);
        int coat = rgba("5a2a7a"), coatDark = rgba("3a1a52"), skin = rgba("f0d080"), skinShade = rgba("c8a050");
        int whisker = rgba("8a6a30");
        bust(g, coat, coatDark, WHITE);
        g.line(19, 34, 24, 37, GOLD);
        g.rect(16, 29, 18, 30, RED);
        face(g, skin, skinShade);
        g.rect(9, 14, 10, 22, whisker);
        g.rect(24, 14, 25, 22, whisker);
        g.line(14, 21, 20, 21, MOUTH);
        g.set(14, 20, MOUTH); g.set(20, 20, MOUTH);
        // Monocle et chaînette.
        g.ellipse(20.5f, 15.5f, 2.5f, 2.5f, GOLD_DARK);
        g.rect(20, 15, 21, 16, rgba("c8e6f0"));
        for (int y = 18; y <= 23; y++) g.set(23 + (y % 2), y, GOLD);
        // Haut-de-forme doré.
        g.rect(11, 1, 23, 9, GOLD);
        g.rect(19, 1, 23, 9, GOLD_DARK);
        g.rect(11, 7, 23, 8, coat);
        g.rect(8, 9, 26, 10, GOLD_DARK);
        // Canne à pommeau d'or.
        g.rect(30, 26, 30, 41, rgba("1a1418"));
        g.ellipse(30f, 25f, 2f, 2f, GOLD);
        return g.outlined();
    }

    /**
     * Foreur : lunettes de protection, casque, bleu de travail et une foreuse
     * à main. Le Grand Foreur a un bras-foreuse énorme et des lunettes rouges.
     */
    private static int[][] foreur(boolean big) {
        PixelCanvas g = new PixelCanvas(W, H);
        int suit = big ? rgba("4a4a52") : rgba("3a6a9a"), suitDark = big ? rgba("2e2e36") : rgba("244a6e");
        int lens = big ? rgba("ff5a3a") : rgba("8ad8f0"), strap = rgba("3a2a1e");
        bust(g, suit, suitDark, 0);
        g.rect(16, 30, 18, 41, STEEL_DARK);
        face(g, SKIN, SKIN_SHADE);
        // Lunettes de protection.
        g.rect(9, 14, 25, 14, strap);
        g.ellipse(13.5f, 15.5f, 2.5f, 2f, STEEL_DARK);
        g.ellipse(20.5f, 15.5f, 2.5f, 2f, STEEL_DARK);
        g.rect(13, 15, 14, 16, lens);
        g.rect(20, 15, 21, 16, lens);
        minerHelmet(g, big ? rgba("e05a20") : rgba("e0c020"), big ? rgba("a03a10") : rgba("a08a10"));
        // Foreuse : moteur et mèche en spirale (bien plus grosse pour le chef).
        int x0 = big ? 19 : 22;
        g.rect(x0, 27, x0 + 6, 33, big ? RED_DARK : rgba("e0c020"));
        g.rect(x0 + 1, 33, x0 + 3, 37, WOOD_DARK);
        int tip = big ? 3 : 1;
        for (int i = 0; i <= 8; i++) {
            int y = 26 - i * 2, half = Math.max(0, tip + 2 - i / 2);
            g.rect(x0 + 3 - half, y - 1, x0 + 3 + half, y, i % 2 == 0 ? STEEL : STEEL_DARK);
        }
        return g.outlined();
    }

    /**
     * Forgeron : bras nus et musclés, tablier de cuir, marteau sur l'épaule.
     * Le Maître de la Forge a une barbe tressée rousse et un casque à cornes.
     */
    private static int[][] forgeron(boolean master) {
        PixelCanvas g = new PixelCanvas(W, H);
        int apron = rgba("6a4022"), apronDark = rgba("462a14"), beard = rgba("c8541e"), beardDark = rgba("8a3410");
        g.ellipseShaded(17f, 34f, 14f, 7f, TAN, TAN_SHADE, 5f);
        g.rect(3, 34, 31, 41, TAN);
        g.rect(23, 34, 31, 41, TAN_SHADE);
        g.rect(9, 30, 25, 41, apron);
        g.rect(21, 30, 25, 41, apronDark);
        g.line(10, 30, 13, 26, apronDark);
        g.line(24, 30, 21, 26, apronDark);
        face(g, TAN, TAN_SHADE);
        g.line(12, 13, 15, 14, EYE);
        g.line(19, 14, 22, 13, EYE);
        if (master) {
            g.ellipseShaded(17f, 24f, 6.5f, 5f, beard, beardDark, 2f);
            g.rect(14, 27, 15, 33, beard);
            g.rect(19, 27, 20, 33, beardDark);
            g.set(14, 30, GOLD); g.set(19, 30, GOLD);
            g.line(15, 21, 19, 21, MOUTH);
            // Casque d'acier à cornes.
            g.ellipseShaded(17f, 9f, 7.5f, 4.5f, STEEL, STEEL_DARK, 2f);
            g.rect(9, 10, 25, 11, STEEL_DARK);
            g.triangle(9, 9, 5, 9, 3, 1, WHITE);
            g.triangle(25, 9, 29, 9, 31, 1, rgba("d8d0c0"));
        } else {
            g.ellipse(17f, 9f, 7f, 3f, rgba("2a1a14"));
            g.line(14, 23, 20, 23, rgba("5a4030"));
        }
        // Marteau sur l'épaule droite.
        g.line(27, 32, 31, 12, WOOD);
        g.line(28, 32, 32, 12, WOOD_DARK);
        g.rect(27, 8, 33, 13, STEEL_DARK);
        g.rect(27, 8, 33, 9, STEEL);
        return g.outlined();
    }

    /**
     * Golem d'or : gros bloc d'or massif aux yeux rouges, fissuré. Le Cœur de
     * la Montagne est un golem de roche grise au cœur d'or qui brille.
     */
    private static int[][] golem(boolean mountain) {
        PixelCanvas g = new PixelCanvas(W, H);
        int body = mountain ? rgba("7a7480") : GOLD, bodyDark = mountain ? rgba("4e4a54") : GOLD_DARK;
        int crack = mountain ? rgba("34303a") : rgba("8a5a10"), eye = mountain ? GOLD_LIGHT : RED;
        // Épaules en blocs, torse massif.
        g.rect(1, 24, 9, 33, body);
        g.rect(25, 24, 33, 33, bodyDark);
        g.rect(6, 26, 28, 41, body);
        g.rect(19, 26, 28, 41, bodyDark);
        g.rect(2, 34, 8, 41, bodyDark);
        g.rect(26, 34, 32, 41, bodyDark);
        // Tête carrée, sans cou.
        g.rect(10, 8, 24, 24, body);
        g.rect(19, 8, 24, 24, bodyDark);
        g.rect(9, 12, 25, 13, crack);
        g.rect(12, 16, 15, 17, eye);
        g.rect(19, 16, 22, 17, eye);
        g.line(13, 21, 21, 21, crack);
        // Fissures.
        g.line(12, 9, 14, 12, crack);
        g.line(23, 25, 21, 31, crack);
        g.line(8, 36, 11, 39, crack);
        if (mountain) {
            // Cœur d'or au milieu de la poitrine, éclats de pépites.
            g.ellipse(14f, 32f, 2.5f, 2.5f, GOLD);
            g.ellipse(19f, 32f, 2.5f, 2.5f, GOLD);
            g.triangle(11, 33, 22, 33, 16, 39, GOLD);
            g.set(13, 31, GOLD_LIGHT); g.set(14, 31, GOLD_LIGHT);
            g.set(4, 28, GOLD); g.set(29, 37, GOLD); g.set(12, 22, GOLD);
            g.rect(14, 4, 20, 7, bodyDark);
            g.rect(16, 2, 18, 3, bodyDark);
        } else {
            g.set(8, 28, GOLD_LIGHT); g.set(9, 29, GOLD_LIGHT);
            g.set(12, 10, GOLD_LIGHT); g.set(13, 10, GOLD_LIGHT);
        }
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Le Casino Englouti
    // -------------------------------------------------------------------------

    /**
     * Barman noyé : peau bleu-vert, algues dans les cheveux, nœud papillon,
     * veste blanche trempée et un shaker ; des bulles s'échappent.
     */
    private static int[][] barman() {
        PixelCanvas g = new PixelCanvas(W, H);
        int skin = rgba("8ac8b0"), skinShade = rgba("5a9a84"), weed = rgba("2e7a3a"), bubble = rgba("c8f0ff");
        bust(g, rgba("dcdcd4"), rgba("a8aca4"), WHITE);
        g.rect(14, 28, 20, 30, EYE); // nœud papillon
        g.rect(16, 29, 18, 29, rgba("3a3a4a"));
        face(g, skin, skinShade);
        g.rect(13, 15, 14, 16, WHITE);
        g.rect(20, 15, 21, 16, WHITE);
        g.ellipse(17f, 9f, 7.5f, 3.5f, rgba("2a3a3a"));
        g.line(10, 10, 9, 17, weed);
        g.line(24, 10, 26, 18, weed);
        g.line(14, 7, 13, 11, weed);
        // Shaker et bulles.
        g.rect(26, 26, 30, 35, STEEL);
        g.rect(29, 26, 30, 35, STEEL_DARK);
        g.rect(27, 24, 29, 25, STEEL_DARK);
        g.set(4, 12, bubble); g.set(3, 8, bubble); g.set(5, 5, bubble); g.set(28, 9, bubble); g.set(30, 5, bubble);
        return g.outlined();
    }

    /**
     * Sirène : longs cheveux violets, coquillages, peau nacrée, collier de
     * perles ; elle chante, la bouche ouverte.
     */
    private static int[][] sirene() {
        PixelCanvas g = new PixelCanvas(W, H);
        int hair = rgba("8a3ac8"), hairDark = rgba("5a2088"), skin = rgba("f0d8e0"), skinShade = rgba("c8a8b8");
        int scale = rgba("2ab89a"), scaleDark = rgba("1a7a66"), shell = rgba("f8a8b8"), pearl = rgba("fff8f0");
        // Chevelure qui tombe dans le dos.
        g.ellipse(17f, 18f, 11f, 14f, hair);
        g.rect(6, 18, 28, 38, hair);
        g.rect(22, 18, 28, 38, hairDark);
        // Buste nacré, queue d'écailles en bas.
        g.ellipseShaded(17f, 34f, 9f, 6f, skin, skinShade, 3f);
        g.rect(8, 38, 26, 41, scale);
        g.rect(20, 38, 26, 41, scaleDark);
        for (int x = 9; x <= 25; x += 3) g.set(x, 39, scaleDark);
        g.ellipse(13f, 33f, 2.5f, 2f, shell);
        g.ellipse(21f, 33f, 2.5f, 2f, shell);
        for (int x = 12; x <= 22; x += 2) g.set(x, 29, pearl);
        g.rect(14, 24, 20, 28, skinShade);
        g.ellipseShaded(17f, 16f, 6.5f, 8f, skin, skinShade, 2f);
        g.rect(13, 15, 14, 16, rgba("2a8aa8"));
        g.rect(20, 15, 21, 16, rgba("2a8aa8"));
        g.ellipse(17f, 21f, 1.5f, 1.5f, MOUTH);
        // Frange et étoile de mer.
        g.ellipse(17f, 9f, 7f, 3f, hair);
        g.rect(10, 9, 11, 16, hair);
        g.rect(23, 9, 24, 16, hairDark);
        g.ellipse(24f, 7f, 2f, 2f, rgba("ff8a3a"));
        // Notes de musique.
        g.rect(29, 6, 29, 11, pearl); g.rect(27, 10, 29, 11, pearl); g.rect(29, 6, 31, 6, pearl);
        g.rect(4, 4, 4, 8, pearl); g.rect(2, 7, 4, 8, pearl);
        return g.outlined();
    }

    /**
     * Bandit manchot : une machine à sous rouillée, un œil dans chaque fenêtre
     * et un seul bras, le levier. Le Jackpot vivant est en or et affiche 777.
     */
    private static int[][] bandit(boolean jackpot) {
        PixelCanvas g = new PixelCanvas(W, H);
        int body = jackpot ? GOLD : rgba("9a5a3a"), bodyDark = jackpot ? GOLD_DARK : rgba("6a3a24");
        int rust = jackpot ? GOLD_LIGHT : rgba("c87a40"), window = rgba("f6f0e2");
        // Fronton arrondi et caisse.
        g.ellipseShaded(15f, 9f, 11f, 6f, body, bodyDark, 4f);
        g.rect(4, 9, 26, 41, body);
        g.rect(20, 9, 26, 41, bodyDark);
        g.rect(7, 4, 23, 7, jackpot ? RED : rgba("5a3a2a"));
        // Fenêtres.
        g.rect(6, 14, 24, 23, EYE);
        for (int i = 0; i < 3; i++) g.rect(7 + i * 6, 15, 11 + i * 6, 22, window);
        if (jackpot) {
            for (int i = 0; i < 3; i++) {
                int x = 7 + i * 6;
                g.rect(x, 16, x + 4, 16, RED);
                g.line(x + 4, 17, x + 2, 21, RED);
            }
            for (int x = 6; x <= 24; x += 3) g.set(x, 2, GOLD_LIGHT);
        } else {
            for (int i = 0; i < 3; i++) { g.rect(8 + i * 6, 17, 10 + i * 6, 20, EYE); g.set(9 + i * 6, 18, RED); }
            g.set(6, 28, rust); g.set(7, 29, rust); g.set(22, 33, rust); g.set(12, 38, rust);
        }
        // Bouche-fente à pièces, plateau.
        g.rect(10, 27, 20, 29, EYE);
        g.rect(11, 28, 19, 28, jackpot ? GOLD_LIGHT : STEEL_DARK);
        g.rect(4, 34, 26, 36, bodyDark);
        if (jackpot) for (int x = 8; x <= 22; x += 3) g.rect(x, 33, x + 1, 33, GOLD_LIGHT);
        // Le levier, son seul bras.
        g.rect(27, 24, 29, 27, STEEL_DARK);
        g.rect(29, 8, 30, 26, STEEL);
        g.ellipse(29.5f, 6f, 2.5f, 2.5f, RED);
        return g.outlined();
    }

    /**
     * Requin : tête de requin grise, dents en scie, costume rayé et cravate.
     * Le Requin banquier ajoute un haut-de-forme, un monocle et une pile de
     * pièces.
     */
    private static int[][] requin(boolean banker) {
        PixelCanvas g = new PixelCanvas(W, H);
        int skin = rgba("7a8aa0"), skinShade = rgba("4e5a6e"), belly = rgba("e0e4ec");
        int suit = banker ? rgba("2a2a3a") : rgba("3a4a6a"), suitDark = banker ? rgba("1a1a26") : rgba("26324a");
        bust(g, suit, suitDark, WHITE);
        for (int x = 6; x <= 28; x += 4) g.line(x, 32, x, 41, banker ? rgba("4a4a5a") : rgba("4e5e7e"));
        g.triangle(16, 28, 18, 28, 17, 36, banker ? RED : GOLD_DARK);
        // Tête de requin : museau pointu, ventre clair, mâchoire de scie.
        g.ellipseShaded(17f, 16f, 9f, 10f, skin, skinShade, 3f);
        g.triangle(8, 14, 26, 14, 17, 3, skin);
        g.ellipse(17f, 21f, 7f, 4f, belly);
        g.rect(11, 19, 23, 23, EYE);
        for (int x = 11; x <= 22; x += 2) { g.set(x, 19, WHITE); g.set(x + 1, 23, WHITE); }
        g.rect(10, 13, 12, 14, EYE);
        g.rect(22, 13, 24, 14, EYE);
        g.set(10, 13, WHITE);
        g.line(7, 17, 8, 19, skinShade);
        g.line(26, 17, 25, 19, skinShade);
        if (banker) {
            g.rect(11, 0, 23, 6, rgba("1a1a26"));
            g.rect(11, 5, 23, 5, RED);
            g.rect(8, 6, 26, 7, rgba("1a1a26"));
            g.ellipse(23f, 13.5f, 2.5f, 2.5f, GOLD_DARK);
            g.rect(22, 13, 24, 14, rgba("c8e6f0"));
            // Pile de pièces.
            for (int i = 0; i < 4; i++) g.rect(26, 38 - i * 2, 32, 39 - i * 2, i % 2 == 0 ? GOLD : GOLD_DARK);
        } else {
            // Aileron dorsal qui dépasse.
            g.triangle(23, 7, 30, 10, 30, 1, skinShade);
        }
        return g.outlined();
    }

    /**
     * Pieuvre croupière : pieuvre violette à visière verte de croupier, une
     * carte dans chaque tentacule. Le Kraken est rouge sombre, couronné, et ses
     * huit bras se tordent.
     */
    private static int[][] pieuvre(boolean kraken) {
        PixelCanvas g = new PixelCanvas(W, H);
        int body = kraken ? rgba("8a1e2a") : rgba("8a4ab8"), bodyDark = kraken ? rgba("5a0e18") : rgba("5a2a82");
        int sucker = kraken ? rgba("e8a0a0") : rgba("e0b8f0");
        // Tentacules.
        int arms = kraken ? 8 : 6;
        for (int i = 0; i < arms; i++) {
            float t = i / (float) (arms - 1);
            int x0 = 8 + Math.round(t * 18), x1 = 1 + Math.round(t * 32);
            int y1 = 41 - (i % 2) * 4;
            for (int d = -1; d <= 1; d++) g.line(x0 + d, 28, x1 + d, y1, i % 2 == 0 ? body : bodyDark);
            g.set(x1, y1 - 2, sucker);
            g.set((x0 + x1) / 2, (28 + y1) / 2, sucker);
        }
        // Tête en ogive.
        g.ellipseShaded(17f, 17f, 10f, 13f, body, bodyDark, 3f);
        g.ellipse(13f, 9f, 2f, 3f, sucker);
        // Gros yeux.
        g.ellipse(13f, 20f, 2.5f, 2.5f, WHITE);
        g.ellipse(21f, 20f, 2.5f, 2.5f, WHITE);
        g.rect(13, 20, 14, 21, EYE);
        g.rect(21, 20, 22, 21, EYE);
        if (kraken) {
            g.line(10, 17, 15, 18, EYE);
            g.line(24, 17, 19, 18, EYE);
            // Couronne d'or.
            g.rect(10, 4, 24, 6, GOLD);
            g.rect(19, 4, 24, 6, GOLD_DARK);
            for (int x : new int[] {10, 14, 17, 20, 24}) g.rect(x, 1, x, 3, GOLD);
            g.set(17, 5, RED);
        } else {
            // Visière verte de croupier.
            g.rect(7, 10, 27, 11, rgba("2e8a5a"));
            g.rect(7, 12, 27, 13, rgba("1e5a3a"));
            // Cartes tenues au bout des bras.
            g.rect(0, 30, 3, 35, WHITE); g.set(1, 32, RED);
            g.rect(30, 30, 33, 35, WHITE); g.set(31, 32, EYE);
        }
        return g.outlined();
    }
}
