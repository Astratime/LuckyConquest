package fr.astratime.lucky.sim;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.exploration.Place;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/** Le simulateur d'équilibrage tourne (en petit) et ses chiffres restent dans les objectifs d'Astra. */
class BalanceSimulatorTest {

    private final BalanceSimulator simulator =
        new BalanceSimulator(Paths.get("../assets").toAbsolutePath().normalize(), BalanceSimulator.Sizes.QUICK);

    @Test
    void theQuickReportCoversTheThreeSections() {
        String report = simulator.report();
        assertTrue(report.contains("## 1. Tirages"));
        assertTrue(report.contains("## 2. Jeu bonus"));
        assertTrue(report.contains("## 3. Donjons"));
    }

    @Test
    void aSpinWithoutCardsReturnsBetweenZeroAndFiveTimesItsCostOnAverage() {
        double average = simulator.spins(EnemyKind.ENTRAINEMENT, false, 3_000, 7L).average();
        assertTrue(average >= 0 && average <= 5, "moyenne " + average);
        double prairie = simulator.spins(Place.PRAIRIE.getDungeons().get(0).getSoldier(), false, 3_000, 7L).average();
        assertTrue(prairie >= 0 && prairie <= 5, "moyenne " + prairie);
    }

    @Test
    void aBonusGamePaysAboutAHundredSpinCostsOnTheClassicMachine() {
        double[] totals = BalanceSimulator.bonusGames(Symbol.classicReels(), 20_000, 7L);
        double sum = 0;
        for (double total : totals) sum += total;
        double average = sum / totals.length;
        assertTrue(average > 70 && average < 130, "moyenne " + average);
    }
}
