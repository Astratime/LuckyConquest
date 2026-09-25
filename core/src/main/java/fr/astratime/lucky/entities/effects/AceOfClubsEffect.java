package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolRegistry;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;
import java.util.Random;

/**
 * As de Trefle : consomme 30% des gains actuels du joueur pour offrir
 * un gros boost de probabilité ET d'attaque sur un symbole d'attaque au hasard.
 * Le coût est payé immédiatement, à la pose de la carte : les bonus sont
 * calculés à ce moment-là (plus les gains consommés sont grands, plus les
 * bonus le sont), affichés avec leurs valeurs réelles, puis appliqués au spin.
 * C'est un choix tactique fort, donc le retour doit être proportionnellement
 * plus grand que les cartes de Trefle classiques.
 */
public class AceOfClubsEffect extends Effect {

    /** Pourcentage des gains actuels du joueur consommé à la pose de la carte. */
    private static final float CONSUME_PERCENT    = 0.3f;
    /** Boost de poids de base appliqué au symbole d'attaque choisi au hasard. */
    private static final int   WEIGHT_BOOST_AMOUNT = 150;
    /** Bonus d'attaque plat de base accordé pour ce tour. */
    private static final int   ATTACK_BOOST_AMOUNT = 115;
    /** Attaque ajoutée par racine carrée des gains consommés (1 000 gains : +316). */
    static final float          ATTACK_PER_SQRT_GAIN  = 10f;
    /** Boost de poids au plus obtenu avec les gains consommés. */
    static final int            MAX_WEIGHT_FROM_GAINS = 1500;
    private static final Random RANDOM             = new Random();

    /**
     * Consomme 30% des gains du joueur, calcule les bonus qui en découlent
     * (poids + attaque) sur un symbole d'attaque tiré au hasard, les met en
     * attente pour le spin et affiche leurs valeurs.
     */
    @Override
    public void onPlay(PlayContext context) {
        int consumed = context.consumeGainsPercent(CONSUME_PERCENT);
        // Rendement décroissant : l'attaque suit la racine des gains consommés, le boost est plafonné.
        int weightBoost = WEIGHT_BOOST_AMOUNT + Math.min(consumed, MAX_WEIGHT_FROM_GAINS);
        int attackBoost = ATTACK_BOOST_AMOUNT + Math.round(ATTACK_PER_SQRT_GAIN * (float) Math.sqrt(consumed));

        context.addPopups(List.of(
            EffectPopup.scaled("-30% GAINS", EffectPopup.Style.GAINS, consumed, PopupScale.ACE_OF_CLUBS_CONSUMED),
            EffectPopup.scaled("ATTAQUE +" + attackBoost, EffectPopup.Style.ATTACK, attackBoost, PopupScale.ACE_OF_CLUBS_ATTACK),
            EffectPopup.scaled("BOOST SYMBOLE +" + weightBoost, EffectPopup.Style.SPECIAL, weightBoost, PopupScale.ACE_OF_CLUBS_BOOST)
        ));

        context.queueForSpin(new AttackEffect(attackBoost));
        List<Symbol> attackSymbols = SymbolRegistry.getAttackSymbols();
        if (!attackSymbols.isEmpty()) {
            Symbol target = attackSymbols.get(RANDOM.nextInt(attackSymbols.size()));
            context.queueForSpin(new BoostSymbolEffect(target, weightBoost));
        }
    }

    /** Aucun effet propre au spin : les bonus calculés à la pose y sont appliqués via les effets mis en attente. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() {
        return "Consomme 30% des gains : gros boost d'un symbole d'attaque";
    }

    /** Textes fixes : aucun, toutes les valeurs dépendent des gains consommés (voir {@link #onPlay}). */
    @Override
    public List<EffectPopup> getPopups() {
        return List.of();
    }
}
