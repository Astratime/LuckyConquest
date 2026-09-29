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
    POTION("POTION");

    /** Dégâts de base d'une Épée. */
    public static final int SWORD_DAMAGE   = 20;
    /** Défense de base d'un Bouclier. */
    public static final int SHIELD_DEFENSE = 100;
    /** Soin de base d'une Potion, en pourcentage des PV max de l'ennemi. */
    public static final int POTION_PERCENT = 30;

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
        };
    }
}
