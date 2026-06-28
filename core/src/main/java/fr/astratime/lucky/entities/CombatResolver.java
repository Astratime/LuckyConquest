package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.actions.Action;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.JackpotEvent;
import fr.astratime.lucky.entities.events.ScoreGainedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Résout le combat d'un tour : exécute les actions, calcule le score,
 * construit le journal d'événements et retourne un TurnResult.
 * C'est le seul endroit qui modifie l'état du combat (HP ennemis, score).
 * Ne reçoit que CombatContext et List<Action> — pas le GameState entier.
 */
public class CombatResolver {

    private static final int SCORE_PAIR    = 10;
    private static final int SCORE_JACKPOT = 50;

    public TurnResult resolve(CombatContext combatContext,
                              List<Action>  actions,
                              Symbol[]      symbols) {
        List<Event> events = new ArrayList<>();

        // Chaque action résout elle-même sa logique et retourne ses événements
        for (Action action : actions) {
            events.addAll(action.resolve(combatContext));
        }

        // Scoring
        int score = 0;
        if (isJackpot(symbols)) {
            score = SCORE_JACKPOT;
            events.add(new JackpotEvent());
        } else if (hasPair(symbols)) {
            score = SCORE_PAIR;
        }
        if (score > 0) {
            events.add(new ScoreGainedEvent(score));
        }

        return new TurnResult(events, symbols, score);
    }

    private boolean isJackpot(Symbol[] s) {
        return s[0] != null && s[0] == s[1] && s[1] == s[2];
    }

    private boolean hasPair(Symbol[] s) {
        if (s[0] == null) return false;
        return s[0] == s[1] || s[1] == s[2] || s[0] == s[2];
    }
}
