package fr.astratime.lucky.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Réglages d'affichage mémorisés d'une partie à l'autre. « Effets réduits »
 * coupe les secousses de l'écran, les flashs et les micro-arrêts sur les gros
 * coups, pour les joueurs qui y sont sensibles. La vitesse des animations
 * accélère le combat ({@link AnimationSpeed}). Pour l'accessibilité, les
 * enseignes renforcées donnent un jeu à quatre couleurs
 * ({@link fr.astratime.lucky.assets.SuitColors}) et le grand texte grossit les
 * infobulles.
 */
public class VisualSettings {

    private static final String PREFERENCES     = "lucky-conquest";
    private static final String REDUCED_EFFECTS = "reducedEffects";
    private static final String ANIMATION_SPEED = "animationSpeed";
    private static final String STRONG_SUITS    = "strongSuitColors";
    private static final String LARGE_TOOLTIPS  = "largeTooltips";

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

    /** @return true si le Carreau est bleu et le Trèfle vert (jeu à quatre couleurs). */
    public boolean isStrongSuitColors() {
        return preferences.getBoolean(STRONG_SUITS, false);
    }

    /** Active ou désactive les enseignes renforcées, et mémorise le choix. */
    public void setStrongSuitColors(boolean strong) {
        preferences.putBoolean(STRONG_SUITS, strong);
        preferences.flush();
    }

    /** @return true si les infobulles s'affichent en grand texte. */
    public boolean isLargeTooltips() {
        return preferences.getBoolean(LARGE_TOOLTIPS, false);
    }

    /** Active ou désactive le grand texte des infobulles, et mémorise le choix. */
    public void setLargeTooltips(boolean large) {
        preferences.putBoolean(LARGE_TOOLTIPS, large);
        preferences.flush();
    }
}
