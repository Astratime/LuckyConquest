package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Corde de rappel (le Puits) : le prochain coup qui devrait tuer le joueur
 * le laisse à 1 PV. Une fois par combat.
 */
public class RopeEffect extends Effect {

    @Override
    public void onPlay(PlayContext context) {
        context.addPopups(context.addRope() ? getPopups()
            : List.of(new EffectPopup(Lang.t("CORDE DÉJÀ UTILISÉE"), EffectPopup.Style.DAMAGE, PopupScale.SECONDARY_INTENSITY)));
    }

    /** Rien de plus au tirage. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return Lang.t("Si un coup devait te tuer, tu restes à 1 PV. Une fois par combat."); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("CORDE DE RAPPEL"), EffectPopup.Style.DEFENSE, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public boolean canBeDoubled() { return false; }

    @Override
    public EffectSound getSound() { return EffectSound.INSURANCE; }
}
