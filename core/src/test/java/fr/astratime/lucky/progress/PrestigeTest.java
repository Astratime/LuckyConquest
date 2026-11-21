package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.RankBonus;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.Place;

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

    /** Vide en difficile le lieu que demande le prochain Prestige (sceau). */
    private static void breakSeal(PlayerProfile profile) {
        for (Dungeon dungeon : PlayerProfile.prestigeSeal(profile.getPrestige() + 1).getDungeons()) {
            profile.clearHardDungeon(dungeon.name());
        }
    }

    @Test
    void prestigeNeedsEveryRank() {
        PlayerProfile profile = PlayerProfileTest.profile(new PlayerProfileTest.MemoryStorage());
        assertFalse(profile.canPrestige());
        assertFalse(profile.ascend());
        assertEquals(0, profile.getPrestige());
    }

    @Test
    void prestigeGivesBackTheRanksEmptiesTheCoinsAndIsSaved() {
        PlayerProfileTest.MemoryStorage storage = new PlayerProfileTest.MemoryStorage();
        PlayerProfile profile = legend(storage);
        profile.addCoins(777);
        assertFalse(profile.canPrestige(), "sceau : la prairie difficile d'abord");
        breakSeal(profile);
        assertTrue(profile.canPrestige());

        assertTrue(profile.ascend());

        assertEquals(1, profile.getPrestige());
        assertNull(profile.getRank());
        assertNull(profile.getActiveRank());
        assertEquals(Rank.AVARE, profile.getNextRank());
        assertEquals(0L, profile.getCoins(), "Astra (2026-11-15) : les pièces repartent à 0");
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
            breakSeal(profile);
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
    void eachPrestigeNeedsTheHardPlaceOfItsTier() {
        // Un Prestige = un palier de lieu (10 lieux, Astra 2026-11-20) ; un palier sans lieu prend le lieu suivant.
        assertEquals(Place.PRAIRIE, PlayerProfile.prestigeSeal(1));
        assertEquals(Place.FOIRE, PlayerProfile.prestigeSeal(2));
        assertEquals(Place.RAPIDE, PlayerProfile.prestigeSeal(3));
        assertEquals(Place.PORT, PlayerProfile.prestigeSeal(4));
        assertEquals(Place.MINES, PlayerProfile.prestigeSeal(6));
        assertEquals(Place.DESERT, PlayerProfile.prestigeSeal(10));
        assertNull(PlayerProfile.prestigeSeal(11));

        PlayerProfile profile = legend(new PlayerProfileTest.MemoryStorage());
        assertEquals(Place.PRAIRIE, profile.missingPrestigeSeal());
        for (Dungeon dungeon : Place.PRAIRIE.getDungeons().subList(0, 3)) profile.clearHardDungeon(dungeon.name());
        assertFalse(profile.canPrestige(), "les 4 donjons");
        profile.clearHardDungeon(Place.PRAIRIE.getDungeons().get(3).name());
        assertNull(profile.missingPrestigeSeal());
        assertTrue(profile.ascend());
        assertEquals(Place.FOIRE, profile.missingPrestigeSeal(), "le Prestige II demande la Foire");
    }

    @Test
    void afterAPrestigeTheBonusesNeverFallUnderHalfTheLegend() {
        RankBonus legend = Rank.LEGENDE.getBonus();
        RankBonus none = Rank.bonusAt(null, 1);
        assertEquals(legend.hp() / 2, none.hp());
        assertEquals(legend.attack() / 2, none.attack());
        assertEquals(legend.defense() / 2, none.defense());
        assertEquals(0, none.gains(), "les gains ne suivent pas le plancher");
        assertEquals(Rank.MAGNAT.getBonus().hp() < legend.hp() / 2 ? legend.hp() / 2 : Rank.MAGNAT.getBonus().hp(),
            Rank.bonusAt(Rank.MAGNAT, 2).hp());
        assertEquals(Rank.ROI_DU_CASINO.getBonus().hp(), Rank.bonusAt(Rank.ROI_DU_CASINO, 2).hp(), "un rang plus fort remplace le plancher");
        assertEquals(RankBonus.NONE.hp(), Rank.bonusAt(null, 0).hp(), "aucun plancher sans Prestige");
    }

    @Test
    void eachLevelAddsTenPercentToAttackAndGainsAndMultipliesHpAndDefense() {
        RankBonus bonus = Rank.AVARE.getBonus().withPrestige(3);
        assertEquals(30, bonus.prestigeBonus(100));
        assertEquals(130, bonus.withPrestigeBonus(100));
        Player player = new Player("Joueur", Player.BASE_HP, List.of(), bonus, Symbol.classicReels());
        assertEquals(Math.round((Player.BASE_HP + Rank.AVARE.getBonus().hp()) * Math.pow(1.4, 3)), player.getMaxHp());
        assertEquals(174, bonus.guardBonus(100), "x1,4 par niveau : 100 -> 274");
        assertEquals(Math.round(7_100 * Math.pow(1.4, 10)), new Player("Joueur", Player.BASE_HP, List.of(),
            Rank.LEGENDE.getBonus().withPrestige(10), Symbol.classicReels()).getMaxHp());
        assertEquals(player.getMaxHp(), player.nextCombat().getMaxHp(), "les PV max ne grossissent pas d'un combat à l'autre");
        assertEquals(0, RankBonus.NONE.prestigeBonus(1000));
    }
}
