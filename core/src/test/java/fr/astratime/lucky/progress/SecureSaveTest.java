package fr.astratime.lucky.progress;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** La sauvegarde signée : brouillée, refusée si on la modifie, sa copie de secours et la reprise de l'ancien profil. */
class SecureSaveTest {

    @TempDir Path folder;

    private static Map<String, String> profile() {
        Map<String, String> values = new HashMap<>();
        values.put("coins", "125000");
        values.put("deck", "1_pique:2,13_trefle:3");
        values.put("guides", "tutorial,menu");
        return values;
    }

    @Test
    void anEncodedSaveReadsBackTheSameValues() throws Exception {
        assertEquals(profile(), SaveCodec.decode(SaveCodec.encode(profile())));
    }

    @Test
    void theValuesCannotBeReadInATextEditor() {
        String text = SaveCodec.encode(profile());
        assertFalse(text.contains("125000"));
        assertFalse(text.contains("coins"));
        assertTrue(text.contains("version=" + SaveCodec.VERSION));
    }

    @Test
    void aSaveChangedByHandIsRefused() {
        String text = SaveCodec.encode(profile());
        String data = text.lines().filter(line -> line.startsWith("data=")).findFirst().orElseThrow();
        char last = data.charAt(data.length() - 2);
        String changed = text.replace(data, data.substring(0, data.length() - 2) + (last == 'A' ? 'B' : 'A')
            + data.charAt(data.length() - 1));
        assertThrows(SaveCodec.InvalidSaveException.class, () -> SaveCodec.decode(changed));

        String otherSave = SaveCodec.encode(Map.of("coins", "999999999"));
        String sign = otherSave.lines().filter(line -> line.startsWith("sign=")).findFirst().orElseThrow();
        String mixed = text.replaceAll("sign=.*", sign);
        assertThrows(SaveCodec.InvalidSaveException.class, () -> SaveCodec.decode(mixed));
        assertThrows(SaveCodec.InvalidSaveException.class, () -> SaveCodec.decode("coins=999999999"));
        assertThrows(SaveCodec.InvalidSaveException.class,
            () -> SaveCodec.decode(text.replace("version=1", "version=99")));
    }

    @Test
    void theSaveSurvivesARelaunchAndTheOldSaveBecomesTheBackup() throws Exception {
        Path file = folder.resolve("save");
        SecureProfileStorage storage = new SecureProfileStorage(file, Map::of, () -> { });
        storage.put("coins", "10");
        storage.flush();
        assertFalse(Files.exists(folder.resolve("save.bak")), "pas de copie avant la première sauvegarde");
        storage.put("coins", "20");
        storage.flush();

        assertEquals("20", new SecureProfileStorage(file, Map::of, () -> { }).get("coins"));
        assertEquals("10", SaveCodec.decode(Files.readString(folder.resolve("save.bak"))).get("coins"));
    }

    @Test
    void aBrokenSaveFallsBackToTheBackup() throws Exception {
        Path file = folder.resolve("save");
        SecureProfileStorage storage = new SecureProfileStorage(file, Map::of, () -> { });
        storage.put("coins", "10");
        storage.flush();
        storage.put("coins", "20");
        storage.flush();
        Files.writeString(file, Files.readString(file).replace("sign=", "sign=0"), StandardCharsets.UTF_8);

        SecureProfileStorage reloaded = new SecureProfileStorage(file, Map::of, () -> { });
        assertTrue(reloaded.isRestoredFromBackup());
        assertEquals("10", reloaded.get("coins"));
    }

    @Test
    void aSaveAndBackupChangedByHandStartANewProfileAndKeepTheRefusedFile() throws Exception {
        Path file = folder.resolve("save");
        Files.writeString(file, "coins=999999999");
        Files.writeString(folder.resolve("save.bak"), "coins=999999999");

        SecureProfileStorage storage = new SecureProfileStorage(file, () -> Map.of("coins", "5"), () -> { });
        assertTrue(storage.isRejected());
        assertNull(storage.get("coins"), "ni la triche ni l'ancien profil");
        assertTrue(Files.exists(folder.resolve("save.refuse")));
    }

    @Test
    void theOldUnsignedProfileIsTakenOnceThenCleared() throws Exception {
        Path file = folder.resolve("save");
        Map<String, String> legacy = new HashMap<>(profile());
        SecureProfileStorage storage = new SecureProfileStorage(file, () -> new HashMap<>(legacy), legacy::clear);

        assertEquals("125000", storage.get("coins"));
        assertTrue(Files.exists(file), "écrit tout de suite au nouveau format");
        assertTrue(legacy.isEmpty(), "l'ancien profil est effacé");
        assertEquals("125000", new SecureProfileStorage(file, Map::of, () -> { }).get("coins"));
    }
}
