package fr.astratime.lucky.progress;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Le profil du joueur dans le jeu : une sauvegarde signée ({@link SecureProfileStorage})
 * dans le dossier {@code .prefs} de l'utilisateur. L'ancien profil (préférences
 * de libGDX, non signé) est repris au premier lancement de cette version, puis effacé.
 */
public final class GdxProfileStorage {

    private static final String LEGACY_PREFERENCES = "lucky-conquest-profile";
    private static final String SAVE_FILE          = ".prefs/lucky-conquest-save";

    private GdxProfileStorage() {}

    /** @return le stockage du profil, chargé depuis le disque. */
    public static SecureProfileStorage open() {
        Preferences legacy = Gdx.app.getPreferences(LEGACY_PREFERENCES);
        Path file = Gdx.files.external(SAVE_FILE).file().toPath();
        return new SecureProfileStorage(file, () -> {
            Map<String, String> values = new LinkedHashMap<>();
            legacy.get().forEach((key, value) -> values.put(key, String.valueOf(value)));
            return values;
        }, () -> {
            legacy.clear();
            legacy.flush();
        });
    }
}
