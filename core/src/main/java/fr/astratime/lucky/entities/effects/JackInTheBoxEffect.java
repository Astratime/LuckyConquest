package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Diable en boîte (archétype Joker, coffre du Train fantôme) : chaque Joker sorti à ce tirage
 * donne du Joker en plus jusqu'à la fin du combat (voir {@link fr.astratime.lucky.controllers.CombatResolver}).
 */
public class JackInTheBoxEffect extends Effect {

    private final int step;
    private final int max;

    /**
     * @param step Joker en plus par Joker sorti
     * @param max  Joker en plus au plus, sur tout le combat
     */
    public JackInTheBoxEffect(int step, int max) {
        this.step = step;
        this.max  = max;
    }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().addJackInTheBox(step, max); }

    @Override
    public String getDescription() {
        return Lang.f("Chaque Joker sorti à ce tirage donne +{0} Joker jusqu'à la fin du combat (+{1} au plus).", step, max);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("DIABLE EN BOÎTE"), EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.BOOST_SYMBOL; }
}
