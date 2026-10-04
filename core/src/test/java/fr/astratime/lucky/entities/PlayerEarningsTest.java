package fr.astratime.lucky.entities;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Les gains acquis pendant un combat, versés en pièces à la fin de celui-ci. */
class PlayerEarningsTest {

    @Test
    void everythingEarnedCountsEvenIfSpentLater() {
        Player player = new Player("Joueur", 100, List.of());
        player.addGains(500);
        player.addGains(-300); // un achat à l'échoppe
        player.addGains(200);
        assertEquals(400, player.getGains());
        assertEquals(700, player.getEarnedThisCombat());
    }

    @Test
    void theNextCombatKeepsTheGainsButStartsANewCount() {
        Player player = new Player("Joueur", 100, List.of());
        player.addGains(1000);
        Player next = player.nextCombat();
        assertEquals(1000, next.getGains());
        assertEquals(0, next.getEarnedThisCombat(), "les gains gardés ne sont pas versés deux fois");
    }
}
