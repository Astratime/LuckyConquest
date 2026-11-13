package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Combo;
import fr.astratime.lucky.entities.CourtAndClock;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.SlotMachine;
import fr.astratime.lucky.entities.context.SpinContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.effects.CorruptionEffect;
import fr.astratime.lucky.entities.effects.Effect;
import fr.astratime.lucky.entities.effects.GoldVeinEffect;
import fr.astratime.lucky.entities.enemy.AspirationRule;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.events.ComboEvent;
import fr.astratime.lucky.entities.events.CorruptionEvent;
import fr.astratime.lucky.entities.events.GainsEarnedEvent;
import fr.astratime.lucky.entities.events.StatusEvent;
import fr.astratime.lucky.entities.exploration.Crossroads;
import fr.astratime.lucky.entities.exploration.PlaceRule;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Phase 1 : applique les effets des cartes jouées par le joueur
 * et construit le TurnContext (SpinContext + CombatContext) qui sera
 * utilisé pour le spin et la résolution du combat. Les effets qui durent
 * plusieurs tours (symboles retirés, bonus de gains du combat, Lames,
 * Corruption) y sont appliqués en premier ; les combinaisons formées par les
 * cartes jouées ({@link Combo}), en dernier.
 */
public class PreparationResolver {

    /** Le hasard du Manège (Fête foraine). */
    private final Random carousel = new Random();

    /** Attaque ajoutée à chaque symbole par Lame (Pique). */
    public static final int BLADE_ATTACK = 20;

    /** Jauges remplies par carte jouée quand une Couleur ou une Suite est formée. */
    static final int COMBO_BLADES = 1;
    static final int COMBO_BLOOD  = 25;
    static final int COMBO_VAULT  = 40;
    static final int COMBO_STAR   = 1;

    /**
     * Construit un TurnContext neuf pour {@code player}/{@code enemy}, puis applique
     * chaque effet en attente dessus (dans l'ordre où les cartes ont été jouées).
     *
     * @param pendingEffects effets accumulés depuis le début du tour
     * @param player         joueur du combat en cours
     * @param enemy          ennemi du combat en cours
     * @return le TurnContext résultant, prêt pour le spin et le combat
     */
    public TurnContext resolve(List<Effect> pendingEffects, Player player, Enemy enemy) {
        return resolve(pendingEffects, player, enemy, PlaceRule.NONE, 1);
    }

