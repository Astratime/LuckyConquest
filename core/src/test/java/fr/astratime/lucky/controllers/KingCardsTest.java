package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.EnemyMaluses;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.loaders.CardLoader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Cartes des rois (proposition C3) : une carte par roi d'Exploration, à la Boutique après 10 victoires sur lui. */
class KingCardsTest {

    private static final Path ASSETS = Path.of("..", "assets");
    private static final CardLoader.AssetReader READER = path -> {
        try {
            return Files.readString(ASSETS.resolve(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    };

    /** Prix choisis par Astra (2026-11-14) : un prix par lieu. */
    private static long placePrice(Place place) {
        return switch (place) {
            case PRAIRIE -> 75_000_000L;
            case FOIRE   -> 200_000_000L;
            case PORT    -> 1_300_000_000L;
            case MINES   -> 2_500_000_000L;
            case CASINO  -> 7_000_000_000L;
            case DESERT  -> 33_000_000_000L;
        };
    }

    @Test
    void everyKingHasOneCardSoldAtThePriceOfItsPlace() {
        Map<String, EnemyKind> kings = CardLoader.loadKingCards(READER);
        Map<String, Long> prices = CardLoader.loadBoutique(READER);
        Set<EnemyKind> expected = new HashSet<>();
        for (Place place : Place.values()) {
            for (Dungeon dungeon : place.getDungeons()) expected.add(dungeon.getKing());
        }
        assertEquals(24, expected.size());
        assertEquals(expected, new HashSet<>(kings.values()), "un roi, une carte");
        assertEquals(kings.size(), new HashSet<>(kings.values()).size());
        for (Map.Entry<String, EnemyKind> entry : kings.entrySet()) {
            Place place = Place.of(entry.getValue());
            assertEquals(placePrice(place), prices.get(entry.getKey()), entry.getKey());
        }
    }

    @Test
    void kingCardsLoadAsSpecialCardsWithAnUpgradedVersion() {
        var factory = CardLoader.cardFactory(READER);
        for (String id : CardLoader.loadKingCards(READER).keySet()) {
            Card card = factory.apply(id);
            assertNull(card.getSuit(), id + " : pas de couleur");
            assertFalse(card.getEffects().isEmpty(), id);
            assertFalse(card.getDescription().isBlank(), id);
            assertNotNull(factory.apply(Card.upgradedId(id)), id + " +");
        }
    }

    @Test
    void enemyMalusesOfTheKingCardsWearOff() {
        EnemyMaluses maluses = new Enemy(EnemyKind.ENTRAINEMENT, List.of()).getMaluses();
        maluses.shiftNextAttack(-50);
        assertEquals(50, maluses.takeNextAttackPercent(), "Carte biseautée");
        assertEquals(100, maluses.takeNextAttackPercent(), "un seul tour");
        maluses.shiftNextAttack(15);
        assertEquals(115, maluses.takeNextAttackPercent(), "Provocation");

        maluses.playFewer(1, 2);
        assertEquals(1, maluses.takeFewerPlays());
        assertEquals(1, maluses.takeFewerPlays(), "Momification : deux tours");
        assertEquals(0, maluses.takeFewerPlays());

        maluses.ember(3, 100);
        assertEquals(100, maluses.takeEmbers());
        assertEquals(100, maluses.takeEmbers());
        assertEquals(100, maluses.takeEmbers());
        assertEquals(0, maluses.takeEmbers(), "Braise : trois tours");

        maluses.ink(1);
        assertTrue(maluses.takeInk());
        assertFalse(maluses.takeInk());
    }

    @Test
    void tiedUpAndGrappledEnemiesPlayFewerCards() {
        Enemy enemy = new Enemy("Ennemi", 100_000);
        int plays = enemy.getPlaysPerTurn();
        enemy.getMaluses().playFewer(1, 1);
        enemy.getMaluses().grapple();
        EnemyTurnResult turn = new EnemyTurnResolver(new Random(1)).resolve(enemy, new Player("Joueur", 100, List.of()));
        assertEquals(Math.max(0, plays - 2), turn.played().size(), "Tournée du patron + Grappin");
        EnemyTurnResult next = new EnemyTurnResolver(new Random(1)).resolve(enemy, new Player("Joueur", 100, List.of()));
        assertEquals(plays, next.played().size());
    }

    @Test
    void theLighthouseShowsTheEnemyNextHand() {
        Enemy enemy = new Enemy("Ennemi", 100_000);
        enemy.getMaluses().light(1);
        new EnemyTurnResolver(new Random(1)).resolve(enemy, new Player("Joueur", 100, List.of()));
        assertFalse(enemy.getRevealedHand().isEmpty(), "sa prochaine main est sur la table");
        new EnemyTurnResolver(new Random(1)).resolve(enemy, new Player("Joueur", 100, List.of()));
        assertTrue(enemy.getRevealedHand().isEmpty(), "le phare s'est éteint");
    }
}
