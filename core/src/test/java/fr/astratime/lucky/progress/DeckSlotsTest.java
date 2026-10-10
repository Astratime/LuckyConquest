package fr.astratime.lucky.progress;

import fr.astratime.lucky.progress.PlayerProfileTest.MemoryStorage;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static fr.astratime.lucky.progress.PlayerProfileTest.profile;
import static fr.astratime.lucky.progress.PlayerProfileTest.starterDeck;
import static org.junit.jupiter.api.Assertions.*;

/** Les trois decks enregistrés à la Table du croupier, et le choix du deck joué. */
class DeckSlotsTest {

    /** Le deck de départ avec "double" x2 à la place de deux cartes. */
    private static Map<String, Integer> otherDeck() {
        Map<String, Integer> deck = new LinkedHashMap<>(starterDeck());
        deck.remove("carte_0");
        deck.remove("carte_1");
        deck.put("double", 2);
        return deck;
    }

    @Test
    void anOldSaveHasItsDeckInTheFirstSlotAndTheOthersEmpty() {
        PlayerProfile profile = profile(new MemoryStorage());

        assertEquals(0, profile.getDeckSlot());
        assertEquals(starterDeck(), profile.getDeckInSlot(0));
        assertFalse(profile.isDeckSlotUsed(1));
        assertFalse(profile.selectDeckSlot(1), "un emplacement vide ne se joue pas");
        assertEquals(0, profile.nextUsedDeckSlot(0), "un seul deck : rien à changer");
    }

    @Test
    void savingInASlotPlaysItAndBothDecksAreKeptForTheNextLaunch() {
        MemoryStorage storage = new MemoryStorage();
        PlayerProfile profile = profile(storage);
        profile.setDeck(1, otherDeck());

        assertEquals(1, profile.getDeckSlot());
        assertEquals(otherDeck(), profile.getDeck());
        assertEquals(starterDeck(), profile.getDeckInSlot(0));

        PlayerProfile reloaded = profile(storage);
        assertEquals(1, reloaded.getDeckSlot());
        assertEquals(otherDeck(), reloaded.getDeck());
        assertEquals(0, reloaded.nextUsedDeckSlot(1));
        assertTrue(reloaded.selectDeckSlot(0));
        assertEquals(starterDeck(), reloaded.getDeck());
        assertEquals(0, profile(storage).getDeckSlot());
    }

    @Test
    void theCurrentDeckCanBeCopiedIntoAnotherSlot() {
        PlayerProfile profile = profile(new MemoryStorage());
        profile.setDeck(2, profile.getDeck());

        assertEquals(starterDeck(), profile.getDeckInSlot(2));
        assertEquals(starterDeck(), profile.getDeck());
    }

    @Test
    void adminModeKeepsItsOwnDeckAndDoesNotTouchTheSlots() {
        PlayerProfile profile = profile(new MemoryStorage());
        profile.setDeck(1, otherDeck());
        profile.setAdmin(true);
        assertFalse(profile.selectDeckSlot(0));
        profile.setAdmin(false);
        assertEquals(1, profile.getDeckSlot());
        assertEquals(otherDeck(), profile.getDeck());
    }
}
