package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.RankBonus;
import fr.astratime.lucky.entities.SlotMachine;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.choices.RiggedReelChoice;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.context.SpinContext;
import fr.astratime.lucky.entities.effects.AllInEffect;
import fr.astratime.lucky.entities.effects.BingoEffect;
import fr.astratime.lucky.entities.effects.BribeEffect;
import fr.astratime.lucky.entities.effects.DoubleOrNothingEffect;
import fr.astratime.lucky.entities.effects.Effect;
import fr.astratime.lucky.entities.effects.ForceReelEffect;
import fr.astratime.lucky.entities.effects.GainEffect;
import fr.astratime.lucky.entities.effects.InsuranceEffect;
import fr.astratime.lucky.entities.effects.OverheatEffect;
import fr.astratime.lucky.entities.effects.RankTokenEffect;
import fr.astratime.lucky.entities.effects.RerollEffect;
import fr.astratime.lucky.entities.effects.RiggedReelEffect;
import fr.astratime.lucky.entities.effects.SafeEffect;
import fr.astratime.lucky.entities.events.AllInLostEvent;
import fr.astratime.lucky.entities.events.AllInWonEvent;
import fr.astratime.lucky.entities.events.SafeOpenedEvent;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Les nouvelles cartes de la boutique. */
class ShopCardsTest {

    private static Card card(String id, Effect... effects) {
        return new Card(id, id, id + ".png", List.of(effects), null, 1);
    }

    /** Contrôleur dont la main contient {@code cards} (au plus une main : tout est pioché). */
    private static GameController controllerWith(Card... cards) {
        List<Card> deck = List.of(cards);
        GameController controller = new GameController(() -> new ArrayList<>(deck));
        controller.drawCards();
        return controller;
    }

    private static Player player(GameController controller) { return controller.getGameState().getPlayer(); }

    private static Card inHand(GameController controller, String id) {
        return player(controller).getCurrentHand().stream().filter(c -> c.getId().equals(id)).findFirst().orElseThrow();
    }

    private static TurnResult resolve(CombatContext context, Symbol... symbols) {
        return new CombatResolver().resolve(context, new ActionResolver().resolve(symbols), symbols, List.of());
    }

    @RepeatedTest(20)
    void rerollSpinsAgainWhenThereIsNoPair() {
        GameController controller = controllerWith(card("relance", new RerollEffect()));
        controller.playCard(inHand(controller, "relance"));
        TurnResult turn = controller.spin();
        Symbol[] first = turn.getRerolledDraw();
        if (first != null) {
            assertFalse(SlotMachine.hasPair(first) || hasJoker(first), "on ne relance qu'un tirage sans paire");
        }
    }

    private static boolean hasJoker(Symbol[] symbols) {
        for (Symbol s : symbols) if (s == Symbol.JOKER) return true;
        return false;
    }

    @Test
    void noRerollWithoutTheCard() {
        GameController controller = controllerWith();
        assertNull(controller.spin().getRerolledDraw());
    }

    @Test
    void theRiggedReelAsksForASymbolAndPutsItInTheMiddle() {
        GameController controller = controllerWith(card("truque", new RiggedReelEffect()));
        assertInstanceOf(RiggedReelChoice.class, controller.playCard(inHand(controller, "truque")).getChoice());
        controller.rigReel(Symbol.SEVEN);
        assertNull(controller.getPendingChoice());
        assertEquals(Symbol.SEVEN, controller.getForcedMiddleSymbol());
        assertEquals(Symbol.SEVEN, controller.spin().getDrawnSymbols()[1]);
    }

    @RepeatedTest(10)
    void theGhostReelPutsAJokerInTheMiddle() {
        GameController controller = controllerWith(card("fantome", new ForceReelEffect(Symbol.JOKER)));
        controller.playCard(inHand(controller, "fantome"));
        TurnResult turn = controller.spin();
        assertEquals(Symbol.JOKER, turn.getDrawnSymbols()[1]);
        assertTrue(SlotMachine.hasPair(turn.getSymbols()), "le Joker fait au moins une paire");
    }

