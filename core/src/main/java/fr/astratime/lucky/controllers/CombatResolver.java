package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolOutcome;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.events.BetLostEvent;
import fr.astratime.lucky.entities.events.CounterAttackEvent;
import fr.astratime.lucky.entities.events.GaugeFilledEvent;
import fr.astratime.lucky.entities.events.BetWonEvent;
import fr.astratime.lucky.entities.events.DamageReflectedEvent;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GainsEarnedEvent;
import fr.astratime.lucky.entities.events.JackpotEvent;
import fr.astratime.lucky.entities.events.PistolShotEvent;
import fr.astratime.lucky.entities.events.PlayerDamagedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    /** Dégâts de base du pistolet quand aucun symbole d'attaque n'est sorti. */
    static final int   PISTOL_BASE_DAMAGE = 10;
    /** Part du Coffre (Carreau) ajoutée à l'attaque ennemie pour calculer le renvoi. */
    static final float VAULT_REFLECT_SHARE = 0.2f;
    /** Part des gains perdue quand le symbole parié ne sort pas. */
    private static final float BET_LOSS   = 0.5f;

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
     * @return le journal d'événements du tour, les symboles, le détail par symbole et les gains de paire/jackpot
     */
    public TurnResult resolve(CombatContext      combatContext,
                              List<SymbolAction> symbolActions,
                              Symbol[]           symbols,
                              List<Event>        priorEvents) {
        return resolve(combatContext, symbolActions, symbols, symbols, priorEvents);
    }

    /**
     * Comme {@link #resolve(CombatContext, List, Symbol[], List)}, avec les
     * symboles arrêtés sur les rouleaux avant le remplacement des Jokers.
     * Après les symboles viennent les tirs de pistolet (Roulette russe), puis
     * le bonus de paire/jackpot et les paris, puis la riposte de l'ennemi.
     *
     * @param drawnSymbols symboles arrêtés sur les rouleaux, Jokers compris
     */
    public TurnResult resolve(CombatContext      combatContext,
                              List<SymbolAction> symbolActions,
                              Symbol[]           symbols,
                              Symbol[]           drawnSymbols,
                              List<Event>        priorEvents) {
        List<Event> events = new ArrayList<>(priorEvents);

        // Chaque action résout elle-même sa logique et retourne ses événements ;
        // on les rattache aussi au symbole qui les a produits pour le journal détaillé.
        List<SymbolOutcome> symbolOutcomes = new ArrayList<>();
        for (SymbolAction symbolAction : symbolActions) {
            List<Event> actionEvents = symbolAction.getAction().resolve(combatContext);
            events.addAll(actionEvents);
            symbolOutcomes.add(new SymbolOutcome(symbolAction.getSymbol(), symbolAction.getSlotIndex(), actionEvents));
        }

        // Tirs de pistolet (Roulette russe), puis contre-attaque du Coffre (As de Carreau)
        List<Event> pistolEvents = firePistol(combatContext, symbolOutcomes);
        counterAttack(combatContext).ifPresent(pistolEvents::add);
        events.addAll(pistolEvents);

        // Bonus de paire/jackpot
        List<Event> pairOrJackpotEvents = new ArrayList<>();
        int gains = 0;
        if (isJackpot(symbols)) {
            gains = GAINS_JACKPOT;
            pairOrJackpotEvents.add(new JackpotEvent());
        } else if (hasPair(symbols)) {
            gains = GAINS_PAIR;
        }
        gains = Math.round(gains * combatContext.getGainFactor());
        if (gains > 0) {
            combatContext.getPlayer().addGains(gains);
            pairOrJackpotEvents.add(new GainsEarnedEvent(gains));
        }

        // Paris : sur les gains du joueur, une fois ceux du tirage crédités
        for (Symbol bet : combatContext.getBets()) {
            pairOrJackpotEvents.add(resolveBet(combatContext.getPlayer(), bet, symbols));
        }
        events.addAll(pairOrJackpotEvents);

        // Riposte de l'ennemi : s'il a survécu au tour du joueur, il attaque à son tour.
        // Le bouclier accumulé par le joueur ce tour absorbe une partie des dégâts
        // (voir Player.takeDamage) ; l'événement reporte les dégâts réellement subis.
        // Le renvoi de dégâts (Carreau) reflète un pourcentage de l'attaque brute de
        // l'ennemi (voir CombatContext.getTotalReflectPercent), indépendamment de ce
        // que le bouclier en a absorbé.
        Enemy enemy = combatContext.getEnemy();
        List<Event> enemyTurnEvents = new ArrayList<>();
        if (!enemy.isDefeated()) {
            Player player         = combatContext.getPlayer();
            int    attackPower    = enemy.getAttackPower();
            int    reflectPercent = combatContext.getTotalReflectPercent(); // selon la vie avant la riposte
            int    actualDamage   = player.takeDamage(attackPower);
            enemyTurnEvents.add(new PlayerDamagedEvent(actualDamage));

            if (reflectPercent > 0) {
                // Le Coffre arme le renvoi : une part de son contenu s'ajoute à l'attaque renvoyée.
                float reflectBase = attackPower + player.getLastingEffects().getVault() * VAULT_REFLECT_SHARE;
                int reflectedDamage = Math.round(reflectBase * (reflectPercent / 100f));
                if (reflectedDamage > 0) {
                    enemy.takeDamage(reflectedDamage);
                    enemyTurnEvents.add(new DamageReflectedEvent(reflectedDamage));
                }
            }
        }

        // Le bouclier ne vaut que pour ce tour : ce qui reste remplit le Coffre (Carreau).
        int leftoverShield = combatContext.getPlayer().getShield();
        if (leftoverShield > 0) {
            combatContext.getPlayer().getLastingEffects().addVault(leftoverShield);
            enemyTurnEvents.add(new GaugeFilledEvent(GaugeFilledEvent.Gauge.COFFRE, leftoverShield));
        }
        events.addAll(enemyTurnEvents);
        combatContext.getPlayer().resetTurnDefenses();

        List<Event> cardEvents = priorEvents.stream().filter(e -> !e.getPopups().isEmpty()).toList();
        return new TurnResult(events, symbols, drawnSymbols, gains, symbolOutcomes, cardEvents, pistolEvents,
            pairOrJackpotEvents, enemyTurnEvents);
    }

    /**
     * Chaque tir de pistolet multiplie les dégâts bruts du symbole d'attaque le
     * plus fort du tirage ({@link #PISTOL_BASE_DAMAGE} si aucun n'est sorti),
     * puis touche l'ennemi (sa défense s'applique, sauf Pique). Rien si
     * l'ennemi est déjà vaincu.
     */
    private List<Event> firePistol(CombatContext context, List<SymbolOutcome> outcomes) {
        List<Event> shots = new ArrayList<>();
        int bestRaw  = PISTOL_BASE_DAMAGE;
        int bestSlot = -1;
        for (SymbolOutcome outcome : outcomes) {
            for (Event event : outcome.getEvents()) {
                if (event instanceof EnemyDamagedEvent hit && (bestSlot < 0 || hit.rawDamage > bestRaw)) {
                    bestRaw  = hit.rawDamage;
                    bestSlot = outcome.getSlotIndex();
                }
            }
        }
        Enemy enemy = context.getEnemy();
        for (int multiplier : context.getPistolShots()) {
            if (enemy.isDefeated()) break;
            int raw     = bestRaw * multiplier;
            int defense = context.isIgnoreDefense() ? 0 : enemy.getDefense();
            int damage  = Math.max(0, raw - defense);
            enemy.takeDamage(damage);
            shots.add(new PistolShotEvent(damage, raw, bestSlot, multiplier));
        }
        return shots;
    }

    /**
     * Contre-attaque (As de Carreau) : le Coffre est vidé d'un coup sur
     * l'ennemi, multiplié, sans tenir compte de sa défense. Rien si l'ennemi est
     * déjà vaincu ou si le Coffre est vide (il reste alors plein).
     */
    private Optional<Event> counterAttack(CombatContext context) {
        Enemy enemy = context.getEnemy();
        LastingEffects lasting = context.getPlayer().getLastingEffects();
        if (context.getCounterAttack() <= 0 || enemy.isDefeated() || lasting.getVault() <= 0) return Optional.empty();
        int damage = lasting.consumeVault() * context.getCounterAttack();
        enemy.takeDamage(damage);
        return Optional.of(new CounterAttackEvent(damage));
    }

    /**
     * Pari sur {@code bet} : s'il sort 1, 2 ou 3 fois, les gains du joueur sont
     * multipliés par 2, 3 ou 4 ; sinon, il en perd la moitié.
     */
    private Event resolveBet(Player player, Symbol bet, Symbol[] symbols) {
        int count = 0;
        for (Symbol symbol : symbols) {
            if (symbol == bet) count++;
        }
        if (count == 0) {
            return new BetLostEvent(bet, player.consumeGainsPercent(BET_LOSS));
        }
        int multiplier = count + 1;
        int won = player.getGains() * (multiplier - 1);
        player.addGains(won);
        return new BetWonEvent(bet, multiplier, won);
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
