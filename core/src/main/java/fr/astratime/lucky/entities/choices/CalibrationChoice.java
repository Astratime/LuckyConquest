package fr.astratime.lucky.entities.choices;

/**
 * Calibrage : le joueur choisit un symbole qui quitte ses rouleaux pendant
 * {@code turns} tirages (voir GameController#calibrate). Les options sont
 * celles du Pari (GameController#getBetOptions).
 *
 * @param turns tirages sans le symbole choisi, celui-ci compris
 */
public record CalibrationChoice(int turns) implements CardChoice { }
