package fr.astratime.lucky.security;

import com.badlogic.gdx.files.FileHandle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Le brouillage des assets est réversible et reconnaissable, et la couche de lecture le rétablit. */
class AssetScramblerTest {

    @Test
    void scrambleThenUnscrambleGivesBackTheOriginal() {
        byte[] original = "{\"carte\":\"as de trèfle\",\"lames\":42}".getBytes(StandardCharsets.UTF_8);
        byte[] scrambled = AssetScrambler.scramble(original);

        assertTrue(AssetScrambler.isScrambled(scrambled), "en-tête présent");
        assertFalse(AssetScrambler.isScrambled(original), "clair non détecté comme brouillé");
        assertArrayEquals(original, AssetScrambler.unscramble(scrambled), "réversible");
    }

    @Test
    void onlyArtAndDataExtensionsAreScrambled() {
        assertTrue(AssetScrambler.shouldScramble("cards/as.png"));
        assertTrue(AssetScrambler.shouldScramble("i18n/fr.json"));
        assertTrue(AssetScrambler.shouldScramble("assets.txt"));
        assertFalse(AssetScrambler.shouldScramble("music/boss/comete.mp3"));
        assertFalse(AssetScrambler.shouldScramble("fonts/jersey.ttf"));
    }

    @Test
    void readingLayerRestoresAScrambledFile(@TempDir Path dir) throws Exception {
        byte[] original = "contenu du jeu".getBytes(StandardCharsets.UTF_8);
        File file = dir.resolve("data.json").toFile();
        Files.write(file.toPath(), AssetScrambler.scramble(original));

        FileHandle wrapped = new ObfuscatedFileHandle(new FileHandle(file));
        assertArrayEquals(original, wrapped.readBytes(), "octets rétablis");
        assertEquals("contenu du jeu", wrapped.readString("UTF-8"), "texte rétabli");
    }

    @Test
    void readingLayerLeavesAPlainFileUnchanged(@TempDir Path dir) throws Exception {
        byte[] original = "déjà en clair".getBytes(StandardCharsets.UTF_8);
        File file = dir.resolve("plain.json").toFile();
        Files.write(file.toPath(), original);

        FileHandle wrapped = new ObfuscatedFileHandle(new FileHandle(file));
        assertArrayEquals(original, wrapped.readBytes(), "pas touché");
    }
}
