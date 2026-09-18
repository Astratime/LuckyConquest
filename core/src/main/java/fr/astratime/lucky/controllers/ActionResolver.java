package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolRegistry;
import fr.astratime.lucky.entities.actions.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * Transforme un tableau de symboles en liste d'actions à exécuter.
 * Chaque symbole produit une action indépendante via SymbolRegistry.
 * Les symboles sans action enregistrée sont ignorés silencieusement.
 * Un symbole apparaissant N fois produit N actions distinctes,
 * ce qui permet à chaque action de recevoir les bonus de combat séparément.
 */
public class ActionResolver {

    /**
     * Convertit chaque symbole tiré en son action associée (voir {@link SymbolRegistry}).
     *
     * @param symbols résultat du spin (peut contenir des {@code null})
     * @return la liste des actions à résoudre, dans l'ordre des symboles
     */
    public List<Action> resolve(Symbol[] symbols) {
        List<Action> actions = new ArrayList<>();
        for (Symbol symbol : symbols) {
            if (symbol != null) {
                SymbolRegistry.getAction(symbol).ifPresent(actions::add);
            }
        }
        return actions;
    }
}
