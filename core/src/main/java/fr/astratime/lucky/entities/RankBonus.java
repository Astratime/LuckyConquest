package fr.astratime.lucky.entities;

/**
 * Bonus que le rang du joueur (acheté à la boutique) et son Prestige lui donnent en combat.
 *
 * @param hp       PV max en plus
 * @param attack   ajouté à la valeur de base de chaque rouleau d'attaque
 * @param defense  ajouté à la valeur de base de chaque rouleau de défense
 * @param gains    ajouté à la valeur de base de chaque rouleau de gains
 * @param prestige niveau de Prestige (0 : aucun) : +{@link #PRESTIGE_PERCENT} % par niveau
 *                 des PV max et de la valeur de base des rouleaux, bonus du rang compris
 */
public record RankBonus(int hp, int attack, int defense, int gains, int prestige) {

    /** Aucun rang : aucun bonus. */
    public static final RankBonus NONE = new RankBonus(0, 0, 0, 0);

    /** Ce que chaque niveau de Prestige ajoute, en %. */
    public static final int PRESTIGE_PERCENT = 10;

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
}
