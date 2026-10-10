package fr.astratime.lucky.progress;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Le rapport de plantage est écrit, puis annoncé une seule fois. */
class CrashReportTest {

    @TempDir Path dir;

    @Test
    void aCrashIsWrittenWithItsStackTraceThenAnnouncedOnce() throws Exception {
        Path file = dir.resolve(".prefs").resolve("lucky-conquest-crash.log");
        assertFalse(CrashReport.takePending(file));

        CrashReport.write(file, new IllegalStateException("rouleau coincé"));

        String report = Files.readString(file);
        assertTrue(report.startsWith("Lucky Conquest : rapport de plantage"));
        assertTrue(report.contains("IllegalStateException: rouleau coincé"));
        assertTrue(report.contains("CrashReportTest"), "la pile d'appels est dans le rapport");
        assertTrue(CrashReport.takePending(file));
        assertFalse(CrashReport.takePending(file), "annoncé une seule fois");
        assertTrue(Files.exists(file), "le rapport reste sur le disque");
    }

    @Test
    void aNewCrashReplacesTheOldReport() throws Exception {
        Path file = dir.resolve("crash.log");
        CrashReport.write(file, new RuntimeException("premier"));
        CrashReport.write(file, new RuntimeException("second"));
        String report = Files.readString(file);
        assertTrue(report.contains("second"));
        assertFalse(report.contains("premier"));
    }

    @Test
    void anUnwritableFolderDoesNotThrow() throws Exception {
        Path blocker = Files.writeString(dir.resolve("pas-un-dossier"), "");
        assertDoesNotThrow(() -> CrashReport.write(blocker.resolve("crash.log"), new RuntimeException("x")));
        assertFalse(CrashReport.takePending(blocker.resolve("crash.log")));
    }
}
