package fr.astratime.lucky.entities.context;

import fr.astratime.lucky.entities.events.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Agrège les deux contextes du tour : spin et combat.
 * Construit par PreparationResolver en début de résolution,
 * à partir des effets de cartes joués par le joueur.
 * drawCount : nombre de cartes à piocher au prochain tour (modifiable par effets),
 * initialisé à baseDrawCount — la valeur de référence possédée par GameController,
 * pour ne pas dupliquer cette constante à deux endroits différents.
 */
public class TurnContext {

    private int drawCount;

    private final SpinContext   spinContext;
    private final CombatContext combatContext;

    /** Événements produits par les effets de cartes en phase 1 (ex : symbole boosté). */
    private final List<Event> events = new ArrayList<>();

    /**
     * @param baseDrawCount nombre de cartes piochées par défaut, avant bonus des effets de ce tour
     * @param spinContext   modificateurs de probabilité pour le spin de ce tour
     * @param combatContext modificateurs de combat pour ce tour
     */
    public TurnContext(int baseDrawCount, SpinContext spinContext, CombatContext combatContext) {
        this.drawCount     = baseDrawCount;
        this.spinContext   = spinContext;
        this.combatContext = combatContext;
    }

    /** @return le contexte de spin de ce tour. */
    public SpinContext   getSpinContext()   { return spinContext; }
    /** @return le contexte de combat de ce tour. */
    public CombatContext getCombatContext() { return combatContext; }
    /** @return le nombre de cartes à piocher au prochain tour. */
    public int           getDrawCount()    { return drawCount; }
    /** Ajoute {@code extra} cartes au nombre de cartes à piocher au prochain tour. */
    public void          addDrawCount(int extra) { drawCount += extra; }

    /** Enregistre un événement survenu pendant l'application des effets de cartes (phase 1). */
    public void addEvent(Event event) { events.add(event); }
    /** @return les événements de phase 1, à fusionner avec ceux du combat pour le journal du tour. */
    public List<Event> getEvents() { return events; }
}
