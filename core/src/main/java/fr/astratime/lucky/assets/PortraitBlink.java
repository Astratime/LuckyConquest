package fr.astratime.lucky.assets;

import java.util.ArrayList;
import java.util.List;

/**
 * Yeux fermés d'un portrait d'ennemi, pour le faire cligner en vue de face : on
 * retrouve ses deux yeux (deux petites taches sombres côte à côte dans le haut du
 * visage, entourées de dessin et non du contour) et on les recouvre de la
 * couleur qui les entoure, avec un trait de paupière en bas.
 *
 * <p>Calculé à partir de la grille du portrait (couleurs RGBA8888, 0 =
 * transparent), sans rien redessiner à la main.
 */
public final class PortraitBlink {

    /** Les yeux sont dans le haut du portrait. */
    private static final float EYE_ZONE = 0.6f;
    /** Taille maximale d'un oeil (pixels), et sa hauteur. */
    private static final int MAX_EYE = 4, MAX_EYE_HEIGHT = 2;
    /** Écart horizontal entre les deux yeux (pixels). */
    private static final int MIN_GAP = 3, MAX_GAP = 14;
    /** Luminosité en dessous de laquelle un pixel compte comme sombre (0-255). */
    private static final int DARK = 70;

    private PortraitBlink() {}

    /** @return la grille aux yeux fermés, ou {@code null} si on ne trouve pas deux yeux (masque, orbites...) */
    public static int[][] closeEyes(int[][] grid) {
        int h = grid.length, w = grid[0].length;
        int zone = Math.round(h * EYE_ZONE);
        boolean[][] seen = new boolean[h][w];
        List<List<int[]>> spots = new ArrayList<>();
        for (int y = 1; y < zone; y++) {
            for (int x = 1; x < w - 1; x++) {
                if (seen[y][x] || !isEyePixel(grid, x, y)) continue;
                List<int[]> spot = new ArrayList<>();
                flood(grid, seen, x, y, zone, spot);
                if (spot.size() <= MAX_EYE && height(spot) <= MAX_EYE_HEIGHT) spots.add(spot);
            }
        }
        List<int[]>[] pair = bestPair(spots);
        if (pair == null) return null;
        int[][] out = new int[h][];
        for (int y = 0; y < h; y++) out[y] = grid[y].clone();
        for (List<int[]> eye : pair) close(grid, out, eye);
        return out;
    }

    private static boolean isEyePixel(int[][] g, int x, int y) {
        if (!isDark(g[y][x])) return false;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int yy = y + dy, xx = x + dx;
                if (yy < 0 || yy >= g.length || xx < 0 || xx >= g[0].length || g[yy][xx] == 0) return false;
            }
        }
        return true;
    }

    private static void flood(int[][] g, boolean[][] seen, int x0, int y0, int zone, List<int[]> spot) {
        List<int[]> stack = new ArrayList<>();
        stack.add(new int[] {x0, y0});
        seen[y0][x0] = true;
        while (!stack.isEmpty()) {
            int[] p = stack.remove(stack.size() - 1);
            spot.add(p);
            if (spot.size() > MAX_EYE + 8) return; // trop grand pour un oeil : on arrête de compter
            int[][] next = {{p[0] + 1, p[1]}, {p[0] - 1, p[1]}, {p[0], p[1] + 1}, {p[0], p[1] - 1}};
            for (int[] n : next) {
                if (n[1] < 1 || n[1] >= zone || n[0] < 1 || n[0] >= g[0].length - 1 || seen[n[1]][n[0]]) continue;
                if (!isEyePixel(g, n[0], n[1])) continue;
                seen[n[1]][n[0]] = true;
                stack.add(n);
            }
        }
    }

    /** Les deux taches à la même hauteur, le plus près du milieu du portrait et l'une de l'autre en miroir. */
    @SuppressWarnings("unchecked")
    private static List<int[]>[] bestPair(List<List<int[]>> spots) {
        List<int[]>[] best = null;
        float bestScore = Float.MAX_VALUE;
        for (int i = 0; i < spots.size(); i++) {
            for (int j = i + 1; j < spots.size(); j++) {
                float ax = centerX(spots.get(i)), bx = centerX(spots.get(j));
                float ay = centerY(spots.get(i)), by = centerY(spots.get(j));
                float gap = Math.abs(ax - bx);
                if (Math.abs(ay - by) > 1f || gap < MIN_GAP || gap > MAX_GAP) continue;
                float score = Math.abs(ay - by) * 4f + Math.abs(spots.get(i).size() - spots.get(j).size()) + ay * 0.05f;
                if (score < bestScore) {
                    bestScore = score;
                    best = new List[] {spots.get(i), spots.get(j)};
                }
            }
        }
        return best;
    }

    /** Recouvre l'oeil de la couleur qui l'entoure ; sa rangée du bas devient la paupière, plus sombre. */
    private static void close(int[][] g, int[][] out, List<int[]> eye) {
        int skin = surrounding(g, eye);
        int bottom = Integer.MIN_VALUE;
        for (int[] p : eye) bottom = Math.max(bottom, p[1]);
        for (int[] p : eye) out[p[1]][p[0]] = p[1] == bottom ? darken(skin) : skin;
    }

    /** @return la couleur la plus fréquente autour de l'oeil (sa peau). */
    private static int surrounding(int[][] g, List<int[]> eye) {
        List<Integer> colors = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (int[] p : eye) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    int c = g[p[1] + dy][p[0] + dx];
                    if (c == 0 || isDark(c)) continue;
                    int k = colors.indexOf(c);
                    if (k < 0) {
                        colors.add(c);
                        counts.add(1);
                    } else {
                        counts.set(k, counts.get(k) + 1);
                    }
                }
            }
        }
        int best = 0;
        for (int k = 1; k < colors.size(); k++) if (counts.get(k) > counts.get(best)) best = k;
        return colors.isEmpty() ? g[eye.get(0)[1]][eye.get(0)[0]] : colors.get(best);
    }

    private static boolean isDark(int rgba) {
        if ((rgba & 0xff) == 0) return false;
        int r = rgba >>> 24, gr = (rgba >>> 16) & 0xff, b = (rgba >>> 8) & 0xff;
        return (r * 3 + gr * 6 + b) / 10 < DARK;
    }

    private static int darken(int rgba) {
        int r = (rgba >>> 24) * 6 / 10, gr = ((rgba >>> 16) & 0xff) * 6 / 10, b = ((rgba >>> 8) & 0xff) * 6 / 10;
        return (r << 24) | (gr << 16) | (b << 8) | (rgba & 0xff);
    }

    private static int height(List<int[]> spot) {
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int[] p : spot) {
            min = Math.min(min, p[1]);
            max = Math.max(max, p[1]);
        }
        return max - min + 1;
    }

    private static float centerX(List<int[]> spot) {
        float sum = 0f;
        for (int[] p : spot) sum += p[0];
        return sum / spot.size();
    }

    private static float centerY(List<int[]> spot) {
        float sum = 0f;
        for (int[] p : spot) sum += p[1];
        return sum / spot.size();
    }
}
