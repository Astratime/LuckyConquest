package fr.astratime.lucky.entities.choices;

/**
 * Choix demandé au joueur par une carte qu'il vient de jouer (ex : le symbole
 * du Pari). Le tour est suspendu jusqu'à sa réponse, transmise à GameController.
 */
public sealed interface CardChoice permits BetChoice, RouletteChoice { }
