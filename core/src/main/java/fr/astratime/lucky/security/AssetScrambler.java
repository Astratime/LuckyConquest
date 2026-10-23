package fr.astratime.lucky.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Brouillage des fichiers d'assets (art + données) pour qu'ils ne soient pas
 * lisibles tels quels dans le .jar livré.
 *
 * <p>Ce n'est PAS du chiffrement : un fichier brouillé reste réversible sans
 * secret, le but est seulement de compliquer l'extraction (on ne peut plus
 * ouvrir une image ou lire un JSON en dézippant le jar). Le vrai verrou reste
 * l'obfuscation du code et la signature des sauvegardes.
 *
 * <p>Format d'un fichier brouillé : l'en-tête {@link #MAGIC} (4 octets) suivi
 * des octets d'origine passés dans un XOR à clé roulante. La présence de
 * l'en-tête permet à la couche de lecture ({@link ObfuscatedFiles}) de
 * reconnaître un fichier brouillé et de le rétablir à la volée ; un fichier non
 * brouillé (mode développement) est lu normalement. Le même jeu tourne donc
 * avec des assets brouillés (build livré) ou en clair (dev), sans recompiler.
 */
public final class AssetScrambler {

    /** En-tête reconnaissable en tête d'un fichier brouillé ("LCX1"). */
    static final byte[] MAGIC = {'L', 'C', 'X', '1'};

    /** Clé du XOR roulant. Volontairement fixe : simple barrière, pas un secret. */
    private static final byte[] KEY =
        "LuckyConquest/astratime/©".getBytes(java.nio.charset.StandardCharsets.UTF_8);

    /**
     * Extensions brouillées : l'art et les données du jeu, lus via
     * {@code readBytes()} / {@code readString()} (chemin entièrement maîtrisé par
     * la couche de lecture). Les sons, musiques, polices et effets de particules
     * sont volontairement laissés en clair : ils sont lus en streaming ou par des
     * chemins natifs que la couche n'intercepte pas, et ne sont pas le cœur du
     * contenu à protéger.
     */
    private static final List<String> SCRAMBLED_EXTENSIONS =
        List.of(".png", ".json", ".txt", ".fnt", ".atlas");

    /**
     * Fichiers laissés en clair bien qu'ayant une extension brouillable : les
     * icônes de la fenêtre ({@code libgdx*.png}) sont lues par le backend LWJGL3
     * à la création de la fenêtre, AVANT que {@link ObfuscatedFiles} ne soit
     * posé ; brouillées, elles seraient illisibles au lancement.
     */
    private static boolean isReadBeforeLayer(String name) {
        String file = name.substring(name.replace('\\', '/').lastIndexOf('/') + 1).toLowerCase(Locale.ROOT);
        return file.startsWith("libgdx") && file.endsWith(".png");
    }

    private AssetScrambler() {}

    /** @return vrai si {@code name} a une extension à brouiller (et n'est pas lu trop tôt). */
    public static boolean shouldScramble(String name) {
        if (isReadBeforeLayer(name)) return false;
        String lower = name.toLowerCase(Locale.ROOT);
        for (String extension : SCRAMBLED_EXTENSIONS) {
            if (lower.endsWith(extension)) return true;
        }
        return false;
    }

    /** @return vrai si {@code data} commence par l'en-tête d'un fichier brouillé. */
    public static boolean isScrambled(byte[] data) {
        if (data == null || data.length < MAGIC.length) return false;
        for (int i = 0; i < MAGIC.length; i++) {
            if (data[i] != MAGIC[i]) return false;
        }
        return true;
    }

    /** @return les octets d'origine précédés de l'en-tête et passés au XOR. */
    public static byte[] scramble(byte[] plain) {
        byte[] out = new byte[MAGIC.length + plain.length];
        System.arraycopy(MAGIC, 0, out, 0, MAGIC.length);
        for (int i = 0; i < plain.length; i++) {
            out[MAGIC.length + i] = (byte) (plain[i] ^ KEY[i % KEY.length]);
        }
        return out;
    }

    /**
     * @param scrambled octets d'un fichier brouillé (en-tête compris)
     * @return les octets d'origine
     */
    public static byte[] unscramble(byte[] scrambled) {
        int length = scrambled.length - MAGIC.length;
        byte[] out = new byte[length];
        for (int i = 0; i < length; i++) {
            out[i] = (byte) (scrambled[MAGIC.length + i] ^ KEY[i % KEY.length]);
        }
        return out;
    }

    /**
     * Brouille sur place, au moment du build, tous les fichiers éligibles d'un
     * dossier (appelé par Gradle sur {@code build/resources/main}). Un fichier
     * déjà brouillé est ignoré (tâche idempotente).
     *
     * @param args {@code args[0]} = dossier racine des ressources à brouiller
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage : AssetScrambler <dossier-ressources>");
            System.exit(2);
        }
        Path root = Path.of(args[0]);
        if (!Files.isDirectory(root)) {
            System.out.println("[AssetScrambler] rien à brouiller : " + root + " absent.");
            return;
        }
        int[] count = {0};
        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : (Iterable<Path>) files.filter(Files::isRegularFile)::iterator) {
                if (!shouldScramble(file.getFileName().toString())) continue;
                byte[] data = Files.readAllBytes(file);
                if (isScrambled(data)) continue;
                Files.write(file, scramble(data));
                count[0]++;
            }
        }
        System.out.println("[AssetScrambler] " + count[0] + " fichier(s) brouillé(s) dans " + root);
    }
}
