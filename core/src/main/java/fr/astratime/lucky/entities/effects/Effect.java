package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.GameState;

/**
 * Interface implémentée par tous les effets de carte.
 * Chaque effet sait s'appliquer sur le GameState et se décrire en une phrase.
 * Ajouter un nouvel effet = créer une classe qui implémente cette interface.
 */
public abstract class Effect {
    public abstract void apply(GameState state);
    public abstract String getDescription();
}
