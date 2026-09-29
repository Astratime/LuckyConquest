package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.*;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GaugeFilledEvent;
import fr.astratime.lucky.entities.effects.Effect;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestre un tour complet en séquençant les phases :
 * Phase 1 : PreparationResolver applique les effets → TurnContext
 * Phase 2 : SlotMachine tire les symboles via SpinContext
 *           ActionResolver convertit les symboles en actions
 *           CombatResolver exécute les actions (et crédite les gains au joueur)
 * Phase 3 : EnemyTurnResolver joue le tour de l'ennemi (s'il a survécu)
 *
 * TurnEngine est quasi-stateless : il ne possède que ses sous-résolveurs.
 * Les gains/dégâts sont déjà appliqués par CombatResolver via CombatContext —
 * TurnEngine n'a donc plus besoin de toucher au GameState pour ça.
 */
public class TurnEngine {

    private final PreparationResolver preparationResolver = new PreparationResolver();
    private final ActionResolver      actionResolver      = new ActionResolver();
    private final CombatResolver      combatResolver      = new CombatResolver();
    private final EnemyTurnResolver   enemyTurnResolver   = new EnemyTurnResolver();

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

        // Phase 2 : spin avec SpinContext, puis les Jokers prennent leur valeur
        Player      player  = gameState.getPlayer();
        SlotMachine machine = player.getSlotMachine();
        Symbol[] drawn   = machine.spin(turnContext.getSpinContext());
        Symbol[] symbols = machine.resolveJokers(drawn, turnContext.getSpinContext());

        // Symboles -> couples (symbole, action)
        List<SymbolAction> symbolActions = actionResolver.resolve(symbols);

        // Combat : actions + CombatContext → TurnResult (mute déjà Player/Enemy)
        // Les événements de phase 1 (ex : symbole boosté par une carte) sont
        // fusionnés en tête du journal du tour.
        TurnResult result = combatResolver.resolve(
            turnContext.getCombatContext(),
            symbolActions,
            symbols,
            drawn,
            turnContext.getEvents()
        );

        // Tour de l'ennemi, s'il a survécu : le bouclier du joueur (ses symboles de
        // défense) absorbe ses attaques, et le renvoi dépend des cartes jouées ce tour.
        Enemy enemy = gameState.getEnemy();
        EnemyTurnResult enemyTurn = null;
        if (!enemy.isDefeated() && !player.isDefeated()) {
            enemyTurn = enemyTurnResolver.resolve(enemy, player,
                turnContext.getCombatContext().getTotalReflectPercent(), CombatResolver.VAULT_REFLECT_SHARE);
        }

        result = result.withEnemyTurn(enemyTurn, storeLeftoverShield(player));

        player.getLastingEffects().endTurn();
        gameState.nextTurn();

        return result;
    }

    /**
     * Fin du tour : le bouclier ne valait que pour ce tour, ce qui en reste
     * (après les attaques de l'ennemi) remplit le Coffre (Carreau).
     *
     * @return l'événement du Coffre rempli, s'il restait du bouclier
     */
    static List<Event> storeLeftoverShield(Player player) {
        List<Event> events = new ArrayList<>();
        int leftoverShield = player.getShield();
        if (leftoverShield > 0) {
            player.getLastingEffects().addVault(leftoverShield);
            events.add(new GaugeFilledEvent(GaugeFilledEvent.Gauge.COFFRE, leftoverShield));
        }
        player.resetTurnDefenses();
        return events;
    }
}
