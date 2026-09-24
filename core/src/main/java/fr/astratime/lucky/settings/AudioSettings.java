package fr.astratime.lucky.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Volumes mémorisés d'une partie à l'autre, par paliers de {@link #STEP} :
 * celui des bruitages (appliqué à chaque son joué, voir
 * {@link fr.astratime.lucky.assets.VolumeSound}) et celui de la musique.
 */
public class AudioSettings {

    /** Écart entre deux paliers de volume. */
    public static final float STEP = 0.25f;

    private static final String PREFERENCES  = "lucky-conquest";
    private static final String SOUND_VOLUME = "soundVolume";
    private static final String MUSIC_VOLUME = "musicVolume";

    private final Preferences preferences = Gdx.app.getPreferences(PREFERENCES);

    /** @return le volume des bruitages, de 0 à 1. */
    public float getSoundVolume() { return preferences.getFloat(SOUND_VOLUME, 1f); }

    /** @return le volume de la musique, de 0 à 1. */
    public float getMusicVolume() { return preferences.getFloat(MUSIC_VOLUME, 1f); }

    /** Monte ({@code direction} > 0) ou baisse d'un palier le volume des bruitages, entre 0 et 1. */
    public void stepSoundVolume(int direction) { step(SOUND_VOLUME, getSoundVolume(), direction); }

    /** Monte ({@code direction} > 0) ou baisse d'un palier le volume de la musique, entre 0 et 1. */
    public void stepMusicVolume(int direction) { step(MUSIC_VOLUME, getMusicVolume(), direction); }

    private void step(String key, float current, int direction) {
        preferences.putFloat(key, Math.clamp(current + Math.signum(direction) * STEP, 0f, 1f));
        preferences.flush();
    }

    /** @return le volume en pourcentage arrondi, pour l'affichage (ex : 75). */
    public static int percent(float volume) {
        return Math.round(volume * 100f);
    }
}
