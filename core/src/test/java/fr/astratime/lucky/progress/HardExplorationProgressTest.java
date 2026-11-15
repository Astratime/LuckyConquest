package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.Place;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HardExplorationProgressTest {

    private static void clearNormal(PlayerProfile profile, Place place) {
        for (Dungeon dungeon : place.getDungeons()) profile.clearDungeon(dungeon.name());
    }

    @Test
    void hardExplorationOpensWithTheDesertAndHasItsOwnProgression() {
        PlayerProfileTest.MemoryStorage storage = new PlayerProfileTest.MemoryStorage();
        PlayerProfile profile = PlayerProfileTest.profile(storage);
        assertFalse(profile.isExplorationHardOpen());
        assertFalse(profile.isOpen(Place.PRAIRIE, true));
        for (Place place : Place.values()) clearNormal(profile, place);
        assertTrue(profile.isExplorationHardOpen(), "le Désert vidé ouvre l'Exploration difficile");
        assertTrue(profile.isOpen(Place.PRAIRIE, true));
        assertFalse(profile.isOpen(Place.FOIRE, true), "chaque lieu difficile attend le précédent en difficile");
        assertFalse(profile.isCleared(Dungeon.PIQUE, true), "le mode normal ne vide rien en difficile");

        for (Dungeon dungeon : Place.PRAIRIE.getDungeons()) assertTrue(profile.clearHardDungeon(dungeon.name()));
        assertFalse(profile.clearHardDungeon(Dungeon.PIQUE.name()), "déjà vidé");
        assertTrue(profile.isPlaceCleared(Place.PRAIRIE, true));
        assertTrue(profile.isOpen(Place.FOIRE, true));
        assertFalse(profile.isOpen(Place.PORT, true));

        PlayerProfile reloaded = PlayerProfileTest.profile(storage);
        assertTrue(reloaded.isCleared(Dungeon.PIQUE, true), "la progression difficile est enregistrée");
        assertTrue(reloaded.isOpen(Place.FOIRE, true));
    }

    @Test
    void theHardChestPaysTheDesertChestAtTheHardSpinCostAndHasItsOwnPot() {
        PlayerProfile profile = PlayerProfileTest.profile(new PlayerProfileTest.MemoryStorage());
        String card = Dungeon.PIQUE.getLoot().get(0).cardId();
        PlayerProfile.ChestReward reward = profile.openChest(card, Place.PRAIRIE, true);
        assertEquals(Place.PRAIRIE.getChestCoins(true), reward.coins());
        assertEquals(700_000_000L, reward.coins(), "5 000 tirages à 140 000");
        assertNotSame(profile.getPot(Place.PRAIRIE), profile.getPot(Place.PRAIRIE, true));
        profile.getPot(Place.PRAIRIE, true).add(1_000);
        assertEquals(0, profile.getPot(Place.PRAIRIE).getAmount(), 0);
    }
}
