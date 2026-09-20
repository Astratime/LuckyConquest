package fr.astratime.lucky.entities;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private final Player player = new Player("Joueur", 100, List.of());

    @Test
    void shieldAbsorbsDamageBeforeHp() {
        player.addShield(30);

        int actualLoss = player.takeDamage(20);

        assertEquals(0, actualLoss, "20 de bouclier suffisent à absorber 20 de dégâts");
        assertEquals(100, player.getHp());
        assertEquals(10, player.getShield(), "il doit rester 30 - 20 = 10 de bouclier");
    }

    @Test
    void damageExceedingShieldSpillsOverToHp() {
        player.addShield(10);

        int actualLoss = player.takeDamage(25);

        assertEquals(15, actualLoss, "25 - 10 de bouclier = 15 en trop");
        assertEquals(85, player.getHp());
        assertEquals(0, player.getShield());
    }

    @Test
    void hpNeverGoesBelowZero() {
        int actualLoss = player.takeDamage(500);

        assertEquals(100, actualLoss);
        assertEquals(0, player.getHp());
        assertTrue(player.isDefeated());
    }

    @Test
    void healNeverExceedsMaxHp() {
        player.takeDamage(30);

        player.heal(1000);

        assertEquals(100, player.getHp());
    }

    @Test
    void gainsNeverGoNegative() {
        player.addGains(5);

        player.addGains(-100);

        assertEquals(0, player.getGains());
    }

    @Test
    void consumeGainsPercentReturnsAndDeductsTheComputedAmount() {
        player.addGains(1000);

        int consumed = player.consumeGainsPercent(0.3f);

        assertEquals(300, consumed);
        assertEquals(700, player.getGains());
    }

    @Test
    void reflectPercentKeepsTheBestValueOfTheTurn() {
        player.setReflectPercent(50);
        player.setReflectPercent(30); // ne doit pas faire redescendre le bonus

        assertEquals(50, player.getReflectPercent());
    }

    @Test
    void resetTurnDefensesClearsShieldAndReflect() {
        player.addShield(40);
        player.setReflectPercent(80);

        player.resetTurnDefenses();

        assertEquals(0, player.getShield());
        assertEquals(0, player.getReflectPercent());
    }
}
