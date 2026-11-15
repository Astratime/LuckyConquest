package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySlotMachine;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.DungeonRun;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.progress.PlayerProfileTest.MemoryStorage;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static fr.astratime.lucky.progress.PlayerProfileTest.profile;
import static org.junit.jupiter.api.Assertions.*;

/** Les ennemis dorés : un soldat d'Exploration sur 20, PV x2, un rouleau de plus, et leur case au bestiaire. */
class GoldenEnemiesTest {

    @Test
    void aGoldenEnemyHasTwiceTheHpAndOneMoreReel() {
        Enemy enemy = new Enemy(EnemyKind.SOLDAT_PIQUE);
        long hp = enemy.getMaxHp();
        assertEquals(EnemySlotMachine.SYMBOL_COUNT, enemy.getReelCount());
        assertFalse(enemy.isGolden());

        enemy.gild();
        enemy.gild(); // une seule fois

        assertTrue(enemy.isGolden());
        assertEquals(hp * 2, enemy.getMaxHp());
        assertEquals(hp * 2, enemy.getHp());
        assertEquals(EnemySlotMachine.SYMBOL_COUNT + 1, enemy.getReelCount());
    }

    @Test
    void aboutOneSoldierInTwentyArrivesGoldenAndNeverTheKing() {
        Random random = new Random(42);
        int golden = 0, runs = 20_000;
        for (int i = 0; i < runs; i++) if (new DungeonRun(Dungeon.PIQUE).rollGolden(random)) golden++;
        assertEquals(runs / DungeonRun.GOLDEN_CHANCE, golden, runs / 100);

        DungeonRun king = new DungeonRun(Dungeon.PIQUE, 1, true);
        assertFalse(king.rollGolden(random));
        DungeonRun forced = new DungeonRun(Dungeon.PIQUE, 0, true); // choix du mode ADMIN
        for (int i = 0; i < 50; i++) assertTrue(forced.rollGolden(random));
        forced.win();
        assertFalse(forced.rollGolden(random));
    }

    @Test
    void everyExplorationSoldierHasAGoldenVersion() {
        List<EnemyKind> golden = Bestiary.goldenEnemies();
        assertEquals(new HashSet<>(golden).size(), golden.size());
        int soldiers = 0;
        for (Place place : Place.values()) soldiers += place.getDungeons().size();
        assertEquals(soldiers, golden.size());
        assertTrue(Bestiary.hasGoldenVersion(EnemyKind.SOLDAT_PIQUE));
        assertFalse(Bestiary.hasGoldenVersion(EnemyKind.ROI_PIQUE));
        assertTrue(Bestiary.enemies().containsAll(golden));
    }

    @Test
    void beatingAGoldenSoldierIsSavedInTheBestiary() {
        MemoryStorage storage = new MemoryStorage();
        PlayerProfile profile = profile(storage);
        profile.defeat(EnemyKind.SOLDAT_PIQUE);
        assertFalse(profile.hasDefeatedGolden(EnemyKind.SOLDAT_PIQUE));

        profile.defeatGolden(EnemyKind.SOLDAT_TREFLE);

        PlayerProfile reloaded = profile(storage);
        assertTrue(reloaded.hasDefeatedGolden(EnemyKind.SOLDAT_TREFLE));
        assertTrue(reloaded.hasDefeated(EnemyKind.SOLDAT_TREFLE));
        assertFalse(reloaded.hasDefeatedGolden(EnemyKind.SOLDAT_PIQUE));
        assertEquals(1, reloaded.getGoldenDefeatedCount());
    }

    @Test
    void adminGoldenFightsAreNotSaved() {
        MemoryStorage storage = new MemoryStorage();
        PlayerProfile profile = profile(storage);
        profile.setAdmin(true);
        assertTrue(profile.hasDefeatedGolden(EnemyKind.SOLDAT_COEUR));
        profile.defeatGolden(EnemyKind.SOLDAT_COEUR);
        profile.setAdmin(false);
        assertFalse(profile(storage).hasDefeatedGolden(EnemyKind.SOLDAT_COEUR));
    }
}
