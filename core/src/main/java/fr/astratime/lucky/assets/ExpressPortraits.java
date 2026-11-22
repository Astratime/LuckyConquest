package fr.astratime.lucky.assets;

import fr.astratime.lucky.entities.enemy.EnemyKind;

import static fr.astratime.lucky.assets.PixelCanvas.rgba;

/**
 * Portraits en pixel art des ennemis du Rapide de Minuit, en buste, à la taille
 * du croupier (voir {@link PlacesPortraits}).
 */
public final class ExpressPortraits {

    private static final int W = EnemyPortraits.WIDTH, H = EnemyPortraits.HEIGHT;

    private static final int SKIN = rgba("f0c8a0"), SKIN_SHADE = rgba("c89a72");
    private static final int PALE = rgba("eee6f0"), PALE_SHADE = rgba("bcb0c8");
    private static final int EYE = rgba("1a1018"), MOUTH = rgba("7a2a20"), WHITE = rgba("f6f0e2"), WHITE_SHADE = rgba("c8c0b4");
    private static final int GOLD = rgba("ffd54a"), GOLD_DARK = rgba("c4891e");
    private static final int RED = rgba("d8202c"), RED_DARK = rgba("8a1a1a");
    private static final int COAL = rgba("26262e"), COAL_LIGHT = rgba("4a4a58"), SOOT = rgba("6a5a5a");
    private static final int STEEL = rgba("dce4ee"), STEEL_DARK = rgba("8a97a8");

    private ExpressPortraits() {}

    /** @return les pixels du portrait de l'ennemi {@code kind}, ou {@code null} s'il n'est pas du Rapide. */
    public static int[][] pixels(EnemyKind kind) { return of(kind); }

    /** Ce qu'une cinématique peut retirer (ou éteindre) sur un portrait (voir {@link #without}). */
    public enum Prop { COWCATCHER, LIGHTS, TOQUE, LADLE, VEIL, ROSE, HAT }

    /** Les accessoires retirés du portrait en cours de dessin (vide hors de {@link #without}). */
    private static java.util.Set<Prop> gone = java.util.EnumSet.noneOf(Prop.class);

    private static boolean has(Prop prop) { return !gone.contains(prop); }

    /**
     * @return le portrait de {@code kind} dessiné sans les accessoires {@code props} (ce qu'il y avait dessous
     * réapparaît ; {@link Prop#LIGHTS} : ses lumières éteintes) : les cinématiques en tirent le vrai morceau qui
     * quitte le boss, jamais une copie.
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

    /** @return le portrait de l'ennemi {@code kind}, ou {@code null} s'il n'est pas du Rapide de Minuit. */
    static int[][] of(EnemyKind kind) {
        return switch (kind) {
            case CHAUFFEUR    -> chauffeur();
            case LOCOMOTIVE   -> locomotive();
            case SERVEUR      -> serveur();
            case GRAND_CHEF   -> grandChef();
            case SOMNAMBULE   -> somnambule();
            case DAME_EN_NOIR -> dameEnNoir();
            case CONVOYEUR    -> convoyeur();
            case DESPERADO    -> desperado();
            default           -> null;
        };
    }

    // -------------------------------------------------------------------------
    // Gabarit commun (comme FairDesertPortraits)
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

    /** Casquette plate {@code cap} à visière {@code peak}, un insigne {@code badge} (0 : aucun). */
    private static void cap(PixelCanvas g, int cap, int peak, int badge) {
        g.ellipse(17f, 7f, 8f, 4f, cap);
        g.rect(9, 7, 25, 9, cap);
        g.rect(8, 9, 26, 10, peak);
        if (badge != 0) { g.rect(16, 5, 18, 7, badge); g.set(17, 6, WHITE); }
    }

    // -------------------------------------------------------------------------
    // La Locomotive
    // -------------------------------------------------------------------------

    /** Chauffeur de chaudière : bleu de chauffe, visage noirci de suie, casquette, foulard rouge, pelle. */
    private static int[][] chauffeur() {
        PixelCanvas g = new PixelCanvas(W, H);
        int overall = rgba("2a4a7a"), overallDark = rgba("1a3054");
        bust(g, overall, overallDark, 0);
        g.rect(11, 29, 13, 41, overallDark);                 // bretelles
        g.rect(21, 29, 23, 41, overallDark);
        g.triangle(12, 27, 22, 27, 17, 32, RED);              // foulard
        face(g, SKIN, SKIN_SHADE);
        for (int[] p : new int[][] {{12, 19}, {13, 20}, {21, 12}, {22, 18}, {16, 23}, {19, 10}}) g.set(p[0], p[1], SOOT);
        g.line(12, 13, 15, 13, COAL);
        g.line(19, 13, 22, 13, COAL);
        cap(g, rgba("3a3a4e"), COAL, 0);
        // La pelle, sur l'épaule gauche.
        g.line(28, 4, 30, 30, rgba("8a5a2a"));
        g.rect(26, 0, 32, 4, STEEL_DARK);
        g.rect(27, 1, 31, 3, STEEL);
        g.set(29, 2, rgba("ff6a20"));                         // une braise sur la pelle
        return g.outlined();
    }

