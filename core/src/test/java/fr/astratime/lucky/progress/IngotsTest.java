package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.Symbol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Les Lingots (Astra, 2026-11-26) : 1 Lingot = 1 milliard de pièces, les prix de 10 milliards ou plus en Lingots. */
class IngotsTest {

    private static final long BILLION = Money.INGOT_VALUE;

    @Test
    void aPriceOfTenBillionsOrMoreIsInIngots() {
        assertEquals(1_000_000_000L, BILLION);
        assertFalse(Money.inIngots(9_999_999_999L));
        assertTrue(Money.inIngots(10 * BILLION));
        assertEquals(38, Money.ingotsOf(38 * BILLION));
        assertEquals(11, Money.ingotsOf(10 * BILLION + 1), "arrondi au Lingot supérieur");
    }

    @Test
    void everyIngotPriceOfTheShopIsAWholeNumberOfBillions() {
        // le seuil à 10 milliards ne change aucun prix : tous ceux qui passent en Lingots sont ronds
        for (Rank rank : Rank.values()) {
            for (int level = 0; level <= PlayerProfile.MAX_PRESTIGE; level++) {
                long price = rank.getPrice(level);
                if (Money.inIngots(price)) assertEquals(0, price % BILLION, rank + " P" + level);
            }
        }
        ReelShop.getPrices().forEach((symbol, price) -> {
            if (Money.inIngots(price)) assertEquals(0, price % BILLION, symbol.name());
        });
        for (Cosmetic cosmetic : Cosmetic.values()) {
            if (cosmetic.getUnlock() == Cosmetic.Unlock.COINS && Money.inIngots(cosmetic.getGoal())) {
                assertEquals(0, cosmetic.getGoal() % BILLION, cosmetic.name());
            }
        }
        for (Achievement achievement : Achievement.values()) {
            if (Money.inIngots(achievement.getReward())) assertEquals(0, achievement.getReward() % BILLION, achievement.name());
        }
    }

    @Test
    void coinsAndIngotsConvertBothWaysWithoutFeesAndAreSaved() {
        PlayerProfileTest.MemoryStorage storage = new PlayerProfileTest.MemoryStorage();
        PlayerProfile profile = PlayerProfileTest.profile(storage);
        profile.addCoins(3 * BILLION + 123);
        assertEquals(3, profile.convertibleIngots());
        assertFalse(profile.convertToIngots(4));
        assertTrue(profile.convertToIngots(3));
        assertEquals(123, profile.getCoins(), "le reste sous un milliard reste en pièces");
        assertEquals(3, profile.getIngots());
        assertTrue(profile.convertToCoins(1));
        assertEquals(BILLION + 123, profile.getCoins());
        assertEquals(2, profile.getIngots());
        assertFalse(profile.convertToCoins(3));

        PlayerProfile reloaded = PlayerProfileTest.profile(storage);
        assertEquals(2, reloaded.getIngots(), "les Lingots sont enregistrés");
        assertEquals(BILLION + 123, reloaded.getCoins());
    }

    @Test
    void anOldSaveHasNoIngotsAndKeepsItsCoins() {
        PlayerProfileTest.MemoryStorage storage = new PlayerProfileTest.MemoryStorage();
        storage.put(PlayerProfile.KEY_COINS, String.valueOf(50 * BILLION));
        PlayerProfile profile = PlayerProfileTest.profile(storage);
        assertEquals(0, profile.getIngots());
        assertEquals(50 * BILLION, profile.getCoins());
    }

    @Test
    void anIngotPurchaseExchangesTheMissingCoinsByItself() {
        PlayerProfile profile = PlayerProfileTest.profile(new PlayerProfileTest.MemoryStorage());
        profile.addCoins(12 * BILLION + 5);
        assertTrue(profile.convertToIngots(2));           // 2 Lingots, 10 Md + 5 pièces
        assertTrue(profile.canAfford(11 * BILLION));
        assertTrue(profile.buyReel(Symbol.SWORD, 11 * BILLION));
        assertEquals(0, profile.getIngots(), "11 Lingots : 2 gardés + 9 convertis");
        assertEquals(BILLION + 5, profile.getCoins());
    }

    @Test
    void aCoinPurchaseExchangesTheMissingIngotsByItself() {
        PlayerProfile profile = PlayerProfileTest.profile(new PlayerProfileTest.MemoryStorage());
        profile.addCoins(2 * BILLION);
        assertTrue(profile.convertToIngots(2));
        assertEquals(0, profile.getCoins());
        assertTrue(profile.buyCard("as_de_pique", 1_300_000_000L));
        assertEquals(0, profile.getIngots(), "1,3 Md : 2 Lingots changés");
        assertEquals(700_000_000L, profile.getCoins());
        assertFalse(profile.canAfford(2 * BILLION));
        assertEquals(1_300_000_000L, profile.missing(2 * BILLION));
    }

    @Test
    void prestigeEmptiesTheIngotsToo() {
        PlayerProfile profile = PlayerProfileTest.profile(new PlayerProfileTest.MemoryStorage());
        for (Rank rank : Rank.values()) {
            profile.addCoins(rank.getPrice(profile.getPrestige()));
            assertTrue(profile.buyNextRank(), rank.name());
        }
        for (var dungeon : PlayerProfile.prestigeSeal(1).getDungeons()) profile.clearHardDungeon(dungeon.name());
        profile.addCoins(5 * BILLION);
        assertTrue(profile.convertToIngots(5));
        assertTrue(profile.ascend());
        assertEquals(0, profile.getIngots());
        assertEquals(0, profile.getCoins());
    }

    @Test
    void pricesAreWrittenInTheirCurrency() {
        assertEquals("38 Lingots", Money.formatPrice(38 * BILLION));
        assertEquals("1 300 000 000 pièces", Money.formatPrice(1_300_000_000L).replace(' ', ' ').replace(' ', ' '));
        assertEquals("38 Lingots", Money.priceTag(38 * BILLION));
    }
}
