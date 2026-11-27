package fr.astratime.lucky.entities;

import java.util.Random;

/**
 * Le hasard des règles du jeu (mélanges, tirages, effets des cartes, ennemis).
 *
 * En jeu, rien ne change : c'est un hasard ordinaire. Le simulateur peut fixer une graine pour le fil
 * qui joue un lot de combats ({@link #seed(long)}) : avec la même graine, le même lot donne alors
 * exactement les mêmes combats, et un « avant / après » ne mesure plus que l'effet du réglage.
 * Les animations et les écrans gardent leur propre hasard, il ne touche à aucune règle.
 */
public final class GameRandom {

    private static final Random              SHARED = new Random();
    private static final ThreadLocal<Random> SEEDED = new ThreadLocal<>();

    private GameRandom() { }

    /** Fixe le hasard du fil courant : tout ce qu'il joue ensuite se répète à l'identique. */
    public static void seed(long seed) { SEEDED.set(new Random(seed)); }

    /** Rend au fil courant le hasard ordinaire. */
    public static void clear() { SEEDED.remove(); }

    /** @return le hasard à graine du fil courant, ou {@code null} s'il n'en a pas (à rendre avec {@link #restore}). */
    public static Random seeded() { return SEEDED.get(); }

    /** Remet au fil courant le hasard {@code seeded} lu par {@link #seeded()} ({@code null} : hasard ordinaire). */
    public static void restore(Random seeded) {
        if (seeded == null) SEEDED.remove(); else SEEDED.set(seeded);
    }

    /** @return le hasard à utiliser maintenant (celui de la graine du fil s'il en a une). */
    public static Random get() {
        Random seeded = SEEDED.get();
        return seeded != null ? seeded : SHARED;
    }

    /**
     * @return un générateur à garder dans un objet (machine, effet de carte, ennemi) : chaque tirage passe
     *         par {@link #get()} au moment où il est fait, donc par la graine du fil qui joue à ce moment-là
     */
    public static Random live() { return LIVE; }

    private static final Random LIVE = new Random() {
        @Override
        protected int next(int bits) { return get().nextInt() >>> (32 - bits); }
    };
}
