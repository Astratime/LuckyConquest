package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Fût de poudre (la Taverne) : l'attaque du tirage est multipliée, et
 * l'explosion coûte tout de suite une part des PV max (sans tuer).
 */
public class PowderKegEffect extends Effect {

    private final int factor;
    private final int hpPercent;

    /**
     * @param factor    multiplicateur de l'attaque du tirage
     * @param hpPercent PV perdus à la pose, en % des PV max
     */
    public PowderKegEffect(int factor, int hpPercent) {
        this.factor    = factor;
        this.hpPercent = hpPercent;
    }

    @Override
    public void onPlay(PlayContext context) {
        context.queueForSpin(this);
        context.addPopups(getPopups());
        int lost = context.sacrificeHpPercent(hpPercent);
        if (lost > 0) context.addPopups(List.of(new EffectPopup("PV -" + lost, EffectPopup.Style.DAMAGE,
            PopupScale.SECONDARY_INTENSITY)));
    }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().multiplyAttack(factor); }

    @Override
    public String getDescription() { return "Attaque x" + factor + " ce tour. L'explosion te retire " + hpPercent + " % de tes PV."; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("BOUM ! ATTAQUE x" + factor, EffectPopup.Style.ATTACK, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ATTACK; }
}
