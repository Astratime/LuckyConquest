package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolRegistry;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.effects.AnchorEffect;
import fr.astratime.lucky.entities.effects.ExtraDrawEffect;
import fr.astratime.lucky.entities.effects.ForgedBladeEffect;
import fr.astratime.lucky.entities.effects.MutinyEffect;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
import fr.astratime.lucky.entities.events.CardStrikeEvent;
import fr.astratime.lucky.entities.events.EnemySelfHitEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.entities.exploration.PlaceRule;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Cartes des coffres des donjons du Port, des Mines et du Casino. */
class PlaceChestCardsTest {

    @Test
    void eachDungeonOfTheLaterPlacesHasItsOwnChest() {
        Set<String> seen = new HashSet<>();
        for (Place place : List.of(Place.PORT, Place.MINES, Place.CASINO)) {
            for (Dungeon dungeon : place.getDungeons()) {
                assertEquals(3, dungeon.getLoot().size());
                for (Dungeon.Loot loot : dungeon.getLoot()) {
                    assertTrue(seen.add(loot.cardId()), loot.cardId() + " est dans deux coffres");
                    assertNotEquals("in_the_sleeve", loot.cardId(), "Dans la manche reste en boutique");
                }
            }
        }
        assertEquals(36, seen.size());
    }

    @Test
    void theRopeKeepsThePlayerAtOneHpOncePerFight() {
        Player player = new Player("Joueur", 100, List.of());
        assertTrue(player.addRope());
        player.takeDamage(500);
        assertEquals(1, player.getHp());
        assertTrue(player.takeRopeSaved());
        assertFalse(player.addRope(), "une fois par combat");
        player.takeDamage(500);
        assertTrue(player.isDefeated());
    }

    @Test
    void theRatTrapCancelsOneSpellOnTheHand() {
        Player player = new Player("Joueur", 100, List.of());
        player.getLastingEffects().addTrap();
        assertNotNull(EnemyTurnResolver.parry(EnemySymbol.NIBBLE, player));
        assertNull(EnemyTurnResolver.parry(EnemySymbol.BLIND, player), "un seul piège");
        assertNull(EnemyTurnResolver.parry(EnemySymbol.SWORD, player));
    }

    @Test
    void theSharkCageProtectsGainsWhileItLasts() {
        Player player = new Player("Joueur", 100, List.of());
        player.addGains(1000);
        LastingEffects lasting = player.getLastingEffects();
        lasting.addCage(1);
        assertNotNull(EnemyTurnResolver.parry(EnemySymbol.BANK_BITE, player));
        lasting.endTurn();
        assertNull(EnemyTurnResolver.parry(EnemySymbol.BANK_BITE, player), "la cage s'est ouverte");
    }

    @Test
    void temperGrowsEveryTurn() {
        LastingEffects lasting = new LastingEffects();
        lasting.addTemper(5);
        assertEquals(5, lasting.getTemperPercent());
        lasting.endTurn();
        lasting.endTurn();
        assertEquals(15, lasting.getTemperPercent());
    }

    @Test
    void theAnchorStopsTheHighTide() {
        Player player = new Player("Joueur", 100, List.of());
        player.getLastingEffects().addAnchor(3);
        int highTide = PlaceRule.HIGH_TIDE;
        CombatContext context = new PreparationResolver()
            .resolve(List.of(), player, new Enemy("Ennemi", 1000), PlaceRule.MAREE, highTide).getCombatContext();
        assertEquals(1f, context.getAttackFactor(), 1e-6);
        assertTrue(new AnchorEffect(3, 30).getDescription().contains("marée"));
    }

    @Test
    void dynamiteIgnoresTheGoldSkin() {
        Enemy golem = new Enemy(EnemyKind.GOLEM_OR);
        assertTrue(golem.hasGoldSkin());
        CombatContext context = new CombatContext(new Player("Joueur", 100, List.of()), golem);
        context.addDynamite(3);
        int before = golem.getHp();
        new CombatResolver().resolve(context, List.of(), new Symbol[] {null, null, null}, List.of());
        assertEquals(Math.round(golem.getMaxHp() * 0.03f), before - golem.getHp());
    }

