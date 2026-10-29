package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Sceau du Pharaon (archétype Rentier, coffre du Tombeau) : des Créances sur l'ennemi, qui comptent
 * double pendant ses prochains tours (voir {@link fr.astratime.lucky.entities.Enemy#pharaohSeal}).
 */
public class PharaohSealEffect extends Effect {

    private final int claims;
    private final int turns;

    /**
     * @param claims Créances posées
     * @param turns  tours de l'ennemi où ses Créances comptent double
     */
    public PharaohSealEffect(int claims, int turns) {
        this.claims = claims;
        this.turns  = turns;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().getEnemy().getMaluses().addClaims(claims);
        context.getCombatContext().getEnemy().getMaluses().pharaohSeal(turns);
    }

    @Override
    public String getDescription() {
        return Lang.f("+{0} Créances sur l'ennemi. Pendant ses {1} prochains tours, ses Créances comptent double.", claims, turns);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("CRÉANCES +{0}", claims), EffectPopup.Style.DRAIN, PopupScale.MAX_INTENSITY),
            new EffectPopup(Lang.t("SCEAU DU PHARAON"), EffectPopup.Style.DRAIN, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.GAINS_MULTIPLIER; }
}
