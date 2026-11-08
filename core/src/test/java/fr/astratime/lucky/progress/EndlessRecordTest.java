package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.tower.Chapter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EndlessRecordTest {

    @Test
    void theEndlessModeOpensAfterHardChapterSix() {
        PlayerProfile profile = PlayerProfileTest.profile(new PlayerProfileTest.MemoryStorage());
        assertFalse(profile.isEndlessOpen());
        profile.clearChapter(Chapter.LE_JACKPOT, false);
        assertFalse(profile.isEndlessOpen(), "le chapitre 6 normal ne suffit pas");
        profile.clearChapter(Chapter.LE_JACKPOT, true);
        assertTrue(profile.isEndlessOpen());
    }

    @Test
    void theBestFloorIsKeptAndSaved() {
        PlayerProfileTest.MemoryStorage storage = new PlayerProfileTest.MemoryStorage();
        PlayerProfile profile = PlayerProfileTest.profile(storage);
        assertEquals(0, profile.getEndlessBest());
        assertTrue(profile.recordEndlessFloor(3));
        assertFalse(profile.recordEndlessFloor(2), "pas un record");
        assertFalse(profile.recordEndlessFloor(3), "égaler n'est pas battre");
        assertTrue(profile.recordEndlessFloor(7));
        assertEquals(7, PlayerProfileTest.profile(storage).getEndlessBest(), "relu depuis la sauvegarde");
    }

    @Test
    void adminFightsRecordNothing() {
        PlayerProfile profile = PlayerProfileTest.profile(new PlayerProfileTest.MemoryStorage());
        profile.setAdmin(true);
        assertTrue(profile.isEndlessOpen());
        assertFalse(profile.recordEndlessFloor(10));
        assertEquals(0, profile.getEndlessBest());
    }
}
