package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.*;
import fr.astratime.lucky.entities.actions.Action;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.effects.Effect;

import java.util.List;

/**
 * Orchestre un tour complet en séquençant les phases :
 * Phase 1 : PreparationResolver applique les effets → TurnContext
 * Phase 2 : SlotMachine tire les symboles via SpinContext
 *           ActionResolver convertit les symboles en actions
 *           CombatResolver exécute les actions (et crédite les gains au joueur)
 *
 * TurnEngine est quasi-stateless : il ne possède que ses sous-résolveurs.
 * Les gains/dégâts sont déjà appliqués par CombatResolver via CombatContext —
 * TurnEngine n'a donc plus besoin de toucher au GameState pour ça.
 */
public class TurnEngine {

    private final PreparationResolver preparationResolver = new PreparationResolver();
    private final ActionResolver      actionResolver      = new ActionResolver();
    private final CombatResolver      combatResolver      = new CombatResolver();

    /**
     * Joue un tour complet : applique les effets en attente, lance la machine
     * à sous, résout le combat, puis avance le compteur de tour de l'état de jeu.
     *
     * @param gameState      état de la partie (joueur, ennemi, numéro de tour)
     * @param pendingEffects effets des cartes jouées par le joueur depuis le début du tour
     * @return le journal d'événements et le résultat du spin pour ce tour
     */
    public TurnResult playTurn(GameState gameState, List<Effect> pendingEffects) {

        // Phase 1 : effets des cartes → TurnContext
        TurnContext turnContext = preparationResolver.resolve(
            pendingEffects,
            gameState.getPlayer(),
            gameState.getEnemy()
        );

        // Phase 2 : spin avec SpinContext
        Symbol[] symbols = gameState.getPlayer()
            .getSlotMachine()
            .spin(turnContext.getSpinContext());

        // Symboles -> actions
        List<Action> actions = actionResolver.resolve(symbols);

        // Combat : actions + CombatContext → TurnResult (mute déjà Player/Enemy)
        // Les événements de phase 1 (ex : symbole boosté par une carte) sont
        // fusionnés en tête du journal du tour.
        TurnResult result = combatResolver.resolve(
            turnContext.getCombatContext(),
            actions,
            symbols,
            turnContext.getEvents()
        );

        gameState.nextTurn();

        return result;
    }
}
