package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.JackpotEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Journal d'événements produit par CombatResolver à la fin d'un tour.
 * GameScreen lit ce journal pour mettre à jour l'affichage.
 * gainsFromPairOrJackpot ne couvre que le bonus de paire/jackpot —
 * les gains issus de GainAction ou d'AttackAction (As de Pique) sont
 * déjà appliqués au joueur et visibles dans le journal d'événements.
 */
public class TurnResult {

    private final List<Event>         events;
    private final Symbol[]            symbols;
    private final int                 gainsFromPairOrJackpot;
    private final List<SymbolOutcome> symbolOutcomes;
    private final List<Event>         pairOrJackpotEvents;
    private final List<Event>         enemyTurnEvents;
    private final Symbol[]            drawnSymbols;
    private final List<Event>         cardEvents;
    private final List<Event>         pistolEvents;
    private final EnemyTurnResult     enemyTurn;

    /**
     * @param events                 journal des événements survenus pendant le tour
     * @param symbols                symboles tirés ce tour (copiés)
     * @param gainsFromPairOrJackpot gains issus uniquement du bonus de paire/jackpot
     * @param symbolOutcomes         détail par symbole tiré : son action et les événements qu'elle a produits
     * @param pairOrJackpotEvents    événements du bonus de paire/jackpot (vide si aucun)
     * @param enemyTurnEvents        événements de fin de tour, après celui de l'ennemi (ex : Coffre rempli)
     */
    public TurnResult(List<Event> events, Symbol[] symbols, int gainsFromPairOrJackpot,
                       List<SymbolOutcome> symbolOutcomes,
                       List<Event> pairOrJackpotEvents, List<Event> enemyTurnEvents) {
        this(events, symbols, symbols, gainsFromPairOrJackpot, symbolOutcomes, List.of(), List.of(),
            pairOrJackpotEvents, enemyTurnEvents);
    }

    /**
     * @param drawnSymbols symboles arrêtés sur les rouleaux, Jokers compris (avant leur remplacement)
     * @param cardEvents   événements des cartes révélés au lancer (ex : combo réussi ou raté)
     * @param pistolEvents tirs de pistolet de la Roulette russe (vide si aucun)
     * @see #TurnResult(List, Symbol[], int, List, List, List)
     */
    public TurnResult(List<Event> events, Symbol[] symbols, Symbol[] drawnSymbols, int gainsFromPairOrJackpot,
                      List<SymbolOutcome> symbolOutcomes, List<Event> cardEvents, List<Event> pistolEvents,
                      List<Event> pairOrJackpotEvents, List<Event> enemyTurnEvents) {
        this(events, symbols, drawnSymbols, gainsFromPairOrJackpot, symbolOutcomes, cardEvents, pistolEvents,
            pairOrJackpotEvents, enemyTurnEvents, null);
    }

    private TurnResult(List<Event> events, Symbol[] symbols, Symbol[] drawnSymbols, int gainsFromPairOrJackpot,
                       List<SymbolOutcome> symbolOutcomes, List<Event> cardEvents, List<Event> pistolEvents,
                       List<Event> pairOrJackpotEvents, List<Event> enemyTurnEvents, EnemyTurnResult enemyTurn) {
        this.enemyTurn = enemyTurn;
        this.events  = List.copyOf(events);
        this.symbols = symbols.clone();
        this.drawnSymbols = drawnSymbols.clone();
        this.gainsFromPairOrJackpot = gainsFromPairOrJackpot;
        this.symbolOutcomes = List.copyOf(symbolOutcomes);
        this.cardEvents = List.copyOf(cardEvents);
        this.pistolEvents = List.copyOf(pistolEvents);
        this.pairOrJackpotEvents = List.copyOf(pairOrJackpotEvents);
        this.enemyTurnEvents = List.copyOf(enemyTurnEvents);
    }

    /** @return le journal d'événements du tour (liste immuable). */
    public List<Event> getEvents()  { return events; }
    /** @return une copie des symboles tirés ce tour, Jokers remplacés par ce qu'ils valent. */
    public Symbol[]    getSymbols() { return symbols.clone(); }
    /** @return une copie des symboles arrêtés sur les rouleaux, Jokers compris. */
    public Symbol[]    getDrawnSymbols() { return drawnSymbols.clone(); }
    /** @return les événements des cartes révélés au lancer (ex : combo réussi ou raté). */
    public List<Event> getCardEvents() { return cardEvents; }
    /** @return les tirs de pistolet de la Roulette russe (vide si aucun). */
    public List<Event> getPistolEvents() { return pistolEvents; }
    /** @return les gains issus uniquement du bonus de paire/jackpot. */
    public int         getGainsFromPairOrJackpot() { return gainsFromPairOrJackpot; }
    /** @return pour chaque symbole tiré ayant une action enregistrée, son effet de base et le résultat obtenu ce tour. */
    public List<SymbolOutcome> getSymbolOutcomes() { return symbolOutcomes; }
    /** @return les événements du bonus de paire/jackpot de ce tour (vide si aucun). */
    public List<Event> getPairOrJackpotEvents() { return pairOrJackpotEvents; }
    /** @return les événements de fin de tour, après celui de l'ennemi (ex : Coffre rempli par le bouclier restant). */
    public List<Event> getEnemyTurnEvents() { return enemyTurnEvents; }

    /** @return le tour de l'ennemi qui a suivi, ou {@code null} s'il n'a pas joué (vaincu, ou joueur vaincu). */
    public EnemyTurnResult getEnemyTurn() { return enemyTurn; }

    /**
     * @param enemyTurn tour de l'ennemi qui suit celui du joueur ({@code null} s'il n'a pas joué)
     * @param endEvents événements de fin de tour (ex : Coffre rempli)
     * @return ce résultat, complété du tour de l'ennemi et de la fin du tour (journal compris)
     */
    public TurnResult withEnemyTurn(EnemyTurnResult enemyTurn, List<Event> endEvents) {
        List<Event> all = new ArrayList<>(events);
        if (enemyTurn != null) all.addAll(enemyTurn.events());
        all.addAll(endEvents);
        List<Event> end = new ArrayList<>(enemyTurnEvents);
        end.addAll(endEvents);
        return new TurnResult(all, symbols, drawnSymbols, gainsFromPairOrJackpot, symbolOutcomes, cardEvents,
            pistolEvents, pairOrJackpotEvents, end, enemyTurn);
    }


    /** @return {@code true} si un {@link JackpotEvent} figure dans le journal de ce tour. */
    public boolean isJackpot() {
        return events.stream().anyMatch(e -> e instanceof JackpotEvent);
    }

    /** @return {@code true} si exactement deux des trois symboles tirés sont identiques (jackpot exclu). */
    public boolean isPair() {
        return !isJackpot() && SlotMachine.hasPair(symbols);
    }

    /**
     * @return le symbole d'un Bingo de gains (trois symboles de gains identiques),
     *         ou {@code null} si ce tour n'en est pas un
     */
    public Symbol getGainBingoSymbol() {
        return isJackpot() && SymbolRegistry.getGainSymbols().contains(symbols[0]) ? symbols[0] : null;
    }

    /** @return {@code true} pour un Bingo de bouclier : trois symboles de défense identiques. */
    public boolean isShieldBingo() {
        return isJackpot() && SymbolRegistry.getDefenseSymbols().contains(symbols[0]);
    }
}
