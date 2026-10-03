package fr.astratime.lucky.assets;

/**
 * Grille de pixels (couleurs RGBA8888, 0 = transparent) sur laquelle on dessine
 * en pixel art avec quelques formes simples, avant d'en faire une texture
 * (voir {@link EnemyTextures}). Sans libGDX : les dessins se vérifient aussi
 * hors du jeu.
 */
final class PixelCanvas {

    /** Contour sombre des dessins (même teinte que {@link Palette#OUTLINE}). */
    static final int OUTLINE = rgba("1a0f0f");

    final int     width;
    final int     height;
    final int[][] pixels;

    PixelCanvas(int width, int height) {
        this.width  = width;
        this.height = height;
        this.pixels = new int[height][width];
    }

    /** Colore le pixel ({@code x}, {@code y}) s'il est dans la grille (y vers le bas). */
    void set(int x, int y, int color) {
        if (x >= 0 && y >= 0 && x < width && y < height) pixels[y][x] = color;
    }

    /** @return la couleur du pixel ({@code x}, {@code y}), 0 hors de la grille. */
    int get(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height ? pixels[y][x] : 0;
    }

    /** Rectangle plein, de ({@code x0}, {@code y0}) à ({@code x1}, {@code y1}) inclus. */
    void rect(int x0, int y0, int x1, int y1, int color) {
        for (int y = y0; y <= y1; y++) for (int x = x0; x <= x1; x++) set(x, y, color);
    }

    /** Ellipse pleine de centre ({@code cx}, {@code cy}) et de rayons {@code rx}, {@code ry}. */
    void ellipse(float cx, float cy, float rx, float ry, int color) {
        for (int y = (int) Math.floor(cy - ry); y <= Math.ceil(cy + ry); y++) {
            for (int x = (int) Math.floor(cx - rx); x <= Math.ceil(cx + rx); x++) {
                float dx = (x - cx) / rx, dy = (y - cy) / ry;
                if (dx * dx + dy * dy <= 1f) set(x, y, color);
            }
        }
    }

    /** Ellipse pleine, ombrée de {@code shade} sur sa droite (au-delà de {@code cx + shadeFrom}). */
    void ellipseShaded(float cx, float cy, float rx, float ry, int color, int shade, float shadeFrom) {
        for (int y = (int) Math.floor(cy - ry); y <= Math.ceil(cy + ry); y++) {
            for (int x = (int) Math.floor(cx - rx); x <= Math.ceil(cx + rx); x++) {
                float dx = (x - cx) / rx, dy = (y - cy) / ry;
                if (dx * dx + dy * dy <= 1f) set(x, y, x > cx + shadeFrom ? shade : color);
            }
        }
    }

    /** Trait d'un pixel de large, de ({@code x0}, {@code y0}) à ({@code x1}, {@code y1}). */
    void line(int x0, int y0, int x1, int y1, int color) {
        int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
        for (int i = 0; i <= steps; i++) {
            float t = steps == 0 ? 0f : i / (float) steps;
            set(Math.round(x0 + (x1 - x0) * t), Math.round(y0 + (y1 - y0) * t), color);
        }
    }

    /** Triangle plein, de sommets ({@code ax}, {@code ay}), ({@code bx}, {@code by}), ({@code cx}, {@code cy}). */
    void triangle(int ax, int ay, int bx, int by, int cx, int cy, int color) {
        int minX = Math.min(ax, Math.min(bx, cx)), maxX = Math.max(ax, Math.max(bx, cx));
        int minY = Math.min(ay, Math.min(by, cy)), maxY = Math.max(ay, Math.max(by, cy));
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float d1 = side(x, y, ax, ay, bx, by), d2 = side(x, y, bx, by, cx, cy), d3 = side(x, y, cx, cy, ax, ay);
                boolean negative = d1 < 0 || d2 < 0 || d3 < 0, positive = d1 > 0 || d2 > 0 || d3 > 0;
                if (!(negative && positive)) set(x, y, color);
            }
        }
    }

    private static float side(float px, float py, float ax, float ay, float bx, float by) {
        return (px - bx) * (ay - by) - (ax - bx) * (py - by);
    }

    /** @return la grille entourée d'un contour sombre (tout pixel vide qui touche un pixel dessiné). */
    int[][] outlined() {
        int[][] out = new int[height][];
        for (int y = 0; y < height; y++) out[y] = pixels[y].clone();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (pixels[y][x] != 0) continue;
                if (get(x, y - 1) != 0 || get(x, y + 1) != 0 || get(x - 1, y) != 0 || get(x + 1, y) != 0) {
                    out[y][x] = OUTLINE;
                }
            }
        }
        return out;
    }

    /** @return la couleur RGBA8888 opaque de {@code hex} ("rrggbb"). */
    static int rgba(String hex) {
        return (Integer.parseInt(hex, 16) << 8) | 0xff;
    }

    /** @return la couleur RGBA8888 de {@code hex} ("rrggbb") à l'opacité {@code alpha} (0 à 255). */
    static int rgba(String hex, int alpha) {
        return (Integer.parseInt(hex, 16) << 8) | (alpha & 0xff);
    }
}
