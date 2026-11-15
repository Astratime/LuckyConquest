package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.WearCards;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Bougie (carte à usure, demande d'Astra du 2026-11-14) : un gros bonus d'attaque, qui perd une part à chaque
 * fois qu'elle est jouée, jusqu'à la fin du combat (la cire fond).
 */
public class CandleEffect extends Effect {

    private final int percent;
    private final int wear;

    /**
     * @param percent attaque en plus à la première pose, en %
     * @param wear    part du bonus perdue à chaque pose, en %
     */
    public CandleEffect(int percent, int wear) {
        this.percent = percent;
        this.wear    = wear;
    }

    @Override
    public void onPlay(PlayContext context) {
        int bonus = context.getLastingEffects().getWearCards().burnCandle(percent, wear);
        context.queueForSpin(new WornAttack(bonus, "Bougie"));
        context.addPopups(List.of(EffectPopup.scaled(Lang.f("ATTAQUE +{0} %", bonus), EffectPopup.Style.ATTACK, bonus,
            PopupScale.CARD_ATTACK_BONUS * 2),
            new EffectPopup(Lang.f("LA BOUGIE FOND : +{0} % ENSUITE", context.getLastingEffects().getWearCards().getNextCandle()),
                EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY)));
    }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().multiplyAttack(1f + percent / 100f, "Bougie"); }

    @Override
    public String getDescription() {
        return Lang.f("Attaque +{0} %. Chaque fois qu'elle est jouée, ce bonus perd {1} % jusqu'à la fin du combat.",
            percent, wear);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(EffectPopup.scaled(Lang.f("ATTAQUE +{0} %", percent), EffectPopup.Style.ATTACK, percent,
            PopupScale.CARD_ATTACK_BONUS * 2));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ATTACK; }
}
