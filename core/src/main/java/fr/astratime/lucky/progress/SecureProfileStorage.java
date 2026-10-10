package fr.astratime.lucky.progress;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Profil enregistré dans un fichier signé ({@link SaveCodec}), avec une copie
 * de secours ({@code .bak}) : avant chaque écriture, la sauvegarde valide en
 * place devient la copie de secours.
 *
 * Au chargement :
 * <ol>
 *   <li>la sauvegarde, si sa signature est bonne ;</li>
 *   <li>sinon la copie de secours ({@link #isRestoredFromBackup()}) ;</li>
 *   <li>sinon, s'il n'y a encore aucune sauvegarde, l'ancien profil non signé
 *       ({@code legacy}), repris une fois puis effacé ;</li>
 *   <li>sinon (fichiers modifiés à la main) un profil neuf : le fichier refusé
 *       est gardé à côté, en {@code .refuse} ({@link #isRejected()}).</li>
 * </ol>
 */
public class SecureProfileStorage implements ProfileStorage {

    private final Path file;
    private final Path backup;
    private final Path rejected;
    private final Map<String, String> values = new LinkedHashMap<>();
    private Runnable clearLegacy;
    private boolean  restoredFromBackup;
    private boolean  wasRejected;

    /**
     * @param file        fichier de la sauvegarde
     * @param legacy      valeurs de l'ancien profil non signé (vide s'il n'y en a pas)
     * @param clearLegacy efface l'ancien profil, une fois la nouvelle sauvegarde écrite
     */
    public SecureProfileStorage(Path file, Supplier<Map<String, String>> legacy, Runnable clearLegacy) {
        this.file     = file;
        this.backup   = file.resolveSibling(file.getFileName() + ".bak");
        this.rejected = file.resolveSibling(file.getFileName() + ".refuse");
        Map<String, String> saved = read(file);
        if (saved == null) {
            saved = read(backup);
            restoredFromBackup = saved != null;
        }
        if (saved != null) {
            values.putAll(saved);
        } else if (!Files.exists(file) && !Files.exists(backup)) {
            values.putAll(legacy.get());
            if (!values.isEmpty()) {
                this.clearLegacy = clearLegacy;
                flush(); // l'ancien profil passe tout de suite au nouveau format
            }
        } else {
            wasRejected = true;
            try {
                if (Files.exists(file)) Files.move(file, rejected, StandardCopyOption.REPLACE_EXISTING);
                Files.deleteIfExists(backup);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /** @return {@code true} si la sauvegarde était abîmée et que la copie de secours a été reprise. */
    public boolean isRestoredFromBackup() { return restoredFromBackup; }

    /** @return {@code true} si la sauvegarde et sa copie ont été modifiées à la main : le profil repart de zéro. */
    public boolean isRejected() { return wasRejected; }

    @Override
    public String get(String key) { return values.get(key); }

    @Override
    public void put(String key, String value) { values.put(key, value); }

    @Override
    public void flush() {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            // La sauvegarde en place, si elle est valide, devient la copie de secours.
            if (read(file) != null) Files.copy(file, backup, StandardCopyOption.REPLACE_EXISTING);
            Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temporary, SaveCodec.encode(values), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (clearLegacy != null) {
            clearLegacy.run();
            clearLegacy = null;
        }
    }

    /** @return les valeurs de la sauvegarde {@code path}, ou {@code null} si elle manque ou n'est pas valide. */
    private static Map<String, String> read(Path path) {
        if (!Files.isRegularFile(path)) return null;
        try {
            return SaveCodec.decode(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException | SaveCodec.InvalidSaveException e) {
            return null;
        }
    }
}
