package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Gigue du Lutin (archétype Lutin) : les gains gagnés ce tour donnent des Lames, avec un plafond.
 */
public class LeprechaunJigEffect extends Effect {
    private final int spinsPerBlade;
    private final int maxBlades;

    /**
     * Gigue du Lutin.
     * @param spinsPerBlade coûts de tirage gagnés pour une Lame
     * @param maxBlades Lames au plus
     */
    public LeprechaunJigEffect(int spinsPerBlade, int maxBlades) {
        this.spinsPerBlade = spinsPerBlade;
        this.maxBlades = maxBlades;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addJig(spinsPerBlade, maxBlades);
    }

    @Override
    public String getDescription() { return Lang.f("Ce tour, +1 Lame par {0} tirages de gains gagnés, {1} Lames au plus.", spinsPerBlade, maxBlades); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("GIGUE DU LUTIN : GAINS = LAMES"), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.SPADE_IGNORE_DEFENSE; }

}
