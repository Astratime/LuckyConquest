package fr.astratime.lucky.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/** Fabrique de textures simples partagée par les écrans et leurs composants. */
final class Textures {

    /**
     * @return une texture 1x1 de la couleur donnée, à étirer pour simuler un fond
     *         uni. L'appelant en est propriétaire et doit la disposer.
     */
    static Texture solidColor(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Textures() {}
}
