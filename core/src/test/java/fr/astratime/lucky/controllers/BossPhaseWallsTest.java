package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.entities.tower.Chapter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Seuils de phase de tous les boss et rois (choix d'Astra) : un coup qui franchit
 * un seuil s'arrête dessus (le surplus est perdu) ; le boss passe à la phase
 * suivante à son tour, une phase à la fois, puis peut être blessé plus bas.
 */
class BossPhaseWallsTest {

    private static final long HUGE = 1_000_000_000_000L;

    /** @return les boss de la Tour et les rois de l'Exploration. */
    private static List<EnemyKind> bosses() {
        List<EnemyKind> bosses = new ArrayList<>();
        for (Chapter chapter : Chapter.values()) bosses.add(chapter.getBoss());
        for (Place place : Place.values()) for (Dungeon dungeon : place.getDungeons()) bosses.add(dungeon.getKing());
        return bosses;
    }

    @Test
    void everyBossHasAtLeastOnePhaseThreshold() {
        List<EnemyKind> bosses = bosses();
        assertEquals(30, bosses.size());
        for (EnemyKind boss : bosses) {
            assertTrue(boss.isBoss(), boss.name());
            assertTrue(boss.phaseCount() >= 2, boss.name() + " a une phase 2");
        }
        assertEquals(0, EnemyKind.SOLDAT_PIQUE.phaseThresholds().length, "un soldat n'a pas de seuil");
    }

    @Test
    void aHugeHitStopsAtEachThresholdUntilTheBossTurn() {
        for (EnemyKind boss : bosses()) {
            Enemy enemy = new Enemy(boss);
            if (boss.houseWins()) assertEquals(0, enemy.takeDamage(HUGE), "la Maison annule son premier gros coup");
            float[] thresholds = boss.phaseThresholds();
            for (int k = 0; k < thresholds.length; k++) {
                long wall = (long) Math.ceil(enemy.getMaxHp() * (double) thresholds[k]);
                enemy.takeDamage(HUGE);
                assertEquals(wall, enemy.getHp(), boss.name() + " s'arrête au seuil " + (k + 1));
                assertNotNull(enemy.takeNotice());
                enemy.takeDamage(1_000_000);
                assertEquals(wall, enemy.getHp(), boss.name() + " : plus rien ne passe avant son tour");
                assertTrue(enemy.enterPhaseTwo(), boss.name() + " change de phase à son tour");
                assertEquals(k + 2, enemy.getPhase(), boss.name());
                assertFalse(enemy.enterPhaseTwo(), boss.name() + " : une seule annonce par phase");
            }
            enemy.resetDamageTaken();
            enemy.takeDamage(HUGE);
            if (boss.hasLastDraw()) assertEquals(1, enemy.getHp(), "la Machine attend son Dernier tirage");
            else assertTrue(enemy.isDefeated(), boss.name() + " : après sa dernière phase, il tombe d'un coup");
        }
    }

    @Test
    void noMoreHalfHpCapPerTurnAboveTheThreshold() {
        // Plus de limite à 50 % des PV max par tour : seule la Garde au seuil de phase arrête les coups.
        // Un roi sans phase franchie perd d'un coup tout ce qui le sépare de son seuil, puis plus rien.
        Enemy king = new Enemy(EnemyKind.TAVERNIER);
        king.takeDamage(king.getMaxHp() / 4);
        king.takeDamage(king.getMaxHp() / 4);
        king.takeDamage(king.getMaxHp() / 4);
        assertEquals((long) Math.ceil(king.getMaxHp() / 2.0), king.getHp(), "arrêté à son seuil, pas avant");
        assertEquals(0, king.takeDamage(king.getMaxHp()), "invincible jusqu'à son tour");
        assertTrue(king.enterPhaseTwo(), "phase 2 à son tour");
        assertEquals(2, king.getPhase());
        assertTrue(king.takeDamage(1) > 0, "l'invincibilité a disparu");
    }

    @Test
    void aKingAboveHisThresholdStaysInPhaseOne() {
        Enemy king = new Enemy(EnemyKind.TAVERNIER);
        king.takeDamage(king.getMaxHp() / 3);
        assertFalse(king.enterPhaseTwo(), "au-dessus de la moitié : pas de phase 2 (plus de Garde par tour)");
        assertEquals(1, king.getPhase());
    }

    @Test
    void theQueenTurnsHerRoyalBetIntoAPhaseTwo() {
        Enemy queen = TowerChaptersTest.plain(EnemyKind.REINE);
        assertFalse(queen.enterPhaseTwo());
        queen.takeDamage(queen.getMaxHp());
        assertEquals((long) Math.ceil(queen.getMaxHp() / 2.0), queen.getHp());
        assertTrue(queen.enterPhaseTwo());
        assertEquals(2, queen.getPhase());
        assertNotEquals(EnemyKind.REINE.phaseText(2), EnemyKind.CHEF.phaseText(2), "sa propre annonce");
    }
}
