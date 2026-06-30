package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.actions.Action;
import fr.astratime.lucky.entities.actions.AttackAction;
import fr.astratime.lucky.entities.actions.DefenseAction;
import fr.astratime.lucky.entities.actions.GainAction;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Associe chaque Symbol à son Action.
 * Point central où est défini ce que fait un symbole en combat.
 * Ajouter un effet à un symbole = ajouter une ligne dans le bloc statique.
 *
 * Les 4 symboles boostés par les suites (DIAMOND, TRIPLE_CHERRY, TRIPLE_SEVEN,
 * GOLD_BAR — voir Deck.createCard) ont une action plus forte que la normale,
 * cohérente avec le thème de leur suite respective.
 */
public class SymbolRegistry {

    private static final Map<Symbol, Action> ACTIONS = new EnumMap<>(Symbol.class);

    static {
        // Symboles d'attaque de base
        ACTIONS.put(Symbol.DOUBLE_BAR, new AttackAction(20));
        ACTIONS.put(Symbol.CHERRY,     new AttackAction(15));
        ACTIONS.put(Symbol.SEVEN,      new AttackAction(30));
        ACTIONS.put(Symbol.BAR,        new AttackAction(10));

        // Symboles de défense / gains de base
        ACTIONS.put(Symbol.GRAPE,      new DefenseAction(8));
        ACTIONS.put(Symbol.BELL,       new GainAction(8));
        ACTIONS.put(Symbol.WATERMELON, new GainAction(12));

        // Symboles rares liés aux suites (thème renforcé)
        ACTIONS.put(Symbol.DIAMOND,       new DefenseAction(20)); // theme Carreau : defense/reflect
        ACTIONS.put(Symbol.TRIPLE_CHERRY, new AttackAction(45));  // theme Coeur : attaque + drain
        ACTIONS.put(Symbol.TRIPLE_SEVEN,  new AttackAction(90));  // theme Pique : attaque perçante
        ACTIONS.put(Symbol.GOLD_BAR,      new GainAction(25));    // theme Trefle : gains
    }

    public static Optional<Action> getAction(Symbol symbol) {
        return Optional.ofNullable(ACTIONS.get(symbol));
    }

    /** Symboles dont l'action est une attaque — utilisé par les effets "boost aléatoire" (As). */
    public static List<Symbol> getAttackSymbols() {
        List<Symbol> result = new ArrayList<>();
        for (Map.Entry<Symbol, Action> entry : ACTIONS.entrySet()) {
            if (entry.getValue() instanceof AttackAction) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    private SymbolRegistry() {}
}
