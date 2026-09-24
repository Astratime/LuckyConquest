package fr.astratime.lucky.entities.choices;

/**
 * Pari : le joueur choisit le symbole sur lequel il parie (voir
 * GameController#placeBet). Les options (symboles pouvant sortir ce tour)
 * sont fournies par GameController#getBetOptions.
 */
public record BetChoice() implements CardChoice { }
