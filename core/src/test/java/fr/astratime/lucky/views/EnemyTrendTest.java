package fr.astratime.lucky.views;

import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Tendance de l'ennemi (Astra, 2026-11-23) : le symbole au plus grand poids, sans rien changer au tirage. */
class EnemyTrendTest {

    @Test
    void theTrendIsTheHeaviestSymbol() {
        Map<EnemySymbol, Integer> weights = new EnumMap<>(EnemySymbol.class);
        weights.put(EnemySymbol.SWORD, 3);
        weights.put(EnemySymbol.SHIELD, 5);
        weights.put(EnemySymbol.POTION, 1);
        assertEquals(EnemySymbol.SHIELD, EnemyView.likeliest(weights));
    }

    @Test
    void everyEnemyHasATrend() {
        for (EnemyKind kind : EnemyKind.values()) {
            for (int phase = 1; phase <= kind.phaseCount(); phase++) {
                assertNotNull(EnemyView.likeliest(kind.getSpinWeights(phase)), kind + " phase " + phase);
            }
        }
    }
}
