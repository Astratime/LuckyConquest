package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Machine en surchauffe : un 4e rouleau tourne ce tour ; le joueur perd une part de ses PV max à la pose. */
public class OverheatEffect extends Effect {

    private final int hpPercent;

    /** @param hpPercent PV perdus à la pose, en % des PV max */
    public OverheatEffect(int hpPercent) { this.hpPercent = hpPercent; }

    @Override
    public void onPlay(PlayContext context) {
        context.sacrificeHpPercent(hpPercent);
        super.onPlay(context);
    }

    @Override
    public void apply(TurnContext context) {
        context.getSpinContext().addExtraReel();
    }

    @Override
    public String getDescription() {
        return "4 symboles tirés ce tour au lieu de 3. Tu perds " + hpPercent + " % de tes PV.";
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup("SURCHAUFFE : 4 ROULEAUX", EffectPopup.Style.ATTACK, PopupScale.MAX_INTENSITY),
            new EffectPopup("PV -" + hpPercent + "%", EffectPopup.Style.DAMAGE, PopupScale.SECONDARY_INTENSITY));
    }

    /** Un seul 4e rouleau : doublée, la carte coûterait des PV pour rien. */
    @Override
    public boolean canBeDoubled() { return false; }

    @Override
    public EffectSound getSound() { return EffectSound.OVERHEAT; }
}
