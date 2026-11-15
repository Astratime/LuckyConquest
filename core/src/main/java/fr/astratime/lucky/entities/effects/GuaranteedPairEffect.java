package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Roue lestée (carte de la Roue de la fortune) : le tirage a une Paire.
 */
public class GuaranteedPairEffect extends Effect {
    private final int percent;

    /** @param percent chance qu'un tirage sans paire en devienne une, en % */
    public GuaranteedPairEffect(int percent) { this.percent = percent; }

    @Override
    public void apply(TurnContext context) { context.getSpinContext().addPairChance(percent / 100f); }

    @Override
    public String getDescription() { return (percent >= 100 ? Lang.t("Ce tour, ton tirage a au moins une Paire.") : Lang.f("Ce tour, +{0} % de chance d'avoir une Paire.", percent)); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("ROUE LESTÉE : PAIRE"), EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.MAGNET; }
}
