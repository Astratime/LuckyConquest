package fr.astratime.lucky.entities;

/**
 * Interface implémentée par tous les effets de carte.
 * Chaque effet sait s'appliquer sur le GameState et se décrire en une phrase.
 * Ajouter un nouvel effet = créer une classe qui implémente cette interface.
 */
public interface CardEffect {
    void apply(GameState state);
    String getDescription();
}
