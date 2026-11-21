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
        PlaceRule.NONE, 1, 0, 0, 0, 100, 100, 500, List.of(Dungeon.PIQUE, Dungeon.TREFLE, Dungeon.COEUR, Dungeon.CARREAU)),

    FOIRE("La Fête foraine", "La Foire",
        "Une fête foraine s'est installée au bord de la prairie. La nuit, les manèges tournent seuls. "
            + "Les forains, eux, trichent toujours.",
        PlaceRule.MANEGE, 2, 30, 300, 15, 93, 700, 1_000,
        List.of(Dungeon.CHAPITEAU, Dungeon.TRAIN_FANTOME, Dungeon.GRANDE_ROUE, Dungeon.STANDS)),

    RAPIDE("Le Rapide de Minuit", "Le Rapide",
        "Un train de nuit relie la Foire au Port. On y joue toute la nuit, wagon après wagon. "
            + "Au bout du convoi, un fourgon blindé transporte l'or de la comète.",
        PlaceRule.WAGON_BONDE, 3, 40, 550, 12, 112, 1_500, 1_250,
        List.of(Dungeon.LOCOMOTIVE, Dungeon.WAGON_RESTAURANT, Dungeon.WAGON_LIT, Dungeon.FOURGON)),

    PORT("Le Port des Contrebandiers", "Le Port",
        "Les bateaux n'apportent plus d'épices. Ils apportent des cartes truquées. Sur les quais, tout s'achète. "
            + "Même la chance.",
        PlaceRule.SCORBUT, 4, 50, 1_000, 10, 135, 3_000, 1_500, List.of(Dungeon.CALE, Dungeon.TAVERNE, Dungeon.PHARE, Dungeon.GALION)),

    MINES("Les Mines d'Or", "Les Mines",
        "Sous la montagne, l'or de la comète coule encore. Les mineurs creusent sans fin. Ils ont oublié la lumière. "
            + "Pas leur cupidité.",
        PlaceRule.GRISOU, 6, 150, 10_000, 20, 78, 12_000, 2_500, List.of(Dungeon.FILON, Dungeon.PUITS, Dungeon.FORGE, Dungeon.GOUFFRE)),

    CASINO("Le Casino Englouti", "Le Casino",
        "Le plus grand casino du monde a sombré une nuit de jackpot. Sous l'eau, les machines tournent toujours. "
            + "Les joueurs aussi.",
        PlaceRule.MAREE, 8, 400, 50_000, 30, 60, 45_000, 4_000, List.of(Dungeon.BAR, Dungeon.MACHINES, Dungeon.COFFRES, Dungeon.VIP)),

    TEMPLE("Le Temple du Destin", "Le Temple",
        "Là où la mer s'est retirée, un temple s'accroche aux falaises. Ses moines lisent l'avenir dans les dés. "
            + "On dit qu'ils savent déjà qui gagnera.",
        PlaceRule.PRESAGE, 9, 320, 55_000, 27, 140, 80_000, 4_500,
        List.of(Dungeon.PARVIS, Dungeon.JARDIN_ORACLES, Dungeon.SALLE_DES_DES, Dungeon.SANCTUAIRE)),

    DESERT("Le Désert de Braise", "Le Désert",
        "Au-delà du Casino Englouti, la mer s'est retirée. Il ne reste qu'un désert brûlant. Au loin, un volcan fume. "
            + "On dit qu'un dragon y dort sur l'or de la comète.",
        PlaceRule.SOIF, 10, 250, 60_000, 25, 196, 140_000, 5_000,
        List.of(Dungeon.CARAVANE, Dungeon.TOMBEAU, Dungeon.OASIS, Dungeon.VOLCAN));

    private final String        name;
    private final String        shortName;
    private final String        description;
    private final PlaceRule     rule;
    private final int           tier;
    private final int           swordDamage;
    private final int           shieldDefense;
    private final int           healPercent;
    private final int           damagePercent;
    private final int           spinCost;
    private final int           chestSpins;
    private final List<Dungeon> dungeons;

    /** Nombre de lieux de l'Exploration une fois tous construits (Astra, 2026-11-20 : un rang par lieu). */
    public static final int FINAL_TIERS = 10;

    /**
     * @param tier          sa place dans l'Exploration à dix lieux (1 la prairie … 10 le Désert), même tant que
     *                      les lieux d'entre deux ne sont pas encore construits : le rang du lieu, le mode
     *                      difficile et les sceaux de Prestige se règlent sur elle
     * @param swordDamage   dégâts de base d'une Épée de ses ennemis (0 : ceux de base)
     * @param shieldDefense défense de base d'un Bouclier de ses ennemis (0 : celle de base)
     * @param healPercent   soin de base d'une Potion de ses ennemis, en % de leurs PV max (0 : celui de base)
     * @param damagePercent part des coups de ses ennemis qui porte, en % (la prairie la règle donjon par donjon)
     * @param spinCost      coût d'un tirage dans ses combats, en gains : très écarté d'un lieu à l'autre (Astra,
     *                      2026-10-29) pour qu'un rang gagne au moins deux fois plus dans son lieu que dans celui d'avant
     * @param chestSpins    pièces du coffre de ses donjons, en coûts de tirage 
     */
    Place(String name, String shortName, String description, PlaceRule rule, int tier,
          int swordDamage, int shieldDefense, int healPercent, int damagePercent, int spinCost, int chestSpins,
          List<Dungeon> dungeons) {
        this.name          = name;
        this.shortName     = shortName;
        this.description   = description;
        this.rule          = rule;
        this.tier          = tier;
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
            case RAPIDE  -> 350;
            case PORT    -> 500;
            case MINES   -> 1_000;
            case CASINO  -> 2_000;
            case TEMPLE  -> 2_500;
            case DESERT  -> 3_000;
        };
    }

    /**
     * @return les PV de ses ennemis, en % de ceux de leur fiche. Modèle de Puissance (Astra, 2026-11-18) : les
     *         bonus d'attaque s'additionnent, les gros tours ne font plus des centaines de fois les PV, donc les PV
     *         redescendent à environ 90 coups de base du meilleur rang du lieu pour un soldat (le double pour un roi),
     *         réglés au simulateur pour garder les victoires du joueur moyen (avant : Mines 135, Casino 140, Désert
     *         350, les autres 100)
     */
    public int getHpPercent() {
        return switch (this) {
            case PRAIRIE -> 55;
            case FOIRE   -> 32;
            case RAPIDE  -> 24;
            case PORT    -> 18;
            case MINES   -> 19;
            case CASINO  -> 15;
            case TEMPLE  -> 25;
            case DESERT  -> 42;
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

    /** @return le coût d'un tirage dans ses combats, en mode difficile si {@code hard} (voir {@link HardExploration}). */
    public int getSpinCost(boolean hard) { return hard ? HardExploration.spinCost(this) : spinCost; }

    /**
     * @return les pièces d'un coffre de ses donjons, en mode difficile si {@code hard} : le coffre du Désert
     *         (5 000 coûts de tirage) au coût de tirage du lieu difficile
     */
    public long getChestCoins(boolean hard) {
        return hard ? (long) DESERT.chestSpins * HardExploration.spinCost(this) : getChestCoins();
    }

    /** @return les pièces gardées au plus après une défaite, en mode difficile si {@code hard}. */
    public long getDefeatCap(boolean hard) { return (long) DEFEAT_CAP_SPINS * getSpinCost(hard); }

    /** @return les pièces en plus d'un coffre dont la carte est déjà au maximum, en mode difficile si {@code hard}. */
    public long getDuplicateCoins(boolean hard) { return getChestCoins(hard) / 2; }

    /** @return {@code true} si {@code kind} est le roi d'un des donjons de ce lieu. */
    public boolean isKing(EnemyKind kind) {
        for (Dungeon dungeon : dungeons) if (dungeon.getKing() == kind) return true;
        return false;
    }

    /** @return le lieu où l'on combat {@code kind} (soldat ou roi d'un de ses donjons), ou {@code null}. */
    public static Place of(EnemyKind kind) {
        if (kind.isRival()) return Rival.placeOf(kind); // le Joueur Masqué : le lieu de sa rencontre
        for (Place place : values()) {
            for (Dungeon dungeon : place.dungeons) {
                if (dungeon.getSoldier() == kind || dungeon.getKing() == kind) return place;
            }
        }
        return null;
    }

    /**
     * @return le rouleau gagné en vidant les quatre donjons du lieu (la Pépite pour
     *         les Mines d'Or, le Ticket, l'Ancre, le Jeton d'or et la Flamme pour la
     *         Foire, le Port, le Casino et le Désert), ou {@code null} (la prairie)
     */
    public Symbol getReelReward() {
        return switch (this) {
            case PRAIRIE -> null;
            case FOIRE   -> Symbol.TICKET;
            case RAPIDE  -> Symbol.STEAM;
            case PORT    -> Symbol.ANCHOR;
            case MINES   -> Symbol.NUGGET;
            case CASINO  -> Symbol.GOLD_CHIP;
            case TEMPLE  -> Symbol.GONG;
            case DESERT  -> Symbol.FLAME;
        };
    }

    /**
     * Drain de vie relatif (2026-11-20) : la part des coups de ses ennemis gardée, en % (voir
     * {@link EnemyKind#hurt}), réglée au simulateur pour que le joueur moyen gagne aussi souvent qu'avant.
     */
    public int getDrainScale() {
        return switch (this) {
            case PRAIRIE -> 90;
            case FOIRE   -> 78;
            case RAPIDE  -> 77;
            case PORT    -> 76;
            case MINES   -> 63;
            case CASINO  -> 72;
            case TEMPLE  -> 57;
            case DESERT  -> 42;
        };
    }

    /** @return sa place dans l'Exploration à dix lieux, de 1 (la prairie) à {@value #FINAL_TIERS} (le Désert). */
    public int getTier() { return tier; }

    /**
     * @return le premier lieu construit dont la place est {@code tier} ou au-dessus (le Désert au-delà) : tant qu'un
     *         lieu prévu n'existe pas, c'est celui d'après qui le remplace
     */
    public static Place atTier(int tier) {
        for (Place place : values()) if (place.tier >= tier) return place;
        return DESERT;
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
    public String getName() { return Lang.t(name); }
    /** @return son nom court, dans la liste des lieux (ex : "Le Port"). */
    public String getShortName() { return Lang.t(shortName); }
    /** @return sa présentation, en quelques phrases courtes. */
    public String getDescription() { return Lang.t(description); }
    /** @return ses donjons, dans l'ordre de leurs icônes sur la carte. */
    public List<Dungeon> getDungeons() { return dungeons; }
}
