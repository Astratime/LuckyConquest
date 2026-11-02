package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.i18n.Lang;

import java.util.Random;

/** Ticket : des gains de base tirés au hasard entre {@code min} et {@code max}. */
public final class RandomGainAction extends GainAction {

    private final int    min;
    private final int    max;
    private final Random random;

    public RandomGainAction(int min, int max) { this(min, max, new Random()); }

    /** @param random source d'aléatoire (ex : graine fixe pour des tests reproductibles) */
    public RandomGainAction(int min, int max, Random random) {
        super(min);
        this.min    = min;
        this.max    = max;
        this.random = random;
    }

    @Override
    protected int rollBaseGain(CombatContext context) { return min + random.nextInt(max - min + 1); }

    @Override
    public String getDescription() { return Lang.f("De {0} à {1} gains de base, au hasard", min, max); }
}
