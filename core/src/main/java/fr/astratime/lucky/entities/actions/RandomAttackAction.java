package fr.astratime.lucky.entities.actions;

import java.util.Random;

/** Dé : inflige des dégâts de base tirés au hasard entre {@code min} et {@code max}, puis comme une attaque. */
public class RandomAttackAction extends AttackAction {

    private final int    min;
    private final int    max;
    private final Random random;

    /**
     * @param min dégâts de base au plus bas
     * @param max dégâts de base au plus haut (compris)
     */
    public RandomAttackAction(int min, int max) { this(min, max, new Random()); }

    /** @param random source d'aléatoire (ex : graine fixe pour des tests reproductibles) */
    public RandomAttackAction(int min, int max, Random random) {
        super(min);
        this.min    = min;
        this.max    = max;
        this.random = random;
    }

    @Override
    protected int rollBaseDamage() { return min + random.nextInt(max - min + 1); }

    @Override
    public String getDescription() { return "De " + min + " à " + max + " dégâts de base, au hasard"; }
}
