package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.ReelGauges;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Soupape et Chaudière ardente (coffres du Rapide de Minuit) : la Vapeur monte tout de suite à sa
 * pression la plus haute ({@link ReelGauges#STEAM_MAX} crans), comme si elle était sortie à chaque
 * tirage d'avant. Elle retombe toujours au premier tirage sans Vapeur.
 */
public class SteamValveEffect extends Effect {

    @Override
    public void onPlay(PlayContext context) {
        context.getLastingEffects().getReelGauges().fillSteam();
        context.addPopups(getPopups());
    }

    /** Rien de plus au tirage. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() {
        return Lang.f("La Vapeur monte tout de suite à sa pression la plus haute (x{0}).",
            1 + ReelGauges.STEAM_STEP_PERCENT * ReelGauges.STEAM_MAX / 100);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("VAPEUR À FOND"), EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.BOOST_SYMBOL; }
}
