package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.views.FrontScene;

/**
 * Des rustines de ciel pour le vrai décor d'un lieu : quand une scène enlève un morceau du décor (la tour de
 * l'Architecte qui s'effondre, la comète du Prophète qui quitte le ciel), on peint par-dessus le ciel qu'il cachait,
 * tiré des pixels du décor tout autour.
 */
final class DecorPatches {

    private DecorPatches() {}

    /** @return l'image du décor de {@code kind}, à libérer ({@code null} sans fichiers ou sans image). */
    static Pixmap read(EnemyKind kind) {
        String path = FrontScene.Decor.of(kind).getPath();
        if (Gdx.files == null || !Gdx.files.internal(path).exists()) return null;
        return new Pixmap(Gdx.files.internal(path));
    }

    private static final Color LIT = new Color();

    /**
     * Pose la rustine {@code patch} sur le décor de {@code scene}, son coin haut-gauche au pixel ({@code px},
     * {@code py}) du décor, éclairée comme lui.
     */
    static void draw(BossScene scene, Batch batch, TextureRegion patch, float px, float py, float alpha) {
        if (patch == null || alpha <= 0f) return;
        scene.litLikeDecor(Color.WHITE, LIT);
        batch.setColor(LIT.r, LIT.g, LIT.b, alpha);
        float k = scene.decorScale();
        batch.draw(patch, scene.decorX(px), scene.decorY(py + patch.getRegionHeight()), patch.getRegionWidth() * k,
            patch.getRegionHeight() * k);
        batch.setColor(Color.WHITE);
    }

    /** @return {@code true} si le pixel RGBA8888 {@code rgba} n'est pas du ciel (un contour, une carte, une étoile). */
    static boolean odd(int rgba) {
        int r = rgba >>> 24 & 0xff, g = rgba >>> 16 & 0xff, b = rgba >>> 8 & 0xff;
        int max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b));
        return max - min > 80 || (r + g + b < 135 && r > b) || min > 200;
    }

    /**
     * @return une rustine de ({@code x0}, {@code y0}) à ({@code x1}, {@code y1}) (pixels du décor, bornes comprises)
     *         qui cache ce qui s'y dresse : chaque ligne y est le ciel en dégradé entre le premier pixel de ciel à
     *         gauche et le premier à droite ; ce qui déborde du morceau sur la même ligne (une carte à cheval) est
     *         effacé aussi. La rustine couvre {@code x0 - spill} à {@code x1 + spill} ; ce qu'elle ne touche pas est vide.
     *         Avec {@code cutout}, c'est l'inverse : les vrais pixels du morceau (ceux que la rustine cache), seuls.
     */
    static Pixmap sideways(Pixmap decor, int x0, int y0, int x1, int y1, int spill, boolean cutout) {
        int left0 = x0 - spill, width = x1 - x0 + 1 + 2 * spill, height = y1 - y0 + 1;
        Pixmap patch = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        patch.setBlending(Pixmap.Blending.None);
        for (int y = y0; y <= y1; y++) {
            int l = x0 - 1, r = x1 + 1;
            while (l > x0 - spill && odd(decor.getPixel(l, y))) l--;
            while (r < x1 + spill && odd(decor.getPixel(r, y))) r++;
            int a = decor.getPixel(l, y), b = decor.getPixel(r, y);
            for (int x = l + 1; x < r; x++) {
                patch.drawPixel(x - left0, y - y0, cutout ? decor.getPixel(x, y) : mix(a, b, (x - l) / (float) (r - l)));
            }
        }
        return patch;
    }

    /**
     * @return une rustine qui efface ce qui passe dans le ciel le long d'une bande (la traîne d'une comète, sa tête) :
     *         de la colonne {@code x0} à {@code x1}, autour de la droite qui passe par ({@code x0}, {@code y0}) et
     *         ({@code x1}, {@code y1}), à {@code band} pixels au-dessus et en dessous. Chaque pixel qui n'est pas du
     *         ciel prend le dégradé entre le ciel juste au-dessus et juste en dessous ; le reste est vide. La rustine
     *         commence à la colonne {@code x0} et à la ligne {@link #top}.
     */
    static Pixmap upright(Pixmap decor, int x0, float y0, int x1, float y1, int band) {
        int top = top(y0, y1, band), bottom = Math.round(Math.max(y0, y1)) + band;
        Pixmap patch = new Pixmap(x1 - x0 + 1, bottom - top + 1, Pixmap.Format.RGBA8888);
        patch.setBlending(Pixmap.Blending.None);
        for (int x = x0; x <= x1; x++) {
            int centre = Math.round(x1 == x0 ? y0 : y0 + (y1 - y0) * (x - x0) / (x1 - x0));
            for (int y = centre - band; y <= centre + band; y++) {
                if (!foreign(decor, x, y, false)) continue;
                int up = y, down = y;
                while (up > 0 && foreign(decor, x, up, false)) up--;
                while (down < decor.getHeight() - 1 && foreign(decor, x, down, false)) down++;
                patch.drawPixel(x - x0, y - top, mix(decor.getPixel(x, up), decor.getPixel(x, down), (y - up) / (float) (down - up)));
            }
        }
        return patch;
    }

    /** @return la première ligne de la rustine {@link #upright} de mêmes paramètres. */
    static int top(float y0, float y1, int band) { return Math.round(Math.min(y0, y1)) - band; }

    /** @return {@code true} si le pixel ({@code x}, {@code y}) du décor n'est pas du ciel de nuit (ou {@code outside} hors de l'image). */
    private static boolean foreign(Pixmap decor, int x, int y, boolean outside) {
        if (x < 0 || y < 0 || x >= decor.getWidth() || y >= decor.getHeight()) return outside;
        int rgba = decor.getPixel(x, y);
        return odd(rgba) || bright(rgba);
    }

    /** @return {@code true} pour un pixel clair dans un ciel de nuit (la traîne pâle de la comète). */
    private static boolean bright(int rgba) {
        return (rgba >>> 24 & 0xff) + (rgba >>> 16 & 0xff) + (rgba >>> 8 & 0xff) > 200;
    }

    private static int mix(int a, int b, float t) {
        int out = 0xff;
        for (int shift = 8; shift <= 24; shift += 8) {
            int ca = a >>> shift & 0xff, cb = b >>> shift & 0xff;
            out |= Math.round(ca + (cb - ca) * t) << shift;
        }
        return out;
    }
}
