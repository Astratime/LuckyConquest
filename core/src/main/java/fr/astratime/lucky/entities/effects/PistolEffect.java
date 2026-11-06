package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Pistolet de la Roulette russe : après le tirage, rejoue le coup le plus fort
 * du tour (après tous ses multiplicateurs), multiplié (x4, x3 avec le Joker maudit).
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
        if (percent > 100 && percent % 100 == 0) {
            return Lang.f("Pistolet : coup le plus fort du tour x{0}", percent / 100);
        }
        return percent == 100 ? Lang.t("Pistolet : rejoue le coup le plus fort du tour")
            : Lang.f("Pistolet : rejoue le coup le plus fort du tour à {0}%", percent);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup(Lang.t("PISTOLET CHARGÉ !"), EffectPopup.Style.ATTACK, PopupScale.MAX_INTENSITY),
            new EffectPopup(shotLabel(percent), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    /** @return « MEILLEUR COUP x4 » (multiple de 100 %), « MEILLEUR COUP REJOUÉ » ou « ... À 80% ». */
    public static String shotLabel(int percent) {
        if (percent > 100 && percent % 100 == 0) return Lang.f("MEILLEUR COUP x{0}", percent / 100);
        return percent == 100 ? Lang.t("MEILLEUR COUP REJOUÉ") : Lang.f("MEILLEUR COUP REJOUÉ À {0}%", percent);
    }

    @Override
    public EffectSound getSound() { return EffectSound.PISTOL; }
}
