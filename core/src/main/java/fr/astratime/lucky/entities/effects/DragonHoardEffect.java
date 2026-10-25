package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Magot du dragon (archétype Lutin, coffre du Volcan) : pendant quelques tirages, chaque coup qui
 * touche rapporte des gains et un peu d'attaque (voir {@link fr.astratime.lucky.controllers.CombatResolver}).
 */
public class DragonHoardEffect extends Effect {

    private final int turns;
    private final int spins;
    private final int step;
    private final int max;

    /**
     * @param turns tirages sous le Magot, celui-ci compris
     * @param spins gains de chaque coup, en coûts de tirage
     * @param step  attaque en plus par coup, en %
     * @param max   attaque en plus au plus, en %
     */
    public DragonHoardEffect(int turns, int spins, int step, int max) {
        this.turns = turns;
        this.spins = spins;
        this.step  = step;
        this.max   = max;
    }

    @Override
    public void onPlay(PlayContext context) {
        context.getLastingEffects().addHoard(turns, spins, step, max);
        context.addPopups(getPopups());
    }

    /** Rien de plus au tirage : le Magot agit tant qu'il dure. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() {
        return Lang.f("Pendant {0} tours, chaque coup qui touche te rapporte {1} tirages de gains et +{2} % d'attaque (+{3} % au plus).",
            turns, spins, step, max);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("MAGOT DU DRAGON"), EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.GAIN; }
}
