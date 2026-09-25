package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolRegistry;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.effects.AceOfDiamondsEffect;
import fr.astratime.lucky.entities.effects.AceOfHeartsEffect;
import fr.astratime.lucky.entities.effects.AceOfSpadesEffect;
import fr.astratime.lucky.entities.effects.CorruptionEffect;
import fr.astratime.lucky.entities.effects.Effect;
import fr.astratime.lucky.entities.effects.GainsMultiplierEffect;
import fr.astratime.lucky.entities.effects.SpadeIgnoreDefenseEffect;
import fr.astratime.lucky.entities.events.CorruptionEvent;
import fr.astratime.lucky.entities.events.CounterAttackEvent;
import fr.astratime.lucky.entities.events.GaugeFilledEvent;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Jauges des couleurs (Lames, Sang, Coffre), leurs As, la Corruption et l'échoppe. */
class SuitGaugesTest {

    private final Player player = new Player("Joueur", 100, List.of());
    private final Enemy  enemy  = new Enemy("Ennemi", 1_000_000);
    private final LastingEffects lasting = player.getLastingEffects();

    /** Joue les effets (pose) puis prépare le tour, comme GameController et TurnEngine. */
    private TurnContext prepare(Effect... effects) {
        List<Effect> pending = new ArrayList<>();
        for (Effect effect : effects) {
            PlayContext play = new PlayContext(player);
            effect.onPlay(play);
            pending.addAll(play.getEffectsForSpin());
            if (play.getGains() != 0) player.addGains(play.getGains());
        }
        return new PreparationResolver().resolve(pending, player, enemy);
    }

    private static SymbolAction action(Symbol symbol, int slot) {
        return new SymbolAction(symbol, slot, SymbolRegistry.getAction(symbol).orElseThrow());
    }

    @Test
    void spadesAddBladesThatBoostEveryAttackForTheCombat() {
        prepare(new SpadeIgnoreDefenseEffect(113, 3));
        assertEquals(3, lasting.getBlades());

        TurnContext nextTurn = prepare();
        assertEquals(3 * PreparationResolver.BLADE_ATTACK, nextTurn.getCombatContext().getAttackBonus(),
            "les Lames restent d'un tour à l'autre");
    }

    @Test
    void aceOfSpadesCashesInTheBlades() {
        lasting.addBlades(4);
        player.addGains(1000); // 10 % = 100 : pas assez pour une Lame de plus

        CombatContext combat = prepare(new AceOfSpadesEffect()).getCombatContext();

        assertEquals(0, lasting.getBlades());
        assertEquals(3f, combat.getAttackFactor(), 1e-6, "1 + 0,5 x 4 Lames");
        assertEquals(4 * PreparationResolver.BLADE_ATTACK, combat.getAttackBonus(), "les Lames tranchent une dernière fois");
        assertTrue(combat.isIgnoreDefense());
        assertEquals(900, player.getGains());
    }

    @Test
    void overhealFillsTheBloodThatTheAceOfHeartsTurnsIntoAttack() {
        CombatContext combat = new CombatContext(player, enemy);
        combat.addLifeDrainPercent(50);
        List<?> events = SymbolRegistry.getAction(Symbol.TRIPLE_SEVEN).orElseThrow().resolve(combat); // 60 dégâts

        assertEquals(30, lasting.getBlood(), "joueur à pleine vie : tout le soin part dans le Sang");
        assertTrue(events.stream().anyMatch(e -> e instanceof GaugeFilledEvent));

        player.takeDamage(50);
        CombatContext frenzy = prepare(new AceOfHeartsEffect()).getCombatContext();
        assertEquals(2f, frenzy.getAttackFactor(), 1e-6, "1 + 2 x 50 % de vie manquante");
        assertEquals(30, frenzy.getAttackBonus());
        assertEquals(0, lasting.getBlood());
    }

