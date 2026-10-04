package fr.astratime.lucky.entities.choices;

/**
 * Rouleau truqué : le joueur choisit le symbole du rouleau du milieu (voir
 * GameController#rigReel). Les options sont celles du Pari
 * (GameController#getBetOptions).
 */
public record RiggedReelChoice() implements CardChoice { }
