package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.exploration.Place;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Pièces dans un lieu en dessous de celui du rang joué (Astra, 2026-11-15) : 25 %, puis 5 %. */
class LowerPlaceCoinsTest {

    @Test
    void eachRankHasItsPlace() {
        assertEquals(Place.PRAIRIE, PlayerProfile.placeOf(null));
        assertEquals(Place.FOIRE, PlayerProfile.placeOf(Rank.AVARE));
        assertEquals(Place.FOIRE, PlayerProfile.placeOf(Rank.PARIEUR));
        assertEquals(Place.PORT, PlayerProfile.placeOf(Rank.HABITUE));
        assertEquals(Place.PALACE, PlayerProfile.placeOf(Rank.REQUIN));
        assertEquals(Place.MINES, PlayerProfile.placeOf(Rank.GROS_BONNET));
        assertEquals(Place.CASINO, PlayerProfile.placeOf(Rank.MAGNAT));
        assertEquals(Place.TEMPLE, PlayerProfile.placeOf(Rank.ROI_DU_CASINO));
        assertEquals(Place.DESERT, PlayerProfile.placeOf(Rank.LEGENDE));
    }

    @Test
    void lowerPlacesPayLess() {
        assertEquals(100, PlayerProfile.coinsPercent(Rank.REQUIN, Place.PALACE, false), "son lieu");
        assertEquals(100, PlayerProfile.coinsPercent(Rank.REQUIN, Place.CASINO, false), "plus haut");
        assertEquals(25, PlayerProfile.coinsPercent(Rank.REQUIN, Place.PORT, false), "un lieu en dessous");
        assertEquals(5, PlayerProfile.coinsPercent(Rank.REQUIN, Place.FOIRE, false), "deux lieux en dessous");
        assertEquals(5, PlayerProfile.coinsPercent(Rank.LEGENDE, Place.PRAIRIE, false));
        assertEquals(100, PlayerProfile.coinsPercent(null, Place.PRAIRIE, false));
        assertEquals(100, PlayerProfile.coinsPercent(Rank.LEGENDE, Place.PRAIRIE, true), "mode difficile : tout");
    }

    @Test
    void theShareIsRounded() {
        assertEquals(250L, PlayerProfile.share(1_000L, 25));
        assertEquals(1_000L, PlayerProfile.share(1_000L, 100));
        assertEquals(50L, PlayerProfile.share(1_000L, 5));
    }

    @Test
    void theChestFollowsThePlayedRank() {
        PlayerProfileTest.MemoryStorage storage = new PlayerProfileTest.MemoryStorage();
        PlayerProfile profile = PlayerProfileTest.profile(storage);
        profile.addCoins(Rank.AVARE.getPrice());
        profile.buyNextRank(); // Avare : son lieu est la Foire
        long before = profile.getCoins();
        PlayerProfile.ChestReward reward = profile.openChest("2_pique", Place.PRAIRIE);
        assertEquals(Place.PRAIRIE.getChestCoins() / 4, reward.coins(), "un lieu en dessous : 25 %");
        assertEquals(before + reward.coins(), profile.getCoins());
    }
}