    /**
     * Comme {@link #resolve(List, Player, Enemy)}, dans un lieu de l'Exploration :
     * la règle {@code rule} (déjà annulée par une Bulle d'air s'il le faut) joue
     * au tour {@code turn}. Le Scorbut resté en main divise le tirage par deux ;
     * la marée haute retire une part de l'attaque.
     */
    public TurnContext resolve(List<Effect> pendingEffects, Player player, Enemy enemy, PlaceRule rule, int turn) {
        SpinContext   spinContext   = new SpinContext();
        CombatContext combatContext = new CombatContext(player, enemy);
        TurnContext   turnContext   = new TurnContext(spinContext, combatContext);

        LastingEffects lasting = player.getLastingEffects();
        lasting.getRemovedSymbols().keySet().forEach(spinContext::removeSymbol);
        if (lasting.getFavoredSymbol() != null) { // Machine abandonnée du carrefour : son symbole sort 2 fois plus
            spinContext.addWeightBoost(lasting.getFavoredSymbol(),
                SlotMachine.BASE_WEIGHT * (Crossroads.MACHINE_FACTOR - 1));
        }
        spinContext.blockReel(lasting.takeForbiddenReel()); // Rouleau interdit de l'ennemi
        // Rouleaux volés par la Machine Originelle : d'abord celui de droite, puis celui de gauche.
        int stolen = enemy.getStolenReels();
        if (stolen >= 1) spinContext.blockReel(SlotMachine.SYMBOL_COUNT - 1);
        if (stolen >= 2) spinContext.blockReel(0);
        lasting.takeVoidReels().forEach(spinContext::blockReel); // Rien ne va plus du Croupier en chef
        if (rule.carouselTurns(turn)) { // Manège (Fête foraine) : un rouleau au hasard tourne avec le manège
            List<Integer> free = new ArrayList<>();
            for (int reel = 0; reel < SlotMachine.SYMBOL_COUNT; reel++) if (!spinContext.getBlockedReels().contains(reel)) free.add(reel);
            if (!free.isEmpty()) {
                spinContext.blockReel(free.get(carousel.nextInt(free.size())));
                turnContext.addEvent(new StatusEvent(Lang.t("MANÈGE : UN ROULEAU TOURNE SANS TOI"), EffectPopup.Style.DAMAGE));
            }
        }
        aspire(lasting.takeAspirations(), player, enemy, spinContext, turnContext);
        int edges = lasting.takeHouseEdges(); // Avantage de la maison
        if (edges > 0) {
            float kept = (float) Math.pow(1f - EnemySymbol.HOUSE_EDGE_PERCENT / 100f, edges);
            combatContext.multiplyGains(kept, "Avantage de la maison");
            turnContext.addEvent(new StatusEvent(Lang.f("AVANTAGE DE LA MAISON : GAINS -{0} %", Math.round((1f - kept) * 100f)),
                EffectPopup.Style.DAMAGE));
        }
        double fake = lasting.takeFakeGains(); // la Fausse monnaie pas dépensée disparaît
        if (fake > 0 && player.getGains() > 0) {
            double lost = Math.min(fake, player.getGains());
            player.addGains(-lost);
            turnContext.addEvent(new StatusEvent(Lang.f("FAUSSE MONNAIE : -{0} GAINS", Lang.big(lost)), EffectPopup.Style.DAMAGE));
        }
        LastingEffects.HouseRule houseRule = lasting.useHouseRule(); // Nouvelle règle du Directeur des Jeux
        if (houseRule == LastingEffects.HouseRule.NO_BINGO) spinContext.forbidBingo();
        if (houseRule == LastingEffects.HouseRule.DOUBLE_SPIN) spinContext.spinTwice();
        if (houseRule != null) {
            turnContext.addEvent(new StatusEvent(Lang.f("RÈGLE : {0}", houseRule.getAnnounce()), EffectPopup.Style.DAMAGE));
        }
        if (lasting.getGainBonus() != 0f) combatContext.multiplyGains(1f + lasting.getGainBonus(), "Porte-bonheur");
        combatContext.addAttackBonus(lasting.getBlades() * BLADE_ATTACK, "Lames");
        if (lasting.getCorruptionTurns() > 0) {
            double consumed = player.consumeGainsPercent(CorruptionEffect.GAINS_PERCENT / 100f);
            combatContext.multiplyAttack(CorruptionEffect.FACTOR, "Corruption");
            combatContext.multiplyDefense(CorruptionEffect.FACTOR, "Corruption");
            turnContext.addEvent(new CorruptionEvent(consumed, CorruptionEffect.FACTOR));
        }

        if (lasting.getGoldVeinTurns() > 0) {
            combatContext.multiplyGains(GoldVeinEffect.FACTOR, "Veine d'or");
            turnContext.addEvent(new StatusEvent(Lang.f("VEINE D'OR : GAINS x{0}", GoldVeinEffect.FACTOR), EffectPopup.Style.GAINS));
        }
        if (lasting.takeNugget()) { // Pépite de l'ennemi
            combatContext.turnGainsToStone();
            turnContext.addEvent(new StatusEvent(Lang.t("PÉPITE : TES GAINS SONT DES PIERRES"), EffectPopup.Style.DAMAGE));
        }
        if (rule == PlaceRule.SCORBUT && hasScurvy(player)) {
            combatContext.multiplyAttack(PlaceRule.SCURVY_FACTOR, "Scorbut");
            combatContext.multiplyDefense(PlaceRule.SCURVY_FACTOR, "Scorbut");
            combatContext.multiplyGains(PlaceRule.SCURVY_FACTOR, "Scorbut");
            turnContext.addEvent(new StatusEvent(Lang.t("SCORBUT : TIRAGE DIVISÉ PAR 2"), EffectPopup.Style.DAMAGE));
        }
        if (lasting.getTemperPercent() > 0) { // Trempe : l'épée durcit à chaque tour
            combatContext.multiplyAttack(1f + lasting.getTemperPercent() / 100f, "Trempe");
            turnContext.addEvent(new StatusEvent(Lang.f("TREMPE : ATTAQUE +{0} %", lasting.getTemperPercent()),
                EffectPopup.Style.ATTACK));
        }
        int scares = lasting.takeScares(); // Frayeur (Fête foraine)
        if (scares > 0) {
            int malus = Math.min(90, EnemySymbol.SCARE_PERCENT * scares);
            combatContext.multiplyAttack(1f - malus / 100f, "Frayeur");
            turnContext.addEvent(new StatusEvent(Lang.f("FRAYEUR : ATTAQUE -{0} %", malus), EffectPopup.Style.DAMAGE));
        }
        int tolls = lasting.takeTolls(); // Péage (Désert de Braise) : des tirages de plus à payer
        if (tolls > 0) {
            player.paySpin((double) combatContext.getSpinCost() * tolls);
            turnContext.addEvent(new StatusEvent(Lang.f("PÉAGE : -{0} GAINS", Lang.big((double) combatContext.getSpinCost() * tolls)),
                EffectPopup.Style.DAMAGE));
        }
        int clubs = (int) player.getPlayedCards().stream().filter(card -> card.getSuit() == Card.Suit.TREFLE).count();
        int paid = lasting.payBills(clubs); // Lettres de change : chaque Trèfle joué en paie une
        if (paid > 0) {
            turnContext.addEvent(new StatusEvent(Lang.f("CRÉANCES PAYÉES : -{0}", paid), EffectPopup.Style.DEFENSE));
        }
        if (rule.isHighTide(turn) && lasting.getAnchorTurns() > 0) {
            turnContext.addEvent(new StatusEvent(Lang.t("ANCRE : LA MARÉE HAUTE NE TE GÊNE PAS"), EffectPopup.Style.DEFENSE));
        } else if (rule.isHighTide(turn)) {
            combatContext.multiplyAttack(1f - PlaceRule.HIGH_TIDE_MALUS / 100f, "Marée haute");
            turnContext.addEvent(new StatusEvent(Lang.f("MARÉE HAUTE : ATTAQUE -{0} %", PlaceRule.HIGH_TIDE_MALUS),
                EffectPopup.Style.DAMAGE));
        }

        archetypes(player, enemy, spinContext, combatContext, turnContext);

        for (Effect effect : pendingEffects) { // chaque bonus porte le nom de sa carte dans le détail des symboles
            combatContext.setSource(effect.getSourceName());
            effect.apply(turnContext);
        }
        combatContext.setSource(null);
        if (lasting.getRiggedSymbol() != null) { // Table truquée du Croupier : après les cartes, rien ne la défait
            spinContext.forceReel(lasting.getRiggedReel(), lasting.getRiggedSymbol());
        }
        if (houseRule != LastingEffects.HouseRule.NO_COMBOS) applyCombos(turnContext, player);

        return turnContext;
    }

