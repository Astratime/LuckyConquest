package fr.astratime.lucky.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Ce que retiennent, le temps d'un combat, les trois archétypes du Valet à l'As
 * (choix d'Astra, 2026-11-08) :
 *  - Cour royale : le Sceptre du roi, qui garde la moitié du multiplicateur des
 *    combinaisons pour l'attaque du tirage suivant ;
 *  - Revenant : les cartes jouées au tour d'avant (le Roi des ombres rejoue la dernière) ;
 *  - Horloger : les Ressorts (attaque à chaque tirage, vidés par les Douze coups),
 *    les Coucous qui chantent quelques tours plus tard et la Grande aiguille.
 */
public final class CourtAndClock {

    /** Ressorts posés au plus. */
    public static final int MAX_SPRINGS = 10;

    /** Sceptre du roi : multiplicateur d'attaque du prochain tirage (1 : aucun). */
    private float sceptreFactor = 1f;
    /** Cartes jouées au tour d'avant, dans l'ordre où elles ont été jouées. */
    private final List<Card> lastPlayed = new ArrayList<>();
    private int springs;
    /** Attaque en plus par Ressort, en % (celle de la carte la plus forte jouée). */
    private int springPercent;
    /** Coucous : pour chacun, tirages avant son chant et son multiplicateur d'attaque. */
    private final List<float[]> cuckoos = new ArrayList<>();
    /** Grande aiguille : tours restants et Ressorts ajoutés à chacun. */
    private int minuteTurns;
    private int minuteSprings;

    // ----- Cour royale -----

    /** Sceptre du roi : l'attaque du prochain tirage sera multipliée par {@code factor} (le plus fort reste). */
    public void setSceptre(float factor) { sceptreFactor = Math.max(sceptreFactor, factor); }

    /** @return le multiplicateur du Sceptre attendu au prochain tirage (1 : aucun). */
    public float getSceptre() { return sceptreFactor; }

    /** Début du tirage : le Sceptre sert une fois. @return son multiplicateur (1 : aucun) */
    public float takeSceptre() {
        float factor = sceptreFactor;
        sceptreFactor = 1f;
        return factor;
    }

    // ----- Revenant -----

    /** Fin du tour : retient les cartes jouées, pour le Roi des ombres du tour suivant. */
    public void rememberPlayed(List<Card> played) {
        lastPlayed.clear();
        lastPlayed.addAll(played);
    }

    /** @return les cartes jouées au tour d'avant, dans l'ordre (vide au premier tour). */
    public List<Card> getLastPlayed() { return List.copyOf(lastPlayed); }

    // ----- Horloger -----

    /**
     * Pose {@code count} Ressorts ({@link #MAX_SPRINGS} au plus), qui donnent chacun
     * {@code percent} % d'attaque à chaque tirage.
     *
     * @return les Ressorts vraiment posés
     */
    public int addSprings(int count, int percent) {
        int before = springs;
        springs = Math.min(MAX_SPRINGS, springs + Math.max(0, count));
        springPercent = Math.max(springPercent, percent);
        return springs - before;
    }

    /** @return les Ressorts posés. */
    public int getSprings() { return springs; }

    /** @return l'attaque en plus des Ressorts à chaque tirage, en %. */
    public int getSpringAttack() { return springs * springPercent; }

    /** Douze coups : vide les Ressorts. @return ceux qui étaient posés */
    public int takeSprings() {
        int taken = springs;
        springs = 0;
        return taken;
    }

    /** Coucou : dans {@code turns} tirages, l'attaque de ce tirage-là sera multipliée par {@code factor}. */
    public void addCuckoo(int turns, float factor) { cuckoos.add(new float[] {turns, factor}); }

    /** @return les tirages avant le prochain chant d'un Coucou (0 : aucun Coucou). */
    public int getNextCuckoo() {
        int next = 0;
        for (float[] cuckoo : cuckoos) if (next == 0 || cuckoo[0] < next) next = (int) cuckoo[0];
        return next;
    }

    /**
     * Début du tirage : chaque Coucou se rapproche de son chant ; ceux qui
     * chantent maintenant multiplient l'attaque de ce tirage.
     *
     * @return le multiplicateur d'attaque des Coucous qui chantent (1 : aucun)
     */
    public float tickCuckoos() {
        float factor = 1f;
        for (float[] cuckoo : cuckoos) {
            if (--cuckoo[0] <= 0) factor *= cuckoo[1];
        }
        cuckoos.removeIf(cuckoo -> cuckoo[0] <= 0);
        return factor;
    }

    /** Grande aiguille : pendant {@code turns} tours, {@code springs} Ressort(s) en plus au début du tour. */
    public void addMinuteHand(int turns, int springs, int percent) {
        minuteTurns   = Math.max(minuteTurns, turns);
        minuteSprings = Math.max(minuteSprings, springs);
        springPercent = Math.max(springPercent, percent);
    }

    /** @return les tours restants de la Grande aiguille (0 : inactive). */
    public int getMinuteTurns() { return minuteTurns; }

    /** Début du tirage : la Grande aiguille avance. @return les Ressorts posés (0 sans Grande aiguille) */
    public int tickMinuteHand() {
        if (minuteTurns <= 0) return 0;
        minuteTurns--;
        int added = addSprings(minuteSprings, springPercent);
        if (minuteTurns == 0) minuteSprings = 0;
        return added;
    }

    /** @return {@code true} si rien n'est en cours (rien à montrer dans le panneau des effets). */
    public boolean isEmpty() {
        return sceptreFactor == 1f && springs == 0 && cuckoos.isEmpty() && minuteTurns == 0;
    }
}
