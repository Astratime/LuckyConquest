package fr.astratime.lucky.entities;

import java.util.EnumMap;
import java.util.Map;

/**
 * Interprète le résultat du spin et applique les effets sur le GameState.
 * C'est ici que vit toute la logique de résolution d'un tour :
 * dégâts, score, jackpot, etc.
 * GameScreen ne fait qu'appeler resolve() et afficher le TurnResult retourné.
 */
public class CombatResolver {

    private static final int SCORE_PAIR    = 10;
    private static final int SCORE_JACKPOT = 50;

    public TurnResult resolve(GameState state) {

        // Récupération des symboles de la machine
        Symbol[] symbols = state.getPlayer().getSlotMachine().getResult();
        // nombre par symbole
        Map<Symbol, Integer> counts = countSymbols(symbols);

        // 1. Appliquer les actions de chaque symbole (dégâts bruts, sans bonus)
        int hpBefore = state.getEnemy().getHp();
        for (Map.Entry<Symbol, Integer> entry : counts.entrySet()) {
            SymbolRegistry.getAction(entry.getKey())
                .ifPresent(action -> action.apply(state, entry.getValue()));
        }
        int rawDamage = hpBefore - state.getEnemy().getHp();

        // 2. Ajouter le bonus d'attaque (cartes jouées) une seule fois
        int attackBonus = state.getTurnContext().getAttackBonus();
        if (rawDamage > 0 && attackBonus > 0) {
            state.getEnemy().takeDamage(attackBonus);
        }
        int totalDamage = hpBefore - state.getEnemy().getHp();

        // 3. Scoring
        boolean jackpot = isJackpot(symbols);
        boolean pair    = !jackpot && hasPair(symbols);
        int score = jackpot ? SCORE_JACKPOT : (pair ? SCORE_PAIR : 0);
        state.addScore(score);

        return new TurnResult(totalDamage, score, jackpot, pair);
    }

    private Map<Symbol, Integer> countSymbols(Symbol[] symbols) {
        Map<Symbol, Integer> counts = new EnumMap<>(Symbol.class);
        for (Symbol s : symbols) {
            if (s != null) counts.merge(s, 1, Integer::sum);
        }
        return counts;
    }

    private boolean isJackpot(Symbol[] s) {
        return s[0] != null && s[0] == s[1] && s[1] == s[2];
    }

    private boolean hasPair(Symbol[] s) {
        if (s[0] == null) return false;
        return s[0] == s[1] || s[1] == s[2] || s[0] == s[2];
    }
}
