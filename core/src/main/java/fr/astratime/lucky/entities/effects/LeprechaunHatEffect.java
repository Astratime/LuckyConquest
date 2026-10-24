package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Chapeau du Lutin (archétype Lutin) : pendant quelques tours, chaque coup qui touche l'ennemi
 * rapporte des gains (voir {@link fr.astratime.lucky.controllers.CombatResolver}).
 */
public class LeprechaunHatEffect extends Effect {

    private final int turns;
    private final int percent;

    /**
     * @param turns   tirages sous le Chapeau, celui-ci compris
     * @param percent gains de chaque coup, en % d'un coût de tirage
     */
    public LeprechaunHatEffect(int turns, int percent) {
        this.turns   = turns;
        this.percent = percent;
    }

    @Override
    public void onPlay(PlayContext context) {
        context.getLastingEffects().addHat(turns, percent);
        context.addPopups(getPopups());
    }

    /** Rien de plus au tirage : le Chapeau agit tant qu'il dure. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() {
        return Lang.f("Pendant {0} tours, chaque coup qui touche l'ennemi te rapporte {1} % d'un tirage en gains.", turns, percent);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("CHAPEAU DU LUTIN : COUPS = GAINS"), EffectPopup.Style.GAINS,
            PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.LUCKY_CHARM; }
}
