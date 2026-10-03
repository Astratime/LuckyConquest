package fr.astratime.lucky.entities.tower;

import fr.astratime.lucky.entities.enemy.EnemyKind;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class TowerRunTest {

    @Test
    void aChapterChainsTheCroupierAChosenChallengerAndTheBoss() {
        TowerRun run = new TowerRun(Chapter.GENESE, new Random(4));
        assertEquals(EnemyKind.CROUPIER, run.getEnemy());
        assertEquals(0, run.getStage());

        assertEquals(TowerRun.Next.CHOOSE_ENEMY, run.win());
        assertEquals(new HashSet<>(EnemyKind.CHALLENGERS), new HashSet<>(run.getChoices()), "3 cartes, 3 ennemis différents");
        EnemyKind chosen = run.getChoices().get(2);
        assertEquals(chosen, run.choose(2));
        assertEquals(chosen, run.getEnemy());
        assertTrue(run.getChoices().isEmpty());

        assertEquals(TowerRun.Next.BOSS, run.win());
        assertTrue(run.isBossStage());
        assertEquals(EnemyKind.COMETE, run.getEnemy());

        assertEquals(TowerRun.Next.CHAPTER_CLEARED, run.win());
    }

    @Test
    void aDefeatRestartsTheChapterAtTheFirstFight() {
        TowerRun run = new TowerRun(Chapter.GENESE, new Random(1));
        run.win();
        run.choose(0);
        run.restart();
        assertEquals(0, run.getStage());
        assertEquals(EnemyKind.CROUPIER, run.getEnemy());
    }

    @Test
    void theCardsAreShuffled() {
        var orders = new HashSet<Object>();
        for (int seed = 0; seed < 20; seed++) {
            TowerRun run = new TowerRun(Chapter.GENESE, new Random(seed));
            run.win();
            orders.add(run.getChoices());
        }
        assertTrue(orders.size() > 1);
    }

    @Test
    void onlyTheFirstChapterIsOpen() {
        assertTrue(Chapter.GENESE.isOpen());
        assertEquals("La genèse", Chapter.GENESE.getTitle());
        assertFalse(Chapter.CHAPITRE_2.isOpen());
        assertFalse(Chapter.CHAPITRE_3.isOpen());
    }
}
