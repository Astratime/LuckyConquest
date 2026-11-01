package fr.astratime.lucky.entities.run;

import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.DungeonRun;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.entities.tower.Chapter;
import fr.astratime.lucky.entities.tower.TowerRun;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Mode ADMIN : commencer une ascension ou une descente au combat choisi. */
class AdminFightChoiceTest {

    @Test
    void aChapterListsItsFiveFightsInOrder() {
        List<EnemyKind> fights = TowerRun.fights(Chapter.LE_JACKPOT);
        assertEquals(5, fights.size());
        assertEquals(EnemyKind.GARDIEN_LEVIER, fights.get(0));
        assertEquals(Chapter.LE_JACKPOT.getChallengers(), fights.subList(1, 4));
        assertEquals(EnemyKind.MACHINE_ORIGINELLE, fights.get(4));
        assertEquals(List.of(1, 2, 2, 2, 3), List.of(0, 1, 2, 3, 4).stream()
            .map(i -> TowerRun.stageNumber(Chapter.LE_JACKPOT, i)).toList());
    }

    @Test
    void aTowerRunCanStartAtAChallengerThenMeetsTheBoss() {
        TowerRun run = TowerRun.startingAt(Chapter.TABLES_SACREES, true, 2);
        assertEquals(Chapter.TABLES_SACREES.getChallengers().get(1), run.getEnemy());
        assertEquals(1, run.getStage());
        assertTrue(run.isHard());
        assertEquals(CombatRun.Next.BOSS, run.win());
        assertEquals(EnemyKind.REINE, run.getEnemy());
        assertEquals(CombatRun.Next.CLEARED, run.win());
    }

    @Test
    void aTowerRunStartedAtTheBossRestartsAtTheBoss() {
        TowerRun run = TowerRun.startingAt(Chapter.LE_JACKPOT, false, 4);
        assertTrue(run.isBossStage());
        assertEquals(EnemyKind.MACHINE_ORIGINELLE, run.getEnemy());
        run.restart();
        assertTrue(run.isBossStage());
        assertEquals(EnemyKind.MACHINE_ORIGINELLE, run.getEnemy());
    }

    @Test
    void aDungeonRunCanStartAtTheKing() {
        Dungeon dungeon = Place.DESERT.getDungeons().get(3);
        DungeonRun run = new DungeonRun(dungeon, 1);
        assertTrue(run.isBossStage());
        assertEquals(dungeon.getKing(), run.getEnemy());
        run.restart();
        assertEquals(dungeon.getKing(), run.getEnemy());
        assertEquals(CombatRun.Next.CLEARED, run.win());
    }

    @Test
    void aNormalDungeonRunStillStartsAtTheSoldier() {
        Dungeon dungeon = Place.PRAIRIE.getDungeons().get(0);
        DungeonRun run = new DungeonRun(dungeon);
        assertEquals(dungeon.getSoldier(), run.getEnemy());
        assertThrows(IllegalArgumentException.class, () -> new DungeonRun(dungeon, 2));
    }
}
