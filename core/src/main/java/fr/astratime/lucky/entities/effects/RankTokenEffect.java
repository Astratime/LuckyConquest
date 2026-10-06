package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Jeton de rang : le bonus du rang (attaque, défense, gains) compte double ce tour. */
public class RankTokenEffect extends Effect {

    /** Multiplicateur du bonus du rang. */
    public static final int FACTOR = 2;

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().multiplyRankBonus(FACTOR);
    }

    @Override
    public String getDescription() { return Lang.t("Le bonus de ton rang compte double ce tour."); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("BONUS DE RANG x{0}", FACTOR), EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.RANK_TOKEN; }
}
