package fr.astratime.lucky.assets;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayDeque;

/** Fabrique de textures simples partagée par les écrans et leurs composants. */
public final class Textures {

    /**
     * @return une texture 1x1 de la couleur donnée, à étirer pour simuler un fond
     *         uni. L'appelant en est propriétaire et doit la disposer.
     */
    public static Texture solidColor(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * @return le profil vertical (1 pixel de large, 99 de haut) d'une bannière
     *         dans le style de {@code jackpot/banner_band.png} : liserés sombres,
     *         galons {@code trim} éclairés sur leur bord, corps {@code body}
     *         assombri près des galons. À étirer horizontalement ; l'appelant en
     *         est propriétaire et doit la disposer.
     */
    public static Texture bannerBand(Color body, Color trim) {
        int height = 99;
        Color outline   = new Color(0.07f, 0.03f, 0.04f, 1f);
        Color trimLight = trim.cpy().lerp(Color.WHITE, 0.4f);
        Color trimDark  = trim.cpy().mul(0.7f, 0.7f, 0.7f, 1f);
        Color bodyDark  = body.cpy().mul(0.78f, 0.78f, 0.78f, 1f);
        Pixmap pixmap = new Pixmap(1, height, Pixmap.Format.RGBA8888);
        for (int y = 0; y < height; y++) {
            int edge = Math.min(y, height - 1 - y); // distance au bord le plus proche : profil symétrique
            Color color;
            if (edge < 2)        color = outline;
            else if (edge < 4)   color = trimLight;
            else if (edge < 10)  color = trim;
            else if (edge < 11)  color = trimDark;
            else if (edge < 13)  color = outline;
            else if (edge < 17)  color = bodyDark;
            else                 color = body;
            pixmap.drawPixel(0, y, Color.rgba8888(color));
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * @return l'image {@code file} détourée : son fond clair (les pixels presque
     *         blancs reliés à ses bords) devient transparent, le reste est
     *         conservé. Sert aux images des symboles, dessinées sur une case
     *         blanche cernée d'un contour sombre. L'appelant est propriétaire de
     *         la texture et doit la disposer.
     */
    public static Texture cutOut(FileHandle file) {
        Pixmap pixmap = new Pixmap(file);
        if (pixmap.getFormat() != Pixmap.Format.RGBA8888) {
            Pixmap rgba = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), Pixmap.Format.RGBA8888);
            rgba.setBlending(Pixmap.Blending.None);
            rgba.drawPixmap(pixmap, 0, 0);
            pixmap.dispose();
            pixmap = rgba;
        }
        pixmap.setBlending(Pixmap.Blending.None);
        int width = pixmap.getWidth(), height = pixmap.getHeight();
        boolean[] visited = new boolean[width * height];
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int x = 0; x < width; x++) { queue.add(x); queue.add((height - 1) * width + x); }
        for (int y = 0; y < height; y++) { queue.add(y * width); queue.add(y * width + width - 1); }
        while (!queue.isEmpty()) {
            int index = queue.poll();
            if (visited[index]) continue;
            visited[index] = true;
            int x = index % width, y = index / width;
            if (!isBackground(pixmap.getPixel(x, y))) continue;
            pixmap.drawPixel(x, y, 0);
            if (x > 0)          queue.add(index - 1);
            if (x < width - 1)  queue.add(index + 1);
            if (y > 0)          queue.add(index - width);
            if (y < height - 1) queue.add(index + width);
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /** @return true pour un pixel du fond à détourer : presque blanc, ou déjà transparent. */
    private static boolean isBackground(int rgba8888) {
        int r = (rgba8888 >>> 24) & 0xff, g = (rgba8888 >>> 16) & 0xff, b = (rgba8888 >>> 8) & 0xff, a = rgba8888 & 0xff;
        return a < 16 || (r > 225 && g > 225 && b > 225);
    }

    private Textures() {}
}
