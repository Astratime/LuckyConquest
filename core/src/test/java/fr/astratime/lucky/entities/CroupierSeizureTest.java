package fr.astratime.lucky.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Saisie du Croupier (Astra, 2026-11-23) : il prend la moitié d'une jauge, ou fait compter tes combinaisons à moitié. */
class CroupierSeizureTest {

    @Test
    void aSeizureTakesHalfOfAGaugeRoundedUp() {
        LastingEffects lasting = new LastingEffects();
        lasting.addVault(75_000);
        lasting.addBlades(7);
        lasting.addLuckyStar(40);
        assertEquals(0.5, lasting.fill(LastingEffects.Seizable.VAULT), 1e-9);
        assertEquals(37_500, lasting.seize(LastingEffects.Seizable.VAULT, 50));
        assertEquals(37_500, lasting.getVault());
        assertEquals(4, lasting.seize(LastingEffects.Seizable.BLADES, 50));
        assertEquals(3, lasting.getBlades());
        assertEquals(20, lasting.seize(LastingEffects.Seizable.STAR, 50));
        assertEquals(20, lasting.getLuckyStar());
    }

    @Test
    void comboSeizuresAreCountedOnceThenCleared() {
        LastingEffects lasting = new LastingEffects();
        assertTrue(lasting.isEmpty());
        lasting.seize(LastingEffects.Seizable.COMBOS, 50);
        assertFalse(lasting.isEmpty());
        assertEquals(1, lasting.takeComboSeizures());
        assertEquals(0, lasting.takeComboSeizures());
    }
}
