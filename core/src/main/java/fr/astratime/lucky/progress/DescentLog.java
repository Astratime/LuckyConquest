package fr.astratime.lucky.progress;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Journal des descentes (2026-11-16) : une ligne par combat de l'Exploration (soldat ou roi), écrite dans
 * {@code ~/.prefs/lucky-conquest-descentes.csv}, à côté de la sauvegarde. Il reste sur l'ordinateur : rien n'est
 * envoyé. Le simulateur le relit ({@code ./gradlew :core:simulate -Pjournal=<fichier>}) pour rejouer les mêmes
 * descentes avec le même équipement et comparer victoires et pièces à celles du vrai joueur.
 *
 * <p>Le mode ADMIN et le tutoriel n'y écrivent rien. N'échoue jamais : si le disque refuse, la ligne est perdue.
 */
public final class DescentLog {

    /** Le journal, dans le dossier de la sauvegarde. */
    public static final Path DEFAULT_FILE = CrashReport.DEFAULT_FILE.resolveSibling("lucky-conquest-descentes.csv");

    /** Colonnes, séparées par {@code ;}. Deck : {@code id:exemplaires} séparés par {@code |} ; machine : symboles séparés par {@code |}. */
    public static final String HEADER = "date;place;dungeon;hard;rank;prestige;stage;won;turns;gains;coins;golden;stake;deck;machine";

    private DescentLog() {}

    /**
     * Un combat de l'Exploration terminé.
     *
     * @param place    nom de l'énumération du lieu ({@code Place.name()})
     * @param dungeon  nom de l'énumération du donjon ({@code Dungeon.name()})
     * @param rank     nom de l'énumération du rang, ou {@code null} sans rang
     * @param king     {@code true} pour le combat du roi, {@code false} pour le soldat
     * @param turns    tours joués dans ce combat
     * @param gains    gains à la fin du combat
     * @param coins    pièces versées à la fin du combat
     * @param stake    Mise en cours à la fin du combat, en %
     * @param deck     le deck joué (id, exemplaires)
     * @param machine  ses rouleaux (noms de l'énumération des symboles)
     */
    public record Entry(String place, String dungeon, boolean hard, String rank, int prestige, boolean king,
                        boolean won, int turns, double gains, long coins, boolean golden, int stake,
                        Map<String, Integer> deck, List<String> machine) {

        /** @return la ligne du journal. */
        String line(LocalDateTime when) {
            List<String> cards = new ArrayList<>();
            deck.forEach((id, copies) -> cards.add(id + ':' + copies));
            return String.join(";", when.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), place, dungeon,
                String.valueOf(hard), rank == null ? "" : rank, String.valueOf(prestige), king ? "roi" : "soldat",
                String.valueOf(won), String.valueOf(turns), String.format(java.util.Locale.ROOT, "%.0f", gains),
                String.valueOf(coins), String.valueOf(golden), String.valueOf(stake), String.join("|", cards),
                String.join("|", machine));
        }
    }

    /** Ajoute {@code entry} à la fin de {@code file} (avec l'en-tête si le fichier est neuf). N'échoue jamais. */
    public static void append(Path file, Entry entry) {
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            StringBuilder text = new StringBuilder();
            if (!Files.exists(file)) text.append(HEADER).append('\n');
            text.append(entry.line(LocalDateTime.now())).append('\n');
            Files.writeString(file, text, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException | RuntimeException ignored) {
            // le journal n'est qu'une aide à l'équilibrage : la partie continue sans lui
        }
    }
}
