package fr.astratime.lucky.assets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** La Machine Originelle tire son levier et cligne des yeux une fois toutes les 10 secondes. */
class MachineAnimationTest {

    @Test
    void leverIsPulledOncePerCycle() {
        assertEquals(Chapter456Art.LEVER_UP, EnemyTextures.machineLever(0f));
        assertEquals(Chapter456Art.LEVER_UP, EnemyTextures.machineLever(3.9f));
        assertEquals(Chapter456Art.LEVER_HALF, EnemyTextures.machineLever(4.02f));
        assertEquals(Chapter456Art.LEVER_DOWN, EnemyTextures.machineLever(4.3f));
        assertEquals(Chapter456Art.LEVER_HALF, EnemyTextures.machineLever(4.6f));
        assertEquals(Chapter456Art.LEVER_UP, EnemyTextures.machineLever(5f));
        assertEquals(Chapter456Art.LEVER_DOWN, EnemyTextures.machineLever(4.3f + EnemyTextures.MACHINE_CYCLE));
    }

    @Test
    void eyesBlinkOncePerCycle() {
        assertEquals(Chapter456Art.LIDS_OPEN, EnemyTextures.machineLids(8.9f));
        assertEquals(Chapter456Art.LIDS_HALF, EnemyTextures.machineLids(9.03f));
        assertEquals(Chapter456Art.LIDS_CLOSED, EnemyTextures.machineLids(9.15f));
        assertEquals(Chapter456Art.LIDS_HALF, EnemyTextures.machineLids(9.28f));
        assertEquals(Chapter456Art.LIDS_OPEN, EnemyTextures.machineLids(9.5f));
        assertEquals(Chapter456Art.LIDS_CLOSED, EnemyTextures.machineLids(19.15f));
    }
}
