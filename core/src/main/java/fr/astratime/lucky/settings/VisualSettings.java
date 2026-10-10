package fr.astratime.lucky.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Réglages d'affichage mémorisés d'une partie à l'autre. « Effets réduits »
 * coupe les secousses de l'écran, les flashs et les micro-arrêts sur les gros
 * coups, pour les joueurs qui y sont sensibles. La vitesse des animations
 * accélère le combat ({@link AnimationSpeed}).
 */
public class VisualSettings {

    private static final String PREFERENCES     = "lucky-conquest";
    private static final String REDUCED_EFFECTS = "reducedEffects";
    private static final String ANIMATION_SPEED = "animationSpeed";

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

    /** @return la vitesse des animations du combat (x1 par défaut). */
    public AnimationSpeed getAnimationSpeed() {
        return AnimationSpeed.parse(preferences.getString(ANIMATION_SPEED, AnimationSpeed.X1.name()));
    }

    /** Passe à la vitesse suivante ({@code direction} 1) ou précédente (-1), et mémorise le choix. */
    public void stepAnimationSpeed(int direction) {
        preferences.putString(ANIMATION_SPEED, getAnimationSpeed().step(direction).name());
        preferences.flush();
    }
}
