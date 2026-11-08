package fr.astratime.lucky.entities.enemy;

/**
 * La difficulté d'un ennemi : mode normal, mode difficile de la Tour, ou un
 * étage du mode sans fin (voir {@link fr.astratime.lucky.entities.tower.EndlessRun}).
 *
 * @param hard  mode difficile (le mode sans fin l'est toujours)
 * @param floor étage du mode sans fin (à partir de 1), 0 hors du mode sans fin
 */
public record Difficulty(boolean hard, int floor) {

    /** Mode normal. */
    public static final Difficulty NORMAL = new Difficulty(false, 0);
    /** Mode difficile de la Tour. */
    public static final Difficulty HARD   = new Difficulty(true, 0);

    public Difficulty {
        if (floor < 0) throw new IllegalArgumentException("Étage négatif : " + floor);
    }

    /** @return le mode normal ou le mode difficile. */
    public static Difficulty of(boolean hard) { return hard ? HARD : NORMAL; }

    /** @return l'étage {@code floor} (à partir de 1) du mode sans fin. */
    public static Difficulty endless(int floor) {
        if (floor < 1) throw new IllegalArgumentException("Étage du mode sans fin : " + floor);
        return new Difficulty(true, floor);
    }

    /** @return {@code true} dans le mode sans fin. */
    public boolean isEndless() { return floor > 0; }
}