    @Test
    void theRankTokenDoublesTheRankBonus() {
        Player player = new Player("Joueur", Player.BASE_HP, List.of(), new RankBonus(0, 100, 50, 70),
            Symbol.classicReels());
        CombatContext context = new CombatContext(player, new Enemy("Ennemi", 100_000));
        context.multiplyRankBonus(RankTokenEffect.FACTOR);
        resolve(context, Symbol.GRAPE, Symbol.BELL, null);
        assertEquals(8 + 100, player.getShield(), "Raisin : 8 + 2 x 50");
        assertEquals(8 + 140, player.getGains(), "Cloche : 8 + 2 x 70");
    }

    @Test
    void allInTriplesTheGainsOnAPair() {
        Player player = new Player("Joueur", 100, List.of());
        player.addGains(1000);
        CombatContext context = new CombatContext(player, new Enemy("Ennemi", 100_000));
        context.addAllIn();
        TurnResult turn = resolve(context, Symbol.BAR, Symbol.BAR, Symbol.SEVEN);
        int beforeAllIn = 1000 + 500; // + bonus de paire
        assertEquals(beforeAllIn * 3, player.getGains());
        assertTrue(turn.getPairOrJackpotEvents().stream().anyMatch(e -> e instanceof AllInWonEvent));
    }

    @Test
    void allInLosesEverythingWithoutAPair() {
        Player player = new Player("Joueur", 100, List.of());
        player.addGains(1000);
        CombatContext context = new CombatContext(player, new Enemy("Ennemi", 100_000));
        context.addAllIn();
        TurnResult turn = resolve(context, Symbol.BAR, Symbol.CHERRY, Symbol.SEVEN);
        assertEquals(0, player.getGains());
        assertTrue(turn.getPairOrJackpotEvents().stream().anyMatch(e -> e instanceof AllInLostEvent));
    }

