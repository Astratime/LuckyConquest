package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.exploration.PlaceRule;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Règles des lieux de l'Exploration (Scorbut, grisou, marée) et ennemis qui en dépendent. */
class PlacesTest {

    private static Card card(String id) {
        return new Card(id, id, "x.png", List.of(), null, 0);
    }

    /** @return un joueur qui a {@code hand} en main. */
    private static Player playerHolding(Card... hand) {
        Player player = new Player("Joueur", 100, List.of(hand));
        player.draw(hand.length);
        return player;
    }

    @Test
    void scurvyComesBackEveryThreeTurnsStartingWithTheFirst() {
        assertTrue(PlaceRule.SCORBUT.scurvyArrives(1));
        assertFalse(PlaceRule.SCORBUT.scurvyArrives(2));
        assertFalse(PlaceRule.SCORBUT.scurvyArrives(3));
        assertTrue(PlaceRule.SCORBUT.scurvyArrives(4));
        assertTrue(PlaceRule.SCORBUT.scurvyArrives(7));
        assertFalse(PlaceRule.NONE.scurvyArrives(1));
    }

    @Test
    void anUnplayedScurvyCardHalvesAttackDefenseAndGains() {
        Player sick = playerHolding(card(PlaceRule.SCURVY_CARD), card("autre"));
        CombatContext halved = new PreparationResolver()
            .resolve(List.of(), sick, new Enemy("Ennemi", 1000), PlaceRule.SCORBUT, 1).getCombatContext();
        assertEquals(0.5f, halved.getAttackFactor(), 1e-6);
        assertEquals(0.5f, halved.getDefenseFactor(), 1e-6);
        assertEquals(0.5f, halved.getGainFactor(), 1e-6);

        Player healthy = playerHolding(card("autre"));
        CombatContext normal = new PreparationResolver()
            .resolve(List.of(), healthy, new Enemy("Ennemi", 1000), PlaceRule.SCORBUT, 1).getCombatContext();
        assertEquals(1f, normal.getAttackFactor(), 1e-6, "Scorbut joué : plus de malus");
    }

    @Test
    void firedampExplodesEveryFifthTurn() {
        assertFalse(PlaceRule.GRISOU.firedampExplodes(4));
        assertTrue(PlaceRule.GRISOU.firedampExplodes(5));
        assertTrue(PlaceRule.GRISOU.firedampExplodes(10));
        assertFalse(PlaceRule.NONE.firedampExplodes(5));
        assertEquals(3, PlaceRule.turnsBeforeFiredamp(3), "tours 3, 4 et 5");
        assertEquals(1, PlaceRule.turnsBeforeFiredamp(5), "à la fin de ce tour");
    }

    @Test
    void highTideCutsTheAttackFromThreeToFiveThenTheSeaGoesBackDown() {
        for (int turn = 1; turn <= 12; turn++) {
            boolean high = PlaceRule.MAREE.isHighTide(turn);
            assertEquals(turn % 6 >= 3, high, "tour " + turn);
        }
        CombatContext context = new PreparationResolver()
            .resolve(List.of(), playerHolding(card("autre")), new Enemy("Ennemi", 1000), PlaceRule.MAREE, 3)
            .getCombatContext();
        assertEquals(0.7f, context.getAttackFactor(), 1e-6);
    }

    @Test
    void goldSkinHalvesDamageWhileTheGolemIsAboveHalfItsHp() {
        Enemy golem = new Enemy(EnemyKind.GOLEM_OR);
        assertEquals(500, golem.takeDamage(1_000));
        Enemy weakened = new Enemy(EnemyKind.GOLEM_OR);
        weakened.takeDamage(weakened.getMaxHp() + 2); // 2 x (PV max / 2 + 1) : juste sous la moitié
        assertEquals(1_000, weakened.takeDamage(1_000), "sous la moitié, plus de peau d'or");
    }

    @Test
    void theKrakenPlaysOneCardPerArmLeft() {
        Enemy kraken = new Enemy(EnemyKind.KRAKEN);
        assertEquals(EnemyKind.KRAKEN_ARMS, kraken.getPlaysPerTurn());
        kraken.takeDamage(kraken.getMaxHp() / 2);
        assertEquals(EnemyKind.KRAKEN_ARMS / 2, kraken.getPlaysPerTurn());
        kraken.takeDamage(kraken.getMaxHp());
        assertEquals(1, kraken.getPlaysPerTurn(), "toujours au moins un bras");
        assertEquals(5, new Enemy(EnemyKind.PIEUVRE).getPlaysPerTurn());
    }
}
