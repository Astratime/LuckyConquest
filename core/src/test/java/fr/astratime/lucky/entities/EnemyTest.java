package fr.astratime.lucky.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnemyTest {

    private final Enemy enemy = new Enemy("Ennemi", 100);

    @Test
    void startsAtMaxHp() {
        assertEquals(100, enemy.getHp());
        assertEquals(100, enemy.getMaxHp());
        assertFalse(enemy.isDefeated());
    }

    @Test
    void takeDamageReducesHp() {
        enemy.takeDamage(30);

        assertEquals(70, enemy.getHp());
        assertFalse(enemy.isDefeated());
    }

    @Test
    void hpNeverGoesBelowZero() {
        enemy.takeDamage(500);

        assertEquals(0, enemy.getHp());
        assertTrue(enemy.isDefeated());
    }
}
