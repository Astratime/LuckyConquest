package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.SlotMachine;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolOutcome;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.effects.ForgeHammerEffect;
import fr.astratime.lucky.entities.effects.ForgedBladeEffect;
import fr.astratime.lucky.entities.effects.RustyLeverEffect;
import fr.astratime.lucky.entities.effects.SunkenJackpotEffect;
import fr.astratime.lucky.entities.events.AllInLostEvent;
import fr.astratime.lucky.entities.events.CardStrikeEvent;
import fr.astratime.lucky.entities.events.StatusEvent;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.entities.events.AllInWonEvent;
import fr.astratime.lucky.entities.events.BetLostEvent;
import fr.astratime.lucky.entities.events.CounterAttackEvent;
import fr.astratime.lucky.entities.events.ExecutionEvent;
import fr.astratime.lucky.entities.events.BetWonEvent;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GainsEarnedEvent;
import fr.astratime.lucky.entities.events.JackpotEvent;
import fr.astratime.lucky.entities.events.PistolShotEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Résout le tour du joueur : exécute les actions, calcule les gains de
 * paire/jackpot, construit le journal d'événements et retourne un TurnResult
 * (le tour de l'ennemi qui suit est résolu par {@link EnemyTurnResolver}).
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
    /** Part du Coffre (Carreau) ajoutée à l'attaque ennemie pour calculer le renvoi (voir EnemyTurnResolver). */
    static final float VAULT_REFLECT_SHARE = 0.2f;
    /** Part des PV restants de l'ennemi que la Guillotine peut infliger au plus en un coup. */
    static final int   EXECUTION_MAX_PERCENT = 50;
    /** Part des gains perdue quand le symbole parié ne sort pas. */
    private static final float BET_LOSS   = 0.5f;
    /** Tapis gagné : multiplicateur des gains du joueur. */
    static final int ALL_IN_FACTOR = 3;

    /**
     * Résout un tour de combat complet à partir des actions déjà déterminées
     * par les symboles tirés : exécute chaque action, applique le bonus de
     * paire/jackpot.
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
     * le bonus de paire/jackpot et les paris.
     *
     * @param drawnSymbols symboles arrêtés sur les rouleaux, Jokers compris
     */
    public TurnResult resolve(CombatContext      combatContext,
                              List<SymbolAction> symbolActions,
                              Symbol[]           symbols,
                              Symbol[]           drawnSymbols,
                              List<Event>        priorEvents) {
        priorEvents = new ArrayList<>(priorEvents);
        priorEvents.addAll(cardsOnDraw(combatContext, symbols)); // Marteau de forge, Levier rouillé, Jackpot englouti
        List<Event> events = new ArrayList<>(priorEvents);
        int gainsBefore = combatContext.getPlayer().getGains(); // pour le Cœur d'or

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
        execution(combatContext).ifPresent(pistolEvents::add);
        pistolEvents.addAll(forgedBlades(combatContext, symbolOutcomes, pistolEvents));
        dynamite(combatContext).ifPresent(pistolEvents::add);
        events.addAll(pistolEvents);

        // Bonus de paire/jackpot
        List<Event> pairOrJackpotEvents = new ArrayList<>();
        int gains = 0;
        Symbol jackpot = SlotMachine.jackpotSymbol(symbols);
        if (jackpot != null) {
            gains = GAINS_JACKPOT;
            pairOrJackpotEvents.add(new JackpotEvent(jackpot));
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
        // Tapis : tous les gains misés sur une paire (un jackpot en est une)
        for (int i = 0; i < combatContext.getAllIn(); i++) {
            pairOrJackpotEvents.add(resolveAllIn(combatContext.getPlayer(), hasPair(symbols)));
        }
        goldenHeart(combatContext, combatContext.getPlayer().getGains() - gainsBefore).ifPresent(pairOrJackpotEvents::add);
        events.addAll(pairOrJackpotEvents);

        List<Event> cardEvents = priorEvents.stream().filter(e -> !e.getPopups().isEmpty()).toList();
        return new TurnResult(events, symbols, drawnSymbols, gains, symbolOutcomes, cardEvents, pistolEvents,
            pairOrJackpotEvents, List.of());
    }

    /**
     * Chaque tir de pistolet multiplie les dégâts bruts du symbole d'attaque le
     * plus fort du tirage ({@link #PISTOL_BASE_DAMAGE} si aucun n'est sorti),
     * puis touche l'ennemi (sa défense absorbe et s'use, sauf Pique). Rien si
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
            int raw         = (int) Math.min((long) bestRaw * multiplier, Integer.MAX_VALUE); // pas de dépassement
            boolean pierced = context.isIgnoreDefense();
            int blocked     = pierced ? 0 : enemy.absorb(raw);
            int damage      = enemy.skinned(raw - blocked);
            enemy.takeDamage(raw - blocked);
            shots.add(new PistolShotEvent(damage, raw, blocked, enemy.getDefense(), pierced, bestSlot, multiplier));
        }
        return shots;
    }

    /**
     * Cartes des coffres des lieux qui dépendent du tirage, avant que les
     * symboles agissent : le Marteau de forge frappe plus fort sur un BAR ou un
     * double BAR, le Levier rouillé sur une paire, le Jackpot englouti sur un Bingo.
     *
     * @return les textes de ces bonus
     */
    private List<Event> cardsOnDraw(CombatContext context, Symbol[] symbols) {
        List<Event> events = new ArrayList<>();
        boolean bar = java.util.Arrays.stream(symbols).anyMatch(s -> s == Symbol.BAR || s == Symbol.DOUBLE_BAR);
        if (context.getHammers() > 0 && bar) {
            float boost = (1f + ForgeHammerEffect.BAR_PERCENT / 100f) / (1f + ForgeHammerEffect.ATTACK_PERCENT / 100f);
            context.multiplyAttack((float) Math.pow(boost, context.getHammers()));
            events.add(new StatusEvent("MARTEAU SUR LE BAR : ATTAQUE +" + ForgeHammerEffect.BAR_PERCENT + " %",
                EffectPopup.Style.ATTACK));
        }
        if (context.getLevers() > 0 && hasPair(symbols)) {
            context.multiplyAttack((float) Math.pow(1f + RustyLeverEffect.ATTACK_PERCENT / 100f, context.getLevers()));
            events.add(new StatusEvent("LEVIER : PAIRE ! ATTAQUE +" + RustyLeverEffect.ATTACK_PERCENT + " %",
                EffectPopup.Style.ATTACK));
        }
        if (context.getSunkenJackpots() > 0 && SlotMachine.jackpotSymbol(symbols) != null) {
            for (int i = 0; i < context.getSunkenJackpots(); i++) {
                context.multiplyGains(SunkenJackpotEffect.GAINS_FACTOR);
                context.multiplyAttack(SunkenJackpotEffect.ATTACK_FACTOR);
            }
            events.add(new StatusEvent("JACKPOT ENGLOUTI : GAINS x" + SunkenJackpotEffect.GAINS_FACTOR
                + ", ATTAQUE x" + SunkenJackpotEffect.ATTACK_FACTOR, EffectPopup.Style.GAINS));
        }
        return events;
    }

    /**
     * Lame forgée : un coup d'épée par carte, à {@link ForgedBladeEffect#PERCENT} %
     * de la plus grosse attaque du tour (symboles et tirs de pistolet), sans
     * tenir compte de la défense ennemie. Rien sans attaque ce tour.
     */
    private List<Event> forgedBlades(CombatContext context, List<SymbolOutcome> outcomes, List<Event> shots) {
        List<Event> strikes = new ArrayList<>();
        if (context.getForgedBlades() <= 0) return strikes;
        int best = 0;
        for (SymbolOutcome outcome : outcomes) {
            for (Event event : outcome.getEvents()) {
                if (event instanceof EnemyDamagedEvent hit) best = Math.max(best, hit.rawDamage);
            }
        }
        for (Event event : shots) {
            if (event instanceof PistolShotEvent shot) best = Math.max(best, shot.rawDamage);
        }
        Enemy enemy = context.getEnemy();
        int hit = Math.round(best * ForgedBladeEffect.PERCENT / 100f);
        for (int i = 0; i < context.getForgedBlades() && hit > 0 && !enemy.isDefeated(); i++) {
            int damage = enemy.skinned(hit);
            enemy.takeDamage(hit);
            strikes.add(new CardStrikeEvent("LAME FORGÉE !", damage, hit, enemy.getDefense()));
        }
        return strikes;
    }

    /** Dynamite : une part des PV max de l'ennemi, sans que sa peau d'or ni sa défense n'en arrêtent rien. */
    private Optional<Event> dynamite(CombatContext context) {
        Enemy enemy = context.getEnemy();
        if (context.getDynamitePercent() <= 0 || enemy.isDefeated()) return Optional.empty();
        int hit = Math.max(1, Math.round(enemy.getMaxHp() * context.getDynamitePercent() / 100f));
        enemy.takeTrueDamage(hit);
        return Optional.of(new CardStrikeEvent("DYNAMITE !", hit, hit, enemy.getDefense()));
    }

    /** Cœur d'or : les gains gagnés ce tour frappent aussi l'ennemi (une fois par carte), sans tenir compte de sa défense. */
    private Optional<Event> goldenHeart(CombatContext context, int gainsEarned) {
        Enemy enemy = context.getEnemy();
        if (context.getGoldenHearts() <= 0 || gainsEarned <= 0 || enemy.isDefeated()) return Optional.empty();
        int hit    = gainsEarned * context.getGoldenHearts();
        int damage = enemy.skinned(hit);
        enemy.takeDamage(hit);
        return Optional.of(new CardStrikeEvent("CŒUR D'OR !", damage, hit, enemy.getDefense()));
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
        int hit    = lasting.consumeVault() * context.getCounterAttack();
        int damage = enemy.skinned(hit);
        enemy.takeDamage(hit);
        return Optional.of(new CounterAttackEvent(damage, enemy.getDefense()));
    }

    /**
     * Guillotine : une part des PV restants de l'ennemi, infligée d'un coup,
     * sans tenir compte de sa défense (au plus {@link #EXECUTION_MAX_PERCENT} %).
     * Rien si l'ennemi est déjà vaincu.
     */
    private Optional<Event> execution(CombatContext context) {
        Enemy enemy = context.getEnemy();
        if (context.getExecutionPercent() <= 0 || enemy.isDefeated()) return Optional.empty();
        int percent = Math.min(EXECUTION_MAX_PERCENT, context.getExecutionPercent());
        int hit     = Math.max(1, Math.round(enemy.getHp() * percent / 100f));
        int damage  = enemy.skinned(hit);
        enemy.takeDamage(hit);
        return Optional.of(new ExecutionEvent(damage, percent, enemy.getDefense()));
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

    /** Tapis : avec une paire, les gains du joueur sont triplés ; sans paire, il les perd tous. */
    private Event resolveAllIn(Player player, boolean pair) {
        if (!pair) return new AllInLostEvent(player.consumeGainsPercent(1f));
        int won = player.getGains() * (ALL_IN_FACTOR - 1);
        player.addGains(won);
        return new AllInWonEvent(won);
    }

    /** @return {@code true} si au moins deux symboles sont identiques et non nuls. */
    private boolean hasPair(Symbol[] s) {
        return SlotMachine.hasPair(s);
    }
}
