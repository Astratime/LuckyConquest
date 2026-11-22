package fr.astratime.lucky.assets;

import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.exploration.Rival;

import static fr.astratime.lucky.assets.PixelCanvas.rgba;

/**
 * Portraits en pixel art du Joueur Masqué (voir {@link Rival}), en buste, à la taille du croupier :
 * redingote violette, chapeau plat avec une carte glissée dans le ruban, demi-masque doré aux yeux
 * qui brillent. À chaque rencontre, il porte une broche de plus sur le revers, gagnée dans le lieu
 * traversé (ticket, roue de locomotive, ancre, flocon, pépite, jeton, dé). Au Désert, il a ôté son masque et le tient à la main.
 */
public final class RivalArt {

    private static final int W = EnemyPortraits.WIDTH, H = EnemyPortraits.HEIGHT;

    private static final int COAT = rgba("4a1e6a"), COAT_DARK = rgba("2e1046"), COAT_LIGHT = rgba("6a3a8e");
    private static final int SHIRT = rgba("ece6da"), SHIRT_SHADE = rgba("c8c0b4");
    private static final int SKIN = rgba("e8bc94"), SKIN_SHADE = rgba("b88a64");
    private static final int HAIR = rgba("1e1424"), HAT = rgba("16101e"), HAT_LIGHT = rgba("3a2e4a"), BAND = rgba("1fb8a8");
    private static final int GOLD = rgba("ffd54a"), GOLD_DARK = rgba("c4891e"), GOLD_LIGHT = rgba("fff2a0");
    private static final int EYE_GLOW = rgba("4af0d8"), EYE = rgba("1a1018"), MOUTH = rgba("6a2020");
    private static final int CARD = rgba("f6f0e2"), RED = rgba("d8202c"), SCAR = rgba("c86a6a");

    private RivalArt() {}

    /** @return le portrait de la rencontre {@code kind} du Joueur Masqué, ou {@code null} si ce n'est pas lui. */
    static int[][] of(EnemyKind kind) {
        int encounter = Rival.encounter(kind);
        if (encounter == 0) return null;
        return portrait(encounter, Rival.isFinal(kind));
    }

    /** @return le portrait de la rencontre {@code encounter} (1 à 7), {@code unmasked} au Désert. */
    public static int[][] portrait(int encounter, boolean unmasked) {
        PixelCanvas g = new PixelCanvas(W, H);
        // Redingote aux épaules carrées, revers clairs, chemise et lavallière.
        g.rect(3, 29, 30, 41, COAT);
        g.rect(19, 29, 30, 41, COAT_DARK);
        g.ellipseShaded(7f, 31f, 4.5f, 3.5f, COAT, COAT_DARK, 3f);
        g.ellipseShaded(26f, 31f, 4.5f, 3.5f, COAT_DARK, COAT_DARK, 0f);
        g.triangle(12, 28, 22, 28, 17, 40, SHIRT);
        g.triangle(17, 28, 22, 28, 17, 40, SHIRT_SHADE);
        g.triangle(9, 29, 13, 28, 15, 39, COAT_LIGHT);   // revers
        g.triangle(25, 29, 21, 28, 19, 39, COAT_DARK);
        g.rect(15, 28, 19, 30, BAND);                    // lavallière turquoise
        g.rect(16, 31, 18, 33, BAND);
        g.set(17, 29, rgba("0f7a70"));
        // Broches gagnées en route, sur le revers gauche : une par lieu traversé.
        int[][] pins = {{10, 32}, {11, 35}, {12, 38}, {8, 36}, {8, 39}, {11, 40}, {6, 33}};
        for (int i = 0; i < Math.min(encounter - 1, pins.length); i++) pin(g, i, pins[i][0], pins[i][1]);
        // Cou, visage, cheveux.
        g.rect(14, 24, 20, 28, SKIN_SHADE);
        g.ellipseShaded(17f, 16f, 7f, 8.5f, SKIN, SKIN_SHADE, 2f);
        g.ellipse(10.5f, 13f, 2f, 4f, HAIR);
        g.ellipse(23.5f, 13f, 2f, 4f, HAIR);
        if (unmasked) {
            // Le visage nu : des yeux fatigués, une cicatrice, un sourire en coin.
            g.rect(13, 15, 14, 16, EYE);
            g.rect(20, 15, 21, 16, EYE);
            g.line(12, 13, 15, 14, HAIR);
            g.line(19, 14, 22, 13, HAIR);
            g.set(13, 17, SKIN_SHADE); g.set(21, 17, SKIN_SHADE);   // cernes
            g.line(22, 11, 20, 19, SCAR);
            g.set(17, 18, SKIN_SHADE);
            g.line(15, 21, 18, 21, MOUTH);
            g.set(19, 20, MOUTH);
            // Le masque, tenu à la main en bas à droite.
            g.ellipse(28f, 39f, 3f, 2.5f, SKIN);
            mask(g, 27, 33, true);
        } else {
            // Le demi-masque doré, et ses yeux qui brillent.
            mask(g, 17, 15, false);
            g.ellipse(17f, 20.5f, 1f, 0.5f, SKIN_SHADE);
            g.line(14, 22, 19, 21, MOUTH);                  // le sourire en coin
            g.set(20, 21, MOUTH);
        }
        // Chapeau plat à large bord, une carte glissée dans le ruban.
        g.rect(10, 2, 24, 7, HAT);
        g.rect(10, 6, 24, 7, BAND);
        g.rect(6, 8, 28, 9, HAT);
        g.rect(11, 3, 11, 5, HAT_LIGHT);
        g.rect(19, 0, 25, 6, CARD);                       // une carte retournée : son dos, sans chiffre
        g.rect(20, 1, 24, 5, rgba("2a5ad8"));
        for (int y = 1; y <= 5; y++) for (int x = 20; x <= 24; x++) if ((x + y) % 2 == 0) g.set(x, y, rgba("1a3a8e"));
        return g.outlined();
    }

