package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Rouleau doré (échoppe) : ce tour, le symbole du rouleau du milieu vaut
 * {@code factor} fois plus (dégâts, bouclier, gains).
 */
public class GoldenReelEffect extends Effect {

    /** Le rouleau du milieu, sur trois. */
    public static final int MIDDLE_REEL = 1;

    private final int factor;

    /** @param factor multiplicateur de la valeur du symbole du rouleau du milieu */
    public GoldenReelEffect(int factor) { this.factor = factor; }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().multiplySlotPower(MIDDLE_REEL, factor);
    }

    @Override
    public String getDescription() {
        return Lang.f("Ce tour, le symbole du rouleau du milieu vaut x{0} (attaque, bouclier et gains)", factor);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup(Lang.t("ROULEAU DORÉ !"), EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY),
            new EffectPopup(Lang.f("ROULEAU DU MILIEU x{0}", factor), EffectPopup.Style.SPECIAL,
                PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.RIGGED_REEL; }
}
