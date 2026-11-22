package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.enemy.Difficulty;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySignature;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
import fr.astratime.lucky.entities.tower.Chapter;
import fr.astratime.lucky.entities.tower.EndlessRun;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/** Le chapitre 3 de la Tour, « Les Pèlerins de la Comète » (Tour à 10 chapitres, 2026-11-22). */
class TowerChapter3Test {

    /** Un tour de {@code enemy} dont les rouleaux montrent {@code symbol} (en cherchant la graine). */
    private static EnemyTurnResult turnWith(EnemySymbol symbol, Supplier<Enemy> enemy, Supplier<Player> player) {
        for (int seed = 0; seed < 500; seed++) {
            EnemyTurnResult turn = new EnemyTurnResolver(new Random(seed)).resolve(enemy.get(), player.get());
            if (Arrays.asList(turn.symbols()).contains(symbol)) return turn;
        }
        throw new AssertionError("aucun tirage avec " + symbol);
    }

    private static List<Card> hand(Card.Suit... suits) {
        List<Card> cards = new ArrayList<>();
        int rank = 2;
        for (Card.Suit suit : suits) cards.add(new Card(suit.cardId(rank), "c" + rank, "x.png", List.of(), suit, rank++));
        return cards;
    }

    @Test
    void theChapterSitsBetweenTheSacredTablesAndTheLastDraw() {
        assertEquals(3, Chapter.PELERINS.getNumber());
        assertEquals(Chapter.TABLES_SACREES, Chapter.PELERINS.getPrevious());
        assertEquals(Chapter.PELERINS, Chapter.DERNIER_TIRAGE.getPrevious());
        assertEquals(4, Chapter.DERNIER_TIRAGE.getNumber(), "l'ancien chapitre 3 devient le 4, réglages gardés");
        assertEquals(300, EnemyKind.ECLAT.getPower(Difficulty.NORMAL));
        assertEquals(500, EnemyKind.ECLAT.getSpinCost());
        assertEquals(EnemyKind.PROPHETE, Chapter.PELERINS.getBoss());
        assertEquals(Chapter.PELERINS, Chapter.of(EnemyKind.DISEUSE));
    }

    @Test
    void itsStrengthIsBetweenItsNeighbours() {
        assertEquals(250, EnemyKind.PROPHETE.getPower(Difficulty.NORMAL));
        assertEquals(300, EnemyKind.PASSEUR.getSpinCost());
        assertEquals(1_290, EnemyKind.PROPHETE.getPower(Difficulty.HARD));
        assertEquals(4_500_000L, EnemyKind.PROPHETE.getMaxHp(Difficulty.HARD));
        for (EnemyKind kind : List.of(EnemyKind.PASSEUR, EnemyKind.PELERIN_FER, EnemyKind.PROPHETE)) {
            int power = kind.getPower(Difficulty.NORMAL);
            assertTrue(power > EnemyKind.REINE.getPower(Difficulty.NORMAL) && power < EnemyKind.ECLAT.getPower(Difficulty.NORMAL));
        }
        assertTrue(EnemyKind.PROPHETE.isBoss() && EnemyKind.PROPHETE.isTowerBoss());
        assertEquals(2, EnemyKind.PROPHETE.phaseCount());
        assertTrue(EnemyKind.PROPHETE.getSymbols(2).contains(EnemySymbol.GOLDEN_COMET), "blessé, il appelle la comète");
        assertTrue(EndlessRun.BOSSES.contains(EnemyKind.PROPHETE));
        assertNotEquals(fr.astratime.lucky.i18n.Lang.f("PHASE {0} !", 2), EnemyKind.PROPHETE.phaseText(2), "sa propre annonce");
    }

    @Test
    void eachVowRaisesTheIronPilgrimsBaseDefenseUpToTheMax() {
        Enemy pilgrim = new Enemy(EnemyKind.PELERIN_FER);
        int base = pilgrim.getBaseDefense();
        assertTrue(pilgrim.addVow());
        int step = pilgrim.getBaseDefense() - base;
        assertEquals(EnemyKind.PELERIN_FER.fortified(EnemySymbol.VOW_DEFENSE, Difficulty.NORMAL), step);
        for (int k = 1; k < EnemySymbol.VOW_MAX; k++) assertTrue(pilgrim.addVow());
        assertFalse(pilgrim.addVow(), "plus de Serment au-delà du maximum");
        assertEquals(base + step * EnemySymbol.VOW_MAX, pilgrim.getBaseDefense());
        pilgrim.resetDefense();
        assertEquals(pilgrim.getBaseDefense(), pilgrim.getDefense(), "sa défense se reforme avec ses Serments");
    }

    @Test
    void pilgrimsGatherAndABigHitDrivesHalfAway() {
        Enemy prophet = new Enemy(EnemyKind.PROPHETE);
        for (int k = 0; k < EnemySymbol.PILGRIM_MAX + 2; k++) prophet.addPilgrim();
        assertEquals(EnemySymbol.PILGRIM_MAX, prophet.getPilgrims());
        prophet.takeDamage(1);
        assertEquals(EnemySymbol.PILGRIM_MAX, prophet.getPilgrims(), "un petit coup ne chasse personne");
        prophet.takeDamage(prophet.getMaxHp() * EnemySymbol.PILGRIM_HIT_PERCENT / 100 + prophet.getDefense() + 10);
        assertEquals(EnemySymbol.PILGRIM_MAX / 2, prophet.getPilgrims());
    }

    @Test
    void thePilgrimageAddsAPilgrimAndHits() {
        Enemy[] prophet = new Enemy[1];
        turnWith(EnemySymbol.PILGRIMAGE, () -> prophet[0] = new Enemy(EnemyKind.PROPHETE),
            () -> new Player("Joueur", 1_000_000, List.of()));
        assertTrue(prophet[0].getPilgrims() >= 1);
    }

    @Test
    void foresightLocksTheStrongestCardAndRelicLocksTheMostHeldSuit() {
        Player[] holder = new Player[1];
        turnWith(EnemySymbol.SIGHT, () -> new Enemy(EnemyKind.DISEUSE), () -> {
            holder[0] = new Player("Joueur", 1_000_000, hand(Card.Suit.PIQUE, Card.Suit.COEUR, Card.Suit.TREFLE));
            holder[0].draw(3);
            return holder[0];
        });
        assertTrue(holder[0].getCurrentHand().size() <= 2, "sa plus forte carte est fermée");

        turnWith(EnemySymbol.RELIC, () -> new Enemy(EnemyKind.PORTEUR), () -> {
            holder[0] = new Player("Joueur", 1_000_000,
                hand(Card.Suit.PIQUE, Card.Suit.PIQUE, Card.Suit.COEUR, Card.Suit.PIQUE, Card.Suit.TREFLE));
            holder[0].draw(5);
            return holder[0];
        });
        assertTrue(holder[0].getCurrentHand().stream().noneMatch(card -> card.getSuit() == Card.Suit.PIQUE),
            "les Piques, la couleur la plus tenue, sont fermés");
    }

    @Test
    void theNewEnemiesHaveTheirOwnCards() {
        assertEquals(List.of(EnemySignature.SERMENT_SOLENNEL), EnemyKind.PELERIN_FER.getSignatures());
        assertEquals(List.of(EnemySignature.BOULE_DE_CRISTAL), EnemyKind.DISEUSE.getSignatures());
        assertEquals(List.of(EnemySignature.AUTEL), EnemyKind.PORTEUR.getSignatures());
        assertTrue(EnemyKind.PROPHETE.getSignatures().contains(EnemySignature.PROCESSION));
    }
}
