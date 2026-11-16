package fr.astratime.lucky.entities;

/**
 * Bonus que le rang du joueur (acheté à la boutique) et son Prestige lui donnent en combat.
 *
 * @param hp       PV max en plus
 * @param attack   ajouté à la valeur de base de chaque rouleau d'attaque
 * @param defense  ajouté à la valeur de base de chaque rouleau de défense
 * @param gains    gains de la machine en plus, en % (formule des gains d'Astra, 2026-11-16 :
 *                 symboles, Paire, Bingo et Jeu bonus, voir {@code GainFormula})
 * @param heart    bonus d'attaque de chaque Cœur d'or joué (l'ancien bonus plat des rouleaux de gains,
 *                 gardé tel quel en attendant que le Cœur d'or soit revu)
 * @param prestige niveau de Prestige (0 : aucun) : PV max et valeur de base des rouleaux d'attaque et
 *                 de défense multipliés par {@link #PRESTIGE_GUARD_FACTOR} à chaque niveau, gains
 *                 +{@link #PRESTIGE_PERCENT} % par niveau
 */
public record RankBonus(int hp, int attack, int defense, int gains, int heart, int prestige) {

    /** Aucun rang : aucun bonus. */
    public static final RankBonus NONE = new RankBonus(0, 0, 0, 0, 0);

    /** Ce que chaque niveau de Prestige ajoute aux gains, en %. */
    public static final int PRESTIGE_PERCENT = 10;

    /**
     * PV max, attaque et défense : multipliés par ce facteur à chaque niveau (x29 au Prestige X). Astra : en
     * mode difficile, la force des ennemis double à chaque chapitre, le joueur ne survivait pas.
     */
    public static final double PRESTIGE_GUARD_FACTOR = 1.4;

    /** Bonus d'un rang, sans Prestige. */
    public RankBonus(int hp, int attack, int defense, int gains, int heart) {
        this(hp, attack, defense, gains, heart, 0);
    }

    /** Bonus d'un rang sans Prestige ni Cœur d'or. */
    public RankBonus(int hp, int attack, int defense, int gains) {
        this(hp, attack, defense, gains, 0, 0);
    }

    /** @return les mêmes bonus, au niveau de Prestige {@code level}. */
    public RankBonus withPrestige(int level) {
        return new RankBonus(hp, attack, defense, gains, heart, Math.max(0, level));
    }

    /** @return le multiplicateur des gains du Prestige : x1,1 par niveau ajouté (1 sans Prestige). */
    public double prestigeGainsFactor() { return 1.0 + prestige * PRESTIGE_PERCENT / 100.0; }

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
