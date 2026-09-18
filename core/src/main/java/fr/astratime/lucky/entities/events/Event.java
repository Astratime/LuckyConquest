package fr.astratime.lucky.entities.events;

/**
 * Représente un événement survenu pendant la résolution d'un tour.
 * TurnResult est un journal d'événements — GameScreen les lit pour l'affichage.
 * Cette structure prépare le terrain pour les animations futures.
 */
public abstract class Event {
    /** @return une description textuelle de l'événement, destinée au journal/log affiché en jeu. */
    public abstract String describe();
}
