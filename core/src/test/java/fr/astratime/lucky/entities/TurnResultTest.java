package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.events.JackpotEvent;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TurnResultTest {

    private static TurnResult result(Symbol a, Symbol b, Symbol c, boolean jackpot) {
        return new TurnResult(jackpot ? List.of(new JackpotEvent()) : List.of(), new Symbol[] { a, b, c }, 0,
            List.of(), List.of(), List.of());
    }

    @Test
    void jackpotIsNotAPair() {
        TurnResult result = result(Symbol.SEVEN, Symbol.SEVEN, Symbol.SEVEN, true);

        assertTrue(result.isJackpot());
        assertFalse(result.isPair());
    }

    @Test
    void anyTwoIdenticalSymbolsMakeAPair() {
        assertTrue(result(Symbol.SEVEN, Symbol.SEVEN, Symbol.BAR, false).isPair());
        assertTrue(result(Symbol.BAR, Symbol.SEVEN, Symbol.SEVEN, false).isPair());
        assertTrue(result(Symbol.SEVEN, Symbol.BAR, Symbol.SEVEN, false).isPair());
    }

    @Test
    void threeDifferentSymbolsAreNeitherPairNorJackpot() {
        TurnResult result = result(Symbol.SEVEN, Symbol.BAR, Symbol.BELL, false);

        assertFalse(result.isPair());
        assertFalse(result.isJackpot());
    }

    @Test
    void symbolsAreCopied() {
        TurnResult result = result(Symbol.SEVEN, Symbol.BAR, Symbol.BELL, false);

        result.getSymbols()[0] = Symbol.GRAPE;

        assertEquals(Symbol.SEVEN, result.getSymbols()[0], "modifier la copie ne doit pas changer le résultat");
    }
}
