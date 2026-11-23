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
    void hardModeGivesEachChapterBossItsHpAndAboutX147PowerEachChapter() {
        // PV des boss : modèle de Puissance (2026-11-18). Force : x1,47 par chapitre sur 10 chapitres (plan validé
        // le 2026-11-20), de la Comète x6 à la Machine x192, chapitre attaché à son nom et non à son numéro.
        assertEquals(1_200_000L, EnemyKind.COMETE.getMaxHp(Difficulty.HARD));
        assertEquals(2_500_000L, EnemyKind.REINE.getMaxHp(Difficulty.HARD));
        assertEquals(8_000_000L, EnemyKind.ECLAT.getMaxHp(Difficulty.HARD));
        assertEquals(40_000_000L, EnemyKind.PRETENDANT.getMaxHp(Difficulty.HARD));
        assertEquals(100_000_000L, EnemyKind.MAISON.getMaxHp(Difficulty.HARD));
        assertEquals(150_000_000L, EnemyKind.MACHINE_ORIGINELLE.getMaxHp(Difficulty.HARD)); // Astra, 2026-11-23
        assertEquals(600, EnemyKind.COMETE.getPower(Difficulty.HARD));
        assertEquals(880, EnemyKind.CHEF.getPower(Difficulty.HARD));
        assertEquals(1_900, EnemyKind.ECLAT.getPower(Difficulty.HARD));
        assertEquals(4_080, EnemyKind.PRETENDANT.getPower(Difficulty.HARD));
        assertEquals(8_800, EnemyKind.MAISON.getPower(Difficulty.HARD));
        assertEquals(13_000, EnemyKind.MACHINE_ORIGINELLE.getPower(Difficulty.HARD));
        Chapter previous = null;
        for (Chapter chapter : Chapter.values()) {
            Chapter.Stats stats = chapter.getStats();
            assertEquals(stats.hardBossHp(), chapter.getBoss().getMaxHp(Difficulty.HARD), chapter.name());
            assertEquals(stats.hardBossHp(), new Enemy(chapter.getBoss(), true).getMaxHp(), chapter.name());
            assertEquals(stats.hardPower(), chapter.getBoss().getPower(Difficulty.HARD), chapter.name());
            assertEquals(stats.hardPower(), chapter.getFirstEnemy().getPower(Difficulty.HARD), chapter.name());
            if (previous != null) {
                assertTrue(stats.hardPower() > previous.getStats().hardPower(), chapter.name());
                // la Machine (3 phases, vole tes rouleaux) a moins de PV que l'Architecte depuis le 2026-11-23
                if (chapter != Chapter.LE_JACKPOT) assertTrue(stats.hardBossHp() > previous.getStats().hardBossHp(), chapter.name());
            }
            previous = chapter;
        }
        assertEquals("8,8", Chapter.TABLES_SACREES.hardPowerText());
        assertEquals("130", Chapter.LE_JACKPOT.hardPowerText());
    }

    @Test
    void hardStoryEnemiesAndTheCroupierHealAtMostOnePercentAndTheCometHitsTwoPercent() {
        assertEquals(1, EnemyKind.MACHINE_ORIGINELLE.maxHealPercent(Difficulty.HARD));
        assertEquals(1, EnemyKind.GRAND_CROUPIER.maxHealPercent(Difficulty.NORMAL));
        assertEquals(5, EnemyKind.MACHINE_ORIGINELLE.maxHealPercent(Difficulty.NORMAL));
        assertEquals(5, EnemyKind.MACHINE_ORIGINELLE.maxHealPercent(Difficulty.endless(3)));
        assertEquals(5, EnemyKind.ROI_PIQUE.maxHealPercent(Difficulty.HARD)); // Exploration difficile : inchangée
        Enemy machine = new Enemy(EnemyKind.MACHINE_ORIGINELLE, true);
        machine.takeDamage(machine.getMaxHp() / 2);
        assertEquals(machine.getMaxHp() / 100, machine.heal(machine.getMaxHp()));
        assertEquals(2, EnemyKind.COMETE.goldenCometPercent(Difficulty.HARD));
        assertEquals(5, EnemyKind.COMETE.goldenCometPercent(Difficulty.NORMAL));
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
        assertEquals(300_000L, EnemyKind.CROUPIER.getMaxHp(Difficulty.HARD), "chapitre 1 : le croupier a le quart du boss");
        assertEquals(67_500_000L, EnemyKind.OMBRE.getMaxHp(Difficulty.HARD));
    }

    @Test
    void hardModeHitsWithTheLateTowerDamageEverywhere() {
        // Chapitre 2 difficile (force x8,8) : l'Épée frappe comme au dernier chapitre normal (x6), x8,8/6.
        int normalLast = EnemyKind.MACHINE_ORIGINELLE.hurt(EnemyKind.CHEF.swordDamage(), Difficulty.NORMAL);
        assertEquals(normalLast * 880 / 600.0, EnemyKind.CHEF.hurt(EnemyKind.CHEF.swordDamage(), Difficulty.HARD), 2);
        assertTrue(EnemyKind.TEMPS_MORT.interestPercent(Difficulty.HARD) <= 100, "jamais plus que tous les gains");
    }

    @Test
    void normalModeExplorationAndTrainingDoNotChange() {
        assertEquals(2_500_000L, EnemyKind.MACHINE_ORIGINELLE.getMaxHp(Difficulty.NORMAL)); // 2 M x 125 % (Puissance)
        assertEquals(11_000L, EnemyKind.ROI_PIQUE.getMaxHp(Difficulty.NORMAL), "l'Exploration normale ne change pas");
        assertEquals(EnemyKind.ENTRAINEMENT.getPower(Difficulty.NORMAL), EnemyKind.ENTRAINEMENT.getPower(Difficulty.HARD), "l'Entraînement non plus");
        assertEquals(600, EnemyKind.MACHINE_ORIGINELLE.getPower(Difficulty.NORMAL));

        // Une Machine à 150 M de PV encaisse et se soigne sans déborder.
        Enemy machine = new Enemy(EnemyKind.MACHINE_ORIGINELLE, true);
        assertEquals(50_000_000L, machine.takeDamage(50_000_000L));
        assertEquals(100_000_000L, machine.getHp());
        assertTrue(machine.heal(100_000_000L) > 0);
        assertFalse(new Enemy(EnemyKind.MACHINE_ORIGINELLE).isHard());
    }
}
