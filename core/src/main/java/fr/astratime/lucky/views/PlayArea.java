package fr.astratime.lucky.views;

import com.badlogic.gdx.scenes.scene2d.Stage;

/**
 * Zone de jeu : tout l'écran sauf la bande occupée à gauche par le panneau
 * latéral. Les vues s'y centrent (main, symboles, piles, barres de vie).
 * Les dimensions sont relues à chaque appel et suivent donc les redimensionnements.
 */
public class PlayArea {

    private final Stage stage;
    private final float leftInset;

    /** @param leftInset largeur réservée à gauche de l'écran (le panneau latéral) */
    public PlayArea(Stage stage, float leftInset) {
        this.stage     = stage;
        this.leftInset = leftInset;
    }

    /** @return l'abscisse (Stage) du bord gauche de la zone de jeu. */
    public float getX()       { return leftInset; }
    /** @return la largeur de la zone de jeu. */
    public float getWidth()   { return stage.getViewport().getWorldWidth() - leftInset; }
    /** @return la hauteur de la zone de jeu (toute la hauteur de l'écran). */
    public float getHeight()  { return stage.getViewport().getWorldHeight(); }
    /** @return l'abscisse (Stage) du centre de la zone de jeu. */
    public float getCenterX() { return getX() + getWidth() / 2f; }
}
