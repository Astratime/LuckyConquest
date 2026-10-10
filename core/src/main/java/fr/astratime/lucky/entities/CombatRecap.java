package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.JackpotEvent;
import fr.astratime.lucky.entities.events.PlayerDamagedEvent;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Récapitulatif d'un combat, affiché au-dessus des boutons de fin : tours
 * joués, dégâts infligés et subis, plus gros coup, Bingos et cartes jouées.
 * Remis à zéro au début de chaque combat.
 */
public class CombatRecap {

    private int  turns;
    private long damageDealt;
    private long bestHit;
    private long damageTaken;
    private int  bingos;
    private int  cardsPlayed;

    /** Nouveau combat : tout repart de zéro. */
    public void reset() {
        turns = 0;
        damageDealt = 0L;
        bestHit = 0L;
        damageTaken = 0L;
        bingos = 0;
        cardsPlayed = 0;
    }

    /** Une carte de la main a été jouée. */
    public void cardPlayed() {
        cardsPlayed++;
    }

    /** Un tirage (le tour du joueur, puis celui de l'ennemi) : ses dégâts et son Bingo éventuel. */
    public void record(TurnResult result) {
        turns++;
        Set<Event> events = Collections.newSetFromMap(new IdentityHashMap<>());
        events.addAll(result.getEvents());
        events.addAll(result.getCardEvents());
        boolean bingo = false;
        for (Event event : events) {
            if (event instanceof EnemyDamagedEvent hit && hit.damage > 0) {
                damageDealt = add(damageDealt, hit.damage);
                bestHit = Math.max(bestHit, hit.damage);
            }
            if (event instanceof PlayerDamagedEvent hurt && hurt.damage > 0) damageTaken = add(damageTaken, hurt.damage);
            if (event instanceof JackpotEvent) bingo = true;
        }
        if (bingo) bingos++;
    }

    /** Somme bloquée au plus grand {@code long} (les dégâts peuvent devenir immenses). */
    private static long add(long total, long amount) {
        long sum = total + amount;
        return sum < total ? Long.MAX_VALUE : sum;
    }

    public int  getTurns()       { return turns; }
    public long getDamageDealt() { return damageDealt; }
    public long getBestHit()     { return bestHit; }
    public long getDamageTaken() { return damageTaken; }
    public int  getBingos()      { return bingos; }
    public int  getCardsPlayed() { return cardsPlayed; }
}
