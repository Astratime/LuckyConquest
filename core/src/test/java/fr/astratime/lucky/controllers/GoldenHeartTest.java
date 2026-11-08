package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.exploration.Place;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Cœur d'or : les gains du tour frappent l'ennemi, comptés au coût de tirage de force du lieu. */
class GoldenHeartTest {

    @Test
    void gainsAreCountedAtThePlacePowerSpinCost() {
        // Désert : 140 000 de gains = 1 tirage, soit 3 000 de coup (et non 140 000)
        assertEquals(Place.DESERT.getPowerSpinCost(),
            CombatResolver.goldenHeartHit(Place.DESERT.getSpinCost(), Place.DESERT.getSpinCost()), 1e-6);
        assertEquals(10.0 * Place.CASINO.getPowerSpinCost(),
            CombatResolver.goldenHeartHit(10.0 * Place.CASINO.getSpinCost(), Place.CASINO.getSpinCost()), 1e-6);
    }

    @Test
    void prairieAndTowerKeepTheirGains() {
        assertEquals(5_000, CombatResolver.goldenHeartHit(5_000, Place.PRAIRIE.getSpinCost()), 1e-6);
        assertEquals(5_000, CombatResolver.goldenHeartHit(5_000, 2_000), 1e-6); // Tour, chapitre 5
    }
}
