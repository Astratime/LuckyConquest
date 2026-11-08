package fr.astratime.lucky.entities.tower;

import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.enemy.Difficulty;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.run.CombatRun;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class EndlessRunTest {

    @Test
    void floorsClimbWithAChoiceOfThreeAndABossEveryFiveFloors() {
        EndlessRun run = new EndlessRun(new Random(7));
        assertEquals(1, run.getFloor());
        assertTrue(EndlessRun.ENEMIES.contains(run.getEnemy()));
        for (int floor = 2; floor <= 30; floor++) {
            EnemyKind previous = run.getEnemy();
            CombatRun.Next next = run.win();
            assertEquals(floor, run.getFloor());
            if (floor % EndlessRun.BOSS_EVERY == 0) {
                assertEquals(CombatRun.Next.BOSS, next, "étage " + floor);
                assertTrue(run.isBossStage());
                assertTrue(EndlessRun.BOSSES.contains(run.getEnemy()));
            } else {
                assertEquals(CombatRun.Next.CHOOSE_ENEMY, next, "étage " + floor);
                List<EnemyKind> choices = run.getChoices();
                assertEquals(EndlessRun.CHOICES, new HashSet<>(choices).size());
                assertFalse(choices.contains(previous), "pas deux fois de suite le même adversaire");
                assertTrue(EndlessRun.ENEMIES.containsAll(choices));
                assertEquals(choices.get(1), run.choose(1));
                assertFalse(run.isBossStage());
            }
            assertEquals(Difficulty.endless(floor), run.getDifficulty());
        }
        assertEquals(List.of(EnemyKind.COMETE, EnemyKind.REINE, EnemyKind.ECLAT, EnemyKind.PRETENDANT, EnemyKind.MAISON),
            EndlessRun.BOSSES);
        assertEquals(EnemyKind.COMETE, EndlessRun.bossOf(30), "les boss reviennent à tour de rôle");
    }

    @Test
    void theOriginalMachineIsNeverMet() {
        assertFalse(EndlessRun.ENEMIES.contains(EnemyKind.MACHINE_ORIGINELLE));
        assertFalse(EndlessRun.BOSSES.contains(EnemyKind.MACHINE_ORIGINELLE));
        assertEquals(24, EndlessRun.ENEMIES.size(), "premiers ennemis et challengers des six chapitres");
    }

    @Test
    void aDefeatRestartsAtFloorOne() {
        EndlessRun run = new EndlessRun(new Random(3));
        run.win();
        run.choose(0);
        run.win();
        run.restart();
        assertEquals(1, run.getFloor());
        assertTrue(run.getChoices().isEmpty());
        assertTrue(run.isHard());
        assertEquals("Mode sans fin", run.getLabel());
    }

    @Test
    void floorOneIsAlreadyStrongerThanHardChapterSix() {
        int hardSixPower = EnemyKind.hardPower(6);
        long hardSixBossHp = EnemyKind.hardBossHp(6);
        long hardSixStrongest = 0;
        for (EnemyKind kind : TowerRun.fights(Chapter.LE_JACKPOT)) {
            if (kind != Chapter.LE_JACKPOT.getBoss()) hardSixStrongest = Math.max(hardSixStrongest, kind.getMaxHp(Difficulty.HARD));
        }
        Difficulty first = Difficulty.endless(1);
        for (EnemyKind kind : EndlessRun.ENEMIES) {
            assertTrue(kind.getPower(first) > hardSixPower, kind.name());
            assertTrue(kind.getMaxHp(first) > hardSixStrongest, kind.name());
            assertTrue(kind.hurt(kind.swordDamage(), first) > EnemyKind.MACHINE_ORIGINELLE.hurt(EnemySymbol.SWORD_DAMAGE, Difficulty.HARD),
                kind.name());
        }
        Difficulty firstBoss = Difficulty.endless(EndlessRun.BOSS_EVERY);
        for (EnemyKind boss : EndlessRun.BOSSES) assertTrue(boss.getMaxHp(firstBoss) > hardSixBossHp, boss.name());
        assertEquals(24_000, EnemyKind.endlessPower(1));
        assertEquals(6_250_000_000L, EnemyKind.endlessHp(1, false));
        assertEquals(12_500_000_000L, EnemyKind.endlessHp(1, true));
    }

    @Test
    void eachFloorIsStrongerThanTheLast() {
        for (int floor = 1; floor < 60; floor++) {
            assertTrue(EnemyKind.endlessPower(floor + 1) > EnemyKind.endlessPower(floor), "étage " + floor);
            assertTrue(EnemyKind.endlessHp(floor + 1, false) > EnemyKind.endlessHp(floor, false), "étage " + floor);
        }
    }

    @Test
    void extremeFloorsStayPositive() {
        Difficulty far = Difficulty.endless(5_000);
        EnemyKind kind = EnemyKind.GARDIEN_LEVIER;
        assertTrue(kind.getPower(far) > 0);
        assertTrue(kind.getMaxHp(far) > 0);
        assertTrue(kind.hurt(kind.swordDamage(), far) > 0);
        assertTrue(kind.getBaseDefense(far) > 0);
        assertTrue(new Enemy(kind, far).getMaxHp() > 0);
    }

    @Test
    void theSpinCostIsChapterSixOnEveryFloor() {
        assertEquals(EnemyKind.ENDLESS_SPIN_COST, new Enemy(EnemyKind.CROUPIER, Difficulty.endless(1)).getSpinCost());
        assertEquals(EnemyKind.MACHINE_ORIGINELLE.getSpinCost(), EnemyKind.ENDLESS_SPIN_COST, "comme au chapitre 6");
    }
}
