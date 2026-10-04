package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.enemy.EnemyCards;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
import fr.astratime.lucky.entities.events.EnemyHealedEvent;
import fr.astratime.lucky.entities.events.EnemyHourglassEvent;
import fr.astratime.lucky.entities.events.EnemyMirrorEvent;
import fr.astratime.lucky.entities.events.EnemyPhaseEvent;
import fr.astratime.lucky.entities.events.EnemyRouletteEvent;
import fr.astratime.lucky.entities.events.EnemyShieldedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GainsStolenEvent;
import fr.astratime.lucky.entities.events.GaugesDrainedEvent;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/** Les ennemis des chapitres 2 et 3 de la Tour des épreuves et leurs nouveaux symboles. */
class TowerChaptersTest {

    /** Un tour de {@code enemy} dont les rouleaux montrent {@code symbol} (en cherchant la graine). */
    private static EnemyTurnResult turnWith(EnemySymbol symbol, Supplier<Enemy> enemy, Supplier<Player> player) {
        for (int seed = 0; seed < 500; seed++) {
            EnemyTurnResult turn = new EnemyTurnResolver(new java.util.Random(seed)).resolve(enemy.get(), player.get(), 0, 0f);
            if (Arrays.asList(turn.symbols()).contains(symbol)) return turn;
        }
        throw new AssertionError("aucun tirage avec " + symbol);
    }

    private static long count(EnemyTurnResult turn, EnemySymbol symbol) {
        return Arrays.stream(turn.symbols()).filter(s -> s == symbol).count();
    }

    private static <T extends Event> List<T> events(EnemyTurnResult turn, Class<T> type) {
        return turn.events().stream().filter(type::isInstance).map(type::cast).toList();
    }

    @Test
    void aLoadedDieEmptiesPartOfThePlayerGauges() {
        Player[] holder = new Player[1];
        EnemyTurnResult turn = turnWith(EnemySymbol.LOADED_DIE, () -> new Enemy(EnemyKind.TRICHEUR), () -> {
            holder[0] = new Player("Joueur", 1_000_000, List.of());
            holder[0].getLastingEffects().addVault(100);
            holder[0].getLastingEffects().addBlood(40);
            return holder[0];
        });
        long dice = count(turn, EnemySymbol.LOADED_DIE);
        int expectedVault = 100, expectedBlood = 40;
        for (int i = 0; i < dice; i++) {
            expectedVault -= (int) Math.ceil(expectedVault * EnemyKind.TRICHEUR.diePercent() / 100.0);
            expectedBlood -= (int) Math.ceil(expectedBlood * EnemyKind.TRICHEUR.diePercent() / 100.0);
        }
        assertEquals(expectedVault, holder[0].getLastingEffects().getVault());
        assertEquals(expectedBlood, holder[0].getLastingEffects().getBlood());
        assertEquals(dice, events(turn, GaugesDrainedEvent.class).size());
    }

    @Test
    void interestTakesGainsAndArmsTheNextBlow() {
        Enemy enemy = new Enemy(EnemyKind.USURIER);
        Player player = new Player("Joueur", 1_000_000, List.of());
        player.addGains(10_000);
        assertEquals(EnemySymbol.INTEREST_MAX, enemy.addInterest(1_000_000), "plafonnée");
        assertEquals(EnemySymbol.INTEREST_MAX, enemy.spendInterest());
        assertEquals(0, enemy.getInterest(), "dépensée au premier coup");

        Player[] holder = new Player[1];
        EnemyTurnResult turn = turnWith(EnemySymbol.INTEREST, () -> new Enemy(EnemyKind.USURIER), () -> {
            holder[0] = new Player("Joueur", 1_000_000, List.of());
            holder[0].addGains(10_000);
            return holder[0];
        });
        GainsStolenEvent first = events(turn, GainsStolenEvent.class).get(0);
        int percent = EnemyKind.USURIER.interestPercent();
        assertTrue(percent > EnemySymbol.INTEREST_PERCENT, "renforcés au chapitre 2");
        assertEquals(10_000 * percent / 100, first.amount);
        assertEquals(first.amount / EnemySymbol.INTEREST_PER_ATTACK, first.attack);
        assertTrue(holder[0].getGains() < 10_000);
    }

    @Test
    void theZeroSpinsOneRoulettePerSymbol() {
        for (int seed = 0; seed < 60; seed++) {
            EnemyTurnResult turn = new EnemyTurnResolver(new java.util.Random(seed))
                .resolve(new Enemy(EnemyKind.ROULETTE), new Player("Joueur", 1_000_000, List.of()), 0, 0f);
            List<EnemyRouletteEvent> roulettes = events(turn, EnemyRouletteEvent.class);
            assertEquals(count(turn, EnemySymbol.ZERO), roulettes.size());
            if (roulettes.size() == 1 && roulettes.get(0).pocket == EnemyRouletteEvent.Pocket.NOIR) {
                int bonus = turn.played().stream().filter(c -> c.getSuit() == Card.Suit.CARREAU)
                    .mapToInt(EnemyCards::shieldBonus).sum();
                for (EnemyShieldedEvent shield : events(turn, EnemyShieldedEvent.class)) {
                    assertEquals(EnemyKind.ROULETTE.empowered((EnemySymbol.SHIELD_DEFENSE + bonus) * 2), shield.defense,
                        "noir : Boucliers doublés");
                }
            }
        }
    }

