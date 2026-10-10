package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolRegistry;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.events.CardBonusEvent;
import fr.astratime.lucky.entities.events.SymbolBoostedEvent;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
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

    /** Puissance de la carte : 1, ou {@link fr.astratime.lucky.entities.Card#UPGRADE_FACTOR} pour sa version « + ». */
    private final float power;

    public AceOfHeartsEffect() { this(1f); }

    /** @param power puissance : ses valeurs sont multipliées par autant (version « + » de la carte) */
    public AceOfHeartsEffect(float power) { this.power = power; }

    @Override
    public void apply(TurnContext context) {
        List<Symbol> attackSymbols = SymbolRegistry.getAttackSymbols(context.getCombatContext().getPlayer()); // ceux de sa machine
        if (!attackSymbols.isEmpty()) {
            Symbol target = attackSymbols.get(RANDOM.nextInt(attackSymbols.size()));
            int boost = Math.round(BOOST_AMOUNT * power);
            context.getSpinContext().addWeightBoost(target, boost);
            context.addEvent(new SymbolBoostedEvent(target, boost));
        }

        CombatContext combat = context.getCombatContext();
        Player player = combat.getPlayer();
        float factor = 1f + FRENZY_PER_MISSING_HP * power * (1f - player.getHpRatio());
        int   blood  = player.getLastingEffects().consumeBlood();
        combat.multiplyAttack(factor, "As de Coeur");
        combat.addAttackBonus(blood, "Sang");
        String times = String.valueOf(Math.round(factor * 10f) / 10f);
        context.addEvent(new CardBonusEvent(Lang.f("Frenesie : attaque x{0}, sang +{1}", times, blood), List.of(
            new EffectPopup(Lang.f("FRÉNÉSIE : ATTAQUE x{0}", times), EffectPopup.Style.DRAIN, PopupScale.MAX_INTENSITY),
            new EffectPopup(Lang.f("SANG : ATTAQUE +{0}", blood), EffectPopup.Style.DRAIN, PopupScale.SECONDARY_INTENSITY))));
    }

    @Override
    public String getDescription() {
        return Lang.f("Frénésie : un symbole d'attaque devient plus fréquent. Attaque multipliée jusqu'à x{0} selon la "
            + "vie perdue. Tout le Sang s'ajoute à l'attaque",
            Math.round(1 + FRENZY_PER_MISSING_HP * power));
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup(Lang.t("FRÉNÉSIE !"), EffectPopup.Style.DRAIN, PopupScale.MAX_INTENSITY)
        );
    }

    @Override
    public EffectSound getSound() { return EffectSound.ACE_OF_HEARTS; }
}
