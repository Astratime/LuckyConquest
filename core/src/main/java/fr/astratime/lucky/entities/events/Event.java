package fr.astratime.lucky.entities.events;

/**
 * Représente un événement survenu pendant la résolution d'un tour.
 * TurnResult est un journal d'événements — GameScreen les lit pour l'affichage.
 * Cette structure prépare le terrain pour les animations futures.
 */
public abstract class Event {
    public abstract String describe();
}
