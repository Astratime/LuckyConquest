package fr.astratime.lucky.entities;

/**
 * Bonus que le rang du joueur (acheté à la boutique) lui donne en combat.
 *
 * @param hp      PV max en plus
 * @param attack  ajouté à la valeur de base de chaque rouleau d'attaque
 * @param defense ajouté à la valeur de base de chaque rouleau de défense
 * @param gains   ajouté à la valeur de base de chaque rouleau de gains
 */
public record RankBonus(int hp, int attack, int defense, int gains) {

    /** Aucun rang : aucun bonus. */
    public static final RankBonus NONE = new RankBonus(0, 0, 0, 0);
}
