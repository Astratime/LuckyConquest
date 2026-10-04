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
        assertEquals(new HashSet<>(Chapter.GENESE.getChallengers()), new HashSet<>(run.getChoices()), "3 cartes, 3 ennemis différents");
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
    void theThreeChaptersAreOpen() {
        for (Chapter chapter : Chapter.values()) assertTrue(chapter.isOpen(), chapter.name());
        assertEquals("La genèse", Chapter.GENESE.getTitle());
        assertEquals("Les Tables Sacrées", Chapter.TABLES_SACREES.getTitle());
        assertEquals("Le Dernier Tirage", Chapter.DERNIER_TIRAGE.getTitle());
    }

    @Test
    void eachChapterHasItsOwnLineUp() {
        TowerRun run = new TowerRun(Chapter.TABLES_SACREES, new Random(2));
        assertEquals(EnemyKind.CHEF, run.getEnemy());
        run.win();
        assertEquals(new HashSet<>(Chapter.TABLES_SACREES.getChallengers()), new HashSet<>(run.getChoices()));
        run.choose(0);
        run.win();
        assertEquals(EnemyKind.REINE, run.getEnemy());
        run.restart();
        assertEquals(EnemyKind.CHEF, run.getEnemy());

        run = new TowerRun(Chapter.DERNIER_TIRAGE, new Random(2));
        assertEquals(EnemyKind.GARDIENNE, run.getEnemy());
        run.win();
        run.choose(1);
        run.win();
        assertEquals(EnemyKind.ECLAT, run.getEnemy());
    }
}
