package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.LastingEffects;
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
 * As de Carreau, carte offensive (Astra 2026-11-04) : un symbole d'attaque de la
 * machine devient plus fréquent et tout le Coffre est vidé en bonus d'attaque, à
 * la manière du Sang de l'As de Coeur. Coffre plein ({@link LastingEffects#MAX_VAULT}) :
 * +15 000 d'attaque. L'ancien renvoi de 500 % et la contre-attaque sont supprimés.
 */
public class AceOfDiamondsEffect extends Effect {

    /** Coffre à vider pour 1 point d'attaque (150 000 de Coffre : +15 000). */
    static final int            VAULT_PER_ATTACK = 10;
    /** Boost de poids appliqué au symbole d'attaque choisi au hasard. */
    private static final int    BOOST_AMOUNT     = 200;
    private static final Random RANDOM           = new Random();

    /** Puissance de la carte : 1, ou {@link fr.astratime.lucky.entities.Card#UPGRADE_FACTOR} pour sa version « + ». */
    private final float power;

    public AceOfDiamondsEffect() { this(1f); }

    /** @param power puissance : le boost de symbole est multiplié par autant (version « + » de la carte) */
    public AceOfDiamondsEffect(float power) { this.power = power; }

    @Override
    public void apply(TurnContext context) {
        CombatContext combat = context.getCombatContext();
        List<Symbol> attackSymbols = SymbolRegistry.getAttackSymbols(combat.getPlayer()); // ceux de sa machine
        if (!attackSymbols.isEmpty()) {
            Symbol target = attackSymbols.get(RANDOM.nextInt(attackSymbols.size()));
            int boost = Math.round(BOOST_AMOUNT * power);
            context.getSpinContext().addWeightBoost(target, boost);
            context.addEvent(new SymbolBoostedEvent(target, boost));
        }

        int vault  = combat.getPlayer().getLastingEffects().consumeVault();
        int attack = vault / VAULT_PER_ATTACK;
        combat.addAttackBonus(attack, "Coffre");
        context.addEvent(new CardBonusEvent(Lang.f("Coffre : attaque +{0}", attack), List.of(
            new EffectPopup(Lang.f("COFFRE : ATTAQUE +{0}", attack), EffectPopup.Style.ATTACK, PopupScale.MAX_INTENSITY))));
    }

    @Override
    public String getDescription() {
        return Lang.f("Un symbole d'attaque devient plus fréquent (+{2}). Vide le Coffre : +1 d'attaque pour {0} de Coffre "
            + "(+{1} au plus, Coffre plein)", VAULT_PER_ATTACK, Lang.big(LastingEffects.MAX_VAULT / VAULT_PER_ATTACK), Math.round(BOOST_AMOUNT * power));
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("COFFRE OUVERT !"), EffectPopup.Style.ATTACK, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ACE_OF_DIAMONDS; }
}
