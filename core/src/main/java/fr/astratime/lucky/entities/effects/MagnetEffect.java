package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Aimant : un tirage sans paire a une chance de devenir une paire. */
public class MagnetEffect extends Effect {

    private final int percent;

    /** @param percent chance (en %) de transformer un tirage sans paire en paire. */
    public MagnetEffect(int percent) { this.percent = percent; }

    @Override
    public void apply(TurnContext context) {
        context.getSpinContext().addPairChance(percent / 100f);
    }

    @Override
    public String getDescription() { return Lang.f("Aimant : +{0}% de chance d'obtenir une paire", percent); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup(Lang.f("AIMANT : PAIRE +{0}%", percent), EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.MAGNET; }
}
