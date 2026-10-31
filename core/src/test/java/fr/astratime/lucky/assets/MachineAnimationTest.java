package fr.astratime.lucky.assets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** La Machine Originelle tire son levier et ses yeux clignotent une fois toutes les 10 secondes. */
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
    void eyesFlashTwiceOncePerCycle() {
        assertEquals(Chapter456Art.EYES_NORMAL, EnemyTextures.machineEyes(8.9f));
        assertEquals(Chapter456Art.EYES_LIT, EnemyTextures.machineEyes(9.05f));
        assertEquals(Chapter456Art.EYES_BLAZE, EnemyTextures.machineEyes(9.2f));
        assertEquals(Chapter456Art.EYES_NORMAL, EnemyTextures.machineEyes(9.45f));
        assertEquals(Chapter456Art.EYES_BLAZE, EnemyTextures.machineEyes(9.7f));
        assertEquals(Chapter456Art.EYES_NORMAL, EnemyTextures.machineEyes(10f));
        assertEquals(Chapter456Art.EYES_BLAZE, EnemyTextures.machineEyes(19.2f));
    }
}
