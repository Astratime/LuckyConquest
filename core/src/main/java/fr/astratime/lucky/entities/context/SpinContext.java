package fr.astratime.lucky.entities.context;

import fr.astratime.lucky.entities.SlotMachine;
import fr.astratime.lucky.entities.Symbol;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
    /** Rouleaux bloqués (Rouleau interdit, rouleaux volés par la Machine Originelle) : ils ne tournent pas et ne donnent rien. */
    private final java.util.Set<Integer> blockedReels = new java.util.TreeSet<>();
    /** Nouvelle règle du Directeur des Jeux : pas de Bingo possible à ce tirage. */
    private boolean noBingo = false;
    /** Nouvelle règle du Directeur des Jeux : chaque rouleau tourne deux fois et garde le pire résultat. */
    private boolean doubleSpin = false;
    /** Le Jeu bonus s'ouvre à coup sûr si le tirage fait un Bingo (carte de test « Jeu bonus »). */
    private boolean bonusGame = false;
    /** Symboles imposés à certains rouleaux (Rouleau truqué, Rouleau fantôme), par indice de rouleau. */
    private final Map<Integer, Symbol> forcedReels = new TreeMap<>();
    /** Relances accordées si le tirage n'a pas de paire (Relance). */
    private int     rerolls = 0;
    /** Tournée générale : la machine tourne une fois de plus par carte, le meilleur tirage reste. */
    private int     bestOfTwo = 0;
    /** Rouleaux en plus des {@link SlotMachine#SYMBOL_COUNT} habituels (Machine en surchauffe). */
    private int     extraReels = 0;

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

    /** Bloque le rouleau {@code reel} pour ce tirage (Rouleau interdit, rouleau volé) ; -1 ne bloque rien. */
    public void blockReel(int reel) { if (reel >= 0) blockedReels.add(reel); }

    /** @return les rouleaux bloqués ce tirage. */
    public java.util.Set<Integer> getBlockedReels() { return java.util.Collections.unmodifiableSet(blockedReels); }

    /** Nouvelle règle : ce tirage ne peut pas faire de Bingo. */
    public void forbidBingo() { noBingo = true; }

    /** @return {@code true} si ce tirage ne peut pas faire de Bingo. */
    public boolean isBingoForbidden() { return noBingo; }

    /** Nouvelle règle : chaque rouleau tourne deux fois et garde le pire résultat. */
    public void spinTwice() { doubleSpin = true; }

    /** @return {@code true} si chaque rouleau tourne deux fois ce tirage. */
    public boolean isDoubleSpin() { return doubleSpin; }

    /** Impose {@code symbol} au rouleau {@code reel} (le dernier imposé l'emporte). */
    public void forceReel(int reel, Symbol symbol) { forcedReels.put(reel, symbol); }

    /** @return les symboles imposés, par indice de rouleau (vue non modifiable). */
    public Map<Integer, Symbol> getForcedReels() { return Collections.unmodifiableMap(forcedReels); }

    /** Tournée générale : la machine tourne une fois de plus, le meilleur tirage reste. */
    public void addBestOfTwo() { bestOfTwo++; }

    /** @return les tirages en plus de la Tournée générale (le meilleur reste). */
    public int getBestOfTwo() { return bestOfTwo; }

    /** Accorde une relance si le tirage n'a pas de paire. */
    public void addReroll() { rerolls++; }

    /** @return les relances accordées ce tour. */
    public int getRerolls() { return rerolls; }

    /** Ajoute un rouleau au tirage de ce tour (au plus {@link SlotMachine#MAX_SYMBOL_COUNT}). */
    public void addExtraReel() { extraReels++; }

    /** @return le nombre de rouleaux qui tournent ce tour. */
    public int getReelCount() {
        return Math.min(SlotMachine.MAX_SYMBOL_COUNT,
            SlotMachine.SYMBOL_COUNT + extraReels);
    }

    /** @return {@code true} si le tirage de ce tour est un jackpot garanti. */
    public boolean isJackpotForced() { return forceJackpot; }

    /** Le Jeu bonus s'ouvrira à coup sûr après le Bingo de ce tirage. */
    public void forceBonusGame() { bonusGame = true; }

    /** @return {@code true} si le Jeu bonus s'ouvre à coup sûr après un Bingo. */
    public boolean isBonusGameForced() { return bonusGame; }
}
