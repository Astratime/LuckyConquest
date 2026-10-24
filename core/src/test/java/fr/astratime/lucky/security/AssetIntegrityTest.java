package fr.astratime.lucky.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

/** Le manifeste d'intégrité liste les fichiers brouillés avec leur empreinte réelle. */
class AssetIntegrityTest {

    @Test
    void manifestListsScrambledFilesWithTheirHash(@TempDir Path root) throws Exception {
        Files.writeString(root.resolve("data.json"), "x");            // sera brouillé puis listé
        Files.createDirectories(root.resolve("music"));
        Files.writeString(root.resolve("music/theme.mp3"), "son");    // en clair, non listé

        // Reproduit l'étape de build : brouillage puis écriture du manifeste.
        AssetScrambler.main(new String[]{root.toString()});

        Path manifest = root.resolve(AssetIntegrity.MANIFEST_PATH);
        assertTrue(Files.exists(manifest), "manifeste écrit");
        String content = Files.readString(manifest);

        assertTrue(content.contains("data.json\t"), "le json brouillé est listé");
        assertFalse(content.contains("theme.mp3"), "le son en clair n'est pas listé");

        // L'empreinte correspond aux octets réellement livrés (brouillés).
        String expected = HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(root.resolve("data.json"))));
        assertTrue(content.contains("data.json\t" + expected), "empreinte des octets brouillés");
    }

    @Test
    void scrambleKeyIsStableAcrossCalls() {
        // La clé dérivée doit être déterministe : même entrée, même sortie.
        byte[] a = AssetScrambler.scramble("test".getBytes(StandardCharsets.UTF_8));
        byte[] b = AssetScrambler.scramble("test".getBytes(StandardCharsets.UTF_8));
        assertArrayEquals(a, b);
    }
}
