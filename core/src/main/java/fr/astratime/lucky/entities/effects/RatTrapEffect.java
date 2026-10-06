package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Piège à rats (la Cale) : le prochain mauvais sort de l'ennemi sur la main
 * (Grignotage, Aveuglement, Chant, Abordage, Fouille) est annulé.
 */
public class RatTrapEffect extends Effect {

    @Override
    public void onPlay(PlayContext context) {
        context.getLastingEffects().addTrap();
        context.addPopups(getPopups());
    }

    /** Rien de plus au tirage. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return Lang.t("Pose un piège. Le prochain mauvais sort de l'ennemi sur ta main est annulé."); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("PIÈGE POSÉ"), EffectPopup.Style.DEFENSE, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.INSURANCE; }
}
