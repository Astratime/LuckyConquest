package fr.astratime.lucky.entities.enemy;

import fr.astratime.lucky.entities.tower.Chapter;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.SpinEconomy;
import fr.astratime.lucky.entities.exploration.HardExploration;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;
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
 *   <li>chapitre 3 : le {@link #PASSEUR}, puis {@link #PELERIN_FER} (Serment), {@link #DISEUSE} (Voyance) ou
 *       {@link #PORTEUR} (Relique), et le {@link #PROPHETE} et ses pèlerins ;</li>
 *   <li>chapitre 4 : la {@link #GARDIENNE}, puis {@link #MIROIR} (Reflet), {@link #HORLOGER}
 *       (Sablier) ou {@link #FOU} (Tapis), et l'{@link #ECLAT}, en deux phases ;</li>
 *   <li>les Éclats dispersés : le {@link #RAMASSEUR} (Éclat), puis l'{@link #HERITIER} (Héritage), la
 *       {@link #VEUVE} (Valse) ou le {@link #NOTAIRE} (Scellé), et le {@link #COLLECTIONNEUR}, en trois phases ;</li>
 *   <li>chapitre 4 : le {@link #PILLEUR}, puis {@link #FAUSSAIRE} (Fausse monnaie), {@link #CARTOMANCIENNE}
 *       (Prédiction) ou {@link #DUELLISTE} (Duel), et le {@link #PRETENDANT} et ses trois éclats ;</li>
 *   <li>La Guerre des Tables : le {@link #SERGENT} (Rassemblement), puis le {@link #CANONNIER} (Canon à jetons),
 *       l'{@link #ESPIONNE} (Espionnage) ou le {@link #MERCENAIRE} (Contrat), et le {@link #GENERAL} en trois tiers ;</li>
 *   <li>chapitre 5 : le {@link #PORTIER}, puis {@link #COMPTABLE} (Taxe), {@link #DIRECTEUR} (Nouvelle
 *       règle) ou {@link #SECURITE} (Fouille), et la {@link #MAISON} en trois parties ;</li>
 *   <li>L'Ascension : le {@link #LIFTIER_TOUR} (Ascenseur), puis l'{@link #ECHO} (ton dernier tour), le {@link #VERTIGE}
 *       (une carte de moins) ou la {@link #GARGOUILLE} (Pétrification), et l'{@link #ARCHITECTE} et ses Plans, en trois phases ;</li>
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
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 2, EnemySymbol.POTION, 2, EnemySymbol.NOTHING_GOES, 1,
            EnemySymbol.RAKE, 1),
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

    // ----- Chapitre 3 : Les Pèlerins de la Comète (Tour à 10 chapitres, 2026-11-22) -----

    PASSEUR("Le Passeur du Gué", "PASSEUR",
        "Il garde le seul gué vers le cratère. Il fait payer le passage, ramasse ta part et pipe les dés.",
        30_000, 100, 25,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.TOLL, 1,
            EnemySymbol.RAKE, 1, EnemySymbol.LOADED_DIE, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.TREFLE, Card.Suit.CARREAU, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    PELERIN_FER("Le Pèlerin de fer", "PÈLERIN DE FER",
        "Il marche depuis des années dans son armure. À chaque Serment, il devient plus dur. Frappe-le tôt.",
        50_000, 120, 10,
        weights(EnemySymbol.IRON_VOW, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1),
        deck(new int[] {4, 8, 12}, new int[] {5, 11},
            new int[] {3, 6, 9, 12, 14}, new int[] {6, 12}),
        List.of(Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.CARREAU, Card.Suit.COEUR, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    DISEUSE("La Diseuse d'éclats", "DISEUSE",
        "Elle lit l'avenir dans les éclats de la comète. Elle voit ta main. Plus tu tiens de cartes, plus elle frappe.",
        50_000, 60, 15,
        weights(EnemySymbol.SIGHT, 2, EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 7, 11, 14}, new int[] {4, 9, 13},
            new int[] {5, 10}, new int[] {4, 8, 12}),
        List.of(Card.Suit.TREFLE, Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.TREFLE, Card.Suit.CARREAU, Card.Suit.PIQUE)),

    PORTEUR("Le Porteur de reliques", "PORTEUR",
        "Il porte sur son dos les reliques de la comète. Chacune bénit une couleur. Tes cartes de cette couleur lui obéissent.",
        50_000, 80, 15,
        weights(EnemySymbol.RELIC, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12}, new int[] {5, 11},
            new int[] {4, 8, 12, 14}, new int[] {5, 10}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.CARREAU, Card.Suit.COEUR, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    PROPHETE("Le Prophète du Cratère", "PROPHÈTE",
        "Il mène les pèlerins vers la lumière. Plus ils sont nombreux, plus il frappe. Blessé, il appelle la comète.",
        120_000, 120, 10,
        weights(EnemySymbol.PILGRIMAGE, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 2, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12, 14},
            new int[] {5, 9, 13}, new int[] {4, 8, 12}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    // ----- Chapitre 4 : Le Dernier Tirage -----

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
        "Le coeur de la comète. Il est le hasard. Blessé, il change de jeu.",
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

    // ----- Exploration : la Fête foraine -----

    CLOWN("Clown jongleur", "CLOWN JONGLEUR",
        "Il jongle avec tout. Avec ses massues. Avec tes cartes aussi.",
        50_000, 40, 25,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.JUGGLE, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    MONSIEUR_LOYAL("Monsieur Loyal", "MONSIEUR LOYAL",
        "Il annonce chaque numéro. Rate le sien, et son fouet claque deux fois.",
        100_000, 40, 25,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.ACT, 1, EnemySymbol.RAGE, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    SQUELETTE("Squelette farceur", "SQUELETTE",
        "Il surgit dans le noir. Bouh ! Ta main tremble.",
        50_000, 40, 25,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SCARE, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    CONDUCTEUR("Conducteur fantôme", "CONDUCTEUR",
        "Il conduit ce train depuis cent ans. Dans son tunnel, tes rouleaux ne se voient plus.",
        100_000, 40, 25,
        weights(EnemySymbol.SWORD, 1, EnemySymbol.TUNNEL, 1, EnemySymbol.FANG, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    FORAIN("Forain de la roue", "FORAIN",
        "Il pousse la roue d'une main. De l'autre, il relance ta chance.",
        50_000, 40, 25,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.TURNSTILE, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    ROUE_FORTUNE("Roue de la Fortune", "ROUE DE LA FORTUNE",
        "Une roue vivante. Où va-t-elle tomber ? Même elle ne le sait pas.",
        100_000, 40, 25,
        weights(EnemySymbol.BIG_WHEEL, 2, EnemySymbol.ZERO, 1, EnemySymbol.SWORD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    BONIMENTEUR("Bonimenteur", "BONIMENTEUR",
        "« Approchez ! Un lot à chaque coup ! » Tous ses lots sont truqués.",
        50_000, 40, 25,
        weights(EnemySymbol.SWORD, 1, EnemySymbol.RIGGED_PRIZE, 1, EnemySymbol.INTEREST, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    ROI_FORAIN("Roi des forains", "ROI DES FORAINS",
        "Il règne sur tous les stands. Contre lui, on ne gagne qu'avec une Paire.",
        100_000, 40, 25,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.RIGGED_GAME, 1, EnemySymbol.THORNS, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    // ----- Exploration : le Rapide de Minuit -----

    CHAUFFEUR("Chauffeur de chaudière", "CHAUFFEUR",
        "Il pellette le charbon sans s'arrêter. Sa chaudière monte en pression. Frappe-le fort avant qu'elle explose.",
        100_000, 40, 20,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.PRESSURE, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    LOCOMOTIVE("La Locomotive vivante", "LOCOMOTIVE",
        "La machine de tête a ouvert les yeux. Elle fonce. Sa chaudière n'a jamais été aussi chaude.",
        200_000, 80, 12,
        weights(EnemySymbol.PRESSURE, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.RAGE, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    SERVEUR("Serveur pickpocket", "SERVEUR",
        "Il sert le café avec le sourire. Il repart avec une de tes cartes.",
        100_000, 40, 20,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.PICKPOCKET, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    GRAND_CHEF("Le Grand Chef", "GRAND CHEF",
        "Il règne sur le wagon-restaurant. Chaque Banquet le soigne et le protège. Et ses serveurs ont les doigts agiles.",
        200_000, 80, 12,
        weights(EnemySymbol.BANQUET, 2, EnemySymbol.SWORD, 2, EnemySymbol.PICKPOCKET, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    SOMNAMBULE("Somnambule", "SOMNAMBULE",
        "Il marche les yeux fermés. Il fredonne une berceuse. Ta prochaine carte s'endort.",
        100_000, 40, 20,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.LULLABY, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    DAME_EN_NOIR("La Dame en noir", "DAME EN NOIR",
        "Elle voyage seule, voilette baissée. Elle chante pour t'endormir. On dit que ceux qui s'endorment ne descendent jamais du train.",
        200_000, 80, 12,
        weights(EnemySymbol.LULLABY, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    CONVOYEUR("Convoyeur de fonds", "CONVOYEUR",
        "Il garde l'or de la comète. Si tu ne le blesses pas assez, il te prend ton bouclier.",
        100_000, 40, 20,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.HOLDUP, 1, EnemySymbol.SHIELD, 2),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    DESPERADO("Le Desperado", "DESPERADO",
        "Il est monté sur le toit du fourgon. Il braque, il mise tout, il tire. Ne lui laisse pas le temps de viser.",
        200_000, 80, 12,
        weights(EnemySymbol.HOLDUP, 2, EnemySymbol.SWORD, 2, EnemySymbol.ALL_IN, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    // ----- Exploration : le Port des Contrebandiers -----

    RAT_CALES("Rat des cales", "RAT DES CALES",
        "Il vit dans le noir. Il ronge tout. Même tes cartes.",
        200_000, 40, 25,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.NIBBLE, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    CAPITAINE_RAT("Capitaine Rat", "CAPITAINE RAT",
        "Il commande la cale. Ses dents mordent. Ses rats rongent ta main.",
        400_000, 80, 15,
        weights(EnemySymbol.NIBBLE, 2, EnemySymbol.FANG, 1, EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    BUVEUR("Buveur", "BUVEUR",
        "Il titube. Il cogne. Son haleine te fait tourner la tête.",
        200_000, 40, 25,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.DRUNK, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    TAVERNIER("Tavernier", "TAVERNIER",
        "Il sert à boire. Toujours. Sous son Ivresse, tes rouleaux trinquent au pire.",
        400_000, 80, 15,
        weights(EnemySymbol.DRUNK, 2, EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    GUETTEUR("Guetteur", "GUETTEUR",
        "Il veille sur la côte. Sa lanterne t'éblouit. Tu joues sans voir.",
        200_000, 60, 25,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.BLIND, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    GARDIEN_PHARE("Gardien du Phare", "GARDIEN DU PHARE",
        "Son phare aveugle les navires. Tu joueras à l'aveugle. Ses Épines te renvoient tes coups.",
        400_000, 120, 15,
        weights(EnemySymbol.BLIND, 2, EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1, EnemySymbol.THORNS, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    PIRATE("Pirate", "PIRATE",
        "Sabre au clair. Il prend ce qu'il veut. Surveille ta main.",
        200_000, 30, 25,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.BOARDING, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    CAPITAINE_NOIR("Capitaine Noir", "CAPITAINE NOIR",
        "Le maître du galion. Il vole tes cartes. Chaque Rage le rend plus fort.",
        400_000, 60, 15,
        weights(EnemySymbol.BOARDING, 2, EnemySymbol.SWORD, 2, EnemySymbol.RAGE, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    // ----- Exploration : le Palace des Neiges -----

    GROOM("Groom", "GROOM",
        "Il porte ton bagage. Il ferme ta meilleure carte à clé. Il attend son pourboire.",
        350_000, 60, 15,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.KEY, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    CONCIERGE("Le Concierge", "CONCIERGE",
        "Il a toutes les clés de l'hôtel. Il les a toutes. Tes meilleures cartes restent au vestiaire.",
        700_000, 100, 10,
        weights(EnemySymbol.KEY, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.RAGE, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    PATINEUSE("Patineuse", "PATINEUSE",
        "Elle tourne sans fin sur la glace. Sous ses lames, tes rouleaux gèlent.",
        350_000, 60, 15,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.FREEZE, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    IMPERATRICE_GIVRE("L'Impératrice du Givre", "IMPÉRATRICE",
        "Elle règne sur la patinoire depuis cent ans. D'un geste, elle fige tes rouleaux. D'un autre, la neige tombe.",
        700_000, 100, 10,
        weights(EnemySymbol.FREEZE, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.AVALANCHE, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    LIFTIER("Liftier", "LIFTIER",
        "Il fait monter la cabine. La neige s'entasse sur le toit. Frappe-le fort avant qu'elle tombe.",
        350_000, 60, 15,
        weights(EnemySymbol.SWORD, 1, EnemySymbol.AVALANCHE, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    YETI("Le Yéti", "YÉTI",
        "Il vit au sommet du téléphérique. Chaque fois que tu le laisses tranquille, son Avalanche grossit.",
        700_000, 100, 10,
        weights(EnemySymbol.AVALANCHE, 2, EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1, EnemySymbol.RAGE, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    MAJORDOME("Majordome", "MAJORDOME",
        "Il sert le champagne glacé. Chaque coupe le soigne et le protège.",
        350_000, 60, 15,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.CHAMPAGNE, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    PRINCE_NEIGES("Le Prince des Neiges", "PRINCE DES NEIGES",
        "Il donne un bal éternel dans la suite royale. Le champagne coule. La glace monte. Personne ne repart.",
        700_000, 100, 10,
        weights(EnemySymbol.CHAMPAGNE, 2, EnemySymbol.SWORD, 2, EnemySymbol.FREEZE, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    // ----- Exploration : les Mines d'Or -----

    CHERCHEUR_OR("Chercheur d'or", "CHERCHEUR D'OR",
        "Il creuse. Il trie. Avec lui, tes gains ne valent que des cailloux.",
        500_000, 60, 10,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.NUGGET, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    BARON_OR("Baron de l'Or", "BARON DE L'OR",
        "Tout l'or de la mine est à lui. Ses Pépites changent tes gains en pierres. Ses Intérêts arment ses coups.",
        1_000_000, 100, 6,
        weights(EnemySymbol.NUGGET, 2, EnemySymbol.SWORD, 1, EnemySymbol.INTEREST, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    FOREUR("Foreur", "FOREUR",
        "Sa foreuse perce tout. Ton bouclier aussi.",
        500_000, 60, 10,
        weights(EnemySymbol.DRILL, 2, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    GRAND_FOREUR("Grand Foreur", "GRAND FOREUR",
        "Il creuse jusqu'au coeur de la montagne. Rien ne l'arrête. Chaque Rage le rend plus fort.",
        1_000_000, 100, 6,
        weights(EnemySymbol.DRILL, 2, EnemySymbol.SWORD, 1, EnemySymbol.RAGE, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    FORGERON("Forgeron", "FORGERON",
        "Chaque coup d'Enclume affûte son arme. Ne traîne pas.",
        500_000, 80, 10,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.ANVIL, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    MAITRE_FORGE("Maître de Forge", "MAÎTRE DE FORGE",
        "Il forge depuis mille ans. Son arme grandit à chaque Enclume. Pour toujours.",
        1_000_000, 120, 6,
        weights(EnemySymbol.ANVIL, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    GOLEM_OR("Golem d'or", "GOLEM D'OR",
        "Un tas d'or qui marche. Tant qu'il a plus de la moitié de ses PV, sa peau d'or encaisse la moitié de tes coups.",
        500_000, 100, 10,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 2, EnemySymbol.PLATING, 1, EnemySymbol.INGOT, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    COEUR_MONTAGNE("Coeur de la Montagne", "COEUR DE LA MONTAGNE",
        "La montagne est vivante. Tant qu'il a plus de la moitié de ses PV, il ne prend que la moitié de tes dégâts.",
        1_000_000, 150, 6,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 2, EnemySymbol.POTION, 1, EnemySymbol.ROCKFALL, 1,
            EnemySymbol.HEARTBEAT, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    // ----- Exploration : le Manoir hanté -----

    VALET_FANTOME("Valet fantôme", "VALET FANTÔME",
        "Il distribue les cartes depuis cent ans. Il ne demande qu'une goutte de ton sang. Puis une autre.",
        800_000, 70, 7,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.BLOOD_PACT, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    COMTE_MINUIT("Le Comte de Minuit", "COMTE DE MINUIT",
        "Le baron ruiné a vendu son âme pour une dernière partie. Il boit ton Sang. À minuit, il ne s'arrête plus.",
        1_600_000, 110, 4,
        weights(EnemySymbol.BLOOD_PACT, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    LIVRE_VIVANT("Livre vivant", "LIVRE VIVANT",
        "Ses pages claquent comme des dents. Il te reprend ta plus belle carte et la range sous ta pioche.",
        800_000, 70, 7,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.CURSE, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    BIBLIOTHECAIRE("La Bibliothécaire spectrale", "BIBLIOTHÉCAIRE",
        "Chut. Elle range chaque carte à sa place, sous ta pioche. Personne ne quitte sa bibliothèque avec un livre.",
        1_600_000, 110, 4,
        weights(EnemySymbol.CURSE, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    GOULE("Goule", "GOULE",
        "Elle sort de sa tombe à chaque coup de pioche. Drapée dans son linceul, elle se relève une fois.",
        800_000, 70, 7,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHROUD, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    FOSSOYEUR("Le Fossoyeur", "FOSSOYEUR",
        "Il a creusé toutes les tombes du manoir. La sienne est vide. Abats-le deux fois.",
        1_600_000, 110, 4,
        weights(EnemySymbol.SHROUD, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.RAGE, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    POUPEE_HANTEE("Poupée hantée", "POUPÉE HANTÉE",
        "Sa tête tourne toute seule. Elle choisit une de tes cartes, et tu la joues.",
        800_000, 70, 7,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.STRINGS, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    MARIONNETTISTE("Le Marionnettiste", "MARIONNETTISTE",
        "Là-haut, sous les combles, il tire les fils de tous les jouets du baron. Ce soir, il tire aussi les tiens.",
        1_600_000, 110, 4,
        weights(EnemySymbol.STRINGS, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    // ----- Exploration : le Casino Englouti -----

    BARMAN_NOYE("Barman noyé", "BARMAN NOYÉ",
        "Il sert sous l'eau. Il fredonne. Ta main n'obéit plus.",
        1_200_000, 80, 5,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SONG, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    SIRENE("Sirène du Bar", "SIRÈNE DU BAR",
        "Son Chant t'envoûte. Tu joues ce qu'elle veut.",
        2_400_000, 120, 3,
        weights(EnemySymbol.SONG, 2, EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    BANDIT_MANCHOT("Bandit manchot", "BANDIT MANCHOT",
        "Une machine à sous rouillée. Trois symboles pareils : Jackpot. Il frappe cinq fois plus fort.",
        1_200_000, 80, 5,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 2, EnemySymbol.SWALLOWED_COIN, 1, EnemySymbol.ARMED_ARM, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    JACKPOT_VIVANT("Jackpot Vivant", "JACKPOT VIVANT",
        "La plus grande machine du casino. Trois symboles pareils : Jackpot. Toute la salle tremble.",
        2_400_000, 120, 3,
        weights(EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1, EnemySymbol.RAGE, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    REQUIN("Requin", "REQUIN",
        "Il sent l'argent. Sa Morsure vide tes poches. Elle le soigne.",
        1_200_000, 80, 5,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.BANK_BITE, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    REQUIN_BANQUIER("Grand Requin Banquier", "REQUIN BANQUIER",
        "Il garde les coffres engloutis. Il dévore tes gains. Chaque pièce le soigne.",
        2_400_000, 120, 3,
        weights(EnemySymbol.BANK_BITE, 2, EnemySymbol.FANG, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    PIEUVRE("Pieuvre croupière", "PIEUVRE",
        "Huit bras pour distribuer. Elle joue cinq cartes à chaque tour.",
        1_200_000, 80, 5,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.ZERO, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    KRAKEN("Kraken", "KRAKEN",
        "Le maître de la salle VIP. Huit bras, huit cartes par tour. Il perd un bras à chaque huitième de ses PV.",
        2_400_000, 150, 3,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.FANG, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    // ----- Exploration : le Temple du Destin -----

    MOINE_HASARD("Moine du hasard", "MOINE DU HASARD",
        "Il sonne les cloches du parvis. À chaque coup de glas, un de tes rouleaux se fige.",
        1_500_000, 60, 15,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.KNELL, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    MAITRE_CLOCHES("Le Maître des cloches", "MAÎTRE DES CLOCHES",
        "Il règne sur le grand bourdon du temple. Quand il sonne le glas, tes rouleaux se taisent.",
        3_000_000, 100, 8,
        weights(EnemySymbol.KNELL, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    ORACLE_AVEUGLE("Oracle aveugle", "ORACLE AVEUGLE",
        "Ses yeux sont bandés. Il voit quand même ton avenir. Et il le noue à son fil.",
        1_500_000, 60, 15,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.FATE_THREAD, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    TISSEUSE("La Tisseuse de destins", "TISSEUSE DE DESTINS",
        "Elle file le destin de chaque joueur. Ton rouleau tourne, mais c'est elle qui choisit le symbole.",
        3_000_000, 100, 8,
        weights(EnemySymbol.FATE_THREAD, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    LANCEUR_DES("Lanceur de dés", "LANCEUR DE DÉS",
        "Il lance deux dés à chaque tour. Sur un double, il frappe deux fois plus fort.",
        1_500_000, 60, 15,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.DOUBLE_SIX, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    SEIGNEUR_DES("Le Seigneur des Dés", "SEIGNEUR DES DÉS",
        "Ses dés sont taillés dans l'or de la comète. Ils tombent toujours du bon côté. Le sien.",
        3_000_000, 100, 8,
        weights(EnemySymbol.DOUBLE_SIX, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.RAGE, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    STATUE_ANIMEE("Statue animée", "STATUE ANIMÉE",
        "La pierre s'est réveillée. Elle compte tes Bingos. Elle te les rendra.",
        1_500_000, 60, 15,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.KARMA, 1, EnemySymbol.SHIELD, 2),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    IDOLE_OR("L'Idole d'Or", "IDOLE D'OR",
        "Elle trône au coeur du sanctuaire. Chaque coup de chance a un prix : le Karma te le renvoie.",
        3_000_000, 100, 8,
        weights(EnemySymbol.KARMA, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    // ----- Exploration : le Désert de Braise -----

    CARAVANIER("Caravanier", "CARAVANIER",
        "Il mène les chameaux. Chaque pas dans le désert a un prix.",
        1_500_000, 40, 25,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.TOLL, 1, EnemySymbol.INTEREST, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    EMIR("Émir des sables", "ÉMIR DES SABLES",
        "Il prête de l'or à tout le monde. Puis il fait signer.",
        3_000_000, 40, 25,
        weights(EnemySymbol.SWORD, 1, EnemySymbol.BILL, 2, EnemySymbol.FANG, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    MOMIE("Momie", "MOMIE",
        "Elle dort depuis mille ans. Ses bandelettes ligotent tes cartes.",
        1_050_000, 40, 25,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.WRAP, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    PHARAON("Pharaon maudit", "PHARAON MAUDIT",
        "Il règne encore sur son tombeau. Blessé, il s'enferme dans son sarcophage.",
        2_100_000, 40, 25,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.WRAP, 1, EnemySymbol.POTION, 1, EnemySymbol.THORNS, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    SCORPION("Scorpion des dunes", "SCORPION",
        "Il attend sous le sable. Sa piqûre brûle longtemps.",
        1_500_000, 40, 25,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.VENOM, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    DJINN("Djinn du Mirage", "DJINN DU MIRAGE",
        "Il sort de sa lampe. Il t'offre de l'eau. Ce n'est qu'un mirage.",
        3_000_000, 40, 25,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.MIRAGE, 1, EnemySymbol.MIRROR, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    SALAMANDRE("Salamandre de lave", "SALAMANDRE",
        "Elle nage dans la lave. Ton bouclier fond à son contact.",
        1_500_000, 40, 25,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.BURN, 1, EnemySymbol.RAGE, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    DRAGON("Ignar, le Dragon du Volcan", "IGNAR",
        "Il dort sur l'or de la comète depuis la nuit des temps. Son souffle traverse tout.",
        3_000_000, 40, 25,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.FIRE_BREATH, 1, EnemySymbol.ANVIL, 1, EnemySymbol.RAGE, 1, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    // ----- Exploration : le Joueur Masqué, qui prend parfois la place du soldat (idée A2 du 2026-11-11) -----

    RIVAL_FOIRE("Le Joueur Masqué", "JOUEUR MASQUÉ",
        "Un joueur au masque doré. Lui aussi monte vers le Croupier. Il triche, et il aime ça.",
        200_000, 40, 25,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.RIGGED_GAME, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    RIVAL_RAPIDE("Le Joueur Masqué", "JOUEUR MASQUÉ",
        "Il a pris le même train que toi. Dans le fourgon, il a appris à braquer.",
        250_000, 80, 12,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.RIGGED_GAME, 1, EnemySymbol.HOLDUP, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    RIVAL_PORT("Le Joueur Masqué", "JOUEUR MASQUÉ",
        "Encore lui. Au port, il a appris à voler les cartes des autres.",
        500_000, 120, 15,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.RIGGED_GAME, 1, EnemySymbol.HOLDUP, 1, EnemySymbol.BOARDING, 1),
        deck(new int[] {3, 6, 9, 12, 13, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    RIVAL_PALACE("Le Joueur Masqué", "JOUEUR MASQUÉ",
        "Il a pris une chambre au Palace. Le Concierge lui a prêté ses clés.",
        800_000, 130, 10,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.RIGGED_GAME, 1, EnemySymbol.HOLDUP, 1, EnemySymbol.BOARDING, 1,
            EnemySymbol.KEY, 1),
        deck(new int[] {3, 6, 9, 12, 13, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    RIVAL_MINES("Le Joueur Masqué", "JOUEUR MASQUÉ",
        "Il est passé avant toi dans la mine. Ses coups percent ton bouclier.",
        1_250_000, 150, 6,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.RIGGED_GAME, 1, EnemySymbol.HOLDUP, 1, EnemySymbol.BOARDING, 1,
            EnemySymbol.KEY, 1, EnemySymbol.DRILL, 1),
        deck(new int[] {3, 6, 9, 12, 13, 14}, new int[] {4, 8, 12, 14},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    RIVAL_MANOIR("Le Joueur Masqué", "JOUEUR MASQUÉ",
        "Il a joué contre le Comte, au manoir. Il a appris à boire le Sang des autres.",
        2_000_000, 150, 4,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.RIGGED_GAME, 1, EnemySymbol.HOLDUP, 1, EnemySymbol.BOARDING, 1,
            EnemySymbol.KEY, 1, EnemySymbol.DRILL, 1, EnemySymbol.BLOOD_PACT, 1),
        deck(new int[] {3, 6, 9, 12, 13, 14}, new int[] {4, 8, 12, 14},
            new int[] {5, 9, 13}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    RIVAL_CASINO("Le Joueur Masqué", "JOUEUR MASQUÉ",
        "Sous l'eau, il a gagné contre les requins. Maintenant, il mord tes gains.",
        3_000_000, 150, 3,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.RIGGED_GAME, 1, EnemySymbol.HOLDUP, 1, EnemySymbol.BOARDING, 1,
            EnemySymbol.KEY, 1, EnemySymbol.DRILL, 1, EnemySymbol.BLOOD_PACT, 1, EnemySymbol.BANK_BITE, 1),
        deck(new int[] {3, 6, 9, 12, 13, 14}, new int[] {4, 8, 12, 14},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    RIVAL_TEMPLE("Le Joueur Masqué", "JOUEUR MASQUÉ",
        "Au temple, les moines lui ont appris à faire parler les dés. Il ne lance plus : il choisit.",
        4_000_000, 150, 3,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.RIGGED_GAME, 1, EnemySymbol.HOLDUP, 1, EnemySymbol.BOARDING, 1,
            EnemySymbol.KEY, 1, EnemySymbol.DRILL, 1, EnemySymbol.BLOOD_PACT, 1, EnemySymbol.BANK_BITE, 1, EnemySymbol.DOUBLE_SIX, 1),
        deck(new int[] {3, 6, 9, 12, 13, 14}, new int[] {4, 8, 12, 14},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11, 14}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    RIVAL_DESERT("Le Joueur Démasqué", "JOUEUR DÉMASQUÉ",
        "Il a ôté son masque. Le Croupier vous a tout appris, à lui comme à toi. Il ne restera qu'un seul élève.",
        6_000_000, 40, 25,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.RIGGED_GAME, 1, EnemySymbol.HOLDUP, 1, EnemySymbol.BOARDING, 1,
            EnemySymbol.KEY, 1, EnemySymbol.DRILL, 1, EnemySymbol.BLOOD_PACT, 1, EnemySymbol.BANK_BITE, 1, EnemySymbol.DOUBLE_SIX, 1, EnemySymbol.BILL, 1),
        deck(new int[] {3, 6, 9, 12, 13, 14}, new int[] {4, 8, 12, 13, 14},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11, 14}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    // ----- Chapitre des Éclats dispersés (entre le Dernier Tirage et le Monde sans Maître) -----
    RAMASSEUR("Le Ramasseur d'éclats", "RAMASSEUR",
        "Il fouille les ruines du cratère. Il ramasse chaque éclat. Au troisième, les éclats se rejoignent et frappent à travers ton bouclier.",
        180_000, 120, 5,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.SHARD, 2),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    HERITIER("L'Héritier ruiné", "HÉRITIER",
        "Il a tout perdu au jeu. Il réclame son héritage : ton bouclier. Il te le prend, puis il frappe.",
        280_000, 80, 4,
        weights(EnemySymbol.LEGACY, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    VEUVE("La Veuve joyeuse", "VEUVE JOYEUSE",
        "Elle a enterré trois maris. Elle porte le deuil en riant. Plus tu es blessé, plus sa Valse frappe fort.",
        280_000, 60, 4,
        weights(EnemySymbol.WALTZ, 2, EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    NOTAIRE("Le Notaire du hasard", "NOTAIRE",
        "Il enregistre chaque coup du hasard. Un coup de tampon, et tu joues une carte de moins au tour suivant.",
        280_000, 100, 4,
        weights(EnemySymbol.SEAL, 1, EnemySymbol.SWORD, 3, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    COLLECTIONNEUR("Le Collectionneur", "COLLECTIONNEUR",
        "Il a ramassé tous les éclats de l'Éclat Originel. Un éclat, un pouvoir. Chaque tiers de ses PV perdu, il en sort un nouveau de sa vitrine.",
        400_000, 130, 3,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.SHARD, 1, EnemySymbol.LEGACY, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
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

    // ----- Chapitre : La Guerre des Tables (entre le Monde sans Maître et la Maison) -----

    SERGENT("Le Sergent de table", "SERGENT",
        "Il recrute à chaque table. Chaque Rassemblement appelle un soldat de plus. Abats-les avant qu'ils soient trois.",
        360_000, 130, 5,
        weights(EnemySymbol.SWORD, 3, EnemySymbol.SHIELD, 2, EnemySymbol.POTION, 1, EnemySymbol.RALLY, 2),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    CANONNIER("Le Canonnier à jetons", "CANONNIER",
        "Ses boulets, ce sont tes jetons. Plus tu gagnes, plus son Canon a de quoi tirer.",
        630_000, 80, 4,
        weights(EnemySymbol.CANNON, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    ESPIONNE("L'Espionne", "ESPIONNE",
        "Elle a lu tes plans. Chaque carte que tu joues lui apprend où se protéger.",
        630_000, 100, 4,
        weights(EnemySymbol.SPY, 2, EnemySymbol.SWORD, 3, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    MERCENAIRE("Le Mercenaire", "MERCENAIRE",
        "Il se bat pour sa prime. Laisse-le tranquille un tour, et son Contrat frappe trois fois plus fort.",
        630_000, 60, 4,
        weights(EnemySymbol.CONTRACT, 2, EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    GENERAL("Le Général Va-Tout", "GÉNÉRAL VA-TOUT",
        "Il commande les armées des Maisons. Ses soldats, ses canons, puis ses lieutenants. Au dernier tiers de ses PV, il fait Va-tout : plus de défense, tout sur l'attaque.",
        1_250_000, 140, 3,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 2, EnemySymbol.POTION, 1, EnemySymbol.RALLY, 1,
            EnemySymbol.CANNON, 1, EnemySymbol.ALL_OUT, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    // ----- Chapitre 5 : La Maison -----

    PORTIER("Le Portier", "PORTIER",
        "Il filtre l'entrée. Une famille de cartes reste dehors tout le combat.",
        400_000, 150, 4,
        weights(EnemySymbol.SWORD, 4, EnemySymbol.SHIELD, 2, EnemySymbol.POTION, 1, EnemySymbol.CLOAKROOM, 1,
            EnemySymbol.BOUNCER, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    COMPTABLE("Le Comptable", "COMPTABLE",
        "Il compte tout. Sa Taxe frappe chaque carte jouée. Même à sec.",
        700_000, 80, 4,
        weights(EnemySymbol.TAX, 2, EnemySymbol.SWORD, 4, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    DIRECTEUR("Le Directeur des Jeux", "DIRECTEUR",
        "Il change les règles. Pour deux tours, le jeu n'est plus le même.",
        700_000, 80, 4,
        weights(EnemySymbol.NEW_RULE, 1, EnemySymbol.SWORD, 4, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    SECURITE("La Sécurité", "SÉCURITÉ",
        "On t'a repéré. Elle fouille ton deck. Plus tu gagnes gros, plus elle confisque.",
        700_000, 120, 4,
        weights(EnemySymbol.FRISK, 2, EnemySymbol.SWORD, 4, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    MAISON("La Maison", "LA MAISON",
        "Pas un ennemi. Un lieu vivant. La Façade protège, le Coffre soigne, la Salle de jeu frappe. La Maison gagne toujours : une fois, elle annule ton plus gros coup.",
        1_400_000, 150, 3,
        weights(EnemySymbol.SHIELD, 2, EnemySymbol.SWORD, 4, EnemySymbol.POTION, 1, EnemySymbol.LOAD_BEARING, 1,
            EnemySymbol.SAFE, 1, EnemySymbol.HOUSE_EDGE, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    // ----- L'Ascension (avant-dernier chapitre) -----

    LIFTIER_TOUR("Le Liftier de la Tour", "LIFTIER",
        "Il tient l'ascenseur de la Tour. Chaque étage le rend plus fort. Frappe fort pour le renvoyer en bas.",
        450_000, 120, 4,
        weights(EnemySymbol.SWORD, 3, EnemySymbol.SHIELD, 2, EnemySymbol.POTION, 1, EnemySymbol.ELEVATOR, 2),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    ECHO("L'Écho", "ÉCHO",
        "Il n'a pas de voix. Il a la tienne. Il te rejoue ton dernier tour.",
        800_000, 80, 4,
        weights(EnemySymbol.ECHO, 2, EnemySymbol.SWORD, 3, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    VERTIGE("Le Vertige", "VERTIGE",
        "Il vit au bord du vide. Ne regarde pas en bas. Tes mains tremblent : tu joues moins de cartes.",
        750_000, 100, 4,
        weights(EnemySymbol.VERTIGO, 2, EnemySymbol.SWORD, 3, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    GARGOUILLE("La Gargouille", "GARGOUILLE",
        "Elle garde la corniche. Changée en pierre, elle encaisse la moitié de tes coups.",
        800_000, 150, 4,
        weights(EnemySymbol.STONE, 2, EnemySymbol.SWORD, 3, EnemySymbol.SHIELD, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),
    ARCHITECTE("L'Architecte", "L'ARCHITECTE",
        "Il a bâti la Tour et la Machine. Chaque Plan ajoute un étage à sa Tour, et chaque étage le protège. "
            + "Tes Bingos les font tomber.",
        1_700_000, 150, 3,
        weights(EnemySymbol.SWORD, 3, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.BLUEPRINT, 2),
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
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.RAGE, 1,
            EnemySymbol.GEAR, 1, EnemySymbol.SHORT_CIRCUIT, 1, EnemySymbol.GOLDEN_COMET, 1, EnemySymbol.ASPIRATION, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)),

    // ----- Boss secret : le Croupier, ouvert après le Désert de Braise et le chapitre 6 difficile -----

    GRAND_CROUPIER("Le Croupier", "LE CROUPIER",
        "Il t'a tout appris. Il tient toutes les machines. Il joue avec ton deck. Blessé, il truque ta table. "
            + "Presque vaincu, il joue cartes sur table.",
        2_000_000, 150, 2,
        weights(EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.RAGE, 1,
            EnemySymbol.MIRROR, 1),
        deck(new int[] {3, 6, 9, 12, 14}, new int[] {4, 8, 12},
            new int[] {5, 9, 13, 14}, new int[] {3, 7, 11}),
        List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR),
        List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE));

    /**
     * Le Croupier, boss secret (idée A1 du 2026-11-11, valeurs par défaut de Claude) : plus fort que le
     * boss du mode sans fin à l'étage 5 (force x351, 18 Md PV) : force x360, 20 Md PV, tirage à 5 000.
     * Modèle de Puissance (2026-11-18) : PV x0,05 comme le mode sans fin, 1 Md.
     */
    public static final long CROUPIER_HP = 1_000_000_000L;
    public static final int  CROUPIER_POWER = 36_000;
    public static final int  CROUPIER_SPIN_COST = 5_000;
    /** Phase 2 du Croupier : chaque table truquée tient ce nombre de tirages du joueur. */
    public static final int  CROUPIER_RIG_TURNS = 2;
    /** Phase 3 du Croupier : il voit ta main et joue cette carte de plus à chaque tour. */
    public static final int  CROUPIER_OPEN_PLAYS = 1;

    /** Force des ennemis du chapitre 2, en % : leurs attaques, défenses et effets sont multipliés d'autant. */
    /** Force gagnée à chaque chapitre de la Tour, en % (chapitre n : n x 100 %). */
    public static final int CHAPTER_POWER_STEP = 100;
    public static final int CHAPTER_2_POWER = 2 * CHAPTER_POWER_STEP;
    /** Multiplicateur des PV des ennemis de la Tour des épreuves avant le modèle de Puissance (le joueur gagne des rangs). */
    public static final int TOWER_HP_FACTOR = 5;
    /**
     * PV des ennemis de la Tour des épreuves en mode normal, en % de ceux de leur fiche, chapitre par chapitre.
     * Modèle de Puissance (Astra, 2026-11-18) : les bonus d'attaque s'additionnent, les PV redescendent ; réglés au
     * simulateur pour garder les victoires du joueur moyen à chaque rang (avant : 500 % partout).
     */
    // (PV de chaque chapitre : Chapter.Stats#hpPercent)
    /** Force des ennemis du chapitre 3, en %. */
    public static final int CHAPTER_3_POWER = 3 * CHAPTER_POWER_STEP;
    /** Force des ennemis des chapitres 4, 5 et 6, en % (choix d'Astra : le rang rendra le joueur plus fort). */
    public static final int CHAPTER_4_POWER = 4 * CHAPTER_POWER_STEP;
    public static final int CHAPTER_5_POWER = 5 * CHAPTER_POWER_STEP;
    public static final int CHAPTER_6_POWER = 6 * CHAPTER_POWER_STEP;

    /**
     * Mode difficile de la Tour (« Nouveau tirage + », ouvert après la Machine
     * Originelle), choix d'Astra (2026-11-06) : PV du boss de chaque chapitre, du 1er au 6e.
     * Les autres ennemis du chapitre gardent leur rapport de PV avec leur boss.
     */
    // (PV du boss de chaque chapitre : Chapter.Stats#hardBossHp ; modèle de Puissance, 2026-11-18)

    /**
     * Force des ennemis de la Tour en mode difficile, en %, du chapitre 1 au 6 (choix d'Astra, 2026-11-06) :
     * le chapitre 2 frappe deux fois plus fort que le chapitre 6 du mode normal, puis x2 à chaque chapitre.
     * Le chapitre 1 a la force du chapitre 6 du mode normal (x6), pour que la chaîne x2 parte de là.
     */
    // (force de chaque chapitre : Chapter.Stats#hardPower ; x1,47 par chapitre depuis la Tour à 10 chapitres)

    /**
     * Mode sans fin (2026-11-08, Astra : plus fort que le chapitre 6 difficile) : à l'étage 1,
     * la force et les PV du chapitre 6 difficile x{@link #ENDLESS_START_PERCENT} %, puis
     * +{@link #ENDLESS_STEP_PERCENT} % (composés) à chaque étage.
     */
    public static final int ENDLESS_START_PERCENT = 125, ENDLESS_STEP_PERCENT = 10;
    /** Coût d'un tirage dans le mode sans fin : celui du chapitre 6. */
    public static final int ENDLESS_SPIN_COST = 5_000;
    /** PV de base d'un ennemi et d'un boss du mode sans fin, avant l'étage (chapitre 6 difficile : boss 500 M). */
    static final long ENDLESS_ENEMY_HP = 250_000_000L, ENDLESS_BOSS_HP = 500_000_000L; // x0,05 (Puissance, 2026-11-18)
    /** Force et PV plafonnés, pour que les calculs restent justes aux étages extrêmes. */
    static final int ENDLESS_MAX_POWER = 1_000_000_000;
    static final long ENDLESS_MAX_HP = Long.MAX_VALUE / 1_000;

    /** @return le multiplicateur de l'étage {@code floor} du mode sans fin par rapport au chapitre 6 difficile. */
    public static double endlessFactor(int floor) {
        return ENDLESS_START_PERCENT / 100.0 * Math.pow(1 + ENDLESS_STEP_PERCENT / 100.0, floor - 1);
    }

    /** @return la force des ennemis de l'étage {@code floor} du mode sans fin, en %. */
    public static int endlessPower(int floor) {
        return (int) Math.min(ENDLESS_MAX_POWER, Math.round(Chapter.last().getStats().hardPower() * endlessFactor(floor)));
    }

    /** @return les PV d'un ennemi (ou d'un boss) de l'étage {@code floor} du mode sans fin. */
    public static long endlessHp(int floor, boolean boss) {
        double hp = (boss ? ENDLESS_BOSS_HP : ENDLESS_ENEMY_HP) * endlessFactor(floor);
        return hp >= ENDLESS_MAX_HP ? ENDLESS_MAX_HP : Math.round(hp);
    }

    /** @return {@code true} pour le boss d'un chapitre de la Tour. */
    public boolean isTowerBoss() {
        Chapter chapter = Chapter.of(this);
        return chapter != null && chapter.getBoss() == this;
    }

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

    /**
     * Symboles du Prophète du Cratère dans sa deuxième phase (valeurs par défaut de Claude) : il appelle la Comète Dorée,
     * qu'il a vue tomber, et ses pèlerins prêtent serment.
     */
    private static final Map<EnemySymbol, Integer> PROPHET_PHASE_TWO = Collections.unmodifiableMap(weights(
        EnemySymbol.PILGRIMAGE, 2, EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1, EnemySymbol.GOLDEN_COMET, 1,
        EnemySymbol.IRON_VOW, 1));

    /**
     * Symboles de la Maison sans sa Façade (plus de Boucliers ni de Mur porteur), puis sans
     * son Coffre (plus de Potions ni de Coffre-fort) : la Salle de jeu garde son Avantage.
     */
    private static final Map<EnemySymbol, Integer> HOUSE_PHASE_TWO = Collections.unmodifiableMap(weights(
        EnemySymbol.SWORD, 2, EnemySymbol.POTION, 1, EnemySymbol.SAFE, 1, EnemySymbol.HOUSE_EDGE, 1));
    private static final Map<EnemySymbol, Integer> HOUSE_PHASE_THREE = Collections.unmodifiableMap(weights(
        EnemySymbol.SWORD, 2, EnemySymbol.HOUSE_EDGE, 1));
    /** Le Général Va-Tout, sous les deux tiers de ses PV : ses lieutenants le rejoignent (l'Espionne et le Mercenaire). */
    private static final Map<EnemySymbol, Integer> GENERAL_PHASE_TWO = Collections.unmodifiableMap(weights(
        EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 2, EnemySymbol.POTION, 1, EnemySymbol.RALLY, 1, EnemySymbol.CANNON, 1,
        EnemySymbol.SPY, 1, EnemySymbol.CONTRACT, 1, EnemySymbol.ALL_OUT, 1));
    /** Le Général Va-Tout, sous le tiers de ses PV : il fait Va-tout, plus de Bouclier ni de Potion, tout sur l'attaque. */
    private static final Map<EnemySymbol, Integer> GENERAL_PHASE_THREE = Collections.unmodifiableMap(weights(
        EnemySymbol.SWORD, 3, EnemySymbol.RALLY, 1, EnemySymbol.CANNON, 1, EnemySymbol.CONTRACT, 1, EnemySymbol.ALL_OUT, 2));

    /**
     * Symboles du Collectionneur à ses phases 2 et 3 : à chaque tiers de ses PV perdu, il sort un nouvel éclat
     * de sa vitrine, et gagne son pouvoir (la Valse, puis le Scellé), en plus des siens.
     */
    private static final Map<EnemySymbol, Integer> COLLECTOR_PHASE_TWO = Collections.unmodifiableMap(weights(
        EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.SHARD, 1, EnemySymbol.LEGACY, 1,
        EnemySymbol.WALTZ, 1));
    private static final Map<EnemySymbol, Integer> COLLECTOR_PHASE_THREE = Collections.unmodifiableMap(weights(
        EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.SHARD, 1, EnemySymbol.LEGACY, 1,
        EnemySymbol.WALTZ, 1, EnemySymbol.SEAL, 1));

    /**
     * Symboles de l'Architecte : ses échafaudages montent (phase 2, l'Écho de l'Ascension), puis la clé de voûte
     * (phase 3, le Vertige) ; il garde ses Plans.
     */
    private static final Map<EnemySymbol, Integer> ARCHITECT_PHASE_TWO = Collections.unmodifiableMap(weights(
        EnemySymbol.SWORD, 3, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.BLUEPRINT, 2, EnemySymbol.ECHO, 1));
    private static final Map<EnemySymbol, Integer> ARCHITECT_PHASE_THREE = Collections.unmodifiableMap(weights(
        EnemySymbol.SWORD, 3, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.BLUEPRINT, 2, EnemySymbol.ECHO, 1,
        EnemySymbol.VERTIGO, 1));

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
     *         rend bien plus fort ; en mode difficile, ceux de {@link Chapter.Stats#hardBossHp()} pour le boss
     *         du chapitre, et autant en proportion pour les autres ennemis du chapitre
     */
    public long getMaxHp(Difficulty hard) {
        if (this == GRAND_CROUPIER) return CROUPIER_HP;
        if (hard.isEndless()) return endlessHp(hard.floor(), isTowerBoss());
        Place place = Place.of(this);
        if (place != null) {
            long hp = (long) maxHp * place.getHpPercent() / 100;
            // Exploration difficile : PV du lieu ramenés à ceux du Désert, puis x2 par lieu.
            return hard.hard() && !hard.isEndless() ? Math.round(hp * HardExploration.hpFactor(place)) : hp;
        }
        Chapter chapter = Chapter.of(this);
        if (chapter == null) return maxHp;
        if (!hard.hard()) return (long) maxHp * chapter.getStats().hpPercent() / 100;
        long bossHp = chapter.getStats().hardBossHp();
        int  bossBase = chapter.getBoss().maxHp;
        return this == chapter.getBoss() ? bossHp : Math.round((double) bossHp * maxHp / bossBase);
    }

    /** @return les PV du boss du chapitre {@code number} de la Tour en mode difficile. */
    public static long hardBossHp(int number) { return Chapter.values()[number - 1].getStats().hardBossHp(); }

    /** @return {@code true} pour un ennemi de la Tour des épreuves (voir {@link fr.astratime.lucky.entities.tower.Chapter}). */
    public boolean isTower() {
        return Chapter.of(this) != null;
    }
    /**
     * @return sa défense de base (renforcée par sa {@linkplain #getPower(boolean) force}), reformée à chacun de ses tours :
     *         en Exploration, celle de son lieu
     */
    public int getBaseDefense(Difficulty hard) {
        Place place = Place.of(this);
        return fortified(place != null && place.getShieldDefense() > 0 ? place.getShieldDefense() : baseDefense, hard);
    }

    /**
     * @return la défense {@code value} renforcée par sa {@linkplain #getPower(Difficulty) force} et, en Exploration
     *         difficile, par la défense de son lieu difficile (voir {@link HardExploration#defenseFactor})
     */
    public int fortified(int value, Difficulty hard) {
        int empowered = empowered(value, hard);
        if (!HardExploration.applies(this, hard)) return empowered;
        return (int) Math.min(Integer.MAX_VALUE, Math.round(empowered * HardExploration.defenseFactor(Place.of(this))));
    }

    /**
     * @return sa force, en % : le multiplicateur de ses attaques, de ses défenses
     *         et de tous ses effets (100 au chapitre 1, puis plus à chaque chapitre ;
     *         celle de {@link Chapter.Stats#hardPower()} dans la Tour en mode difficile)
     */
    public int getPower(Difficulty hard) {
        if (this == GRAND_CROUPIER) return CROUPIER_POWER;
        if (hard.isEndless()) return endlessPower(hard.floor());
        Chapter chapter = Chapter.of(this);
        return chapter != null && hard.hard() ? chapter.getStats().hardPower() : chapterPower();
    }

    /** @return la force des ennemis du chapitre {@code number} de la Tour en mode difficile, en %. */
    public static int hardPower(int number) { return Chapter.values()[number - 1].getStats().hardPower(); }

    /**
     * @return le coût d'un tirage contre lui, en gains : celui de son lieu en
     *         Exploration ; dans la Tour, selon le chapitre (100 aux chapitres 1
     *         et 2, 500 aux 3 et 4, 2 000 au 5, 5 000 au 6, mode difficile compris) ;
     *         100 à l'Entraînement
     */
    public int getSpinCost(Difficulty hard) {
        if (HardExploration.applies(this, hard)) return HardExploration.spinCost(Place.of(this));
        return getSpinCost();
    }

    /** @return le coût d'un tirage contre lui en mode normal (voir {@link #getSpinCost(Difficulty)}). */
    public int getSpinCost() {
        if (this == GRAND_CROUPIER) return CROUPIER_SPIN_COST;
        Place place = Place.of(this);
        if (place != null) return place.getSpinCost();
        Chapter chapter = Chapter.of(this);
        return chapter == null ? SpinEconomy.BASE_SPIN_COST : chapter.getStats().spinCost();
    }

    /** @return sa force en mode normal, en % : celle de son chapitre de la Tour, 100 hors de la Tour. */
    private int chapterPower() {
        Chapter chapter = Chapter.of(this);
        return chapter == null ? CHAPTER_POWER_STEP : chapter.getStats().power();
    }

    /** Dégâts de base d'une Épée du croupier d'entraînement. */
    public static final int TRAINING_SWORD_DAMAGE = 10;

    /** @return les dégâts de base de son Épée, avant sa {@linkplain #getPower(boolean) force}. */
    public int swordDamage() {
        if (this == ENTRAINEMENT) return TRAINING_SWORD_DAMAGE;
        Place place = Place.of(this);
        return place != null && place.getSwordDamage() > 0 ? place.getSwordDamage() : EnemySymbol.SWORD_DAMAGE;
    }

    /**
     * @return la défense d'un de ses Boucliers, avant sa {@linkplain #getPower(boolean) force} : en Exploration,
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

    /** @return {@code value} multiplié par sa {@linkplain #getPower(boolean) force} (arrondi). */
    public int empowered(int value, Difficulty hard) {
        return (int) Math.min(Integer.MAX_VALUE, Math.round((double) value * getPower(hard) / 100));
    }

    /**
     * @return la part de ses coups qui porte, en % : dans la prairie, réglée donjon
     *         par donjon pour qu'un joueur sans rang en gagne environ 7 sur 10
     *         (simulateur d'équilibrage) ; {@link #TOWER_LATE_DAMAGE} dans la Tour à
     *         partir du chapitre 3 ; 100 partout ailleurs
     */
    public int damagePercent() {
        return switch (this) {
            case SOLDAT_PIQUE,   ROI_PIQUE   -> PIQUE_DAMAGE;
            case SOLDAT_TREFLE,  ROI_TREFLE  -> TREFLE_DAMAGE;
            case SOLDAT_COEUR,   ROI_COEUR   -> COEUR_DAMAGE;
            case SOLDAT_CARREAU, ROI_CARREAU -> CARREAU_DAMAGE;
            case FORAIN, ROUE_FORTUNE -> WHEEL_DAMAGE;
            case PIRATE, CAPITAINE_NOIR -> GALLEON_DAMAGE;
            case FOREUR, GRAND_FOREUR -> SHAFT_DAMAGE;
            case BANDIT_MANCHOT, JACKPOT_VIVANT -> MACHINES_DAMAGE;
            case BARMAN_NOYE, SIRENE -> BAR_DAMAGE;
            case REQUIN, REQUIN_BANQUIER -> VAULTS_DAMAGE;
            case SCORPION, DJINN -> OASIS_DAMAGE;
            case GRAND_CROUPIER -> TOWER_LATE_DAMAGE;
            case RIVAL_FOIRE, RIVAL_RAPIDE, RIVAL_PORT, RIVAL_PALACE, RIVAL_MINES, RIVAL_MANOIR, RIVAL_CASINO, RIVAL_TEMPLE, RIVAL_DESERT ->
                Place.of(this).getDamagePercent() * rivalDamage(Place.of(this)) / 100;
            default -> {
                if (isTower()) yield Chapter.of(this).getStats().damage();
                Place place = Place.of(this);
                yield place != null && place.getDamagePercent() > 0 ? place.getDamagePercent() : 100;
            }
        };
    }

    /**
     * Dégâts des ennemis de la Tour à partir du chapitre 3, en % (choix d'Astra, 2026-10-11 :
     * l'Épée du chapitre 3 frappe à 100, soit 20 x3 x1,67). Les chapitres 4 à 6 gardent le même
     * écart avec le chapitre 3 : Épée à 134, 167 et 200. En mode difficile, toute la Tour frappe à cette part.
     */
    static final int TOWER_LATE_DAMAGE = 167;
    /**
     * Dégâts des ennemis de la Tour en mode normal, en %, chapitre par chapitre (à partir du chapitre 3). Chapitres par
     * rang (Astra, 2026-11-18) : le 4 pour le Gros bonnet (x1,3), le 5 trop dur pour lui, gagnable par le Baron et le
     * Magnat (Épée à 300, choix d'Astra le 2026-11-19), le 6 pour le Roi du casino et la Légende. Des coups plus forts plutôt que des PV en plus :
     * le combat devient plus dangereux sans s'allonger.
     */
    // (part de chaque chapitre : Chapter.Stats#damage)

    /**
     * Dégâts des donjons réglés à part dans les autres lieux, en % (simulateur, 2026-10-24 ; relevés avec leur lieu
     * le 2026-10-25, choix d'Astra : Foire +5 et Port +12 par Épée, lieux suivants +30 %) : la Grande Roue
     * (Fête foraine), le Galion (Port), le Puits (Mines), la Salle des machines et les Coffres (Casino), l'Oasis (Désert).
     * Le Désert frappe deux fois plus fort depuis le 2026-10-30 (Astra : trop facile pour un joueur qui maîtrise ses combos).
     * Même jour, choix d'Astra : Port x1,3, Mines x1,5, Casino x2,5, pour que le rang d'avant n'y gagne plus qu'environ
     * 25 % des descentes avec le « joueur malin » du simulateur.
     * Les autres ennemis d'un lieu frappent à la part de leur lieu ({@link Place#getDamagePercent()}).
     */
    static final int WHEEL_DAMAGE = 77, GALLEON_DAMAGE = 122, SHAFT_DAMAGE = 58, MACHINES_DAMAGE = 32,
        VAULTS_DAMAGE = 87, OASIS_DAMAGE = 118;

    /**
     * Dégâts du Bar (Barman noyé, Sirène), en % (Astra, 2026-10-29 : le Bar frappait trop peu et ses combats
     * s'éternisaient ; 34 au lieu des 24 du Casino, simulateur : un Magnat le gagne aussi souvent que la Salle VIP).
     * x2,5 avec le Casino le 2026-10-30.
     */
    static final int BAR_DAMAGE = 85;

    /**
     * Le Joueur Masqué (simulateur, joueur malin, un combat à PV pleins, 2026-11-14) : sa part qui porte, en % de celle
     * de son lieu, de la Foire au Désert, pour qu'il soit au moins aussi dur que la moyenne des rois du lieu
     * (il n'a pas leur phase 2, qui frappe 25 % plus fort) ; le duel final du Désert, plus dur encore.
     */
    static int rivalDamage(Place place) {
        return switch (place) {
            case FOIRE  -> 185;
            case RAPIDE -> 150;
            case PORT   -> 125;
            case PALACE -> 130;
            case MINES  -> 130;
            case MANOIR -> 120;
            case CASINO -> 110;
            case TEMPLE -> 130;
            case DESERT -> 150;
            case PRAIRIE -> 100;
        };
    }

    /** Dégâts des ennemis des donjons de la prairie, en % (voir {@link #damagePercent()}). */
    static final int PIQUE_DAMAGE = 48, TREFLE_DAMAGE = 165, COEUR_DAMAGE = 210, CARREAU_DAMAGE = 165;

    /** @return l'attaque {@code value} renforcée par sa {@linkplain #getPower(boolean) force}, puis à la {@linkplain #damagePercent() part qui porte}. */
    /**
     * Drain de vie relatif (2026-11-20, voir {@link fr.astratime.lucky.entities.LifeDrain}) : avant, le drain rendait
     * plusieurs fois la vie du joueur à chaque tour, et les coups des ennemis avaient été réglés en comptant dessus
     * (simulateur : sans Coeur, un deck Pique gagnait 2 % des descentes du Désert au Roi du casino). Le drain ne
     * soigne plus qu'une part des PV max du joueur, donc les ennemis frappent moins fort : part de leurs coups gardée,
     * en %, lieu par lieu (Exploration, de la prairie au Désert ; le mode difficile suit, il se règle sur le Désert)
     * pour que le « joueur malin » du simulateur gagne aussi souvent qu'avant dans le lieu de son rang (±5 points).
     */
    // (part de chaque lieu : Place#getDrainScale)
    /** Pareil pour la Tour des épreuves, chapitre par chapitre ; en difficile, toute la Tour prend la part du chapitre 6. */
    // (part de chaque chapitre : Chapter.Stats#drainScale)

    public int hurt(int value, Difficulty hard) {
        int empowered = empowered(value, hard);
        // Mode difficile : toute la Tour frappe comme ses derniers chapitres, pour que sa force compte en entier.
        int percent = hard.hard() && isTower() ? TOWER_LATE_DAMAGE : damagePercent();
        double factor = HardExploration.applies(this, hard) ? HardExploration.attackFactor(Place.of(this)) : 1;
        double scaled = percent * drainScale(hard) / 100.0; // drain de vie relatif : les ennemis frappent moins fort
        if (scaled == 100 && factor == 1) return empowered;
        return (int) Math.min(Integer.MAX_VALUE, Math.round(empowered * scaled / 100 * factor));
    }

    /** @return la part de ses coups gardée depuis le drain de vie relatif, en % (voir {@link #EXPLORATION_DAMAGE_SCALE}). */
    int drainScale(Difficulty hard) {
        if (isTower()) {
            // toute la Tour difficile frappe comme le dernier chapitre
            if (hard.hard()) return Chapter.last().getStats().drainScale();
            return Chapter.of(this).getStats().drainScale();
        }
        Place place = Place.of(this);
        return place == null ? 100 : place.getDrainScale();
    }

    /** @return sa force en multiplicateur affichable (ex : "x1,5"). */
    public String powerText(Difficulty hard) {
        float factor = getPower(hard) / 100f;
        String text = factor == Math.round(factor) ? String.valueOf(Math.round(factor)) : String.valueOf(factor);
        return "x" + Lang.decimal(text);
    }

    /** @return la part des dégâts du joueur renvoyée par chaque Épines, en %. */
    public int thornsPercent(Difficulty hard) { return empowered(EnemySymbol.THORNS_PERCENT, hard); }

    /** @return les dégâts maximum que ses Épines renvoient en un tour. */
    public int thornsMax(Difficulty hard) {
        int max = empowered(EnemySymbol.THORNS_MAX, hard);
        if (!HardExploration.applies(this, hard)) return max;
        return (int) Math.min(Integer.MAX_VALUE, Math.round(max * HardExploration.attackFactor(Place.of(this))));
    }

    /** @return la part des jauges du joueur vidée par chaque Dé pipé, en %. */
    public int diePercent(Difficulty hard) { return Math.min(100, empowered(EnemySymbol.DIE_PERCENT, hard)); }

    /** @return la part des gains du joueur rendue fausse par chaque Fausse monnaie, en %. */
    public int fakePercent(Difficulty hard) { return Math.min(100, empowered(EnemySymbol.FAKE_PERCENT, hard)); }

    /** @return la part des gains du joueur prise par chaque Intérêts, en %. */
    public int interestPercent(Difficulty hard) { return Math.min(100, empowered(EnemySymbol.INTEREST_PERCENT, hard) + EnemySymbol.THEFT_BONUS_PERCENT); }

    /** @return la part des gains que prend sa Morsure bancaire, en % (sa puissance, plus le vol relevé). */
    public int bankBitePercent(Difficulty hard) { return Math.min(100, empowered(EnemySymbol.BANK_BITE_PERCENT, hard) + EnemySymbol.THEFT_BONUS_PERCENT); }

    /** @return {@code true} s'il a la capacité {@code trait} (voir {@link EnemyTrait}). */
    public boolean has(EnemyTrait trait) { return trait.of(this); }

    /** @return {@code true} si son deck contient le Rouleau interdit (l'Éclat Originel). */
    public boolean forbidsReels() { return has(EnemyTrait.FORBIDDEN_REEL); }
    /** @return les cartes qu'il joue à chaque tour, parmi celles piochées. */
    public int getPlaysPerTurn() {
        return switch (this) {
            case PIEUVRE -> OCTOPUS_PLAYS;
            case KRAKEN  -> KRAKEN_ARMS;
            default      -> Enemy.PLAYS_PER_TURN;
        };
    }
    /**
     * @return les cartes propres qu'il joue au plus à chaque tour, en plus de ses cartes
     *         habituelles : une, ou jusqu'à 3 pour les ennemis du chapitre 6 (choix d'Astra)
     */
    public int getOwnPlaysPerTurn() {
        Chapter chapter = Chapter.of(this);
        return chapter == null ? 1 : chapter.getStats().ownPlays();
    }
    /** @return {@code true} pour le Joueur Masqué, à l'une de ses cinq rencontres (voir {@link fr.astratime.lucky.entities.exploration.Rival}). */
    public boolean isRival() {
        return switch (this) {
            case RIVAL_FOIRE, RIVAL_RAPIDE, RIVAL_PORT, RIVAL_PALACE, RIVAL_MINES, RIVAL_MANOIR, RIVAL_CASINO, RIVAL_TEMPLE, RIVAL_DESERT -> true;
            default -> false;
        };
    }

    /** @return {@code true} pour le boss d'un chapitre ou le roi d'un donjon. */
    public boolean isBoss() {
        return switch (this) {
            case COMETE, REINE, PROPHETE, ECLAT, COLLECTIONNEUR, PRETENDANT, GENERAL, MAISON, ARCHITECTE, MACHINE_ORIGINELLE, GRAND_CROUPIER, ROI_PIQUE, ROI_COEUR, ROI_CARREAU, ROI_TREFLE,
                 CAPITAINE_RAT, TAVERNIER, GARDIEN_PHARE, CAPITAINE_NOIR,
                 MONSIEUR_LOYAL, CONDUCTEUR, ROUE_FORTUNE, ROI_FORAIN, EMIR, PHARAON, DJINN, DRAGON,
                 LOCOMOTIVE, GRAND_CHEF, DAME_EN_NOIR, DESPERADO,
                 CONCIERGE, IMPERATRICE_GIVRE, YETI, PRINCE_NEIGES,
                 MAITRE_CLOCHES, TISSEUSE, SEIGNEUR_DES, IDOLE_OR,
                 COMTE_MINUIT, BIBLIOTHECAIRE, FOSSOYEUR, MARIONNETTISTE,
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
    public boolean hasGoldSkin() { return has(EnemyTrait.GOLD_SKIN); }

    /** @return {@code true} si ses trois rouleaux identiques font un Jackpot (attaques et Boucliers x{@link EnemySymbol#JACKPOT_FACTOR}). */
    public boolean hitsJackpots() { return has(EnemyTrait.JACKPOTS); }

    /** @return {@code true} s'il joue une carte par bras, et perd un bras à chaque huitième de ses PV (le Kraken). */
    public boolean hasArms() { return has(EnemyTrait.ARMS); }

    /**
     * @return {@code true} s'il s'enferme dans son sarcophage en passant en phase 2 (le Pharaon maudit) :
     *         pendant le tour suivant du joueur, il ne prend aucun dégât, et il se soigne de
     *         {@link EnemySymbol#SARCOPHAGUS_HEAL_PERCENT} % de ses PV max
     */
    public boolean hasSarcophagus() { return has(EnemyTrait.SARCOPHAGUS); }

    /**
     * @return la force de ses soins (Potions et Crocs), en pourcentage de ceux
     *         du croupier : les ennemis aux PV énormes se soignent d'une plus
     *         petite part de leurs PV max
     */
    public int getHealScale() { return healScale; }

    /**
     * Soin le plus fort d'un ennemi, en % de ses PV max, à chaque soin (Potion, Croc, Morsure,
     * Coffre-fort, Battement, Sarcophage...), dans tous les modes (Astra, 2026-11-01).
     */
    public static final int MAX_HEAL_PERCENT = 5;

    /** @return {@code percent} ramené à {@link #MAX_HEAL_PERCENT} au plus (le soin qu'il rend vraiment). */
    public static float cappedHeal(float percent) { return Math.min(percent, MAX_HEAL_PERCENT); }

    /** @return les points de vie que rend une Potion renforcée de {@code bonusPercent} (Cœurs), en % de ses PV max. */
    public float potionPercent(int bonusPercent, Difficulty hard) {
        if (this == CHEF) return CHEF_POTION_PERCENT * (EnemySymbol.POTION_PERCENT + bonusPercent) / (float) EnemySymbol.POTION_PERCENT;
        Place place = Place.of(this);
        if (place != null && place.getHealPercent() > 0) { // soin fixé par son lieu, les Cœurs en proportion
            return place.getHealPercent() * (EnemySymbol.POTION_PERCENT + bonusPercent)
                / (float) EnemySymbol.POTION_PERCENT * getPower(hard) / 100f;
        }
        return (EnemySymbol.POTION_PERCENT + bonusPercent) * healScale * (float) getPower(hard) / 10_000f;
    }

    /** @return la part de ses PV max qu'un Croc lui rend pour chaque PV volé, en %. */
    public float drainPercent(Difficulty hard) { return EnemySymbol.FANG_DRAIN * healScale * (float) getPower(hard) / 10_000f; }

    /** @return {@code true} s'il joue ses cartes au hasard (la Roulette Vivante). */
    public boolean playsAtRandom() { return has(EnemyTrait.RANDOM_PLAYS); }

    /** Croupier en chef (nerf d'Astra) : sa Potion soigne 5 % de ses PV max (soins plafonnés à 5 %, 2026-11-01). */
    public static final int CHEF_POTION_PERCENT = 5;
    /** Croupier en chef : chance de base, à chaque tour, que son Rien ne va plus sorte (sur un seul rouleau). */
    public static final int CHEF_NOTHING_GOES_PERCENT = 5;
    /** Croupier en chef : chance de Rien ne va plus en plus pour chaque carte Annonce jouée ce tour. */
    public static final int CHEF_NOTHING_GOES_CARD_PERCENT = 2;

    /**
     * @return {@code true} si son Rien ne va plus ne tourne pas sur ses rouleaux, mais sort au plus une fois
     *         par tour avec {@link #CHEF_NOTHING_GOES_PERCENT} % de chance, plus
     *         {@link #CHEF_NOTHING_GOES_CARD_PERCENT} % par carte Annonce jouée ce tour (le Croupier en chef)
     */
    public boolean rollsNothingGoesApart() { return has(EnemyTrait.NOTHING_GOES_APART); }

    /** @return les poids de ses rouleaux dans la phase {@code phase}, sans le symbole qu'il tire à part. */
    public Map<EnemySymbol, Integer> getSpinWeights(int phase) {
        Map<EnemySymbol, Integer> weights = getWeights(phase);
        if (!rollsNothingGoesApart()) return weights;
        Map<EnemySymbol, Integer> spin = new LinkedHashMap<>(weights);
        spin.remove(EnemySymbol.NOTHING_GOES);
        return Collections.unmodifiableMap(spin);
    }

    /** @return {@code true} si ses Potions reforment aussi sa défense (le Croupier en chef). */
    public boolean potionShields() { return has(EnemyTrait.SHIELDING_POTIONS); }

    /** @return {@code true} si ses Trèfles comptent double quand sa vie est basse (la Reine des Tables). */
    public boolean royalBet() { return has(EnemyTrait.ROYAL_BET); }

    /**
     * @return {@code true} s'il change de symboles sous {@link #PHASE_TWO_RATIO} de vie : l'Éclat
     *         Originel, et les rois de l'Exploration (ils gagnent un symbole, voir {@link #kingPhaseSymbol()})
     */
    public boolean hasPhaseTwo() { return this == ECLAT || this == PROPHETE || kingPhaseSymbol() != null; }

    /**
     * Seuils de phase (choix d'Astra) : la part de ses PV max sous laquelle il ne descend
     * pas avant d'être passé à la phase suivante, à son tour. Le surplus d'un coup qui
     * franchit un seuil est perdu. Tous les boss et rois en ont : la moitié de leurs PV ;
     * le Prétendant et la Maison, les deux tiers puis le tiers ; la Machine Originelle,
     * la moitié puis {@link #MACHINE_LAST_RATIO}.
     *
     * @return ses seuils, de la phase 1 à la dernière (vide s'il n'a qu'une phase)
     */
    public float[] phaseThresholds() {
        if (!isBoss()) return new float[0];
        return switch (this) {
            case COLLECTIONNEUR, PRETENDANT, GENERAL, MAISON, ARCHITECTE -> new float[] {2f / 3f, 1f / 3f};
            case MACHINE_ORIGINELLE, GRAND_CROUPIER -> new float[] {PHASE_TWO_RATIO, MACHINE_LAST_RATIO};
            default -> new float[] {PHASE_TWO_RATIO};
        };
    }

    /** @return le nombre de ses phases (1 s'il n'en change pas). */
    public int phaseCount() { return phaseThresholds().length + 1; }

    /**
     * @return le symbole qu'un roi de l'Exploration ajoute à ses rouleaux en phase 2, sous
     *         {@link #PHASE_TWO_RATIO} de vie (poids {@link #KING_PHASE_WEIGHT}), ou {@code null}
     */
    public EnemySymbol kingPhaseSymbol() {
        return switch (this) {
            case ROI_PIQUE, SIRENE, BARON_OR -> EnemySymbol.FANG;
            case ROI_COEUR, TAVERNIER, GARDIEN_PHARE, MAITRE_FORGE, REQUIN_BANQUIER -> EnemySymbol.RAGE;
            case ROI_CARREAU, JACKPOT_VIVANT -> EnemySymbol.INTEREST;
            case ROI_TREFLE -> EnemySymbol.DRUNK;
            case CAPITAINE_RAT -> EnemySymbol.BOARDING;
            case CAPITAINE_NOIR, KRAKEN -> EnemySymbol.BLIND;
            case GRAND_FOREUR -> EnemySymbol.THORNS;
            case COEUR_MONTAGNE -> EnemySymbol.ANVIL;
            case MONSIEUR_LOYAL -> EnemySymbol.JUGGLE;
            case CONDUCTEUR -> EnemySymbol.SCARE;
            case ROUE_FORTUNE -> EnemySymbol.TURNSTILE;
            case ROI_FORAIN -> EnemySymbol.RIGGED_PRIZE;
            case EMIR -> EnemySymbol.TOLL;
            case PHARAON -> EnemySymbol.VENOM;
            case DJINN -> EnemySymbol.BILL;
            case DRAGON -> EnemySymbol.BURN;
            case LOCOMOTIVE -> EnemySymbol.BURN;
            case GRAND_CHEF -> EnemySymbol.FANG;
            case DAME_EN_NOIR -> EnemySymbol.VENOM;
            case DESPERADO -> EnemySymbol.BOARDING;
            case CONCIERGE -> EnemySymbol.CHAMPAGNE;
            case IMPERATRICE_GIVRE -> EnemySymbol.CHAMPAGNE;
            case YETI -> EnemySymbol.FREEZE;
            case PRINCE_NEIGES -> EnemySymbol.KEY;
            case MAITRE_CLOCHES -> EnemySymbol.DRUNK;
            case TISSEUSE -> EnemySymbol.PREDICTION;
            case SEIGNEUR_DES -> EnemySymbol.LOADED_DIE;
            case IDOLE_OR -> EnemySymbol.MIRROR;
            case COMTE_MINUIT -> EnemySymbol.FANG;
            case BIBLIOTHECAIRE -> EnemySymbol.BLIND;
            case FOSSOYEUR -> EnemySymbol.ROCKFALL;
            case MARIONNETTISTE -> EnemySymbol.SONG;
            default -> null;
        };
    }

    /** Poids du symbole qu'un roi ajoute en phase 2. */
    public static final int KING_PHASE_WEIGHT = 1;
    /** En phase 2, les attaques d'un roi valent ce % de leur valeur (il est plus fort)... */
    public static final int KING_PHASE_ATTACK_PERCENT = 125;
    /** ... et ses soins ce % de leur valeur : il ne remonte pas à sa vie pleine. */
    public static final int KING_PHASE_HEAL_PERCENT = 50;

    /**
     * @return sa phase quand il lui reste {@code hpRatio} de ses PV : l'Éclat
     *         Originel, la Comète Dorée et la Reine des Tables (sa Mise royale) passent à 2 sous la moitié ; le Prétendant perd un éclat et
     *         la Maison une de ses parties à chaque tiers ; la Machine
     *         Originelle vole un rouleau sous la moitié, puis un autre sous
     *         {@link #MACHINE_LAST_RATIO} ; les rois de l'Exploration passent à 2
     *         sous la moitié. Les autres restent en phase 1.
     */
    public int phaseFor(float hpRatio) {
        return switch (this) {
            case ECLAT, COMETE, REINE -> hpRatio < PHASE_TWO_RATIO ? 2 : 1;
            case COLLECTIONNEUR, PRETENDANT, GENERAL, MAISON, ARCHITECTE -> hpRatio * 3f <= 1f ? 3 : hpRatio * 3f <= 2f ? 2 : 1;
            case MACHINE_ORIGINELLE, GRAND_CROUPIER -> hpRatio < MACHINE_LAST_RATIO ? 3 : hpRatio < PHASE_TWO_RATIO ? 2 : 1;
            default -> kingPhaseSymbol() != null && hpRatio < PHASE_TWO_RATIO ? 2 : 1;
        };
    }

    /** @return le texte qui annonce sa phase {@code phase}, quand il y entre. */
    public String phaseText(int phase) {
        return switch (this) {
            case PRETENDANT -> phase == 2 ? Lang.t("ÉCLAT PERDU : FINI LE ROULEAU INTERDIT") : Lang.t("ÉCLAT PERDU : FINIE LA MISE ROYALE");
            case COLLECTIONNEUR -> phase == 2 ? Lang.t("NOUVEL ÉCLAT : VALSE !") : Lang.t("NOUVEL ÉCLAT : SCELLÉ !");
            case MAISON -> phase == 2 ? Lang.t("LA FAÇADE TOMBE : PLUS DE DÉFENSE") : Lang.t("LE COFFRE TOMBE : PLUS DE SOINS");
            case ARCHITECTE -> phase == 2 ? Lang.t("LES ÉCHAFAUDAGES MONTENT : ÉCHO !")
                : Lang.t("LA CLÉ DE VOÛTE : SA TOUR FRAPPE, VERTIGE !");
            case MACHINE_ORIGINELLE -> Lang.t("ELLE TE VOLE UN ROULEAU !");
            case GRAND_CROUPIER -> phase == 2 ? Lang.t("IL TRUQUE TA TABLE !") : Lang.t("CARTES SUR TABLE !");
            case COMETE -> Lang.t("LA COMÈTE S'EMBRASE !");
            case GENERAL -> phase == 2 ? Lang.t("SES LIEUTENANTS ARRIVENT : ESPIONNAGE ET CONTRAT !")
                : Lang.t("VA-TOUT : PLUS DE DÉFENSE, TOUT SUR L'ATTAQUE !");
            case PROPHETE -> Lang.t("LA COMÈTE RÉPOND À SON APPEL : COMÈTE !");
            case REINE -> Lang.t("MISE ROYALE : SES TRÈFLES COMPTENT DOUBLE !");
            case ECLAT -> Lang.t("L'ÉCLAT SE FEND : NOUVEAUX SYMBOLES !");
            case ROI_PIQUE -> Lang.t("LAMES ASSOIFFÉES : CROC !");
            case ROI_COEUR -> Lang.t("COEUR FURIEUX : RAGE !");
            case ROI_CARREAU -> Lang.t("LE TRÉSOR RAPPORTE : INTÉRÊTS !");
            case ROI_TREFLE -> Lang.t("LA TABLE TOURNE : IVRESSE !");
            case CAPITAINE_RAT -> Lang.t("LES RATS MONTENT À BORD : ABORDAGE !");
            case TAVERNIER -> Lang.t("BAGARRE DE TAVERNE : RAGE !");
            case GARDIEN_PHARE -> Lang.t("LA TEMPÊTE SE LÈVE : RAGE !");
            case CAPITAINE_NOIR -> Lang.t("BORDÉE DE FUMÉE : AVEUGLEMENT !");
            case BARON_OR -> Lang.t("AVIDITÉ : CROC !");
            case GRAND_FOREUR -> Lang.t("ÉCLATS DE ROCHE : ÉPINES !");
            case MAITRE_FORGE -> Lang.t("FORGE ARDENTE : RAGE !");
            case COEUR_MONTAGNE -> Lang.t("LE COEUR DURCIT : ENCLUME !");
            case SIRENE -> Lang.t("CHANT MORTEL : CROC !");
            case JACKPOT_VIVANT -> Lang.t("MISE MAXIMALE : INTÉRÊTS !");
            case REQUIN_BANQUIER -> Lang.t("FRÉNÉSIE : RAGE !");
            case KRAKEN -> Lang.t("NUAGE D'ENCRE : AVEUGLEMENT !");
            case MONSIEUR_LOYAL -> Lang.t("NUMÉRO FINAL : JONGLAGE !");
            case CONDUCTEUR -> Lang.t("TERMINUS : FRAYEUR !");
            case ROUE_FORTUNE -> Lang.t("LA ROUE S'EMBALLE : TOURNIQUET !");
            case ROI_FORAIN -> Lang.t("TOUT EST TRUQUÉ : LOT TRUQUÉ !");
            case EMIR -> Lang.t("TEMPÊTE DE SABLE : PÉAGE !");
            case PHARAON -> Lang.t("LE SARCOPHAGE SE FERME : VENIN !");
            case DJINN -> Lang.t("VOEU EXAUCÉ : LETTRE DE CHANGE !");
            case DRAGON -> Lang.t("ENVOL : BRÛLURE !");
            case LOCOMOTIVE -> Lang.t("VAPEUR BRÛLANTE : BRÛLURE !");
            case GRAND_CHEF -> Lang.t("COUP DE FEU EN CUISINE : CROC !");
            case DAME_EN_NOIR -> Lang.t("LA VOILETTE SE LÈVE : VENIN !");
            case DESPERADO -> Lang.t("TOUT LE MONDE À TERRE : ABORDAGE !");
            case CONCIERGE -> Lang.t("SERVICE D'ÉTAGE : CHAMPAGNE GLACÉ !");
            case IMPERATRICE_GIVRE -> Lang.t("LE BAL DE GLACE COMMENCE : CHAMPAGNE GLACÉ !");
            case YETI -> Lang.t("SOUFFLE POLAIRE : GEL !");
            case PRINCE_NEIGES -> Lang.t("LES PORTES DU BAL SE FERMENT : CLÉ !");
            case MAITRE_CLOCHES -> Lang.t("LE BOURDON TE SAOULE : IVRESSE !");
            case TISSEUSE -> Lang.t("ELLE LIT TON FIL : PRÉDICTION !");
            case SEIGNEUR_DES -> Lang.t("LES DÉS SONT PIPÉS : DÉ PIPÉ !");
            case IDOLE_OR -> Lang.t("SON OR TE RENVOIE TON IMAGE : REFLET !");
            case COMTE_MINUIT -> Lang.t("LES DOUZE COUPS SONNENT : CROC !");
            case BIBLIOTHECAIRE -> Lang.t("LES BOUGIES S'ÉTEIGNENT : AVEUGLEMENT !");
            case FOSSOYEUR -> Lang.t("LA TERRE TREMBLE : ÉBOULEMENT !");
            case MARIONNETTISTE -> Lang.t("LE RIDEAU SE LÈVE : CHANT !");
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
    public int stolenReels(int phase) { return has(EnemyTrait.REEL_THEFT) ? phase - 1 : 0; }

    /** @return {@code true} s'il garde une copie (un As sombre) de chaque carte achetée à l'échoppe (le Pilleur). */
    public boolean copiesPurchases() { return has(EnemyTrait.COPIES_PURCHASES); }

    /** @return {@code true} s'il interdit une famille de cartes tout le combat (le Portier). */
    public boolean bansFamily() { return has(EnemyTrait.BANS_FAMILY); }

    /** @return {@code true} s'il joue avec une copie du deck du joueur (l'Ombre du Joueur). */
    public boolean copiesPlayerDeck() { return has(EnemyTrait.COPIES_PLAYER_DECK); }

    /** @return les tours que dure le combat (le Temps Mort), 0 s'il n'y a pas de limite. */
    public int getTurnLimit() { return this == TEMPS_MORT ? TIME_LIMIT : 0; }

    /** @return {@code true} si une fois par combat, il annule le plus gros coup du joueur (la Maison). */
    public boolean houseWins() { return has(EnemyTrait.HOUSE_WINS); }

    /** @return {@code true} si, achevé, il résiste et tente un dernier tirage (la Machine Originelle). */
    public boolean hasLastDraw() { return has(EnemyTrait.LAST_DRAW); }

    /** @return les symboles de ses rouleaux et leur poids (dans l'ordre de tirage), en première phase. */
    public Map<EnemySymbol, Integer> getWeights() { return weights; }

    /** @return les symboles de ses rouleaux et leur poids dans la phase {@code phase} (1 ou 2). */
    public Map<EnemySymbol, Integer> getWeights(int phase) {
        if (phase >= 2 && this == ECLAT) return ECLAT_PHASE_TWO;
        if (phase >= 2 && this == PROPHETE) return PROPHET_PHASE_TWO;
        if (phase >= 2 && kingPhaseSymbol() != null) {
            Map<EnemySymbol, Integer> king = new LinkedHashMap<>(weights);
            king.merge(kingPhaseSymbol(), KING_PHASE_WEIGHT, Integer::sum);
            return Collections.unmodifiableMap(king);
        }
        if (this == ARCHITECTE && phase >= 3) return ARCHITECT_PHASE_THREE;
        if (this == ARCHITECTE && phase == 2) return ARCHITECT_PHASE_TWO;
        if (this == COLLECTIONNEUR && phase >= 3) return COLLECTOR_PHASE_THREE;
        if (this == COLLECTIONNEUR && phase == 2) return COLLECTOR_PHASE_TWO;
        if (this == GENERAL && phase >= 3) return GENERAL_PHASE_THREE;
        if (this == GENERAL && phase == 2) return GENERAL_PHASE_TWO;
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

    /**
     * @return un deck neuf : les cartes de sa composition, sans les couleurs qui
     *         ne servent pas ses rouleaux (voir {@link #removedSuits()}), et
     *         {@link EnemySignature#COPIES} exemplaires de chacune de ses cartes propres
     */
    public List<Card> createDeck() {
        List<Card> cards = new ArrayList<>();
        Set<Card.Suit> removed = removedSuits();
        for (Map.Entry<Card.Suit, int[]> entry : deck.entrySet()) {
            if (removed.contains(entry.getKey())) continue;
            for (int rank : entry.getValue()) cards.add(EnemyCards.card(entry.getKey(), rank));
        }
        for (EnemySignature signature : getSignatures()) {
            for (int i = 0; i < EnemySignature.COPIES; i++) cards.add(signature.card());
        }
        if (forbidsReels()) {
            for (int i = 0; i < FORBIDDEN_REELS; i++) cards.add(EnemyCards.forbiddenReel());
        }
        return cards;
    }

    /**
     * @return {@code true} s'il garde le deck de base, toutes couleurs, sans carte
     *         propre : les premiers ennemis, qui apprennent les couleurs au joueur
     *         (le croupier de l'Entraînement, le Croupier démoniaque et les soldats de la prairie)
     */
    public boolean keepsBaseDeck() {
        return switch (this) {
            case ENTRAINEMENT, CROUPIER, SOLDAT_PIQUE, SOLDAT_COEUR, SOLDAT_CARREAU, SOLDAT_TREFLE -> true;
            default -> false;
        };
    }

    /**
     * @return les couleurs retirées de son deck, car elles ne font rien chez lui :
     *         le Cœur sans Potion, Croc ni Morsure (ce qu'il renforce), le Carreau
     *         sans Bouclier. Aucune pour un ennemi qui {@linkplain #keepsBaseDeck() garde son deck de base}.
     */
    public Set<Card.Suit> removedSuits() {
        Set<Card.Suit> removed = EnumSet.noneOf(Card.Suit.class);
        if (keepsBaseDeck()) return removed;
        if (!weights.containsKey(EnemySymbol.POTION) && !weights.containsKey(EnemySymbol.FANG)
            && !weights.containsKey(EnemySymbol.BANK_BITE)) removed.add(Card.Suit.COEUR);
        if (!weights.containsKey(EnemySymbol.SHIELD)) removed.add(Card.Suit.CARREAU);
        return removed;
    }

    /** @return ses cartes propres, liées à ses rouleaux (voir le document « Cartes des ennemis »). */
    public List<EnemySignature> getSignatures() {
        return switch (this) {
            case GARDIEN, ROI_CARREAU -> List.of(EnemySignature.RONCES);
            case SANGSUE, ROI_COEUR -> List.of(EnemySignature.SAIGNEE);
            case BRETTEUR, ROI_PIQUE, JACKPOT_VIVANT -> List.of(EnemySignature.PROVOCATION);
            case COMETE -> List.of(EnemySignature.QUEUE_DE_COMETE);
            case TRICHEUR -> List.of(EnemySignature.DE_CHARGE);
            case USURIER -> List.of(EnemySignature.TAUX_USURAIRE);
            case ROULETTE, PIEUVRE -> List.of(EnemySignature.BILLE_TRUQUEE);
            case REINE -> List.of(EnemySignature.MAIN_DE_LA_REINE);
            case PASSEUR -> List.of(EnemySignature.COUP_DE_RATEAU, EnemySignature.DE_CHARGE);
            case PELERIN_FER -> List.of(EnemySignature.SERMENT_SOLENNEL);
            case DISEUSE -> List.of(EnemySignature.BOULE_DE_CRISTAL);
            case PORTEUR -> List.of(EnemySignature.AUTEL);
            case PROPHETE -> List.of(EnemySignature.PROCESSION, EnemySignature.QUEUE_DE_COMETE);
            case GARDIENNE -> List.of(EnemySignature.RONCES, EnemySignature.SAIGNEE, EnemySignature.PROVOCATION);
            case MIROIR -> List.of(EnemySignature.MIROIR_DEFORMANT);
            case HORLOGER -> List.of(EnemySignature.REMONTOIR);
            case FOU -> List.of(EnemySignature.TOUT_SUR_LE_ROUGE);
            case PILLEUR -> List.of(EnemySignature.SAC_DE_BUTIN);
            case FAUSSAIRE -> List.of(EnemySignature.PLANCHE_A_BILLETS);
            case CARTOMANCIENNE -> List.of(EnemySignature.TAROT);
            case DUELLISTE -> List.of(EnemySignature.BOTTE_SECRETE);
            case PRETENDANT -> List.of(EnemySignature.ECLAT_VOLE);
            case SERGENT -> List.of(EnemySignature.CLAIRON);
            case CANONNIER -> List.of(EnemySignature.POUDRE_NOIRE);
            case ESPIONNE -> List.of(EnemySignature.MICROFILM);
            case MERCENAIRE -> List.of(EnemySignature.PRIME_DE_RISQUE);
            case GENERAL -> List.of(EnemySignature.ORDRE_DE_CHARGE, EnemySignature.CLAIRON);
            case RAMASSEUR -> List.of(EnemySignature.POCHE_PLEINE);
            case HERITIER -> List.of(EnemySignature.TESTAMENT);
            case VEUVE -> List.of(EnemySignature.DERNIERE_DANSE);
            case NOTAIRE -> List.of(EnemySignature.PARAPHE);
            case COLLECTIONNEUR -> List.of(EnemySignature.POCHE_PLEINE, EnemySignature.TESTAMENT);
            case COMPTABLE -> List.of(EnemySignature.REDRESSEMENT);
            case DIRECTEUR -> List.of(EnemySignature.REGLEMENT_INTERIEUR);
            case SECURITE -> List.of(EnemySignature.CAMERAS);
            case GARDIEN_LEVIER -> List.of(EnemySignature.PLANCHE_A_BILLETS, EnemySignature.REDRESSEMENT,
                EnemySignature.BOTTE_SECRETE);
            case BANQUEROUTE -> List.of(EnemySignature.SAISIE);
            case TEMPS_MORT -> List.of(EnemySignature.COMPTE_A_REBOURS);
            case CHEF -> List.of(EnemySignature.ANNONCE, EnemySignature.COUP_DE_RATEAU);
            case PORTIER -> List.of(EnemySignature.TENUE_EXIGEE, EnemySignature.PAS_CE_SOIR);
            case MAISON -> List.of(EnemySignature.MACONNERIE, EnemySignature.DOUBLE_SERRURE, EnemySignature.BANQUE_GAGNE);
            case LIFTIER_TOUR -> List.of(EnemySignature.BOUTON_D_APPEL);
            case ECHO -> List.of(EnemySignature.RESONANCE);
            case VERTIGE -> List.of(EnemySignature.LE_VIDE);
            case GARGOUILLE -> List.of(EnemySignature.GRANIT);
            case ARCHITECTE -> List.of(EnemySignature.FONDATIONS, EnemySignature.CLE_DE_VOUTE);
            case MACHINE_ORIGINELLE -> List.of(EnemySignature.HUILE_NOIRE, EnemySignature.PLUIE_D_ETOILES,
                EnemySignature.SURTENSION, EnemySignature.GRANDE_ASPIRATION);
            case GOLEM_OR -> List.of(EnemySignature.OR_MASSIF, EnemySignature.POIDS_DE_L_OR);
            case COEUR_MONTAGNE -> List.of(EnemySignature.GRONDEMENT, EnemySignature.FILON);
            case BANDIT_MANCHOT -> List.of(EnemySignature.MONNAIE, EnemySignature.LEVIER_GRILLE);
            case ROI_TREFLE -> List.of(EnemySignature.TAUX_USURAIRE, EnemySignature.DE_CHARGE);
            case RAT_CALES -> List.of(EnemySignature.DENTS_DE_RAT);
            case CAPITAINE_RAT -> List.of(EnemySignature.DENTS_DE_RAT, EnemySignature.SAIGNEE);
            case BUVEUR, TAVERNIER -> List.of(EnemySignature.TOURNEE);
            case GUETTEUR -> List.of(EnemySignature.LANTERNE);
            case GARDIEN_PHARE -> List.of(EnemySignature.LANTERNE, EnemySignature.RONCES);
            case PIRATE -> List.of(EnemySignature.GRAPPIN);
            case CAPITAINE_NOIR -> List.of(EnemySignature.GRAPPIN, EnemySignature.PROVOCATION);
            case CHERCHEUR_OR -> List.of(EnemySignature.TAMIS_NOIR);
            case BARON_OR -> List.of(EnemySignature.TAMIS_NOIR, EnemySignature.TAUX_USURAIRE);
            case FOREUR -> List.of(EnemySignature.MECHE_DE_DIAMANT);
            case GRAND_FOREUR -> List.of(EnemySignature.MECHE_DE_DIAMANT, EnemySignature.PROVOCATION);
            case FORGERON, MAITRE_FORGE -> List.of(EnemySignature.TREMPE);
            case BARMAN_NOYE, SIRENE -> List.of(EnemySignature.REFRAIN);
            case REQUIN -> List.of(EnemySignature.FRENESIE);
            case REQUIN_BANQUIER -> List.of(EnemySignature.FRENESIE, EnemySignature.SAIGNEE);
            case KRAKEN -> List.of(EnemySignature.ENCRE);
            case CLOWN -> List.of(EnemySignature.MASSUES);
            case MONSIEUR_LOYAL -> List.of(EnemySignature.ROULEMENT_DE_TAMBOUR, EnemySignature.PROVOCATION);
            case SQUELETTE -> List.of(EnemySignature.BOUH);
            case CONDUCTEUR -> List.of(EnemySignature.TUNNEL_SANS_FIN, EnemySignature.SAIGNEE);
            case FORAIN -> List.of(EnemySignature.TOUR_DE_MANIVELLE);
            case ROUE_FORTUNE -> List.of(EnemySignature.ROUE_LESTEE, EnemySignature.BILLE_TRUQUEE);
            case BONIMENTEUR -> List.of(EnemySignature.LOT_DE_CONSOLATION, EnemySignature.TAUX_USURAIRE);
            case ROI_FORAIN -> List.of(EnemySignature.CARTE_BISEAUTEE, EnemySignature.RONCES);
            case CARAVANIER -> List.of(EnemySignature.OCTROI, EnemySignature.TAUX_USURAIRE);
            case EMIR -> List.of(EnemySignature.USURE, EnemySignature.SAIGNEE);
            case MOMIE -> List.of(EnemySignature.MOMIFICATION);
            case PHARAON -> List.of(EnemySignature.MOMIFICATION, EnemySignature.RONCES);
            case SCORPION -> List.of(EnemySignature.DARD);
            case DJINN -> List.of(EnemySignature.FATA_MORGANA, EnemySignature.MIROIR_DEFORMANT);
            case SALAMANDRE -> List.of(EnemySignature.COULEE_DE_LAVE);
            case DRAGON -> List.of(EnemySignature.BRAISE, EnemySignature.TREMPE);
            case CHAUFFEUR -> List.of(EnemySignature.CHARBON);
            case LOCOMOTIVE -> List.of(EnemySignature.CHARBON, EnemySignature.PLEIN_REGIME);
            case SERVEUR -> List.of(EnemySignature.DOIGTS_AGILES);
            case GRAND_CHEF -> List.of(EnemySignature.DESSERT, EnemySignature.DOIGTS_AGILES);
            case SOMNAMBULE -> List.of(EnemySignature.MARCHAND_DE_SABLE);
            case DAME_EN_NOIR -> List.of(EnemySignature.MARCHAND_DE_SABLE, EnemySignature.DARD);
            case CONVOYEUR -> List.of(EnemySignature.HAUT_LES_MAINS);
            case DESPERADO -> List.of(EnemySignature.HAUT_LES_MAINS, EnemySignature.TOUT_SUR_LE_ROUGE);
            case GROOM -> List.of(EnemySignature.TROUSSEAU);
            case CONCIERGE -> List.of(EnemySignature.TROUSSEAU, EnemySignature.PROVOCATION);
            case PATINEUSE -> List.of(EnemySignature.GIVRE_ETERNEL);
            case IMPERATRICE_GIVRE -> List.of(EnemySignature.GIVRE_ETERNEL, EnemySignature.TEMPETE);
            case LIFTIER -> List.of(EnemySignature.BOULE_DE_NEIGE);
            case YETI -> List.of(EnemySignature.BOULE_DE_NEIGE, EnemySignature.TEMPETE);
            case MAJORDOME -> List.of(EnemySignature.GRAND_CRU);
            case PRINCE_NEIGES -> List.of(EnemySignature.GRAND_CRU, EnemySignature.GIVRE_ETERNEL);
            case MOINE_HASARD -> List.of(EnemySignature.CARILLON);
            case MAITRE_CLOCHES -> List.of(EnemySignature.CARILLON, EnemySignature.TOCSIN);
            case ORACLE_AVEUGLE -> List.of(EnemySignature.QUENOUILLE);
            case TISSEUSE -> List.of(EnemySignature.QUENOUILLE, EnemySignature.CISEAUX_DU_DESTIN);
            case LANCEUR_DES -> List.of(EnemySignature.COUP_DE_DES);
            case SEIGNEUR_DES -> List.of(EnemySignature.COUP_DE_DES, EnemySignature.QUITTE_OU_DOUBLE);
            case STATUE_ANIMEE -> List.of(EnemySignature.OFFRANDE);
            case IDOLE_OR -> List.of(EnemySignature.OFFRANDE, EnemySignature.JUGEMENT);
            case VALET_FANTOME -> List.of(EnemySignature.PACTE_SCELLE);
            case COMTE_MINUIT -> List.of(EnemySignature.PACTE_SCELLE, EnemySignature.DOUZE_COUPS);
            case LIVRE_VIVANT -> List.of(EnemySignature.PAGE_ARRACHEE);
            case BIBLIOTHECAIRE -> List.of(EnemySignature.PAGE_ARRACHEE, EnemySignature.SILENCE);
            case GOULE -> List.of(EnemySignature.TERRE_FRAICHE);
            case FOSSOYEUR -> List.of(EnemySignature.TERRE_FRAICHE, EnemySignature.GLAS);
            case POUPEE_HANTEE -> List.of(EnemySignature.FIL_DE_SOIE);
            case MARIONNETTISTE -> List.of(EnemySignature.FIL_DE_SOIE, EnemySignature.DERNIER_ACTE);
            // Le Joueur Masqué : à chaque rencontre, il garde une carte du lieu traversé.
            case RIVAL_FOIRE -> List.of(EnemySignature.CARTE_BISEAUTEE);
            case RIVAL_RAPIDE -> List.of(EnemySignature.CARTE_BISEAUTEE, EnemySignature.HAUT_LES_MAINS);
            case RIVAL_PORT -> List.of(EnemySignature.CARTE_BISEAUTEE, EnemySignature.HAUT_LES_MAINS, EnemySignature.GRAPPIN);
            case RIVAL_PALACE -> List.of(EnemySignature.CARTE_BISEAUTEE, EnemySignature.HAUT_LES_MAINS, EnemySignature.GRAPPIN,
                EnemySignature.TROUSSEAU);
            case RIVAL_MINES -> List.of(EnemySignature.CARTE_BISEAUTEE, EnemySignature.HAUT_LES_MAINS, EnemySignature.GRAPPIN, EnemySignature.TROUSSEAU, EnemySignature.MECHE_DE_DIAMANT);
            case RIVAL_MANOIR -> List.of(EnemySignature.CARTE_BISEAUTEE, EnemySignature.HAUT_LES_MAINS, EnemySignature.GRAPPIN, EnemySignature.TROUSSEAU, EnemySignature.MECHE_DE_DIAMANT,
                EnemySignature.PACTE_SCELLE);
            case RIVAL_CASINO -> List.of(EnemySignature.CARTE_BISEAUTEE, EnemySignature.HAUT_LES_MAINS, EnemySignature.GRAPPIN, EnemySignature.TROUSSEAU, EnemySignature.MECHE_DE_DIAMANT,
                EnemySignature.PACTE_SCELLE, EnemySignature.FRENESIE);
            case RIVAL_TEMPLE -> List.of(EnemySignature.CARTE_BISEAUTEE, EnemySignature.HAUT_LES_MAINS, EnemySignature.GRAPPIN, EnemySignature.TROUSSEAU, EnemySignature.MECHE_DE_DIAMANT,
                EnemySignature.PACTE_SCELLE, EnemySignature.FRENESIE, EnemySignature.COUP_DE_DES);
            case RIVAL_DESERT -> List.of(EnemySignature.CARTE_BISEAUTEE, EnemySignature.HAUT_LES_MAINS, EnemySignature.GRAPPIN, EnemySignature.TROUSSEAU, EnemySignature.MECHE_DE_DIAMANT,
                EnemySignature.PACTE_SCELLE, EnemySignature.FRENESIE, EnemySignature.COUP_DE_DES, EnemySignature.USURE);
            default -> List.of();
        };
    }

    /** @return la phrase qui résume ses rouleaux, pour son infobulle (ex : "Épée, Bouclier x2, Épines x2"). */
    public String describeReels() {
        return describeReels(1);
    }

    /** @return comme {@link #describeReels()}, pour la phase {@code phase}. */
    public String describeReels(int phase) {
        Map<EnemySymbol, Integer> weights = getSpinWeights(phase);
        List<String> parts = new ArrayList<>();
        int total = weights.values().stream().mapToInt(Integer::intValue).sum();
        for (Map.Entry<EnemySymbol, Integer> entry : weights.entrySet()) {
            parts.add(Lang.f("{0} {1} %", entry.getKey().getDisplayName(), Math.round(100f * entry.getValue() / total)));
        }
        if (rollsNothingGoesApart()) {
            parts.add(Lang.f("{0} {1} % par tour (+{2} % par Annonce)", EnemySymbol.NOTHING_GOES.getDisplayName(),
                CHEF_NOTHING_GOES_PERCENT, CHEF_NOTHING_GOES_CARD_PERCENT));
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
