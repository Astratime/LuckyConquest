package fr.astratime.lucky.entities;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Effets de cartes qui durent plus d'un tour, pour le combat en cours,
 * affichés dans le panneau latéral :
 *  - symboles retirés des rouleaux (Recyclage, pour quelques tours) ;
 *  - bonus de gains (Porte-bonheur, jusqu'à la fin du combat) ;
 *  - Corruption (attaque et défense des symboles décuplées, quelques tours) ;
 *  - les jauges des couleurs, remplies au fil du combat et vidées par leur As :
 *    Lames (Pique), Sang (Coeur) et Coffre (Carreau).
 */
public class LastingEffects {

    /** Symboles retirés des rouleaux, avec le nombre de tirages restants avant leur retour. */
    private final Map<Symbol, Integer> removedSymbols = new EnumMap<>(Symbol.class);
    /** Bonus de gains pour tout le combat (0.5 = +50 %), cumulé sur les Porte-bonheur joués. */
    private float gainBonus = 0f;
    /** Tirages restants sous l'effet de la Corruption (0 : inactive). */
    private int corruptionTurns = 0;
    /** Lames (Pique) : chacune ajoute de l'attaque à tous les symboles. */
    private int blades = 0;
    /** Sang (Coeur) : soin reçu au-delà des PV max. */
    private int blood  = 0;
    /** Coffre (Carreau) : bouclier resté inutilisé à la fin des tours. */
    private int vault  = 0;

    /** Retire {@code symbol} des rouleaux pour les {@code turns} prochains tirages (prolonge s'il l'est déjà). */
    public void removeSymbol(Symbol symbol, int turns) {
        removedSymbols.merge(symbol, turns, Math::max);
    }

    /** @return les symboles retirés et leurs tirages restants (vue non modifiable, dans l'ordre des symboles). */
    public Map<Symbol, Integer> getRemovedSymbols() {
        return Collections.unmodifiableMap(removedSymbols);
    }

    /** Ajoute {@code bonus} au bonus de gains du combat (0.5 = +50 %). */
    public void addGainBonus(float bonus) { gainBonus += bonus; }

    /** @return le bonus de gains du combat (0 si aucun). */
    public float getGainBonus() { return gainBonus; }

    /** Active la Corruption pour les {@code turns} prochains tirages (prolonge si elle l'est déjà). */
    public void addCorruption(int turns) { corruptionTurns = Math.max(corruptionTurns, turns); }

    /** @return les tirages restants sous l'effet de la Corruption (0 si inactive). */
    public int getCorruptionTurns() { return corruptionTurns; }

    /** Ajoute {@code count} Lames. */
    public void addBlades(int count) { blades += count; }
    /** @return le nombre de Lames. */
    public int getBlades() { return blades; }
    /** Vide les Lames. @return leur nombre avant d'être vidées */
    public int consumeBlades() { int n = blades; blades = 0; return n; }

    /** Ajoute {@code amount} au Sang. */
    public void addBlood(int amount) { blood += amount; }
    /** @return la réserve de Sang. */
    public int getBlood() { return blood; }
    /** Vide le Sang. @return la réserve avant d'être vidée */
    public int consumeBlood() { int n = blood; blood = 0; return n; }

    /** Ajoute {@code amount} au Coffre. */
    public void addVault(int amount) { vault += amount; }
    /** @return le contenu du Coffre. */
    public int getVault() { return vault; }
    /** Vide le Coffre. @return son contenu avant d'être vidé */
    public int consumeVault() { int n = vault; vault = 0; return n; }

    /** Multiplie les trois jauges (Lames, Sang, Coffre) par {@code factor} (ex : Pot de Lutin). */
    public void multiplyGauges(int factor) {
        blades *= factor;
        blood  *= factor;
        vault  *= factor;
    }

    /** Fin d'un tirage : chaque symbole retiré se rapproche de son retour, la Corruption aussi de sa fin. */
    public void endTurn() {
        removedSymbols.replaceAll((symbol, turns) -> turns - 1);
        removedSymbols.values().removeIf(turns -> turns <= 0);
        if (corruptionTurns > 0) corruptionTurns--;
    }

    /** @return {@code true} si aucun effet n'est actif et les jauges sont vides. */
    public boolean isEmpty() {
        return removedSymbols.isEmpty() && gainBonus == 0f && corruptionTurns == 0
            && blades == 0 && blood == 0 && vault == 0;
    }
}
