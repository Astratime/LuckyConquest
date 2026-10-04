package fr.astratime.lucky.progress;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Le deck en cours de construction : 20 cartes exactement, au plus 3 exemplaires, seulement des cartes possédées. */
class DeckDraftTest {

    private static PlayerProfile profile() {
        return new PlayerProfile(new PlayerProfileTest.MemoryStorage(), PlayerProfileTest.collection(),
            PlayerProfileTest.starterDeck());
    }

    @Test
    void itStartsFromThePlayersDeck() {
        DeckDraft draft = new DeckDraft(profile());
        assertEquals(PlayerProfile.DECK_SIZE, draft.size());
        assertTrue(draft.isComplete());
    }

    @Test
    void aFullDeckRefusesMoreCards() {
        DeckDraft draft = new DeckDraft(profile());
        assertNotNull(draft.addProblem("double"));
        assertFalse(draft.add("double"));
    }

    @Test
    void onlyOwnedCopiesCanBeAdded() {
        DeckDraft draft = new DeckDraft(profile());
        draft.remove("carte_0");
        draft.remove("carte_1");
        draft.remove("carte_2");

        assertFalse(draft.add("carte_3"), "une seule carte_3 possédée");
        assertTrue(draft.add("double"));
        assertTrue(draft.add("double"));
        assertFalse(draft.add("double"), "deux exemplaires possédés");
        assertFalse(draft.add("inconnue"));
        assertEquals(2, draft.getMaxCopies("double"));
    }

    @Test
    void atMostThreeCopiesOfACardEvenWhenMoreAreOwned() {
        PlayerProfile profile = profile();
        for (int i = 0; i < 5; i++) profile.openChest("rare");
        DeckDraft draft = new DeckDraft(profile);
        for (int i = 0; i < 4; i++) draft.remove("carte_" + i);

        assertTrue(draft.add("rare"));
        assertTrue(draft.add("rare"));
        assertTrue(draft.add("rare"));
        assertFalse(draft.add("rare"));
        assertEquals(PlayerProfile.MAX_COPIES, draft.getMaxCopies("rare"));
    }

    @Test
    void onlyACompleteDeckIsSaved() {
        PlayerProfile profile = profile();
        DeckDraft draft = new DeckDraft(profile);
        draft.remove("carte_0");
        assertFalse(draft.save());
        assertEquals(PlayerProfileTest.starterDeck(), profile.getDeck());

        draft.add("double");
        assertTrue(draft.save());
        assertEquals(1, profile.getDeck().get("double"));
        assertNull(profile.getDeck().get("carte_0"));
    }

    @Test
    void clearAndResetToStarter() {
        DeckDraft draft = new DeckDraft(profile());
        draft.clear();
        assertEquals(0, draft.size());
        draft.resetToStarter();
        assertEquals(PlayerProfileTest.starterDeck(), draft.getCopies());
    }
}
