package fr.astratime.lucky.entities.enemy;

/**
 * Symboles de la machine à sous de l'ennemi. Leur effet de base est renforcé
 * par les cartes que l'ennemi joue (voir {@link EnemyCards}).
 */
public enum EnemySymbol {

    /** Attaque le joueur. */
    SWORD("ÉPÉE"),
    /** Ajoute de la défense à l'ennemi, pendant le prochain tour du joueur. */
    SHIELD("BOUCLIER"),
    /** Rend des PV à l'ennemi, en pourcentage de ses PV max. */
    POTION("POTION"),
    /** Renvoie au joueur une part des dégâts qu'il inflige pendant son prochain tour. */
    THORNS("ÉPINES"),
    /** Mord le joueur et rend à l'ennemi une part de ses PV max pour chaque PV volé. */
    FANG("CROC"),
    /** Toutes ses attaques frappent plus fort, jusqu'à la fin du combat. */
    RAGE("RAGE"),
    /** Il triche : les jauges du joueur (Coffre, Sang, Lames) perdent une part de leur contenu. */
    LOADED_DIE("DÉ PIPÉ"),
    /** Il prend une part des gains du joueur ; ce qu'il vole renforce sa prochaine attaque. */
    INTEREST("INTÉRÊTS"),
    /** Il lance une roulette : rouge, ses attaques du tour doublent ; noir, ses Boucliers ; zéro, les deux. */
    ZERO("ZÉRO"),
    /** Il rejoue la dernière carte jouée par le joueur, à moitié de sa force. */
    MIRROR("REFLET"),
    /** Son compte à rebours avance ; arrivé au bout, il explose. */
    HOURGLASS("SABLIER"),
    /** Il fait tapis : à son tour suivant, sa mise double (ses attaques avec), sauf s'il est touché d'ici là. */
    ALL_IN("TAPIS"),
    /** Il ronge une carte : au prochain tour du joueur, une carte de sa main part à la défausse. */
    NIBBLE("GRIGNOTAGE"),
    /** Au prochain tirage du joueur, un de ses rouleaux tourne deux fois et garde le pire résultat. */
    DRUNK("IVRESSE"),
    /** Au prochain tour du joueur, ses cartes en main sont faces cachées. */
    BLIND("AVEUGLEMENT"),
    /** Il vole la meilleure carte du joueur (pour tout le combat) et la joue contre lui. */
    BOARDING("ABORDAGE"),
    /** Au prochain tirage du joueur, ses symboles de gain deviennent des pierres : ils ne rapportent rien. */
    NUGGET("PÉPITE"),
    /** Une attaque qui traverse le bouclier du joueur. */
    DRILL("FORAGE"),
    /** Son attaque grandit, pour tout le combat (sans limite). */
    ANVIL("ENCLUME"),
    /** Au début du prochain tour du joueur, une carte au hasard de sa main est jouée d'office. */
    SONG("CHANT"),
    /** Il dévore une part des gains du joueur et se soigne d'autant. */
    BANK_BITE("MORSURE");

    /** Dégâts de base d'une Épée. */
    public static final int SWORD_DAMAGE   = 20;
    /** Défense de base d'un Bouclier. */
    public static final int SHIELD_DEFENSE = 100;
    /** Soin de base d'une Potion, en pourcentage des PV max de l'ennemi. */
    public static final int POTION_PERCENT = 30;
    /** Part des dégâts du joueur renvoyée par chaque Épines, en pourcentage. */
    public static final int THORNS_PERCENT = 2;
    /** Dégâts de base d'un Croc. */
    public static final int FANG_DAMAGE    = 10;
    /** Soin d'un Croc, en pourcentage des PV max de l'ennemi, pour chaque PV volé au joueur. */
    public static final int FANG_DRAIN     = 1;
    /** Attaque ajoutée par chaque Rage, jusqu'à la fin du combat. */
    public static final int RAGE_ATTACK    = 3;
    /** La Rage ne dépasse pas ce bonus d'attaque. */
    public static final int RAGE_MAX       = 30;
    /** Les Épines ne renvoient pas plus que ces dégâts en un tour. */
    public static final int THORNS_MAX     = 20;
    /** Part des jauges du joueur vidée par chaque Dé pipé, en pourcentage. */
    public static final int DIE_PERCENT    = 25;
    /** Part des gains du joueur prise par chaque Intérêts, en pourcentage. */
    public static final int INTEREST_PERCENT = 5;
    /** Gains volés pour chaque point d'attaque ajouté par les Intérêts. */
    public static final int INTEREST_PER_ATTACK = 100;
    /** Les Intérêts n'ajoutent pas plus que cette attaque. */
    public static final int INTEREST_MAX   = 25;
    /** Chances sur 37 que la roulette du Zéro tombe sur le zéro (le reste : moitié rouge, moitié noir). */
    public static final int ZERO_POCKETS   = 5;
    /** Tours de Sablier avant l'explosion. */
    public static final int HOURGLASS_MAX  = 5;
    /** Dégâts de l'explosion du Sablier (plus sa Rage). */
    public static final int HOURGLASS_DAMAGE = 35;
    /** Un coup du joueur d'au moins cette part de ses PV max (en pour mille) fait reculer son Sablier. */
    public static final int HOURGLASS_HIT_PER_MILLE = 10;
    /** Le Tapis ne monte pas au-delà de cette mise (multiplicateur de ses attaques). */
    public static final int ALL_IN_MAX     = 4;
    /** Attaque ajoutée par chaque Enclume, jusqu'à la fin du combat (sans limite). */
    public static final int ANVIL_ATTACK   = 5;
    /** Part des gains du joueur dévorée par chaque Morsure de la banque, en pourcentage. */
    public static final int BANK_BITE_PERCENT = 10;
    /** Les trois rouleaux de l'ennemi identiques (Jackpot) : ses attaques et ses Boucliers du tour sont multipliés d'autant. */
    public static final int JACKPOT_FACTOR = 5;

