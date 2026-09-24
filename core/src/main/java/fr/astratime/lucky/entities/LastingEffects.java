package fr.astratime.lucky.entities;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Effets de cartes qui durent plus d'un tour, pour le combat en cours :
 * symboles retirés des rouleaux (Recyclage, pour quelques tours) et bonus de
 * gains (Porte-bonheur, jusqu'à la fin du combat). Affichés dans le panneau
 * latéral.
 */
public class LastingEffects {

    /** Symboles retirés des rouleaux, avec le nombre de tirages restants avant leur retour. */
    private final Map<Symbol, Integer> removedSymbols = new EnumMap<>(Symbol.class);
    /** Bonus de gains pour tout le combat (0.5 = +50 %), cumulé sur les Porte-bonheur joués. */
    private float gainBonus = 0f;

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

    /** Fin d'un tirage : chaque symbole retiré se rapproche de son retour. */
    public void endTurn() {
        removedSymbols.replaceAll((symbol, turns) -> turns - 1);
        removedSymbols.values().removeIf(turns -> turns <= 0);
    }

    /** @return {@code true} si aucun effet n'est actif. */
    public boolean isEmpty() { return removedSymbols.isEmpty() && gainBonus == 0f; }
}
