package fr.astratime.lucky.views;

import fr.astratime.lucky.entities.exploration.Place;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DungeonRewardsOverlayTest {

    @Test
    void theCoinLinesGiveTheChestOfThePlaceTheDefeatShareAndTheMinesReel() {
        List<String> prairie = DungeonRewardsOverlay.coinLines(Place.PRAIRIE, false);
        assertTrue(prairie.contains("Coffre : 50 000 pièces (500 tirages à 100)."), prairie.toString());
        assertTrue(prairie.contains("Carte du coffre déjà en 3 exemplaires : 25 000 pièces en plus."), prairie.toString());
        assertTrue(prairie.contains("Défaite : tu gardes 20 % de tes gains, en pièces (au plus 50 000), selon la part du donjon faite."), prairie.toString());

        List<String> casino = DungeonRewardsOverlay.coinLines(Place.CASINO, false);
        assertTrue(casino.contains("Coffre : 67 500 000 pièces (13 500 tirages à 5 000)."), casino.toString());

        List<String> mines = DungeonRewardsOverlay.coinLines(Place.MINES, false);
        assertEquals(prairie.size() + 1, mines.size(), "le Rouleau de la Mine en plus");
        assertTrue(DungeonRewardsOverlay.coinLines(Place.PRAIRIE, true).get(0).startsWith("Mode ADMIN"));
    }
}
