package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.actions.AttackAction;
import fr.astratime.lucky.entities.actions.DefenseAction;
import fr.astratime.lucky.entities.actions.GainAction;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActionResolverTest {

    private final ActionResolver resolver = new ActionResolver();

    @Test
    void mapsEachNonNullSymbolToItsRegisteredAction() {
        Symbol[] symbols = { Symbol.SEVEN, null, Symbol.GRAPE };

        List<SymbolAction> symbolActions = resolver.resolve(symbols);

        assertEquals(2, symbolActions.size(), "le symbole null ne doit produire aucune action");
        assertEquals(Symbol.SEVEN, symbolActions.get(0).getSymbol());
        assertInstanceOf(AttackAction.class, symbolActions.get(0).getAction());
        assertEquals(Symbol.GRAPE, symbolActions.get(1).getSymbol());
        assertInstanceOf(DefenseAction.class, symbolActions.get(1).getAction());
    }

    @Test
    void sameSymbolAppearingTwiceProducesTwoIndependentEntries() {
        Symbol[] symbols = { Symbol.BELL, Symbol.BELL, null };

        List<SymbolAction> symbolActions = resolver.resolve(symbols);

        assertEquals(2, symbolActions.size(), "deux BELL doivent produire deux entrées à résoudre séparément");
        assertInstanceOf(GainAction.class, symbolActions.get(0).getAction());
        assertInstanceOf(GainAction.class, symbolActions.get(1).getAction());
    }

    @Test
    void allNullSymbolsProduceNoAction() {
        Symbol[] symbols = { null, null, null };

        assertTrue(resolver.resolve(symbols).isEmpty());
    }
}
