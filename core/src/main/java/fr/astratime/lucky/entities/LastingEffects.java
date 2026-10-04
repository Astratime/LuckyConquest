package fr.astratime.lucky.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Effets de cartes qui durent plus d'un tour, pour le combat en cours,
 * affichés dans le panneau latéral :
 *  - symboles retirés des rouleaux (Recyclage, pour quelques tours) ;
 *  - bonus de gains (Porte-bonheur, jusqu'à la fin du combat) ;
 *  - Corruption (attaque et défense des symboles décuplées, quelques tours) ;
 *  - Dans la manche (plus de cartes jouables par tour, quelques tours) ;
 *  - Coffres-forts (gains mis de côté, rendus doublés quelques tours plus tard) ;
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
    /** Dans la manche : cartes jouables par tour tant qu'il reste des tours d'effet. */
    private int extraPlays = 0;
    /** Tours restants sous l'effet de Dans la manche (0 : inactif). */
    private int extraPlaysTurns = 0;
    /** Lames (Pique) : chacune ajoute de l'attaque à tous les symboles. */
    private int blades = 0;
    /** Sang (Coeur) : soin reçu au-delà des PV max. */
    private int blood  = 0;
    /** Coffre (Carreau) : bouclier resté inutilisé à la fin des tours. */
    private int vault  = 0;
    /** Rouleau bloqué au prochain tirage par le Rouleau interdit de l'ennemi (-1 : aucun). */
    private int forbiddenReel = -1;
    /** Coffres-forts : gains mis de côté (déjà doublés) et tours restants avant leur ouverture. */
    private final List<int[]> safes = new ArrayList<>();

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

    /**
     * Dans la manche : {@code plays} cartes jouables par tour pendant les
     * {@code turns} prochains tours, celui-ci compris (prolonge et garde la
     * meilleure limite si l'effet est déjà actif).
     */
    public void addExtraPlays(int plays, int turns) {
        extraPlays      = Math.max(extraPlays, plays);
        extraPlaysTurns = Math.max(extraPlaysTurns, turns);
    }

    /** @return les cartes jouables par tour sous Dans la manche, ou 0 s'il est inactif. */
    public int getExtraPlays() { return extraPlaysTurns > 0 ? extraPlays : 0; }

    /** @return les tours restants sous Dans la manche (0 s'il est inactif). */
    public int getExtraPlaysTurns() { return extraPlaysTurns; }

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

    /**
     * Retire {@code percent} % de chaque jauge (Lames, Sang, Coffre), arrondi au-dessus (Dé pipé de l'ennemi).
     *
     * @return le total retiré des trois jauges
     */
    public int drainGauges(int percent) {
        int lostBlades = (int) Math.ceil(blades * percent / 100.0);
        int lostBlood  = (int) Math.ceil(blood * percent / 100.0);
        int lostVault  = (int) Math.ceil(vault * percent / 100.0);
        blades -= lostBlades;
        blood  -= lostBlood;
        vault  -= lostVault;
        return lostBlades + lostBlood + lostVault;
    }

    /** Le Rouleau interdit de l'ennemi bloque le rouleau {@code reel} au prochain tirage. */
    public void forbidReel(int reel) { forbiddenReel = reel; }

    /** @return le rouleau bloqué au prochain tirage, ou -1 si aucun. */
    public int getForbiddenReel() { return forbiddenReel; }

    /** Tirage : le rouleau bloqué l'est pour ce tirage, puis se libère. @return ce rouleau, ou -1 */
    public int takeForbiddenReel() {
        int reel = forbiddenReel;
        forbiddenReel = -1;
        return reel;
    }

    /** Coffre-fort : {@code amount} gains reviennent à la fin du {@code turns}-ième tour, celui-ci compris. */
    public void addSafe(int amount, int turns) { safes.add(new int[] {amount, turns}); }

    /** @return les Coffres-forts fermés : pour chacun, ses gains et ses tours restants. */
    public List<int[]> getSafes() {
        List<int[]> copy = new ArrayList<>();
        safes.forEach(safe -> copy.add(safe.clone()));
        return copy;
    }

    /**
     * Fin du tour, avant {@link #endTurn()} : ouvre les Coffres-forts arrivés à
     * leur dernier tour, ou tous si {@code all} (ennemi vaincu).
     *
     * @return les gains rendus
     */
    public int openSafes(boolean all) {
        int total = 0;
        for (Iterator<int[]> it = safes.iterator(); it.hasNext(); ) {
            int[] safe = it.next();
            if (all || safe[1] <= 1) {
                total += safe[0];
                it.remove();
            }
        }
        return total;
    }

    /** Fin d'un tirage : chaque symbole retiré se rapproche de son retour, la Corruption et Dans la manche de leur fin. */
    public void endTurn() {
        removedSymbols.replaceAll((symbol, turns) -> turns - 1);
        removedSymbols.values().removeIf(turns -> turns <= 0);
        if (corruptionTurns > 0) corruptionTurns--;
        if (extraPlaysTurns > 0 && --extraPlaysTurns == 0) extraPlays = 0;
        safes.forEach(safe -> safe[1]--);
    }

    /** @return {@code true} si aucun effet n'est actif et les jauges sont vides. */
    public boolean isEmpty() {
        return removedSymbols.isEmpty() && gainBonus == 0f && corruptionTurns == 0 && extraPlaysTurns == 0
            && blades == 0 && blood == 0 && vault == 0 && safes.isEmpty();
    }
}
