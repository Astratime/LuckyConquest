package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Combo;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.context.SpinContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.effects.CorruptionEffect;
import fr.astratime.lucky.entities.effects.Effect;
import fr.astratime.lucky.entities.events.ComboEvent;
import fr.astratime.lucky.entities.events.CorruptionEvent;

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
        SpinContext   spinContext   = new SpinContext();
        CombatContext combatContext = new CombatContext(player, enemy);
        TurnContext   turnContext   = new TurnContext(spinContext, combatContext);

        LastingEffects lasting = player.getLastingEffects();
        lasting.getRemovedSymbols().keySet().forEach(spinContext::removeSymbol);
        spinContext.blockReel(lasting.takeForbiddenReel()); // Rouleau interdit de l'ennemi
        if (lasting.getGainBonus() != 0f) combatContext.multiplyGains(1f + lasting.getGainBonus());
        combatContext.addAttackBonus(lasting.getBlades() * BLADE_ATTACK);
        if (lasting.getCorruptionTurns() > 0) {
            int consumed = player.consumeGainsPercent(CorruptionEffect.GAINS_PERCENT / 100f);
            combatContext.multiplyAttack(CorruptionEffect.FACTOR);
            combatContext.multiplyDefense(CorruptionEffect.FACTOR);
            turnContext.addEvent(new CorruptionEvent(consumed, CorruptionEffect.FACTOR));
        }

        pendingEffects.forEach(effect -> effect.apply(turnContext));
        applyCombos(turnContext, player);

        return turnContext;
    }

    /**
     * Chaque combinaison formée par les cartes jouées ce tour multiplie les
     * gains et l'attaque du tirage (les multiplicateurs se cumulent) ; une
     * Couleur ou une Suite remplit en plus la jauge de chaque carte à suite jouée.
     */
    private static void applyCombos(TurnContext turnContext, Player player) {
        CombatContext combat = turnContext.getCombatContext();
        for (Combo combo : Combo.formed(player.getPlayedCards())) {
            combat.multiplyGains(combo.getFactor());
            combat.multiplyAttack(combo.getFactor());
            if (combo.fillsGauges()) fillGauges(player);
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
