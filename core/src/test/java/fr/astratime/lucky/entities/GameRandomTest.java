package fr.astratime.lucky.entities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** Le hasard des règles : ordinaire en jeu, répétable quand le simulateur fixe une graine. */
class GameRandomTest {

    @AfterEach
    void ordinaryChanceAgain() { GameRandom.clear(); }

    private static List<Integer> draws(Random random) {
        List<Integer> draws = new ArrayList<>();
        for (int i = 0; i < 20; i++) draws.add(random.nextInt(1000));
        return draws;
    }

    @Test
    void theSameSeedGivesTheSameDraws() {
        GameRandom.seed(42);
        List<Integer> first = draws(GameRandom.live());
        GameRandom.seed(42);
        assertEquals(first, draws(GameRandom.live()), "un objet qui garde live() suit la graine du fil");
        GameRandom.seed(43);
        assertNotEquals(first, draws(GameRandom.get()));
    }

    @Test
    void restoreGivesBackTheThreadsOwnSeed() {
        GameRandom.seed(1);
        Random own = GameRandom.seeded();
        GameRandom.seed(2);
        GameRandom.restore(own);
        assertSame(own, GameRandom.get());
        GameRandom.restore(null);
        assertNull(GameRandom.seeded());
    }
}