    private final String displayName;

    EnemySymbol(String displayName) {
        this.displayName = displayName;
    }

    /** @return le nom affiché du symbole (ex : "ÉPÉE"). */
    public String getDisplayName() { return displayName; }

    /**
     * @return la description de l'effet de base du symbole chez l'ennemi
     *         {@code kind} (renforcé par sa force ; ses Potions et ses Crocs
     *         soignent selon sa force de soin)
     */
    public String getDescription(EnemyKind kind) {
        return switch (this) {
            case SWORD  -> "Épée : attaque de " + kind.empowered(kind.swordDamage());
            case SHIELD -> "Bouclier : défense +" + kind.empowered(SHIELD_DEFENSE) + " pendant le tour suivant";
            case POTION -> "Potion : soigne " + percent(kind.potionPercent(0)) + " % des PV max";
            case THORNS -> "Épines : renvoie " + kind.thornsPercent() + " % de tes dégâts. Au début de son tour";
            case FANG   -> "Croc : mord de " + kind.empowered(FANG_DAMAGE) + ". Chaque PV volé lui rend "
                + percent(kind.drainPercent()) + " % de ses PV max";
            case RAGE   -> "Rage : attaque +" + kind.empowered(RAGE_ATTACK) + ". Jusqu'à la fin du combat";
            case LOADED_DIE -> "Dé pipé : tes jauges (Coffre, Sang, Lames) perdent " + kind.diePercent() + " %";
            case INTEREST -> "Intérêts : il prend " + kind.interestPercent() + " % de tes gains. Attaque +1 par " + INTEREST_PER_ATTACK + " volés, au prochain coup. Sans gains, il mord";
            case ZERO   -> "Zéro : rouge, ses attaques doublent. Noir, ses Boucliers. Zéro, les deux";
            case MIRROR -> "Reflet : il rejoue ta dernière carte. À moitié de sa force";
            case HOURGLASS -> "Sablier : +1. À " + HOURGLASS_MAX + ", il explose (" + kind.empowered(HOURGLASS_DAMAGE)
                + "). Tes gros coups le font reculer";
            case ALL_IN -> "Tapis : à son prochain tour, ses attaques doublent. Touche-le avant pour l'annuler";
            case NIBBLE -> "Grignotage : il ronge une carte. Au prochain tour, une carte de ta main part à la défausse";
            case DRUNK  -> "Ivresse : à ton prochain tirage, un rouleau tourne deux fois et garde le pire résultat";
            case BLIND  -> "Aveuglement : au prochain tour, tes cartes en main sont faces cachées";
            case BOARDING -> "Abordage : il vole ta meilleure carte en main pour tout le combat. Il la joue contre toi";
            case NUGGET -> "Pépite : à ton prochain tirage, tes symboles de gain deviennent des pierres. Ils ne rapportent rien";
            case DRILL  -> "Forage : attaque de " + kind.empowered(kind.swordDamage()) + ". Traverse ton bouclier";
            case ANVIL  -> "Enclume : attaque +" + kind.empowered(ANVIL_ATTACK) + ". Jusqu'à la fin du combat, sans limite";
            case SONG   -> "Chant : au début de ton prochain tour, une carte au hasard de ta main est jouée d'office";
            case BANK_BITE -> "Morsure : il dévore " + kind.empowered(BANK_BITE_PERCENT) + " % de tes gains et se soigne d'autant. Sans gains, il mord";
        };
    }

    /** @return {@code value} sans décimale inutile (ex : "30", "1,5"). */
    private static String percent(float value) {
        return value == Math.round(value) ? String.valueOf(Math.round(value))
            : String.valueOf(Math.round(value * 10f) / 10f).replace('.', ',');
    }

    /** @return la description de l'effet de base du symbole (chez le croupier), pour son infobulle. */
    public String getDescription() {
        return getDescription(EnemyKind.CROUPIER);
    }
}
