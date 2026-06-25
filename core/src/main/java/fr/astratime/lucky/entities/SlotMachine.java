package fr.astratime.lucky.entities;

import java.util.Random;

/**
 * Représente la machine à sous du joueur.
 * Chaque appel à spin() tire 3 symboles indépendamment au hasard
 * parmi tous les symboles disponibles.
 */
public class SlotMachine {

    private static final Symbol[] SYMBOLS = Symbol.values();

    private final Random  random = new Random();
    private       Symbol[] result = new Symbol[3];

    public void spin() {
        for (int i = 0; i < result.length; i++) {
            result[i] = SYMBOLS[random.nextInt(SYMBOLS.length)];
        }
    }

    public Symbol[] getResult() {
        return result.clone();
    }

    /** Retourne true si les 3 rouleaux affichent le même symbole (jackpot). */
    public boolean isJackpot() {
        return result[0] != null
            && result[0] == result[1]
            && result[1] == result[2];
    }
}
