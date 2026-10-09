package fr.astratime.lucky.entities.exploration;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.i18n.Lang;

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
        PlaceRule.NONE, 0, 0, 0, 100, List.of(Dungeon.PIQUE, Dungeon.TREFLE, Dungeon.COEUR, Dungeon.CARREAU)),

    PORT("Le Port des Contrebandiers", "Le Port",
        "Les bateaux n'apportent plus d'épices. Ils apportent des cartes truquées. Sur les quais, tout s'achète. "
            + "Même la chance.",
        PlaceRule.SCORBUT, 50, 1_000, 10, 500, List.of(Dungeon.CALE, Dungeon.TAVERNE, Dungeon.PHARE, Dungeon.GALION)),

    MINES("Les Mines d'Or", "Les Mines",
        "Sous la montagne, l'or de la comète coule encore. Les mineurs creusent sans fin. Ils ont oublié la lumière. "
            + "Pas leur cupidité.",
        PlaceRule.GRISOU, 150, 10_000, 20, 2_000, List.of(Dungeon.FILON, Dungeon.PUITS, Dungeon.FORGE, Dungeon.GOUFFRE)),

    CASINO("Le Casino Englouti", "Le Casino",
        "Le plus grand casino du monde a sombré une nuit de jackpot. Sous l'eau, les machines tournent toujours. "
            + "Les joueurs aussi.",
        PlaceRule.MAREE, 400, 50_000, 30, 5_000, List.of(Dungeon.BAR, Dungeon.MACHINES, Dungeon.COFFRES, Dungeon.VIP));

    private final String        name;
    private final String        shortName;
    private final String        description;
    private final PlaceRule     rule;
    private final int           swordDamage;
    private final int           shieldDefense;
    private final int           healPercent;
    private final int           spinCost;
    private final List<Dungeon> dungeons;

    /**
     * @param swordDamage   dégâts de base d'une Épée de ses ennemis (0 : ceux de base)
     * @param shieldDefense défense de base d'un Bouclier de ses ennemis (0 : celle de base)
     * @param healPercent   soin de base d'une Potion de ses ennemis, en % de leurs PV max (0 : celui de base)
     * @param spinCost      coût d'un tirage dans ses combats, en gains
     */
    Place(String name, String shortName, String description, PlaceRule rule,
          int swordDamage, int shieldDefense, int healPercent, int spinCost, List<Dungeon> dungeons) {
        this.name          = name;
        this.shortName     = shortName;
        this.description   = description;
        this.rule          = rule;
        this.swordDamage   = swordDamage;
        this.shieldDefense = shieldDefense;
        this.healPercent   = healPercent;
        this.spinCost      = spinCost;
        this.dungeons      = dungeons;
    }

    /** @return les dégâts de base d'une Épée de ses ennemis, soldats et rois, ou 0 s'ils gardent ceux de base. */
    public int getSwordDamage() { return swordDamage; }
    /** @return la défense de base d'un Bouclier de ses ennemis, ou 0 s'ils gardent celle de base. */
    public int getShieldDefense() { return shieldDefense; }
    /** @return le soin de base d'une Potion de ses ennemis, en % de leurs PV max, ou 0 s'ils gardent celui de base. */
    public int getHealPercent() { return healPercent; }
    /** @return le coût d'un tirage dans ses combats, en gains (voir {@link fr.astratime.lucky.entities.SpinEconomy}). */
    public int getSpinCost() { return spinCost; }

    /** @return le lieu où l'on combat {@code kind} (soldat ou roi d'un de ses donjons), ou {@code null}. */
    public static Place of(EnemyKind kind) {
        for (Place place : values()) {
            for (Dungeon dungeon : place.dungeons) {
                if (dungeon.getSoldier() == kind || dungeon.getKing() == kind) return place;
            }
        }
        return null;
    }

    /**
     * @return le rouleau gagné en vidant les quatre donjons du lieu (le Rouleau
     *         de la Mine pour les Mines d'Or), ou {@code null}
     */
    public Symbol getReelReward() { return this == MINES ? Symbol.NUGGET : null; }

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
    public String getName() { return Lang.t(name); }
    /** @return son nom court, dans la liste des lieux (ex : "Le Port"). */
    public String getShortName() { return Lang.t(shortName); }
    /** @return sa présentation, en quelques phrases courtes. */
    public String getDescription() { return Lang.t(description); }
    /** @return ses donjons, dans l'ordre de leurs icônes sur la carte. */
    public List<Dungeon> getDungeons() { return dungeons; }
}
