package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.GameState;

/**
 * Définit l'effet d'un symbole lorsqu'il apparaît dans le résultat du spin.
 * Miroir de CardEffect pour les symboles : même principe, contexte différent.
 * count = nombre de fois que ce symbole apparaît dans le résultat (1, 2 ou 3).
 */
public abstract class Action {
    public abstract void apply(GameState state, int count);

    public abstract String getDescription();
}
