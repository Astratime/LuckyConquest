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
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/** Le chapitre de la Tour « La Guerre des Tables » : sa place, ses réglages, ses ennemis et leurs symboles. */
class WarOfTablesTest {

    /** Un tour de l'ennemi {@code kind} dont les rouleaux montrent {@code symbol} (en cherchant la graine). */
    private static EnemyTurnResult turnWith(EnemySymbol symbol, Supplier<Enemy> enemy, Supplier<Player> player,
                                            Consumer<EnemyTurnResult> check) {
        for (int seed = 0; seed < 500; seed++) {
            EnemyTurnResult turn = new EnemyTurnResolver(new Random(seed)).resolve(enemy.get(), player.get());
            if (Arrays.asList(turn.symbols()).contains(symbol)) {
                check.accept(turn);
                return turn;
            }
        }
        throw new AssertionError("aucun tirage avec " + symbol);
    }

    private static Player player() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 20; i++) cards.add(new Card("c" + i, "c" + i, "x.png", List.of(), Card.Suit.TREFLE, 2 + i % 12));
        return new Player("Joueur", 1_000_000, cards);
    }

    @Test
    void itSitsBetweenTheWorldWithoutMasterAndTheHouse() {
        Chapter war = Chapter.GUERRE_DES_TABLES;
        assertEquals(Chapter.MONDE_SANS_MAITRE, war.getPrevious());
        assertEquals(war, Chapter.LA_MAISON.getPrevious());
        assertEquals(EnemyKind.SERGENT, war.getFirstEnemy());
        assertEquals(List.of(EnemyKind.CANONNIER, EnemyKind.ESPIONNE, EnemyKind.MERCENAIRE), war.getChallengers());
        assertEquals(EnemyKind.GENERAL, war.getBoss());
        assertNull(war.getEnding(), "pas de fin d'histoire au milieu de la Tour");
        // Plan des 10 chapitres (Astra, 2026-11-20) : force x4,5, tirage 1 000, difficile x60 (6 000 %), boss 63 M.
        assertEquals(450, EnemyKind.GENERAL.getPower(Difficulty.NORMAL));
        assertEquals(1_000, EnemyKind.SERGENT.getSpinCost());
        assertEquals(10_200, EnemyKind.CANONNIER.getPower(Difficulty.HARD));
        assertEquals(63_000_000L, EnemyKind.GENERAL.getMaxHp(Difficulty.HARD));
        assertEquals(1, EnemyKind.MERCENAIRE.getOwnPlaysPerTurn(), "4 cartes au plus, comme avant le dernier chapitre");
        // Ses voisins entre lesquels il se place : force et PV.
        assertTrue(EnemyKind.PRETENDANT.getPower(Difficulty.NORMAL) < EnemyKind.GENERAL.getPower(Difficulty.NORMAL));
        assertTrue(EnemyKind.GENERAL.getPower(Difficulty.NORMAL) < EnemyKind.MAISON.getPower(Difficulty.NORMAL));
        assertTrue(EnemyKind.PRETENDANT.getMaxHp(Difficulty.HARD) < EnemyKind.GENERAL.getMaxHp(Difficulty.HARD));
        assertTrue(EnemyKind.GENERAL.getMaxHp(Difficulty.HARD) < EnemyKind.MAISON.getMaxHp(Difficulty.HARD));
        for (EnemyKind kind : List.of(EnemyKind.SERGENT, EnemyKind.CANONNIER, EnemyKind.ESPIONNE, EnemyKind.MERCENAIRE,
                EnemyKind.GENERAL)) {
            assertTrue(kind.isTower(), kind.name());
            assertFalse(kind.getSignatures().isEmpty(), kind + " a sa carte propre");
        }
    }

    @Test
    void theGeneralFallsInThirdsAndGoesAllOutAtTheEnd() {
        EnemyKind general = EnemyKind.GENERAL;
        assertTrue(general.isBoss());
        assertEquals(3, general.phaseCount());
        assertEquals(1, general.phaseFor(0.8f));
        assertEquals(2, general.phaseFor(0.5f));
        assertEquals(3, general.phaseFor(0.2f));
        assertFalse(general.getWeights(1).containsKey(EnemySymbol.SPY));
        assertTrue(general.getWeights(2).containsKey(EnemySymbol.SPY), "ses lieutenants arrivent");
        assertTrue(general.getWeights(2).containsKey(EnemySymbol.CONTRACT));
        assertFalse(general.getWeights(3).containsKey(EnemySymbol.SHIELD), "Va-tout : plus de Bouclier");
        assertFalse(general.getWeights(3).containsKey(EnemySymbol.POTION));
        assertEquals(2, general.getWeights(3).get(EnemySymbol.ALL_OUT));
    }

    @Test
    void troopsJoinUpToThreeAndABigHitKnocksOneDown() {
        Enemy sergent = new Enemy(EnemyKind.SERGENT);
        for (int i = 0; i < EnemySymbol.RALLY_MAX; i++) assertTrue(sergent.addTroop());
        assertFalse(sergent.addTroop(), "trois soldats au plus");
        sergent.takeDamage(sergent.getMaxHp() * EnemySymbol.RALLY_HIT_PERCENT / 100 - 1);
        assertEquals(3, sergent.getTroops(), "un petit coup ne suffit pas");
        sergent.takeDamage(sergent.getMaxHp() * EnemySymbol.RALLY_HIT_PERCENT / 100);
        assertEquals(2, sergent.getTroops());
    }

    @Test
    void aRallyCallsASoldierWhoFiresAtTheNextTurn() {
        turnWith(EnemySymbol.RALLY, () -> new Enemy(EnemyKind.SERGENT), WarOfTablesTest::player, turn -> { });
        Enemy[] held = new Enemy[1];
        turnWith(EnemySymbol.RALLY, () -> held[0] = new Enemy(EnemyKind.SERGENT), WarOfTablesTest::player,
            turn -> assertTrue(held[0].getTroops() >= 1));
        Player player = player();
        long before = player.getHp();
        new EnemyTurnResolver(new Random(1)).resolve(held[0], player);
        assertTrue(player.getHp() < before, "ses soldats tirent");
    }

    @Test
    void theCannonLoadsTenPercentOfTheGains() {
        Player[] held = new Player[1];
        turnWith(EnemySymbol.CANNON, () -> new Enemy(EnemyKind.CANONNIER), () -> {
            held[0] = player();
            held[0].addGains(100_000);
            return held[0];
        }, turn -> {
            long cannons = Arrays.stream(turn.symbols()).filter(s -> s == EnemySymbol.CANNON).count();
            assertEquals(100_000 * Math.pow(0.9, cannons), held[0].getGains(), 1.0);
        });
    }

    @Test
    void theSpyShieldsHerselfOnePerCardPlayed() {
        Enemy[] held = new Enemy[1];
        turnWith(EnemySymbol.SPY, () -> held[0] = new Enemy(EnemyKind.ESPIONNE), () -> {
            Player player = player();
            player.draw(5);
            for (Card card : new ArrayList<>(player.getCurrentHand()).subList(0, 3)) player.playCard(card);
            return player;
        }, turn -> assertTrue(held[0].getDefense() >= EnemyKind.ESPIONNE.getBaseDefense(Difficulty.NORMAL)
            + EnemyKind.ESPIONNE.empowered(EnemyKind.ESPIONNE.shieldDefense() * 3, Difficulty.NORMAL),
            "trois cartes jouées : au moins trois Boucliers"));
    }

    @Test
    void anUnpaidContractHitsThreeTimesHarder() {
        // Contrat seul sur ses rouleaux : sans dégâts reçus, il touche sa prime (x3) ; touché, il frappe d'une Épée.
        long[] lost = new long[2];
        for (int k = 0; k < 2; k++) {
            boolean hurt = k == 1;
            Player[] held = new Player[1];
            turnWith(EnemySymbol.CONTRACT, () -> {
                Enemy mercenary = new Enemy(EnemyKind.MERCENAIRE);
                if (hurt) mercenary.takeDamage(mercenary.getMaxHp() * EnemySymbol.CONTRACT_PERCENT / 100);
                return mercenary;
            }, () -> held[0] = player(), turn -> { });
            lost[k] = held[0].getMaxHp() - held[0].getHp();
        }
        assertTrue(lost[0] > lost[1], "le Contrat payé frappe plus fort");
    }

    @Test
    void allOutDropsHisDefenseAfterHisTurn() {
        Enemy[] held = new Enemy[1];
        turnWith(EnemySymbol.ALL_OUT, () -> {
            held[0] = new Enemy(EnemyKind.GENERAL);
            return held[0];
        }, WarOfTablesTest::player, turn -> assertEquals(0, held[0].getDefense(), "Va-tout : plus de défense jusqu'à son tour"));
        assertEquals(EnemySignature.ORDRE_FACTOR, 3);
    }
}
