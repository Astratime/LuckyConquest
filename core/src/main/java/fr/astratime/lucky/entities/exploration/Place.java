package fr.astratime.lucky.entities.exploration;

import java.util.List;

/**
 * Les lieux à explorer. Chacun est une carte (map) où l'on choisit un donjon
 * (voir {@link fr.astratime.lucky.screens.ExplorationScreen}).
 */
public enum Place {

    PRAIRIE("La prairie",
        "L'herbe est haute. Le vent est doux. Quatre donjons dorment sous les collines. "
            + "Chacun garde un coffre au trésor.",
        List.of(Dungeon.PIQUE, Dungeon.TREFLE, Dungeon.COEUR, Dungeon.CARREAU));

    private final String        name;
    private final String        description;
    private final List<Dungeon> dungeons;

    Place(String name, String description, List<Dungeon> dungeons) {
        this.name        = name;
        this.description = description;
        this.dungeons    = dungeons;
    }

    /** @return le nom du lieu (ex : "La prairie"). */
    public String getName() { return name; }
    /** @return sa présentation, en quelques phrases courtes. */
    public String getDescription() { return description; }
    /** @return ses donjons, dans l'ordre de leurs icônes sur la carte. */
    public List<Dungeon> getDungeons() { return dungeons; }
}