    /**
     * La Locomotive vivante : l'avant d'une machine noire vue de face, sa porte de boîte à fumée en guise de
     * visage (deux hublots pour yeux, une grille qui sourit), la lanterne au front, la cheminée qui crache.
     */
    private static int[][] locomotive() {
        PixelCanvas g = new PixelCanvas(W, H);
        // Le châssis rouge et le chasse-buffles, en bas.
        if (has(Prop.COWCATCHER)) {
            g.rect(3, 34, 30, 37, RED_DARK);
            g.triangle(5, 37, 29, 37, 17, 41, RED);
            for (int x = 9; x <= 25; x += 4) g.line(x, 37, 17 + (x - 17) / 3, 41, RED_DARK);
        }
        // La chaudière vue de face : un grand disque noir cerclé de laiton.
        g.ellipseShaded(17f, 22f, 13f, 12.5f, COAL_LIGHT, COAL, 3f);
        for (int a = 0; a < 360; a += 4) {
            double r = Math.toRadians(a);
            g.set((int) Math.round(17 + 12 * Math.cos(r)), (int) Math.round(22 - 11.5 * Math.sin(r)), GOLD_DARK);
        }
        // Les yeux : deux hublots qui brillent, des sourcils de rivets froncés.
        for (int cx : new int[] {11, 23}) {
            g.ellipse(cx, 19f, 3f, 3f, STEEL_DARK);
            g.ellipse(cx, 19f, 2f, 2f, has(Prop.LIGHTS) ? rgba("ffd27a") : COAL_LIGHT);
            g.set(cx, 19, has(Prop.LIGHTS) ? WHITE : COAL);
        }
        g.line(7, 14, 13, 16, GOLD);
        g.line(27, 14, 21, 16, GOLD);
        // La grille de la porte, comme une bouche qui sourit.
        g.rect(10, 26, 24, 30, COAL);
        for (int x = 11; x <= 23; x += 2) g.line(x, 26, x, 30, STEEL_DARK);
        g.set(9, 25, COAL); g.set(25, 25, COAL);
        // La lanterne au front et la cheminée au-dessus.
        g.rect(14, 8, 20, 11, GOLD_DARK);
        g.rect(15, 9, 19, 10, has(Prop.LIGHTS) ? rgba("fff2a0") : COAL_LIGHT);
        g.rect(13, 1, 21, 6, COAL);
        g.rect(12, 0, 22, 1, COAL_LIGHT);
        g.ellipse(6f, 3f, 3f, 2.5f, rgba("8a8a96"));         // la fumée
        g.ellipse(28f, 4f, 3.5f, 2.5f, rgba("6a6a78"));
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Le Wagon-restaurant
    // -------------------------------------------------------------------------

    /** Serveur pickpocket : veste blanche, nœud papillon, cheveux gominés, une montre volée qui dépasse de sa poche. */
    private static int[][] serveur() {
        PixelCanvas g = new PixelCanvas(W, H);
        bust(g, WHITE, WHITE_SHADE, COAL);
        g.rect(15, 28, 19, 30, COAL);                         // nœud papillon
        g.set(17, 29, RED);
        for (int y = 32; y <= 40; y += 3) g.set(17, y, GOLD);  // boutons
        g.line(24, 34, 27, 36, GOLD);                          // la chaîne de la montre volée
        g.ellipse(27f, 37f, 1.5f, 1.5f, GOLD);
        face(g, SKIN, SKIN_SHADE);
        g.ellipse(17f, 9f, 7.5f, 3.5f, rgba("2a1a14"));       // cheveux gominés, raie au milieu
        g.line(17, 6, 17, 9, rgba("5a3a2a"));
        g.line(14, 21, 19, 20, MOUTH);                         // le sourire en biais
        g.line(13, 13, 15, 14, rgba("2a1a14"));
        g.line(19, 14, 21, 13, rgba("2a1a14"));
        // Le plateau, tenu haut sur la main droite.
        g.rect(0, 22, 9, 23, STEEL_DARK);
        g.rect(1, 21, 8, 21, STEEL);
        g.ellipse(4f, 20f, 1.5f, 1.5f, RED);
        g.line(4, 24, 5, 29, SKIN);
        return g.outlined();
    }

    /** Le Grand Chef : la toque haute, la veste croisée, la moustache, une louche dorée. */
    private static int[][] grandChef() {
        PixelCanvas g = new PixelCanvas(W, H);
        bust(g, WHITE, WHITE_SHADE, 0);
        for (int y = 31; y <= 40; y += 3) { g.set(13, y, GOLD); g.set(21, y, GOLD); }
        g.triangle(14, 27, 20, 27, 17, 31, RED);               // foulard
        face(g, rgba("f4c098"), SKIN_SHADE);
        g.ellipse(12f, 20.5f, 4f, 1.6f, rgba("6a4a3a"));       // la moustache
        g.ellipse(22f, 20.5f, 4f, 1.6f, rgba("6a4a3a"));
        g.set(7, 19, rgba("6a4a3a")); g.set(27, 19, rgba("6a4a3a"));
        g.ellipse(11f, 18f, 1.5f, 1f, rgba("ff9a8a"));         // les joues rouges
        g.ellipse(23f, 18f, 1.5f, 1f, rgba("ff9a8a"));
        if (!has(Prop.TOQUE)) g.ellipse(17f, 9f, 7f, 2.5f, rgba("6a4a3a"));   // sans la toque : ses cheveux
        if (has(Prop.TOQUE)) {                                  // la toque
            g.rect(10, 6, 24, 10, WHITE);
            g.ellipse(12f, 3f, 4f, 3.5f, WHITE);
            g.ellipse(17f, 2f, 4.5f, 3f, WHITE);
            g.ellipse(22f, 3f, 4f, 3.5f, WHITE_SHADE);
            g.line(10, 9, 24, 9, WHITE_SHADE);
        }
        if (has(Prop.LADLE)) {                                  // la louche dorée, levée
            g.line(30, 8, 29, 30, GOLD_DARK);
            g.ellipse(30f, 7f, 2.5f, 2f, GOLD);
        }
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Le Wagon-lit
    // -------------------------------------------------------------------------

    /** Somnambule : chemise de nuit rayée, bonnet à pompon, yeux fermés, bras tendus, une bougie. */
    private static int[][] somnambule() {
        PixelCanvas g = new PixelCanvas(W, H);
        int gown = rgba("c8d8f0"), gownDark = rgba("8aa0c8"), stripe = rgba("6a80b8");
        bust(g, gown, gownDark, 0);
        for (int x = 6; x <= 28; x += 4) g.line(x, 30, x, 41, stripe);
        face(g, PALE, PALE_SHADE);
        g.rect(13, 15, 14, 16, PALE);                          // les yeux fermés
        g.rect(20, 15, 21, 16, PALE);
        g.line(12, 16, 15, 16, EYE);
        g.line(19, 16, 22, 16, EYE);
        g.ellipse(17f, 21f, 1.2f, 1f, MOUTH);                  // la bouche qui ronfle
        // Le bonnet de nuit, qui tombe sur le côté, et son pompon.
        g.triangle(9, 10, 25, 10, 30, 2, rgba("3a5ab8"));
        g.rect(9, 9, 25, 10, WHITE);
        g.ellipse(30f, 3f, 2f, 2f, WHITE);
        // Les bras tendus devant lui, et la bougie.
        g.rect(0, 26, 8, 28, gown);
        g.rect(0, 26, 2, 28, PALE);
        g.rect(2, 21, 3, 25, WHITE);
        g.set(2, 20, rgba("ffb020")); g.set(2, 19, rgba("fff2a0"));
        return g.outlined();
    }

    /** La Dame en noir : robe noire à col haut, chapeau à voilette, visage pâle, lèvres rouges, perles. */
    private static int[][] dameEnNoir() {
        PixelCanvas g = new PixelCanvas(W, H);
        int dress = rgba("1a1420"), dressLight = rgba("3a2e4a"), veil = rgba("2a2236");
        bust(g, dress, rgba("120e18"), 0);
        g.rect(13, 26, 21, 29, dressLight);                    // le col haut
        for (int x = 11; x <= 23; x += 2) g.set(x, 31 + Math.abs(17 - x) / 3, WHITE); // le collier de perles
        face(g, PALE, PALE_SHADE);
        g.line(12, 13, 15, 14, EYE);                           // des sourcils fins
        g.line(19, 14, 22, 13, EYE);
        g.ellipse(17f, 21f, 2f, 1f, RED);                      // les lèvres rouges
        g.set(17, 21, RED_DARK);
        // Les cheveux noirs en chignon et le chapeau à voilette qui tombe sur les yeux.
        g.ellipse(17f, 9f, 8f, 4f, dress);
        g.ellipse(9f, 15f, 2f, 5f, dress);
        g.ellipse(25f, 15f, 2f, 5f, dress);
        g.rect(8, 4, 26, 6, dress);
        g.ellipse(17f, 3f, 5f, 2.5f, dress);
        if (has(Prop.ROSE)) g.ellipse(23f, 2f, 2f, 1.5f, RED_DARK);   // une rose sur le chapeau
        if (has(Prop.VEIL)) for (int y = 7; y <= 17; y += 2) for (int x = 10 + (y % 4 == 1 ? 1 : 0); x <= 24; x += 2) {
            if (g.get(x, y) != 0 && y > 12) continue;
            g.set(x, y, veil);
        }
        g.rect(13, 15, 14, 16, rgba("b8a0ff"));                // les yeux qui luisent sous la voilette
        g.rect(20, 15, 21, 16, rgba("b8a0ff"));
        return g.outlined();
    }

    // -------------------------------------------------------------------------
    // Le Fourgon blindé
    // -------------------------------------------------------------------------

    /** Convoyeur : uniforme bleu de la compagnie, casquette à insigne doré, gilet blindé, trousseau de clés. */
    private static int[][] convoyeur() {
        PixelCanvas g = new PixelCanvas(W, H);
        int uniform = rgba("1e3a6a"), uniformDark = rgba("122648");
        bust(g, uniform, uniformDark, WHITE);
        g.rect(8, 31, 26, 41, rgba("5a6070"));                 // le gilet blindé
        g.rect(19, 31, 26, 41, rgba("40444e"));
        for (int x = 10; x <= 24; x += 4) g.set(x, 33, STEEL);
        face(g, SKIN, SKIN_SHADE);
        g.line(12, 13, 15, 13, rgba("3a2a1e"));
        g.line(19, 13, 22, 13, rgba("3a2a1e"));
        g.line(15, 21, 19, 21, rgba("3a2a1e"));                // un pli sévère
        g.ellipse(17f, 23f, 4f, 1.5f, rgba("3a2a1e"));         // la barbe courte
        cap(g, uniform, COAL, GOLD);
        // Le trousseau de clés, à la ceinture.
        g.ellipse(28f, 38f, 2f, 2f, GOLD_DARK);
        g.ellipse(28f, 38f, 1f, 1f, 0);
        g.line(29, 39, 31, 41, GOLD);
        g.line(27, 40, 26, 41, GOLD);
        return g.outlined();
    }

    /** Le Desperado : chapeau de cow-boy, foulard rouge sur le bas du visage, yeux plissés, revolver levé. */
    private static int[][] desperado() {
        PixelCanvas g = new PixelCanvas(W, H);
        int duster = rgba("8a6a4a"), dusterDark = rgba("5e4630"), hat = rgba("4a3424"), hatDark = rgba("2e2016");
        bust(g, duster, dusterDark, 0);
        g.line(8, 30, 26, 40, rgba("3a2a1e"));                  // la cartouchière
        for (int i = 0; i <= 6; i++) g.set(9 + i * 3, 31 + (int) Math.round(i * 1.6), GOLD);
        face(g, rgba("d8a070"), rgba("a8784a"));
        g.line(12, 15, 15, 15, EYE);                             // les yeux plissés
        g.line(19, 15, 22, 15, EYE);
        g.line(12, 13, 15, 14, hatDark);
        g.line(19, 14, 22, 13, hatDark);
        // Le foulard rouge sur le nez et la bouche, noué derrière.
        g.rect(10, 18, 24, 25, RED);
        g.rect(20, 18, 24, 25, RED_DARK);
        g.triangle(10, 25, 24, 25, 17, 29, RED);
        for (int x = 12; x <= 22; x += 3) g.set(x, 21, WHITE);
        if (!has(Prop.HAT)) g.ellipse(17f, 9f, 7f, 2.5f, rgba("3a2a1e"));   // sans le chapeau : ses cheveux
        if (has(Prop.HAT)) {                                    // le chapeau à larges bords
            g.rect(11, 3, 23, 9, hat);
            g.rect(15, 3, 19, 4, hatDark);
            g.rect(4, 9, 30, 10, hat);
            g.set(4, 8, hat); g.set(30, 8, hat);
            g.line(11, 8, 23, 8, hatDark);
        }
        // Le revolver, levé à droite.
        g.rect(28, 12, 32, 13, STEEL);
        g.rect(28, 13, 29, 18, rgba("6a4420"));
        g.line(29, 19, 30, 29, rgba("d8a070"));
        return g.outlined();
    }
}
