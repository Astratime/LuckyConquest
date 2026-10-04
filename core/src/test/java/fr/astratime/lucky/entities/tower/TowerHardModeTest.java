package fr.astratime.lucky.entities.tower;

import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TowerHardModeTest {

    @AfterEach
    void backToNormal() { EnemyKind.setTowerHard(false); }

    @Test
    void aHardRunIsLabelledAndRemembered() {
        TowerRun run = new TowerRun(Chapter.GENESE, true);
        assertTrue(run.isHard());
        assertEquals("Chapitre 1 difficile", run.getLabel());
        assertFalse(new TowerRun(Chapter.GENESE).isHard());
        assertEquals("Chapitre 1", new TowerRun(Chapter.GENESE).getLabel());
    }

    @Test
    void hardModeDoublesTheHpAndPowerOfTowerEnemiesOnly() {
        int hp = EnemyKind.MACHINE_ORIGINELLE.getMaxHp();
        int power = EnemyKind.MACHINE_ORIGINELLE.getPower();
        int croupierPower = EnemyKind.CROUPIER.getPower();
        int dungeonHp = EnemyKind.ROI_PIQUE.getMaxHp();
        int trainingPower = EnemyKind.ENTRAINEMENT.getPower();

        EnemyKind.setTowerHard(true);
        assertEquals(hp * EnemyKind.HARD_HP_FACTOR, EnemyKind.MACHINE_ORIGINELLE.getMaxHp());
        assertEquals(power * EnemyKind.HARD_POWER_FACTOR, EnemyKind.MACHINE_ORIGINELLE.getPower());
        assertEquals(croupierPower * EnemyKind.HARD_POWER_FACTOR, EnemyKind.CROUPIER.getPower(), "dès le chapitre 1");
        assertEquals(20_000_000, new Enemy(EnemyKind.MACHINE_ORIGINELLE).getMaxHp());
        assertEquals(dungeonHp, EnemyKind.ROI_PIQUE.getMaxHp(), "l'Exploration ne change pas");
        assertEquals(trainingPower, EnemyKind.ENTRAINEMENT.getPower(), "l'Entraînement non plus");

        EnemyKind.setTowerHard(false);
        assertEquals(hp, EnemyKind.MACHINE_ORIGINELLE.getMaxHp());
        assertEquals(power, EnemyKind.MACHINE_ORIGINELLE.getPower());
    }
}
