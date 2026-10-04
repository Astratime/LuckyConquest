package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.SlotMachine;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.context.SpinContext;
import fr.astratime.lucky.entities.effects.BingoEffect;
import fr.astratime.lucky.entities.enemy.EnemyCards;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
import fr.astratime.lucky.entities.events.DamageReflectedEvent;
import fr.astratime.lucky.entities.events.PlayerDamagedEvent;
import fr.astratime.lucky.entities.events.ReelForbiddenEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** La force des ennemis selon le chapitre, le Rouleau interdit et le Bingo de bouclier. */
class EnemyPowerTest {

    @Test
    void enemiesGrowStrongerWithEachChapter() {
        assertEquals(100, EnemyKind.CROUPIER.getPower());
        assertEquals(100, EnemyKind.COMETE.getPower());
        assertEquals(EnemyKind.CHAPTER_2_POWER, EnemyKind.REINE.getPower());
        assertEquals(EnemyKind.CHAPTER_3_POWER, EnemyKind.ECLAT.getPower());
        assertEquals(30, EnemyKind.CROUPIER.getBaseDefense(), "le croupier ne change pas");
        assertEquals(120 * EnemyKind.CHAPTER_3_POWER / 100, EnemyKind.ECLAT.getBaseDefense());
        assertEquals(EnemySymbol.THORNS_PERCENT * EnemyKind.CHAPTER_3_POWER / 100, EnemyKind.GARDIENNE.thornsPercent());
        assertTrue(EnemyKind.CHAPTER_2_POWER > 100 && EnemyKind.CHAPTER_3_POWER > EnemyKind.CHAPTER_2_POWER);
        assertEquals("x1", EnemyKind.CROUPIER.powerText());
        assertTrue(EnemyKind.ECLAT.diePercent() <= 100);
    }

    @Test
    void everyBlowIsMultipliedByThePower() {
        for (int seed = 0; seed < 200; seed++) {
            Player player = new Player("Joueur", 1_000_000, List.of());
            EnemyTurnResult turn = new EnemyTurnResolver(new Random(seed))
                .resolve(new Enemy(EnemyKind.TRICHEUR), player, 0, 0f);
            for (var event : turn.events()) {
                if (event instanceof PlayerDamagedEvent hit) {
                    int attack = hit.damage + hit.blocked;
                    assertTrue(attack >= EnemyKind.TRICHEUR.empowered(EnemySymbol.SWORD_DAMAGE), "coup renforcé par sa force");
                }
            }
        }
    }

    @Test
    void onlyTheFinalBossHoldsTheForbiddenReel() {
        assertEquals(1, EnemyKind.ECLAT.createDeck().stream().filter(EnemyCards::isForbiddenReel).count());
        for (EnemyKind kind : EnemyKind.values()) {
            if (kind != EnemyKind.ECLAT) assertTrue(kind.createDeck().stream().noneMatch(EnemyCards::isForbiddenReel));
        }
    }

    @Test
    void theForbiddenReelBlocksAReelForTheNextSpin() {
        for (int seed = 0; seed < 50; seed++) {
            Player player = new Player("Joueur", 1_000_000, List.of());
            EnemyTurnResult turn = new EnemyTurnResolver(new Random(seed))
                .resolve(new Enemy(EnemyKind.ECLAT), player, 0, 0f);
            boolean played = turn.played().stream().anyMatch(EnemyCards::isForbiddenReel);
            assertEquals(played, turn.drawn().stream().anyMatch(EnemyCards::isForbiddenReel),
                "tiré, il est toujours joué");
            List<ReelForbiddenEvent> events = turn.events().stream()
                .filter(ReelForbiddenEvent.class::isInstance).map(ReelForbiddenEvent.class::cast).toList();
            if (!played) {
                assertTrue(events.isEmpty());
                assertEquals(-1, player.getLastingEffects().getForbiddenReel());
                continue;
            }
            assertEquals(1, events.size());
            assertEquals(events.get(0).reel, player.getLastingEffects().getForbiddenReel());
            return;
        }
        fail("le Rouleau interdit n'a jamais été joué");
    }

    @Test
    void aBlockedReelStaysEmptyAndPreventsTheBingo() {
        SpinContext spin = new SpinContext();
        spin.forceJackpot(Symbol.SEVEN);
        spin.blockReel(1);
        Symbol[] symbols = new SlotMachine().spin(spin);
        assertArrayEquals(new Symbol[] {Symbol.SEVEN, null, Symbol.SEVEN}, symbols);

        GameController controller = new GameController(() -> new ArrayList<>(List.of(
            new Card("bingo", "Bingo", "bingo.png", List.of(new BingoEffect(1, Symbol.SEVEN)), null, 1))));
        controller.drawCards();
        Player player = controller.getGameState().getPlayer();
        player.getLastingEffects().forbidReel(2);
        controller.playCard(player.getCurrentHand().get(0));
        TurnResult turn = controller.spin();
        assertNull(turn.getSymbols()[2]);
        assertFalse(turn.isJackpot(), "pas de Bingo avec un rouleau bloqué");
        assertTrue(turn.isPair(), "les deux autres rouleaux font une paire");
        assertNotEquals(2, player.getLastingEffects().getForbiddenReel(), "le rouleau se libère après le tirage");
    }

