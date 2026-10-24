package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Coup de bluff (archétype Bluffeur) : plus d'attaque pour chaque malus qui gêne l'ennemi
 * (voir {@link fr.astratime.lucky.entities.Enemy#getActiveMaluses()}).
 */
public class BluffCallEffect extends Effect {
    private final int percent;
    private final int maxPercent;

    /**
     * Coup de bluff.
     * @param percent attaque en plus par malus, en %
     * @param maxPercent attaque en plus au plus, en %
     */
    public BluffCallEffect(int percent, int maxPercent) {
        this.percent = percent;
        this.maxPercent = maxPercent;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addBluffCall(percent, maxPercent);
    }

    @Override
    public String getDescription() { return Lang.f("Attaque +{0} % par malus qui gêne l'ennemi, {1} % au plus.", percent, maxPercent); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("COUP DE BLUFF"), EffectPopup.Style.ATTACK, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ATTACK; }

}
