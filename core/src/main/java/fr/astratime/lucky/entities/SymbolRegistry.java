package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.actions.AttackAction;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Point central qui associe chaque Symbol à son SymbolAction.
 * C'est ici — et nulle part ailleurs — que l'on définit ce que fait un symbole.
 * Ajouter un symbole avec un effet = ajouter une ligne dans le bloc statique.
 */
public class SymbolRegistry {

    private static final Map<Symbol, SymbolAction> ACTIONS = new EnumMap<>(Symbol.class);

    static {
        AttackAction baseAttack = new AttackAction(10);
        ACTIONS.put(Symbol.DOUBLE_BAR, baseAttack);
        ACTIONS.put(Symbol.CHERRY,     baseAttack);
        ACTIONS.put(Symbol.SEVEN,      baseAttack);
        ACTIONS.put(Symbol.BAR,        baseAttack);
        // GRAPE, BELL, DIAMOND, TRIPLE_CHERRY, TRIPLE_SEVEN, GOLD_BAR, WATERMELON :
        // aucune action pour l'instant — à définir au fil du design
    }

    public static Optional<SymbolAction> getAction(Symbol symbol) {
        return Optional.ofNullable(ACTIONS.get(symbol));
    }

    private SymbolRegistry() {}
}
