package fr.astratime.lucky.progress;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** La collection, le deck et les pièces du joueur, et leur enregistrement d'un lancement à l'autre. */
class PlayerProfileTest {

    /** Profil enregistré en mémoire : un nouveau {@link PlayerProfile} sur le même stockage simule un nouveau lancement. */
    static final class MemoryStorage implements ProfileStorage {
        final Map<String, String> values = new HashMap<>();
        int flushes;

        @Override public String get(String key) { return values.get(key); }
        @Override public void put(String key, String value) { values.put(key, value); }
        @Override public void flush() { flushes++; }
    }

    /** Collection de départ : 20 cartes différentes en 1 exemplaire, plus 2 exemplaires de "double". */
    static Map<String, Integer> collection() {
        Map<String, Integer> collection = new LinkedHashMap<>();
        for (int i = 0; i < 20; i++) collection.put("carte_" + i, 1);
        collection.put("double", 2);
        return collection;
    }

    /** Deck de départ : les 20 cartes "carte_i". */
    static Map<String, Integer> starterDeck() {
        Map<String, Integer> deck = new LinkedHashMap<>();
        for (int i = 0; i < 20; i++) deck.put("carte_" + i, 1);
        return deck;
    }

    static PlayerProfile profile(MemoryStorage storage) {
        return new PlayerProfile(storage, collection(), starterDeck());
    }

    @Test
    void theFirstLaunchGivesTheStartingCollectionTheStarterDeckAndNoCoins() {
        PlayerProfile profile = profile(new MemoryStorage());

        assertEquals(collection(), profile.getCollection());
        assertEquals(starterDeck(), profile.getDeck());
        assertEquals(0L, profile.getCoins());
    }

    @Test
    void aDeckMustHaveExactlyTwentyOwnedCardsAndAtMostThreeCopiesOfEach() {
        PlayerProfile profile = profile(new MemoryStorage());

        Map<String, Integer> nineteen = new LinkedHashMap<>(starterDeck());
        nineteen.remove("carte_0");
        assertNotNull(profile.deckProblem(nineteen), "19 cartes");

        Map<String, Integer> twentyOne = new LinkedHashMap<>(starterDeck());
        twentyOne.put("double", 1);
        assertNotNull(profile.deckProblem(twentyOne), "21 cartes");

        Map<String, Integer> notOwned = new LinkedHashMap<>(nineteen);
        notOwned.put("inconnue", 1);
        assertNotNull(profile.deckProblem(notOwned), "carte pas possédée");

        Map<String, Integer> tooMany = new LinkedHashMap<>(nineteen);
        tooMany.put("carte_1", 2);
        assertNotNull(profile.deckProblem(tooMany), "plus d'exemplaires que possédés");

        Map<String, Integer> withDouble = new LinkedHashMap<>(nineteen);
        withDouble.remove("carte_1");
        withDouble.put("double", 2);
        assertNull(profile.deckProblem(withDouble));
        assertThrows(IllegalArgumentException.class, () -> profile.setDeck(nineteen));
    }

    @Test
    void noCardCanHaveMoreThanThreeCopiesInTheDeckEvenIfOwned() {
        MemoryStorage storage = new MemoryStorage();
        PlayerProfile profile = profile(storage);
        for (int i = 0; i < 5; i++) profile.openChest("rare");

        Map<String, Integer> deck = new LinkedHashMap<>();
        for (int i = 0; i < 16; i++) deck.put("carte_" + i, 1);
        deck.put("rare", 4);
        assertNotNull(profile.deckProblem(deck));
    }

    @Test
    void theDeckCoinsAndCollectionAreSavedForTheNextLaunch() {
        MemoryStorage storage = new MemoryStorage();
        PlayerProfile profile = profile(storage);
        Map<String, Integer> deck = new LinkedHashMap<>(starterDeck());
        deck.remove("carte_0");
        deck.remove("carte_1");
        deck.put("double", 2);
        profile.setDeck(deck);
        profile.addCoins(1234);
        profile.openChest("guillotine");

        PlayerProfile reloaded = profile(storage);
        assertEquals(deck, reloaded.getDeck());
        assertEquals(1234L + PlayerProfile.CHEST_COINS, reloaded.getCoins());
        assertEquals(1, reloaded.getOwnedCopies("guillotine"));
        assertTrue(storage.flushes > 0);
    }

    @Test
    void aChestAddsACopyAndTenThousandCoinsThenFiveThousandMoreOnceTheCardIsMaxedOut() {
        PlayerProfile profile = profile(new MemoryStorage());

        for (int copy = 1; copy <= PlayerProfile.MAX_COPIES; copy++) {
            PlayerProfile.ChestReward reward = profile.openChest("guillotine");
            assertTrue(reward.newCopy());
            assertEquals(copy, reward.copies());
            assertEquals(PlayerProfile.CHEST_COINS, reward.coins());
        }
        PlayerProfile.ChestReward extra = profile.openChest("guillotine");
        assertFalse(extra.newCopy());
        assertEquals(PlayerProfile.MAX_COPIES, profile.getOwnedCopies("guillotine"));
        assertEquals(15_000, extra.coins());
        assertEquals(3L * 10_000 + 15_000, profile.getCoins());
    }

    @Test
    void aSavedDeckThatIsNoLongerValidFallsBackToTheStarterDeck() {
        MemoryStorage storage = new MemoryStorage();
        storage.put(PlayerProfile.KEY_DECK, "carte_0:3,inconnue:1");
        storage.put(PlayerProfile.KEY_COINS, "abc");

        PlayerProfile profile = profile(storage);
        assertEquals(starterDeck(), profile.getDeck());
        assertEquals(0L, profile.getCoins(), "pièces illisibles : 0");
    }

    @Test
    void negativeCoinsAreIgnored() {
        PlayerProfile profile = profile(new MemoryStorage());
        profile.addCoins(-50);
        assertEquals(0L, profile.getCoins());
    }

    @Test
    void copiesAreWrittenAsIdsAndCounts() {
        Map<String, Integer> copies = new LinkedHashMap<>();
        copies.put("1_pique", 2);
        copies.put("joker", 1);
        assertEquals("1_pique:2,joker:1", PlayerProfile.encode(copies));
        assertEquals(copies, PlayerProfile.decode("1_pique:2,joker:1"));
        assertEquals(Map.of("joker", 1), PlayerProfile.decode("joker:1,abîmé,x:y"));
        assertTrue(PlayerProfile.decode("").isEmpty());
    }

    @Test
    void coinsAreShownWithSpacesBetweenThousands() {
        assertEquals("0", PlayerProfile.formatCoins(0));
        assertEquals("999", PlayerProfile.formatCoins(999));
        assertEquals("10 000", PlayerProfile.formatCoins(10_000));
        assertEquals("1 234 567", PlayerProfile.formatCoins(1_234_567));
    }

    @Test
    void combatCoinsAreTheShownGainsAfterTheLastFightOnly() {
        assertEquals(32_000, PlayerProfile.combatReward(true, true, 32_000), "les gains affichés, pas plus");
        assertEquals(0, PlayerProfile.combatReward(true, false, 32_000), "rien après le soldat");
        assertEquals(0, PlayerProfile.combatReward(false, true, 32_000), "rien en cas de défaite");
        assertEquals(0, PlayerProfile.combatReward(false, false, 32_000));
    }
}
