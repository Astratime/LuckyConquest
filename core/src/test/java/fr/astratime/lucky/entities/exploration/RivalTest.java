package fr.astratime.lucky.entities.exploration;

import fr.astratime.lucky.entities.enemy.Difficulty;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.run.CombatRun;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Le Joueur Masqué : où et quand il prend la place du soldat, ce qu'il paie et sa force. */
class RivalTest {

    @Test
    void oneEncounterPerPlaceFromTheFairToTheDesert() {
        assertNull(Rival.of(Place.PRAIRIE));
        for (Place place : Place.values()) {
            EnemyKind rival = Rival.of(place);
            if (rival == null) continue;
            assertTrue(rival.isRival());
            assertEquals(place, Rival.placeOf(rival));
            assertEquals(place, Place.of(rival), "ses stats sont celles du lieu de la rencontre");
            assertEquals(place.ordinal(), Rival.encounter(rival));
        }
        assertEquals(8, Rival.encounter(EnemyKind.RIVAL_DESERT));
        assertTrue(Rival.isFinal(EnemyKind.RIVAL_DESERT));
        assertFalse(EnemyKind.SOLDAT_PIQUE.isRival());
        assertNull(Rival.placeOf(EnemyKind.CLOWN));
    }

    @Test
    void heCostsTheSpinsOfThePlaceLikeTheSoldierHeReplaces() {
        for (Place place : Place.values()) {
            EnemyKind rival = Rival.of(place);
            if (rival == null) continue;
            for (Dungeon dungeon : place.getDungeons()) {
                assertEquals(dungeon.getSoldier().getSpinCost(), rival.getSpinCost(), dungeon.name());
            }
        }
    }

    @Test
    void anEncounterWonNeverComesBackAndTheDesertWaitsForTheFourOthers() {
        Set<EnemyKind> defeated = EnumSet.noneOf(EnemyKind.class);
        assertEquals(EnemyKind.RIVAL_FOIRE, Rival.due(Place.FOIRE, defeated::contains));
        assertEquals(EnemyKind.RIVAL_CASINO, Rival.due(Place.CASINO, defeated::contains), "chaque lieu a sa rencontre");
        assertNull(Rival.due(Place.DESERT, defeated::contains), "le duel final attend les sept premières");
        assertNull(Rival.due(Place.PRAIRIE, defeated::contains));

        defeated.add(EnemyKind.RIVAL_FOIRE);
        assertNull(Rival.due(Place.FOIRE, defeated::contains));
        defeated.addAll(Set.of(EnemyKind.RIVAL_RAPIDE, EnemyKind.RIVAL_PORT, EnemyKind.RIVAL_PALACE, EnemyKind.RIVAL_MINES));
        assertNull(Rival.due(Place.DESERT, defeated::contains));
        defeated.add(EnemyKind.RIVAL_CASINO);
        assertNull(Rival.due(Place.DESERT, defeated::contains));
        defeated.add(EnemyKind.RIVAL_TEMPLE);
        assertEquals(EnemyKind.RIVAL_DESERT, Rival.due(Place.DESERT, defeated::contains));
        defeated.add(EnemyKind.RIVAL_DESERT);
        assertNull(Rival.due(Place.DESERT, defeated::contains));
    }

    @Test
    void heShowsUpAboutOnceInEight() {
        Random random = new Random(7);
        int seen = 0, tries = 80_000;
        for (int i = 0; i < tries; i++) {
            if (Rival.roll(Place.PORT, kind -> false, random) != null) seen++;
        }
        assertEquals(tries / Rival.CHANCE, seen, tries / 80.0);
        assertNull(Rival.roll(Place.PORT, kind -> true, random), "jamais une rencontre déjà gagnée");
    }

    @Test
    void heTakesTheSoldiersPlaceAndComesBackIfTheDungeonRestarts() {
        DungeonRun run = new DungeonRun(Dungeon.CALE, 0, EnemyKind.RIVAL_PORT);
        assertEquals(EnemyKind.RIVAL_PORT, run.getEnemy());
        assertEquals(CombatRun.Next.BOSS, run.win());
        assertEquals(Dungeon.CALE.getKing(), run.getEnemy(), "le roi suit, comme après le soldat");
        run.restart();
        assertEquals(EnemyKind.RIVAL_PORT, run.getEnemy());

        assertEquals(Dungeon.CALE.getSoldier(), new DungeonRun(Dungeon.CALE).getEnemy());
        assertThrows(IllegalArgumentException.class, () -> new DungeonRun(Dungeon.CALE, 0, EnemyKind.PIRATE));
    }

    @Test
    void heIsAtLeastAsToughAsTheKingsOfThePlaceAndGrowsAtEachEncounter() {
        int symbols = 0, cards = 0;
        for (Place place : Place.values()) {
            EnemyKind rival = Rival.of(place);
            if (rival == null) continue;
            for (Dungeon dungeon : place.getDungeons()) {
                EnemyKind king = dungeon.getKing();
                assertTrue(rival.getMaxHp(Difficulty.NORMAL) >= king.getMaxHp(Difficulty.NORMAL), king.name());
                assertTrue(rival.getBaseDefense(Difficulty.NORMAL) >= king.getBaseDefense(Difficulty.NORMAL), king.name());
            }
            int nowSymbols = rival.getSpinWeights(1).size(), nowCards = rival.createDeck().size();
            assertTrue(nowSymbols > symbols, "sa machine grandit : " + rival);
            assertTrue(nowCards > cards, "son deck grandit : " + rival);
            assertEquals(Rival.encounter(rival), rival.getSignatures().size(), "une carte propre de plus par lieu");
            symbols = nowSymbols;
            cards = nowCards;
            assertFalse(rival.isBoss(), "il prend la place du soldat");
        }
    }
}
