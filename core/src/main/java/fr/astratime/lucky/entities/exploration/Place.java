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
        PlaceRule.NONE, 0, 0, 0, 100, 100, 500, List.of(Dungeon.PIQUE, Dungeon.TREFLE, Dungeon.COEUR, Dungeon.CARREAU)),

    FOIRE("La Fête foraine", "La Foire",
        "Une fête foraine s'est installée au bord de la prairie. La nuit, les manèges tournent seuls. "
            + "Les forains, eux, trichent toujours.",
        PlaceRule.MANEGE, 30, 300, 15, 93, 700, 1_000,
        List.of(Dungeon.CHAPITEAU, Dungeon.TRAIN_FANTOME, Dungeon.GRANDE_ROUE, Dungeon.STANDS)),

    PORT("Le Port des Contrebandiers", "Le Port",
        "Les bateaux n'apportent plus d'épices. Ils apportent des cartes truquées. Sur les quais, tout s'achète. "
            + "Même la chance.",
        PlaceRule.SCORBUT, 50, 1_000, 10, 104, 3_000, 1_500, List.of(Dungeon.CALE, Dungeon.TAVERNE, Dungeon.PHARE, Dungeon.GALION)),

    MINES("Les Mines d'Or", "Les Mines",
        "Sous la montagne, l'or de la comète coule encore. Les mineurs creusent sans fin. Ils ont oublié la lumière. "
            + "Pas leur cupidité.",
        PlaceRule.GRISOU, 150, 10_000, 20, 52, 12_000, 2_500, List.of(Dungeon.FILON, Dungeon.PUITS, Dungeon.FORGE, Dungeon.GOUFFRE)),

    CASINO("Le Casino Englouti", "Le Casino",
        "Le plus grand casino du monde a sombré une nuit de jackpot. Sous l'eau, les machines tournent toujours. "
            + "Les joueurs aussi.",
        PlaceRule.MAREE, 400, 50_000, 30, 24, 45_000, 4_000, List.of(Dungeon.BAR, Dungeon.MACHINES, Dungeon.COFFRES, Dungeon.VIP)),

    DESERT("Le Désert de Braise", "Le Désert",
        "Au-delà du Casino Englouti, la mer s'est retirée. Il ne reste qu'un désert brûlant. Au loin, un volcan fume. "
            + "On dit qu'un dragon y dort sur l'or de la comète.",
        PlaceRule.SOIF, 250, 60_000, 25, 98, 140_000, 5_000,
        List.of(Dungeon.CARAVANE, Dungeon.TOMBEAU, Dungeon.OASIS, Dungeon.VOLCAN));

    private final String        name;
    private final String        shortName;
    private final String        description;
    private final PlaceRule     rule;
    private final int           swordDamage;
    private final int           shieldDefense;
    private final int           healPercent;
    private final int           damagePercent;
    private final int           spinCost;
    private final int           chestSpins;
    private final List<Dungeon> dungeons;

    /**
     * @param swordDamage   dégâts de base d'une Épée de ses ennemis (0 : ceux de base)
     * @param shieldDefense défense de base d'un Bouclier de ses ennemis (0 : celle de base)
     * @param healPercent   soin de base d'une Potion de ses ennemis, en % de leurs PV max (0 : celui de base)
     * @param damagePercent part des coups de ses ennemis qui porte, en % (la prairie la règle donjon par donjon)
     * @param spinCost      coût d'un tirage dans ses combats, en gains : très écarté d'un lieu à l'autre (Astra,
     *                      2026-10-29) pour qu'un rang gagne au moins deux fois plus dans son lieu que dans celui d'avant
     * @param chestSpins    pièces du coffre de ses donjons, en coûts de tirage 
     */
    Place(String name, String shortName, String description, PlaceRule rule,
          int swordDamage, int shieldDefense, int healPercent, int damagePercent, int spinCost, int chestSpins,
          List<Dungeon> dungeons) {
        this.name          = name;
        this.shortName     = shortName;
        this.description   = description;
        this.rule          = rule;
        this.swordDamage   = swordDamage;
        this.shieldDefense = shieldDefense;
        this.healPercent   = healPercent;
        this.damagePercent = damagePercent;
        this.spinCost      = spinCost;
        this.chestSpins    = chestSpins;
        this.dungeons      = dungeons;
    }

    /** @return les dégâts de base d'une Épée de ses ennemis, soldats et rois, ou 0 s'ils gardent ceux de base. */
    public int getSwordDamage() { return swordDamage; }
    /** @return la défense de base d'un Bouclier de ses ennemis, ou 0 s'ils gardent celle de base. */
    public int getShieldDefense() { return shieldDefense; }
    /** @return le soin de base d'une Potion de ses ennemis, en % de leurs PV max, ou 0 s'ils gardent celui de base. */
    public int getHealPercent() { return healPercent; }
    /** @return la part des coups de ses ennemis qui porte, en % (voir {@link EnemyKind#damagePercent()}). */
    public int getDamagePercent() { return damagePercent; }
    /** @return le coût d'un tirage dans ses combats, en gains (voir {@link fr.astratime.lucky.entities.SpinEconomy}). */
    public int getSpinCost() { return spinCost; }

    /**
     * @return le coût de tirage d'avant l'étirement (Astra, 2026-10-29) : la force tirée des gains (Fortune,
     *         Bourse garnie) se compte avec lui, sinon elle grimperait avec l'économie (x47 au Désert)
     */
    public int getPowerSpinCost() {
        return switch (this) {
            case PRAIRIE -> 100;
            case FOIRE   -> 250;
            case PORT    -> 500;
            case MINES   -> 1_000;
            case CASINO  -> 2_000;
            case DESERT  -> 3_000;
        };
    }

    /**
     * @return les PV de ses ennemis, en % de ceux de leur fiche : relevés comme le bonus d'attaque du rang le plus
     *         fort prévu pour le lieu (Astra, 2026-10-30) : Mines (Gros bonnet 1 000 -> 1 350), Casino (Magnat
     *         2 000 -> 2 800), Désert (Légende 4 000 -> 7 000)
     */
    public int getHpPercent() {
        return switch (this) {
            case MINES  -> 135;
            case CASINO -> 140;
            case DESERT -> 175;
            default     -> 100;
        };
    }

    /** @return les pièces du coffre de ses donjons, en coûts de tirage (Prairie 500 … Désert 5 000). */
    public int getChestSpins() { return chestSpins; }

    /** @return les pièces d'un coffre de ses donjons (Prairie 50 000 … Désert 700 000 000). */
    public long getChestCoins() { return (long) chestSpins * spinCost; }

    /** Part gardée après une défaite, au plus : 500 coûts de tirage (Prairie 50 000 … Désert 70 000 000). */
    public static final int DEFEAT_CAP_SPINS = 500;

    /** @return les pièces gardées au plus après une défaite dans un de ses donjons. */
    public long getDefeatCap() { return (long) DEFEAT_CAP_SPINS * spinCost; }

    /** @return les pièces en plus d'un coffre dont la carte est déjà au maximum d'exemplaires : la moitié du coffre. */
    public long getDuplicateCoins() { return getChestCoins() / 2; }

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
