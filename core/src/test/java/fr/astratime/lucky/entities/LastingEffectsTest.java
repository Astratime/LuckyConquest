package fr.astratime.lucky.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LastingEffectsTest {

    @Test
    void aRemovedSymbolComesBackAfterItsTurns() {
        LastingEffects effects = new LastingEffects();
        effects.removeSymbol(Symbol.SEVEN, 3);

        effects.endTurn();
        effects.endTurn();
        assertEquals(1, effects.getRemovedSymbols().get(Symbol.SEVEN));

        effects.endTurn();
        assertFalse(effects.getRemovedSymbols().containsKey(Symbol.SEVEN));
        assertTrue(effects.isEmpty());
    }

    @Test
    void gainBonusesAddUpAndLastTheWholeCombat() {
        LastingEffects effects = new LastingEffects();
        effects.addGainBonus(0.5f);
        effects.addGainBonus(0.5f);

        for (int i = 0; i < 10; i++) effects.endTurn();

        assertEquals(1f, effects.getGainBonus(), 1e-6);
    }
}
