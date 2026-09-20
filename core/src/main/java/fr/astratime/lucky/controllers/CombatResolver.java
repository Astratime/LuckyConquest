package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolOutcome;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.events.DamageReflectedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GainsEarnedEvent;
import fr.astratime.lucky.entities.events.JackpotEvent;
import fr.astratime.lucky.entities.events.PlayerDamagedEvent;

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

    /** Gains accordés quand deux des trois symboles tirés sont identiques. */
    private static final int GAINS_PAIR    = 500;
    /** Gains accordés quand les trois symboles tirés sont identiques (jackpot). */
    private static final int GAINS_JACKPOT = 2000;

    /**
     * Résout un tour de combat complet à partir des actions déjà déterminées
     * par les symboles tirés : exécute chaque action, applique le bonus de
     * paire/jackpot, puis fait riposter l'ennemi s'il a survécu.
     *
     * @param combatContext contexte de combat (joueur, ennemi, modificateurs des cartes)
     * @param symbolActions couples symbole/action à résoudre, dans l'ordre des symboles
     * @param symbols       symboles tirés ce tour (utilisés pour le bonus de paire/jackpot)
     * @param priorEvents   événements déjà survenus en phase 1 (ex : symbole boosté par une
     *                      carte), à faire figurer en tête du journal du tour
     * @param nextDrawCount nombre de cartes à piocher au prochain tour (voir TurnContext),
     *                      simplement reporté tel quel dans le TurnResult
     * @return le journal d'événements du tour, les symboles, le détail par symbole et les gains de paire/jackpot
     */
    public TurnResult resolve(CombatContext      combatContext,
                              List<SymbolAction> symbolActions,
                              Symbol[]           symbols,
                              List<Event>        priorEvents,
                              int                nextDrawCount) {
        List<Event> events = new ArrayList<>(priorEvents);

        // Chaque action résout elle-même sa logique et retourne ses événements ;
        // on les rattache aussi au symbole qui les a produits pour le journal détaillé.
        List<SymbolOutcome> symbolOutcomes = new ArrayList<>(symbolActions.size());
        for (SymbolAction symbolAction : symbolActions) {
            List<Event> actionEvents = symbolAction.getAction().resolve(combatContext);
            events.addAll(actionEvents);
            symbolOutcomes.add(new SymbolOutcome(symbolAction.getSymbol(), actionEvents));
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

        // Riposte de l'ennemi : s'il a survécu au tour du joueur, il attaque à son tour.
        // Le bouclier accumulé par le joueur ce tour absorbe une partie des dégâts
        // (voir Player.takeDamage) ; l'événement reporte les dégâts réellement subis.
        // Le renvoi de dégâts (Carreau) reflète un pourcentage de l'attaque brute de
        // l'ennemi, indépendamment de ce que le bouclier en a absorbé.
        Enemy enemy = combatContext.getEnemy();
        if (!enemy.isDefeated()) {
            Player player       = combatContext.getPlayer();
            int    attackPower  = enemy.getAttackPower();
            int    actualDamage = player.takeDamage(attackPower);
            events.add(new PlayerDamagedEvent(actualDamage));

            int reflectPercent = player.getReflectPercent();
            if (reflectPercent > 0) {
                int reflectedDamage = Math.round(attackPower * (reflectPercent / 100f));
                if (reflectedDamage > 0) {
                    enemy.takeDamage(reflectedDamage);
                    events.add(new DamageReflectedEvent(reflectedDamage));
                }
            }
        }

        // Le bouclier (et le renvoi de dégâts) ne vaut que pour ce tour.
        combatContext.getPlayer().resetTurnDefenses();

        return new TurnResult(events, symbols, gains, nextDrawCount, symbolOutcomes);
    }

    /** @return {@code true} si les trois symboles sont identiques et non nuls. */
    private boolean isJackpot(Symbol[] s) {
        return s[0] != null && s[0] == s[1] && s[1] == s[2];
    }

    /** @return {@code true} si au moins deux des trois symboles sont identiques et non nuls. */
    private boolean hasPair(Symbol[] s) {
        if (s[0] == null) return false;
        return s[0] == s[1] || s[1] == s[2] || s[0] == s[2];
    }
}
