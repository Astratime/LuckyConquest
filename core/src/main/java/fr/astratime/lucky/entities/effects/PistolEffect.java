package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Pistolet de la Roulette russe : après le tirage, rejoue le coup le plus fort
 * du tour (après tous ses multiplicateurs), en entier ou en partie (Joker maudit).
 */
public class PistolEffect extends Effect {

    private final int percent;

    /** @param percent part (%) du coup le plus fort du tour que le pistolet rejoue. */
    public PistolEffect(int percent) { this.percent = percent; }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addPistolShot(percent);
    }

    @Override
    public String getDescription() {
        return percent >= 100 ? Lang.t("Pistolet : rejoue le coup le plus fort du tour")
            : Lang.f("Pistolet : rejoue le coup le plus fort du tour à {0}%", percent);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup(Lang.t("PISTOLET CHARGÉ !"), EffectPopup.Style.ATTACK, PopupScale.MAX_INTENSITY),
            new EffectPopup(percent >= 100 ? Lang.t("MEILLEUR COUP REJOUÉ")
                : Lang.f("MEILLEUR COUP REJOUÉ À {0}%", percent), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.PISTOL; }
}
