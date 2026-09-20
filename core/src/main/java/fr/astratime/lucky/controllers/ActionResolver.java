package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Transforme un tableau de symboles en liste de couples (symbole, action) à résoudre.
 * Chaque symbole produit une action indépendante via SymbolRegistry.
 * Les symboles sans action enregistrée sont ignorés silencieusement.
 * Un symbole apparaissant N fois produit N couples distincts, ce qui permet
 * à chaque action de recevoir les bonus de combat séparément — et à
 * CombatResolver de rattacher les événements résultants au bon symbole.
 */
public class ActionResolver {

    /**
     * Convertit chaque symbole tiré en son action associée (voir {@link SymbolRegistry}).
     *
     * @param symbols résultat du spin (peut contenir des {@code null})
     * @return la liste des couples symbole/action à résoudre, dans l'ordre des symboles
     */
    public List<SymbolAction> resolve(Symbol[] symbols) {
        List<SymbolAction> symbolActions = new ArrayList<>();
        for (Symbol symbol : symbols) {
            if (symbol != null) {
                SymbolRegistry.getAction(symbol).ifPresent(action -> symbolActions.add(new SymbolAction(symbol, action)));
            }
        }
        return symbolActions;
    }
}
