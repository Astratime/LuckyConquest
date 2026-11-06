package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Prime de chasse : l'ennemi est tombé ce tour, les gains du tour reviennent une fois de plus. */
public class BountyEvent extends GainsEarnedEvent {

    /** @param amount gains versés par la prime */
    public BountyEvent(double amount) { super(amount); }

    @Override
    public String describe() { return "Prime de chasse : +" + Lang.big(amount) + " gains"; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup(Lang.t("PRIME DE CHASSE !"), EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY),
            EffectPopup.scaled(Lang.f("GAINS +{0}", Lang.big(amount)), EffectPopup.Style.GAINS, amount, PopupScale.SPIN_GAINS));
    }
}
