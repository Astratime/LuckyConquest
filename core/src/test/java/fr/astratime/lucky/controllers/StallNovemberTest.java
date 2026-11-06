package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.SlotMachine;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.effects.BingoEffect;
import fr.astratime.lucky.entities.effects.BountyEffect;
import fr.astratime.lucky.entities.effects.DealerHandEffect;
import fr.astratime.lucky.entities.effects.Effect;
import fr.astratime.lucky.entities.effects.LuckyStrikeEffect;
import fr.astratime.lucky.entities.events.BountyEvent;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.Event;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Échoppe de novembre 2026 (choix d'Astra) : rouleaux achetés ou retirés le temps
 * du combat, et les cartes Rouleau doré, Main du croupier, Prime de chasse et
 * Coup de chance.
 */
class StallNovemberTest {

    private static Card card(String id, Effect... effects) {
        return new Card(id, id, id + ".png", List.of(effects), null, 1);
    }

    private static List<Card> plainCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) cards.add(card("c" + i));
        return cards;
    }

    private static GameController controllerWith(List<Card> deck) {
        return new GameController(() -> new ArrayList<>(deck));
    }

    private static Player player(GameController controller) {
        return controller.getGameState().getPlayer();
    }

    private static Card inHand(GameController controller, String id) {
        return player(controller).getCurrentHand().stream().filter(c -> c.getId().equals(id)).findFirst().orElseThrow();
    }

    private static int drawn(fr.astratime.lucky.entities.CardPlayResult result) {
        return result.getDrawResult().getAddedToHand().size() + result.getDrawResult().getDiscarded().size();
    }

    private static void setGameState(GameController controller, GameState state) throws Exception {
        Field field = GameController.class.getDeclaredField("gameState");
        field.setAccessible(true);
        field.set(controller, state);
    }

    // ----- Rouleaux -----

    @Test
    void theStallSellsThreeReelsMissingFromTheMachineAndTheirPriceDoubles() {
        GameController controller = controllerWith(plainCards(20));
        List<ReelStall.ReelOffer> offers = controller.getReelOffers();
        assertEquals(ReelStall.OFFERS, offers.size());
        for (ReelStall.ReelOffer offer : offers) {
            assertFalse(Symbol.classicReels().contains(offer.symbol()), "seulement des rouleaux absents de la machine");
            assertNotEquals(Symbol.JOKER, offer.symbol());
            assertFalse(offer.symbol().isSummoned());
            assertEquals(30 * 100, offer.price(), "30 prix du tirage (Prairie)");
        }
        Symbol bought = offers.get(0).symbol();
        assertFalse(controller.buyReel(bought), "pas assez de gains");
        player(controller).addGains(10_000);
        assertTrue(controller.buyReel(bought));
        assertEquals(7_000, player(controller).getGains());
        assertTrue(player(controller).getSlotMachine().getReels().contains(bought));
        assertEquals(12, player(controller).getSlotMachine().getReels().size());
        assertEquals(2, controller.getReelOffers().size(), "le rouleau acheté n'est plus en vente");
        assertEquals(6_000, controller.getReelOffers().get(0).price(), "le prix double");
        assertNotNull(controller.reelBuyProblem(bought));
    }

    @Test
    void theMachineGoesNoHigherThanFourteenReels() {
        GameController controller = controllerWith(plainCards(20));
        SlotMachine machine = player(controller).getSlotMachine();
        for (Symbol symbol : ReelStall.allReels()) {
            if (machine.getReels().size() >= ReelStall.MAX_REELS - 1) break;
            if (controller.getReelOffers().stream().noneMatch(o -> o.symbol() == symbol)) machine.addReel(symbol);
        }
        player(controller).addGains(1_000_000);
        Symbol offered = controller.getReelOffers().get(0).symbol();
        assertTrue(controller.buyReel(offered), "13 -> 14 rouleaux");
        Symbol next = controller.getReelOffers().get(0).symbol();
        assertNotNull(controller.reelBuyProblem(next), "14 rouleaux : plus d'achat");
        assertFalse(controller.buyReel(next));
    }

    @Test
    void aReelCanBeRemovedDownToEightReels() {
        GameController controller = controllerWith(plainCards(20));
        player(controller).addGains(1_000_000);
        assertEquals(15 * 100, controller.getReelRemovePrice());
        assertTrue(controller.removeReel(Symbol.CHERRY));
        assertFalse(player(controller).getSlotMachine().getReels().contains(Symbol.CHERRY));
        assertEquals(3_000, controller.getReelRemovePrice(), "le prix double");
        assertTrue(controller.removeReel(Symbol.BAR));
        assertTrue(controller.removeReel(Symbol.BELL));
        assertEquals(ReelStall.MIN_REELS, player(controller).getSlotMachine().getReels().size());
        assertNotNull(controller.reelRemoveProblem(Symbol.GRAPE), "8 rouleaux : plus de retrait");
        assertFalse(controller.removeReel(Symbol.GRAPE));
        assertNotNull(controller.reelRemoveProblem(Symbol.CHERRY), "déjà retiré");
    }

    @Test
    void theMachineComesBackAtTheNextFight() {
        GameController controller = controllerWith(plainCards(20));
        player(controller).addGains(1_000_000);
        Symbol bought = controller.getReelOffers().get(0).symbol();
        controller.buyReel(bought);
        controller.removeReel(Symbol.CHERRY);
        Player next = player(controller).nextCombat();
        assertEquals(Symbol.classicReels(), next.getSlotMachine().getReels(), "seulement le temps du combat");
    }

    @Test
    void aBingoOfARemovedReelCannotBePlayed() {
        GameController controller = controllerWith(List.of(card("bingo_cherry", new BingoEffect(1, Symbol.CHERRY)),
            card("a"), card("b"), card("c"), card("d"), card("e")));
        controller.drawCards();
        player(controller).addGains(1_000_000);
        Card bingo = inHand(controller, "bingo_cherry");
        assertNull(controller.unplayableReason(bingo));
        controller.removeReel(Symbol.CHERRY);
        assertNotNull(controller.unplayableReason(bingo));
    }

    // ----- Cartes -----

    @Test
    void theGoldenReelTriplesTheMiddleSymbolOnly() {
        Player player = new Player("Joueur", 100, List.of());
        CombatContext context = new CombatContext(player, new Enemy("Ennemi", 1_000_000));
        context.multiplySlotPower(1, 3);
        Symbol[] symbols = {Symbol.TRIPLE_SEVEN, Symbol.TRIPLE_SEVEN, Symbol.SEVEN};
        TurnResult turn = new CombatResolver().resolve(context, new ActionResolver().resolve(symbols), symbols,
            symbols, List.of());
        List<Long> raw = new ArrayList<>();
        turn.getSymbolOutcomes().forEach(outcome -> outcome.getEvents().stream()
            .filter(EnemyDamagedEvent.class::isInstance).map(EnemyDamagedEvent.class::cast)
            .forEach(hit -> raw.add(hit.rawDamage)));
        assertEquals(3, raw.size());
        assertEquals(raw.get(0) * 3, raw.get(1), "le rouleau du milieu vaut x3");
        assertEquals(1, context.getSymbolPower(), "hors résolution d'un symbole, pas de facteur de rouleau");
    }

    @Test
    void afterTheDealersHandEachCardPlayedDrawsACard() {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 20; i++) deck.add(card("hand", new DealerHandEffect()));
        GameController controller = controllerWith(deck);
        controller.drawCards();
        Player player = player(controller);
        player.getLastingEffects().addBonusPlays(2);
        assertEquals(0, drawn(controller.playCard(player.getCurrentHand().get(0))), "elle-même ne fait pas piocher");
        assertEquals(1, drawn(controller.playCard(player.getCurrentHand().get(0))), "la carte suivante fait piocher");
        assertEquals(2, drawn(controller.playCard(player.getCurrentHand().get(0))), "deux Mains : 2 cartes par carte");
        controller.spin();
        controller.drawCards();
        assertEquals(0, drawn(controller.playCard(player.getCurrentHand().get(0))), "seulement jusqu'à la fin du tour");
    }

    @Test
    void theBountyCostsTwentyPercentOfGainsAndDoublesTheTurnGainsWhenTheEnemyFalls() throws Exception {
        GameController controller = controllerWith(List.of(card("bounty", new BountyEffect(20)),
            card("bingo", new BingoEffect(5, Symbol.TRIPLE_SEVEN)), card("a"), card("b"), card("c")));
        Player player = player(controller);
        setGameState(controller, new GameState(player, new Enemy("Cible", 10)));
        controller.drawCards();
        player.addGains(10_000);
        controller.playCard(inHand(controller, "bounty"));
        assertEquals(8_000, player.getGains(), "20 % des gains consommés");
        controller.playCard(inHand(controller, "bingo"));
        TurnResult turn = controller.spin();
        assertTrue(controller.getGameState().getEnemy().isDefeated());
        BountyEvent bounty = turn.getEvents().stream().filter(BountyEvent.class::isInstance)
            .map(BountyEvent.class::cast).findFirst().orElseThrow();
        assertTrue(bounty.amount > 0);
        assertEquals(8_000 + 2 * bounty.amount, player.getGains(), 0.5, "les gains du tour, doublés");
    }

    @Test
    void noBountyIfTheEnemySurvives() throws Exception {
        GameController controller = controllerWith(List.of(card("bounty", new BountyEffect(20)),
            card("bingo", new BingoEffect(5, Symbol.BELL)), card("a"), card("b"), card("c")));
        Player player = player(controller);
        setGameState(controller, new GameState(player, new Enemy("Cible", Integer.MAX_VALUE)));
        controller.drawCards();
        player.addGains(10_000);
        controller.playCard(inHand(controller, "bounty"));
        controller.playCard(inHand(controller, "bingo"));
        TurnResult turn = controller.spin();
        List<Event> bounties = turn.getEvents().stream().filter(BountyEvent.class::isInstance).toList();
        assertTrue(bounties.isEmpty());
    }

    @Test
    void theLuckyStrikeMakesTheJokerThreeTimesMoreLikely() {
        LuckyStrikeEffect effect = new LuckyStrikeEffect(3);
        fr.astratime.lucky.entities.context.TurnContext context = new fr.astratime.lucky.entities.context.TurnContext(
            new fr.astratime.lucky.entities.context.SpinContext(), new CombatContext(new Player("Joueur", 100, List.of()), new Enemy("Ennemi", 100)));
        effect.apply(context);
        assertEquals(SlotMachine.JOKER_WEIGHT * 2, context.getSpinContext().getWeightBoost(Symbol.JOKER),
            "poids 4 -> 12 : trois fois plus souvent");
    }
}
