package fr.astratime.lucky.progress;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * Journal détaillé des combats (2026-11-16) : pour voir comment le joueur joue et apprendre au simulateur à jouer
 * pareil. Écrit dans {@code ~/.prefs/lucky-conquest-combats.log}, à côté de la sauvegarde ; rien n'est envoyé.
 *
 * <p>Une ligne par événement, en texte : {@code COMBAT} (mode, ennemi, rang, deck, machine), puis à chaque tour
 * {@code TOUR} (PV, gains, Mise, main piochée), les décisions du joueur ({@code JOUE}, {@code ECHOPPE},
 * {@code ROULEAU_ACHETE}, {@code ROULEAU_RETIRE}, {@code CHOIX}), {@code TIRAGE} (Mise, combinaisons, symboles,
 * Bingo, dégâts, gains) et {@code ENNEMI} (PV perdus pendant son tour), enfin {@code FIN}. Les valeurs sont écrites
 * {@code clé=valeur}.
 *
 * <p>Le mode ADMIN et le tutoriel n'y écrivent rien. Au-delà de {@link #MAX_BYTES}, le fichier devient
 * {@code .1} (l'ancien {@code .1} est effacé) et un nouveau commence. N'échoue jamais.
 */
public final class CombatLog {

    /** Le journal, dans le dossier de la sauvegarde. */
    public static final Path DEFAULT_FILE = CrashReport.DEFAULT_FILE.resolveSibling("lucky-conquest-combats.log");

    /** Taille au-delà de laquelle le journal recommence (l'ancien est gardé en {@code .1}). */
    public static final long MAX_BYTES = 20L * 1024 * 1024;

    private CombatLog() {}

    /** Ajoute {@code line} à la fin de {@code file}. N'échoue jamais : si le disque refuse, la ligne est perdue. */
    public static void append(Path file, String line) {
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            if (Files.exists(file) && Files.size(file) > MAX_BYTES) {
                Files.move(file, file.resolveSibling(file.getFileName() + ".1"), StandardCopyOption.REPLACE_EXISTING);
            }
            Files.writeString(file, line + '\n', StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException | RuntimeException ignored) {
            // le journal n'est qu'une aide à l'équilibrage : la partie continue sans lui
        }
    }
}
