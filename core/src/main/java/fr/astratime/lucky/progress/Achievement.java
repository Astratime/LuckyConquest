package fr.astratime.lucky.progress;

import fr.astratime.lucky.i18n.Lang;

/**
 * Les succès : un objectif, et une petite récompense en pièces versée une
 * seule fois, quand il est atteint (voir {@link PlayerProfile#checkAchievements()}).
 *
 * Récompense (recalée le 2026-11-13) : environ une descente du rang où le succès s'obtient d'habitude
 * (Prairie 50 k à 250 k, Fête foraine 1 à 3 M, Port 20 à 50 M, Mines 200 à 300 M, Casino 0,5 à 1 Md, Désert 5 à 10 Md).
 */
public enum Achievement {
    // Combats
    FIRST_WIN("Première victoire", "Gagner un combat.", 50_000),
    VETERAN("Vétéran", "Gagner 50 combats.", 2_000_000),
    FIRST_BINGO("Premier Bingo", "Réussir un Bingo.", 50_000),
    HUNDRED_BINGOS("Machine à Bingos", "Réussir 100 Bingos.", 20_000_000),
    BINGO_COLLECTOR("Collectionneur", "Réussir le Bingo de 10 symboles différents.", 2_000_000),
    BONUS_GAME("Partie bonus", "Jouer un Jeu bonus.", 100_000),
    SQUARE("Carré d'as", "Réussir un Carré.", 100_000),
    MILLION_HIT("Coup d'un million", "Infliger 1 million de dégâts en un coup.", 1_000_000),
    BILLION_HIT("Coup d'un milliard", "Infliger 1 milliard de dégâts en un coup.", 200_000_000),
    HIGH_ROLLER("Gros joueur", "Gagner 10 millions de gains en combat, au total.", 2_000_000),
    CHAMPION("Champion", "Gagner 250 combats.", 50_000_000),
    CLOSE_CALL("Sur le fil", "Gagner un combat avec moins de 10 % de ses PV.", 100_000),
    FLAWLESS("Intouchable", "Finir un combat gagné avec tous ses PV.", 100_000),
    THOUSAND_SPINS("Mille tirages", "Lancer 1 000 tirages.", 2_000_000),
    BINGO_KING("Roi du Bingo", "Réussir 500 Bingos.", 1_000_000_000L),
    GREAT_COLLECTOR("Grand collectionneur", "Réussir le Bingo de 25 symboles différents.", 1_000_000_000L),
    BONUS_REGULAR("Habitué du bonus", "Jouer 25 Jeux bonus.", 20_000_000),
    SQUARE_MASTER("Maître des Carrés", "Réussir 25 Carrés.", 20_000_000),
    TEN_BILLION_HIT("Coup de dix milliards", "Infliger 10 milliards de dégâts en un coup.", 1_000_000_000L),
    BILLIONAIRE("Milliardaire", "Gagner 1 milliard de gains en combat, au total.", 200_000_000),
    // Exploration
    FIRST_DUNGEON("Premier donjon", "Vider un donjon.", 100_000),
    PRAIRIE("Maître de la prairie", "Vider les 4 donjons de la prairie.", 250_000),
    FOIRE("Roi de la fête", "Vider les 4 donjons de la Fête foraine.", 3_000_000),
    PORT("Loup de mer", "Vider les 4 donjons du Port des Contrebandiers.", 50_000_000),
    MINES("Chercheur d'or", "Vider les 4 donjons des Mines d'Or.", 300_000_000),
    CASINO("Maître du Casino", "Vider les 4 donjons du Casino Englouti.", 1_000_000_000L),
    DESERT("Tueur de dragon", "Vider les 4 donjons du Désert de Braise.", 5_000_000_000L),
    GREAT_EXPLORER("Grand explorateur", "Vider tous les donjons de l'Exploration.", 5_000_000_000L),
    FORTUNE("Fortune faite", "Gagner 1 milliard de pièces en Exploration, au total.", 300_000_000),
    // Tour des épreuves
    COMET("Comète abattue", "Finir le chapitre 1 de la Tour des épreuves.", 250_000),
    HALFWAY("Mi-chemin", "Finir le chapitre 3 de la Tour des épreuves.", 30_000_000),
    ORIGIN("Fin de la Machine", "Finir le chapitre 6 de la Tour des épreuves.", 1_000_000_000L),
    HARD_CHAPTER("Sans pitié", "Finir un chapitre de la Tour en mode difficile.", 200_000_000),
    HARD_TOWER("Tour domptée", "Finir les 6 chapitres de la Tour en mode difficile.", 10_000_000_000L),
    ENDLESS_5("Toujours plus haut", "Gagner l'étage 5 du mode sans fin.", 500_000_000),
    ENDLESS_10("Vertige", "Gagner l'étage 10 du mode sans fin.", 5_000_000_000L),
    // Progression
    FIRST_RANK("Premier rang", "Acheter un rang à la boutique.", 1_000_000),
    UPGRADED_CARD("Carte en or", "Fusionner une carte « + » à la Table du croupier.", 200_000_000),
    GOLDSMITH("Orfèvre", "Avoir 5 cartes « + » différentes.", 1_000_000_000L),
    REEL_COLLECTOR("Mécanicien", "Posséder 10 rouleaux achetés ou gagnés.", 200_000_000),
    LEGEND("Légende vivante", "Acheter le rang Légende du Jackpot.", 10_000_000_000L),
    // Bestiaire
    HUNTER("Chasseur", "Vaincre 50 ennemis différents.", 200_000_000),
    FULL_BESTIARY("Bestiaire complet", "Vaincre tous les ennemis du bestiaire.", 10_000_000_000L);

    private final String name;
    private final String description;
    private final long   reward;

    Achievement(String name, String description, long reward) {
        this.name        = name;
        this.description = description;
        this.reward      = reward;
    }

    /** @return le nom du succès, dans la langue choisie. */
    public String getName() { return Lang.t(name); }

    /** @return l'objectif du succès, dans la langue choisie. */
    public String getDescription() { return Lang.t(description); }

    /** @return les pièces versées quand le succès est atteint. */
    public long getReward() { return reward; }
}