    @Test
    void theSafeKeepsGainsAsideAndGivesThemBackDoubledAfterThreeTurns() {
        Card safe = card("coffre", new SafeEffect(30, 3));
        GameController controller = controllerWith(safe);
        player(controller).addGains(1000);
        controller.playCard(inHand(controller, "coffre"));
        assertEquals(700, player(controller).getGains());

        List<TurnResult> turns = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            if (i > 0) controller.drawCards();
            turns.add(controller.spin());
        }
        assertTrue(turns.get(0).getEnemyTurnEvents().stream().noneMatch(e -> e instanceof SafeOpenedEvent));
        assertTrue(turns.get(1).getEnemyTurnEvents().stream().noneMatch(e -> e instanceof SafeOpenedEvent));
        SafeOpenedEvent opened = (SafeOpenedEvent) turns.get(2).getEnemyTurnEvents().stream()
            .filter(e -> e instanceof SafeOpenedEvent).findFirst().orElseThrow();
        assertEquals(600, opened.amount);
        assertTrue(player(controller).getLastingEffects().getSafes().isEmpty());
    }

    @Test
    void theSafeOpensAsSoonAsTheEnemyFalls() {
        GameController controller = controllerWith(card("coffre", new SafeEffect(30, 3)));
        player(controller).addGains(1000);
        controller.playCard(inHand(controller, "coffre"));
        Enemy enemy = controller.getGameState().getEnemy();
        enemy.takeDamage(enemy.getHp());
        TurnResult turn = controller.spin();
        assertTrue(turn.getEnemyTurnEvents().stream().anyMatch(e -> e instanceof SafeOpenedEvent));
    }

    @Test
    void insuranceCapsTheDamageOfTheTurn() {
        Player player = new Player("Joueur", 1000, List.of());
        player.insure(10);
        assertEquals(60, player.takeDamage(60));
        assertEquals(40, player.takeDamage(500), "100 PV au plus pour le tour");
        assertEquals(0, player.takeDamage(500));
        player.resetTurnDefenses();
        assertEquals(500, player.takeDamage(500), "le tour suivant, plus d'assurance");
    }

    @Test
    void bribeDropsTheEnemyDefenseUntilItsTurn() {
        Enemy enemy = new Enemy("Ennemi", 100_000);
        enemy.addShieldDefense(500);
        assertTrue(enemy.getDefense() > 0);
        CombatContext context = new CombatContext(new Player("Joueur", 100, List.of()), enemy);
        new BribeEffect().apply(new fr.astratime.lucky.entities.context.TurnContext(new SpinContext(), context));
        assertEquals(0, enemy.getDefense());
        resolve(context, Symbol.BAR, null, null);
        assertEquals(100_000 - 10, enemy.getHp(), "aucun point absorbé");
        enemy.resetDefense();
        assertEquals(enemy.getBaseDefense(), enemy.getDefense(), "elle se reforme à son tour");
    }

    @Test
    void doubleOrNothingMakesTheNextCardCountTwice() {
        GameController controller = controllerWith(card("double", new DoubleOrNothingEffect()),
            card("gains", new GainEffect(500)), card("gains2", new GainEffect(500)));
        controller.playCard(inHand(controller, "double"));
        assertTrue(controller.isDoubleNextPending());
        controller.playCard(inHand(controller, "gains"));
        assertEquals(1000, player(controller).getGains());
        assertFalse(controller.isDoubleNextPending());
        controller.playCard(inHand(controller, "gains2"));
        assertEquals(1500, player(controller).getGains(), "seule la carte suivante compte deux fois");
        assertEquals(3, controller.getCardsPlayedThisTurn());
    }

    @Test
    void doubleOrNothingWaitsPastACardWithAChoice() {
        GameController controller = controllerWith(card("double", new DoubleOrNothingEffect()),
            card("truque", new RiggedReelEffect()), card("gains", new GainEffect(500)));
        controller.playCard(inHand(controller, "double"));
        controller.playCard(inHand(controller, "truque"));
        controller.rigReel(Symbol.BELL);
        assertTrue(controller.isDoubleNextPending());
        controller.playCard(inHand(controller, "gains"));
        assertEquals(1000, player(controller).getGains());
    }

    @Test
    void doubleOrNothingEndsWithTheTurn() {
        GameController controller = controllerWith(card("double", new DoubleOrNothingEffect()));
        controller.playCard(inHand(controller, "double"));
        controller.spin();
        assertFalse(controller.isDoubleNextPending());
    }

    @Test
    void aDoubledBingoCountsOnce() {
        BingoEffect bingo = new BingoEffect(100);
        assertFalse(bingo.canBeDoubled());
    }

    @Test
    void overheatAddsAFourthReelAndCostsLife() {
        GameController controller = controllerWith(card("surchauffe", new OverheatEffect(10)));
        assertEquals(3, controller.getReelCount());
        controller.playCard(inHand(controller, "surchauffe"));
        assertEquals(90, player(controller).getHp());
        assertEquals(4, controller.getReelCount());
        TurnResult turn = controller.spin();
        assertEquals(4, turn.getDrawnSymbols().length);
        assertEquals(4, turn.getSymbols().length);
        assertEquals(3, controller.getReelCount(), "un seul tour");
    }

    @Test
    void doubledOverheatCostsLifeOnce() {
        GameController controller = controllerWith(card("double", new DoubleOrNothingEffect()),
            card("surchauffe", new OverheatEffect(10)));
        controller.playCard(inHand(controller, "double"));
        controller.playCard(inHand(controller, "surchauffe"));
        assertEquals(90, player(controller).getHp());
    }

    @Test
    void threeOfAKindOutOfFourReelsIsAJackpot() {
        Symbol[] four = {Symbol.BAR, Symbol.CHERRY, Symbol.BAR, Symbol.BAR};
        assertEquals(Symbol.BAR, SlotMachine.jackpotSymbol(four));
        TurnResult turn = resolve(new CombatContext(new Player("Joueur", 100, List.of()),
            new Enemy("Ennemi", 100_000)), four);
        assertTrue(turn.isJackpot());
        assertNull(SlotMachine.jackpotSymbol(new Symbol[] {Symbol.BAR, Symbol.CHERRY, Symbol.BAR, Symbol.SEVEN}));
    }

    @Test
    void aJokerAmongFourReelsJoinsTheMostFrequentSymbol() {
        Symbol[] drawn = {Symbol.BAR, Symbol.JOKER, Symbol.CHERRY, Symbol.CHERRY};
        Symbol[] resolved = new SlotMachine().resolveJokers(drawn, new SpinContext());
        assertEquals(Symbol.CHERRY, resolved[1]);
    }
}
