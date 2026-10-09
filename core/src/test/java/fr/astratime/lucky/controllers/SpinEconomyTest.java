package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.SpinEconomy;
import fr.astratime.lucky.entities.SpinEconomy.Debt;
import fr.astratime.lucky.entities.SpinEconomy.Stake;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.actions.GainAction;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.GainsEarnedEvent;
import fr.astratime.lucky.entities.events.StatusEvent;
import fr.astratime.lucky.entities.exploration.Place;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Les gains en combat : coût du tirage, départ, Paires et Bingos, dette et Mise (voir {@link SpinEconomy}). */
class SpinEconomyTest {

    private static List<Card> plainCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 20; i++) cards.add(new Card("c" + i, "c" + i, "c.png", List.of(), null, 1));
        return cards;
    }

    /** Un combat réel (après {@link GameController#restart}) contre {@code kind}. */
    private static GameController fight(EnemyKind kind) {
        GameController controller = new GameController(SpinEconomyTest::plainCards);
        controller.restart(kind);
        return controller;
    }

    private static Player player(GameController controller) { return controller.getGameState().getPlayer(); }

    private static long damageDealt(TurnResult result) {
        return result.getSymbolOutcomes().stream().flatMap(o -> o.getEvents().stream())
            .filter(e -> e instanceof EnemyDamagedEvent).mapToLong(e -> ((EnemyDamagedEvent) e).rawDamage).sum();
    }

    @Test
    void theSpinCostFollowsThePlaceAndTheTowerChapter() {
        assertEquals(100, EnemyKind.ENTRAINEMENT.getSpinCost());
        assertEquals(100, EnemyKind.CROUPIER.getSpinCost(), "chapitre 1");
        assertEquals(100, EnemyKind.CHEF.getSpinCost(), "chapitre 2");
        assertEquals(500, EnemyKind.GARDIENNE.getSpinCost(), "chapitre 3");
        assertEquals(500, EnemyKind.PRETENDANT.getSpinCost(), "chapitre 4");
        assertEquals(2_000, EnemyKind.PORTIER.getSpinCost(), "chapitre 5");
        assertEquals(5_000, EnemyKind.MACHINE_ORIGINELLE.getSpinCost(), "chapitre 6");
        int[] costs = {100, 500, 2_000, 5_000};
        for (Place place : Place.values()) {
            for (var dungeon : place.getDungeons()) {
                assertEquals(costs[place.ordinal()], dungeon.getSoldier().getSpinCost(), place + " soldat");
                assertEquals(costs[place.ordinal()], dungeon.getKing().getSpinCost(), place + " roi");
            }
        }
    }

    @Test
    void aFightStartsWithFiveSpinsAndEachSpinCostsOne() {
        GameController controller = fight(EnemyKind.CROUPIER);
        assertEquals(500, player(controller).getGains());
        assertEquals(100, controller.getSpinCost());
        controller.drawCards();
        controller.rigSpin(Symbol.SEVEN, Symbol.BAR, Symbol.GRAPE); // ni gains, ni Paire
        controller.spin();
        assertEquals(400, player(controller).getGains());

        GameController casino = fight(Place.CASINO.getDungeons().get(0).getSoldier());
        assertEquals(25_000, player(casino).getGains());
    }

    @Test
    void theNextFightOfAChapterKeepsTheGainsWithoutANewStart() {
        GameController controller = fight(EnemyKind.CROUPIER);
        player(controller).addGains(1_000);
        controller.startCombat(EnemyKind.GARDIEN);
        assertEquals(1_500, player(controller).getGains());
    }

    @Test
    void aControllerWithoutRealFightSpinsForFree() {
        GameController controller = new GameController(SpinEconomyTest::plainCards);
        assertEquals(0, controller.getSpinCost());
        assertEquals(0, player(controller).getGains());
    }

    @Test
    void pairsAndBingosPayInSpinCosts() {
        assertEquals(1_500, SpinEconomy.pairGains(500));
        assertEquals(10_000, SpinEconomy.jackpotGains(Symbol.CHERRY, 500), "fruit commun : 20");
        assertEquals(15_000, SpinEconomy.jackpotGains(Symbol.SEVEN, 500), "symbole moyen : 30");
        assertEquals(25_000, SpinEconomy.jackpotGains(Symbol.TRIPLE_SEVEN, 500), "symbole rare : 50");
        assertEquals(25_000, SpinEconomy.jackpotGains(Symbol.NUGGET, 500), "rouleau de la Mine : 50");
        assertEquals(25_000, SpinEconomy.jackpotGains(Symbol.CROWN, 500), "rouleau de la boutique : 50");
    }

    @Test
    void gainSymbolsFollowTheSpinCost() {
        Enemy portSoldier = new Enemy(Place.PORT.getDungeons().get(0).getSoldier());
        Player player = new Player("Joueur", Player.BASE_HP, plainCards());
        CombatContext context = new CombatContext(player, portSoldier);
        new GainAction(8).resolve(context);
        assertEquals(40, player.getGains(), "Cloche au Port : 8 x 5");
    }

    @Test
    void debtStartsBelowZeroAndTheBailiffComesAtFiveSpins() {
        assertEquals(Debt.NONE, SpinEconomy.debt(0, 100));
        assertEquals(Debt.INDEBTED, SpinEconomy.debt(-1, 100));
        assertEquals(Debt.INDEBTED, SpinEconomy.debt(-499, 100));
        assertEquals(Debt.BAILIFF, SpinEconomy.debt(-500, 100));
        assertEquals(Debt.INDEBTED, SpinEconomy.debt(-2_000, 500));
    }

    @Test
    void onlyTheSpinCostDigsTheDebt() {
        GameController controller = fight(EnemyKind.CROUPIER);
        player(controller).addGains(-10_000);
        assertEquals(0, player(controller).getGains(), "une perte ne fait pas passer sous 0");
        player(controller).paySpin(100);
        assertEquals(-100, player(controller).getGains());
        assertEquals(Debt.INDEBTED, controller.getDebt());
    }

    @Test
    void debtWeakensTheSymbols() {
        GameController rich = fight(EnemyKind.CROUPIER);
        rich.drawCards();
        rich.rigSpin(Symbol.DOUBLE_BAR, Symbol.GRAPE, Symbol.BELL);
        long full = damageDealt(rich.spin());

        GameController poor = fight(EnemyKind.CROUPIER);
        player(poor).paySpin(500); // à 0 : le tirage le fait passer à -100
        poor.drawCards();
        poor.rigSpin(Symbol.DOUBLE_BAR, Symbol.GRAPE, Symbol.BELL);
        TurnResult result = poor.spin();
        assertEquals(Math.round(full * SpinEconomy.INDEBTED_FACTOR), damageDealt(result));
        assertTrue(result.getEvents().stream().anyMatch(e -> e instanceof StatusEvent));
    }

    @Test
    void theBailiffMakesTheEnemyPlayOneMoreCard() {
        GameController controller = fight(EnemyKind.CROUPIER);
        player(controller).paySpin(1_000); // -500, puis -600 après le tirage
        controller.drawCards();
        controller.rigSpin(Symbol.BAR, Symbol.GRAPE, Symbol.BELL);
        TurnResult result = controller.spin();
        assertEquals(Debt.BAILIFF, controller.getDebt());
        assertNotNull(result.getEnemyTurn());
        assertEquals(EnemyKind.CROUPIER.getPlaysPerTurn() + 1, result.getEnemyTurn().played().size());
    }

    @Test
    void theStakeIsAShareOfTheGainsAndMultipliesTheSymbols() {
        assertEquals(100, Stake.LOW.amount(1_000));
        assertEquals(250, Stake.MEDIUM.amount(1_000));
        assertEquals(500, Stake.HIGH.amount(1_000));
        assertEquals(0, Stake.HIGH.amount(-300), "on ne mise que ce qu'on a");
        assertEquals(Stake.NONE, Stake.HIGH.next());

        GameController plain = fight(EnemyKind.CROUPIER);
        plain.drawCards();
        plain.rigSpin(Symbol.SEVEN, Symbol.BAR, Symbol.BELL);
        TurnResult base = plain.spin();

        GameController staked = fight(EnemyKind.CROUPIER);
        staked.drawCards();
        assertEquals(Stake.LOW, staked.nextStake());
        assertEquals(Stake.MEDIUM, staked.nextStake());
        assertEquals(125, staked.getStakeAmount(), "25 % de 500");
        staked.rigSpin(Symbol.SEVEN, Symbol.BAR, Symbol.BELL);
        TurnResult result = staked.spin();
        assertEquals(damageDealt(base) * 3, damageDealt(result));
        int bell = base.getEvents().stream().filter(e -> e instanceof GainsEarnedEvent)
            .mapToInt(e -> ((GainsEarnedEvent) e).amount).sum();
        assertEquals(500 - 125 - 100 + bell * 3, player(staked).getGains());
        assertEquals(Stake.NONE, staked.getStake(), "la Mise ne vaut que pour un tirage");
    }

    @Test
    void noStakeWithoutGains() {
        GameController controller = fight(EnemyKind.CROUPIER);
        player(controller).paySpin(500);
        assertFalse(controller.canStake());
        assertEquals(Stake.NONE, controller.nextStake());
    }
}