    @Test
    void aShieldBingoReflectsTheShieldAndOffersNothing() {
        boolean reflected = false;
        for (int attempt = 0; attempt < 40 && !reflected; attempt++) {
            GameController controller = new GameController(() -> new ArrayList<>(List.of(
                new Card("grape", "Bingo Raisin", "b.png", List.of(new BingoEffect(100, Symbol.GRAPE)), null, 1))),
                id -> { throw new AssertionError("aucune carte offerte"); });
            controller.drawCards();
            Player player = controller.getGameState().getPlayer();
            controller.playCard(player.getCurrentHand().get(0));
            TurnResult turn = controller.spin();
            assertTrue(turn.isShieldBingo());

            boolean attacked = turn.getEnemyTurn().events().stream().anyMatch(PlayerDamagedEvent.class::isInstance);
            List<DamageReflectedEvent> reflects = turn.getEnemyTurn().afterEvents().stream()
                .filter(DamageReflectedEvent.class::isInstance).map(DamageReflectedEvent.class::cast).toList();
            assertEquals(attacked, !reflects.isEmpty(), "renvoi seulement s'il attaque");
            if (attacked) {
                int shield = turn.getEvents().stream()
                    .filter(e -> e instanceof fr.astratime.lucky.entities.events.ShieldGainedEvent)
                    .mapToInt(e -> ((fr.astratime.lucky.entities.events.ShieldGainedEvent) e).amount).sum();
                assertEquals(shield, reflects.get(0).damage, "tout le bouclier du Bingo est renvoyé");
                reflected = true;
            }

            assertNull(controller.claimBonusCard(), "pas de carte après un Bingo de bouclier");
        }
        assertTrue(reflected, "l'ennemi n'a jamais attaqué");
    }

    @Test
    void theBingoCardSlipsARandomBingoIntoTheDeckOnAGainBingo() {
        boolean gained = false;
        for (int attempt = 0; attempt < 200 && !gained; attempt++) {
            List<String> created = new ArrayList<>();
            GameController controller = new GameController(() -> new ArrayList<>(List.of(
                new Card("bingo", "Bingo", "b.png", List.of(new BingoEffect(100)), null, 1))),
                id -> {
                    created.add(id);
                    return new Card(id, "Bingo", "b.png", List.of(new BingoEffect(1, Symbol.BELL)), null, 1);
                });
            controller.drawCards();
            Player player = controller.getGameState().getPlayer();
            controller.playCard(player.getCurrentHand().get(0));
            TurnResult turn = controller.spin();
            int deckBefore = player.getDeck().getCards().size();
            GameController.Purchase gift = controller.claimBonusCard();
            if (turn.getGainBingoSymbol() == null) {
                assertNull(gift, "pas de carte sans Bingo de gains");
                continue;
            }
            gained = true;
            assertNotNull(gift, "un Bingo est offert");
            assertEquals(1, created.size());
            assertTrue(created.get(0).startsWith(GameController.BINGO_GIFT_PREFIX), created.get(0));
            assertFalse(gift.addedToHand(), "dans le deck, pas sur la table");
            assertEquals(deckBefore + 1, player.getDeck().getCards().size());
            assertNull(controller.claimBonusCard(), "une seule fois");
        }
        assertTrue(gained, "jamais de Bingo de gains");
    }

    @Test
    void aGainBingoWithoutTheBingoCardOffersNothing() {
        GameController controller = new GameController(() -> new ArrayList<>(List.of(
            new Card("bingo_bell", "Bingo Cloche", "b.png", List.of(new BingoEffect(1, Symbol.BELL)), null, 1))),
            id -> { throw new AssertionError("aucune carte offerte"); });
        controller.drawCards();
        controller.playCard(controller.getGameState().getPlayer().getCurrentHand().get(0));
        TurnResult turn = controller.spin();
        assertEquals(Symbol.BELL, turn.getGainBingoSymbol());
        assertNull(controller.claimBonusCard(), "seule la carte « Bingo » offre un Bingo");
    }

    @Test
    void anAttackBingoOffersNothing() {
        GameController controller = new GameController(() -> new ArrayList<>(List.of(
            new Card("seven", "Bingo Sept", "b.png", List.of(new BingoEffect(1, Symbol.SEVEN)), null, 1))),
            id -> { throw new AssertionError("aucune carte offerte"); });
        controller.drawCards();
        controller.playCard(controller.getGameState().getPlayer().getCurrentHand().get(0));
        TurnResult turn = controller.spin();
        assertTrue(turn.isJackpot());
        assertFalse(turn.isShieldBingo());
        assertNull(controller.claimBonusCard());
        assertTrue(Arrays.stream(turn.getSymbols()).allMatch(s -> s == Symbol.SEVEN));
    }

    @Test
    void theTrainingCroupierIsGentle() {
        assertEquals(1_000, EnemyKind.ENTRAINEMENT.getMaxHp());
        assertEquals(10, EnemyKind.ENTRAINEMENT.empowered(EnemyKind.ENTRAINEMENT.swordDamage()));
        assertEquals(EnemySymbol.SWORD_DAMAGE, EnemyKind.CROUPIER.swordDamage(), "le croupier de la Tour ne change pas");
        assertEquals(25_000, EnemyKind.CROUPIER.getMaxHp(), "PV de la Tour x5");
    }
}
