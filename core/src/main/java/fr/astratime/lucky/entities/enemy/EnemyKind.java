package fr.astratime.lucky.entities.enemy;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Les ennemis du jeu et leur façon de jouer : points de vie, défense de base,
 * symboles de leurs rouleaux (et leur poids), composition de leur deck sombre
 * et ordre dans lequel ils jouent leurs cartes.
 * <ul>
 *   <li>{@link #ENTRAINEMENT} : le croupier du mode Entraînement, peu de PV et une Épée faible ;</li>
 *   <li>{@link #CROUPIER} : le combat de départ, équilibré ;</li>
 *   <li>{@link #GARDIEN} : défense épaisse et Épines, qui renvoient les coups ;</li>
 *   <li>{@link #SANGSUE} : Crocs, qui volent la vie du joueur ;</li>
 *   <li>{@link #BRETTEUR} : Épées et Rage, il frappe de plus en plus fort ;</li>
 *   <li>{@link #COMETE} : le boss du chapitre 1, qui reprend tout ;</li>
 *   <li>chapitre 2 : {@link #CHEF}, puis {@link #TRICHEUR} (Dés pipés), {@link #USURIER}
 *       (Intérêts) ou {@link #ROULETTE} (Zéro), et la {@link #REINE} ;</li>
 *   <li>chapitre 3 : la {@link #GARDIENNE}, puis {@link #MIROIR} (Reflet), {@link #HORLOGER}
 *       (Sablier) ou {@link #FOU} (Tapis), et l'{@link #ECLAT}, en deux phases ;</li>
 *   <li>Exploration : dans chaque donjon, un premier ennemi puis son chef (le roi de la
 *       couleur dans la prairie ; voir {@link fr.astratime.lucky.entities.exploration.Dungeon}).
 *       Port des Contrebandiers : Grignotage, Ivresse, Aveuglement, Abordage ; Mines d'Or :
 *       Pépite, Forage, Enclume, peau d'or ; Casino Englouti : Chant, Jackpot, Morsure, bras du Kraken.</li>
 * </ul>
 * Les lignes de chaque chapitre sont dans {@link fr.astratime.lucky.entities.tower.Chapter}.
 */
public enum EnemyKind {

    ENTRAINEMENT("Croupier d'entraînement", "ENTRAÎNEMENT",
        "Il t'apprend la table. Il frappe doucement. Il se protège. Il se soigne.",
        1_000, 30, 100,
        weights(EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1),
        deck(new int[] {2, 5, 8, 11, 14}, new int[] {2, 5, 8, 11, 14},
            new int[] {2, 5, 8, 11, 14}, new int[] {2, 5, 8, 11, 14}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    CROUPIER("Croupier démoniaque", "ENNEMI",
        "Il tient la table. Il frappe. Il se protège. Il se soigne.",
        5_000, 30, 100,
        weights(EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1),
        deck(new int[] {2, 5, 8, 11, 14}, new int[] {2, 5, 8, 11, 14},
            new int[] {2, 5, 8, 11, 14}, new int[] {2, 5, 8, 11, 14}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    GARDIEN("Gardien de la Banque", "GARDIEN",
        "Il garde le coffre. Sa défense est épaisse. Ses Épines te renvoient tes coups.",
        10_000, 150, 100,
        weights(EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1, EnemySymbol.THORNS, 1),
        deck(new int[] {4, 9}, new int[] {5, 11},
            new int[] {2, 5, 7, 9, 11, 12, 13, 14}, new int[] {3, 8, 12}),
        List.of(Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.PIQUE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.PIQUE)),

    SANGSUE("Sangsue du Tapis", "SANGSUE",
        "Elle colle au feutre. Chaque morsure la soigne. Garde ton bouclier levé.",
        10_000, 30, 100,
        weights(EnemySymbol.FANG, 2, EnemySymbol.SHIELD, 2, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {2, 5, 8, 11, 13, 14},
            new int[] {4, 10}, new int[] {6, 12}),
        List.of(Card.Suit.PIQUE, Card.Suit.TREFLE, Card.Suit.COEUR, Card.Suit.CARREAU),
        List.of(Card.Suit.COEUR, Card.Suit.PIQUE, Card.Suit.TREFLE, Card.Suit.CARREAU)),

    BRETTEUR("Bretteur à la Mise", "BRETTEUR",
        "Il ne pare jamais. Il frappe. Chaque Rage le rend plus fort.",
        10_000, 0, 100,
        weights(EnemySymbol.SWORD, 1, EnemySymbol.RAGE, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {5, 11},
            new int[] {3, 9}, new int[] {4, 8, 13}),
        List.of(Card.Suit.PIQUE, Card.Suit.TREFLE, Card.Suit.CARREAU, Card.Suit.COEUR),
        List.of(Card.Suit.PIQUE, Card.Suit.TREFLE, Card.Suit.CARREAU, Card.Suit.COEUR)),

    COMETE("Comète Dorée", "COMÈTE DORÉE",
        "Elle est tombée du ciel. Elle a créé la règle. Elle joue tous les jeux à la fois.",
        20_000, 100, 100,
        weights(EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 2, EnemySymbol.THORNS, 1,
            EnemySymbol.FANG, 1, EnemySymbol.RAGE, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12, 14},
            new int[] {5, 9, 13, 14}, new int[] {4, 8, 12}),
        List.of(Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.PIQUE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    // ----- Chapitre 2 : Les Tables Sacrées -----

    CHEF("Croupier en chef", "CROUPIER EN CHEF",
        "Le croupier a pris du galon. Il frappe. Il se protège. Ses Potions reforment aussi sa défense.",
        20_000, 100, 40,
        weights(EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1),
        deck(new int[] {2, 5, 8, 11, 14}, new int[] {2, 5, 8, 11, 14},
            new int[] {2, 5, 8, 11, 14}, new int[] {2, 5, 8, 11, 14}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    TRICHEUR("Tricheur aux Dés pipés", "TRICHEUR",
        "Il sourit. Il triche. Ses Dés pipés vident tes jauges.",
        40_000, 60, 30,
        weights(EnemySymbol.LOADED_DIE, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 7, 11}, new int[] {4, 9, 13},
            new int[] {5, 10}, new int[] {2, 6, 9, 12, 14}),
        List.of(Card.Suit.TREFLE, Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.TREFLE, Card.Suit.CARREAU, Card.Suit.PIQUE)),

    USURIER("Usurier du Comptoir", "USURIER",
        "Il prête. Il reprend. Ses Intérêts mangent tes gains et arment ses coups.",
        40_000, 30, 30,
        weights(EnemySymbol.INTEREST, 2, EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {5, 11},
            new int[] {4, 8, 12}, new int[] {5, 10}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE)),

    ROULETTE("Roulette Vivante", "ROULETTE",
        "Elle tourne. Elle ne pense pas. Rouge ou noir, personne ne sait.",
        40_000, 50, 30,
        weights(EnemySymbol.ZERO, 1, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1),
        deck(new int[] {2, 5, 8, 11, 14}, new int[] {2, 5, 8, 11, 14},
            new int[] {2, 5, 8, 11, 14}, new int[] {2, 5, 8, 11, 14}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR)),

    REINE("Reine des Tables", "REINE DES TABLES",
        "Elle règne sur toutes les tables. Elle triche, elle prête, elle mise. Blessée, sa chance double.",
        100_000, 100, 20,
        weights(EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1, EnemySymbol.LOADED_DIE, 1,
            EnemySymbol.INTEREST, 1, EnemySymbol.ZERO, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 6, 9, 12, 14}),
        List.of(Card.Suit.TREFLE, Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.COEUR),
        List.of(Card.Suit.TREFLE, Card.Suit.COEUR, Card.Suit.PIQUE, Card.Suit.CARREAU)),

    // ----- Chapitre 3 : Le Dernier Tirage -----

    GARDIENNE("Gardienne du Cratère", "GARDIENNE",
        "Elle veille au bord du cratère. Elle a appris des trois. Épines, Crocs et Rage.",
        120_000, 150, 5,
        weights(EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 3, EnemySymbol.THORNS, 1,
            EnemySymbol.FANG, 1, EnemySymbol.RAGE, 1),
        deck(new int[] {3, 8}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {4, 8, 12}),
        List.of(Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    MIROIR("Le Miroir", "MIROIR",
        "Il n'a pas de visage. Il a le tien. Chaque Reflet rejoue ta dernière carte.",
        200_000, 80, 4,
        weights(EnemySymbol.MIRROR, 2, EnemySymbol.SHIELD, 2),
        deck(new int[] {4, 9, 14}, new int[] {5, 11},
            new int[] {4, 8, 12}, new int[] {5, 10}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE)),

    HORLOGER("L'Horloger", "HORLOGER",
        "Tic. Tac. Son Sablier coule. Frappe fort, ou il explose.",
        200_000, 80, 15,
        weights(EnemySymbol.HOURGLASS, 2, EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 7, 11, 14}, new int[] {5, 11},
            new int[] {4, 8, 12, 14}, new int[] {6, 12}),
        List.of(Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.CARREAU, Card.Suit.COEUR, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    FOU("Le Joueur Fou", "JOUEUR FOU",
        "Il met tout sur la table. Chaque Tapis double ses coups du tour suivant. Touche-le avant, et il retombe.",
        200_000, 0, 15,
        weights(EnemySymbol.ALL_IN, 2, EnemySymbol.SWORD, 1),
        deck(new int[] {4}, new int[] {5, 11},
            new int[] {4, 9}, new int[] {4, 8, 13}),
        List.of(Card.Suit.PIQUE, Card.Suit.TREFLE, Card.Suit.CARREAU, Card.Suit.COEUR),
        List.of(Card.Suit.PIQUE, Card.Suit.TREFLE, Card.Suit.CARREAU, Card.Suit.COEUR)),

    ECLAT("Éclat Originel", "ÉCLAT ORIGINEL",
        "Le cœur de la comète. Il est le hasard. Blessé, il change de jeu.",
        400_000, 120, 5,
        weights(EnemySymbol.SHIELD, 3, EnemySymbol.THORNS, 1, EnemySymbol.FANG, 1, EnemySymbol.RAGE, 1),
        deck(new int[] {4, 9, 14}, new int[] {4, 8, 12, 14},
            new int[] {5, 9, 13, 14}, new int[] {4, 8, 12}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    // ----- Exploration : les donjons de la prairie (un soldat, puis le roi de la couleur) -----

    SOLDAT_PIQUE("Soldat de Pique", "SOLDAT DE PIQUE",
        "Il garde le donjon. Sa lance est affûtée. Il frappe sans pitié.",
        10_000, 30, 100,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 5, 7, 9, 11, 13}, new int[] {6},
            new int[] {4, 10}, new int[] {8}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.PIQUE, Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.TREFLE)),

    ROI_PIQUE("Roi de Pique", "ROI DE PIQUE",
        "Il règne sur les lames. Il frappe. Chaque Rage le rend plus fort.",
        20_000, 60, 100,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.RAGE, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 11, 12, 13, 14}, new int[] {5, 11},
            new int[] {4, 9}, new int[] {8}),
        List.of(Card.Suit.PIQUE, Card.Suit.TREFLE, Card.Suit.CARREAU, Card.Suit.COEUR),
        List.of(Card.Suit.PIQUE, Card.Suit.COEUR, Card.Suit.TREFLE, Card.Suit.CARREAU)),

    SOLDAT_COEUR("Soldat de Coeur", "SOLDAT DE COEUR",
        "Il panse ses blessures. Ses Potions le relèvent. Frappe vite.",
        10_000, 30, 100,
        weights(EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 2),
        deck(new int[] {5, 10}, new int[] {3, 5, 7, 9, 11, 13},
            new int[] {6}, new int[] {8}),
        List.of(Card.Suit.COEUR, Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    ROI_COEUR("Roi de Coeur", "ROI DE COEUR",
        "Il boit ta vie. Chaque Croc le soigne. Garde ton bouclier levé.",
        20_000, 50, 100,
        weights(EnemySymbol.FANG, 2, EnemySymbol.POTION, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {4, 9, 13}, new int[] {3, 6, 9, 11, 12, 13, 14},
            new int[] {5, 10}, new int[] {7}),
        List.of(Card.Suit.COEUR, Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    SOLDAT_CARREAU("Soldat de Carreau", "SOLDAT DE CARREAU",
        "Il se cache derrière son écu. Sa défense est épaisse. Perce-la.",
        10_000, 100, 100,
        weights(EnemySymbol.SHIELD, 2, EnemySymbol.SWORD, 1),
        deck(new int[] {5, 10}, new int[] {6},
            new int[] {3, 5, 7, 9, 11, 13}, new int[] {8}),
        List.of(Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.CARREAU, Card.Suit.COEUR, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    ROI_CARREAU("Roi de Carreau", "ROI DE CARREAU",
        "Il dort sur son trésor. Sa défense est épaisse. Ses Épines te renvoient tes coups.",
        20_000, 150, 100,
        weights(EnemySymbol.SHIELD, 2, EnemySymbol.THORNS, 1, EnemySymbol.SWORD, 1),
        deck(new int[] {4, 9}, new int[] {5, 11},
            new int[] {3, 6, 9, 11, 12, 13, 14}, new int[] {8}),
        List.of(Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.CARREAU, Card.Suit.COEUR, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    SOLDAT_TREFLE("Soldat de Trèfle", "SOLDAT DE TRÈFLE",
        "Il compte les pièces. Il frappe. Ses Intérêts mangent tes gains.",
        10_000, 30, 100,
        weights(EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1, EnemySymbol.INTEREST, 1),
        deck(new int[] {5, 10}, new int[] {6},
            new int[] {8}, new int[] {3, 5, 7, 9, 11, 13}),
        List.of(Card.Suit.TREFLE, Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.COEUR),
        List.of(Card.Suit.TREFLE, Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE)),

    ROI_TREFLE("Roi de Trèfle", "ROI DE TRÈFLE",
        "Toutes les tables lui appartiennent. Ses Intérêts arment ses coups. Ses Dés pipés vident tes jauges.",
        20_000, 50, 100,
        weights(EnemySymbol.INTEREST, 2, EnemySymbol.SWORD, 1, EnemySymbol.LOADED_DIE, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {4, 9}, new int[] {5, 11},
            new int[] {7}, new int[] {3, 6, 9, 11, 12, 13, 14}),
        List.of(Card.Suit.TREFLE, Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.COEUR),
        List.of(Card.Suit.TREFLE, Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE)),

    // ----- Exploration : le Port des Contrebandiers -----

    RAT_CALES("Rat des cales", "RAT DES CALES",
        "Il vit dans le noir. Il ronge tout. Même tes cartes.",
        250_000, 40, 25,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.NIBBLE, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    CAPITAINE_RAT("Capitaine Rat", "CAPITAINE RAT",
        "Il commande la cale. Ses dents mordent. Ses rats rongent ta main.",
        500_000, 80, 15,
        weights(EnemySymbol.NIBBLE, 2, EnemySymbol.FANG, 1, EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    BUVEUR("Buveur", "BUVEUR",
        "Il titube. Il cogne. Son haleine te fait tourner la tête.",
        250_000, 40, 25,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.DRUNK, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    TAVERNIER("Tavernier", "TAVERNIER",
        "Il sert à boire. Toujours. Sous son Ivresse, tes rouleaux trinquent au pire.",
        500_000, 80, 15,
        weights(EnemySymbol.DRUNK, 2, EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    GUETTEUR("Guetteur", "GUETTEUR",
        "Il veille sur la côte. Sa lanterne t'éblouit. Tu joues sans voir.",
        250_000, 60, 25,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.BLIND, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    GARDIEN_PHARE("Gardien du Phare", "GARDIEN DU PHARE",
        "Son phare aveugle les navires. Tu joueras à l'aveugle. Ses Épines te renvoient tes coups.",
        500_000, 120, 15,
        weights(EnemySymbol.BLIND, 2, EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1, EnemySymbol.THORNS, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    PIRATE("Pirate", "PIRATE",
        "Sabre au clair. Il prend ce qu'il veut. Surveille ta main.",
        250_000, 30, 25,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.BOARDING, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    CAPITAINE_NOIR("Capitaine Noir", "CAPITAINE NOIR",
        "Le maître du galion. Il vole tes cartes. Chaque Rage le rend plus fort.",
        500_000, 60, 15,
        weights(EnemySymbol.BOARDING, 2, EnemySymbol.SWORD, 2, EnemySymbol.RAGE, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    // ----- Exploration : les Mines d'Or -----

    CHERCHEUR_OR("Chercheur d'or", "CHERCHEUR D'OR",
        "Il creuse. Il trie. Avec lui, tes gains ne valent que des cailloux.",
        600_000, 60, 10,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.NUGGET, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    BARON_OR("Baron de l'Or", "BARON DE L'OR",
        "Tout l'or de la mine est à lui. Ses Pépites changent tes gains en pierres. Ses Intérêts arment ses coups.",
        1_200_000, 100, 6,
        weights(EnemySymbol.NUGGET, 2, EnemySymbol.SWORD, 1, EnemySymbol.INTEREST, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    FOREUR("Foreur", "FOREUR",
        "Sa foreuse perce tout. Ton bouclier aussi.",
        600_000, 60, 10,
        weights(EnemySymbol.DRILL, 2, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    GRAND_FOREUR("Grand Foreur", "GRAND FOREUR",
        "Il creuse jusqu'au coeur de la montagne. Rien ne l'arrête. Chaque Rage le rend plus fort.",
        1_200_000, 100, 6,
        weights(EnemySymbol.DRILL, 2, EnemySymbol.SWORD, 1, EnemySymbol.RAGE, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    FORGERON("Forgeron", "FORGERON",
        "Chaque coup d'Enclume affûte son arme. Ne traîne pas.",
        600_000, 80, 10,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.ANVIL, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    MAITRE_FORGE("Maître de Forge", "MAÎTRE DE FORGE",
        "Il forge depuis mille ans. Son arme grandit à chaque Enclume. Pour toujours.",
        1_200_000, 120, 6,
        weights(EnemySymbol.ANVIL, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    GOLEM_OR("Golem d'or", "GOLEM D'OR",
        "Un tas d'or qui marche. Tant qu'il a plus de la moitié de ses PV, sa peau d'or encaisse la moitié de tes coups.",
        600_000, 100, 10,
        weights(EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 2),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    COEUR_MONTAGNE("Coeur de la Montagne", "COEUR DE LA MONTAGNE",
        "La montagne est vivante. Tant qu'il a plus de la moitié de ses PV, il ne prend que la moitié de tes dégâts.",
        1_200_000, 150, 6,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 2, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    // ----- Exploration : le Casino Englouti -----

    BARMAN_NOYE("Barman noyé", "BARMAN NOYÉ",
        "Il sert sous l'eau. Il fredonne. Ta main n'obéit plus.",
        1_500_000, 80, 5,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SONG, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    SIRENE("Sirène du Bar", "SIRÈNE DU BAR",
        "Son Chant t'envoûte. Tu joues ce qu'elle veut.",
        3_000_000, 120, 3,
        weights(EnemySymbol.SONG, 2, EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    BANDIT_MANCHOT("Bandit manchot", "BANDIT MANCHOT",
        "Une machine à sous rouillée. Trois symboles pareils : Jackpot. Il frappe cinq fois plus fort.",
        1_500_000, 80, 5,
        weights(EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    JACKPOT_VIVANT("Jackpot Vivant", "JACKPOT VIVANT",
        "La plus grande machine du casino. Trois symboles pareils : Jackpot. Toute la salle tremble.",
        3_000_000, 120, 3,
        weights(EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1, EnemySymbol.RAGE, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    REQUIN("Requin", "REQUIN",
        "Il sent l'argent. Sa Morsure vide tes poches. Elle le soigne.",
        1_500_000, 80, 5,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.BANK_BITE, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    REQUIN_BANQUIER("Grand Requin Banquier", "REQUIN BANQUIER",
        "Il garde les coffres engloutis. Il dévore tes gains. Chaque pièce le soigne.",
        3_000_000, 120, 3,
        weights(EnemySymbol.BANK_BITE, 2, EnemySymbol.FANG, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    PIEUVRE("Pieuvre croupière", "PIEUVRE",
        "Huit bras pour distribuer. Elle joue cinq cartes à chaque tour.",
        1_500_000, 80, 5,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.ZERO, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    KRAKEN("Kraken", "KRAKEN",
        "Le maître de la salle VIP. Huit bras, huit cartes par tour. Il perd un bras à chaque huitième de ses PV.",
        3_000_000, 150, 3,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.FANG, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE));

    /** Force des ennemis du chapitre 2, en % : leurs attaques, défenses et effets sont multipliés d'autant. */
    public static final int CHAPTER_2_POWER = 200;
    /** Multiplicateur des PV des ennemis de la Tour des épreuves (le joueur gagne des rangs à la boutique). */
    public static final int TOWER_HP_FACTOR = 5;
    /** Force des ennemis du chapitre 3, en %. */
    public static final int CHAPTER_3_POWER = 300;

    /** Rouleaux interdits dans le deck de l'Éclat Originel. */
    static final int FORBIDDEN_REELS = 1;

    /** Sous cette part de vie, l'Éclat Originel passe à sa deuxième phase. */
    public static final float PHASE_TWO_RATIO = 0.5f;

    /** Symboles de l'Éclat Originel dans sa deuxième phase : ceux du chapitre 3. */
    private static final Map<EnemySymbol, Integer> ECLAT_PHASE_TWO = Collections.unmodifiableMap(weights(
        EnemySymbol.SHIELD, 3, EnemySymbol.MIRROR, 1, EnemySymbol.HOURGLASS, 1, EnemySymbol.ALL_IN, 1));

    private final String                     displayName;
    private final String                     barName;
    private final String                     description;
    private final int                        maxHp;
    private final int                        baseDefense;
    private final int                        healScale;
    private final Map<EnemySymbol, Integer>  weights;
    private final Map<Card.Suit, int[]>      deck;
    private final List<Card.Suit>            priority;
    private final List<Card.Suit>            lowHpPriority;

    EnemyKind(String displayName, String barName, String description, int maxHp, int baseDefense, int healScale,
              Map<EnemySymbol, Integer> weights, Map<Card.Suit, int[]> deck, List<Card.Suit> priority,
              List<Card.Suit> lowHpPriority) {
        this.displayName  = displayName;
        this.barName      = barName;
        this.description  = description;
        this.maxHp        = maxHp;
        this.baseDefense  = baseDefense;
        this.healScale    = healScale;
        this.weights      = Collections.unmodifiableMap(weights);
        this.deck         = deck;
        this.priority     = priority;
        this.lowHpPriority = lowHpPriority;
    }

    /** @return le nom affiché (ex : "Gardien de la Banque"). */
    public String getDisplayName() { return displayName; }
    /** @return le nom court affiché dans sa barre de vie (ex : "GARDIEN"). */
    public String getBarName() { return barName; }
    /** @return sa présentation, en quelques phrases courtes. */
    public String getDescription() { return description; }
    /**
     * @return ses points de vie maximum : ceux de la Tour des épreuves sont
     *         multipliés par {@link #TOWER_HP_FACTOR}, car le rang du joueur le
     *         rend bien plus fort
     */
    public int getMaxHp() { return isTower() ? maxHp * TOWER_HP_FACTOR : maxHp; }

    /** @return {@code true} pour un ennemi de la Tour des épreuves (voir {@link fr.astratime.lucky.entities.tower.Chapter}). */
    public boolean isTower() {
        return switch (this) {
            case CROUPIER, GARDIEN, SANGSUE, BRETTEUR, COMETE,
                 CHEF, TRICHEUR, USURIER, ROULETTE, REINE,
                 GARDIENNE, MIROIR, HORLOGER, FOU, ECLAT -> true;
            default -> false;
        };
    }
    /** @return sa défense de base (renforcée par sa {@linkplain #getPower() force}), reformée à chacun de ses tours. */
    public int getBaseDefense() { return empowered(baseDefense); }

    /**
     * @return sa force, en % : le multiplicateur de ses attaques, de ses défenses
     *         et de tous ses effets (100 au chapitre 1, puis plus à chaque chapitre)
     */
    public int getPower() {
        return switch (this) {
            case CHEF, TRICHEUR, USURIER, ROULETTE, REINE -> CHAPTER_2_POWER;
            case GARDIENNE, MIROIR, HORLOGER, FOU, ECLAT  -> CHAPTER_3_POWER;
            default -> 100;
        };
    }

    /** Dégâts de base d'une Épée du croupier d'entraînement. */
    public static final int TRAINING_SWORD_DAMAGE = 10;

    /** @return les dégâts de base de son Épée, avant sa {@linkplain #getPower() force}. */
    public int swordDamage() {
        return this == ENTRAINEMENT ? TRAINING_SWORD_DAMAGE : EnemySymbol.SWORD_DAMAGE;
    }

    /** @return {@code value} multiplié par sa {@linkplain #getPower() force} (arrondi). */
    public int empowered(int value) { return Math.round(value * getPower() / 100f); }

    /** @return sa force en multiplicateur affichable (ex : "x1,5"). */
    public String powerText() {
        float factor = getPower() / 100f;
        String text = factor == Math.round(factor) ? String.valueOf(Math.round(factor)) : String.valueOf(factor);
        return "x" + text.replace('.', ',');
    }

    /** @return la part des dégâts du joueur renvoyée par chaque Épines, en %. */
    public int thornsPercent() { return empowered(EnemySymbol.THORNS_PERCENT); }

    /** @return les dégâts maximum que ses Épines renvoient en un tour. */
    public int thornsMax() { return empowered(EnemySymbol.THORNS_MAX); }

    /** @return la part des jauges du joueur vidée par chaque Dé pipé, en %. */
    public int diePercent() { return Math.min(100, empowered(EnemySymbol.DIE_PERCENT)); }

    /** @return la part des gains du joueur prise par chaque Intérêts, en %. */
    public int interestPercent() { return empowered(EnemySymbol.INTEREST_PERCENT); }

    /** @return {@code true} si son deck contient le Rouleau interdit (l'Éclat Originel). */
    public boolean forbidsReels() { return this == ECLAT; }
    /** @return les cartes qu'il joue à chaque tour, parmi celles piochées. */
    public int getPlaysPerTurn() {
        return switch (this) {
            case PIEUVRE -> OCTOPUS_PLAYS;
            case KRAKEN  -> KRAKEN_ARMS;
            default      -> Enemy.PLAYS_PER_TURN;
        };
    }
    /** @return {@code true} pour le boss d'un chapitre ou le roi d'un donjon. */
    public boolean isBoss() {
        return switch (this) {
            case COMETE, REINE, ECLAT, ROI_PIQUE, ROI_COEUR, ROI_CARREAU, ROI_TREFLE,
                 CAPITAINE_RAT, TAVERNIER, GARDIEN_PHARE, CAPITAINE_NOIR,
                 BARON_OR, GRAND_FOREUR, MAITRE_FORGE, COEUR_MONTAGNE,
                 SIRENE, JACKPOT_VIVANT, REQUIN_BANQUIER, KRAKEN -> true;
            default -> false;
        };
    }

    /** Cartes jouées à chaque tour par la Pieuvre croupière. */
    static final int OCTOPUS_PLAYS = 5;
    /** Bras du Kraken à pleine vie : une carte jouée par bras. */
    public static final int KRAKEN_ARMS = 8;

    /** @return {@code true} si sa peau d'or encaisse la moitié des coups tant qu'il a plus de la moitié de ses PV. */
    public boolean hasGoldSkin() { return this == GOLEM_OR || this == COEUR_MONTAGNE; }

    /** @return {@code true} si ses trois rouleaux identiques font un Jackpot (attaques et Boucliers x{@link EnemySymbol#JACKPOT_FACTOR}). */
    public boolean hitsJackpots() { return this == BANDIT_MANCHOT || this == JACKPOT_VIVANT; }

    /** @return {@code true} s'il joue une carte par bras, et perd un bras à chaque huitième de ses PV (le Kraken). */
    public boolean hasArms() { return this == KRAKEN; }

    /**
     * @return la force de ses soins (Potions et Crocs), en pourcentage de ceux
     *         du croupier : les ennemis aux PV énormes se soignent d'une plus
     *         petite part de leurs PV max
     */
    public int getHealScale() { return healScale; }

    /** @return les points de vie que rend une Potion renforcée de {@code bonusPercent} (Cœurs), en % de ses PV max. */
    public float potionPercent(int bonusPercent) {
        return (EnemySymbol.POTION_PERCENT + bonusPercent) * healScale * getPower() / 10_000f;
    }

    /** @return la part de ses PV max qu'un Croc lui rend pour chaque PV volé, en %. */
    public float drainPercent() { return EnemySymbol.FANG_DRAIN * healScale * getPower() / 10_000f; }

    /** @return {@code true} s'il joue ses cartes au hasard (la Roulette Vivante). */
    public boolean playsAtRandom() { return this == ROULETTE; }

    /** @return {@code true} si ses Potions reforment aussi sa défense (le Croupier en chef). */
    public boolean potionShields() { return this == CHEF; }

    /** @return {@code true} si ses Trèfles comptent double quand sa vie est basse (la Reine des Tables). */
    public boolean royalBet() { return this == REINE; }

    /** @return {@code true} s'il change de symboles sous {@link #PHASE_TWO_RATIO} de vie (l'Éclat Originel). */
    public boolean hasPhaseTwo() { return this == ECLAT; }

    /** @return les symboles de ses rouleaux et leur poids (dans l'ordre de tirage), en première phase. */
    public Map<EnemySymbol, Integer> getWeights() { return weights; }

    /** @return les symboles de ses rouleaux et leur poids dans la phase {@code phase} (1 ou 2). */
    public Map<EnemySymbol, Integer> getWeights(int phase) {
        return phase >= 2 && hasPhaseTwo() ? ECLAT_PHASE_TWO : weights;
    }

    /** @return les symboles qui peuvent sortir sur ses rouleaux, en première phase. */
    public List<EnemySymbol> getSymbols() { return List.copyOf(weights.keySet()); }

    /** @return les symboles qui peuvent sortir sur ses rouleaux dans la phase {@code phase}. */
    public List<EnemySymbol> getSymbols(int phase) { return List.copyOf(getWeights(phase).keySet()); }

    /** @return les couleurs qu'il joue en priorité quand il est en forme (la première d'abord). */
    public List<Card.Suit> getPriority() { return priority; }

    /** @return les couleurs qu'il joue en priorité quand sa vie est basse. */
    public List<Card.Suit> getLowHpPriority() { return lowHpPriority; }

    /** @return un deck neuf, aux cartes de sa composition. */
    public List<Card> createDeck() {
        List<Card> cards = new ArrayList<>();
        for (Map.Entry<Card.Suit, int[]> entry : deck.entrySet()) {
            for (int rank : entry.getValue()) cards.add(EnemyCards.card(entry.getKey(), rank));
        }
        if (forbidsReels()) {
            for (int i = 0; i < FORBIDDEN_REELS; i++) cards.add(EnemyCards.forbiddenReel());
        }
        return cards;
    }

    /** @return la phrase qui résume ses rouleaux, pour son infobulle (ex : "Épée, Bouclier x2, Épines x2"). */
    public String describeReels() {
        return describeReels(1);
    }

    /** @return comme {@link #describeReels()}, pour la phase {@code phase}. */
    public String describeReels(int phase) {
        Map<EnemySymbol, Integer> weights = getWeights(phase);
        List<String> parts = new ArrayList<>();
        int total = weights.values().stream().mapToInt(Integer::intValue).sum();
        for (Map.Entry<EnemySymbol, Integer> entry : weights.entrySet()) {
            parts.add(entry.getKey().getDisplayName() + " " + Math.round(100f * entry.getValue() / total) + " %");
        }
        return String.join(", ", parts);
    }

    private static Map<EnemySymbol, Integer> weights(Object... pairs) {
        Map<EnemySymbol, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) map.put((EnemySymbol) pairs[i], (Integer) pairs[i + 1]);
        return map;
    }

    /** Rangs des cartes de chaque couleur, dans l'ordre Pique, Cœur, Carreau, Trèfle. */
    private static Map<Card.Suit, int[]> deck(int[] pique, int[] coeur, int[] carreau, int[] trefle) {
        Map<Card.Suit, int[]> map = new EnumMap<>(Card.Suit.class);
        map.put(Card.Suit.PIQUE, pique);
        map.put(Card.Suit.COEUR, coeur);
        map.put(Card.Suit.CARREAU, carreau);
        map.put(Card.Suit.TREFLE, trefle);
        return map;
    }
}
