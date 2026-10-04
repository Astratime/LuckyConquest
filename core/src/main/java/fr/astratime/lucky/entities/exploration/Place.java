package fr.astratime.lucky.entities.exploration;

import java.util.List;

/**
 * Les lieux à explorer. Chacun est une carte (map) où l'on choisit un donjon
 * (voir {@link fr.astratime.lucky.screens.ExplorationScreen}), et a sa règle
 * ({@link PlaceRule}). Un lieu s'ouvre quand tous les donjons du précédent
 * sont vidés (leurs chefs battus).
 */
public enum Place {

    PRAIRIE("La prairie", "La prairie",
        "L'herbe est haute. Le vent est doux. Quatre donjons dorment sous les collines. "
            + "Chacun garde un coffre au trésor.",
        PlaceRule.NONE, List.of(Dungeon.PIQUE, Dungeon.TREFLE, Dungeon.COEUR, Dungeon.CARREAU)),

    PORT("Le Port des Contrebandiers", "Le Port",
        "Les bateaux n'apportent plus d'épices. Ils apportent des cartes truquées. Sur les quais, tout s'achète. "
            + "Même la chance.",
        PlaceRule.SCORBUT, List.of(Dungeon.CALE, Dungeon.TAVERNE, Dungeon.PHARE, Dungeon.GALION)),

    MINES("Les Mines d'Or", "Les Mines",
        "Sous la montagne, l'or de la comète coule encore. Les mineurs creusent sans fin. Ils ont oublié la lumière. "
            + "Pas leur cupidité.",
        PlaceRule.GRISOU, List.of(Dungeon.FILON, Dungeon.PUITS, Dungeon.FORGE, Dungeon.GOUFFRE)),

    CASINO("Le Casino Englouti", "Le Casino",
        "Le plus grand casino du monde a sombré une nuit de jackpot. Sous l'eau, les machines tournent toujours. "
            + "Les joueurs aussi.",
        PlaceRule.MAREE, List.of(Dungeon.BAR, Dungeon.MACHINES, Dungeon.COFFRES, Dungeon.VIP));

    private final String        name;
    private final String        shortName;
    private final String        description;
    private final PlaceRule     rule;
    private final List<Dungeon> dungeons;

    Place(String name, String shortName, String description, PlaceRule rule, List<Dungeon> dungeons) {
        this.name        = name;
        this.shortName   = shortName;
        this.description = description;
        this.rule        = rule;
        this.dungeons    = dungeons;
    }

    /** @return la règle du lieu, qui joue dans tous ses combats. */
    public PlaceRule getRule() { return rule; }

    /** @return le lieu à vider avant d'ouvrir celui-ci (tous ses donjons), ou {@code null} pour la prairie. */
    public Place getPrevious() { return ordinal() == 0 ? null : values()[ordinal() - 1]; }

    /** @return le lieu de {@code dungeon}. */
    public static Place of(Dungeon dungeon) {
        for (Place place : values()) {
            if (place.dungeons.contains(dungeon)) return place;
        }
        throw new IllegalArgumentException("Donjon sans lieu : " + dungeon);
    }

    /** @return le nom du lieu (ex : "La prairie"). */
    public String getName() { return name; }
    /** @return son nom court, dans la liste des lieux (ex : "Le Port"). */
    public String getShortName() { return shortName; }
    /** @return sa présentation, en quelques phrases courtes. */
    public String getDescription() { return description; }
    /** @return ses donjons, dans l'ordre de leurs icônes sur la carte. */
    public List<Dungeon> getDungeons() { return dungeons; }
}
