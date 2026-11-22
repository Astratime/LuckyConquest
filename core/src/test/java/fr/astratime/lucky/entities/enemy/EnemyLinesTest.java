package fr.astratime.lucky.entities.enemy;

import fr.astratime.lucky.i18n.Lang;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Réactions des ennemis (D2) : chaque roi et chaque boss de la Tour parle, en français et en anglais. */
class EnemyLinesTest {

    @Test
    void everyKingAndEveryTowerBossSpeaks() {
        Set<EnemyKind> bosses = EnumSet.noneOf(EnemyKind.class);
        for (EnemyKind kind : EnemyKind.values()) if (kind.isBoss()) bosses.add(kind);
        assertEquals(bosses, EnemyLines.speakers());
        assertEquals(41 + fr.astratime.lucky.entities.tower.Chapter.values().length, bosses.size(), "40 rois, un boss par chapitre de la Tour et le Croupier");
        assertFalse(EnemyLines.speaks(EnemyKind.SOLDAT_PIQUE));
        assertNull(EnemyLines.bingo(EnemyKind.SOLDAT_PIQUE, 0));
        assertNull(EnemyLines.lowHp(EnemyKind.CROUPIER));
    }

    @Test
    void threeToFiveLinesWithOneForEachPhase() {
        for (EnemyKind kind : EnemyLines.speakers()) {
            List<String> lines = EnemyLines.all(kind);
            assertTrue(lines.size() >= 3 && lines.size() <= 5, kind + " : " + lines.size() + " phrases");
            assertEquals(kind.phaseCount() - 1, EnemyLines.phaseLines(kind), kind + " : une phrase par phase");
            for (int phase = 2; phase <= kind.phaseCount(); phase++) assertNotNull(EnemyLines.phase(kind, phase), kind.name());
            assertNull(EnemyLines.phase(kind, kind.phaseCount() + 1));
            assertNull(EnemyLines.phase(kind, 1));
            for (String line : lines) {
                assertFalse(line.contains("œ") || line.contains("Œ"), kind + " : la police n'a pas de œ");
                assertTrue(line.length() <= 60, kind + " : phrase trop longue pour la bulle : " + line);
            }
        }
    }

    @Test
    void bingoLinesAlternate() {
        assertNotEquals(EnemyLines.bingo(EnemyKind.ROI_PIQUE, 0), EnemyLines.bingo(EnemyKind.ROI_PIQUE, 1));
        assertEquals(EnemyLines.bingo(EnemyKind.ROI_PIQUE, 0), EnemyLines.bingo(EnemyKind.ROI_PIQUE, 2));
    }

    @Test
    void underTenPercentIsLow() {
        assertTrue(EnemyLines.isLow(9, 100));
        assertFalse(EnemyLines.isLow(10, 100));
        assertFalse(EnemyLines.isLow(0, 100), "vaincu : plus de phrase");
        assertTrue(EnemyLines.isLow(199_999_999, 2_000_000_000L));
    }

    @Test
    void everyLineIsTranslated() throws Exception {
        String json = Files.readString(Path.of("..", "assets", "i18n", "en.json"), StandardCharsets.UTF_8);
        Map<String, String> english = Lang.parse(json);
        for (EnemyKind kind : EnemyLines.speakers()) {
            for (String line : EnemyLines.all(kind)) {
                assertTrue(english.containsKey(line), kind + " : sans traduction anglaise : " + line);
                assertFalse(english.get(line).isBlank(), line);
            }
        }
    }
}
