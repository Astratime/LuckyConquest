package fr.astratime.lucky.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import fr.astratime.lucky.settings.ScreenMode;
import fr.astratime.lucky.settings.ScreenModeSwitcher;

/**
 * Modes d'affichage sur ordinateur : au lancement (configuration de la
 * fenêtre) et pendant le jeu (choix dans les options).
 *
 * En mode fenêtré, la fenêtre est agrandie : elle occupe l'écran sans cacher la
 * barre des tâches et garde ses boutons réduire et fermer. Si le joueur la
 * restaure, elle reprend une taille qui tient dans l'écran, centrée.
 */
public class Lwjgl3ScreenModes implements ScreenModeSwitcher {

    /** Part de l'écran occupée par la fenêtre restaurée (non agrandie). */
    private static final float RESTORED_SHARE = 0.8f;

    /** Prépare l'ouverture de la fenêtre du jeu en mode {@code mode}. */
    public static void configure(Lwjgl3ApplicationConfiguration configuration, ScreenMode mode) {
        Graphics.DisplayMode screen = Lwjgl3ApplicationConfiguration.getDisplayMode();
        if (mode == ScreenMode.FULLSCREEN) {
            configuration.setFullscreenMode(screen);
        } else {
            configuration.setWindowedMode(restoredWidth(screen), restoredHeight(screen));
            configuration.setMaximized(true);
        }
    }

    @Override
    public void apply(ScreenMode mode) {
        Graphics.DisplayMode screen = Gdx.graphics.getDisplayMode();
        if (mode == ScreenMode.FULLSCREEN) {
            Gdx.graphics.setFullscreenMode(screen);
        } else {
            Gdx.graphics.setWindowedMode(restoredWidth(screen), restoredHeight(screen));
            ((Lwjgl3Graphics) Gdx.graphics).getWindow().maximizeWindow();
        }
    }

    private static int restoredWidth(Graphics.DisplayMode screen) {
        return Math.round(screen.width * RESTORED_SHARE);
    }

    private static int restoredHeight(Graphics.DisplayMode screen) {
        return Math.round(screen.height * RESTORED_SHARE);
    }
}
