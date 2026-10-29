package fr.astratime.lucky.security;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Stream;

/**
 * Contrôle d'intégrité des assets protégés. Au build, on écrit une empreinte
 * SHA-256 de chaque fichier brouillé dans un manifeste embarqué ; au lancement,
 * on recompare. Si un fichier a été remplacé ou modifié (jeu trafiqué, « mod »),
 * on le signale au joueur via le Croupier, sans refuser de lancer le jeu (pour
 * ne jamais gêner une mise à jour légitime).
 *
 * <p>Absent en développement (le manifeste n'existe que dans un build
 * {@code -Pobfuscate}) : le contrôle est alors simplement sauté.
 */
public final class AssetIntegrity {

    /** Chemin du manifeste dans les ressources (non brouillé). */
    static final String MANIFEST_PATH = "security/integrity.sha256";

    private AssetIntegrity() {}

    // --- Build : écriture du manifeste -------------------------------------------

    /**
     * Écrit {@link #MANIFEST_PATH} sous {@code root} : une ligne
     * {@code chemin<TAB>sha256hex} par fichier brouillé, trié. À appeler APRÈS le
     * brouillage, pour que les empreintes portent sur les octets réellement
     * livrés.
     */
    public static void writeManifest(Path root) throws IOException {
        List<String> lines = new ArrayList<>();
        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : (Iterable<Path>) files.filter(Files::isRegularFile)::iterator) {
                String name = file.getFileName().toString();
                if (!AssetScrambler.shouldScramble(name)) continue;
                String rel = root.relativize(file).toString().replace('\\', '/');
                lines.add(rel + '\t' + sha256(Files.readAllBytes(file)));
            }
        }
        lines.sort(null);
        Path manifest = root.resolve(MANIFEST_PATH);
        Files.createDirectories(manifest.getParent());
        Files.write(manifest, String.join("\n", lines).getBytes(StandardCharsets.UTF_8));
        System.out.println("[AssetIntegrity] " + lines.size() + " empreinte(s) écrite(s) dans " + manifest);
    }

    // --- Lancement : vérification ------------------------------------------------

    /**
     * Vérifie les assets livrés contre le manifeste embarqué. À appeler AVANT
     * {@link ObfuscatedFiles#install()} : on compare les octets bruts (tels que
     * dans le jar), comme au build.
     *
     * @return le nombre de fichiers manquants ou modifiés ; 0 si tout est
     *         conforme ou si aucun manifeste n'est présent (mode développement)
     */
    public static int check() {
        FileHandle manifest = Gdx.files.internal(MANIFEST_PATH);
        if (!manifest.exists()) return 0;
        int problems = 0;
        for (String line : manifest.readString("UTF-8").split("\n")) {
            if (line.isBlank()) continue;
            int tab = line.lastIndexOf('\t');
            if (tab < 0) continue;
            String path = line.substring(0, tab);
            String expected = line.substring(tab + 1);
            try {
                FileHandle asset = Gdx.files.internal(path);
                if (!asset.exists() || !sha256(asset.readBytes()).equals(expected)) problems++;
            } catch (RuntimeException e) {
                problems++;
            }
        }
        if (problems > 0) Gdx.app.error("AssetIntegrity", problems + " fichier(s) modifié(s) ou manquant(s).");
        return problems;
    }

    // --- Commun ------------------------------------------------------------------

    private static String sha256(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
