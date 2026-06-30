package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.events.Event;

import java.util.List;

/**
 * Action produite par un symbole de la machine à sous.
 * resolve() applique l'effet sur le CombatContext et retourne les événements générés.
 * Chaque Action connaît sa propre logique et lit les modificateurs dans CombatContext.
 * CombatResolver appelle resolve() sur chaque action de la liste.
 */
public abstract class Action {
    public abstract List<Event> resolve(CombatContext context);
    public abstract String getDescription();
}
