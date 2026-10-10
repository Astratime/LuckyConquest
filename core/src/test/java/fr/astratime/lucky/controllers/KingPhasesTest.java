package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
import fr.astratime.lucky.entities.events.EnemyPhaseEvent;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.Place;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** Les rois de l'Exploration entrent en phase 2 sous la moitié de leurs PV : un symbole de plus. */
class KingPhasesTest {

    @Test
    void everyKingGainsANewSymbolBelowHalfLifeAndKeepsTheOthers() {
        for (Place place : Place.values()) {
            for (Dungeon dungeon : place.getDungeons()) {
                EnemyKind king = dungeon.getKing();
                EnemySymbol added = king.kingPhaseSymbol();
                assertNotNull(added, king.name());
                assertFalse(king.getWeights(1).containsKey(added), king.name() + " : un symbole nouveau pour lui");
                Map<EnemySymbol, Integer> phaseTwo = king.getWeights(2);
                king.getWeights(1).forEach((symbol, weight) ->
                    assertEquals(weight, phaseTwo.get(symbol), king.name() + " garde " + symbol));
                assertEquals(EnemyKind.KING_PHASE_WEIGHT, phaseTwo.get(added));
                assertNotEquals(EnemyKind.ECLAT.phaseText(2), king.phaseText(2), "chaque roi a sa bannière");
                assertNull(dungeon.getSoldier().kingPhaseSymbol(), "les soldats n'ont pas de phase");
            }
        }
    }

    @Test
    void aKingAnnouncesItsPhaseTwoAtTheStartOfItsTurn() {
        Enemy king = new Enemy(EnemyKind.ROI_PIQUE);
        assertFalse(king.enterPhaseTwo(), "en pleine forme : phase 1");
        king.takeDamage(king.getMaxHp() / 2 + 1);
        EnemyTurnResult turn = new EnemyTurnResolver(new Random(1))
            .resolve(king, new Player("Joueur", 1_000_000, List.of()), 0, 0f);

        assertInstanceOf(EnemyPhaseEvent.class, turn.openingEvents().get(0));
        assertEquals(2, king.getPhase());
        assertTrue(king.getSymbols().contains(EnemySymbol.FANG));
        for (EnemySymbol symbol : turn.symbols()) assertTrue(king.getSymbols().contains(symbol), symbol.name());
        assertFalse(new Enemy(EnemyKind.SOLDAT_PIQUE).enterPhaseTwo());
    }
}