    /**
     * Cartes des archétypes qui durent : les Intérêts composés font grossir les
     * Créances de l'ennemi, la Rente viagère pose une Créance et paie, la
     * Martingale récompense les tirages ratés d'affilée.
     */
    private static void archetypes(Player player, Enemy enemy, SpinContext spin, CombatContext combat, TurnContext turn) {
        LastingEffects lasting = player.getLastingEffects();
        int grown = enemy.getMaluses().growClaims();
        if (grown > 0) {
            turn.addEvent(new StatusEvent(Lang.f("INTÉRÊTS COMPOSÉS : CRÉANCES +{0}", grown), EffectPopup.Style.DRAIN));
        }
        int[] annuity = lasting.takeAnnuity();
        if (annuity != null) {
            int added = enemy.getMaluses().addClaims(annuity[0]);
            double gains = (double) annuity[1] * combat.getSpinCost();
            player.addGains(gains);
            turn.addEvent(new StatusEvent(Lang.f("RENTE VIAGÈRE : CRÉANCES +{0}", added), EffectPopup.Style.DRAIN));
            turn.addEvent(new GainsEarnedEvent(gains));
        }
        if (lasting.getJokerBox() > 0) spin.addWeightBoost(fr.astratime.lucky.entities.Symbol.JOKER, lasting.getJokerBox());
        if (lasting.getHoardPercent() > 0) {
            combat.multiplyAttack(1f + lasting.getHoardPercent() / 100f, "Magot du dragon");
            turn.addEvent(new StatusEvent(Lang.f("MAGOT DU DRAGON : ATTAQUE +{0} %", lasting.getHoardPercent()),
                EffectPopup.Style.ATTACK));
        }
        courtAndClock(lasting.getCourtAndClock(), combat, turn);
        if (lasting.getMartingale() > 0) {
            spin.addPairChance(lasting.getMartingalePair() / 100f);
            combat.multiplyAttack(1f + lasting.getMartingaleAttack() / 100f, "Martingale");
            turn.addEvent(new StatusEvent(Lang.f("MARTINGALE : ATTAQUE +{0} %, PAIRE +{1} %", lasting.getMartingaleAttack(),
                lasting.getMartingalePair()), EffectPopup.Style.SPECIAL));
        }
    }