    /** Le demi-masque doré, centré sur ({@code cx}, {@code cy}) ; {@code small} : tenu à la main, plus petit. */
    private static void mask(PixelCanvas g, int cx, int cy, boolean small) {
        if (small) {
            g.rect(cx - 4, cy, cx + 4, cy + 2, GOLD);
            g.rect(cx - 4, cy + 2, cx + 4, cy + 2, GOLD_DARK);
            g.set(cx - 2, cy + 1, EYE); g.set(cx + 2, cy + 1, EYE);
            g.set(cx - 3, cy, GOLD_LIGHT);
            return;
        }
        g.rect(cx - 7, cy - 3, cx + 7, cy + 2, GOLD);
        g.rect(cx + 3, cy - 3, cx + 7, cy + 2, GOLD_DARK);
        g.triangle(cx - 9, cy - 5, cx - 6, cy - 3, cx - 7, cy + 1, GOLD);       // les pointes du masque
        g.triangle(cx + 9, cy - 5, cx + 6, cy - 3, cx + 7, cy + 1, GOLD_DARK);
        g.line(cx - 6, cy - 3, cx - 1, cy - 3, GOLD_LIGHT);
        g.rect(cx - 5, cy - 1, cx - 2, cy, EYE);                                 // trous des yeux
        g.rect(cx + 2, cy - 1, cx + 5, cy, EYE);
        g.set(cx - 4, cy - 1, EYE_GLOW); g.set(cx - 3, cy, EYE_GLOW);
        g.set(cx + 3, cy - 1, EYE_GLOW); g.set(cx + 4, cy, EYE_GLOW);
        g.set(cx, cy + 1, GOLD_DARK);                                            // l'arête du nez
    }

    /** La broche du lieu {@code index} (0 Foire, 1 Rapide, 2 Port, 3 Palace, 4 Mines, 5 Casino, 6 Temple) en ({@code x}, {@code y}). */
    private static void pin(PixelCanvas g, int index, int x, int y) {
        switch (index) {
            case 0 -> { g.rect(x - 1, y, x + 1, y + 1, RED); g.set(x, y, GOLD_LIGHT); }                    // ticket
            case 1 -> { g.ellipse(x, y, 1.3f, 1.3f, rgba("26262e")); g.set(x, y, GOLD); }                  // roue de locomotive
            case 2 -> { g.line(x, y - 1, x, y + 1, rgba("b8c0cc")); g.line(x - 1, y + 1, x + 1, y + 1, rgba("b8c0cc")); } // ancre
            case 3 -> { g.line(x - 1, y, x + 1, y, rgba("bfe8ff")); g.line(x, y - 1, x, y + 1, rgba("bfe8ff")); g.set(x, y, CARD); } // flocon
            case 4 -> { g.ellipse(x, y, 1.2f, 1f, GOLD); g.set(x, y, GOLD_LIGHT); }                       // pépite
            case 6 -> { g.rect(x - 1, y - 1, x + 1, y + 1, CARD); g.set(x, y, RED); }                        // dé du Temple
            default -> { g.ellipse(x, y, 1.3f, 1.3f, rgba("2a5ad8")); g.set(x, y, CARD); }               // jeton
        }
    }
}
