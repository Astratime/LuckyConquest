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
        TurnResult result = combatResolver.resolve(
            turnContext.getCombatContext(),
            actions,
            symbols
        );

        gameState.nextTurn();

        return result;
    }
}
