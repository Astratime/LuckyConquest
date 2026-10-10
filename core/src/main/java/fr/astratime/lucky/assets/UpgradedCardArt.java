package fr.astratime.lucky.assets;

/**
 * Image d'une carte « + » : l'image de la carte, avec un liseré doré tout le
 * long de son bord (en suivant ses coins arrondis) et une pastille dorée « + »
 * dans son coin supérieur droit.
 *
 * Travaille sur des pixels RGBA8888 (un {@code int} par pixel, ligne par ligne,
 * de haut en bas) pour se passer de libGDX ; {@link CardTextures} s'en sert
 * pour créer la texture.
 */
public final class UpgradedCardArt {

    /** Épaisseur du liseré, en part de la largeur de la carte. */
    static final float BORDER = 0.04f;
    /** Rayon de la pastille « + », en part de la largeur de la carte. */
    static final float BADGE  = 0.12f;

    private static final int OUTLINE   = rgba(0x3a, 0x22, 0x00);
    /** Noir des contours du pixel art du jeu. */
    private static final int INK       = rgba(0x14, 0x0c, 0x08);
    private static final int GOLD_DARK = rgba(0xb0, 0x74, 0x0c);
    private static final int GOLD      = rgba(0xff, 0xd5, 0x4a);
    private static final int GOLD_LIGHT = rgba(0xff, 0xf3, 0xb8);

    /** @return les pixels de la carte « + » faite à partir des pixels {@code source} ({@code width} x {@code height}) */
    public static int[] gild(int[] source, int width, int height) {
        int[] pixels = source.clone();
        int border = Math.max(3, Math.round(width * BORDER));
        float[] distance = edgeDistance(source, width, height);
        for (int i = 0; i < pixels.length; i++) {
            if (alpha(source[i]) == 0) continue;
            float d = distance[i];
            if (d > border + 1) continue;
            pixels[i] = borderColor(d, border);
        }
        drawBadge(pixels, width, height, border);
        return pixels;
    }

    /** Couleur du liseré à {@code d} pixels du bord : contour sombre, or éclairé, or, or sombre, puis un trait sombre. */
    static int borderColor(float d, int border) {
        if (d <= 1.2f) return OUTLINE;
        if (d > border) return OUTLINE;
        float t = (d - 1.2f) / (border - 1.2f); // 0 au bord, 1 à l'intérieur
        if (t < 0.3f) return mix(GOLD, GOLD_LIGHT, t / 0.3f);
        if (t < 0.55f) return mix(GOLD_LIGHT, GOLD, (t - 0.3f) / 0.25f);
        return mix(GOLD, GOLD_DARK, (t - 0.55f) / 0.45f);
    }

    /**
     * @return pour chaque pixel, sa distance (en pixels, approchée par une
     *         distance de chanfrein 3-4) au plus proche pixel transparent ou au
     *         bord de l'image
     */
    static float[] edgeDistance(int[] pixels, int width, int height) {
        int far = 1 << 20;
        int[] d = new int[pixels.length];
        for (int i = 0; i < d.length; i++) d[i] = alpha(pixels[i]) == 0 ? 0 : far;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = y * width + x;
                if (d[i] == 0) continue;
                int best = d[i];
                best = Math.min(best, at(d, width, height, x - 1, y) + 3);
                best = Math.min(best, at(d, width, height, x, y - 1) + 3);
                best = Math.min(best, at(d, width, height, x - 1, y - 1) + 4);
                best = Math.min(best, at(d, width, height, x + 1, y - 1) + 4);
                d[i] = best;
            }
        }
        for (int y = height - 1; y >= 0; y--) {
            for (int x = width - 1; x >= 0; x--) {
                int i = y * width + x;
                if (d[i] == 0) continue;
                int best = d[i];
                best = Math.min(best, at(d, width, height, x + 1, y) + 3);
                best = Math.min(best, at(d, width, height, x, y + 1) + 3);
                best = Math.min(best, at(d, width, height, x + 1, y + 1) + 4);
                best = Math.min(best, at(d, width, height, x - 1, y + 1) + 4);
                d[i] = best;
            }
        }
        float[] distance = new float[d.length];
        for (int i = 0; i < d.length; i++) distance[i] = d[i] / 3f;
        return distance;
    }

    /** Distance déjà connue du pixel ({@code x}, {@code y}) ; hors de l'image, le bord : 0. */
    private static int at(int[] d, int width, int height, int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) return 0;
        return d[y * width + x];
    }

    /**
     * La pastille « + », dans le coin supérieur droit : un petit dessin en pixel
     * art ({@link #BADGE_CELLS} x {@link #BADGE_CELLS} gros pixels), comme les
     * images du jeu : contour noir épais, or éclairé en haut et sombre en bas,
     * « + » blanc cerné de noir.
     */
    private static void drawBadge(int[] pixels, int width, int height, int border) {
        int cell = Math.max(2, Math.round(width * BADGE * 2f / BADGE_CELLS));
        int size = cell * BADGE_CELLS;
        int left = width - size - Math.max(0, border / 3);
        int top  = Math.max(0, border / 3);
        for (int row = 0; row < BADGE_CELLS; row++) {
            for (int col = 0; col < BADGE_CELLS; col++) {
                int color = badgeCell(row, col);
                if (color == 0) continue;
                for (int y = top + row * cell; y < top + (row + 1) * cell && y < height; y++) {
                    for (int x = left + col * cell; x < left + (col + 1) * cell && x < width; x++) {
                        if (x >= 0) pixels[y * width + x] = color;
                    }
                }
            }
        }
    }

    /** Côté de la pastille, en gros pixels. */
    static final int BADGE_CELLS = 14;

    /** @return la couleur du gros pixel ({@code row}, {@code col}) de la pastille, 0 en dehors */
    static int badgeCell(int row, int col) {
        float center = (BADGE_CELLS - 1) / 2f;
        float dx = col - center, dy = row - center;
        float r = (float) Math.sqrt(dx * dx + dy * dy);
        if (r > center + 0.45f) return 0;
        if (r > center - 0.75f) return INK; // contour d'un gros pixel
        if (isCross(dx, dy)) return 0xffffffff;
        if (isCross(dx + 1, dy) || isCross(dx - 1, dy) || isCross(dx, dy + 1) || isCross(dx, dy - 1)) return INK;
        if (dy <= -4f || dy <= -3f && dx <= -2f) return GOLD_LIGHT;
        if (dy >= 4f) return GOLD_DARK;
        return GOLD;
    }

    /** @return {@code true} pour les gros pixels du « + » : deux barres de 2 de large et 6 de long. */
    private static boolean isCross(float dx, float dy) {
        return Math.abs(dx) <= 0.5f && Math.abs(dy) <= 3f || Math.abs(dy) <= 0.5f && Math.abs(dx) <= 3f;
    }

    private static int alpha(int rgba) { return rgba & 0xff; }

    private static int rgba(int r, int g, int b) { return r << 24 | g << 16 | b << 8 | 0xff; }

    /** @return la couleur entre {@code a} (t = 0) et {@code b} (t = 1), opaque. */
    static int mix(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = Math.round((a >>> 24) + ((b >>> 24) - (a >>> 24)) * t);
        int g = Math.round((a >>> 16 & 0xff) + ((b >>> 16 & 0xff) - (a >>> 16 & 0xff)) * t);
        int bl = Math.round((a >>> 8 & 0xff) + ((b >>> 8 & 0xff) - (a >>> 8 & 0xff)) * t);
        return rgba(r, g, bl);
    }

    private UpgradedCardArt() {}
}
