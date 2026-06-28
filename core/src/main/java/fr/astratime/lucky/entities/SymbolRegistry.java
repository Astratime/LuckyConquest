package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.actions.Action;
import fr.astratime.lucky.entities.actions.AttackAction;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Associe chaque Symbol à son Action.
 * Point central où est défini ce que fait un symbole en combat.
 * Ajouter un effet à un symbole = ajouter une ligne dans le bloc statique.
 */
public class SymbolRegistry {

    private static final Map<Symbol, Action> ACTIONS = new EnumMap<>(Symbol.class);

    static {
        Action baseAttack = new AttackAction(10);
        ACTIONS.put(Symbol.DOUBLE_BAR, baseAttack);
        ACTIONS.put(Symbol.CHERRY,     baseAttack);
        ACTIONS.put(Symbol.SEVEN,      baseAttack);
        ACTIONS.put(Symbol.BAR,        baseAttack);
        // GRAPE, BELL, DIAMOND, TRIPLE_CHERRY, TRIPLE_SEVEN, GOLD_BAR, WATERMELON :
        // pas d'action pour l'instant — à définir au fil du design
    }

    public static Optional<Action> getAction(Symbol symbol) {
        return Optional.ofNullable(ACTIONS.get(symbol));
    }

    private SymbolRegistry() {}
}
