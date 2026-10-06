package fr.astratime.lucky.entities.enemy;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.i18n.Lang;

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
 *   <li>chapitre 4 : le {@link #PILLEUR}, puis {@link #FAUSSAIRE} (Fausse monnaie), {@link #CARTOMANCIENNE}
 *       (Prédiction) ou {@link #DUELLISTE} (Duel), et le {@link #PRETENDANT} et ses trois éclats ;</li>
 *   <li>chapitre 5 : le {@link #PORTIER}, puis {@link #COMPTABLE} (Taxe), {@link #DIRECTEUR} (Nouvelle
 *       règle) ou {@link #SECURITE} (Fouille), et la {@link #MAISON} en trois parties ;</li>
 *   <li>chapitre 6 : le {@link #GARDIEN_LEVIER}, puis l'{@link #OMBRE} (ton deck), la {@link #BANQUEROUTE}
 *       (Faillite) ou le {@link #TEMPS_MORT} (10 tours), et la {@link #MACHINE_ORIGINELLE} ;</li>
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
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    // ----- Chapitre 4 : Le Monde sans Maître -----
    PILLEUR("Le Pilleur", "PILLEUR",
        "Il a pillé la boutique. Chaque carte que tu achètes, il en garde une copie : un As de plus dans son deck.",
        320_000, 120, 5,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.INTEREST, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    FAUSSAIRE("Le Faussaire", "FAUSSAIRE",
        "Ses billets sont faux. Les tiens aussi, bientôt. Dépense vite.",
        560_000, 80, 4,
        weights(EnemySymbol.FAKE_MONEY, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    CARTOMANCIENNE("La Cartomancienne", "CARTOMANCIENNE",
        "Elle connaît ton avenir. Si sa Prédiction sort, elle frappe trois fois plus fort.",
        560_000, 80, 4,
        weights(EnemySymbol.PREDICTION, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    DUELLISTE("Le Duelliste", "DUELLISTE",
        "Un contre un. Chacun tire une carte. La plus haute frappe. Tes As et tes figures comptent enfin.",
        560_000, 60, 4,
        weights(EnemySymbol.DUEL, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    PRETENDANT("Le Prétendant", "PRÉTENDANT",
        "Il veut devenir le nouveau maître de la chance. Trois éclats, trois pouvoirs volés. Chaque tiers de ses PV perdu lui en retire un.",
        1_100_000, 120, 3,
        weights(EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 2, EnemySymbol.THORNS, 1, EnemySymbol.FANG, 1, EnemySymbol.RAGE, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    // ----- Chapitre 5 : La Maison -----

    PORTIER("Le Portier", "PORTIER",
        "Il filtre l'entrée. Une famille de cartes reste dehors tout le combat.",
        400_000, 150, 4,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 2, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    COMPTABLE("Le Comptable", "COMPTABLE",
        "Il compte tout. Sa Taxe frappe chaque carte jouée. Même à sec.",
        700_000, 80, 4,
        weights(EnemySymbol.TAX, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    DIRECTEUR("Le Directeur des Jeux", "DIRECTEUR",
        "Il change les règles. Pour deux tours, le jeu n'est plus le même.",
        700_000, 80, 4,
        weights(EnemySymbol.NEW_RULE, 1, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    SECURITE("La Sécurité", "SÉCURITÉ",
        "On t'a repéré. Elle fouille ton deck. Plus tu gagnes gros, plus elle confisque.",
        700_000, 120, 4,
        weights(EnemySymbol.FRISK, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    MAISON("La Maison", "LA MAISON",
        "Pas un ennemi. Un lieu vivant. La Façade protège, le Coffre soigne, la Salle de jeu frappe. La Maison gagne toujours : une fois, elle annule ton plus gros coup.",
        1_400_000, 150, 3,
        weights(EnemySymbol.SHIELD, 2, EnemySymbol.SWORD, 2, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    // ----- Chapitre 6 : Le Jackpot -----

    GARDIEN_LEVIER("Le Gardien du Levier", "GARDIEN DU LEVIER",
        "Il garde le levier. Il a tout appris en route. Fausse monnaie, Taxe et Duel.",
        500_000, 100, 4,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.FAKE_MONEY, 1, EnemySymbol.TAX, 1, EnemySymbol.DUEL, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    OMBRE("L'Ombre du Joueur", "OMBRE",
        "Ton passé. Il a ton deck. Exactement le même.",
        900_000, 80, 4,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.MIRROR, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    BANQUEROUTE("La Banqueroute", "BANQUEROUTE",
        "Tout ou rien. Plus riche que lui, tu perds tout. Plus pauvre, c'est lui qui perd.",
        900_000, 60, 4,
        weights(EnemySymbol.BANKRUPTCY, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    TEMPS_MORT("Le Temps Mort", "TEMPS MORT",
        "Les tours comptent. Le combat dure 10 tours. Au 11e, tu perds. Moins de PV, mais il ne pardonne pas.",
        600_000, 80, 3,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.HOURGLASS, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    MACHINE_ORIGINELLE("La Machine Originelle", "MACHINE ORIGINELLE",
        "La machine qui a créé la comète. Cinq rouleaux contre tes trois. Blessée, elle te vole tes rouleaux. Achevée, elle tente un dernier tirage.",
        2_000_000, 150, 2,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.RAGE, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE));

    /** Force des ennemis du chapitre 2, en % : leurs attaques, défenses et effets sont multipliés d'autant. */
    public static final int CHAPTER_2_POWER = 200;
    /** Multiplicateur des PV des ennemis de la Tour des épreuves (le joueur gagne des rangs à la boutique). */
    public static final int TOWER_HP_FACTOR = 5;
    /** Force des ennemis du chapitre 3, en %. */
    public static final int CHAPTER_3_POWER = 300;
    /** Force des ennemis des chapitres 4, 5 et 6, en % (choix d'Astra : le rang rendra le joueur plus fort). */
    public static final int CHAPTER_4_POWER = 400;
    public static final int CHAPTER_5_POWER = 500;
    public static final int CHAPTER_6_POWER = 600;

    /**
     * Mode difficile de la Tour (« Nouveau tirage + », ouvert après la Machine
     * Originelle) : PV et force des ennemis de la Tour multipliés d'autant.
     */
    public static final int HARD_HP_FACTOR = 2, HARD_POWER_FACTOR = 2;

    /** {@code true} pendant une ascension de la Tour en mode difficile (voir {@link #setTowerHard(boolean)}). */
    private static boolean towerHard;

    /**
     * Règle la difficulté de la Tour pour les combats qui suivent : l'écran de
     * jeu l'appelle à chaque nouvelle suite de combats (faux hors de la Tour).
     */
    public static void setTowerHard(boolean hard) { towerHard = hard; }

    /** @return {@code true} si les ennemis de la Tour sont en mode difficile. */
    public static boolean isTowerHard() { return towerHard; }

    /** Tours du combat contre le Temps Mort : au tour suivant, le joueur a perdu. */
    public static final int TIME_LIMIT = 10;
    /** Rouleaux de la Machine Originelle, puis quand elle a volé un rouleau au joueur. */
    public static final int MACHINE_REELS = 5, MACHINE_GREEDY_REELS = 6;
    /** Sous cette part de vie, la Machine Originelle vole un deuxième rouleau au joueur. */
    public static final float MACHINE_LAST_RATIO = 0.1f;
    /** La Maison annule le premier coup qui lui retire au moins cette part de ses PV max (en %). */
    public static final int HOUSE_CANCEL_PERCENT = 5;

    /** Rouleaux interdits dans le deck de l'Éclat Originel. */
    static final int FORBIDDEN_REELS = 1;

    /** Sous cette part de vie, l'Éclat Originel passe à sa deuxième phase. */
    public static final float PHASE_TWO_RATIO = 0.5f;

    /** Symboles de l'Éclat Originel dans sa deuxième phase : ceux du chapitre 3. */
    private static final Map<EnemySymbol, Integer> ECLAT_PHASE_TWO = Collections.unmodifiableMap(weights(
        EnemySymbol.SHIELD, 3, EnemySymbol.MIRROR, 1, EnemySymbol.HOURGLASS, 1, EnemySymbol.ALL_IN, 1));

    /** Symboles de la Maison sans sa Façade (plus de Boucliers), puis sans son Coffre (plus de Potions). */
    private static final Map<EnemySymbol, Integer> HOUSE_PHASE_TWO = Collections.unmodifiableMap(weights(
        EnemySymbol.SWORD, 2, EnemySymbol.POTION, 1));
    private static final Map<EnemySymbol, Integer> HOUSE_PHASE_THREE = Collections.unmodifiableMap(weights(
        EnemySymbol.SWORD, 1));

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
    public String getDisplayName() { return Lang.t(displayName); }
    /** @return le nom court affiché dans sa barre de vie (ex : "GARDIEN"). */
    public String getBarName() { return Lang.t(barName); }
    /** @return sa présentation, en quelques phrases courtes. */
    public String getDescription() { return Lang.t(description); }
    /**
     * @return ses points de vie maximum : ceux de la Tour des épreuves sont
     *         multipliés par {@link #TOWER_HP_FACTOR}, car le rang du joueur le
     *         rend bien plus fort (et encore par {@link #HARD_HP_FACTOR} en mode difficile)
     */
    public int getMaxHp() {
        if (!isTower()) return maxHp;
        return maxHp * TOWER_HP_FACTOR * (towerHard ? HARD_HP_FACTOR : 1);
    }

    /** @return {@code true} pour un ennemi de la Tour des épreuves (voir {@link fr.astratime.lucky.entities.tower.Chapter}). */
    public boolean isTower() {
        return switch (this) {
            case CROUPIER, GARDIEN, SANGSUE, BRETTEUR, COMETE,
                 CHEF, TRICHEUR, USURIER, ROULETTE, REINE,
                 GARDIENNE, MIROIR, HORLOGER, FOU, ECLAT,
                 PILLEUR, FAUSSAIRE, CARTOMANCIENNE, DUELLISTE, PRETENDANT,
                 PORTIER, COMPTABLE, DIRECTEUR, SECURITE, MAISON,
                 GARDIEN_LEVIER, OMBRE, BANQUEROUTE, TEMPS_MORT, MACHINE_ORIGINELLE -> true;
            default -> false;
        };
    }
    /**
     * @return sa défense de base (renforcée par sa {@linkplain #getPower() force}), reformée à chacun de ses tours :
     *         en Exploration, celle de son lieu
     */
    public int getBaseDefense() {
        Place place = Place.of(this);
        return empowered(place != null && place.getShieldDefense() > 0 ? place.getShieldDefense() : baseDefense);
    }

    /**
     * @return sa force, en % : le multiplicateur de ses attaques, de ses défenses
     *         et de tous ses effets (100 au chapitre 1, puis plus à chaque chapitre ;
     *         multipliée par {@link #HARD_POWER_FACTOR} dans la Tour en mode difficile)
     */
    public int getPower() {
        return isTower() && towerHard ? chapterPower() * HARD_POWER_FACTOR : chapterPower();
    }

    /** @return sa force en mode normal, en %, selon son chapitre. */
    private int chapterPower() {
        return switch (this) {
            case CHEF, TRICHEUR, USURIER, ROULETTE, REINE -> CHAPTER_2_POWER;
            case GARDIENNE, MIROIR, HORLOGER, FOU, ECLAT  -> CHAPTER_3_POWER;
            case PILLEUR, FAUSSAIRE, CARTOMANCIENNE, DUELLISTE, PRETENDANT -> CHAPTER_4_POWER;
            case PORTIER, COMPTABLE, DIRECTEUR, SECURITE, MAISON -> CHAPTER_5_POWER;
            case GARDIEN_LEVIER, OMBRE, BANQUEROUTE, TEMPS_MORT, MACHINE_ORIGINELLE -> CHAPTER_6_POWER;
            default -> 100;
        };
    }

    /** Dégâts de base d'une Épée du croupier d'entraînement. */
    public static final int TRAINING_SWORD_DAMAGE = 10;

    /** @return les dégâts de base de son Épée, avant sa {@linkplain #getPower() force}. */
    public int swordDamage() {
        if (this == ENTRAINEMENT) return TRAINING_SWORD_DAMAGE;
        Place place = Place.of(this);
        return place != null && place.getSwordDamage() > 0 ? place.getSwordDamage() : EnemySymbol.SWORD_DAMAGE;
    }

    /**
     * @return la défense d'un de ses Boucliers, avant sa {@linkplain #getPower() force} : en Exploration,
     *         à l'échelle de la défense de base de son lieu
     */
    public int shieldDefense() { return Math.round(EnemySymbol.SHIELD_DEFENSE * shieldScale()); }

    /** @return l'attaque ajoutée par une de ses cartes Pique, avant sa force (à l'échelle de l'Épée de son lieu). */
    public int swordBonus(Card card) { return Math.round(EnemyCards.swordBonus(card) * swordScale()); }

    /** @return la défense ajoutée par une de ses cartes Carreau, avant sa force (à l'échelle de la défense de son lieu). */
    public int shieldBonus(Card card) { return Math.round(EnemyCards.shieldBonus(card) * shieldScale()); }

    /** @return l'échelle de ses attaques par rapport à la base : l'Épée de son lieu sur {@link EnemySymbol#SWORD_DAMAGE}. */
    private float swordScale() {
        Place place = Place.of(this);
        return place != null && place.getSwordDamage() > 0 ? place.getSwordDamage() / (float) EnemySymbol.SWORD_DAMAGE : 1f;
    }

    /** @return l'échelle de ses défenses par rapport à la base : la défense de son lieu sur {@link EnemySymbol#SHIELD_DEFENSE}. */
    private float shieldScale() {
        Place place = Place.of(this);
        return place != null && place.getShieldDefense() > 0 ? place.getShieldDefense() / (float) EnemySymbol.SHIELD_DEFENSE : 1f;
    }

    /** @return {@code value} multiplié par sa {@linkplain #getPower() force} (arrondi). */
    public int empowered(int value) { return Math.round(value * getPower() / 100f); }

    /** @return sa force en multiplicateur affichable (ex : "x1,5"). */
    public String powerText() {
        float factor = getPower() / 100f;
        String text = factor == Math.round(factor) ? String.valueOf(Math.round(factor)) : String.valueOf(factor);
        return "x" + Lang.decimal(text);
    }

    /** @return la part des dégâts du joueur renvoyée par chaque Épines, en %. */
    public int thornsPercent() { return empowered(EnemySymbol.THORNS_PERCENT); }

    /** @return les dégâts maximum que ses Épines renvoient en un tour. */
    public int thornsMax() { return empowered(EnemySymbol.THORNS_MAX); }

    /** @return la part des jauges du joueur vidée par chaque Dé pipé, en %. */
    public int diePercent() { return Math.min(100, empowered(EnemySymbol.DIE_PERCENT)); }

    /** @return la part des gains du joueur rendue fausse par chaque Fausse monnaie, en %. */
    public int fakePercent() { return Math.min(100, empowered(EnemySymbol.FAKE_PERCENT)); }

    /** @return la part des gains du joueur prise par chaque Intérêts, en %. */
    public int interestPercent() { return empowered(EnemySymbol.INTEREST_PERCENT); }

    /** @return {@code true} si son deck contient le Rouleau interdit (l'Éclat Originel). */
    public boolean forbidsReels() { return this == ECLAT || this == PRETENDANT; }
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
            case COMETE, REINE, ECLAT, PRETENDANT, MAISON, MACHINE_ORIGINELLE, ROI_PIQUE, ROI_COEUR, ROI_CARREAU, ROI_TREFLE,
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
        Place place = Place.of(this);
        if (place != null && place.getHealPercent() > 0) { // soin fixé par son lieu, les Cœurs en proportion
            return place.getHealPercent() * (EnemySymbol.POTION_PERCENT + bonusPercent)
                / (float) EnemySymbol.POTION_PERCENT * getPower() / 100f;
        }
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

    /**
     * @return sa phase quand il lui reste {@code hpRatio} de ses PV : l'Éclat
     *         Originel passe à 2 sous la moitié ; le Prétendant perd un éclat et
     *         la Maison une de ses parties à chaque tiers ; la Machine
     *         Originelle vole un rouleau sous la moitié, puis un autre sous
     *         {@link #MACHINE_LAST_RATIO}. Les autres restent en phase 1.
     */
    public int phaseFor(float hpRatio) {
        return switch (this) {
            case ECLAT -> hpRatio < PHASE_TWO_RATIO ? 2 : 1;
            case PRETENDANT, MAISON -> hpRatio * 3f <= 1f ? 3 : hpRatio * 3f <= 2f ? 2 : 1;
            case MACHINE_ORIGINELLE -> hpRatio < MACHINE_LAST_RATIO ? 3 : hpRatio < PHASE_TWO_RATIO ? 2 : 1;
            default -> 1;
        };
    }

    /** @return le texte qui annonce sa phase {@code phase}, quand il y entre. */
    public String phaseText(int phase) {
        return switch (this) {
            case PRETENDANT -> phase == 2 ? Lang.t("ÉCLAT PERDU : FINI LE ROULEAU INTERDIT") : Lang.t("ÉCLAT PERDU : FINIE LA MISE ROYALE");
            case MAISON -> phase == 2 ? Lang.t("LA FAÇADE TOMBE : PLUS DE DÉFENSE") : Lang.t("LE COFFRE TOMBE : PLUS DE SOINS");
            case MACHINE_ORIGINELLE -> Lang.t("ELLE TE VOLE UN ROULEAU !");
            default -> Lang.f("PHASE {0} !", phase);
        };
    }

    /** @return les éclats que porte le Prétendant dans sa phase {@code phase} (3, 2 puis 1). */
    public static int shards(int phase) { return Math.max(0, 4 - phase); }

    /** @return le nombre de rouleaux de sa machine dans la phase {@code phase} (3, sauf la Machine Originelle). */
    public int getReelCount(int phase) {
        if (this != MACHINE_ORIGINELLE) return EnemySlotMachine.SYMBOL_COUNT;
        return phase >= 2 ? MACHINE_GREEDY_REELS : MACHINE_REELS;
    }

    /** @return les rouleaux qu'il a volés au joueur dans la phase {@code phase} (la Machine Originelle). */
    public int stolenReels(int phase) { return this == MACHINE_ORIGINELLE ? phase - 1 : 0; }

    /** @return {@code true} s'il garde une copie (un As sombre) de chaque carte achetée à l'échoppe (le Pilleur). */
    public boolean copiesPurchases() { return this == PILLEUR; }

    /** @return {@code true} s'il interdit une famille de cartes tout le combat (le Portier). */
    public boolean bansFamily() { return this == PORTIER; }

    /** @return {@code true} s'il joue avec une copie du deck du joueur (l'Ombre du Joueur). */
    public boolean copiesPlayerDeck() { return this == OMBRE; }

    /** @return les tours que dure le combat (le Temps Mort), 0 s'il n'y a pas de limite. */
    public int getTurnLimit() { return this == TEMPS_MORT ? TIME_LIMIT : 0; }

    /** @return {@code true} si une fois par combat, il annule le plus gros coup du joueur (la Maison). */
    public boolean houseWins() { return this == MAISON; }

    /** @return {@code true} si, achevé, il résiste et tente un dernier tirage (la Machine Originelle). */
    public boolean hasLastDraw() { return this == MACHINE_ORIGINELLE; }

    /** @return les symboles de ses rouleaux et leur poids (dans l'ordre de tirage), en première phase. */
    public Map<EnemySymbol, Integer> getWeights() { return weights; }

    /** @return les symboles de ses rouleaux et leur poids dans la phase {@code phase} (1 ou 2). */
    public Map<EnemySymbol, Integer> getWeights(int phase) {
        if (phase >= 2 && hasPhaseTwo()) return ECLAT_PHASE_TWO;
        if (this == MAISON && phase >= 3) return HOUSE_PHASE_THREE;
        if (this == MAISON && phase == 2) return HOUSE_PHASE_TWO;
        return weights;
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
