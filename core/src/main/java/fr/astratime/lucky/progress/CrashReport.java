package fr.astratime.lucky.progress;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Rapport de plantage : quand le jeu plante, le lanceur écrit l'erreur dans
 * {@code ~/.prefs/lucky-conquest-crash.log} (à côté de la sauvegarde), et au
 * lancement suivant le Croupier dit où le trouver, une fois.
 *
 * <p>N'utilise pas libGDX : le plantage peut arriver avant qu'il soit prêt, ou
 * l'avoir emporté avec lui.</p>
 */
public final class CrashReport {

    /** Le rapport, dans le dossier de la sauvegarde (noms rangés sous forme codée). */
    public static final Path DEFAULT_FILE = Paths.get(System.getProperty("user.home"),
        fr.astratime.lucky.security.Hidden.reveal(new int[]{0x15, 0x30, 0x37, 0x2f, 0x29, 0x27}),
        fr.astratime.lucky.security.Hidden.reveal(new int[]{0x57, 0x35, 0x26, 0x21, 0x36, 0x79, 0x3a,
            0x31, 0x0d, 0x19, 0x18, 0x17, 0x04, 0x08, 0xac, 0xe5, 0xf9, 0xf1, 0xe6, 0xf2, 0xb1, 0xc8,
            0xc6, 0xc9}));

    private CrashReport() {}

    /**
     * Écrit le rapport de {@code error} dans {@code file} (il remplace le précédent),
     * et marque qu'il n'a pas encore été montré au joueur. N'échoue jamais : si le
     * disque refuse, le plantage reste seulement dans la console.
     */
    public static void write(Path file, Throwable error) {
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            StringWriter trace = new StringWriter();
            error.printStackTrace(new PrintWriter(trace));
            String report = "Lucky Conquest : rapport de plantage\n"
                + "Date : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + '\n'
                + "Java : " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")\n"
                + "Système : " + System.getProperty("os.name") + ' ' + System.getProperty("os.version")
                + " (" + System.getProperty("os.arch") + ")\n\n"
                + trace;
            Files.writeString(file, report, StandardCharsets.UTF_8);
            Files.writeString(pendingFlag(file), "");
        } catch (IOException | RuntimeException ignored) {
            // Rien de plus à faire : l'erreur d'origine est relancée par l'appelant.
        }
    }

    /**
     * @return {@code true} si un rapport écrit dans {@code file} n'a pas encore été
     *         montré au joueur ; il est alors marqué comme montré
     */
    public static boolean takePending(Path file) {
        try {
            return Files.deleteIfExists(pendingFlag(file)) && Files.exists(file);
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }

    /** Fichier vide à côté du rapport, tant que le joueur ne l'a pas vu. */
    private static Path pendingFlag(Path file) {
        return file.resolveSibling(file.getFileName() + ".nouveau");
    }
}
