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
 * Maillet de gong (cartes du Temple du Destin) : à ce tirage, chaque Gong qui sort étourdit à coup
 * sûr un rouleau de l'ennemi (au lieu de {@link ReelGauges#GONG_STUN_PERCENT} % des fois).
 */
public class GongMalletEffect extends Effect {

    @Override
    public void onPlay(PlayContext context) {
        context.getLastingEffects().getReelGauges().sureGong();
        context.addPopups(getPopups());
    }

    /** Rien de plus au tirage. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() {
        return Lang.t("Ce tour, le Gong étourdit à coup sûr un rouleau de l'ennemi.");
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("GONG ASSURÉ"), EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.BOOST_SYMBOL; }
}
