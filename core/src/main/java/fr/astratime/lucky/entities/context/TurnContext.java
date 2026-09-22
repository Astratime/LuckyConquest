package fr.astratime.lucky.entities.context;

import fr.astratime.lucky.entities.events.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Agrège les deux contextes du tour : spin et combat.
 * Construit par PreparationResolver en début de résolution,
 * à partir des effets de cartes joués par le joueur.
 */
public class TurnContext {

    private final SpinContext   spinContext;
    private final CombatContext combatContext;

    /** Événements produits par les effets de cartes en phase 1 (ex : symbole boosté). */
    private final List<Event> events = new ArrayList<>();

    /**
     * @param spinContext   modificateurs de probabilité pour le spin de ce tour
     * @param combatContext modificateurs de combat pour ce tour
     */
    public TurnContext(SpinContext spinContext, CombatContext combatContext) {
        this.spinContext   = spinContext;
        this.combatContext = combatContext;
    }

    /** @return le contexte de spin de ce tour. */
    public SpinContext   getSpinContext()   { return spinContext; }
    /** @return le contexte de combat de ce tour. */
    public CombatContext getCombatContext() { return combatContext; }

    /** Enregistre un événement survenu pendant l'application des effets de cartes (phase 1). */
    public void addEvent(Event event) { events.add(event); }
    /** @return les événements de phase 1, à fusionner avec ceux du combat pour le journal du tour. */
    public List<Event> getEvents() { return events; }
}
