package fr.astratime.lucky.assets;

import fr.astratime.lucky.entities.exploration.DungeonRun;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.entities.tower.Chapter;
import fr.astratime.lucky.entities.tower.TowerRun;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
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
    void everyMusicFileExists() {
        for (String path : List.of(CombatMusic.DEFAULT, CombatMusic.PORT, CombatMusic.MINES, CombatMusic.CASINO,
            CombatMusic.TOWER, CombatMusic.BOSS)) {
            assertTrue(Files.exists(Paths.get("../assets").resolve(path)), path);
        }
    }
}
