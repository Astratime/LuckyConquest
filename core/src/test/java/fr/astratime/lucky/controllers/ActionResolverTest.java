package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.actions.Action;
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

        List<Action> actions = resolver.resolve(symbols);

        assertEquals(2, actions.size(), "le symbole null ne doit produire aucune action");
        assertInstanceOf(AttackAction.class, actions.get(0));
        assertInstanceOf(DefenseAction.class, actions.get(1));
    }

    @Test
    void sameSymbolAppearingTwiceProducesTwoIndependentActions() {
        Symbol[] symbols = { Symbol.BELL, Symbol.BELL, null };

        List<Action> actions = resolver.resolve(symbols);

        assertEquals(2, actions.size(), "deux BELL doivent produire deux actions à résoudre séparément");
        assertInstanceOf(GainAction.class, actions.get(0));
        assertInstanceOf(GainAction.class, actions.get(1));
    }

    @Test
    void allNullSymbolsProduceNoAction() {
        Symbol[] symbols = { null, null, null };

        assertTrue(resolver.resolve(symbols).isEmpty());
    }
}
