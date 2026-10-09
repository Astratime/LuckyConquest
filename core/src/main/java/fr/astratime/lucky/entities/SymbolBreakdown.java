package fr.astratime.lucky.entities;

import java.util.List;

/**
 * Le calcul de ce qu'a fait un symbole tiré, étape par étape, affiché au
 * survol de son rouleau : un coup sur l'ennemi ({@link DamageBreakdown}), un
 * bouclier ou des gains ({@link BoostBreakdown}).
 */
public interface SymbolBreakdown {
    /** @return les lignes du calcul, la première pour {@code symbol}, la dernière pour le résultat. */
    List<String> lines(Symbol symbol);
}
