package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.enemy.Difficulty;
import fr.astratime.lucky.entities.enemy.EnemyCards;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
import fr.astratime.lucky.entities.events.StatusEvent;
import fr.astratime.lucky.entities.run.CombatRun;
import fr.astratime.lucky.entities.tower.CroupierRun;
import fr.astratime.lucky.entities.tower.EndlessRun;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** Le Croupier, boss secret : ton deck, la table truquée en phase 2, cartes sur table en phase 3. */
class CroupierBossTest {

    private static Player player() {
        List<Card> deck = new ArrayList<>(EnemyCards.starterDeck());
        return new Player("Joueur", 1_000_000_000, deck, fr.astratime.lucky.entities.RankBonus.NONE,
            List.of(Symbol.CHERRY, Symbol.CHERRY, Symbol.CHERRY, Symbol.BELL, Symbol.BELL, Symbol.SEVEN, Symbol.JOKER));
    }

    private static Enemy croupier(Player player) {
        return new Enemy(EnemyKind.GRAND_CROUPIER, EnemyCards.shadowDeck(player.getAllCards()), Difficulty.HARD);
    }

    @Test
    void heIsStrongerThanTheHardestTowerBossAndTheFirstEndlessFloors() {
        Enemy croupier = new Enemy(EnemyKind.GRAND_CROUPIER, Difficulty.NORMAL);
        assertEquals(EnemyKind.CROUPIER_HP, croupier.getMaxHp());
        assertTrue(EnemyKind.GRAND_CROUPIER.isBoss());
        assertTrue(EnemyKind.GRAND_CROUPIER.copiesPlayerDeck(), "phase 1 : il joue avec ton deck");
        assertEquals(3, EnemyKind.GRAND_CROUPIER.phaseCount());
        assertTrue(EnemyKind.CROUPIER_POWER > EnemyKind.hardPower(6), "plus fort que la Machine Originelle difficile");
        assertTrue(EnemyKind.CROUPIER_POWER > EnemyKind.endlessPower(1), "plus fort que le premier étage sans fin");
        assertTrue(EnemyKind.CROUPIER_HP > EnemyKind.hardBossHp(6));
        assertFalse(EndlessRun.BOSSES.contains(EnemyKind.GRAND_CROUPIER));
    }

    @Test
    void inPhaseTwoHeRigsOneOfYourReelsWithYourRarestSymbolForTwoSpins() {
        Player player = player();
        Enemy croupier = croupier(player);
        croupier.takeDamage(croupier.getMaxHp() / 2 + 1);
        EnemyTurnResult turn = new EnemyTurnResolver(new Random(3)).resolve(croupier, player);
        assertEquals(2, croupier.getPhase());

        LastingEffects lasting = player.getLastingEffects();
        assertEquals(EnemyKind.CROUPIER_RIG_TURNS, lasting.getRiggedTurns());
        assertEquals(Symbol.SEVEN, lasting.getRiggedSymbol(), "le plus rare de tes rouleaux, jamais le Joker");
        int reel = lasting.getRiggedReel();
        assertTrue(reel >= 0 && reel < fr.astratime.lucky.entities.SlotMachine.SYMBOL_COUNT);
        assertTrue(turn.openingEvents().stream().anyMatch(event -> event instanceof StatusEvent));

        // Le tirage du joueur : le rouleau truqué est imposé, même après les cartes.
        var context = new PreparationResolver().resolve(List.of(), player, croupier);
        assertEquals(Symbol.SEVEN, context.getSpinContext().getForcedReels().get(reel));

        lasting.endTurn();
        new EnemyTurnResolver(new Random(4)).resolve(croupier, player);
        assertEquals(1, lasting.getRiggedTurns(), "pas de nouveau truquage tant que le premier dure");
        lasting.endTurn();
        assertNull(lasting.getRiggedSymbol(), "deux tirages, puis la table est libre");
        new EnemyTurnResolver(new Random(5)).resolve(croupier, player);
        assertEquals(EnemyKind.CROUPIER_RIG_TURNS, lasting.getRiggedTurns(), "il retruque la table");
    }

    @Test
    void inPhaseThreeHisNextHandLiesFaceUpAndHePlaysItWithOneMoreCard() {
        Player player = player();
        Enemy croupier = croupier(player);
        croupier.takeDamage(croupier.getMaxHp() / 2 + 1);
        EnemyTurnResolver resolver = new EnemyTurnResolver(new Random(6));
        resolver.resolve(croupier, player);
        assertTrue(croupier.getRevealedHand().isEmpty(), "phase 2 : sa main reste cachée");

        croupier.resetDamageTaken();
        croupier.takeDamage(croupier.getHp() - croupier.getMaxHp() / 20);
        resolver.resolve(croupier, player);
        assertEquals(3, croupier.getPhase());
        assertNull(player.getLastingEffects().getRiggedSymbol(), "cartes sur table : il ne truque plus");
        List<Card> shown = new ArrayList<>(croupier.getRevealedHand());
        assertEquals(Enemy.HAND_SIZE, shown.size(), "le joueur voit toute sa prochaine main");

        EnemyTurnResult next = resolver.resolve(croupier, player);
        assertEquals(shown, next.drawn(), "il joue la main que le joueur a vue");
        assertEquals(EnemyKind.GRAND_CROUPIER.getPlaysPerTurn() + EnemyKind.CROUPIER_OPEN_PLAYS, next.played().size());
        assertEquals(Enemy.HAND_SIZE, croupier.getRevealedHand().size(), "et pose déjà la suivante");
    }

    @Test
    void hisFightIsASingleBossFightWithATrueEnding() {
        CroupierRun run = new CroupierRun();
        assertEquals(EnemyKind.GRAND_CROUPIER, run.getEnemy());
        assertTrue(run.isBossStage());
        assertFalse(run.isHard());
        assertNotNull(run.getEnding());
        assertEquals(CombatRun.Next.CLEARED, run.win());
    }
}