    /**
     * Archétypes du Valet à l'As, au début du tirage : la Grande aiguille pose un
     * Ressort, les Ressorts donnent de l'attaque, les Coucous qui arrivent à leur
     * heure chantent, et le Sceptre du roi rend sa part des combinaisons du tour d'avant.
     */
    private static void courtAndClock(CourtAndClock clock, CombatContext combat, TurnContext turn) {
        int wound = clock.tickMinuteHand();
        if (wound > 0) {
            turn.addEvent(new StatusEvent(Lang.f("GRANDE AIGUILLE : RESSORTS +{0}", wound), EffectPopup.Style.ATTACK));
        }
        if (clock.getSprings() > 0) {
            combat.multiplyAttack(1f + clock.getSpringAttack() / 100f, "Ressorts");
            turn.addEvent(new StatusEvent(Lang.f("RESSORTS : ATTAQUE +{0} %", clock.getSpringAttack()), EffectPopup.Style.ATTACK));
        }
        float cuckoo = clock.tickCuckoos();
        if (cuckoo != 1f) {
            combat.multiplyAttack(cuckoo, "Coucou");
            turn.addEvent(new StatusEvent(Lang.f("COUCOU ! ATTAQUE x{0}", factor(cuckoo)), EffectPopup.Style.ATTACK));
        }
        float sceptre = clock.takeSceptre();
        if (sceptre != 1f) {
            combat.multiplyAttack(sceptre, "Sceptre du roi");
            turn.addEvent(new StatusEvent(Lang.f("SCEPTRE DU ROI : ATTAQUE x{0}", factor(sceptre)), EffectPopup.Style.ATTACK));
        }
    }

    /**
     * Aspirations de la Machine Originelle : pour chaque règle que les cartes jouées
     * ce tour ne respectent pas, elle aspire un rouleau pour ce tirage (celui de
     * droite d'abord, puis celui de gauche, puis celui du milieu).
     */
    static void aspire(List<AspirationRule> rules, Player player, Enemy enemy, SpinContext spin, TurnContext turn) {
        int aspired = 0;
        for (AspirationRule rule : rules) {
            if (rule.isMet(player.getPlayedCards())) {
                turn.addEvent(new StatusEvent(Lang.t("ASPIRATION CONTRÉE !"), EffectPopup.Style.DEFENSE));
                continue;
            }
            int reel = aspiredReel(spin.getBlockedReels());
            if (reel < 0) continue;
            spin.blockReel(reel);
            aspired++;
            turn.addEvent(new StatusEvent(Lang.t("ASPIRATION : ROULEAU ASPIRÉ"), EffectPopup.Style.DAMAGE));
        }
        enemy.setAspired(aspired);
    }

    /** @return le prochain rouleau que l'Aspiration prend (droite, gauche, milieu), ou -1 s'ils sont tous bloqués. */
    public static int aspiredReel(Set<Integer> blocked) {
        int[] order = {SlotMachine.SYMBOL_COUNT - 1, 0, 1};
        for (int reel : order) if (!blocked.contains(reel)) return reel;
        return -1;
    }

