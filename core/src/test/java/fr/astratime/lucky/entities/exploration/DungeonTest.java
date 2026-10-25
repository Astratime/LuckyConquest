package fr.astratime.lucky.entities.exploration;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.run.CombatRun;
import fr.astratime.lucky.loaders.CardLoader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/** Les donjons de la prairie : leurs deux combats, leur coffre et la descente elle-même. */
class DungeonTest {

    private static final CardLoader.AssetReader READER = path -> {
        try {
            return Files.readString(Path.of("..", "assets").resolve(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    };

    @Test
    void thePrairieHasTheFourSuitDungeons() {
        assertEquals("La prairie", Place.PRAIRIE.getName());
        EnumSet<Card.Suit> suits = EnumSet.noneOf(Card.Suit.class);
        Place.PRAIRIE.getDungeons().forEach(dungeon -> suits.add(dungeon.getSuit()));
        assertEquals(EnumSet.allOf(Card.Suit.class), suits);
    }

    @Test
    void eachPrairieDungeonHasASoldierThenItsKing() {
        for (Dungeon dungeon : Place.PRAIRIE.getDungeons()) {
            assertEquals(10_000, new Enemy(dungeon.getSoldier()).getMaxHp(), dungeon.name());
            assertEquals(20_000, new Enemy(dungeon.getKing()).getMaxHp(), dungeon.name());
            assertEquals(100, dungeon.getSoldier().getPower(), "force du chapitre 1");
            assertEquals(100, dungeon.getKing().getPower());
            assertTrue(dungeon.getKing().isBoss());
            assertFalse(dungeon.getSoldier().isBoss());
            assertTrue(dungeon.getSoldier().getDisplayName().startsWith("Soldat de"));
            assertTrue(dungeon.getKing().getDisplayName().startsWith("Roi de"));
            assertEquals(dungeon.getSuit(), dungeon.getSoldier().getPriority().get(0), "le soldat joue sa couleur d'abord");
            assertEquals(dungeon.getSuit(), dungeon.getKing().getPriority().get(0));
        }
    }

    @Test
    void thePlacesOpenInOrderWithTheirHpAndAChiefAtTheEnd() {
        Place[] order = {Place.PRAIRIE, Place.FOIRE, Place.PORT, Place.MINES, Place.CASINO, Place.DESERT};
        assertEquals(List.of(order), List.of(Place.values()));
        // PV du soldat (le roi en a le double), choix d'Astra 2026-10-24 : le Gouffre, la Salle des machines,
        // les Coffres et le Tombeau à part
        Map<Dungeon, Integer> hp = new HashMap<>();
        for (Dungeon d : Place.FOIRE.getDungeons()) hp.put(d, 50_000); // Astra 2026-10-25
        for (Dungeon d : Place.PORT.getDungeons()) hp.put(d, 200_000);
        for (Dungeon d : Place.MINES.getDungeons()) hp.put(d, 300_000);
        for (Dungeon d : Place.CASINO.getDungeons()) hp.put(d, 1_200_000);
        for (Dungeon d : Place.DESERT.getDungeons()) hp.put(d, 1_500_000);
        hp.put(Dungeon.GOUFFRE, 200_000);
        hp.put(Dungeon.MACHINES, 900_000);
        hp.put(Dungeon.COFFRES, 2_500_000);
        hp.put(Dungeon.TOMBEAU, 1_050_000);
        for (int i = 1; i < order.length; i++) {
            assertEquals(4, order[i].getDungeons().size(), order[i].name());
            assertEquals(order[i - 1], order[i].getPrevious(), "ordre d'ouverture");
            for (Dungeon dungeon : order[i].getDungeons()) {
                assertEquals((int) hp.get(dungeon), new Enemy(dungeon.getSoldier()).getMaxHp(), dungeon.name());
                assertEquals(2 * hp.get(dungeon), new Enemy(dungeon.getKing()).getMaxHp(), dungeon.name());
                assertTrue(dungeon.getKing().isBoss(), dungeon.name());
                assertFalse(dungeon.getSoldier().isBoss(), dungeon.name());
                assertEquals(order[i], Place.of(dungeon));
            }
        }
    }

    @Test
    void eachChestHoldsTwoCardsAtFortyPercentAndAStrongerOneAtTwenty() {
        Function<String, Card> factory = CardLoader.cardFactory(READER);
        for (Dungeon dungeon : Dungeon.values()) {
            List<Dungeon.Loot> loot = dungeon.getLoot();
            assertEquals(List.of(40, 40, 20), loot.stream().map(Dungeon.Loot::percent).toList(), dungeon.name());
            for (Dungeon.Loot entry : loot) {
                Card card = factory.apply(entry.cardId()); // la carte existe
                assertNull(card.getSuit(), entry.cardId() + " n'a pas de suite : elle ne compte pas dans les combinaisons");
            }
        }
    }

    @Test
    void theChestCardIsDrawnWithItsChances() {
        Map<String, Integer> counts = new HashMap<>();
        Random random = new Random(42);
        int draws = 20_000;
        for (int i = 0; i < draws; i++) counts.merge(Dungeon.PIQUE.rollLoot(random), 1, Integer::sum);
        assertEquals(0.4, counts.get("pierre_a_aiguiser") / (double) draws, 0.02);
        assertEquals(0.4, counts.get("dague_de_l_ombre") / (double) draws, 0.02);
        assertEquals(0.2, counts.get("guillotine") / (double) draws, 0.02);
    }

    @Test
    void aDungeonRunGoesFromTheSoldierToTheKingThenTheChest() {
        DungeonRun run = new DungeonRun(Dungeon.COEUR);
        assertEquals(Dungeon.COEUR.getSoldier(), run.getEnemy());
        assertFalse(run.isBossStage());
        assertEquals("Donjon du Coeur", run.getLabel());

        assertEquals(CombatRun.Next.BOSS, run.win());
        assertEquals(Dungeon.COEUR.getKing(), run.getEnemy());
        assertTrue(run.isBossStage());

        assertEquals(CombatRun.Next.CLEARED, run.win());
    }

    @Test
    void aDefeatRestartsTheDungeonAtTheSoldier() {
        DungeonRun run = new DungeonRun(Dungeon.TREFLE);
        run.win();
        run.restart();
        assertEquals(0, run.getStage());
        assertEquals(Dungeon.TREFLE.getSoldier(), run.getEnemy());
    }

    @Test
    void shortNamesAreTheSuitNames() {
        assertEquals("Pique", Dungeon.PIQUE.getShortName());
        assertEquals("Trèfle", Dungeon.TREFLE.getShortName());
        assertEquals("Coeur", Dungeon.COEUR.getShortName());
        assertEquals("Carreau", Dungeon.CARREAU.getShortName());
    }
}
