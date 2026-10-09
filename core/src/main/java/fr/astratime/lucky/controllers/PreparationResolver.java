package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Combo;
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
import fr.astratime.lucky.entities.events.ComboEvent;
import fr.astratime.lucky.entities.events.CorruptionEvent;
import fr.astratime.lucky.entities.events.StatusEvent;
import fr.astratime.lucky.entities.exploration.PlaceRule;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;

import java.util.List;

/**
 * Phase 1 : applique les effets des cartes jouées par le joueur
 * et construit le TurnContext (SpinContext + CombatContext) qui sera
 * utilisé pour le spin et la résolution du combat. Les effets qui durent
 * plusieurs tours (symboles retirés, bonus de gains du combat, Lames,
 * Corruption) y sont appliqués en premier ; les combinaisons formées par les
 * cartes jouées ({@link Combo}), en dernier.
 */
public class PreparationResolver {

    /** Attaque ajoutée à chaque symbole par Lame (Pique). */
    public static final int BLADE_ATTACK = 20;

    /** Jauges remplies par carte jouée quand une Couleur ou une Suite est formée. */
    static final int COMBO_BLADES = 1;
    static final int COMBO_BLOOD  = 25;
    static final int COMBO_VAULT  = 40;

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
        spinContext.blockReel(lasting.takeForbiddenReel()); // Rouleau interdit de l'ennemi
        // Rouleaux volés par la Machine Originelle : d'abord celui de droite, puis celui de gauche.
        int stolen = enemy.getStolenReels();
        if (stolen >= 1) spinContext.blockReel(SlotMachine.SYMBOL_COUNT - 1);
        if (stolen >= 2) spinContext.blockReel(0);
        int fake = lasting.takeFakeGains(); // la Fausse monnaie pas dépensée disparaît
        if (fake > 0 && player.getGains() > 0) {
            int lost = Math.min(fake, player.getGains());
            player.addGains(-lost);
            turnContext.addEvent(new StatusEvent(Lang.f("FAUSSE MONNAIE : -{0} GAINS", lost), EffectPopup.Style.DAMAGE));
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
            int consumed = player.consumeGainsPercent(CorruptionEffect.GAINS_PERCENT / 100f);
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
        if (rule.isHighTide(turn) && lasting.getAnchorTurns() > 0) {
            turnContext.addEvent(new StatusEvent(Lang.t("ANCRE : LA MARÉE HAUTE NE TE GÊNE PAS"), EffectPopup.Style.DEFENSE));
        } else if (rule.isHighTide(turn)) {
            combatContext.multiplyAttack(1f - PlaceRule.HIGH_TIDE_MALUS / 100f, "Marée haute");
            turnContext.addEvent(new StatusEvent(Lang.f("MARÉE HAUTE : ATTAQUE -{0} %", PlaceRule.HIGH_TIDE_MALUS),
                EffectPopup.Style.DAMAGE));
        }

        pendingEffects.forEach(effect -> effect.apply(turnContext));
        if (houseRule != LastingEffects.HouseRule.NO_COMBOS) applyCombos(turnContext, player);

        return turnContext;
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
        String names = combos.stream().map(Combo::getDisplayName).collect(java.util.stream.Collectors.joining(" + "));
        combat.multiplyGains(total, names);
        combat.multiplyAttack(total, names);
        for (Combo combo : combos) {
            if (combo.fillsGauges()){
                fillGauges(player);
            }
            turnContext.addEvent(new ComboEvent(combo));
        }
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
                case TREFLE  -> { } // le Trèfle rapporte déjà des gains
            }
        }
    }
}