    /** @return {@code true} si le joueur garde une carte Scorbut en main, sans l'avoir jouée. */
    static boolean hasScurvy(Player player) {
        return player.getCurrentHand().stream().anyMatch(card -> PlaceRule.SCURVY_CARD.equals(card.getId()));
    }

    /**
     * Les combinaisons formées par les cartes jouées ce tour multiplient les
     * gains et l'attaque du tirage par la somme de leurs multiplicateurs (ex :
     * Suite et Paire, x9.5) ; une Couleur ou une Suite remplit en plus la jauge
     * de chaque carte à suite jouée.
     */
    private static void applyCombos(TurnContext turnContext, Player player) {
        CombatContext combat = turnContext.getCombatContext();
        List<Combo> combos = Combo.formed(player.getPlayedCards());
        if (combos.isEmpty()) return;
        float total = Combo.totalFactor(combos) + combat.getComboBonus() * combos.size(); // Chope
        String names = combos.stream().map(Combo::getDisplayName).collect(Collectors.joining(" + "));
        combat.multiplyGains(total, names);
        combat.multiplyAttack(total, names);
        courtCombos(turnContext, player, combos, total);
        for (Combo combo : combos) {
            if (combo.fillsGauges()){
                fillGauges(player);
            }
            turnContext.addEvent(new ComboEvent(combo));
        }
    }

    /**
     * Cour royale, une fois les combinaisons formées : la Dame de cour ajoute de
     * l'attaque par combinaison, la Quinte royale multiplie l'attaque si Suite et
     * Couleur sont formées ensemble, et le Sceptre du roi garde une part du
     * multiplicateur pour l'attaque du tirage suivant (jamais pour les gains).
     */
    private static void courtCombos(TurnContext turnContext, Player player, List<Combo> combos, float total) {
        CombatContext combat = turnContext.getCombatContext();
        if (combat.getCourtLadyPercent() > 0) {
            int percent = combat.getCourtLadyPercent() * combos.size();
            combat.multiplyAttack(1f + percent / 100f, "Dame de cour");
            turnContext.addEvent(new StatusEvent(Lang.f("DAME DE COUR : ATTAQUE +{0} %", percent), EffectPopup.Style.ATTACK));
        }
        if (combat.getRoyalFlushFactor() != 1f && combos.contains(Combo.SUITE) && combos.contains(Combo.COULEUR)) {
            combat.multiplyAttack(combat.getRoyalFlushFactor(), "Quinte royale");
            turnContext.addEvent(new StatusEvent(Lang.f("QUINTE ROYALE : ATTAQUE x{0}", factor(combat.getRoyalFlushFactor())),
                EffectPopup.Style.ATTACK));
        }
        if (combat.getSceptrePercent() > 0) {
            float kept = Math.max(1f, total * combat.getSceptrePercent() / 100f);
            player.getLastingEffects().getCourtAndClock().setSceptre(kept);
            turnContext.addEvent(new StatusEvent(Lang.f("SCEPTRE DU ROI : x{0} AU PROCHAIN TIRAGE", factor(kept)),
                EffectPopup.Style.ATTACK));
        }
    }

    /** @return le multiplicateur {@code value}, à deux décimales au plus (ex : « 4,75 »). */
    public static String factor(float value) {
        return Lang.decimal(Combo.formatFactor(Math.round(value * 100f) / 100f));
    }

    /** Chaque carte à suite jouée remplit la jauge de sa couleur. */
    private static void fillGauges(Player player) {
        LastingEffects lasting = player.getLastingEffects();
        for (Card card : player.getPlayedCards()) {
            if (card.getSuit() == null) continue;
            switch (card.getSuit()) {
                case PIQUE   -> lasting.addBlades(COMBO_BLADES);
                case COEUR   -> lasting.addBlood(COMBO_BLOOD);
                case CARREAU -> lasting.addVault(COMBO_VAULT);
                case TREFLE  -> lasting.addLuckyStar(COMBO_STAR);
            }
        }
    }
}