    @Test
    void leftoverShieldFillsTheVaultThatTheAceOfDiamondsUnleashes() {
        CombatContext combat = new CombatContext(player, enemy);
        Symbol[] symbols = { Symbol.DIAMOND, null, null };
        new CombatResolver().resolve(combat, List.of(action(Symbol.DIAMOND, 0)), symbols, List.of());
        assertEquals(20 - 10, lasting.getVault(), "bouclier 20, riposte 10 : il reste 10 pour le Coffre");

        lasting.addVault(90);
        TurnContext turn = prepare(new AceOfDiamondsEffect());
        TurnResult result = new CombatResolver().resolve(turn.getCombatContext(), List.of(),
            new Symbol[] { null, null, null }, List.of());

        CounterAttackEvent counter = (CounterAttackEvent) result.getPistolEvents().stream()
            .filter(e -> e instanceof CounterAttackEvent).findFirst().orElseThrow();
        assertEquals(100 * 3, counter.damage);
    }

    @Test
    void corruptionLastsThreeTurnsAndCostsTenPercentEachTurn() {
        player.addGains(1000);
        prepare(new CorruptionEffect(3)); // tour 1 : celui où la carte est jouée
        lasting.endTurn();

        for (int turn = 2; turn <= 3; turn++) {
            TurnContext context = new PreparationResolver().resolve(List.of(), player, enemy);
            assertEquals(CorruptionEffect.FACTOR, context.getCombatContext().getAttackFactor(), 1e-6);
            assertEquals(CorruptionEffect.FACTOR, context.getCombatContext().getDefenseFactor(), 1e-6);
            assertTrue(context.getEvents().stream().anyMatch(e -> e instanceof CorruptionEvent));
            lasting.endTurn();
        }
        assertEquals(729, player.getGains(), "1000 x 0,9 x 0,9 x 0,9");
        assertEquals(1f, new PreparationResolver().resolve(List.of(), player, enemy)
            .getCombatContext().getAttackFactor(), 1e-6, "la Corruption est terminée");
    }

    @Test
    void potDeLutinDoublesTheGauges() {
        lasting.addBlades(2);
        lasting.addBlood(10);
        lasting.addVault(30);

        prepare(new GainsMultiplierEffect(5, 2));

        assertEquals(4, lasting.getBlades());
        assertEquals(20, lasting.getBlood());
        assertEquals(60, lasting.getVault());
    }

    // --- Échoppe -----------------------------------------------------------

    private static Card corruption() {
        return new Card("corruption", "Corruption", "x.png", List.of(new CorruptionEffect(3)), null, 1);
    }

    private static GameController shopController(int deckSize) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < deckSize; i++) deck.add(new Card("c" + i, "c" + i, "x.png", List.of(), null, 1));
        return new GameController(() -> new ArrayList<>(deck), id -> corruption(), Map.of("corruption", 2000));
    }

    @Test
    void buyingNeedsEnoughGains() {
        GameController controller = shopController(10);
        controller.getGameState().getPlayer().addGains(1999);

        assertNull(controller.buy(controller.getShopOffers().get(0)));
        assertEquals(1999, controller.getGameState().getPlayer().getGains());
    }

    @Test
    void aBoughtCardLandsOnTheTableAndStaysForTheNextDraw() {
        GameController controller = shopController(10);
        Player buyer = controller.getGameState().getPlayer();
        buyer.addGains(2500);

        GameController.Purchase purchase = controller.buy(controller.getShopOffers().get(0));

        assertTrue(purchase.addedToHand());
        assertEquals(500, buyer.getGains());
        controller.drawCards();
        assertTrue(buyer.getCurrentHand().contains(purchase.card()), "la pioche ne défausse pas la carte achetée");
        assertEquals(1 + GameController.DEFAULT_DRAW_COUNT, buyer.getCurrentHand().size());
    }

    @Test
    void aBoughtCardGoesToTheDeckWhenTheTableIsFull() {
        GameController controller = shopController(20);
        Player buyer = controller.getGameState().getPlayer();
        buyer.draw(Player.MAX_HAND_SIZE);
        buyer.addGains(2000);

        GameController.Purchase purchase = controller.buy(controller.getShopOffers().get(0));

        assertFalse(purchase.addedToHand());
        assertTrue(buyer.getDeck().getCards().contains(purchase.card()));
    }
}
