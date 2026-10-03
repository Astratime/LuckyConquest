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
    RAGE("RAGE");

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

    private final String displayName;

    EnemySymbol(String displayName) {
        this.displayName = displayName;
    }

    /** @return le nom affiché du symbole (ex : "ÉPÉE"). */
    public String getDisplayName() { return displayName; }

    /** @return la description de l'effet de base du symbole, pour son infobulle. */
    public String getDescription() {
        return switch (this) {
            case SWORD  -> "Épée : attaque de " + SWORD_DAMAGE;
            case SHIELD -> "Bouclier : défense +" + SHIELD_DEFENSE + " pendant le tour suivant";
            case POTION -> "Potion : soigne " + POTION_PERCENT + " % des PV max";
            case THORNS -> "Épines : renvoie " + THORNS_PERCENT + " % de tes dégâts. Au début de son tour";
            case FANG   -> "Croc : mord de " + FANG_DAMAGE + ". Chaque PV volé lui rend " + FANG_DRAIN
                + " % de ses PV max";
            case RAGE   -> "Rage : attaque +" + RAGE_ATTACK + ". Jusqu'à la fin du combat";
        };
    }
}
