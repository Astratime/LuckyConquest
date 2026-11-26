package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.effects.ExtraDrawEffect;
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
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/** Le chapitre des Éclats dispersés : sa place, ses réglages, ses quatre symboles et le Collectionneur. */
class ScatteredShardsTest {

    /** Un tour de {@code enemy} dont les rouleaux montrent {@code symbol} (en cherchant la graine). */
    private static EnemyTurnResult turnWith(EnemySymbol symbol, Supplier<Enemy> enemy, Supplier<Player> player) {
        for (int seed = 0; seed < 500; seed++) {
            EnemyTurnResult turn = new EnemyTurnResolver(new Random(seed)).resolve(enemy.get(), player.get());
            if (Arrays.asList(turn.symbols()).contains(symbol)) return turn;
        }
        throw new AssertionError("aucun tirage avec " + symbol);
    }

    @Test
    void theChapterSitsBetweenTheLastDrawAndTheWorldWithoutMaster() {
        Chapter chapter = Chapter.ECLATS_DISPERSES;
        assertSame(Chapter.DERNIER_TIRAGE, chapter.getPrevious());
        assertSame(chapter, Chapter.MONDE_SANS_MAITRE.getPrevious());
        assertSame(chapter, Chapter.of(EnemyKind.COLLECTIONNEUR));
        assertSame(EnemyKind.RAMASSEUR, chapter.getFirstEnemy());
        assertEquals(List.of(EnemyKind.HERITIER, EnemyKind.VEUVE, EnemyKind.NOTAIRE), chapter.getChallengers());
        assertTrue(EnemyKind.COLLECTIONNEUR.isBoss());
        assertTrue(EnemyKind.COLLECTIONNEUR.isTowerBoss());
        // Plan des 10 chapitres : force x3,5 et tirage à 500 ; difficile x27,8 et boss à 18 M (entre 8 M et 40 M).
        assertEquals(350, EnemyKind.HERITIER.getPower(Difficulty.NORMAL));
        assertEquals(500, EnemyKind.HERITIER.getSpinCost());
        assertEquals(10_000, EnemyKind.COLLECTIONNEUR.getPower(Difficulty.HARD));
        assertEquals(18_000_000L, EnemyKind.COLLECTIONNEUR.getMaxHp(Difficulty.HARD));
        assertEquals(1, EnemyKind.VEUVE.getOwnPlaysPerTurn());
    }

    @Test
    void theCollectorGainsOneShardPowerPerPhase() {
        EnemyKind collector = EnemyKind.COLLECTIONNEUR;
        assertEquals(3, collector.phaseCount());
        assertEquals(1, collector.phaseFor(0.9f));
        assertEquals(2, collector.phaseFor(0.5f));
        assertEquals(3, collector.phaseFor(0.2f));
        assertTrue(collector.getSymbols(1).contains(EnemySymbol.LEGACY));
        assertFalse(collector.getSymbols(1).contains(EnemySymbol.WALTZ));
        assertTrue(collector.getSymbols(2).contains(EnemySymbol.WALTZ));
        assertFalse(collector.getSymbols(2).contains(EnemySymbol.SEAL));
        assertTrue(collector.getSymbols(3).containsAll(List.of(EnemySymbol.SHARD, EnemySymbol.LEGACY, EnemySymbol.WALTZ,
            EnemySymbol.SEAL)));
        assertTrue(collector.getSignatures().contains(EnemySignature.POCHE_PLEINE));
    }

    @Test
    void theThirdShardJoinsTheOthersAndStartsAgain() {
        Enemy picker = new Enemy(EnemyKind.RAMASSEUR);
        assertFalse(picker.addShard());
        assertFalse(picker.addShard());
        assertEquals(2, picker.getShards());
        assertTrue(picker.addShard(), "au troisième, les éclats se rejoignent");
        assertEquals(0, picker.getShards());
    }

    @Test
    void theJoinedShardsStrikeThroughTheShield() {
        Player[] holder = new Player[1];
        Enemy[] picker = new Enemy[1];
        turnWith(EnemySymbol.SHARD, () -> {
            picker[0] = new Enemy(EnemyKind.RAMASSEUR);
            picker[0].addShard();
            picker[0].addShard();
            return picker[0];
        }, () -> {
            holder[0] = new Player("Joueur", 1_000_000, List.of());
            holder[0].addShield(100_000_000);
            return holder[0];
        });
        assertTrue(holder[0].getHp() < 1_000_000, "le coup des éclats traverse le bouclier");
    }

    @Test
    void theLegacyTakesHalfTheShield() {
        Player[] holder = new Player[1];
        turnWith(EnemySymbol.LEGACY, () -> new Enemy(EnemyKind.HERITIER), () -> {
            holder[0] = new Player("Joueur", 1_000_000, List.of());
            holder[0].addShield(1_000_000_000);
            return holder[0];
        });
        assertTrue(holder[0].getShield() <= 500_000_000, "il prend au moins la moitié du bouclier");
    }

    @Test
    void theWaltzHitsHarderWhenThePlayerIsHurt() {
        long full = totalHit(1_000_000), hurt = totalHit(250_000);
        assertTrue(hurt > full, "plus le joueur est blessé, plus la Valse frappe (" + full + " / " + hurt + ")");
    }

    /** @return les PV qu'un tour de la Veuve (graine 0 qui sort une Valse) fait perdre au joueur, qui en a {@code hp}. */
    private static long totalHit(int hp) {
        for (int seed = 0; seed < 500; seed++) {
            Player player = new Player("Joueur", 1_000_000, List.of());
            player.takeDamage(1_000_000 - hp, true);
            EnemyTurnResult turn = new EnemyTurnResolver(new Random(seed)).resolve(new Enemy(EnemyKind.VEUVE), player);
            if (Arrays.stream(turn.symbols()).allMatch(symbol -> symbol == EnemySymbol.WALTZ)) return hp - player.getHp();
        }
        throw new AssertionError("aucun tirage de trois Valses");
    }

    @Test
    void aSealTakesOneCardOffTheNextTurn() {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 8; i++) deck.add(new Card("carte" + i, "carte" + i, "x.png", List.of(new ExtraDrawEffect(0)), null, 1));
        GameController controller = new GameController(() -> new ArrayList<>(deck), id -> null);
        controller.getGameState().getPlayer().getLastingEffects().addSeals(EnemySymbol.SEAL_PLAYS);
        controller.drawCards();
        assertEquals(GameController.DEFAULT_PLAY_LIMIT - 1, controller.getPlayLimit());
        controller.spin();
        controller.drawCards();
        assertEquals(GameController.DEFAULT_PLAY_LIMIT, controller.getPlayLimit(), "un Scellé ne vaut qu'un tour");
    }

    @Test
    void manySealsStillLeaveOneCard() {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 8; i++) deck.add(new Card("carte" + i, "carte" + i, "x.png", List.of(new ExtraDrawEffect(0)), null, 1));
        GameController controller = new GameController(() -> new ArrayList<>(deck), id -> null);
        controller.getGameState().getPlayer().getLastingEffects().addSeals(10);
        controller.drawCards();
        assertEquals(1, controller.getPlayLimit());
    }
}
