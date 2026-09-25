package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolRegistry;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.events.CardBonusEvent;
import fr.astratime.lucky.entities.events.SymbolBoostedEvent;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;
import java.util.Random;

/**
 * As de Coeur, « Frénésie » : boost de probabilité sur un symbole d'attaque
 * choisi au hasard ; l'attaque du tour est multipliée d'autant plus que le
 * joueur est blessé, et tout le Sang est encaissé en attaque.
 */
public class AceOfHeartsEffect extends Effect {

    /** Boost de poids appliqué au symbole d'attaque choisi au hasard. */
    private static final int    BOOST_AMOUNT = 200;
    /** Multiplicateur ajouté par proportion de vie manquante (x3 à 0 PV). */
    static final float          FRENZY_PER_MISSING_HP = 2f;
    private static final Random RANDOM       = new Random();

    @Override
    public void apply(TurnContext context) {
        List<Symbol> attackSymbols = SymbolRegistry.getAttackSymbols();
        if (!attackSymbols.isEmpty()) {
            Symbol target = attackSymbols.get(RANDOM.nextInt(attackSymbols.size()));
            context.getSpinContext().addWeightBoost(target, BOOST_AMOUNT);
            context.addEvent(new SymbolBoostedEvent(target, BOOST_AMOUNT));
        }

        CombatContext combat = context.getCombatContext();
        Player player = combat.getPlayer();
        float factor = 1f + FRENZY_PER_MISSING_HP * (1f - player.getHpRatio());
        int   blood  = player.getLastingEffects().consumeBlood();
        combat.multiplyAttack(factor);
        combat.addAttackBonus(blood);
        String times = String.valueOf(Math.round(factor * 10f) / 10f);
        context.addEvent(new CardBonusEvent("Frenesie : attaque x" + times + ", sang +" + blood, List.of(
            new EffectPopup("FRÉNÉSIE : ATTAQUE x" + times, EffectPopup.Style.DRAIN, PopupScale.MAX_INTENSITY),
            new EffectPopup("SANG : ATTAQUE +" + blood, EffectPopup.Style.DRAIN, PopupScale.SECONDARY_INTENSITY))));
    }

    @Override
    public String getDescription() {
        return "Frenesie : boost d'un symbole d'attaque ; attaque x(1 + 2 x vie manquante) ; tout le Sang devient de l'attaque";
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup("FRÉNÉSIE !", EffectPopup.Style.DRAIN, PopupScale.MAX_INTENSITY)
        );
    }
}
