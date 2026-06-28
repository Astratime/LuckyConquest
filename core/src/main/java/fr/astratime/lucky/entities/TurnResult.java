package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.JackpotEvent;

import java.util.List;

/**
 * Journal d'événements produit par CombatResolver à la fin d'un tour.
 * GameScreen lit ce journal pour mettre à jour l'affichage.
 * Contient également les symboles tirés (pour affichage) et le score gagné.
 * La structure List<Event> prépare le terrain pour les animations futures
 * sans que l'UI ait à les consommer immédiatement.
 */
public class TurnResult {

    private final List<Event> events;
    private final Symbol[]    symbols;
    private final int         scoreGained;

    public TurnResult(List<Event> events, Symbol[] symbols, int scoreGained) {
        this.events     = List.copyOf(events);
        this.symbols    = symbols.clone();
        this.scoreGained = scoreGained;
    }

    public List<Event> getEvents()     { return events; }
    public Symbol[]    getSymbols()    { return symbols.clone(); }
    public int         getScoreGained() { return scoreGained; }

    // -- Raccourcis pour l'affichage -------------------------------------------

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
