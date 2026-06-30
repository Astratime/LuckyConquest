package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.actions.Action;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GainsEarnedEvent;
import fr.astratime.lucky.entities.events.JackpotEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Résout le combat d'un tour : exécute les actions, calcule les gains de
 * paire/jackpot, construit le journal d'événements et retourne un TurnResult.
 * C'est le seul endroit qui modifie l'état du combat (HP ennemi, gains du joueur).
 * Ne reçoit que CombatContext et List<Action> — pas le GameState entier.
 * Les gains sont crédités directement au joueur via CombatContext.getPlayer(),
 * qui est la seule porte d'accès autorisée à l'état du joueur depuis ce niveau.
 */
public class CombatResolver {

    private static final int GAINS_PAIR    = 500;
    private static final int GAINS_JACKPOT = 2000;

    public TurnResult resolve(CombatContext combatContext,
                              List<Action>  actions,
                              Symbol[]      symbols) {
        List<Event> events = new ArrayList<>();

        // Chaque action résout elle-même sa logique et retourne ses événements
        for (Action action : actions) {
            events.addAll(action.resolve(combatContext));
        }

        // Bonus de paire/jackpot
        int gains = 0;
        if (isJackpot(symbols)) {
            gains = GAINS_JACKPOT;
            events.add(new JackpotEvent());
        } else if (hasPair(symbols)) {
            gains = GAINS_PAIR;
        }
        if (gains > 0) {
            combatContext.getPlayer().addGains(gains);
            events.add(new GainsEarnedEvent(gains));
        }

        return new TurnResult(events, symbols, gains);
    }

    private boolean isJackpot(Symbol[] s) {
        return s[0] != null && s[0] == s[1] && s[1] == s[2];
    }

    private boolean hasPair(Symbol[] s) {
        if (s[0] == null) return false;
        return s[0] == s[1] || s[1] == s[2] || s[0] == s[2];
    }
}
