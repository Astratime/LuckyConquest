package fr.astratime.lucky.entities.tower;

import fr.astratime.lucky.entities.enemy.Difficulty;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TowerHardModeTest {

    @Test
    void aHardRunIsLabelledAndRemembered() {
        TowerRun run = new TowerRun(Chapter.GENESE, true);
        assertTrue(run.isHard());
        assertEquals("Chapitre 1 difficile", run.getLabel());
        assertFalse(new TowerRun(Chapter.GENESE).isHard());
        assertEquals("Chapitre 1", new TowerRun(Chapter.GENESE).getLabel());
    }

    @Test
    void hardModeGivesEachChapterBossAstrasHpAndDoublesThePowerEachChapter() {
        long[] bossHp = {20_000_000L, 50_000_000L, 200_000_000L, 800_000_000L, 2_000_000_000L, 10_000_000_000L};
        int[] power = {600, 1_200, 2_400, 4_800, 9_600, 19_200};
        for (Chapter chapter : Chapter.values()) {
            int i = chapter.getNumber() - 1;
            assertEquals(bossHp[i], chapter.getBoss().getMaxHp(Difficulty.HARD), chapter.name());
            assertEquals(bossHp[i], new Enemy(chapter.getBoss(), true).getMaxHp(), chapter.name());
            assertEquals(power[i], chapter.getBoss().getPower(Difficulty.HARD), chapter.name());
            assertEquals(power[i], chapter.getFirstEnemy().getPower(Difficulty.HARD), chapter.name());
        }
        assertEquals(2 * EnemyKind.CHAPTER_6_POWER, EnemyKind.CHEF.getPower(Difficulty.HARD), "chapitre 2 : deux fois le chapitre 6 normal");
    }

    @Test
    void theOtherEnemiesOfAChapterKeepTheirHpRatioToTheBoss() {
        for (Chapter chapter : Chapter.values()) {
            EnemyKind boss = chapter.getBoss();
            double ratio = (double) boss.getMaxHp(Difficulty.HARD) / boss.getMaxHp(Difficulty.NORMAL);
            assertEquals(Math.round(chapter.getFirstEnemy().getMaxHp(Difficulty.NORMAL) * ratio), chapter.getFirstEnemy().getMaxHp(Difficulty.HARD));
            for (EnemyKind challenger : chapter.getChallengers()) {
                assertEquals(Math.round(challenger.getMaxHp(Difficulty.NORMAL) * ratio), challenger.getMaxHp(Difficulty.HARD), challenger.name());
            }
        }
        assertEquals(5_000_000L, EnemyKind.CROUPIER.getMaxHp(Difficulty.HARD), "chapitre 1 : le croupier a le quart du boss");
        assertEquals(4_500_000_000L, EnemyKind.OMBRE.getMaxHp(Difficulty.HARD));
    }

    @Test
    void hardModeHitsWithTheLateTowerDamageEverywhere() {
        // Chapitre 2 difficile : l'Épée frappe deux fois plus fort qu'au chapitre 6 normal.
        int normalSix = EnemyKind.MACHINE_ORIGINELLE.hurt(EnemyKind.CHEF.swordDamage(), Difficulty.NORMAL);
        assertEquals(2 * normalSix, EnemyKind.CHEF.hurt(EnemyKind.CHEF.swordDamage(), Difficulty.HARD), 2);
        assertTrue(EnemyKind.TEMPS_MORT.interestPercent(Difficulty.HARD) <= 100, "jamais plus que tous les gains");
    }

    @Test
    void normalModeExplorationAndTrainingDoNotChange() {
        assertEquals(EnemyKind.MACHINE_ORIGINELLE.getMaxHp(Difficulty.NORMAL), 10_000_000L);
        assertEquals(EnemyKind.ROI_PIQUE.getMaxHp(Difficulty.NORMAL), EnemyKind.ROI_PIQUE.getMaxHp(Difficulty.HARD), "l'Exploration ne change pas");
        assertEquals(EnemyKind.ENTRAINEMENT.getPower(Difficulty.NORMAL), EnemyKind.ENTRAINEMENT.getPower(Difficulty.HARD), "l'Entraînement non plus");
        assertEquals(600, EnemyKind.MACHINE_ORIGINELLE.getPower(Difficulty.NORMAL));

        // Une Machine à 10 Md de PV encaisse et se soigne sans déborder.
        Enemy machine = new Enemy(EnemyKind.MACHINE_ORIGINELLE, true);
        assertEquals(3_000_000_000L, machine.takeDamage(3_000_000_000L));
        assertEquals(7_000_000_000L, machine.getHp());
        assertTrue(machine.heal(1_000_000_000L) > 0);
        assertFalse(new Enemy(EnemyKind.MACHINE_ORIGINELLE).isHard());
    }
}
