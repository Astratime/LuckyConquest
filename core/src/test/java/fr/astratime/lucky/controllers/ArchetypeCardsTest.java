package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.EnemyMaluses;
import fr.astratime.lucky.entities.Archetype;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Combo;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.enemy.EnemyCards;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.loaders.CardLoader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;

/** Les 25 cartes des archétypes de deck (Joker, Lutin, Rentier, Bluffeur, Relanceur). */
class ArchetypeCardsTest {

    private static final Path ASSETS = Path.of("..", "assets");
    private static final CardLoader.AssetReader READER = path -> {
        try {
            return Files.readString(ASSETS.resolve(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    };

    private static List<Card> archetypeCards() {
        return CardLoader.loadAll(READER).stream().filter(card -> card.getArchetype() != null).toList();
    }

    private static Card card(String id) {
        return CardLoader.loadAll(READER).stream().filter(c -> c.getId().equals(id)).findFirst().orElseThrow();
    }

    @Test
    void eachArchetypeHasFiveCardsAndThoseOfTheChestsOneMore() {
        Map<Archetype, Long> counts = archetypeCards().stream()
            .collect(Collectors.groupingBy(Card::getArchetype, Collectors.counting()));
        for (Archetype archetype : Archetype.values()) {
            assertEquals(archetype == Archetype.BLUFFEUR ? 5L : 6L, counts.get(archetype), archetype.name());
        }
    }

    @Test
    void theOtherArchetypeCardsAreTheTreasuresOfTheNewPlaces() {
        Set<String> loot = new HashSet<>();
        for (fr.astratime.lucky.entities.exploration.Dungeon dungeon : fr.astratime.lucky.entities.exploration.Dungeon.values()) {
            dungeon.getLoot().forEach(entry -> loot.add(entry.cardId()));
        }
        for (Card card : archetypeCards()) {
            assertEquals(card.getArchetype() != Archetype.BLUFFEUR, loot.contains(card.getId()), card.getId());
        }
    }

    @Test
    void onlyTheBluffCardsAreSoldInTheShop() {
        Map<String, Long> shop = CardLoader.loadBoutique(READER);
        for (Card card : archetypeCards()) {
            assertEquals(card.getArchetype() == Archetype.BLUFFEUR, shop.containsKey(card.getId()), card.getId());
        }
    }

    @Test
    void claimsAreCappedAndPaidOnTheEnemyTurn() {
        Enemy enemy = new Enemy(EnemyKind.ENTRAINEMENT, List.of());
        assertEquals(EnemyMaluses.MAX_CLAIMS, enemy.getMaluses().addClaims(50), "pas plus de 20 Créances");
        assertEquals(0, enemy.getMaluses().addClaims(1));

        Player player = new Player("Joueur", 1_000_000, List.of());
        CombatContext combat = new CombatContext(player, enemy);
        long hp = enemy.getHp();
        double gains = player.getGains();
        TurnEngine.chargeClaims(enemy, player, combat);
        assertTrue(enemy.getHp() < hp, "les Créances blessent l'ennemi");
        assertEquals(gains + EnemyMaluses.MAX_CLAIMS * EnemyMaluses.CLAIM_SPINS * combat.getSpinCost(), player.getGains(), 1e-6);
        assertEquals(EnemyMaluses.MAX_CLAIMS, enemy.getMaluses().getClaims(), "elles restent après avoir payé");
        assertEquals(EnemyMaluses.MAX_CLAIMS, enemy.getMaluses().takeClaims());
        assertEquals(0, enemy.getMaluses().getClaims(), "Recouvrement : tout est encaissé");
    }

    @Test
    void compoundInterestGrowsTheClaimsForAFewSpins() {
        Enemy enemy = new Enemy(EnemyKind.ENTRAINEMENT, List.of());
        enemy.getMaluses().addClaims(4);
        enemy.getMaluses().compound(2, 25);
        assertEquals(1, enemy.getMaluses().growClaims());
        assertEquals(2, enemy.getMaluses().growClaims(), "25 % de 5, arrondi au-dessus");
        assertEquals(0, enemy.getMaluses().growClaims(), "les Intérêts composés sont finis");
        assertEquals(7, enemy.getMaluses().getClaims());
    }

    @Test
    void bossesAndKingsSufferHalfTheBluffMaluses() {
        Enemy normal = new Enemy(EnemyKind.CROUPIER, List.of());
        Enemy king = new Enemy(EnemyKind.ROI_PIQUE, List.of());
        assertTrue(king.getKind().isBoss());
        normal.getMaluses().intimidate(30);
        king.getMaluses().intimidate(30);
        assertEquals(30, normal.getMaluses().takeIntimidation());
        assertEquals(15, king.getMaluses().takeIntimidation(), "un roi n'en subit que la moitié");
        assertEquals(0, king.getMaluses().takeIntimidation(), "l'Intimidation vaut pour un seul tour");
    }

    @Test
    void bluffMalusesAreCountedAndSpentOneByOne() {
        Enemy enemy = new Enemy(EnemyKind.CROUPIER, List.of());
        assertEquals(0, enemy.getMaluses().getActiveMaluses());
        enemy.getMaluses().markCards(2);
        enemy.getMaluses().addRedHerring();
        enemy.getMaluses().addSleight();
        enemy.getMaluses().addClaims(1);
        assertEquals(4, enemy.getMaluses().getActiveMaluses());
        assertTrue(enemy.getMaluses().takeMarkedCards());
        assertTrue(enemy.getMaluses().takeMarkedCards());
        assertFalse(enemy.getMaluses().takeMarkedCards(), "Cartes marquées : 2 tours");
        assertTrue(enemy.getMaluses().takeRedHerring());
        assertFalse(enemy.getMaluses().takeRedHerring());
        assertTrue(enemy.getMaluses().takeSleight());
        assertFalse(enemy.getMaluses().takeSleight());
        assertEquals(1, enemy.getMaluses().getActiveMaluses());
    }

    @Test
    void theChameleonJokerCompletesTheBestCombo() {
        Card wild = card("joker_cameleon");
        assertTrue(wild.isWild());
        List<Combo> combos = Combo.formed(List.of(EnemyCards.card(Card.Suit.PIQUE, 7), EnemyCards.card(Card.Suit.COEUR, 7), wild));
        assertTrue(combos.contains(Combo.BRELAN), combos.toString());
        assertTrue(Set.copyOf(Combo.formed(List.of(wild))).isEmpty(), "seul, il ne forme rien");
    }

    @Test
    void theMartingaleGrowsOnMissedPairsAndResetsOnAPair() {
        LastingEffects lasting = new LastingEffects();
        for (int i = 0; i < 5; i++) lasting.updateMartingale(true, false, 20, 30);
        assertEquals(LastingEffects.MAX_MARTINGALE, lasting.getMartingale());
        assertEquals(60, lasting.getMartingaleAttack());
        assertEquals(90, lasting.getMartingalePair());
        lasting.updateMartingale(true, true, 20, 30);
        assertEquals(0, lasting.getMartingale(), "une paire remet la série à zéro");
    }

    @Test
    void theAnnuityPaysForItsTurnsThenStops() {
        LastingEffects lasting = new LastingEffects();
        lasting.addAnnuity(2, 1, 2);
        assertArrayEquals(new int[]{1, 2}, lasting.takeAnnuity());
        assertArrayEquals(new int[]{1, 2}, lasting.takeAnnuity());
        assertNull(lasting.takeAnnuity());
    }

    @Test
    void thePharaohSealDoublesTheClaimsForThreeEnemyTurns() {
        Enemy enemy = new Enemy(EnemyKind.ENTRAINEMENT, List.of());
        Player player = new Player("Joueur", 1_000_000, List.of());
        CombatContext combat = new CombatContext(player, enemy);
        card("sceau_du_pharaon").getEffects().forEach(effect -> effect.apply(new fr.astratime.lucky.entities.context.TurnContext(new fr.astratime.lucky.entities.context.SpinContext(), combat)));
        assertEquals(4, enemy.getMaluses().getClaims());
        assertEquals(3, enemy.getMaluses().getPharaohSealTurns());
        for (int turn = 1; turn <= 4; turn++) {
            double gains = player.getGains();
            TurnEngine.chargeClaims(enemy, player, combat);
            int counted = turn <= 3 ? 8 : 4;
            assertEquals(gains + counted * EnemyMaluses.CLAIM_SPINS * combat.getSpinCost(), player.getGains(), 1e-6, "tour " + turn);
        }
    }

    @Test
    void theDragonHoardPaysEachHitAndGrowsTheAttackUntilItEnds() {
        LastingEffects lasting = new LastingEffects();
        lasting.addHoard(3, 2, 5, 50);
        assertEquals(2, lasting.getHoardSpins());
        assertEquals(15, lasting.growHoard(3));
        assertEquals(50, lasting.growHoard(20), "+50 % au plus");
        lasting.endTurn();
        lasting.endTurn();
        assertEquals(50, lasting.getHoardPercent());
        lasting.endTurn();
        assertEquals(0, lasting.getHoardTurns());
        assertEquals(0, lasting.getHoardPercent(), "le Magot est fini");
        assertEquals(0, lasting.getHoardSpins());
    }

    @Test
    void theJackInTheBoxKeepsItsJokerUntilTheEndOfTheFight() {
        LastingEffects lasting = new LastingEffects();
        assertEquals(20, lasting.addJokerBox(20, 50));
        assertEquals(30, lasting.addJokerBox(40, 50), "+50 au plus");
        for (int i = 0; i < 10; i++) lasting.endTurn();
        assertEquals(50, lasting.getJokerBox(), "il dure tout le combat");
    }

    @Test
    void theNewChestCardsShowTheirNameWhenPlayed() {
        for (String id : List.of("diable_en_boite", "chamboule_tout", "sceau_du_pharaon", "magot_du_dragon")) {
            Card card = card(id);
            assertNotNull(card.getArchetype(), id);
            assertFalse(card.getEffects().isEmpty(), id);
        }
    }
}
