package fr.astratime.lucky.entities.choices;

/**
 * Atelier du Joker : le joueur choisit ce que vaut le Joker à ce tirage (voir
 * GameController#shapeJoker). Les options sont celles du Pari
 * (GameController#getBetOptions).
 */
public record JokerWorkshopChoice() implements CardChoice { }
