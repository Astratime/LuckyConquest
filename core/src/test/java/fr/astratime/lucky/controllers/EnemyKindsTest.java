package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
import fr.astratime.lucky.entities.events.EnemyDrainEvent;
import fr.astratime.lucky.entities.events.EnemyRageEvent;
import fr.astratime.lucky.entities.events.EnemyThornsEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.PlayerDamagedEvent;
import fr.astratime.lucky.entities.events.ThornsEvent;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** Les ennemis de la Tour des épreuves : leurs rouleaux, leur deck, et leurs nouveaux symboles. */
class EnemyKindsTest {

    @Test
    void theChallengersAndTheBossHaveTheRequestedLife() {
        for (EnemyKind kind : EnemyKind.CHALLENGERS) assertEquals(10_000, new Enemy(kind).getMaxHp(), kind.name());
        assertEquals(20_000, new Enemy(EnemyKind.COMETE).getMaxHp());
        assertEquals(5_000, new Enemy(EnemyKind.CROUPIER).getMaxHp(), "le 1er combat reste celui du jeu");
    }

    @Test
    void eachEnemyOnlyDrawsItsOwnSymbols() {
        for (EnemyKind kind : EnemyKind.values()) {
            for (int seed = 0; seed < 20; seed++) {
                Enemy enemy = new Enemy(kind);
                EnemyTurnResult turn = new EnemyTurnResolver(new Random(seed))
                    .resolve(enemy, new Player("Joueur", 1_000_000, List.of()), 0, 0f);
                for (EnemySymbol symbol : turn.symbols()) {
                    assertTrue(kind.getSymbols().contains(symbol), kind + " ne tire pas " + symbol);
                }
                assertEquals(kind.getPlaysPerTurn(), turn.played().size());
            }
        }
    }

    @Test
    void theBossUsesTheMechanicsOfEveryChallenger() {
        List<EnemySymbol> boss = EnemyKind.COMETE.getSymbols();
        assertTrue(boss.containsAll(List.of(EnemySymbol.THORNS, EnemySymbol.FANG, EnemySymbol.RAGE, EnemySymbol.SHIELD)));
    }

    @Test
    void thornsReturnPartOfThePlayersDamageAtTheStartOfTheEnemysTurn() {
        Enemy  enemy  = new Enemy(EnemyKind.GARDIEN);
        Player player = new Player("Joueur", 1_000, List.of());
        enemy.addThorns(4);
        enemy.takeDamage(500); // coups du tour du joueur

        EnemyTurnResult turn = new EnemyTurnResolver(new Random(3)).resolve(enemy, player, 0, 0f);

        ThornsEvent thorns = (ThornsEvent) turn.openingEvents().get(0);
        assertEquals(20, thorns.damage, "4 % de 500");
        assertEquals(turn.openingEvents().get(0), turn.events().get(0), "les Épines piquent en premier");
    }

    @Test
    void thornsOnlyCountTheNextPlayerTurnAndTheShieldAbsorbsThem() {
        Enemy  enemy  = new Enemy(EnemyKind.GARDIEN);
        enemy.takeDamage(900); // avant ses Épines : ne compte pas
        enemy.resetDamageTaken();
        enemy.addThorns(10);
        enemy.takeDamage(300);
        Player player = new Player("Joueur", 1_000, List.of());
        player.addShield(10);

        EnemyTurnResult turn = new EnemyTurnResolver(new Random(3)).resolve(enemy, player, 0, 0f);

        ThornsEvent thorns = (ThornsEvent) turn.openingEvents().get(0);
        assertEquals(10, thorns.blocked);
        assertEquals(20, thorns.damage, "10 % de 300, moins 10 de bouclier");
    }

    @Test
    void aThornsSymbolArmsTheThornsForTheNextPlayerTurn() {
        for (int seed = 0; seed < 50; seed++) {
            Enemy enemy = new Enemy(EnemyKind.GARDIEN);
            EnemyTurnResult turn = new EnemyTurnResolver(new Random(seed))
                .resolve(enemy, new Player("Joueur", 1_000_000, List.of()), 0, 0f);
            long thorns = Arrays.stream(turn.symbols()).filter(s -> s == EnemySymbol.THORNS).count();
            assertEquals(thorns * EnemySymbol.THORNS_PERCENT, enemy.getThornsPercent());
            assertEquals(thorns, turn.events().stream().filter(e -> e instanceof EnemyThornsEvent).count());
        }
    }

    @Test
    void aFangBitesAndHealsTheEnemyForEachStolenPoint() {
        int bites = 0;
        for (int seed = 0; seed < 50 && bites == 0; seed++) {
            Enemy enemy = new Enemy(EnemyKind.SANGSUE);
            enemy.takeDamage(9_000);
            Player player = new Player("Joueur", 1_000_000, List.of());

            EnemyTurnResult turn = new EnemyTurnResolver(new Random(seed)).resolve(enemy, player, 0, 0f);

            for (int i = 0; i < 3; i++) {
                if (turn.symbols()[i] != EnemySymbol.FANG) continue;
                bites++;
                List<Event> events = turn.outcomes().get(i);
                int stolen = ((PlayerDamagedEvent) events.get(0)).damage;
                assertTrue(stolen >= EnemySymbol.FANG_DAMAGE);
                EnemyDrainEvent drain = (EnemyDrainEvent) events.get(1);
                assertTrue(drain.amount > 0 && drain.amount <= stolen * 100, "1 % de 10 000 PV max par PV volé, au plus");
            }
        }
        assertTrue(bites > 0, "au moins un Croc");
    }

