package fr.astratime.lucky.assets;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Option « Enseignes renforcées » (accessibilité) : un jeu à quatre couleurs.
 * Le Carreau passe en bleu pour ne plus se confondre avec le Cœur (rouge), et
 * le Trèfle en vert pour ne plus se confondre avec le Pique (noir).
 *
 * Seules les cartes classiques (cards/light/ du joueur, cards/dark/ de
 * l'ennemi) changent : leurs enseignes sont peintes avec quelques couleurs
 * exactes, remplacées une à une. Travaille sur des pixels RGBA8888 comme
 * {@link UpgradedCardArt}, pour se passer de libGDX.
 */
public final class SuitColors {

    /** Image d'une carte classique : son jeu (light ou dark) et la lettre de son enseigne. */
    private static final Pattern CLASSIC = Pattern.compile("cards/(light|dark)/[^/]+-([CDHP])\\.png");

    private static final int RED        = rgba(172, 50, 50);
    private static final int RED_SHADE  = rgba(153, 47, 47);
    private static final int BLUE       = rgba(48, 112, 220);
    private static final int BLUE_SHADE = rgba(36, 86, 182);

    /** Encre des Trèfles du jeu clair, et gris (et son ombre) des Trèfles du jeu sombre. */
    private static final int INK         = rgba(34, 32, 52);
    private static final int SILVER      = rgba(204, 204, 204);
    private static final int SILVER_SHADE = rgba(155, 173, 183);
    private static final int GREEN       = rgba(30, 128, 66);
    private static final int LIGHT_GREEN = rgba(112, 210, 124);
    private static final int GREEN_SHADE = rgba(70, 164, 90);

    private static final Map<Integer, Integer> DIAMONDS    = Map.of(RED, BLUE, RED_SHADE, BLUE_SHADE);
    private static final Map<Integer, Integer> LIGHT_CLUBS = Map.of(INK, GREEN);
    private static final Map<Integer, Integer> DARK_CLUBS  = Map.of(SILVER, LIGHT_GREEN, SILVER_SHADE, GREEN_SHADE);

    private SuitColors() { }

    /** @return true si l'image {@code path} change de couleurs avec l'option (Carreaux et Trèfles classiques). */
    public static boolean recolors(String path) {
        return palette(path) != null;
    }

    /**
     * @return les pixels {@code source} de l'image {@code path}, enseignes recolorées ;
     *         {@code source} lui-même si l'image ne change pas
     */
    public static int[] recolor(String path, int[] source) {
        Map<Integer, Integer> palette = palette(path);
        if (palette == null) return source;
        int[] pixels = source.clone();
        for (int i = 0; i < pixels.length; i++) pixels[i] = palette.getOrDefault(pixels[i], pixels[i]);
        return pixels;
    }

    private static Map<Integer, Integer> palette(String path) {
        Matcher matcher = CLASSIC.matcher(path.replace('\\', '/'));
        if (!matcher.find()) return null;
        boolean light = matcher.group(1).equals("light");
        return switch (matcher.group(2)) {
            case "D" -> DIAMONDS;
            case "C" -> light ? LIGHT_CLUBS : DARK_CLUBS;
            default  -> null;
        };
    }

    private static int rgba(int r, int g, int b) { return r << 24 | g << 16 | b << 8 | 0xff; }
}
