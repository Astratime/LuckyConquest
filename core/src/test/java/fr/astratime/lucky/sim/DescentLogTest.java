package fr.astratime.lucky.sim;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.progress.DescentLog;
import fr.astratime.lucky.progress.Rank;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Le journal des descentes, écrit par le jeu, se relit dans le simulateur. */
class DescentLogTest {

    private static DescentLog.Entry entry(Dungeon dungeon, boolean king, boolean won, long coins) {
        Map<String, Integer> deck = new LinkedHashMap<>();
        deck.put("1_pique", 1);
        deck.put("draw_2", 1);
        return new DescentLog.Entry(Place.PRAIRIE.name(), dungeon.name(), false, Rank.BARON.name(), 2, king, won, 5,
            12_345, coins, false, 15, deck, List.of(Symbol.SEVEN.name(), Symbol.BAR.name()));
    }

    @Test
    void descentsAreRebuiltFromSoldierAndKingLines(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("journal.csv");
        Dungeon pique = Place.PRAIRIE.getDungeons().get(0);
        DescentLog.append(file, entry(pique, false, true, 0));    // soldat battu
        DescentLog.append(file, entry(pique, true, true, 9_000)); // roi battu : descente gagnée
        DescentLog.append(file, entry(pique, false, false, 300)); // soldat perdu : descente perdue
        DescentLog.append(file, entry(pique, false, true, 0));    // soldat battu, roi jamais joué : ignoré

        assertTrue(Files.readAllLines(file).get(0).startsWith("date;place"));
        List<BalanceSimulator.LoggedDescent> descents = BalanceSimulator.readJournal(file);
        assertEquals(2, descents.size());
        assertTrue(descents.get(0).won());
        assertEquals(9_000, descents.get(0).coins());
        assertFalse(descents.get(1).won());
        assertEquals(Rank.BARON, descents.get(0).rank());
        assertEquals(2, descents.get(0).prestige());
        assertEquals(List.of(Symbol.SEVEN, Symbol.BAR), descents.get(0).machine());
        assertEquals(Map.of("1_pique", 1, "draw_2", 1), descents.get(0).deck());
    }
}
