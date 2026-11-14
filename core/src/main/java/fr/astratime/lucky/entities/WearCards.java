package fr.astratime.lucky.entities;

/**
 * Cartes à usure (demande d'Astra, 2026-11-14) : ce qu'elles retiennent le temps d'un combat.
 *  - Bougie : très forte au début, son bonus d'attaque perd une part à chaque fois qu'elle est jouée ;
 *  - Graine : faible au début, elle pousse à chaque tour passé sans être jouée, et repart de zéro une fois jouée.
 * (La Pile ou face, elle, ne retient rien : chaque pose est un nouveau lancer.)
 */
public final class WearCards {

    /** Fois où la Bougie a été jouée pendant ce combat. */
    private int candlePlays;
    /** Usure de la Bougie, en % de son bonus perdu à chaque pose ({@code 0} : jamais jouée). */
    private int candleWear;
    /** Bonus de départ de la Bougie, en % (celui de la dernière Bougie jouée). */
    private int candlePercent;
    /** Tours passés depuis que la Graine a été jouée (ou depuis le début du combat). */
    private int seedTurns;
    /** {@code true} si une Graine a été jouée à ce tour : elle ne pousse pas à la fin du tour. */
    private boolean seedPlayed;
    /** Valeurs de la Graine du deck (départ, pousse par tour, maximum), en % ; {@code null} : pas de Graine. */
    private int[] seed;

    // ----- Bougie -----

    /**
     * Bougie jouée : rend son bonus d'attaque de cette pose, puis l'use.
     *
     * @param percent bonus de départ, en %
     * @param wear    part du bonus perdue à chaque pose, en %
     * @return le bonus d'attaque de cette pose, en %
     */
    public int burnCandle(int percent, int wear) {
        int bonus = candleBonus(percent, wear, candlePlays);
        candlePlays++;
        candlePercent = percent;
        candleWear    = wear;
        return bonus;
    }

    /** @return le bonus de la Bougie à la pose suivante, en % ({@code 0} si elle n'a pas encore été jouée) */
    public int getNextCandle() {
        return candlePlays == 0 ? 0 : candleBonus(candlePercent, candleWear, candlePlays);
    }

    /** @return les fois où la Bougie a été jouée pendant ce combat */
    public int getCandlePlays() { return candlePlays; }

    /** @return le bonus d'une Bougie déjà jouée {@code plays} fois, en % (arrondi, jamais négatif) */
    public static int candleBonus(int percent, int wear, int plays) {
        return (int) Math.round(percent * Math.pow(1 - wear / 100.0, plays));
    }

    // ----- Graine -----

    /**
     * Graine jouée : rend son bonus d'attaque, qui a poussé à chaque tour passé sans être jouée, et la fait
     * repartir de zéro.
     *
     * @param percent bonus de départ, en %
     * @param growth  bonus gagné à chaque tour passé sans être jouée, en %
     * @param max     bonus au plus, en %
     * @return le bonus d'attaque de cette pose, en %
     */
    public int harvestSeed(int percent, int growth, int max) {
        seed = new int[] {percent, growth, max};
        int bonus = getSeedBonus();
        seedTurns  = 0;
        seedPlayed = true;
        return bonus;
    }

    /** @return le bonus de la Graine si elle était jouée maintenant, en % ({@code 0} : pas de Graine) */
    public int getSeedBonus() {
        return seed == null ? 0 : Math.min(seed[2], seed[0] + seed[1] * seedTurns);
    }

    /**
     * Fin du tour du joueur qui a une Graine dans ses cartes : si elle n'a pas été jouée à ce tour, elle pousse.
     *
     * @param percent bonus de départ de la Graine, en %
     * @param growth  bonus gagné à chaque tour passé sans être jouée, en %
     * @param max     bonus au plus, en %
     */
    public void growSeed(int percent, int growth, int max) {
        seed = new int[] {percent, growth, max};
        if (!seedPlayed) seedTurns++;
        seedPlayed = false;
    }

    /** @return {@code true} si la Graine a atteint son bonus le plus fort */
    public boolean isSeedRipe() { return seed != null && getSeedBonus() >= seed[2]; }

    /** @return les tours passés depuis que la Graine a été jouée (ou depuis le début du combat) */
    public int getSeedTurns() { return seedTurns; }

    /** @return {@code true} si rien n'est à afficher (aucune Bougie jouée, la Graine n'a pas poussé) */
    public boolean isEmpty() { return candlePlays == 0 && (seed == null || seedTurns == 0); }
}
