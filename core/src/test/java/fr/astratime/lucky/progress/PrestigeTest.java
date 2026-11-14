package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.RankBonus;
import fr.astratime.lucky.entities.Symbol;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Le Prestige : rendre tous ses rangs pour un bonus permanent, sans perdre le reste. */
class PrestigeTest {

    private static PlayerProfile legend(PlayerProfileTest.MemoryStorage storage) {
        PlayerProfile profile = PlayerProfileTest.profile(storage);
        for (Rank rank : Rank.values()) {
            profile.addCoins(rank.getPrice(profile.getPrestige()));
            assertTrue(profile.buyNextRank());
        }
        return profile;
    }

    @Test
    void prestigeNeedsEveryRank() {
        PlayerProfile profile = PlayerProfileTest.profile(new PlayerProfileTest.MemoryStorage());
        assertFalse(profile.canPrestige());
        assertFalse(profile.ascend());
        assertEquals(0, profile.getPrestige());
    }

    @Test
    void prestigeGivesBackTheRanksKeepsTheCoinsAndIsSaved() {
        PlayerProfileTest.MemoryStorage storage = new PlayerProfileTest.MemoryStorage();
        PlayerProfile profile = legend(storage);
        profile.addCoins(777);
        assertTrue(profile.canPrestige());

        assertTrue(profile.ascend());

        assertEquals(1, profile.getPrestige());
        assertNull(profile.getRank());
        assertNull(profile.getActiveRank());
        assertEquals(Rank.AVARE, profile.getNextRank());
        assertEquals(777L, profile.getCoins(), "Astra : les pièces sont gardées");
        assertEquals(1, profile.getRankBonus().prestige(), "le Prestige compte même sans rang");
        assertEquals(1, PlayerProfileTest.profile(storage).getPrestige(), "le Prestige est enregistré");
    }

    @Test
    void prestigeStopsAtTen() {
        PlayerProfileTest.MemoryStorage storage = new PlayerProfileTest.MemoryStorage();
        PlayerProfile profile = PlayerProfileTest.profile(storage);
        for (int level = 0; level < PlayerProfile.MAX_PRESTIGE; level++) {
            for (Rank rank : Rank.values()) {
                profile.addCoins(rank.getPrice(profile.getPrestige()));
                assertTrue(profile.buyNextRank());
            }
            assertTrue(profile.ascend());
        }
        for (Rank rank : Rank.values()) {
            profile.addCoins(rank.getPrice(profile.getPrestige()));
            assertTrue(profile.buyNextRank());
        }
        assertEquals(PlayerProfile.MAX_PRESTIGE, profile.getPrestige());
        assertFalse(profile.canPrestige());
        assertEquals("X", PlayerProfile.prestigeNumeral(profile.getPrestige()));
    }

    @Test
    void eachLevelAddsTenPercentToHpAndReelValuesRankIncluded() {
        RankBonus bonus = Rank.AVARE.getBonus().withPrestige(3);
        assertEquals(30, bonus.prestigeBonus(100));
        assertEquals(130, bonus.withPrestigeBonus(100));
        Player player = new Player("Joueur", Player.BASE_HP, List.of(), bonus, Symbol.classicReels());
        assertEquals(Math.round((Player.BASE_HP + Rank.AVARE.getBonus().hp()) * 1.3), player.getMaxHp());
        assertEquals(player.getMaxHp(), player.nextCombat().getMaxHp(), "les PV max ne grossissent pas d'un combat à l'autre");
        assertEquals(0, RankBonus.NONE.prestigeBonus(1000));
    }
}