    @Test
    void aBlockedFangHealsNothing() {
        for (int seed = 0; seed < 30; seed++) {
            Enemy enemy = new Enemy(EnemyKind.SANGSUE);
            enemy.takeDamage(5_000);
            Player player = new Player("Joueur", 100, List.of());
            player.addShield(10_000);

            EnemyTurnResult turn = new EnemyTurnResolver(new Random(seed)).resolve(enemy, player, 0, 0f);

            assertTrue(turn.events().stream().noneMatch(e -> e instanceof EnemyDrainEvent), "rien de volé, rien de soigné");
        }
    }

    @Test
    void rageMakesEveryLaterAttackStrongerUpToItsCap() {
        Enemy enemy = new Enemy(EnemyKind.BRETTEUR);
        assertEquals(EnemySymbol.RAGE_ATTACK, enemy.addRage(EnemySymbol.RAGE_ATTACK));
        for (int i = 0; i < 20; i++) enemy.addRage(EnemySymbol.RAGE_ATTACK);
        assertEquals(EnemySymbol.RAGE_MAX, enemy.getRage());
        assertEquals(0, enemy.addRage(EnemySymbol.RAGE_ATTACK));

        // Une Épée frappe avec la Rage en plus (deck sans Pique : aucun bonus de carte).
        List<Card> clubs = List.of(fr.astratime.lucky.entities.enemy.EnemyCards.card(Card.Suit.TREFLE, 2));
        for (int seed = 0; seed < 40; seed++) {
            Enemy bretteur = new Enemy("Bretteur", 1000, clubs);
            bretteur.addRage(9);
            Player player = new Player("Joueur", 1_000_000, List.of());
            EnemyTurnResult turn = new EnemyTurnResolver(new Random(seed)).resolve(bretteur, player, 0, 0f);
            for (int i = 0; i < 3; i++) {
                if (turn.symbols()[i] == EnemySymbol.SWORD) {
                    assertEquals(EnemySymbol.SWORD_DAMAGE + 9, ((PlayerDamagedEvent) turn.outcomes().get(i).get(0)).damage);
                }
            }
        }
    }

    @Test
    void aRageSymbolIsAnnounced() {
        for (int seed = 0; seed < 30; seed++) {
            Enemy enemy = new Enemy(EnemyKind.BRETTEUR);
            EnemyTurnResult turn = new EnemyTurnResolver(new Random(seed))
                .resolve(enemy, new Player("Joueur", 1_000_000, List.of()), 0, 0f);
            long rages = Arrays.stream(turn.symbols()).filter(s -> s == EnemySymbol.RAGE).count();
            assertEquals(rages * EnemySymbol.RAGE_ATTACK, enemy.getRage());
            assertEquals(rages, turn.events().stream().filter(e -> e instanceof EnemyRageEvent).count());
        }
    }

    @Test
    void eachEnemyPlaysItsFavouriteSuitFirst() {
        List<Card> hand = List.of(EnemyCardsHelper.card(Card.Suit.PIQUE, 14), EnemyCardsHelper.card(Card.Suit.CARREAU, 2),
            EnemyCardsHelper.card(Card.Suit.COEUR, 5), EnemyCardsHelper.card(Card.Suit.TREFLE, 8));
        assertEquals(Card.Suit.CARREAU, EnemyTurnResolver.choose(hand, 1f, EnemyKind.GARDIEN).get(0).getSuit());
        assertEquals(Card.Suit.PIQUE, EnemyTurnResolver.choose(hand, 1f, EnemyKind.BRETTEUR).get(0).getSuit());
        assertEquals(Card.Suit.PIQUE, EnemyTurnResolver.choose(hand, 0.1f, EnemyKind.BRETTEUR).get(0).getSuit(),
            "le Bretteur attaque même blessé");
        assertEquals(Card.Suit.COEUR, EnemyTurnResolver.choose(hand, 0.1f, EnemyKind.SANGSUE).get(0).getSuit());
    }

    @Test
    void theCroupierDeckIsTheStarterDeck() {
        assertEquals(fr.astratime.lucky.entities.enemy.EnemyCards.starterDeck().stream().map(Card::getId).sorted().toList(),
            EnemyKind.CROUPIER.createDeck().stream().map(Card::getId).sorted().toList());
        for (EnemyKind kind : EnemyKind.values()) assertTrue(kind.createDeck().size() >= Enemy.HAND_SIZE, kind.name());
    }

    private static final class EnemyCardsHelper {
        static Card card(Card.Suit suit, int rank) { return fr.astratime.lucky.entities.enemy.EnemyCards.card(suit, rank); }
    }
}
