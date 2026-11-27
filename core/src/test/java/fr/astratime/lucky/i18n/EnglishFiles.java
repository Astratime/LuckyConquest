package fr.astratime.lucky.i18n;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Pour les tests : toutes les traductions anglaises, lues dans les fichiers {@code assets/i18n/en/} (voir {@link Lang#PARTS}). */
public final class EnglishFiles {

    private EnglishFiles() { }

    /** @return texte français → texte anglais, tous thèmes réunis */
    public static Map<String, String> read() {
        Map<String, String> all = new HashMap<>();
        for (String part : Lang.PARTS) {
            Path file = Path.of("..", "assets").resolve(Lang.partPath(Lang.Language.ENGLISH, part));
            try {
                all.putAll(Lang.parse(Files.readString(file, StandardCharsets.UTF_8)));
            } catch (IOException e) {
                throw new UncheckedIOException(file + " illisible", e);
            }
        }
        return all;
    }
}
