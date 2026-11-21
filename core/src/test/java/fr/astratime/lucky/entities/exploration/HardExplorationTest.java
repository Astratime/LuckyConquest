package fr.astratime.lucky.entities.exploration;

import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.SpinEconomy;
import fr.astratime.lucky.entities.enemy.Difficulty;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.progress.PlayerProfile;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.*;

class HardExplorationTest {

    private static List<EnemyKind> enemies(Place place) {
        List<EnemyKind> enemies = new ArrayList<>();
        for (Dungeon dungeon : place.getDungeons()) {
            enemies.add(dungeon.getSoldier());
            enemies.add(dungeon.getKing());
        }
        return enemies;
    }

    private static double average(Place place, ToDoubleFunction<EnemyKind> stat) {
        return enemies(place).stream().mapToDouble(stat).average().orElseThrow();
    }

    @Test
    void theFirstHardPlaceIsAsHardAsTheDesertThenEachTierIsRootTwoStronger() {
        for (Place place : Place.values()) {
            double times = Math.pow(Math.sqrt(2.0), place.getTier() - 1);
            assertEquals(times, HardExploration.strength(place), 1e-9);
            double hp = average(Place.DESERT, k -> k.getMaxHp(Difficulty.NORMAL)) * times;
            double attack = average(Place.DESERT, k -> k.hurt(k.swordDamage(), Difficulty.NORMAL)) * times;
            double defense = average(Place.DESERT, k -> k.getBaseDefense(Difficulty.NORMAL)) * times;
            assertEquals(1, average(place, k -> k.getMaxHp(Difficulty.HARD)) / hp, 0.001, place + " : PV");
            assertEquals(1, average(place, k -> k.hurt(k.swordDamage(), Difficulty.HARD)) / attack, 0.015, place + " : attaque (arrondis des petits coups)");
            assertEquals(1, average(place, k -> k.getBaseDefense(Difficulty.HARD)) / defense, 0.001, place + " : défense");
        }
    }

    @Test
    void theDungeonsOfAPlaceKeepTheirDifferences() {
        // Le Pique de la prairie frappe toujours moins fort que le Coeur, et le roi a toujours deux fois les PV du soldat.
        assertTrue(EnemyKind.ROI_PIQUE.hurt(EnemyKind.ROI_PIQUE.swordDamage(), Difficulty.HARD)
            < EnemyKind.ROI_COEUR.hurt(EnemyKind.ROI_COEUR.swordDamage(), Difficulty.HARD));
        assertEquals(2 * EnemyKind.SOLDAT_PIQUE.getMaxHp(Difficulty.HARD), EnemyKind.ROI_PIQUE.getMaxHp(Difficulty.HARD));
    }

    @Test
    void normalModeIsUnchanged() {
        assertEquals(11_000L, EnemyKind.ROI_PIQUE.getMaxHp(Difficulty.NORMAL)); // 20 000 x 55 % (Puissance)
        assertEquals(100, EnemyKind.ROI_PIQUE.getSpinCost(Difficulty.NORMAL));
        assertEquals(140_000, new Enemy(EnemyKind.DRAGON).getSpinCost());
        assertEquals(EnemyKind.DRAGON.getBaseDefense(Difficulty.NORMAL), EnemyKind.DRAGON.fortified(60_000, Difficulty.NORMAL));
        // La Tour difficile ne passe pas par l'Exploration difficile.
        assertEquals(5_000, EnemyKind.MACHINE_ORIGINELLE.getSpinCost(Difficulty.HARD));
    }

    @Test
    void coinsFollowTheDesertSpinCostPlusTwentyTwoAndAHalfPercentPerTier() {
        assertEquals(100, HardExploration.COINS_PERCENT, "aucune prime (choix d'Astra)");
        // Paliers 1 (prairie), 2 (Foire), 3 (Rapide), 4 (Port), 6 (Mines), 8 (Casino), 10 (Désert), arrondis à la centaine.
        int[] expected = {140_000, 171_500, 210_100, 257_400, 386_200, 579_500, 869_700};
        for (Place place : Place.values()) {
            int cost = expected[place.ordinal()];
            assertEquals(cost, place.getSpinCost(true));
            assertEquals(cost, new Enemy(place.getDungeons().get(0).getKing(), true).getSpinCost());
            assertEquals(5_000L * cost, place.getChestCoins(true));
            assertEquals(500L * cost, place.getDefeatCap(true));
            // La force tirée des gains suit les ennemis (le Désert x1,41 par palier).
            assertEquals(Math.round(3_000 * HardExploration.strength(place)), SpinEconomy.powerSpinCost(cost));
        }
        long kept = PlayerProfile.combatReward(false, true, 1e18, Place.PRAIRIE, true, 1);
        assertEquals(Place.PRAIRIE.getDefeatCap(true), kept);
    }

    @Test
    void aHardRunIsLabelled() {
        DungeonRun run = new DungeonRun(Dungeon.PIQUE).hard(true);
        assertTrue(run.isHard());
        assertEquals(Difficulty.HARD, run.getDifficulty());
        assertTrue(run.getLabel().endsWith("difficile"));
        assertFalse(new DungeonRun(Dungeon.PIQUE).isHard());
    }
}
