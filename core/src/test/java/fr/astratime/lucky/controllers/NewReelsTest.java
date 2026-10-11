package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.enemy.AspirationRule;
import fr.astratime.lucky.entities.enemy.EnemyCards;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySignature;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Les rouleaux ajoutés aux 7 ennemis qui n'avaient que des rouleaux de base, et l'Aspiration de la Machine Originelle. */
class NewReelsTest {

    private static Player player() { return new Player("Joueur", 1_000_000, List.of()); }

    private static Card card(Card.Suit suit, int rank) { return EnemyCards.card(suit, rank); }

    @Test
    void theSevenEnemiesHaveOwnReelsAndAnOwnCardForEach() {
        List<EnemyKind> kinds = List.of(EnemyKind.CHEF, EnemyKind.PORTIER, EnemyKind.MAISON, EnemyKind.MACHINE_ORIGINELLE,
            EnemyKind.GOLEM_OR, EnemyKind.COEUR_MONTAGNE, EnemyKind.BANDIT_MANCHOT);
        for (EnemyKind kind : kinds) {
            Set<EnemySymbol> special = EnumSet.noneOf(EnemySymbol.class);
            for (EnemySymbol symbol : kind.getSymbols()) if (!EnemyTurnResolver.isBasic(symbol)) special.add(symbol);
            assertFalse(special.isEmpty(), kind + " a des rouleaux à lui");
            for (EnemySymbol symbol : special) {
                if (symbol == EnemySymbol.RAGE) continue; // la Rage de la Machine Originelle était déjà là
                assertTrue(kind.getSignatures().stream().anyMatch(signature -> signature.getSymbol() == symbol),
                    kind + " a une carte pour " + symbol);
            }
        }
    }

    @Test
    void theMachineGivesThePhaseTwoReelBackAfterThreeTurnsButKeepsTheLastOne() {
        Enemy machine = new Enemy(EnemyKind.MACHINE_ORIGINELLE, List.of());
        machine.takeDamage(machine.getMaxHp() * 6L / 10);
        machine.resetDamageTaken();
        assertTrue(machine.enterPhaseTwo());
        assertEquals(1, machine.getStolenReels());
        assertEquals(EnemyKind.MACHINE_REELS + 1, machine.getReelCount());
        for (int turn = 0; turn < EnemySymbol.MACHINE_THEFT_TURNS; turn++) {
            assertEquals(1, machine.getStolenReels(), "tour " + turn);
            machine.tickTheft();
        }
        assertEquals(0, machine.getStolenReels(), "le rouleau revient");
        assertEquals(EnemyKind.MACHINE_REELS, machine.getReelCount());

        machine.takeDamage(machine.getHp() - machine.getMaxHp() / 20);
        machine.resetDamageTaken();
        assertTrue(machine.enterPhaseTwo());
        for (int turn = 0; turn < 10; turn++) machine.tickTheft();
        assertEquals(1, machine.getStolenReels(), "le vol de la phase 3 reste");
    }

    @Test
    void anAspirationRuleCountsEachConditionApart() {
        AspirationRule rule = new AspirationRule(AspirationRule.RULES.get(1)); // un As et 2 Carreaux
        assertFalse(rule.isMet(List.of(card(Card.Suit.PIQUE, 1), card(Card.Suit.CARREAU, 5))));
        assertTrue(rule.isMet(List.of(card(Card.Suit.CARREAU, 1), card(Card.Suit.CARREAU, 5))),
            "l'As de Carreau compte pour l'As et pour un Carreau");
        AspirationRule colors = new AspirationRule(AspirationRule.RULES.get(6));
        assertTrue(colors.isMet(List.of(card(Card.Suit.PIQUE, 2), card(Card.Suit.COEUR, 3), card(Card.Suit.TREFLE, 4))));
        assertFalse(colors.isMet(List.of(card(Card.Suit.PIQUE, 2), card(Card.Suit.PIQUE, 3), card(Card.Suit.TREFLE, 4))));
    }

    @Test
    void aGreatAspirationAddsACondition() {
        for (int seed = 0; seed < 30; seed++) {
            AspirationRule plain = AspirationRule.random(new Random(seed), false);
            AspirationRule great = AspirationRule.random(new Random(seed), true);
            int plainCards = plain.getConditions().stream().mapToInt(AspirationRule.Condition::count).sum();
            int greatCards = great.getConditions().stream().mapToInt(AspirationRule.Condition::count).sum();
            assertEquals(plainCards + 1, greatCards, "graine " + seed);
        }
    }

    @Test
    void anUnmetAspirationTakesTheRightReelThenTheLeftOne() {
        assertEquals(2, PreparationResolver.aspiredReel(Set.of()));
        assertEquals(0, PreparationResolver.aspiredReel(Set.of(2)));
        assertEquals(1, PreparationResolver.aspiredReel(Set.of(0, 2)));
        assertEquals(-1, PreparationResolver.aspiredReel(Set.of(0, 1, 2)));
    }

    @Test
    void theBouncerOnlyMeetsHalfTheShield() {
        Player player = player();
        player.addShield(100);
        int lost = player.takeDamage(80, 100 - EnemySymbol.BOUNCER_PIERCE_PERCENT);
        assertEquals(30, lost, "50 de bouclier comptent, 30 passent");
        assertEquals(50, player.getShield());
    }

