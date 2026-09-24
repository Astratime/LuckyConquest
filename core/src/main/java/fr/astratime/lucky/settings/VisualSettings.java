package fr.astratime.lucky.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Réglages d'affichage mémorisés d'une partie à l'autre. « Effets réduits »
 * coupe les secousses de l'écran, les flashs et les micro-arrêts sur les gros
 * coups, pour les joueurs qui y sont sensibles.
 */
public class VisualSettings {

    private static final String PREFERENCES     = "lucky-conquest";
    private static final String REDUCED_EFFECTS = "reducedEffects";

    private final Preferences preferences = Gdx.app.getPreferences(PREFERENCES);

    /** @return true si les secousses, flashs et micro-arrêts sont désactivés. */
    public boolean isReducedEffects() {
        return preferences.getBoolean(REDUCED_EFFECTS, false);
    }

    /** Active ou désactive les effets réduits, et mémorise le choix. */
    public void setReducedEffects(boolean reduced) {
        preferences.putBoolean(REDUCED_EFFECTS, reduced);
        preferences.flush();
    }
}
