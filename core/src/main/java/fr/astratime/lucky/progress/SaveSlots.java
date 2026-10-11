package fr.astratime.lucky.progress;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Les {@link #COUNT} emplacements de sauvegarde, numérotés de 1 à {@link #COUNT},
 * chacun une partie complète ({@link SecureProfileStorage}) dans le même dossier.
 * L'emplacement 1 garde le fichier de la sauvegarde d'avant les emplacements :
 * une partie déjà commencée s'y retrouve, sans rien à reprendre. Le dernier
 * emplacement joué est noté à côté, en clair (ce n'est pas une progression).
 */
public final class SaveSlots {

    /** Nombre d'emplacements. */
    public static final int COUNT = 3;

    private static final String SAVE_NAME = "lucky-conquest-save";
    private static final String LAST_NAME = "lucky-conquest-last-slot";

    private final Path folder;

    /** @param folder dossier des sauvegardes */
    public SaveSlots(Path folder) {
        this.folder = folder;
    }

    /** @return le fichier de la sauvegarde de l'emplacement {@code slot} (1 : celui d'avant les emplacements). */
    public Path file(int slot) {
        check(slot);
        return folder.resolve(slot == 1 ? SAVE_NAME : SAVE_NAME + "-" + slot);
    }

    /** @return {@code true} si une partie est enregistrée à l'emplacement {@code slot} (ou sa copie de secours). */
    public boolean isUsed(int slot) {
        return Files.exists(file(slot)) || Files.exists(backup(slot));
    }

    /** @return les emplacements où une partie est enregistrée, dans l'ordre. */
    public List<Integer> usedSlots() {
        List<Integer> used = new ArrayList<>();
        for (int slot = 1; slot <= COUNT; slot++) if (isUsed(slot)) used.add(slot);
        return used;
    }

    /**
     * @return les valeurs de la partie de l'emplacement {@code slot} (sa copie de
     *         secours si la sauvegarde est abîmée), lues sans rien changer sur le
     *         disque ; {@code null} si l'emplacement est vide ou illisible
     */
    public Map<String, String> peek(int slot) {
        Map<String, String> values = SecureProfileStorage.read(file(slot));
        return values != null ? values : SecureProfileStorage.read(backup(slot));
    }

    /**
     * @return le rang de la partie de l'emplacement {@code slot} ({@code null} :
     *         aucun rang acheté, emplacement vide ou illisible)
     */
    public Rank rank(int slot) {
        Map<String, String> values = peek(slot);
        if (values == null) return null;
        try {
            int ranks = Integer.parseInt(values.getOrDefault(PlayerProfile.KEY_RANK, "0").trim());
            return ranks <= 0 ? null : Rank.values()[Math.min(ranks, Rank.values().length) - 1];
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Efface la partie de l'emplacement {@code slot} (sauvegarde, copie de secours,
     * fichier temporaire) : une nouvelle partie y commence.
     */
    public void erase(int slot) {
        Path file = file(slot);
        try {
            Files.deleteIfExists(file);
            Files.deleteIfExists(backup(slot));
            Files.deleteIfExists(file.resolveSibling(file.getFileName() + ".tmp"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * @return l'emplacement que « Continuer partie » reprend : le dernier joué s'il
     *         a une partie, sinon celui dont la partie a été enregistrée en dernier ;
     *         {@code 0} s'il n'y a aucune partie
     */
    public int lastUsed() {
        int noted = notedSlot();
        if (noted > 0 && isUsed(noted)) return noted;
        int latest = 0;
        FileTime latestTime = null;
        for (int slot : usedSlots()) {
            FileTime time = modified(slot);
            if (latest == 0 || (time != null && (latestTime == null || time.compareTo(latestTime) > 0))) {
                latest = slot;
                latestTime = time;
            }
        }
        return latest;
    }

    /** Note {@code slot} comme le dernier emplacement joué. */
    public void setLastUsed(int slot) {
        check(slot);
        try {
            Files.createDirectories(folder);
            Files.writeString(folder.resolve(LAST_NAME), String.valueOf(slot), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private int notedSlot() {
        try {
            int slot = Integer.parseInt(Files.readString(folder.resolve(LAST_NAME), StandardCharsets.UTF_8).trim());
            return slot >= 1 && slot <= COUNT ? slot : 0;
        } catch (IOException | NumberFormatException e) {
            return 0;
        }
    }

    private FileTime modified(int slot) {
        Path path = Files.exists(file(slot)) ? file(slot) : backup(slot);
        try {
            return Files.getLastModifiedTime(path);
        } catch (IOException e) {
            return null;
        }
    }

    private Path backup(int slot) {
        Path file = file(slot);
        return file.resolveSibling(file.getFileName() + ".bak");
    }

    private static void check(int slot) {
        if (slot < 1 || slot > COUNT) throw new IllegalArgumentException("emplacement " + slot);
    }
}
