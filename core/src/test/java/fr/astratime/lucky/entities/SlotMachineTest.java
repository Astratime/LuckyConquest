package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.context.SpinContext;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SlotMachineTest {

    @Test
    void spinDrawsThreeSymbols() {
        Symbol[] result = new SlotMachine(new Random(42)).spin(new SpinContext());

        assertEquals(3, result.length);
        for (Symbol symbol : result) assertNotNull(symbol);
    }

    @Test
    void aHugeBoostMakesASymbolDominate() {
        SpinContext context = new SpinContext();
        context.addWeightBoost(Symbol.SEVEN, 1_000_000);
        SlotMachine machine = new SlotMachine(new Random(42));

        int sevens = 0;
        for (int i = 0; i < 100; i++) {
            for (Symbol symbol : machine.spin(context)) {
                if (symbol == Symbol.SEVEN) sevens++;
            }
        }

        assertTrue(sevens >= 299, "le symbole boosté doit sortir quasiment à chaque fois : " + sevens + "/300");
    }

    @Test
    void sameSeedGivesSameResults() {
        Symbol[] first  = new SlotMachine(new Random(7)).spin(new SpinContext());
        Symbol[] second = new SlotMachine(new Random(7)).spin(new SpinContext());

        assertArrayEquals(first, second);
    }

    @Test
    void aForcedJackpotShowsThreeTimesTheSameSymbolButNeverTheJoker() {
        SpinContext context = new SpinContext();
        context.forceJackpot();
        context.addWeightBoost(Symbol.JOKER, 1_000_000);
        SlotMachine machine = new SlotMachine(new Random(3));

        for (int i = 0; i < 50; i++) {
            Symbol[] result = machine.spin(context);
            assertEquals(result[0], result[1]);
            assertEquals(result[1], result[2]);
            assertNotEquals(Symbol.JOKER, result[0]);
        }
    }

    @Test
    void aRemovedSymbolNeverComesOut() {
        SpinContext context = new SpinContext();
        context.addWeightBoost(Symbol.SEVEN, 1_000_000);
        context.removeSymbol(Symbol.SEVEN);
        SlotMachine machine = new SlotMachine(new Random(5));

        for (int i = 0; i < 100; i++) {
            for (Symbol symbol : machine.spin(context)) assertNotEquals(Symbol.SEVEN, symbol);
        }
    }

    @Test
    void aFullMagnetAlwaysGivesAPairOrAJoker() {
        SpinContext context = new SpinContext();
        context.addPairChance(1f);
        SlotMachine machine = new SlotMachine(new Random(11));

        for (int i = 0; i < 200; i++) {
            Symbol[] s = machine.spin(context);
            boolean joker = List.of(s).contains(Symbol.JOKER);
            assertTrue(joker || s[0] == s[1] || s[1] == s[2] || s[0] == s[2], List.of(s).toString());
        }
    }

    @Test
    void aJokerCompletesTwoIdenticalSymbolsIntoAJackpot() {
        Symbol[] resolved = new SlotMachine(new Random(1))
            .resolveJokers(new Symbol[] { Symbol.BELL, Symbol.JOKER, Symbol.BELL }, new SpinContext());

        assertArrayEquals(new Symbol[] { Symbol.BELL, Symbol.BELL, Symbol.BELL }, resolved);
    }

    @Test
    void twoJokersTakeTheValueOfTheThirdSymbol() {
        Symbol[] resolved = new SlotMachine(new Random(1))
            .resolveJokers(new Symbol[] { Symbol.JOKER, Symbol.GRAPE, Symbol.JOKER }, new SpinContext());

        assertArrayEquals(new Symbol[] { Symbol.GRAPE, Symbol.GRAPE, Symbol.GRAPE }, resolved);
    }

    @Test
    void aJokerBetweenTwoDifferentSymbolsMakesAPairWithOneOfThem() {
        Symbol[] resolved = new SlotMachine(new Random(1))
            .resolveJokers(new Symbol[] { Symbol.BELL, Symbol.JOKER, Symbol.SEVEN }, new SpinContext());

        assertTrue(resolved[1] == Symbol.BELL || resolved[1] == Symbol.SEVEN);
        assertEquals(Symbol.BELL, resolved[0]);
        assertEquals(Symbol.SEVEN, resolved[2]);
    }

    @Test
    void threeJokersBecomeAJackpotOfAnAvailableSymbol() {
        SpinContext context = new SpinContext();
        for (Symbol symbol : Symbol.values()) {
            if (symbol != Symbol.GOLD_BAR) context.removeSymbol(symbol);
        }
        Symbol[] resolved = new SlotMachine(new Random(1))
            .resolveJokers(new Symbol[] { Symbol.JOKER, Symbol.JOKER, Symbol.JOKER }, context);

        assertArrayEquals(new Symbol[] { Symbol.GOLD_BAR, Symbol.GOLD_BAR, Symbol.GOLD_BAR }, resolved);
    }

    @Test
    void symbolsWithoutJokerAreKeptAsDrawn() {
        Symbol[] drawn = { Symbol.BELL, Symbol.SEVEN, Symbol.GRAPE };

        assertArrayEquals(drawn, new SlotMachine(new Random(1)).resolveJokers(drawn, new SpinContext()));
    }
}