    @Test
    void theForgedBladeStrikesAtHalfTheBiggestAttack() {
        Player player = new Player("Joueur", 100, List.of());
        Enemy enemy = new Enemy("Ennemi", 100_000);
        CombatContext context = new CombatContext(player, enemy);
        context.setIgnoreDefense(true);
        context.addForgedBlade();
        List<SymbolAction> actions = List.of(
            new SymbolAction(Symbol.CHERRY, 0, SymbolRegistry.getAction(Symbol.CHERRY).orElseThrow()));
        TurnResult result = new CombatResolver().resolve(context, actions, new Symbol[] {Symbol.CHERRY, null, null},
            List.of());
        CardStrikeEvent blade = result.getPistolEvents().stream().filter(e -> e instanceof CardStrikeEvent)
            .map(e -> (CardStrikeEvent) e).findFirst().orElseThrow();
        int attack = result.getSymbolOutcomes().get(0).getEvents().stream()
            .filter(e -> e instanceof fr.astratime.lucky.entities.events.EnemyDamagedEvent)
            .mapToInt(e -> ((fr.astratime.lucky.entities.events.EnemyDamagedEvent) e).rawDamage).max().orElseThrow();
        assertEquals(Math.round(attack * ForgedBladeEffect.PERCENT / 100f), blade.rawDamage);
    }

    @Test
    void theSunkenJackpotMultipliesGainsOnABingo() {
        CombatContext context = new CombatContext(new Player("Joueur", 100, List.of()), new Enemy("Ennemi", 1000));
        context.addSunkenJackpot();
        new CombatResolver().resolve(context, List.of(), new Symbol[] {Symbol.BELL, Symbol.BELL, Symbol.BELL}, List.of());
        assertEquals(10f, context.getGainFactor(), 1e-6);
        assertEquals(3f, context.getAttackFactor(), 1e-6);
    }

    @Test
    void theHarpoonCutsAnArmOfTheKraken() {
        Enemy kraken = new Enemy(EnemyKind.KRAKEN);
        int arms = kraken.getPlaysPerTurn();
        kraken.harpoon();
        assertEquals(arms - 1, kraken.getPlaysPerTurn());
        Enemy other = new Enemy("Ennemi", 1000);
        other.harpoon();
        assertEquals(1, other.takeHarpoons());
    }

    @Test
    void aMutinyTurnsTheEnemyCardsAgainstHim() {
        Enemy enemy = new Enemy("Ennemi", 100_000);
        enemy.addMutiny();
        EnemyTurnResult turn = new EnemyTurnResolver(new Random(1))
            .resolve(enemy, new Player("Joueur", 100, List.of()), 0, 0f);
        assertTrue(turn.played().isEmpty(), "il ne joue pas ses cartes");
        EnemySelfHitEvent hit = turn.openingEvents().stream().filter(e -> e instanceof EnemySelfHitEvent)
            .map(e -> (EnemySelfHitEvent) e).findFirst().orElseThrow();
        assertEquals(enemy.getKind().getPlaysPerTurn() * MutinyEffect.PERCENT_PER_CARD * 1000, hit.damage);
    }

    @Test
    void aDazzledEnemySkipsHisTurn() {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 8; i++) deck.add(new Card("c" + i, "c" + i, "x.png", List.of(new ExtraDrawEffect(0)), null, 1));
        GameController controller = new GameController(() -> new ArrayList<>(deck), id -> null);
        controller.getGameState().getEnemy().dazzle();
        controller.drawCards();
        TurnResult result = controller.spin();
        if (!controller.getGameState().getEnemy().isDefeated()) assertNull(result.getEnemyTurn());
        List<Event> end = result.getEnemyTurnEvents();
        assertTrue(end.stream().anyMatch(e -> e.describe().contains("ÉBLOUI")));
    }
}
