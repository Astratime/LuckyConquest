package fr.astratime.lucky.progress;

import fr.astratime.lucky.i18n.Lang;

/**
 * Les succès : un objectif, et une petite récompense en pièces versée une
 * seule fois, quand il est atteint (voir {@link PlayerProfile#checkAchievements()}).
 */
public enum Achievement {
    // Combats
    FIRST_WIN("Première victoire", "Gagner un combat.", 10_000),
    VETERAN("Vétéran", "Gagner 50 combats.", 250_000),
    FIRST_BINGO("Premier Bingo", "Réussir un Bingo.", 10_000),
    HUNDRED_BINGOS("Machine à Bingos", "Réussir 100 Bingos.", 500_000),
    BINGO_COLLECTOR("Collectionneur", "Réussir le Bingo de 10 symboles différents.", 250_000),
    BONUS_GAME("Partie bonus", "Jouer un Jeu bonus.", 25_000),
    SQUARE("Carré d'as", "Réussir un Carré.", 50_000),
    MILLION_HIT("Coup d'un million", "Infliger 1 million de dégâts en un coup.", 100_000),
    BILLION_HIT("Coup d'un milliard", "Infliger 1 milliard de dégâts en un coup.", 1_000_000),
    HIGH_ROLLER("Gros joueur", "Gagner 10 millions de gains en combat, au total.", 250_000),
    // Exploration
    FIRST_DUNGEON("Premier donjon", "Vider un donjon.", 10_000),
    PRAIRIE("Maître de la prairie", "Vider les 4 donjons de la prairie.", 50_000),
    PORT("Loup de mer", "Vider les 4 donjons du Port des Contrebandiers.", 250_000),
    MINES("Chercheur d'or", "Vider les 4 donjons des Mines d'Or.", 500_000),
    CASINO("Maître du Casino", "Vider les 4 donjons du Casino Englouti.", 1_000_000),
    // Tour des épreuves
    COMET("Comète abattue", "Finir le chapitre 1 de la Tour des épreuves.", 25_000),
    ORIGIN("Fin de la Machine", "Finir le chapitre 6 de la Tour des épreuves.", 1_000_000),
    HARD_CHAPTER("Sans pitié", "Finir un chapitre de la Tour en mode difficile.", 500_000),
    // Progression
    FIRST_RANK("Premier rang", "Acheter un rang à la boutique.", 50_000),
    UPGRADED_CARD("Carte en or", "Fusionner une carte « + » à la Table du croupier.", 500_000);

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
