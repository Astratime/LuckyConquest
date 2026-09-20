package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.events.Event;

import java.util.List;

/**
 * Résultat de la résolution d'un symbole tiré : le symbole lui-même et les
 * événements produits par son action une fois les bonus de cartes du tour
 * appliqués (CombatContext). Sert notamment à journaliser, pour chaque
 * symbole, son effet de base (voir {@link Symbol#getDescription()}) à côté
 * de l'effet réellement obtenu ce tour.
 */
public class SymbolOutcome {

    private final Symbol symbol;
    private final List<Event> events;

    public SymbolOutcome(Symbol symbol, List<Event> events) {
        this.symbol = symbol;
        this.events = List.copyOf(events);
    }

    /** @return le symbole tiré. */
    public Symbol getSymbol() { return symbol; }
    /** @return les événements produits par l'action de ce symbole (liste immuable, peut être vide). */
    public List<Event> getEvents() { return events; }
}
