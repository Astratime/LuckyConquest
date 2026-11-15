package fr.astratime.lucky.entities;

/**
 * Bonus que le rang du joueur (acheté à la boutique) et son Prestige lui donnent en combat.
 *
 * @param hp       PV max en plus
 * @param attack   ajouté à la valeur de base de chaque rouleau d'attaque
 * @param defense  ajouté à la valeur de base de chaque rouleau de défense
 * @param gains    ajouté à la valeur de base de chaque rouleau de gains
 * @param prestige niveau de Prestige (0 : aucun) : PV max et valeur de base des rouleaux d'attaque et
 *                 de défense multipliés par {@link #PRESTIGE_GUARD_FACTOR} à chaque niveau, rouleaux de
 *                 gains +{@link #PRESTIGE_PERCENT} % par niveau, bonus du rang compris
 */
public record RankBonus(int hp, int attack, int defense, int gains, int prestige) {

    /** Aucun rang : aucun bonus. */
    public static final RankBonus NONE = new RankBonus(0, 0, 0, 0);

    /** Ce que chaque niveau de Prestige ajoute aux gains, en %. */
    public static final int PRESTIGE_PERCENT = 10;

    /**
     * PV max, attaque et défense : multipliés par ce facteur à chaque niveau (x29 au Prestige X). Astra : en
     * mode difficile, la force des ennemis double à chaque chapitre, le joueur ne survivait pas.
     */
    public static final double PRESTIGE_GUARD_FACTOR = 1.4;

    /** Bonus d'un rang, sans Prestige. */
    public RankBonus(int hp, int attack, int defense, int gains) {
        this(hp, attack, defense, gains, 0);
    }

    /** @return les mêmes bonus, au niveau de Prestige {@code level}. */
    public RankBonus withPrestige(int level) {
        return new RankBonus(hp, attack, defense, gains, Math.max(0, level));
    }

    /** @return le bonus que le Prestige ajoute à {@code value} (0 sans Prestige). */
    public int prestigeBonus(int value) {
        return (int) Math.round((double) value * prestige * PRESTIGE_PERCENT / 100.0);
    }

    /** @return {@code value} avec le bonus du Prestige. */
    public int withPrestigeBonus(int value) { return value + prestigeBonus(value); }

    /** @return le multiplicateur des PV max, de l'attaque et de la défense au niveau de Prestige (1 sans Prestige). */
    public double guardFactor() { return Math.pow(PRESTIGE_GUARD_FACTOR, prestige); }

    /** @return ce que le Prestige ajoute à une valeur de PV max, d'attaque ou de défense {@code value} (0 sans Prestige). */
    public int guardBonus(int value) {
        return (int) Math.min(Integer.MAX_VALUE - (long) value, Math.round(value * (guardFactor() - 1.0)));
    }
}
