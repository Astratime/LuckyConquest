package fr.astratime.lucky.progress;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Les parties du joueur dans le jeu : {@link SaveSlots#COUNT} sauvegardes signées
 * ({@link SecureProfileStorage}) dans le dossier {@code .prefs} de l'utilisateur.
 * L'ancien profil (préférences de libGDX, non signé) est repris dans l'emplacement 1
 * au premier lancement d'une version signée, puis effacé.
 */
public final class GdxProfileStorage {

    // Chaînes rangées sous forme codée (voir fr.astratime.lucky.security.Hidden)
    // pour ne pas exposer en clair l'agencement des sauvegardes dans le binaire.
    private static final String LEGACY_PREFERENCES = fr.astratime.lucky.security.Hidden.reveal(
        new int[]{0x57, 0x35, 0x26, 0x21, 0x36, 0x79, 0x3a, 0x31, 0x0d, 0x19, 0x18, 0x17, 0x04, 0x08,
            0xac, 0xf6, 0xf9, 0xff, 0xf3, 0xf3, 0xf3, 0xc1});
    private static final String SAVE_FOLDER        = fr.astratime.lucky.security.Hidden.reveal(
        new int[]{0x15, 0x30, 0x37, 0x2f, 0x29, 0x27});

    private GdxProfileStorage() {}

    /** @return les emplacements de sauvegarde, dans le dossier {@code .prefs} de l'utilisateur. */
    public static SaveSlots slots() {
        return new SaveSlots(Gdx.files.external(SAVE_FOLDER).file().toPath());
    }

    /**
     * @param slots emplacements de sauvegarde
     * @param slot  l'emplacement à ouvrir (de 1 à {@link SaveSlots#COUNT})
     * @return le stockage de la partie de l'emplacement {@code slot}, chargé depuis le disque
     */
    public static SecureProfileStorage open(SaveSlots slots, int slot) {
        Path file = slots.file(slot);
        if (slot != 1) return new SecureProfileStorage(file, Map::of, () -> { });
        Preferences legacy = Gdx.app.getPreferences(LEGACY_PREFERENCES);
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
