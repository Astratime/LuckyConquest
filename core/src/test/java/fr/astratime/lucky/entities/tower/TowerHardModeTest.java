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
    void hardModeDoublesTheHpAndPowerOfTowerEnemiesOnly() {
        int hp = EnemyKind.MACHINE_ORIGINELLE.getMaxHp(false);
        int power = EnemyKind.MACHINE_ORIGINELLE.getPower(false);
        int croupierPower = EnemyKind.CROUPIER.getPower(false);
        int dungeonHp = EnemyKind.ROI_PIQUE.getMaxHp(false);
        int trainingPower = EnemyKind.ENTRAINEMENT.getPower(false);

        assertEquals(hp * EnemyKind.HARD_HP_FACTOR, EnemyKind.MACHINE_ORIGINELLE.getMaxHp(true));
        assertEquals(power * EnemyKind.HARD_POWER_FACTOR, EnemyKind.MACHINE_ORIGINELLE.getPower(true));
        assertEquals(croupierPower * EnemyKind.HARD_POWER_FACTOR, EnemyKind.CROUPIER.getPower(true), "dès le chapitre 1");
        assertEquals(20_000_000, new Enemy(EnemyKind.MACHINE_ORIGINELLE, true).getMaxHp());
        assertTrue(new Enemy(EnemyKind.MACHINE_ORIGINELLE, true).isHard());
        assertEquals(dungeonHp, EnemyKind.ROI_PIQUE.getMaxHp(true), "l'Exploration ne change pas");
        assertEquals(trainingPower, EnemyKind.ENTRAINEMENT.getPower(true), "l'Entraînement non plus");

        // Plus d'état global : un ennemi normal créé ensuite reste normal.
        assertEquals(hp, new Enemy(EnemyKind.MACHINE_ORIGINELLE).getMaxHp());
        assertFalse(new Enemy(EnemyKind.MACHINE_ORIGINELLE).isHard());
    }
}
