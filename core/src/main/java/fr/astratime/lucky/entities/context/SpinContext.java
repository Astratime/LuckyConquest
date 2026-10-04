package fr.astratime.lucky.entities.context;

import fr.astratime.lucky.entities.Symbol;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Modificateurs liés au spin pour le tour en cours.
 * Alimenté par les effets de cartes (ex: BoostSymbolEffect).
 * Transmis à SlotMachine.spin() — la machine ne connaît rien d'autre.
 */
public class SpinContext {

    private final Map<Symbol, Integer> weightBoosts   = new EnumMap<>(Symbol.class);
    /** Symboles retirés des rouleaux (Recyclage) : ils ne peuvent pas sortir ce tour. */
    private final Set<Symbol>          removedSymbols = EnumSet.noneOf(Symbol.class);
    /** Chance (0 à 1) de transformer un tirage sans paire en paire (Aimant). */
    private float   pairChance   = 0f;
    /** Les trois rouleaux affichent le même symbole (Bingo). */
    private boolean forceJackpot = false;
    /** Symbole imposé au jackpot garanti ; {@code null} : tiré au hasard. */
    private Symbol  jackpotSymbol;
    /** Rouleau bloqué (Rouleau interdit de l'ennemi) : il ne tourne pas et ne donne rien ; -1 : aucun. */
    private int     blockedReel = -1;

    /** Ajoute {@code amount} au boost de poids du symbole (cumulable sur plusieurs cartes). */
    public void addWeightBoost(Symbol symbol, int amount) {
        weightBoosts.merge(symbol, amount, Integer::sum);
    }

    /** @return le boost de poids accumulé pour ce symbole (0 si aucun). */
    public int getWeightBoost(Symbol symbol) {
        return weightBoosts.getOrDefault(symbol, 0);
    }

    /** Retire {@code symbol} des rouleaux pour ce tour. */
    public void removeSymbol(Symbol symbol) { removedSymbols.add(symbol); }

    /** @return {@code true} si {@code symbol} ne peut pas sortir ce tour. */
    public boolean isRemoved(Symbol symbol) { return removedSymbols.contains(symbol); }

    /** Ajoute {@code chance} à la chance de forcer une paire (plafonnée à 1). */
    public void addPairChance(float chance) { pairChance = Math.min(1f, pairChance + chance); }

    /** @return la chance (0 à 1) de transformer un tirage sans paire en paire. */
    public float getPairChance() { return pairChance; }

    /** Garantit que les trois rouleaux affichent le même symbole. */
    public void forceJackpot() { forceJackpot = true; }

    /** Garantit que les trois rouleaux affichent {@code symbol}. */
    public void forceJackpot(Symbol symbol) {
        forceJackpot  = true;
        jackpotSymbol = symbol;
    }

    /** @return le symbole imposé au jackpot garanti, ou {@code null} s'il est tiré au hasard. */
    public Symbol getJackpotSymbol() { return jackpotSymbol; }

    /** Bloque le rouleau {@code reel} pour ce tirage (Rouleau interdit). */
    public void blockReel(int reel) { blockedReel = reel; }

    /** @return le rouleau bloqué ce tirage, ou -1 si aucun. */
    public int getBlockedReel() { return blockedReel; }

    /** @return {@code true} si le tirage de ce tour est un jackpot garanti. */
    public boolean isJackpotForced() { return forceJackpot; }
}
