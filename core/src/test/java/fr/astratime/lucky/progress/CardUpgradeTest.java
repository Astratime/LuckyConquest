package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.Card;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Fusion de 3 exemplaires d'une carte en sa version « + », à la Table du croupier. */
class CardUpgradeTest {

    /** Collection : 20 cartes "carte_i" en 1 exemplaire, et 3 exemplaires de "triple". */
    private static Map<String, Integer> collection() {
        Map<String, Integer> collection = new LinkedHashMap<>();
        collection.put("triple", 3);
        for (int i = 0; i < 20; i++) collection.put("carte_" + i, 1);
        return collection;
    }

    /** Deck : les 3 "triple" et 17 "carte_i". */
    private static Map<String, Integer> deck() {
        Map<String, Integer> deck = new LinkedHashMap<>();
        deck.put("triple", 3);
        for (int i = 0; i < 17; i++) deck.put("carte_" + i, 1);
        return deck;
    }

    private static PlayerProfile profile(PlayerProfileTest.MemoryStorage storage) {
        PlayerProfile profile = new PlayerProfile(storage, collection(), deck());
        profile.setUpgradable(List.of("triple", "carte_0"));
        if (profile.getCoins() == 0) profile.addCoins(Card.UPGRADE_PRICE + 500);
        return profile;
    }

    @Test
    void threeCopiesBecomeOnePlusCard() {
        PlayerProfile profile = profile(new PlayerProfileTest.MemoryStorage());
        assertNull(profile.upgradeProblem("triple"));

        assertTrue(profile.upgradeCard("triple"));

        assertEquals(0, profile.getOwnedCopies("triple"));
        assertEquals(1, profile.getOwnedCopies("triple+"));
        assertEquals(500, profile.getCoins(), "la fusion coûte 10 millions de pièces");
        assertEquals("triple+", profile.getCollection().keySet().iterator().next(), "à la place de la carte fusionnée");
    }

    @Test
    void thePlusCardTakesTheDeckPlaceAndTheCollectionFillsTheRest() {
        PlayerProfile profile = profile(new PlayerProfileTest.MemoryStorage());
        profile.upgradeCard("triple");

        Map<String, Integer> deck = profile.getDeck();
        assertNull(profile.deckProblem(deck), "le deck enregistré reste valide");
        assertEquals(1, deck.get("triple+"));
        assertFalse(deck.containsKey("triple"));
        assertEquals(1, deck.get("carte_17"));
        assertEquals(1, deck.get("carte_18"));
    }

    @Test
    void theFusionIsSaved() {
        PlayerProfileTest.MemoryStorage storage = new PlayerProfileTest.MemoryStorage();
        profile(storage).upgradeCard("triple");

        PlayerProfile again = profile(storage);
        assertEquals(1, again.getOwnedCopies("triple+"));
        assertEquals(1, again.getDeck().get("triple+"));
    }

    @Test
    void aFusionNeedsThreeCopiesOfACardThatHasAPlusVersion() {
        PlayerProfile profile = profile(new PlayerProfileTest.MemoryStorage());
        assertNotNull(profile.upgradeProblem("carte_0"), "un seul exemplaire");
        assertNotNull(profile.upgradeProblem("carte_1"), "pas de version +");
        assertNotNull(profile.upgradeProblem("triple+"), "déjà une carte +");
        assertFalse(profile.upgradeCard("carte_0"));
        assertEquals(1, profile.getOwnedCopies("carte_0"));
    }

    @Test
    void aFusionCostsTenMillionCoins() {
        PlayerProfile profile = new PlayerProfile(new PlayerProfileTest.MemoryStorage(), collection(), deck());
        profile.setUpgradable(List.of("triple"));
        profile.addCoins(Card.UPGRADE_PRICE - 1);

        assertNull(profile.upgradeOffer("triple"), "FUSIONNER s'affiche, la fenêtre dit le prix");
        assertNotNull(profile.upgradeProblem("triple"), "il manque une pièce");
        assertFalse(profile.upgradeCard("triple"));
        assertEquals(3, profile.getOwnedCopies("triple"));
        assertEquals(Card.UPGRADE_PRICE - 1, profile.getCoins());
    }

    @Test
    void aFusionIsRefusedWhenTheDeckCouldNotBeFilled() {
        Map<String, Integer> small = new LinkedHashMap<>();
        small.put("triple", 3);
        for (int i = 0; i < 17; i++) small.put("carte_" + i, 1);
        PlayerProfile profile = new PlayerProfile(new PlayerProfileTest.MemoryStorage(), small, deck());
        profile.setUpgradable(List.of("triple"));
        profile.addCoins(Card.UPGRADE_PRICE);

        assertNotNull(profile.upgradeProblem("triple"), "18 cartes après la fusion");
        assertFalse(profile.upgradeCard("triple"));
    }

    @Test
    void theAdminModeHasThePlusCardsButCannotFuse() {
        PlayerProfile profile = profile(new PlayerProfileTest.MemoryStorage());
        profile.setCatalog(List.of("triple"));
        profile.setAdmin(true);

        assertEquals(PlayerProfile.MAX_COPIES, profile.getOwnedCopies("triple+"));
        assertNotNull(profile.upgradeProblem("triple"));
    }

    @Test
    void theDraftSwapsTheFusedCopiesForThePlusCard() {
        PlayerProfile profile = profile(new PlayerProfileTest.MemoryStorage());
        DeckDraft draft = new DeckDraft(profile);
        profile.upgradeCard("triple");

        draft.afterUpgrade("triple");

        assertEquals(0, draft.getCopies("triple"));
        assertEquals(1, draft.getCopies("triple+"));
        assertEquals(PlayerProfile.DECK_SIZE - 2, draft.size(), "le joueur complète son deck");
    }

    @Test
    void theStarterDeckUsesThePlusCardOnceTheCardIsFused() {
        PlayerProfile profile = profile(new PlayerProfileTest.MemoryStorage());
        profile.upgradeCard("triple");
        DeckDraft draft = new DeckDraft(profile);

        draft.resetToStarter();

        assertEquals(1, draft.getCopies(Card.upgradedId("triple")));
        assertEquals(0, draft.getCopies("triple"));
    }
}
