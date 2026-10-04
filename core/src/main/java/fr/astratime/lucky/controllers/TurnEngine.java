package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.*;
import fr.astratime.lucky.entities.context.SpinContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GaugeFilledEvent;
import fr.astratime.lucky.entities.events.PlayerDamagedEvent;
import fr.astratime.lucky.entities.events.SafeOpenedEvent;
import fr.astratime.lucky.entities.events.StatusEvent;
import fr.astratime.lucky.entities.effects.Effect;
import fr.astratime.lucky.entities.exploration.PlaceRule;
import fr.astratime.lucky.popups.EffectPopup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private final Random              random              = new Random();

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
            gameState.getEnemy(),
            gameState.getActiveRule(),
            gameState.getTurnNumber()
        );

        // Phase 2 : spin avec SpinContext, puis les Jokers prennent leur valeur
        Player      player  = gameState.getPlayer();
        SlotMachine machine = player.getSlotMachine();
        Symbol[] drawn   = machine.spin(turnContext.getSpinContext());
        Symbol[] symbols = machine.resolveJokers(drawn, turnContext.getSpinContext());
        // Relance : sans paire, la machine relance (une fois par carte)
        Symbol[] rerolled = null;
        for (int i = 0; i < turnContext.getSpinContext().getRerolls() && !SlotMachine.hasPair(symbols); i++) {
            if (rerolled == null) rerolled = drawn;
            drawn   = machine.spin(turnContext.getSpinContext());
            symbols = machine.resolveJokers(drawn, turnContext.getSpinContext());
        }

        // Ivresse (ennemi) : un rouleau tourne deux fois et garde le pire résultat
        int drunk = player.getLastingEffects().takeDrunk();
        for (int i = 0; i < drunk; i++) {
            Symbol[] twice = drunkReel(machine, drawn, turnContext);
            drawn   = twice;
            symbols = machine.resolveJokers(twice, turnContext.getSpinContext());
        }

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
            // Bingo de bouclier : s'il frappe, tout le bouclier lui est renvoyé.
            int shieldReflect = result.isShieldBingo() ? player.getShield() : 0;
            enemyTurn = enemyTurnResolver.resolve(enemy, player,
                turnContext.getCombatContext().getTotalReflectPercent(), CombatResolver.VAULT_REFLECT_SHARE,
                shieldReflect);
        }

        // Coup de grisou (Mines d'Or) : il frappe le joueur seul, son bouclier le protège
        List<Event> firedamp = new ArrayList<>();
        if (gameState.getActiveRule().firedampExplodes(gameState.getTurnNumber()) && !player.isDefeated()
            && !enemy.isDefeated()) {
            int shieldBefore = player.getShield();
            int lost = player.takeDamage(Math.round(player.getMaxHp() * PlaceRule.FIREDAMP_PERCENT / 100f));
            firedamp.add(new StatusEvent("COUP DE GRISOU !", EffectPopup.Style.DAMAGE));
            firedamp.add(new PlayerDamagedEvent(lost, shieldBefore - player.getShield(), player.getShield()));
        }

        List<Event> endEvents = storeLeftoverShield(player);
        endEvents.addAll(0, firedamp);
        // Coffres-forts : ceux arrivés à terme s'ouvrent ; tous, si l'ennemi est vaincu
        int safe = player.getLastingEffects().openSafes(enemy.isDefeated());
        if (safe > 0 && !player.isDefeated()) {
            player.addGains(safe);
            endEvents.add(new SafeOpenedEvent(safe));
        }
        result = result.withEnemyTurn(enemyTurn, endEvents);
        if (rerolled != null) result = result.withReroll(rerolled);

        player.getLastingEffects().endTurn();
        gameState.nextTurn();

        return result;
    }

    /**
     * Ivresse : un rouleau tiré au hasard (ni vide ni imposé) tourne une seconde
     * fois ; le tirage garde le pire des deux résultats, celui qui aligne le
     * moins de symboles identiques (au second, à égalité).
     *
     * @return les symboles arrêtés sur les rouleaux, après l'Ivresse
     */
    private Symbol[] drunkReel(SlotMachine machine, Symbol[] drawn, TurnContext turnContext) {
        List<Integer> open = new ArrayList<>();
        for (int i = 0; i < drawn.length; i++) {
            if (drawn[i] != null && !turnContext.getSpinContext().getForcedReels().containsKey(i)) open.add(i);
        }
        if (open.isEmpty()) return drawn;
        int reel = open.get(random.nextInt(open.size()));
        Symbol[] twice = drawn.clone();
        twice[reel] = machine.spin(turnContext.getSpinContext())[reel];
        SpinContext spin = turnContext.getSpinContext();
        boolean worse = matches(machine.resolveJokers(twice, spin)) <= matches(machine.resolveJokers(drawn, spin));
        return worse ? twice : drawn;
    }

    /** @return le plus grand nombre de symboles identiques parmi {@code symbols} (rouleaux vides exclus). */
    static int matches(Symbol[] symbols) {
        int best = 0;
        for (Symbol a : symbols) {
            if (a == null) continue;
            int count = 0;
            for (Symbol b : symbols) if (b == a) count++;
            best = Math.max(best, count);
        }
        return best;
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
