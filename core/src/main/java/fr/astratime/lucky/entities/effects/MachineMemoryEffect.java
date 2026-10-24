package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Mémoire de la machine (archétype Relanceur) : le symbole le plus sorti au dernier tirage revient plus souvent.
 */
public class MachineMemoryEffect extends Effect {

    private final int amount;

    /** @param amount boost de poids du symbole le plus sorti au dernier tirage */
    public MachineMemoryEffect(int amount) { this.amount = amount; }

    @Override
    public void onPlay(PlayContext context) {
        context.queueForSpin(this);
        Symbol remembered = remembered(context.getLastingEffects().getLastDrawn());
        context.addPopups(remembered == null ? getPopups() : List.of(EffectPopup.scaled(
            Lang.f("MÉMOIRE : {0} +{1}", remembered.getDisplayName(), amount), EffectPopup.Style.SPECIAL, amount,
            PopupScale.CARD_SYMBOL_BOOST)));
    }

    @Override
    public void apply(TurnContext context) {
        Symbol remembered = remembered(context.getCombatContext().getPlayer().getLastingEffects().getLastDrawn());
        if (remembered != null) context.getSpinContext().addWeightBoost(remembered, amount);
    }

    /**
     * @return le symbole sorti le plus de fois parmi {@code drawn} (ni Joker ni rouleau vide ; à égalité,
     *         le plus à gauche), ou {@code null} s'il n'y en a pas
     */
    static Symbol remembered(Symbol[] drawn) {
        if (drawn == null) return null;
        Symbol best = null;
        int bestCount = 0;
        for (Symbol symbol : drawn) {
            if (symbol == null || symbol == Symbol.JOKER) continue;
            int count = 0;
            for (Symbol other : drawn) if (other == symbol) count++;
            if (count > bestCount) {
                best = symbol;
                bestCount = count;
            }
        }
        return best;
    }

    @Override
    public String getDescription() {
        return Lang.f("Le symbole le plus sorti au dernier tirage est plus fréquent (+{0}).", amount);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("MÉMOIRE DE LA MACHINE"), EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.BOOST_SYMBOL; }
}
