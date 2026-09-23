package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolRegistry;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;

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
    private static final int   ATTACK_BOOST_AMOUNT = 15;
    private static final Random RANDOM             = new Random();

    /** Valeurs donnant la taille de texte maximale. */
    private static final float POPUP_MAX_CONSUMED = 1000f;
    private static final float POPUP_MAX_ATTACK   = 300f;
    private static final float POPUP_MAX_BOOST    = 1500f;

    /**
     * Consomme 30% des gains du joueur, calcule les bonus qui en découlent
     * (poids + attaque) sur un symbole d'attaque tiré au hasard, les met en
     * attente pour le spin et affiche leurs valeurs.
     */
    @Override
    public void onPlay(PlayContext context) {
        int consumed = context.consumeGainsPercent(CONSUME_PERCENT);
        int weightBoost = WEIGHT_BOOST_AMOUNT + consumed;
        int attackBoost = ATTACK_BOOST_AMOUNT + consumed / 2;

        context.addPopups(List.of(
            EffectPopup.scaled("-30% GAINS", EffectPopup.Style.GAINS, consumed, POPUP_MAX_CONSUMED),
            EffectPopup.scaled("ATTAQUE +" + attackBoost, EffectPopup.Style.ATTACK, attackBoost, POPUP_MAX_ATTACK),
            EffectPopup.scaled("BOOST SYMBOLE +" + weightBoost, EffectPopup.Style.SPECIAL, weightBoost, POPUP_MAX_BOOST)
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
