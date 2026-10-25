package fr.astratime.lucky.assets;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

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
        Color outline   = Palette.OUTLINE;
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
     * @return l'image {@code file} détourée : tous les pixels de la couleur de
     *         son coin supérieur gauche (le fond uni des images des symboles)
     *         deviennent transparents, y compris ceux enfermés dans le dessin.
     *         L'appelant est propriétaire de la texture et doit la disposer.
     */
    public static Texture cutOut(FileHandle file) {
        Pixmap source = new Pixmap(file);
        Pixmap pixmap = new Pixmap(source.getWidth(), source.getHeight(), Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.drawPixmap(source, 0, 0);
        source.dispose();
        int background = pixmap.getPixel(0, 0);
        for (int y = 0; y < pixmap.getHeight(); y++) {
            for (int x = 0; x < pixmap.getWidth(); x++) {
                if (pixmap.getPixel(x, y) == background) pixmap.drawPixel(x, y, 0);
            }
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /** Pastille « donjon vaincu », 15 x 15 pixels : '#' contour, 'o' or, 'd' or sombre, 'g' vert, 'G' vert clair, 'w' coche. */
    private static final String[] CLEARED_BADGE = {
        ".....#####.....",
        "...##ooooo##...",
        "..#oogggggdd#..",
        ".#ogGGggggggd#.",
        ".#oGGgggggwwd#.",
        "#ogGgggggwwwgd#",
        "#oggggggwwwggd#",
        "#ogwwgggwwgggd#",
        "#oggwwgwwwgggd#",
        "#ogggwwwwggggd#",
        ".#oggwwwgggdd#.",
        ".#dggggwggggd#.",
        "..#ddgggggdd#..",
        "...##ddddd##...",
        ".....#####.....",
    };

    /**
     * @return la pastille pixel art posée sur l'entrée d'un donjon vaincu :
     *         médaille verte cerclée d'or, coche blanche. À afficher sans
     *         lissage ; l'appelant en est propriétaire et doit la disposer.
     */
    public static Texture clearedBadge() {
        int size = CLEARED_BADGE.length;
        Color goldDark = Palette.GOLD.cpy().mul(0.62f, 0.5f, 0.3f, 1f);
        Color green      = Color.valueOf("2fa85a");
        Color greenLight = Color.valueOf("6fe39a");
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                Color color = switch (CLEARED_BADGE[y].charAt(x)) {
                    case '#' -> Palette.OUTLINE;
                    case 'o' -> Palette.GOLD;
                    case 'd' -> goldDark;
                    case 'g' -> green;
                    case 'G' -> greenLight;
                    case 'w' -> Color.WHITE;
                    default  -> null;
                };
                pixmap.drawPixel(x, y, color == null ? 0 : Color.rgba8888(color));
            }
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Textures() {}
}
