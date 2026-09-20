package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.actions.Action;

/**
 * Associe un symbole tiré à l'action qu'il produit (voir ActionResolver).
 * Permet à CombatResolver de rattacher les événements résultants (après
 * application des bonus de cartes) au symbole qui les a déclenchés.
 */
public class SymbolAction {

    private final Symbol symbol;
    private final Action action;

    public SymbolAction(Symbol symbol, Action action) {
        this.symbol = symbol;
        this.action = action;
    }

    public Symbol getSymbol() { return symbol; }
    public Action getAction() { return action; }
}
