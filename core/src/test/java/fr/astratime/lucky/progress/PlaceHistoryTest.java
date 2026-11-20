package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.exploration.Place;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Historique de l'Exploration : compté par lieu et par mode, enregistré, jamais en mode ADMIN. */
class PlaceHistoryTest {

    @Test
    void fightsAreCountedPerPlaceAndModeAndSurviveASaveAndReload() {
        PlayerProfileTest.MemoryStorage storage = new PlayerProfileTest.MemoryStorage();
        PlayerProfile profile = PlayerProfileTest.profile(storage);
        profile.recordFight(Place.PRAIRIE, false, false, true, 1_000);
        profile.recordFight(Place.PRAIRIE, false, true, false, 300);
        profile.recordFight(Place.PRAIRIE, true, true, true, 9_000);

        PlaceHistory.Line normal = PlayerProfileTest.profile(storage).getHistory().get(Place.PRAIRIE, false);
        assertEquals(2, normal.fights());
        assertEquals(50, normal.winPercent());
        assertEquals(1, normal.kings());
        assertEquals(0, normal.kingWins());
        assertEquals(650, normal.coinsPerFight());
        assertEquals(1_000, normal.best());
        PlaceHistory.Line hard = PlayerProfileTest.profile(storage).getHistory().get(Place.PRAIRIE, true);
        assertEquals(1, hard.kingWins());
        assertEquals(0, profile.getHistory().get(Place.PORT, false).fights());
    }

    @Test
    void unreadableOrUnknownLinesAreIgnored() {
        PlaceHistory history = PlaceHistory.decode("NOWHERE:1,1,1,1,1,1;PORT:2,1,0,0,abc,5;MINES#D:3,2,1,1,600,400");
        assertEquals(0, history.get(Place.PORT, false).fights());
        assertEquals(3, history.get(Place.MINES, true).fights());
        assertEquals("MINES#D:3,2,1,1,600,400", history.encode());
    }
}
