package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.*;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.context.SpinContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
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
import java.util.Map;
import java.util.Random;
import java.util.HashMap;

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
    /** Le Jeu bonus peut s'ouvrir après un Bingo (pas dans le tutoriel). */
    private boolean                   bonusGameEnabled    = true;

    /** Chances qu'un Bingo ouvre le Jeu bonus (plus haut dans la Tour, voir {@link BonusGame#TOWER_TRIGGER_CHANCE}). */
    private float                     bonusGameChance     = BonusGame.TRIGGER_CHANCE;
    /** Cagnotte du Jeu bonus, que remplit la Mise. */
    private BonusPot                  pot                 = new BonusPot();

    /** Ouvre ou ferme le Jeu bonus après les Bingos (fermé dans le tutoriel). */
    public void setBonusGameEnabled(boolean enabled) { bonusGameEnabled = enabled; }

    /** Change les chances qu'un Bingo ouvre le Jeu bonus. */
    public void setBonusGameChance(float chance) { bonusGameChance = chance; }

    /** @return les chances qu'un Bingo ouvre le Jeu bonus. */
    public float getBonusGameChance() { return bonusGameChance; }

    /** Change la cagnotte du Jeu bonus (celle du lieu, de l'Entraînement ou du chapitre). */
    public void setPot(BonusPot pot) { this.pot = pot; }

    /** @return la cagnotte du Jeu bonus. */
    public BonusPot getPot() { return pot; }

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
     * payée (et le coût du tirage aussi) : elle multiplie l'attaque et la défense du tour.
     * La dette du joueur, après ce paiement, affaiblit ses symboles (voir {@link SpinEconomy.Debt}).
     */
    public TurnResult playTurn(GameState gameState, List<Effect> pendingEffects, SpinEconomy.Stake stake) {

        // Soif (Désert de Braise) : les soins du tour sont divisés
        gameState.getPlayer().setHealDivisor(gameState.getActiveRule().isThirsty() ? PlaceRule.THIRST_HEAL_DIVISOR : 1);
        gameState.getPlayer().takeHealed();

        // Phase 1 : effets des cartes → TurnContext
        TurnContext turnContext = preparationResolver.resolve(
            pendingEffects,
            gameState.getPlayer(),
            gameState.getEnemy(),
            gameState.getActiveRule(),
            gameState.getTurnNumber()
        );
        applyDebt(turnContext, gameState);
        applyStake(turnContext, stake);

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

        // Chamboule-tout : chaque rouleau hors paire relance une fois
        for (int i = 0; i < turnContext.getSpinContext().getKnockdowns(); i++) {
            Symbol[] knocked = knockDown(machine, drawn, turnContext.getSpinContext());
            if (knocked == drawn) continue;
            if (rerolled == null) rerolled = drawn;
            drawn   = knocked;
            symbols = machine.resolveJokers(knocked, turnContext.getSpinContext());
            turnContext.addEvent(new StatusEvent(Lang.t("CHAMBOULE-TOUT : ROULEAUX RELANCÉS"), EffectPopup.Style.SPECIAL));
        }

        // Second levier : une paire sans Bingo, le rouleau qui manque relance pour tenter le Bingo
        int levers = turnContext.getSpinContext().getSecondLevers();
        if (levers > 0) {
            Symbol[] levered = secondLever(machine, drawn, turnContext.getSpinContext(), levers);
            if (levered != null) {
                if (rerolled == null) rerolled = drawn;
                drawn   = levered;
                symbols = machine.resolveJokers(levered, turnContext.getSpinContext());
                turnContext.addEvent(new StatusEvent(Lang.t("SECOND LEVIER : BINGO !"), EffectPopup.Style.SPECIAL));
            } else if (SlotMachine.hasPair(symbols) && SlotMachine.jackpotSymbol(symbols) == null) {
                turnContext.addEvent(new StatusEvent(Lang.t("SECOND LEVIER : PAS DE BINGO"), EffectPopup.Style.SPECIAL));
            }
        }

        // Ivresse (ennemi) : un rouleau tourne deux fois et garde le pire résultat
        int drunk = player.getLastingEffects().takeDrunk();
        for (int i = 0; i < drunk; i++) {
            Symbol[] twice = drunkReel(machine, drawn, turnContext);
            drawn   = twice;
            symbols = machine.resolveJokers(twice, turnContext.getSpinContext());
        }

        // Court-circuit (Machine Originelle) : le meilleur rouleau devient le pire
        int shorts = player.getLastingEffects().takeShortCircuits();
        for (int i = 0; i < shorts; i++) {
            Symbol[] burnt = shortCircuit(machine, drawn, turnContext.getSpinContext());
            if (burnt != drawn) {
                turnContext.addEvent(new StatusEvent(Lang.t("COURT-CIRCUIT : ROULEAU GRILLÉ"), EffectPopup.Style.DAMAGE));
            }
            drawn   = burnt;
            symbols = machine.resolveJokers(burnt, turnContext.getSpinContext());
        }

        // Tunnel (Fête foraine) : un rouleau passe dans le noir et prend un symbole au hasard
        int tunnels = player.getLastingEffects().takeTunnels();
        for (int i = 0; i < tunnels; i++) {
            Symbol[] dark = tunnel(machine, drawn, turnContext.getSpinContext(), false);
            if (dark != drawn) turnContext.addEvent(new StatusEvent(Lang.t("TUNNEL : UN ROULEAU DANS LE NOIR"), EffectPopup.Style.DAMAGE));
            drawn   = dark;
            symbols = machine.resolveJokers(dark, turnContext.getSpinContext());
        }
        // Tourniquet (Fête foraine) : un rouleau du meilleur groupe est relancé une fois
        int turnstiles = player.getLastingEffects().takeTurnstiles();
        for (int i = 0; i < turnstiles; i++) {
            Symbol[] turned = tunnel(machine, drawn, turnContext.getSpinContext(), true);
            if (turned != drawn) turnContext.addEvent(new StatusEvent(Lang.t("TOURNIQUET : ROULEAU RELANCÉ"), EffectPopup.Style.DAMAGE));
            drawn   = turned;
            symbols = machine.resolveJokers(turned, turnContext.getSpinContext());
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

        player.getLastingEffects().setLastDrawn(drawn); // Rouleau retenu, Mémoire de la machine

        riggedGames(turnContext, player.getLastingEffects(), symbols);

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

        // Jeu bonus : après un Bingo, une fois sur dix, la grille s'ouvre ; ses gains sont crédités tout de suite.
        BonusGame bonus = bonusGame(result, turnContext.getSpinContext(), player, turnContext.getCombatContext());

        // Tour de l'ennemi, s'il a survécu : le bouclier du joueur (ses symboles de
        // défense) absorbe ses attaques, et le renvoi dépend des cartes jouées ce tour.
        Enemy enemy = gameState.getEnemy();
        List<Event> afterCombat = new ArrayList<>();
        String notice = enemy.takeNotice(); // un coup annulé ou une résistance passés inaperçus
        if (notice != null) afterCombat.add(new StatusEvent(notice, EffectPopup.Style.SPECIAL));
        if (enemy.isLastDrawPending() && !player.isDefeated()) afterCombat.addAll(lastDraw(player, enemy));

        // Créances (Rentier) : au début de son tour, elles lui coûtent des PV et rapportent des gains au joueur
        if (!enemy.isDefeated() && !player.isDefeated()) afterCombat.addAll(chargeClaims(enemy, player, turnContext.getCombatContext()));

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
                EnemyTurnResolver.Reflection reflection = new EnemyTurnResolver.Reflection(
                    turnContext.getCombatContext().getTotalReflectPercent(), CombatResolver.VAULT_REFLECT_SHARE,
                    shieldReflect, turnContext.getCombatContext().getReflectHpPercent());
                enemyTurn = enemyTurnResolver.resolve(enemy, player, reflection, thorns);
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

        // Venin (Désert de Braise) : chaque Venin mord, à travers le bouclier
        int venoms = player.isDefeated() || enemy.isDefeated() ? 0 : player.getLastingEffects().tickVenoms();
        if (venoms > 0) {
            int lost = player.takeDamage((int) Math.max(1L, (long) player.getMaxHp() * venoms * EnemySymbol.VENOM_PERCENT / 100), 0);
            firedamp.add(new StatusEvent(Lang.f("VENIN x{0}", venoms), EffectPopup.Style.DAMAGE));
            firedamp.add(new PlayerDamagedEvent(lost, 0, player.getShield()));
        }
        // Soif (Désert de Braise) : un tour sans soin coûte des PV, à travers le bouclier
        if (gameState.getActiveRule().isThirsty() && !player.takeHealed() && !player.isDefeated() && !enemy.isDefeated()) {
            int lost = player.takeDamage((int) Math.max(1L, (long) player.getMaxHp() * PlaceRule.THIRST_PERCENT / 100), 0);
            firedamp.add(new StatusEvent(Lang.t("SOIF !"), EffectPopup.Style.DAMAGE));
            firedamp.add(new PlayerDamagedEvent(lost, 0, player.getShield()));
        }

        notice = enemy.takeNotice(); // pendant son tour (renvoi de dégâts, Duel...)
        if (notice != null) firedamp.add(0, new StatusEvent(notice, EffectPopup.Style.SPECIAL));
        if (enemy.isLastDrawPending() && !player.isDefeated()) firedamp.addAll(lastDraw(player, enemy));

        // Le Temps Mort : le dernier tour est passé, le joueur a perdu.
        int limit = enemy.getTurnLimit(); // moins ses Comptes à rebours
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
        double safe = player.getLastingEffects().openSafes(enemy.isDefeated());
        if (safe > 0 && !player.isDefeated()) {
            player.addGains(safe);
            endEvents.add(new SafeOpenedEvent(safe));
        }
        result = result.withEnemyTurn(enemyTurn, endEvents);
        if (rerolled != null) result = result.withReroll(rerolled);
        if (bonus != null) result = result.withBonusGame(bonus);

        player.getLastingEffects().endTurn();
        gameState.nextTurn();

        return result;
    }

    /**
     * @return le Jeu bonus qui suit le Bingo de {@code result} (une fois sur dix, trois
     *         dans la Tour, à coup sûr avec la carte de test) : ses gains de base restent fixes,
     *         seul un petit bonus (par symbole aligné, plus le rang par alignement)
     *         reçoit les multiplicateurs du tour, et chaque alignement paie en plus toute la
     *         cagnotte, qui repart de 0 ; ses gains déjà crédités au joueur ;
     *         {@code null} sans Bingo ou si la grille ne s'ouvre pas
     */
    private BonusGame bonusGame(TurnResult result, SpinContext spin, Player player, CombatContext combat) {
        if (!bonusGameEnabled || !result.isJackpot() || player.isDefeated()) return null;
        if (!spin.isBonusGameForced() && random.nextFloat() >= bonusGameChance) return null;
        int spinCost = combat.getSpinCost();
        float scale = SpinEconomy.gainScale(spinCost);
        int perSymbol = Math.round(BonusGame.GAINS_PER_SYMBOL * scale);
        int rank = Math.round(player.getRankBonus().gains() * combat.getRankFactor() * scale);
        float factor = combat.getGainMultiplier() * combat.getGainFactor() * combat.getSymbolPower();
        BonusGame bonus = BonusGame.play(player.getSlotMachine().getReels(), spinCost, random)
            .boosted(perSymbol, rank, factor)
            .withPot(pot.take()); // la cagnotte n'est pas multipliée par le tour
        player.addGains(bonus.getTotal());
        player.addPotWinnings(bonus.getPot() * bonus.getLines().size()); // ce que la cagnotte a rapporté ne se remise pas
        return bonus;
    }

    /**
     * Mise du tirage (choix d'Astra, 2026-10-27) : déjà payée et versée à la
     * cagnotte, elle multiplie l'attaque et la défense de tous les symboles du tour.
     */
    private static void applyStake(TurnContext turnContext, SpinEconomy.Stake stake) {
        if (stake == SpinEconomy.Stake.NONE) return;
        CombatContext combat = turnContext.getCombatContext();
        combat.multiplyAttack(stake.power, "Mise");
        combat.multiplyDefense(stake.power, "Mise");
        turnContext.addEvent(new StatusEvent(Lang.f("MISE : ATTAQUE ET DÉFENSE x{0}", stake.powerText()),
            EffectPopup.Style.SPECIAL));
    }

    /** Dette du tirage : symboles affaiblis (Endetté), et l'Huissier. */
    private static void applyDebt(TurnContext turnContext, GameState gameState) {
        CombatContext combat = turnContext.getCombatContext();
        SpinEconomy.Debt debt = SpinEconomy.debt(gameState.getPlayer().getGains(), combat.getSpinCost());
        if (debt == SpinEconomy.Debt.NONE) return;
        String debtLabel = debt == SpinEconomy.Debt.BAILIFF ? "Huissier" : "Endetté";
        combat.multiplyAttack(debt.factor, debtLabel);
        combat.multiplyDefense(debt.factor, debtLabel);
        if (debt == SpinEconomy.Debt.BAILIFF) {
            gameState.getEnemy().sendBailiff();
            turnContext.addEvent(new StatusEvent(Lang.t("HUISSIER : SYMBOLES -50 %"), EffectPopup.Style.DAMAGE));
        } else {
            turnContext.addEvent(new StatusEvent(Lang.t("ENDETTÉ : SYMBOLES -25 %"), EffectPopup.Style.DAMAGE));
        }
    }

    /**
     * Créances de l'ennemi (Rentier), au début de son tour : chacune lui retire
     * {@link Enemy#CLAIM_HP_PER_MILLE} pour mille de ses PV max (sa défense n'y
     * fait rien, sa Garde si) et rapporte {@link Enemy#CLAIM_SPINS} coût de tirage
     * de gains au joueur. Elles restent jusqu'à la fin du combat.
     *
     * @return le coup des Créances et leurs gains (rien sans Créance)
     */
    static List<Event> chargeClaims(Enemy enemy, Player player, CombatContext combat) {
        List<Event> events = new ArrayList<>();
        int claims = enemy.getClaims();
        if (claims <= 0) return events;
        if (enemy.takePharaohSeal()) claims *= 2; // Sceau du Pharaon
        long raw = Math.max(1L, (long) enemy.getMaxHp() * claims * Enemy.CLAIM_HP_PER_MILLE / 1000);
        long damage = enemy.skinned(raw);
        enemy.takeDamage(raw);
        events.add(new fr.astratime.lucky.entities.events.CardStrikeEvent(Lang.f("CRÉANCES x{0}", claims), damage, raw,
            enemy.getDefense()));
        double gains = (double) claims * Enemy.CLAIM_SPINS * combat.getSpinCost();
        player.addGains(gains);
        events.add(new fr.astratime.lucky.entities.events.GainsEarnedEvent(gains));
        return events;
    }

    /**
     * Second levier : si le tirage fait une paire sans Bingo, un rouleau qui
     * n'en fait pas partie (ni vide ni imposé) relance jusqu'à {@code tries} fois ;
     * le premier Bingo obtenu reste.
     *
     * @return les symboles du Bingo obtenu, ou {@code null} sans paire, avec déjà un Bingo, ou si aucun Bingo ne sort
     */
    private Symbol[] secondLever(SlotMachine machine, Symbol[] drawn, SpinContext spin, int tries) {
        Symbol[] resolved = machine.resolveJokers(drawn, spin);
        if (!SlotMachine.hasPair(resolved) || SlotMachine.jackpotSymbol(resolved) != null) return null;
        Map<Symbol, Integer> counts = new HashMap<>();
        for (Symbol symbol : resolved) if (symbol != null) counts.merge(symbol, 1, Integer::sum);
        Symbol group = counts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
        int reel = -1;
        for (int i = 0; i < drawn.length && reel < 0; i++) {
            if (drawn[i] != null && resolved[i] != group && !spin.getForcedReels().containsKey(i)) reel = i;
        }
        if (reel < 0) return null;
        for (int i = 0; i < tries; i++) {
            Symbol[] again = drawn.clone();
            again[reel] = machine.spin(spin)[reel];
            if (SlotMachine.jackpotSymbol(machine.resolveJokers(again, spin)) != null) return again;
        }
        return null;
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
     * Court-circuit : un rouleau du plus grand groupe de symboles identiques (ni vide
     * ni imposé) tourne à nouveau jusqu'à casser ce groupe : la paire ou le Bingo
     * tombe. Sans groupe, un rouleau au hasard tourne une seconde fois, le pire reste.
     *
     * @return les symboles arrêtés sur les rouleaux, après le Court-circuit ({@code drawn} s'il n'a rien changé)
     */
    private Symbol[] shortCircuit(SlotMachine machine, Symbol[] drawn, SpinContext spin) {
        Symbol[] resolved = machine.resolveJokers(drawn, spin);
        Map<Symbol, Integer> counts = new HashMap<>();
        for (Symbol symbol : resolved) if (symbol != null) counts.merge(symbol, 1, Integer::sum);
        Symbol group = counts.entrySet().stream().filter(entry -> entry.getValue() >= 2)
            .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
        List<Integer> open = new ArrayList<>();
        for (int i = 0; i < drawn.length; i++) {
            if (drawn[i] != null && !spin.getForcedReels().containsKey(i) && (group == null || resolved[i] == group)) open.add(i);
        }
        if (open.isEmpty()) return drawn;
        int reel = open.get(random.nextInt(open.size()));
        if (group == null) {
            Symbol[] twice = drawn.clone();
            twice[reel] = machine.spin(spin)[reel];
            return matches(machine.resolveJokers(twice, spin)) <= matches(resolved) ? twice : drawn;
        }
        Symbol[] result = drawn.clone();
        for (int tries = 0; tries < 50 && machine.resolveJokers(result, spin)[reel] == group; tries++) {
            result[reel] = machine.spin(spin)[reel];
        }
        return result;
    }

    /**
     * Chamboule-tout : chaque rouleau (ni vide ni imposé) dont le symbole ne fait
     * pas partie d'une paire tourne à nouveau, et le nouveau symbole reste.
     *
     * @return les symboles arrêtés sur les rouleaux ({@code drawn} si aucun rouleau ne relance)
     */
    private static Symbol[] knockDown(SlotMachine machine, Symbol[] drawn, SpinContext spin) {
        Symbol[] resolved = machine.resolveJokers(drawn, spin);
        Map<Symbol, Integer> counts = new HashMap<>();
        for (Symbol symbol : resolved) if (symbol != null) counts.merge(symbol, 1, Integer::sum);
        Symbol[] again = null;
        Symbol[] result = drawn.clone();
        for (int i = 0; i < drawn.length; i++) {
            if (drawn[i] == null || spin.getForcedReels().containsKey(i) || counts.getOrDefault(resolved[i], 0) >= 2) continue;
            if (again == null) again = machine.spin(spin);
            result[i] = again[i];
        }
        return again == null ? drawn : result;
    }

    /**
     * Tunnel : un rouleau au hasard (ni vide ni imposé) tourne à nouveau, et le
     * nouveau symbole reste. Tourniquet ({@code best}) : le rouleau est pris dans
     * le plus grand groupe de symboles identiques, s'il y en a un.
     *
     * @return les symboles arrêtés sur les rouleaux ({@code drawn} si aucun rouleau ne pouvait tourner)
     */
    private Symbol[] tunnel(SlotMachine machine, Symbol[] drawn, SpinContext spin, boolean best) {
        Symbol[] resolved = machine.resolveJokers(drawn, spin);
        Map<Symbol, Integer> counts = new HashMap<>();
        for (Symbol symbol : resolved) if (symbol != null) counts.merge(symbol, 1, Integer::sum);
        Symbol group = !best ? null : counts.entrySet().stream().filter(entry -> entry.getValue() >= 2)
            .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
        List<Integer> open = new ArrayList<>();
        for (int i = 0; i < drawn.length; i++) {
            if (drawn[i] != null && !spin.getForcedReels().containsKey(i) && (group == null || resolved[i] == group)) open.add(i);
        }
        if (open.isEmpty()) return drawn;
        int reel = open.get(random.nextInt(open.size()));
        Symbol[] result = drawn.clone();
        result[reel] = machine.spin(spin)[reel];
        return result;
    }

    /**
     * Lot truqué : un tirage avec une Paire (sans Bingo) ne rapporte aucun gain.
     * Jeu truqué : sans Paire ni Bingo, les dégâts du tour sont divisés.
     */
    private static void riggedGames(TurnContext turnContext, LastingEffects lasting, Symbol[] symbols) {
        boolean pair = SlotMachine.hasPair(symbols);
        boolean bingo = SlotMachine.jackpotSymbol(symbols) != null;
        if (lasting.getRiggedPrizes() > 0 && pair && !bingo && lasting.takeRiggedPrize()) {
            turnContext.getCombatContext().multiplyGains(0f, "Lot truqué");
            turnContext.addEvent(new StatusEvent(Lang.t("LOT TRUQUÉ : TA PAIRE NE PAIE PAS"), EffectPopup.Style.DAMAGE));
        } else if (lasting.getRiggedPrizes() > 0 && !pair) {
            lasting.takeRiggedPrize(); // le tirage est passé sans Paire : le lot est perdu
        }
        int divisor = lasting.takeRiggedGame();
        if (divisor > 1 && !pair && !bingo) {
            turnContext.getCombatContext().multiplyAttack(1f / divisor, "Jeu truqué");
            turnContext.addEvent(new StatusEvent(Lang.f("JEU TRUQUÉ : DÉGÂTS /{0}", divisor), EffectPopup.Style.DAMAGE));
        }
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
