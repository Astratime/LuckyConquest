package fr.astratime.lucky.assets;

import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.exploration.DungeonRun;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.entities.tower.Chapter;
import fr.astratime.lucky.entities.tower.TowerRun;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Chaque lieu, la Tour et ses boss ont leur musique, et chaque fichier existe. */
class CombatMusicTest {

    @Test
    void eachPlaceHasItsOwnMusic() {
        assertEquals(CombatMusic.DEFAULT, CombatMusic.of(null), "Entraînement");
        assertEquals(CombatMusic.DEFAULT, CombatMusic.of(new DungeonRun(Place.PRAIRIE.getDungeons().get(0))));
        assertEquals(CombatMusic.PORT, CombatMusic.of(new DungeonRun(Place.PORT.getDungeons().get(0))));
        assertEquals(CombatMusic.MINES, CombatMusic.of(new DungeonRun(Place.MINES.getDungeons().get(3))));
        assertEquals(CombatMusic.CASINO, CombatMusic.of(new DungeonRun(Place.CASINO.getDungeons().get(1))));
    }

    @Test
    void theTowerBossHasItsOwnMusic() {
        TowerRun run = new TowerRun(Chapter.values()[0]);
        assertEquals(CombatMusic.TOWER, CombatMusic.of(run));
        while (!run.isBossStage()) {
            if (run.win() == TowerRun.Next.CHOOSE_ENEMY) run.choose(0);
        }
        assertEquals(CombatMusic.BOSS, CombatMusic.of(run));
    }

    @Test
    void someEnemiesHaveTheirOwnMusic() {
        TowerRun tower = new TowerRun(Chapter.values()[0]);
        assertEquals("music/boss/croupier.mp3", CombatMusic.of(tower, new Enemy(EnemyKind.CROUPIER)));
        assertEquals("music/boss/mousquetaire.mp3", CombatMusic.of(tower, new Enemy(EnemyKind.BRETTEUR)));
        assertEquals("music/boss/pretendant.mp3", CombatMusic.of(tower, new Enemy(EnemyKind.PRETENDANT)));
        assertEquals(CombatMusic.TOWER, CombatMusic.of(tower, new Enemy(EnemyKind.CHEF)), "sans musique : celle du lieu");
        // le Croupier de l'Entraînement garde la musique d'origine
        assertEquals(CombatMusic.DEFAULT, CombatMusic.of(null, new Enemy(EnemyKind.CROUPIER)));
        DungeonRun pique = new DungeonRun(Place.PRAIRIE.getDungeons().get(0));
        assertEquals("music/boss/roi_de_pique.mp3", CombatMusic.of(pique, new Enemy(EnemyKind.ROI_PIQUE)));
        assertEquals(CombatMusic.DEFAULT, CombatMusic.of(pique, new Enemy(EnemyKind.SOLDAT_PIQUE)));
    }

    @Test
    void theMachineChangesMusicInPhaseTwo() {
        TowerRun tower = new TowerRun(Chapter.LE_JACKPOT);
        Enemy machine = new Enemy(EnemyKind.MACHINE_ORIGINELLE);
        assertEquals("music/boss/machine_originelle_phase_1.mp3", CombatMusic.of(tower, machine));
        machine.takeDamage(machine.getMaxHp() * 6 / 10);
        machine.enterPhaseTwo();
        assertTrue(machine.getPhase() >= 2);
        assertEquals(CombatMusic.MACHINE_PHASE_2, CombatMusic.of(tower, machine));
    }

    @Test
    void theCometChangesMusicInPhaseTwo() {
        TowerRun tower = new TowerRun(Chapter.GENESE);
        Enemy comet = new Enemy(EnemyKind.COMETE);
        assertEquals("music/boss/comete.mp3", CombatMusic.of(tower, comet));
        comet.takeDamage(comet.getMaxHp() * 6 / 10);
        assertTrue(comet.enterPhaseTwo(), "sous la moitié de ses PV, elle s'embrase");
        assertEquals(CombatMusic.COMETE_PHASE_2, CombatMusic.of(tower, comet));
    }

    @Test
    void everyMusicFileExists() {
        List<String> paths = new ArrayList<>(List.of(CombatMusic.DEFAULT, CombatMusic.PORT, CombatMusic.MINES,
            CombatMusic.CASINO, CombatMusic.TOWER, CombatMusic.BOSS, CombatMusic.MACHINE_PHASE_2, CombatMusic.COMETE_PHASE_2));
        paths.addAll(CombatMusic.TOWER_ENEMIES.values());
        paths.addAll(CombatMusic.EXPLORATION_ENEMIES.values());
        for (String path : paths) {
            assertTrue(Files.exists(Paths.get("../assets").resolve(path)), path);
        }
    }
}
