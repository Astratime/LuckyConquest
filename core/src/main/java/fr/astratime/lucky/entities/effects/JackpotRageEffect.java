package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Rage du jackpot (carte du Jackpot vivant) : la Paire est plus probable, et une Paire multiplie l'attaque et les gains du tirage.
 */
public class JackpotRageEffect extends Effect {
    private final int pairPercent;
    private final int factor;

    /** @param pairPercent chance de Paire en plus, en % ; @param factor attaque et gains sur une Paire (x) */
    public JackpotRageEffect(int pairPercent, int factor) { this.pairPercent = pairPercent; this.factor = factor; }

    @Override
    public void apply(TurnContext context) {
        context.getSpinContext().addPairChance(pairPercent / 100f);
        context.getCombatContext().addJackpotRage(factor);
    }

    @Override
    public String getDescription() { return Lang.f("+{0} % de chance d'avoir une Paire. Sur une Paire : attaque et gains du tour x{1}.", pairPercent, factor); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("RAGE DU JACKPOT"), EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.MAGNET; }
}
