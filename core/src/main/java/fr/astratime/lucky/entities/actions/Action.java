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

    /**
     * Applique l'effet de l'action au combat en cours.
     *
     * @param context contexte de combat (joueur, ennemi, modificateurs des cartes)
     * @return les événements générés par cette action, pour le journal du tour
     */
    public abstract List<Event> resolve(CombatContext context);

    /** @return une courte description de l'action, affichée en infobulle. */
    public abstract String getDescription();
}
