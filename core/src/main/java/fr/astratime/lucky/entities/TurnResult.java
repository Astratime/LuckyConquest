package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
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

    private final List<Event> events;
    private final Symbol[]    symbols;
    private final int         gainsFromPairOrJackpot;

    public TurnResult(List<Event> events, Symbol[] symbols, int gainsFromPairOrJackpot) {
        this.events  = List.copyOf(events);
        this.symbols = symbols.clone();
        this.gainsFromPairOrJackpot = gainsFromPairOrJackpot;
    }

    public List<Event> getEvents()  { return events; }
    public Symbol[]    getSymbols() { return symbols.clone(); }
    public int         getGainsFromPairOrJackpot() { return gainsFromPairOrJackpot; }

    public int getTotalDamage() {
        return events.stream()
            .filter(e -> e instanceof EnemyDamagedEvent)
            .mapToInt(e -> ((EnemyDamagedEvent) e).damage)
            .sum();
    }

    public boolean isJackpot() {
        return events.stream().anyMatch(e -> e instanceof JackpotEvent);
    }
}
