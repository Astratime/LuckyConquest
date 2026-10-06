package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Coffre-fort ouvert : les gains mis de côté reviennent, doublés. */
public class SafeOpenedEvent extends GainsEarnedEvent {

    /** @param amount gains rendus (déjà doublés) */
    public SafeOpenedEvent(int amount) { super(amount); }

    @Override
    public String describe() { return "Coffre-fort ouvert : +" + amount + " gains"; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup(Lang.t("COFFRE-FORT OUVERT !"), EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY),
            EffectPopup.scaled(Lang.f("GAINS +{0}", amount), EffectPopup.Style.GAINS, amount, PopupScale.SPIN_GAINS));
    }
}
