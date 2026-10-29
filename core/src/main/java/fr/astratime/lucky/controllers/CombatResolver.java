package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.BigNumber;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.SlotMachine;
import fr.astratime.lucky.entities.SpinEconomy;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolOutcome;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.Combo;
import fr.astratime.lucky.entities.SymbolRegistry;
import fr.astratime.lucky.entities.actions.AttackAction;
import fr.astratime.lucky.entities.actions.BombAction;
import fr.astratime.lucky.entities.actions.GainAction;
import fr.astratime.lucky.entities.actions.MixedAction;
import fr.astratime.lucky.entities.actions.RandomAttackAction;
import fr.astratime.lucky.entities.effects.ForgeHammerEffect;
import fr.astratime.lucky.entities.effects.ForgedBladeEffect;
import fr.astratime.lucky.entities.effects.RustyLeverEffect;
import fr.astratime.lucky.entities.effects.SunkenJackpotEffect;
import fr.astratime.lucky.entities.events.AllInLostEvent;
import fr.astratime.lucky.entities.events.CardStrikeEvent;
import fr.astratime.lucky.entities.events.StatusEvent;
import fr.astratime.lucky.i18n.Lang;
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
import java.util.Arrays;

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
        priorEvents.addAll(archetypesOnDraw(combatContext, symbols, drawnSymbols)); // Joker sauvage, Couronne du Fou...
        List<Event> events = new ArrayList<>(priorEvents);
        double gainsBefore = combatContext.getPlayer().getGains(); // pour le Cœur d'or

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
        List<Event> collected = debtCollection(combatContext); // Recouvrement : le coup, puis ses gains
        collected.stream().filter(e -> !(e instanceof GainsEarnedEvent)).forEach(pistolEvents::add);
        events.addAll(pistolEvents);

        // Bonus de paire/jackpot
        List<Event> pairOrJackpotEvents = new ArrayList<>();
        double gains = 0;
        Symbol jackpot = SlotMachine.jackpotSymbol(symbols);
        if (jackpot != null) {
            gains = SpinEconomy.jackpotGains(jackpot, combatContext.getSpinCost());
            pairOrJackpotEvents.add(new JackpotEvent(jackpot));
        } else if (hasPair(symbols)) {
            gains = SpinEconomy.pairGains(combatContext.getSpinCost());
        }
        gains = BigNumber.round(gains * combatContext.getGainFactor());
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
        collected.stream().filter(e -> e instanceof GainsEarnedEvent).forEach(pairOrJackpotEvents::add);
        List<Event> hits = new ArrayList<>(events);
        hits.addAll(pistolEvents);
        pairOrJackpotEvents.addAll(archetypesAfterDraw(combatContext, symbols, drawnSymbols, hits, gainsBefore));
        events.addAll(pairOrJackpotEvents);

        List<Event> cardEvents = priorEvents.stream().filter(e -> !e.getPopups().isEmpty()).toList();
        return new TurnResult(events, symbols, drawnSymbols, gains, symbolOutcomes, cardEvents, pistolEvents,
            pairOrJackpotEvents, List.of());
    }

    /**
     * Chaque tir de pistolet rejoue le coup le plus fort du tour : le plus gros
     * coup d'un symbole, avec tous ses multiplicateurs (combos, Bingo,
     * Corruption...), à {@code percent} % (moins avec le Joker maudit). Il
     * touche l'ennemi comme un coup normal (sa défense absorbe et s'use, sauf
     * la part percée par une Pique). Sans symbole d'attaque, il tire à blanc.
     * Rien si l'ennemi est déjà vaincu.
     */
    private List<Event> firePistol(CombatContext context, List<SymbolOutcome> outcomes) {
        List<Event> shots = new ArrayList<>();
        long bestRaw = 0;
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
        for (int percent : context.getPistolShots()) {
            if (enemy.isDefeated()) break;
            if (bestSlot < 0) {
                shots.add(PistolShotEvent.blank(enemy.getDefense(), percent));
                continue;
            }
            long raw    = Math.round(bestRaw * percent / 100.0); // le vrai coup, sans plafond
            long hit    = enemy.capHit(raw);
            int pierce  = context.getPiercePercent();
            int blocked = enemy.absorb(hit, pierce);
            long damage = enemy.skinned(raw - blocked);
            enemy.takeDamage(hit - blocked);
            shots.add(new PistolShotEvent(damage, raw, blocked, enemy.getDefense(), pierce, bestSlot, percent));
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
        boolean bar = Arrays.stream(symbols).anyMatch(s -> s == Symbol.BAR || s == Symbol.DOUBLE_BAR);
        if (context.getHammers() > 0 && bar) {
            float boost = (1f + ForgeHammerEffect.BAR_PERCENT / 100f) / (1f + ForgeHammerEffect.ATTACK_PERCENT / 100f);
            context.multiplyAttack((float) Math.pow(boost, context.getHammers()), "Marteau de forge");
            events.add(new StatusEvent(Lang.f("MARTEAU SUR LE BAR : ATTAQUE +{0} %", ForgeHammerEffect.BAR_PERCENT),
                EffectPopup.Style.ATTACK));
        }
        if (context.getLevers() > 0 && hasPair(symbols)) {
            context.multiplyAttack((float) Math.pow(1f + RustyLeverEffect.ATTACK_PERCENT / 100f, context.getLevers()),
                "Levier rouillé");
            events.add(new StatusEvent(Lang.f("LEVIER : PAIRE ! ATTAQUE +{0} %", RustyLeverEffect.ATTACK_PERCENT),
                EffectPopup.Style.ATTACK));
        }
        if (context.getSunkenJackpots() > 0 && SlotMachine.jackpotSymbol(symbols) != null) {
            for (int i = 0; i < context.getSunkenJackpots(); i++) {
                context.multiplyGains(SunkenJackpotEffect.GAINS_FACTOR, "Jackpot englouti");
                context.multiplyAttack(SunkenJackpotEffect.ATTACK_FACTOR, "Jackpot englouti");
            }
            events.add(new StatusEvent(Lang.f("JACKPOT ENGLOUTI : GAINS x{0}, ATTAQUE x{1}",
                SunkenJackpotEffect.GAINS_FACTOR, SunkenJackpotEffect.ATTACK_FACTOR), EffectPopup.Style.GAINS));
        }
        return events;
    }

    /**
     * Cartes des archétypes qui dépendent du tirage, avant que les symboles
     * agissent : le Joker sauvage (attaque par Joker sorti), la Couronne du Fou
     * (Bingo fait avec un Joker), le Taux variable (Créances par symbole de
     * gains) et le Coup de bluff (attaque par malus de l'ennemi).
     *
     * @param drawn symboles arrêtés sur les rouleaux, Jokers compris
     * @return les textes de ces bonus
     */
    private List<Event> archetypesOnDraw(CombatContext context, Symbol[] symbols, Symbol[] drawn) {
        List<Event> events = new ArrayList<>();
        int jokers = (int) Arrays.stream(drawn).filter(s -> s == Symbol.JOKER).count();
        if (context.getWildJokerPercent() > 0 && jokers > 0) {
            int percent = context.getWildJokerPercent() * jokers;
            context.multiplyAttack(1f + percent / 100f, "Joker sauvage");
            events.add(new StatusEvent(Lang.f("JOKER SAUVAGE : ATTAQUE +{0} %", percent), EffectPopup.Style.ATTACK));
        }
        if (context.getFoolCrownFactor() > 1f && jokers > 0 && SlotMachine.jackpotSymbol(symbols) != null) {
            context.multiplyGains(context.getFoolCrownFactor(), "Couronne du Fou");
            events.add(new StatusEvent(Lang.f("COURONNE DU FOU : GAINS x{0}", Combo.formatFactor(context.getFoolCrownFactor())),
                EffectPopup.Style.GAINS));
        }
        if (context.getVariableRate() > 0) {
            int gainSymbols = (int) Arrays.stream(symbols).filter(CombatResolver::isGainSymbol).count();
            int added = context.getEnemy().getMaluses().addClaims(gainSymbols * context.getVariableRate());
            events.add(new StatusEvent(Lang.f("TAUX VARIABLE : CRÉANCES +{0}", added), EffectPopup.Style.DRAIN));
        }
        if (context.getBluffPercent() > 0) {
            int maluses = context.getEnemy().getMaluses().getActiveMaluses();
            int percent = Math.min(context.getBluffMax(), context.getBluffPercent() * maluses);
            if (percent > 0) context.multiplyAttack(1f + percent / 100f, "Coup de bluff");
            events.add(new StatusEvent(Lang.f("COUP DE BLUFF : {0} MALUS, ATTAQUE +{1} %", maluses, percent), EffectPopup.Style.ATTACK));
        }
        return events;
    }

    /** @return {@code true} pour un symbole qui rapporte des gains (seul, ou avec autre chose : Étoile, Pépite). */
    static boolean isGainSymbol(Symbol symbol) {
        if (symbol == null) return false;
        return SymbolRegistry.getAction(symbol).map(action -> action instanceof GainAction
            || action instanceof MixedAction mixed
            && mixed.getActions().stream().anyMatch(part -> part instanceof GainAction)).orElse(false);
    }

    /** @return {@code true} pour un symbole d'attaque (frappe seule, Dé, Bombe). */
    static boolean isAttackSymbol(Symbol symbol) {
        if (symbol == null) return false;
        return SymbolRegistry.getAction(symbol).map(action -> action instanceof AttackAction
            || action instanceof RandomAttackAction || action instanceof BombAction).orElse(false);
    }

    /**
     * Recouvrement : toutes les Créances de l'ennemi sont encaissées d'un coup,
     * une part de ses PV max par Créance (sa défense n'y fait rien, sa Garde si),
     * et des gains par Créance.
     *
     * @return le coup et les gains, ou un texte s'il n'y avait rien à encaisser
     */
    private List<Event> debtCollection(CombatContext context) {
        List<Event> events = new ArrayList<>();
        Enemy enemy = context.getEnemy();
        if (context.getCollectionHpPercent() <= 0f || enemy.isDefeated()) return events;
        int claims = enemy.getMaluses().takeClaims();
        if (claims == 0) {
            events.add(new StatusEvent(Lang.t("RECOUVREMENT : AUCUNE CRÉANCE"), EffectPopup.Style.DRAIN));
            return events;
        }
        long raw = Math.max(1L, Math.round((double) enemy.getMaxHp() * claims * context.getCollectionHpPercent() / 100.0));
        long damage = enemy.skinned(raw);
        enemy.takeDamage(raw);
        events.add(new CardStrikeEvent(Lang.f("RECOUVREMENT : {0} CRÉANCES", claims), damage, raw, enemy.getDefense()));
        double gains = (double) claims * context.getCollectionSpins() * context.getSpinCost();
        if (gains > 0) {
            context.getPlayer().addGains(gains);
            events.add(new GainsEarnedEvent(gains));
        }
        return events;
    }

    /**
     * Cartes des archétypes qui agissent une fois le tirage joué : Chapeau du
     * Lutin (gains par coup), Gigue du Lutin (Lames par gains), Farce du Lutin
     * (défense tombée), Marteau du cordonnier (ennemi vaincu), Bout de
     * l'arc-en-ciel (Pot de Lutin dans la pioche), Rire du Joker (pioche du tour
     * suivant) et Martingale (tirage sans paire).
     *
     * @param hits        tous les événements du tirage (coups des symboles, des cartes, du pistolet)
     * @param gainsBefore gains du joueur avant le tirage
     */
    private List<Event> archetypesAfterDraw(CombatContext context, Symbol[] symbols, Symbol[] drawn, List<Event> hits,
                                            double gainsBefore) {
        List<Event> events = new ArrayList<>();
        Player player = context.getPlayer();
        Enemy  enemy  = context.getEnemy();
        LastingEffects lasting = player.getLastingEffects();
        int spinCost = context.getSpinCost();

        int hatPercent = lasting.getHatPercent();
        if (hatPercent > 0) {
            int touched = (int) hits.stream().filter(e -> e instanceof EnemyDamagedEvent hit && hit.damage > 0).count();
            double gains = (double) touched * spinCost * hatPercent / 100.0;
            if (gains > 0) {
                player.addGains(gains);
                events.add(new StatusEvent(Lang.f(Lang.plural(touched) ? "CHAPEAU DU LUTIN : {0} COUPS" : "CHAPEAU DU LUTIN : {0} COUP",
                    touched), EffectPopup.Style.GAINS));
                events.add(new GainsEarnedEvent(gains));
            }
        }
        int hoardSpins = lasting.getHoardSpins();
        if (hoardSpins > 0) {
            int touched = (int) hits.stream().filter(e -> e instanceof EnemyDamagedEvent hit && hit.damage > 0).count();
            if (touched > 0) {
                double gains = (double) touched * spinCost * hoardSpins;
                player.addGains(gains);
                int attack = lasting.growHoard(touched);
                events.add(new StatusEvent(Lang.f("MAGOT DU DRAGON : ATTAQUE +{0} %", attack), EffectPopup.Style.GAINS));
                events.add(new GainsEarnedEvent(gains));
            }
        }
        if (context.getJackStep() > 0 && drawn != null) {
            int jokers = 0;
            for (Symbol symbol : drawn) if (symbol == Symbol.JOKER) jokers++;
            int added = jokers == 0 ? 0 : lasting.addJokerBox(jokers * context.getJackStep(), context.getJackMax());
            if (added > 0) {
                events.add(new StatusEvent(Lang.f("DIABLE EN BOÎTE : JOKER +{0}", lasting.getJokerBox()), EffectPopup.Style.SPECIAL));
            }
        }
        if (context.getPrankSpins() > 0 && context.getEnemyDefenseAtStart() > 0 && enemy.getDefense() <= 0) {
            double gains = (double) context.getPrankSpins() * spinCost;
            player.addGains(gains);
            lasting.addBlades(context.getPrankBlades());
            events.add(new StatusEvent(Lang.f("FARCE DU LUTIN : DÉFENSE BRISÉE, LAMES +{0}", context.getPrankBlades()),
                EffectPopup.Style.ATTACK));
            events.add(new GainsEarnedEvent(gains));
        }
        if (context.getCobblerPercent() > 0 && enemy.isDefeated() && player.getGains() > 0) {
            double gains = BigNumber.percent(player.getGains(), context.getCobblerPercent());
            player.addGains(gains);
            events.add(new StatusEvent(Lang.f("MARTEAU DU CORDONNIER : GAINS DU COMBAT +{0} %", context.getCobblerPercent()),
                EffectPopup.Style.GAINS));
            events.add(new GainsEarnedEvent(gains));
        }
        if (context.getJigSpinsPerBlade() > 0) {
            double earned = player.getGains() - gainsBefore;
            int blades = earned <= 0 ? 0 : (int) Math.min(context.getJigMaxBlades(),
                Math.floor(earned / ((double) context.getJigSpinsPerBlade() * spinCost)));
            if (blades > 0) lasting.addBlades(blades);
            events.add(new StatusEvent(Lang.f("GIGUE DU LUTIN : LAMES +{0}", blades), EffectPopup.Style.ATTACK));
        }
        if (!context.getRainbowsEnds().isEmpty() && hasAttackPair(symbols)) {
            context.getRainbowsEnds().forEach(lasting::addDeckCard);
            events.add(new StatusEvent(Lang.t("BOUT DE L'ARC-EN-CIEL : UN POT DE LUTIN DANS TA PIOCHE"), EffectPopup.Style.GAINS));
        }
        if (context.getJokerLaughs() > 0 && Arrays.stream(drawn).anyMatch(s -> s == Symbol.JOKER)) {
            lasting.addNextDraws(context.getJokerLaughs());
            events.add(new StatusEvent(Lang.f("RIRE DU JOKER : +{0} CARTES AU PROCHAIN TOUR", context.getJokerLaughs()),
                EffectPopup.Style.DRAW));
        }
        boolean pair = hasPair(symbols);
        lasting.updateMartingale(context.isMartingale(), pair, context.getMartingaleAttack(), context.getMartingalePair());
        if (context.isMartingale() && !pair) {
            events.add(new StatusEvent(Lang.f("MARTINGALE x{0} : LE PROCHAIN TIRAGE SERA MEILLEUR", lasting.getMartingale()),
                EffectPopup.Style.SPECIAL));
        }
        return events;
    }

    /** @return {@code true} si au moins deux symboles d'attaque identiques sont sortis. */
    private static boolean hasAttackPair(Symbol[] symbols) {
        for (Symbol symbol : symbols) {
            if (!isAttackSymbol(symbol)) continue;
            int count = 0;
            for (Symbol other : symbols) if (other == symbol) count++;
            if (count >= 2) return true;
        }
        return false;
    }

    /**
     * Lame forgée : un coup d'épée par carte, à {@link ForgedBladeEffect#PERCENT} %
     * de la plus grosse attaque du tour (symboles et tirs de pistolet), sans
     * tenir compte de la défense ennemie. Rien sans attaque ce tour.
     */
    private List<Event> forgedBlades(CombatContext context, List<SymbolOutcome> outcomes, List<Event> shots) {
        List<Event> strikes = new ArrayList<>();
        if (context.getForgedBlades() <= 0) return strikes;
        long best = 0;
        for (SymbolOutcome outcome : outcomes) {
            for (Event event : outcome.getEvents()) {
                if (event instanceof EnemyDamagedEvent hit) best = Math.max(best, hit.rawDamage);
            }
        }
        for (Event event : shots) {
            if (event instanceof PistolShotEvent shot) best = Math.max(best, shot.rawDamage);
        }
        Enemy enemy = context.getEnemy();
        long raw = Math.round(best * ForgedBladeEffect.PERCENT / 100.0); // le vrai coup, sans plafond
        long hit = enemy.capHit(raw);
        for (int i = 0; i < context.getForgedBlades() && hit > 0 && !enemy.isDefeated(); i++) {
            long damage = enemy.skinned(raw);
            enemy.takeDamage(hit);
            strikes.add(new CardStrikeEvent(Lang.t("LAME FORGÉE !"), damage, raw, enemy.getDefense()));
        }
        return strikes;
    }

    /** Dynamite : une part des PV max de l'ennemi, sans que sa peau d'or ni sa défense n'en arrêtent rien. */
    private Optional<Event> dynamite(CombatContext context) {
        Enemy enemy = context.getEnemy();
        if (context.getDynamitePercent() <= 0 || enemy.isDefeated()) return Optional.empty();
        int hit = Math.max(1, Math.round(enemy.getMaxHp() * context.getDynamitePercent() / 100f));
        enemy.takeTrueDamage(hit);
        return Optional.of(new CardStrikeEvent(Lang.t("DYNAMITE !"), hit, hit, enemy.getDefense()));
    }

    /** Cœur d'or : les gains gagnés ce tour frappent aussi l'ennemi (une fois par carte), sans tenir compte de sa défense. */
    private Optional<Event> goldenHeart(CombatContext context, double gainsEarned) {
        Enemy enemy = context.getEnemy();
        if (context.getGoldenHearts() <= 0 || gainsEarned <= 0 || enemy.isDefeated()) return Optional.empty();
        long raw    = BigNumber.toLong(gainsEarned * context.getGoldenHearts()); // le vrai coup, sans plafond
        long damage = enemy.skinned(raw);
        enemy.takeDamage(enemy.capHit(raw));
        return Optional.of(new CardStrikeEvent(Lang.t("COEUR D'OR !"), damage, raw, enemy.getDefense()));
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
        long raw    = (long) lasting.consumeVault() * context.getCounterAttack(); // le vrai coup, sans plafond
        long damage = enemy.skinned(raw);
        enemy.takeDamage(enemy.capHit(raw));
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
        long damage = enemy.skinned(hit);
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
        double won = BigNumber.cap(player.getGains() * (multiplier - 1));
        player.addGains(won);
        return new BetWonEvent(bet, multiplier, won);
    }

    /** Tapis : avec une paire, les gains du joueur sont triplés ; sans paire, il les perd tous. */
    private Event resolveAllIn(Player player, boolean pair) {
        if (!pair) return new AllInLostEvent(player.consumeGainsPercent(1f));
        double won = BigNumber.cap(player.getGains() * (ALL_IN_FACTOR - 1));
        player.addGains(won);
        return new AllInWonEvent(won);
    }

    /** @return {@code true} si au moins deux symboles sont identiques et non nuls. */
    private boolean hasPair(Symbol[] s) {
        return SlotMachine.hasPair(s);
    }
}
