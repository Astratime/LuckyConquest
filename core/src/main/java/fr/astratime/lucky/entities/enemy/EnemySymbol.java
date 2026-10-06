package fr.astratime.lucky.entities.enemy;

import fr.astratime.lucky.i18n.Lang;

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
    BANK_BITE("MORSURE"),
    /** Une part des gains du joueur devient fausse : elle disparaît au prochain tirage, si elle n'est pas dépensée. */
    FAKE_MONEY("FAUSSE MONNAIE"),
    /** Il annonce un symbole : s'il sort au prochain tirage du joueur, ses attaques du tour suivant triplent. */
    PREDICTION("PRÉDICTION"),
    /** Chacun tire une carte au hasard de son deck : la plus haute frappe l'autre. */
    DUEL("DUEL"),
    /** Au prochain tour du joueur, chaque carte jouée lui coûte une part de ses gains, puis une somme fixe. */
    TAX("TAXE"),
    /** Une règle du jeu change pendant deux tours : sans combinaisons, sans Bingo, ou rouleaux qui tournent deux fois. */
    NEW_RULE("NOUVELLE RÈGLE"),
    /** Il confisque une carte du deck du joueur jusqu'à la fin du combat (plus s'il a gros gains). */
    FRISK("FOUILLE"),
    /** Tout ou rien : plus riche que lui, le joueur perd tous ses gains ; sinon, lui perd une part de ses PV. */
    BANKRUPTCY("FAILLITE");

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

    /** Part des gains du joueur rendue fausse par chaque Fausse monnaie, en pourcentage (avant sa force). */
    public static final int FAKE_PERCENT = 5;
    /** Ses attaques sont multipliées d'autant quand sa Prédiction sort sur les rouleaux du joueur. */
    public static final int PREDICTION_FACTOR = 3;
    /** Duel gagné par le joueur : l'ennemi perd cette part de ses PV max par rang de la carte, en pour mille. */
    public static final int DUEL_PER_MILLE_PER_RANK = 2;
    /** Part des gains retirée par la Taxe à chaque carte jouée, en pourcentage (fixée par Astra, sans sa force). */
    public static final int TAX_PERCENT = 20;
    /** Gains retirés en plus par la Taxe à chaque carte jouée, même à sec : les gains peuvent passer sous zéro. */
    public static final int TAX_FLAT = 1_000;
    /** Tours pendant lesquels vaut la Nouvelle règle. */
    public static final int NEW_RULE_TURNS = 2;
    /** Une Fouille confisque une carte de plus par tranche de ces gains du joueur. */
    public static final int FRISK_GAINS_STEP = 100_000;
    /** Une Fouille ne confisque pas plus de cartes. */
    public static final int FRISK_MAX = 3;
    /** Fortune de la Banqueroute, en pourcentage de ses PV restants. */
    public static final int BANKRUPTCY_FORTUNE_PERCENT = 20;
    /** PV perdus par la Banqueroute quand sa Faillite tourne mal, en pourcentage de ses PV restants. */
    public static final int BANKRUPTCY_HP_PERCENT = 20;

    private final String displayName;

    EnemySymbol(String displayName) {
        this.displayName = displayName;
    }

    /** @return le nom affiché du symbole (ex : "ÉPÉE"). */
    public String getDisplayName() { return Lang.t(displayName); }

    /**
     * @return la description de l'effet de base du symbole chez l'ennemi
     *         {@code kind} (renforcé par sa force ; ses Potions et ses Crocs
     *         soignent selon sa force de soin)
     */
    public String getDescription(EnemyKind kind) {
        return switch (this) {
            case SWORD  -> Lang.f("Épée : attaque de {0}", kind.empowered(kind.swordDamage()));
            case SHIELD -> Lang.f("Bouclier : défense +{0} pendant le tour suivant",
                kind.empowered(kind.shieldDefense()));
            case POTION -> Lang.f("Potion : soigne {0} % des PV max", percent(kind.potionPercent(0)));
            case THORNS -> Lang.f("Épines : renvoie {0} % de tes dégâts. Au début de son tour", kind.thornsPercent());
            case FANG   -> Lang.f("Croc : mord de {0}. Chaque PV volé lui rend {1} % de ses PV max",
                kind.empowered(FANG_DAMAGE), percent(kind.drainPercent()));
            case RAGE   -> Lang.f("Rage : attaque +{0}. Jusqu'à la fin du combat", kind.empowered(RAGE_ATTACK));
            case LOADED_DIE -> Lang.f("Dé pipé : tes jauges (Coffre, Sang, Lames) perdent {0} %", kind.diePercent());
            case INTEREST -> Lang.f("Intérêts : il prend {0} % de tes gains. Attaque +1 par {1} volés, au prochain coup. Sans gains, "
                + "il mord",
                kind.interestPercent(), INTEREST_PER_ATTACK);
            case ZERO   -> Lang.t("Zéro : rouge, ses attaques doublent. Noir, ses Boucliers. Zéro, les deux");
            case MIRROR -> Lang.t("Reflet : il rejoue ta dernière carte. À moitié de sa force");
            case HOURGLASS -> Lang.f("Sablier : +1. À {0}, il explose ({1}). Tes gros coups le font reculer",
                HOURGLASS_MAX, kind.empowered(HOURGLASS_DAMAGE));
            case ALL_IN -> Lang.t("Tapis : à son prochain tour, ses attaques doublent. Touche-le avant pour l'annuler");
            case NIBBLE -> Lang.t("Grignotage : il ronge une carte. Au prochain tour, une carte de ta main part à la défausse");
            case DRUNK  -> Lang.t("Ivresse : à ton prochain tirage, un rouleau tourne deux fois et garde le pire résultat");
            case BLIND  -> Lang.t("Aveuglement : au prochain tour, tes cartes en main sont faces cachées");
            case BOARDING -> Lang.t("Abordage : il vole ta meilleure carte en main pour tout le combat. Il la joue contre toi");
            case NUGGET -> Lang.t("Pépite : à ton prochain tirage, tes symboles de gain deviennent des pierres. Ils ne rapportent "
                + "rien");
            case DRILL  -> Lang.f("Forage : attaque de {0}. Traverse ton bouclier",
                kind.empowered(kind.swordDamage()));
            case ANVIL  -> Lang.f("Enclume : attaque +{0}. Jusqu'à la fin du combat, sans limite",
                kind.empowered(ANVIL_ATTACK));
            case SONG   -> Lang.t("Chant : au début de ton prochain tour, une carte au hasard de ta main est jouée d'office");
            case BANK_BITE -> Lang.f("Morsure : il dévore {0} % de tes gains et se soigne d'autant. Sans gains, il mord",
                kind.empowered(BANK_BITE_PERCENT));
            case FAKE_MONEY -> Lang.f("Fausse monnaie : {0} % de tes gains deviennent faux. Dépense-les avant ton tirage, ou ils "
                + "disparaissent",
                kind.fakePercent());
            case PREDICTION -> Lang.t("Prédiction : il annonce un de tes symboles. S'il sort à ton tirage, ses attaques du tour "
                + "triplent");
            case DUEL -> Lang.t("Duel : chacun tire une carte de son deck. La plus haute frappe l'autre. Tes As et tes figures "
                + "comptent");
            case TAX -> Lang.f("Taxe : au prochain tour, chaque carte jouée te coûte {0} % de tes gains, puis {1}. Tes gains "
                + "peuvent passer sous zéro",
                TAX_PERCENT, TAX_FLAT);
            case NEW_RULE -> Lang.f("Nouvelle règle : pendant {0} tours, plus de combinaisons, ou plus de Bingo, ou tes rouleaux "
                + "tournent deux fois (le pire reste)",
                NEW_RULE_TURNS);
            case FRISK -> Lang.f("Fouille : il confisque une carte de ton deck jusqu'à la fin du combat. Une de plus par {0} gains",
                FRISK_GAINS_STEP);
            case BANKRUPTCY -> Lang.f("Faillite : si tes gains dépassent sa fortune ({0} % de ses PV), il te prend tout. Sinon, il "
                + "perd {1} % de ses PV",
                BANKRUPTCY_FORTUNE_PERCENT, BANKRUPTCY_HP_PERCENT);
        };
    }

    /** @return {@code value} sans décimale inutile (ex : "30", "1,5"). */
    private static String percent(float value) {
        return value == Math.round(value) ? String.valueOf(Math.round(value))
            : Lang.decimal(String.valueOf(Math.round(value * 10f) / 10f));
    }

    /** @return la description de l'effet de base du symbole (chez le croupier), pour son infobulle. */
    public String getDescription() {
        return getDescription(EnemyKind.CROUPIER);
    }
}