    @Test
    void aLoadBearingWallKeepsTheDefenseUntilItsTurn() {
        Enemy house = new Enemy(EnemyKind.MAISON, List.of());
        house.addShieldDefense(500);
        int defense = house.getDefense();
        house.holdDefense();
        house.absorb(300);
        assertEquals(defense, house.getDefense());
        house.resetDefense();
        assertFalse(house.isDefenseSolid());
    }

    @Test
    void platingHoldsTheGoldenSkinBelowHalfHp() {
        Enemy golem = new Enemy(EnemyKind.GOLEM_OR, List.of());
        golem.takeTrueDamage(golem.getMaxHp() * 8L / 10);
        assertFalse(golem.hasGoldSkin());
        golem.plate(EnemySymbol.PLATING_TURNS);
        assertTrue(golem.hasGoldSkin());
        golem.tickPlating();
        assertFalse(golem.hasGoldSkin());
    }

    @Test
    void theIngotGrowsWithItsDefenseUpToThreeTimes() {
        Enemy golem = new Enemy(EnemyKind.GOLEM_OR, List.of());
        assertEquals(100, EnemyTurnResolver.ingotPercent(golem));
        golem.addShieldDefense(golem.getKind().getBaseDefense());
        assertEquals(200, EnemyTurnResolver.ingotPercent(golem));
        golem.addShieldDefense(golem.getKind().getBaseDefense() * 10);
        assertEquals(300, EnemyTurnResolver.ingotPercent(golem));
    }

    @Test
    void threeSwallowedCoinsBuyAJackpot() {
        Enemy bandit = new Enemy(EnemyKind.BANDIT_MANCHOT, List.of());
        bandit.swallowCoins(2);
        assertFalse(bandit.spendCoins());
        bandit.swallowCoins(1);
        assertTrue(bandit.spendCoins());
        assertEquals(0, bandit.getCoins());
    }

    @Test
    void theArmedArmRerollsTheReelFarthestFromTheJackpot() {
        EnemySymbol[] symbols = {EnemySymbol.SWORD, EnemySymbol.POTION, EnemySymbol.SWORD};
        assertEquals(1, EnemyTurnResolver.worstReel(symbols));
        EnemySymbol[] arm = {EnemySymbol.SWORD, EnemySymbol.SWORD, EnemySymbol.ARMED_ARM};
        assertEquals(2, EnemyTurnResolver.worstReel(arm), "le Bras armé se relance lui-même en dernier recours");
        EnemySymbol[] jackpot = {EnemySymbol.SHIELD, EnemySymbol.SHIELD, EnemySymbol.SHIELD};
        assertEquals(-1, EnemyTurnResolver.worstReel(jackpot));
    }

    @Test
    void theOriginalJackpotNeedsThreeMatchingSymbols() {
        assertEquals(1, EnemyTurnResolver.originalJackpot(new EnemySymbol[] {EnemySymbol.SHIELD, EnemySymbol.SWORD,
            EnemySymbol.GEAR, EnemySymbol.SWORD, EnemySymbol.SWORD}));
        assertEquals(-1, EnemyTurnResolver.originalJackpot(new EnemySymbol[] {EnemySymbol.SHIELD, EnemySymbol.SWORD,
            EnemySymbol.GEAR, EnemySymbol.SWORD, EnemySymbol.POTION}));
    }

    @Test
    void theNewReelsLeaveTheirMarkOnThePlayer() {
        // Chaque nouveau rouleau du Croupier en chef, de la Machine et du Cœur de la Montagne finit par sortir.
        Set<EnemySymbol> seen = EnumSet.noneOf(EnemySymbol.class);
        for (EnemyKind kind : List.of(EnemyKind.CHEF, EnemyKind.MACHINE_ORIGINELLE, EnemyKind.COEUR_MONTAGNE)) {
            for (int seed = 0; seed < 200; seed++) {
                Enemy enemy = new Enemy(kind);
                Player player = player();
                player.addGains(10_000);
                var turn = new EnemyTurnResolver(new Random(seed)).resolve(enemy, player, 0, 0f);
                for (EnemySymbol symbol : turn.symbols()) seen.add(symbol);
                var curses = player.getLastingEffects();
                if (List.of(turn.symbols()).contains(EnemySymbol.NOTHING_GOES)) assertFalse(curses.getForbiddenReels().isEmpty());
                if (List.of(turn.symbols()).contains(EnemySymbol.ASPIRATION)) assertFalse(curses.getAspirations().isEmpty());
                if (List.of(turn.symbols()).contains(EnemySymbol.SHORT_CIRCUIT)) assertTrue(curses.getShortCircuits() > 0);
                if (List.of(turn.symbols()).contains(EnemySymbol.ROCKFALL)) assertTrue(curses.getRockfalls() > 0);
            }
        }
        for (EnemySymbol symbol : List.of(EnemySymbol.NOTHING_GOES, EnemySymbol.RAKE, EnemySymbol.GEAR, EnemySymbol.SHORT_CIRCUIT,
                EnemySymbol.GOLDEN_COMET, EnemySymbol.ASPIRATION, EnemySymbol.ROCKFALL, EnemySymbol.HEARTBEAT)) {
            assertTrue(seen.contains(symbol), symbol + " sort");
        }
    }

    @Test
    void ownCardsOfTheNewReelsAreInTheDecks() {
        List<Card> deck = new ArrayList<>(EnemyKind.MACHINE_ORIGINELLE.createDeck());
        for (EnemySignature signature : EnemyKind.MACHINE_ORIGINELLE.getSignatures()) {
            assertEquals(EnemySignature.COPIES, deck.stream().filter(card -> EnemySignature.of(card) == signature).count());
        }
    }
}
