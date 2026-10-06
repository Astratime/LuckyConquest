package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Porte-bonheur : tous les gains du tirage sont augmentés jusqu'à la fin du combat. */
public class LuckyCharmEffect extends Effect {

    private final int percent;

    /** @param percent bonus de gains (en %) pour tout le combat. */
    public LuckyCharmEffect(int percent) { this.percent = percent; }

    /** Le bonus dure tout le combat : il est enregistré tout de suite (voir PreparationResolver). */
    @Override
    public void onPlay(PlayContext context) {
        context.getLastingEffects().addGainBonus(percent / 100f);
        context.addPopups(getPopups());
    }

    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return Lang.f("Gains +{0}% pendant tout le combat", percent); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup(Lang.t("PORTE-BONHEUR"), EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY),
            new EffectPopup(Lang.f("GAINS +{0}% (COMBAT)", percent), EffectPopup.Style.GAINS, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.LUCKY_CHARM; }
}
