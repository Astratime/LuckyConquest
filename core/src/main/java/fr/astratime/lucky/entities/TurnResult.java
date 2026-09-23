package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.JackpotEvent;

import java.util.List;

/**
 * Journal d'événements produit par CombatResolver à la fin d'un tour.
 * GameScreen lit ce journal pour mettre à jour l'affichage.
 * gainsFromPairOrJackpot ne couvre que le bonus de paire/jackpot —
 * les gains issus de GainAction ou d'AttackAction (As de Pique) sont
 * déjà appliqués au joueur et visibles dans le journal d'événements.
 */
public class TurnResult {

    private final List<Event>         events;
    private final Symbol[]            symbols;
    private final int                 gainsFromPairOrJackpot;
    private final List<SymbolOutcome> symbolOutcomes;
    private final List<Event>         pairOrJackpotEvents;
    private final List<Event>         enemyTurnEvents;

    /**
     * @param events                 journal des événements survenus pendant le tour
     * @param symbols                symboles tirés ce tour (copiés)
     * @param gainsFromPairOrJackpot gains issus uniquement du bonus de paire/jackpot
     * @param symbolOutcomes         détail par symbole tiré : son action et les événements qu'elle a produits
     * @param pairOrJackpotEvents    événements du bonus de paire/jackpot (vide si aucun)
     * @param enemyTurnEvents        événements de la riposte de l'ennemi (vie perdue, renvoi ; vide s'il est vaincu)
     */
    public TurnResult(List<Event> events, Symbol[] symbols, int gainsFromPairOrJackpot,
                       List<SymbolOutcome> symbolOutcomes,
                       List<Event> pairOrJackpotEvents, List<Event> enemyTurnEvents) {
        this.events  = List.copyOf(events);
        this.symbols = symbols.clone();
        this.gainsFromPairOrJackpot = gainsFromPairOrJackpot;
        this.symbolOutcomes = List.copyOf(symbolOutcomes);
        this.pairOrJackpotEvents = List.copyOf(pairOrJackpotEvents);
        this.enemyTurnEvents = List.copyOf(enemyTurnEvents);
    }

    /** @return le journal d'événements du tour (liste immuable). */
    public List<Event> getEvents()  { return events; }
    /** @return une copie des symboles tirés ce tour. */
    public Symbol[]    getSymbols() { return symbols.clone(); }
    /** @return les gains issus uniquement du bonus de paire/jackpot. */
    public int         getGainsFromPairOrJackpot() { return gainsFromPairOrJackpot; }
    /** @return pour chaque symbole tiré ayant une action enregistrée, son effet de base et le résultat obtenu ce tour. */
    public List<SymbolOutcome> getSymbolOutcomes() { return symbolOutcomes; }
    /** @return les événements du bonus de paire/jackpot de ce tour (vide si aucun). */
    public List<Event> getPairOrJackpotEvents() { return pairOrJackpotEvents; }
    /** @return les événements de la riposte de l'ennemi (vide s'il a été vaincu avant de riposter). */
    public List<Event> getEnemyTurnEvents() { return enemyTurnEvents; }


    /** @return {@code true} si un {@link JackpotEvent} figure dans le journal de ce tour. */
    public boolean isJackpot() {
        return events.stream().anyMatch(e -> e instanceof JackpotEvent);
    }

    /** @return {@code true} si exactement deux des trois symboles tirés sont identiques (jackpot exclu). */
    public boolean isPair() {
        if (isJackpot() || symbols[0] == null) return false;
        return symbols[0] == symbols[1] || symbols[1] == symbols[2] || symbols[0] == symbols[2];
    }
}
