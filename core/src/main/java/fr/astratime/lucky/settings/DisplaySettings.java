package fr.astratime.lucky.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Mode d'affichage mémorisé d'une partie à l'autre (voir {@link ScreenMode}).
 * Le lanceur le relit avant d'ouvrir la fenêtre, pour démarrer directement
 * dans le bon mode.
 */
public class DisplaySettings {

    /** Nom des préférences du jeu (partagées avec les autres réglages). */
    public static final String PREFERENCES = "lucky-conquest";
    /** Clé du mode d'affichage dans les préférences. */
    public static final String SCREEN_MODE = "screenMode";

    private final Preferences preferences;

    public DisplaySettings() {
        this(Gdx.app.getPreferences(PREFERENCES));
    }

    /** @param preferences préférences du jeu, lues avant le lancement par le lanceur */
    public DisplaySettings(Preferences preferences) {
        this.preferences = preferences;
    }

    /** @return le mode d'affichage choisi (fenêtré par défaut). */
    public ScreenMode getScreenMode() {
        return ScreenMode.parse(preferences.getString(SCREEN_MODE, ScreenMode.WINDOWED.name()));
    }

    /** Mémorise le mode d'affichage choisi. */
    public void setScreenMode(ScreenMode mode) {
        preferences.putString(SCREEN_MODE, mode.name());
        preferences.flush();
    }
}
