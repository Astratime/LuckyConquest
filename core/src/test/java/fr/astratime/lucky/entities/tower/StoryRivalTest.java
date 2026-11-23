package fr.astratime.lucky.entities.tower;

import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.exploration.Place;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Le Joueur Masqué dans l'Histoire (Astra, 2026-11-23) : caché sous une carte des challengers de la Maison. */
class StoryRivalTest {

    @Test
    void heHidesUnderOneChallengerCardOfTheHouse() {
        TowerRun run = new TowerRun(Chapter.RIVAL_CHAPTER, false).withRival(true);
        run.win();
        assertEquals(1, run.getChoices().stream().filter(k -> k == EnemyKind.RIVAL_HISTOIRE).count());
        assertEquals(3, run.getChoices().size());
    }

    @Test
    void notOnceBeatenNorInHardModeNorElsewhere() {
        for (TowerRun run : new TowerRun[] {new TowerRun(Chapter.RIVAL_CHAPTER, false).withRival(false),
                new TowerRun(Chapter.RIVAL_CHAPTER, true).withRival(true), new TowerRun(Chapter.GENESE, false).withRival(true)}) {
            run.win();
            assertFalse(run.getChoices().contains(EnemyKind.RIVAL_HISTOIRE));
        }
    }

    @Test
    void heFightsWithTheStatsOfTheHouseNotOfAPlace() {
        assertEquals(Chapter.RIVAL_CHAPTER, Chapter.of(EnemyKind.RIVAL_HISTOIRE));
        assertNull(Place.of(EnemyKind.RIVAL_HISTOIRE));
        assertTrue(EnemyKind.RIVAL_HISTOIRE.isTower());
        assertFalse(EnemyKind.RIVAL_HISTOIRE.isBoss());
    }
}