    @Test
    void theMirrorReplaysTheLastCardThePlayerPlayed() {
        Supplier<Player> playerWithADiamond = () -> {
            Player player = new Player("Joueur", 1_000_000, List.of(EnemyCards.card(Card.Suit.CARREAU, 13)));
            player.draw(1);
            player.playCard(player.getCurrentHand().get(0));
            return player;
        };
        EnemyTurnResult turn = turnWith(EnemySymbol.MIRROR, () -> new Enemy(EnemyKind.MIROIR), playerWithADiamond);
        List<EnemyMirrorEvent> mirrors = events(turn, EnemyMirrorEvent.class);
        assertEquals(count(turn, EnemySymbol.MIRROR), mirrors.size());
        assertEquals("13 de Carreau", mirrors.get(0).cardName);
        int copied = EnemyKind.MIROIR.empowered(
            (EnemySymbol.SHIELD_DEFENSE + EnemyCards.shieldBonus(EnemyCards.card(Card.Suit.CARREAU, 13))) / 2);
        assertTrue(events(turn, EnemyShieldedEvent.class).stream().anyMatch(e -> e.defense == copied),
            "un Carreau copié : un Bouclier à moitié de sa force");
    }

    @Test
    void theHourglassExplodesAtTheEndAndBigHitsPushItBack() {
        Enemy enemy = new Enemy(EnemyKind.HORLOGER);
        for (int i = 1; i < EnemySymbol.HOURGLASS_MAX; i++) assertFalse(enemy.tickHourglass());
        assertEquals(EnemySymbol.HOURGLASS_MAX - 1, enemy.getHourglass());
        enemy.takeDamage(100); // trop petit pour compter
        assertEquals(EnemySymbol.HOURGLASS_MAX - 1, enemy.getHourglass());
        enemy.takeDamage(enemy.getMaxHp() * EnemySymbol.HOURGLASS_HIT_PER_MILLE / 1000);
        assertEquals(EnemySymbol.HOURGLASS_MAX - 2, enemy.getHourglass(), "un gros coup le fait reculer");
        assertFalse(enemy.tickHourglass());
        assertTrue(enemy.tickHourglass(), "au bout, il explose");
        assertEquals(0, enemy.getHourglass());

        EnemyTurnResult turn = turnWith(EnemySymbol.HOURGLASS, () -> new Enemy(EnemyKind.HORLOGER),
            () -> new Player("Joueur", 1_000_000, List.of()));
        assertEquals(count(turn, EnemySymbol.HOURGLASS), events(turn, EnemyHourglassEvent.class).size());
    }

    @Test
    void allInDoublesTheStakeNextTurnUnlessTheEnemyIsHit() {
        Enemy enemy = new Enemy(EnemyKind.FOU);
        enemy.goAllIn();
        assertTrue(enemy.raiseStake());
        assertEquals(2, enemy.getStake());
        enemy.goAllIn();
        enemy.takeDamage(1);
        assertFalse(enemy.raiseStake(), "touché : le Tapis tombe");
        assertEquals(1, enemy.getStake(), "et sa mise retombe");
        for (int i = 0; i < 5; i++) {
            enemy.goAllIn();
            enemy.raiseStake();
        }
        assertEquals(EnemySymbol.ALL_IN_MAX, enemy.getStake());
    }

    @Test
    void theOriginalShardChangesItsReelsBelowHalfLife() {
        Enemy enemy = new Enemy(EnemyKind.ECLAT);
        assertFalse(enemy.enterPhaseTwo());
        enemy.takeDamage(enemy.getMaxHp() / 2 + 1);
        EnemyTurnResult turn = new EnemyTurnResolver(new java.util.Random(1))
            .resolve(enemy, new Player("Joueur", 1_000_000, List.of()), 0, 0f);
        assertInstanceOf(EnemyPhaseEvent.class, turn.openingEvents().get(0));
        assertEquals(2, enemy.getPhase());
        for (EnemySymbol symbol : turn.symbols()) assertTrue(enemy.getSymbols().contains(symbol), symbol.name());
        assertTrue(enemy.getSymbols().contains(EnemySymbol.MIRROR));
        assertFalse(enemy.getSymbols().contains(EnemySymbol.THORNS));
        assertFalse(new Enemy(EnemyKind.REINE).enterPhaseTwo(), "seul l'Éclat a deux phases");
    }

    @Test
    void bigEnemiesHealASmallerShareOfTheirLife() {
        assertEquals(EnemySymbol.POTION_PERCENT, EnemyKind.CROUPIER.potionPercent(0), 0.001f, "le croupier ne change pas");
        assertEquals(EnemySymbol.FANG_DRAIN, EnemyKind.SANGSUE.drainPercent(), 0.001f);
        assertTrue(EnemyKind.REINE.potionPercent(0) < EnemySymbol.POTION_PERCENT);

        int life = EnemyKind.REINE.getMaxHp();
        EnemyTurnResult turn = turnWith(EnemySymbol.POTION, () -> {
            Enemy q = new Enemy(EnemyKind.REINE);
            q.takeDamage(life * 9 / 10);
            return q;
        }, () -> new Player("Joueur", 1_000_000, List.of()));
        float most = EnemyKind.REINE.potionPercent(Enemy.PLAYS_PER_TURN * 20); // tous ses Cœurs au plus fort
        for (EnemyHealedEvent heal : events(turn, EnemyHealedEvent.class)) {
            assertTrue(heal.amount <= Math.round(life * most / 100f),
                "soin d'une Potion : au plus " + most + " % de ses PV max");
        }
    }
}
