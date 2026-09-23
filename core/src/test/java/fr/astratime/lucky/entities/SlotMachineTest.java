package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.context.SpinContext;

import org.junit.jupiter.api.Test;

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
}
