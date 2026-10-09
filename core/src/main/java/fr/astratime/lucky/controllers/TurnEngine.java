package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.*;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.context.SpinContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GaugeFilledEvent;
import fr.astratime.lucky.entities.events.LastDrawEvent;
import fr.astratime.lucky.entities.events.PlayerDamagedEvent;
import fr.astratime.lucky.entities.events.SafeOpenedEvent;
import fr.astratime.lucky.entities.events.StatusEvent;
import fr.astratime.lucky.entities.effects.Effect;
import fr.astratime.lucky.entities.exploration.PlaceRule;
import fr.astratime.lucky.i18n.Lang;
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
        return playTurn(gameState, pendingEffects, SpinEconomy.Stake.NONE);
    }

    /**
     * Comme {@link #playTurn(GameState, List)}, avec la Mise {@code stake} déjà
     * payée (et le coût du tirage aussi) : elle multiplie les symboles du tirage.
     * La dette du joueur, après ce paiement, affaiblit ses symboles (voir {@link SpinEconomy.Debt}).
     */
    public TurnResult playTurn(GameState gameState, List<Effect> pendingEffects, SpinEconomy.Stake stake) {

        // Phase 1 : effets des cartes → TurnContext
        TurnContext turnContext = preparationResolver.resolve(
            pendingEffects,
            gameState.getPlayer(),
            gameState.getEnemy(),
            gameState.getActiveRule(),
            gameState.getTurnNumber()
        );
        applyEconomy(turnContext, gameState, stake);

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

        // Tournée générale : la machine tourne encore, le meilleur tirage reste
        for (int i = 0; i < turnContext.getSpinContext().getBestOfTwo(); i++) {
            Symbol[] again = machine.spin(turnContext.getSpinContext());
            if (matches(machine.resolveJokers(again, turnContext.getSpinContext())) > matches(symbols)) {
                drawn   = again;
                symbols = machine.resolveJokers(again, turnContext.getSpinContext());
            }
        }

        // Ivresse (ennemi) : un rouleau tourne deux fois et garde le pire résultat
        int drunk = player.getLastingEffects().takeDrunk();
        for (int i = 0; i < drunk; i++) {
            Symbol[] twice = drunkReel(machine, drawn, turnContext);
            drawn   = twice;
            symbols = machine.resolveJokers(twice, turnContext.getSpinContext());
        }

        // Nouvelle règle du Directeur des Jeux : les rouleaux tournent deux fois (le pire reste)…
        SpinContext spin = turnContext.getSpinContext();
        if (spin.isDoubleSpin()) {
            Symbol[] again = machine.spin(spin);
            if (matches(machine.resolveJokers(again, spin)) <= matches(symbols)) {
                drawn   = again;
                symbols = machine.resolveJokers(again, spin);
            }
        }
        // … ou le Bingo est interdit : le dernier rouleau libre retourne tant qu'il complète un Bingo.
        if (spin.isBingoForbidden()) {
            Symbol[] unbingoed = breakJackpot(machine, drawn, spin);
            drawn   = unbingoed;
            symbols = machine.resolveJokers(unbingoed, spin);
        }

        // Prédiction de la Cartomancienne : son symbole est-il sorti ?
        gameState.getEnemy().checkPrediction(symbols);

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
        List<Event> afterCombat = new ArrayList<>();
        String notice = enemy.takeNotice(); // un coup annulé ou une résistance passés inaperçus
        if (notice != null) afterCombat.add(new StatusEvent(notice, EffectPopup.Style.SPECIAL));
        if (enemy.isLastDrawPending() && !player.isDefeated()) afterCombat.addAll(lastDraw(player, enemy));

        EnemyTurnResult enemyTurn = null;
        if (!enemy.isDefeated() && !player.isDefeated() && enemy.takeDazzle()) {
            // Rayon du phare : ébloui, il passe son tour (sa défense ne valait que pour celui du joueur).
            enemy.collectThorns(); // ses Épines ne piquent pas non plus
            enemy.resetDefense();
            enemy.resetDamageTaken();
            afterCombat.add(new StatusEvent(Lang.t("ÉBLOUI : L'ENNEMI PASSE SON TOUR"), EffectPopup.Style.SPECIAL));
        } else if (!enemy.isDefeated() && !player.isDefeated()) {
            // Ses Épines piquent d'abord : si le joueur en meurt, l'ennemi ne joue pas son tour.
            List<Event> thorns = enemyTurnResolver.prickThorns(enemy, player);
            if (player.isDefeated()) {
                afterCombat.addAll(thorns);
            } else {
                // Bingo de bouclier : s'il frappe, tout le bouclier lui est renvoyé.
                int shieldReflect = result.isShieldBingo() ? player.getShield() : 0;
                enemyTurn = enemyTurnResolver.resolve(enemy, player,
                    turnContext.getCombatContext().getTotalReflectPercent(), CombatResolver.VAULT_REFLECT_SHARE,
                    shieldReflect, thorns, turnContext.getCombatContext().getReflectHpPercent());
            }
        }

        // Coup de grisou (Mines d'Or) : il frappe le joueur seul, son bouclier le protège
        List<Event> firedamp = new ArrayList<>();
        boolean explodes = gameState.getActiveRule().firedampExplodes(gameState.getTurnNumber()) && !player.isDefeated()
            && !enemy.isDefeated();
        if (explodes && player.getLastingEffects().useLamp()) { // Lampe à carbure : le grisou est évité
            firedamp.add(new StatusEvent(Lang.t("LAMPE À CARBURE : GRISOU ÉVITÉ"), EffectPopup.Style.DEFENSE));
        } else if (explodes) {
            int shieldBefore = player.getShield();
            int lost = player.takeDamage(Math.round(player.getMaxHp() * PlaceRule.FIREDAMP_PERCENT / 100f));
            firedamp.add(new StatusEvent(Lang.t("COUP DE GRISOU !"), EffectPopup.Style.DAMAGE));
            firedamp.add(new PlayerDamagedEvent(lost, shieldBefore - player.getShield(), player.getShield()));
        }

        notice = enemy.takeNotice(); // pendant son tour (renvoi de dégâts, Duel...)
        if (notice != null) firedamp.add(0, new StatusEvent(notice, EffectPopup.Style.SPECIAL));
        if (enemy.isLastDrawPending() && !player.isDefeated()) firedamp.addAll(lastDraw(player, enemy));

        // Le Temps Mort : le dernier tour est passé, le joueur a perdu.
        int limit = enemy.getKind().getTurnLimit();
        if (limit > 0 && gameState.getTurnNumber() >= limit && !enemy.isDefeated() && !player.isDefeated()) {
            firedamp.add(new StatusEvent(Lang.t("TEMPS MORT : LE TEMPS EST ÉCOULÉ"), EffectPopup.Style.DAMAGE));
            firedamp.add(new PlayerDamagedEvent(player.loseAllHp(), 0, player.getShield()));
        }

        if (player.takeRopeSaved()) {
            firedamp.add(new StatusEvent(Lang.t("CORDE DE RAPPEL : TU TIENS À 1 PV"), EffectPopup.Style.DEFENSE));
        }

        List<Event> endEvents = storeLeftoverShield(player);
        endEvents.addAll(0, firedamp);
        endEvents.addAll(0, afterCombat);
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

    /** Mise (symboles multipliés) et dette (symboles affaiblis, Huissier) du tirage. */
    private static void applyEconomy(TurnContext turnContext, GameState gameState, SpinEconomy.Stake stake) {
        CombatContext combat = turnContext.getCombatContext();
        if (stake != SpinEconomy.Stake.NONE) {
            combat.multiplySymbolPower(stake.factor, "Mise");
            turnContext.addEvent(new StatusEvent(Lang.f("MISE : SYMBOLES x{0}", stake.factor), EffectPopup.Style.SPECIAL));
        }
        SpinEconomy.Debt debt = SpinEconomy.debt(gameState.getPlayer().getGains(), combat.getSpinCost());
        if (debt == SpinEconomy.Debt.NONE) return;
        combat.multiplyAttack(debt.factor, debt == SpinEconomy.Debt.BAILIFF ? "Huissier" : "Endetté");
        combat.multiplyDefense(debt.factor);
        if (debt == SpinEconomy.Debt.BAILIFF) {
            gameState.getEnemy().sendBailiff();
            turnContext.addEvent(new StatusEvent(Lang.t("HUISSIER : SYMBOLES -50 %"), EffectPopup.Style.DAMAGE));
        } else {
            turnContext.addEvent(new StatusEvent(Lang.t("ENDETTÉ : SYMBOLES -25 %"), EffectPopup.Style.DAMAGE));
        }
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

    /**
     * Bingo interdit : tant que les rouleaux font un Bingo, le dernier rouleau
     * libre (ni vide ni imposé) tourne à nouveau.
     *
     * @return les symboles arrêtés, sans Bingo si c'était possible
     */
    private Symbol[] breakJackpot(SlotMachine machine, Symbol[] drawn, SpinContext spin) {
        int reel = -1;
        for (int i = 0; i < drawn.length; i++) {
            if (drawn[i] != null && !spin.getForcedReels().containsKey(i)) reel = i;
        }
        Symbol[] result = drawn.clone();
        for (int tries = 0; reel >= 0 && tries < 50
                && SlotMachine.jackpotSymbol(machine.resolveJokers(result, spin)) != null; tries++) {
            result[reel] = machine.spin(spin)[reel];
        }
        return result;
    }

    /**
     * Dernier tirage de la Machine Originelle, qui a résisté au coup fatal :
     * le joueur et elle lancent chacun un seul rouleau (le joueur, un de ses
     * symboles ; elle, un des rouleaux classiques). Le meilleur score gagne (le
     * rang du symbole : fruits en bas, 7, 777 puis Joker en haut) ; à égalité, on relance. Gagné, elle
     * tombe ; perdu, le joueur tombe.
     *
     * @return le Dernier tirage (et le coup fatal au joueur, s'il perd)
     */
    List<Event> lastDraw(Player player, Enemy enemy) {
        List<Event> events = new ArrayList<>();
        List<Symbol> mine = player.getSlotMachine().getReels(), hers = Symbol.classicReels();
        List<Symbol> own = new ArrayList<>(), theirs = new ArrayList<>();
        do {
            own.add(mine.get(random.nextInt(mine.size())));
            theirs.add(hers.get(random.nextInt(hers.size())));
        } while (score(own.get(own.size() - 1)) == score(theirs.get(theirs.size() - 1)));
        boolean won = score(own.get(own.size() - 1)) > score(theirs.get(theirs.size() - 1));
        enemy.endLastDraw(won);
        events.add(new LastDrawEvent(mine, own, theirs, won));
        if (!won) events.add(new PlayerDamagedEvent(player.loseAllHp(), 0, player.getShield()));
        return events;
    }

    /** @return le rang d'un symbole au Dernier tirage (voir {@link LastDrawEvent#score}). */
    static int score(Symbol symbol) { return LastDrawEvent.score(symbol); }

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
