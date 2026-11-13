package fr.astratime.lucky.entities.exploration;

import fr.astratime.lucky.controllers.GameController;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.run.CombatRun;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Le carrefour d'un donjon : deux portes, cinq salles, un avantage pour le roi, jamais de pièces. */
class CrossroadsTest {

    private static Player player() {
        return new Player("Joueur", 1000, List.of(),
            fr.astratime.lucky.entities.RankBonus.NONE, List.of(Symbol.CHERRY, Symbol.BELL, Symbol.SEVEN));
    }

    @Test
    void twoDifferentDoorsAndEveryRoomCanAppear() {
        Crossroads crossroads = new Crossroads(new Random(1));
        Set<Crossroads.Room> seen = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            List<Crossroads.Room> doors = crossroads.openDoors();
            assertEquals(2, doors.size());
            assertNotEquals(doors.get(0), doors.get(1));
            seen.addAll(doors);
        }
        assertEquals(Set.of(Crossroads.Room.values()), seen);
    }

    @Test
    void theAbandonedMachineFavoursOneOfThePlayersReels() {
        Crossroads crossroads = new Crossroads(new Random(2));
        Symbol found = crossroads.searchMachine(player());
        assertTrue(List.of(Symbol.CHERRY, Symbol.BELL, Symbol.SEVEN).contains(found));
        assertEquals(found, crossroads.getBoon().favored());
    }

    @Test
    void theDenEitherGivesAStallCardOrTakesTwentyPercentOfTheHp() {
        int won = 0, lost = 0;
        Random random = new Random(5);
        for (int i = 0; i < 100; i++) {
            Crossroads crossroads = new Crossroads(random);
            Player player = player();
            String card = crossroads.gamble(player, List.of("bingo", "roulette"));
            if (card != null) {
                won++;
                assertEquals(1000, player.getHp());
                assertEquals(List.of(card), crossroads.getBoon().cards());
            } else {
                lost++;
                assertEquals(800, player.getHp());
                assertTrue(crossroads.getBoon().isEmpty());
            }
        }
        assertTrue(won > 30 && lost > 30, won + " / " + lost);
    }

    @Test
    void theFountainHealsOrTradesHpForOneMorePlay() {
        Player player = player();
        player.takeDamage(500);
        Crossroads crossroads = new Crossroads(new Random(3));
        assertEquals(300, crossroads.drink(player));
        assertEquals(800, player.getHp());
        assertTrue(crossroads.getBoon().isEmpty());

        assertEquals(300, crossroads.offer(player));
        assertEquals(500, player.getHp());
        assertEquals(1, crossroads.getBoon().extraPlays());
    }

    @Test
    void theTrappedChestGivesAClassicCardAndNeverKills() {
        Player player = player();
        player.takeDamage(950);
        String card = new Crossroads(new Random(4)).openTrappedChest(player);
        assertTrue(card.matches("([2-9]|1[0-3])_(pique|coeur|trefle|carreau)"), card);
        assertEquals(1, player.getHp(), "le piège laisse toujours 1 PV");
    }

    @Test
    void aRestartedDungeonLosesTheBoon() {
        DungeonRun run = new DungeonRun(Place.PRAIRIE.getDungeons().get(0));
        run.getCrossroads().offer(player());
        assertEquals(CombatRun.Next.BOSS, run.win());
        assertFalse(run.getCrossroads().getBoon().isEmpty());
        run.restart();
        assertTrue(run.getCrossroads().getBoon().isEmpty());
    }

    @Test
    void theBoonIsAppliedAtTheStartOfTheKingsFight() {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 20; i++) deck.add(new Card("c" + i, "c" + i, "x.png", List.of(), null, 1));
        GameController controller = new GameController(() -> new ArrayList<>(deck),
            id -> new Card(id, id, "x.png", List.of(), null, 1), Map.of());
        controller.restart(EnemyKind.CROUPIER);
        controller.startCombat(EnemyKind.CROUPIER);
        controller.applyBoon(new Crossroads.Boon(Symbol.BELL, List.of("7_pique"), 1));

        Player player = controller.getGameState().getPlayer();
        assertEquals(Symbol.BELL, player.getLastingEffects().getFavoredSymbol());
        assertEquals(GameController.DEFAULT_PLAY_LIMIT + 1, controller.getPlayLimit());
        int usual = controller.drawCards().getAddedToHand().size();
        assertTrue(player.getCurrentHand().stream().anyMatch(card -> card.getId().equals("7_pique")));
        assertEquals(GameController.DEFAULT_DRAW_COUNT + 1, usual, "la carte du coffre s'ajoute à la main habituelle");

        controller.startCombat(EnemyKind.CROUPIER); // combat suivant : plus rien
        assertNull(controller.getGameState().getPlayer().getLastingEffects().getFavoredSymbol());
    }
}
