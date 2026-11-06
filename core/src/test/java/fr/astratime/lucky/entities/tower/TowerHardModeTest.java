package fr.astratime.lucky.entities.tower;

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
            assertEquals(bossHp[i], chapter.getBoss().getMaxHp(true), chapter.name());
            assertEquals(bossHp[i], new Enemy(chapter.getBoss(), true).getMaxHp(), chapter.name());
            assertEquals(power[i], chapter.getBoss().getPower(true), chapter.name());
            assertEquals(power[i], chapter.getFirstEnemy().getPower(true), chapter.name());
        }
        assertEquals(2 * EnemyKind.CHAPTER_6_POWER, EnemyKind.CHEF.getPower(true), "chapitre 2 : deux fois le chapitre 6 normal");
    }

    @Test
    void theOtherEnemiesOfAChapterKeepTheirHpRatioToTheBoss() {
        for (Chapter chapter : Chapter.values()) {
            EnemyKind boss = chapter.getBoss();
            double ratio = (double) boss.getMaxHp(true) / boss.getMaxHp(false);
            assertEquals(Math.round(chapter.getFirstEnemy().getMaxHp(false) * ratio), chapter.getFirstEnemy().getMaxHp(true));
            for (EnemyKind challenger : chapter.getChallengers()) {
                assertEquals(Math.round(challenger.getMaxHp(false) * ratio), challenger.getMaxHp(true), challenger.name());
            }
        }
        assertEquals(5_000_000L, EnemyKind.CROUPIER.getMaxHp(true), "chapitre 1 : le croupier a le quart du boss");
        assertEquals(4_500_000_000L, EnemyKind.OMBRE.getMaxHp(true));
    }

    @Test
    void hardModeHitsWithTheLateTowerDamageEverywhere() {
        // Chapitre 2 difficile : l'Épée frappe deux fois plus fort qu'au chapitre 6 normal.
        int normalSix = EnemyKind.MACHINE_ORIGINELLE.hurt(EnemyKind.CHEF.swordDamage(), false);
        assertEquals(2 * normalSix, EnemyKind.CHEF.hurt(EnemyKind.CHEF.swordDamage(), true), 2);
        assertTrue(EnemyKind.TEMPS_MORT.interestPercent(true) <= 100, "jamais plus que tous les gains");
    }

    @Test
    void normalModeExplorationAndTrainingDoNotChange() {
        assertEquals(EnemyKind.MACHINE_ORIGINELLE.getMaxHp(false), 10_000_000L);
        assertEquals(EnemyKind.ROI_PIQUE.getMaxHp(false), EnemyKind.ROI_PIQUE.getMaxHp(true), "l'Exploration ne change pas");
        assertEquals(EnemyKind.ENTRAINEMENT.getPower(false), EnemyKind.ENTRAINEMENT.getPower(true), "l'Entraînement non plus");
        assertEquals(600, EnemyKind.MACHINE_ORIGINELLE.getPower(false));

        // Une Machine à 10 Md de PV encaisse et se soigne sans déborder.
        Enemy machine = new Enemy(EnemyKind.MACHINE_ORIGINELLE, true);
        assertEquals(3_000_000_000L, machine.takeDamage(3_000_000_000L));
        assertEquals(7_000_000_000L, machine.getHp());
        assertTrue(machine.heal(1_000_000_000L) > 0);
        assertFalse(new Enemy(EnemyKind.MACHINE_ORIGINELLE).isHard());
    }
}
