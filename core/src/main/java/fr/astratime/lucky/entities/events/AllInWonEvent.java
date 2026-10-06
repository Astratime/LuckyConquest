package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Tapis gagné : une paire est sortie, les gains du joueur sont triplés. */
public class AllInWonEvent extends GainsEarnedEvent {

    /** @param amount gains crédités par le Tapis */
    public AllInWonEvent(int amount) { super(amount); }

    @Override
    public String describe() { return "Tapis gagne : gains x3 (+" + amount + ")"; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup(Lang.t("TAPIS GAGNÉ ! GAINS x3"), EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY),
            EffectPopup.scaled(Lang.f("GAINS +{0}", amount), EffectPopup.Style.GAINS, amount, PopupScale.SPIN_GAINS));
    }
}
