package fr.astratime.lucky.views;

import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Comme {@link ScreenViewport} (une unité par pixel, l'écran montre plus de
 * table quand la fenêtre grandit), mais sans descendre sous une taille
 * minimale : dans une fenêtre plus petite (écran de portable, fenêtre
 * agrandie amputée de la barre des tâches), tout est réduit d'un même facteur
 * au lieu de se chevaucher.
 */
public class MinimumScreenViewport extends ScreenViewport {

    private final float minWidth;
    private final float minHeight;

    /** @param minWidth  largeur minimale du monde, en unités ; @param minHeight hauteur minimale */
    public MinimumScreenViewport(float minWidth, float minHeight) {
        this.minWidth  = minWidth;
        this.minHeight = minHeight;
    }

    @Override
    public void update(int screenWidth, int screenHeight, boolean centerCamera) {
        if (screenWidth > 0 && screenHeight > 0) {
            setUnitsPerPixel(Math.max(1f, Math.max(minWidth / screenWidth, minHeight / screenHeight)));
        }
        super.update(screenWidth, screenHeight, centerCamera);
    }
}
