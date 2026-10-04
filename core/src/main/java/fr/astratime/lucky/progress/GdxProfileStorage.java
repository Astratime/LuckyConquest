package fr.astratime.lucky.progress;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/** Profil du joueur enregistré dans les préférences de libGDX (un fichier dans le dossier de l'utilisateur). */
public class GdxProfileStorage implements ProfileStorage {

    private static final String PREFERENCES = "lucky-conquest-profile";

    private final Preferences preferences = Gdx.app.getPreferences(PREFERENCES);

    @Override
    public String get(String key) {
        return preferences.contains(key) ? preferences.getString(key) : null;
    }

    @Override
    public void put(String key, String value) { preferences.putString(key, value); }

    @Override
    public void flush() { preferences.flush(); }
}
